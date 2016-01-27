/**
 * e-Science Central
 * Copyright (C) 2008-2015 School of Computing Science, Newcastle University
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation at:
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, 5th Floor, Boston, MA 02110-1301, USA.
 */
package com.connexience.scheduler.beans;

import com.connexience.performance.model.WorkflowEngineInstance;
import com.connexience.scheduler.*;
import com.connexience.scheduler.api.InvocationSchedulerLocal;
import com.connexience.scheduler.api.IDispatcher;
import com.connexience.scheduler.impl.CPULoadDispatcher;
import com.connexience.scheduler.jboss.JBossASConnectionFactory;
import com.connexience.scheduler.model.*;

import com.connexience.server.ConnexienceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Perf;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;
import javax.jms.Queue;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This is the main component of the scheduler which coordinates scheduling tasks. This component is a singleton which
 * includes a thread of execution.
 *
 * The main use of this bean is to request and release an allocation of resources.
 *
 * Created by Jacek on 14/12/2015.
 */
@Startup
@Singleton
@EJB(name = "java:global/ejb/scheduler/InvocationSchedulerBean", beanInterface = InvocationSchedulerLocal.class)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class InvocationSchedulerBean extends TimerTask implements InvocationSchedulerLocal
{
    private static final Logger _Logger = LoggerFactory.getLogger(InvocationSchedulerBean.class);

    private static final long DISPATCH_INTERVAL_IN_MILLIS = 500;

    /** Monitor engines' status with 2 min interval */
    private static final long MONITOR_INTERVAL_IN_SECONDS = 60;


    @Inject
    @JBossASConnectionFactory
    private ConnectionFactory _connectionFactory;


    /** These are uncommitted requests to the scheduler which will be processed on the next interval */
    private final LinkedBlockingQueue<ResourceAllocationRequest> _requests = new LinkedBlockingQueue<>();

    /**
     * These are committed allocations which need to be released at some point. Maps { requestId -> allocationRequest }
     */
    private final HashMap<String, CommittedResourceAllocation> _committedResources = new HashMap<>();

    /**
     * These are compute nodes involved in resource allocations. Maps { requestId -> nodeId }.
     *
     * The invariant is allocatedNodes.keySet().equals(committedResources.keySet())
     */
    //private final HashMap<ResourceAllocationRequest, ComputeNode> _allocatedNodes = new HashMap<>();

    private final HashMap<String, ComputeNode> _availableNodes = new HashMap<>();
    private final HashMap<String, String> _registrationMap = new HashMap<>();

    private final ScheduledThreadPoolExecutor _wakeUpTimer;

    private IDispatcher _invocationDispatcher;


    public InvocationSchedulerBean()
    {
        _Logger.info("A scheduler instance has been created.");

        _invocationDispatcher = new CPULoadDispatcher();
        // TODO: Consider using a semaphore to optimise the work of the scheduler by avoiding unnecessary wake ups.
        _wakeUpTimer = new ScheduledThreadPoolExecutor(1);
    }


    @PostConstruct
    public void init()
    {
        //_wakeUpTimer.schedule(this, INTERVAL_IN_MILLIS);
        _wakeUpTimer.schedule(this, DISPATCH_INTERVAL_IN_MILLIS, TimeUnit.MILLISECONDS);
        _wakeUpTimer.schedule(new EngineMonitoringTask(), MONITOR_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);

        _Logger.info("The initial scheduling event has been scheduled.");
    }

    public String listEngines()
    {
        return "Many engines";
    }


    /**
     * This is the main activity of the Scheduler -- to dispatch request among available nodes;
     */
    @Override
    public void run()
    {
        try {
            // Get all allocation requests which the scheduler was asked to process
            ArrayList<ResourceAllocationRequest> currentRequestList = new ArrayList<>();
            if (_requests.drainTo(currentRequestList) > 0) {

                // Get a snapshot of the currently available nodes
                ArrayList<ComputeNode> currentNodes = new ArrayList<>();
                synchronized (_availableNodes) {
                    currentNodes.addAll(_availableNodes.values());
                }

                // FIXME: What about requests that cannot be allocated at all? For example due to strange resources requested?

                // Dispatch requests
                List<Map.Entry<ResourceAllocationRequest, ComputeNode>> allocationMap = _invocationDispatcher.dispatch(currentRequestList, currentNodes);
                for (Map.Entry<ResourceAllocationRequest, ComputeNode> entry : allocationMap) {
                    // Just for convenience
                    ResourceAllocationRequest req = entry.getKey();
                    ComputeNode node = entry.getValue();

                    try {
                        QueueManager.PushMessage(_connectionFactory, QueueManager.GetQueueNameForEngine(node.id), (Message) req.content);

                        // If push went ok, remove the request from the list and...
                        currentRequestList.remove(req);

                        // add it to the committed resource map.
                        synchronized (_committedResources) {
                            _committedResources.put(req.id, new CommittedResourceAllocation(req, new Date().getTime(), node));
                        }

                        // TODO: Some mechanism to deal with incomplete/failed invocations should be put it place here
                        // For example, what if an engine has been killed without letting the Scheduler know. It's
                        // queue may include some waiting invocations. Note that started invocations are already monitored
                        // by the server.

                    } catch (JMSException | NamingException x) {
                        // However, if push failed, we need to release resources allocated during dispatch
                        node.release(req.resourceAllocations);

                        // TODO: Two more issues if it's not possible to push the request to the node:
                        // 1. The request should be reallocated; perhaps during this event.
                        // 2. The node may no longer be valid and perhaps should be removed from available nodes?
                        _Logger.error("Cannot push request {} to node {}: {}", req.id, node.id, x);
                    }
                }

                // Re-add all requests that missed this dispatch event
                _requests.addAll(currentRequestList);
            }

            // Schedule next dispatching event
            _wakeUpTimer.schedule(this, DISPATCH_INTERVAL_IN_MILLIS, TimeUnit.MILLISECONDS);
        } catch (Exception x) {
            _Logger.error("Error in the scheduler's dispatch loop: " + x);
        }
    }


    @Override
    public void requestResources(ResourceAllocationRequest request)
    {
        _Logger.debug("Resource request: " + request.id);
        _requests.add(request);
    }


    @Override
    public void releaseResources(String requestId) throws ResourceNotAvailableException
    {
        CommittedResourceAllocation allocation;
        synchronized (_committedResources) {
            allocation = _committedResources.remove(requestId);
            if (allocation == null) {
                throw new ResourceNotAvailableException("Invalid request to release: " + requestId);
            }
        }

        allocation.node.release(allocation.request.resourceAllocations);
    }


    @Override
    public ResourceAllocationRequest[] listResourceRequests()
    {
        // Do not use _requests.size() to allocate the output array as the _requests queue may change its size just
        // before the .toArray(.) call, which may result in array larger than the actual size of the queue returned.
        return _requests.toArray(new ResourceAllocationRequest[0]);
    }


    @Override
    public String registerNode(ComputeNode node) throws AlreadyRegisteredException
    {
        if (node == null || node.id == null) {
            throw new IllegalArgumentException("Node to register cannot be null.");
        }

        String registrationId;

        synchronized (_availableNodes) {
            // FIXME: Can we allow node to be re-registred?
            if (_availableNodes.containsKey(node.id)) {
                _Logger.warn("Node {} has already been registered", node.id);
            //    throw new AlreadyRegisteredException("Node " + node.id + " has already been registered");
            }

            _availableNodes.put(node.id, node);

            registrationId = UUID.randomUUID().toString();
            _registrationMap.put(node.id, registrationId);
        }

        try {
            QueueManager.CreateQueue(QueueManager.GetQueueNameForEngine(node.id));
            return registrationId;
        } catch (IOException x) {
            _Logger.error("Cannot create queue for node: {}", node.id, x);
        }

        synchronized (_availableNodes) {
            _availableNodes.remove(node.id);
            _registrationMap.remove(node.id);
        }

        return null;
        // TODO: Think whether it is wise to fire the wake up timer immediately after the node registration...
        // Note, however, that there might be hundreds of registrations done in short time, e.g. during system startup.
    }


    @Override
    public void unregisterNode(String nodeId, String registrationId) throws ResourceNotAvailableException
    {
        // Note that unregistration can also take place if the node is currently processing some tasks. It should not
        // hurt, actually. It only means that the Scheduler will not consider this node during any future dispatch
        // events.

        synchronized (_availableNodes) {
            // FIXME: Can we allow registrationId null to unregister any node?
            if (registrationId != null) {
                String regId = _registrationMap.get(nodeId);

                if (regId == null || !regId.equals(registrationId)) {
                    throw new ResourceNotAvailableException("Cannot unregister node: " + nodeId + " invalid registration id");
                }
            }

            _availableNodes.remove(nodeId);
            _registrationMap.remove(nodeId);
        }

        try {
            QueueManager.DestroyQueue(QueueManager.GetQueueNameForEngine(nodeId));
        } catch (IOException x) {
            _Logger.warn("Unable to destroy queue for node: {}", nodeId, x);
        }
    }


    @Override
    public ComputeNode[] listRegisteredNodes()
    {
        synchronized (_availableNodes) {
            return _availableNodes.values().toArray(new ComputeNode[_availableNodes.size()]);
        }
    }


    /**
     * FIXME: Refresh details about nodes, but only these nodes which are not running anything.
     *
     * @param nodes
     */
    void refreshNodesRegistration(ComputeNode[] nodes) //, HashSet<String> freeNodes)
    {
        // First clean the availableNodes map of all inactive nodes
        //
        synchronized (_availableNodes) {
            // Use .toArray(...) to avoid concurrent map modification exceptions.
            int size = _availableNodes.size();
            for (String nodeId : _availableNodes.keySet().toArray(new String[size])) {
                boolean onTheList = false;
                for (ComputeNode node : nodes) {
                    if (nodeId.equals(node.id)) {
                        onTheList = true;
                        break;
                    }
                }

                if (!onTheList) {
                    _availableNodes.remove(nodeId);
                    _registrationMap.remove(nodeId);
                }
            }
        }

        // Second, update node resources but only for the nodes which are free (as perceived by the PerfMon) and have
        // no committed allocations (as perceived by the Scheduler).
        //
//        synchronized (_committedResources) {
//            for (ComputeNode freeNode : freeNodes) {
//                boolean isFree = true;
//                for (CommittedResourceAllocation alloc : _committedResources.values()) {
//                    if (freeNode.id.equals(alloc.node.id)) {
//                        isFree = false;
//                        break;
//                    }
//                }
//
//                if (isFree) {
//
//                }
//            }
//        }
    }


    // This is a helper class that monitors the status of engines in the performance monitor and removes engines which
    // are no longer registered in the PerfMon.
    // FIXME: The main issue with this class is that it breaks the overall generality approach of the scheduler and
    // works with WorkflowEngineInstance data. To think how this tight dependency may be loosen without much lose in
    // performance of this monitoring task.
    private class EngineMonitoringTask implements Runnable
    {
        /**
         * From the given list of engines return only these which are not running anything. Otherwise the resource
         * usage may be inaccurate.
         *
         * @param engines
         * @return
         */
        private HashSet<String> _filterBusy(WorkflowEngineInstance[] engines)
        {
            HashSet<String> freeEngines = new HashSet<>(engines.length);

            for (WorkflowEngineInstance engine : engines) {
                if (engine.getRunningWorkflowCount() == 0) {
                    freeEngines.add(engine.getIpAddress());
                }
            }

            return freeEngines;
        }


        @Override
        public void run() {
            try {
                WorkflowEngineInstance[] engineInfo = PerformanceClient.RefreshEngineInfo();
                refreshNodesRegistration(Utils.ToComputeNodes(engineInfo));//, _filterBusy(engineInfo));
            } catch (ConnexienceException x) {
                _Logger.warn("Unable to retrieve engine information from the performance monitor.", x);
            }

            // Schedule the next update event
            _wakeUpTimer.schedule(this, MONITOR_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
        }
    }
}
