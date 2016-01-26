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
package com.connexience.scheduler.impl;

import com.connexience.scheduler.ResourceNotAvailableException;
import com.connexience.scheduler.Utils;
import com.connexience.scheduler.api.IDispatcher;
import com.connexience.scheduler.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This is a very simple invocation dispatcher that tries to distribute allocations among available nodes such that
 * priority is given to nodes with the least loaded CPU.
 *
 * Note that this dispatcher does not try to build a schedule of tasks but merely distributes allocations up to
 * the resource limits. The requests which has not been matched with any of the nodes may be dispatched later on when
 * some compute resources become available.
 *
 * Created by Jacek on 17/12/2015.
 */
public class CPULoadDispatcher implements IDispatcher
{
    private static final Logger _Logger = LoggerFactory.getLogger(CPULoadDispatcher.class);

    public CPULoadDispatcher()
    { }


    /**
     * This is a simple method to read CPU load of a ComputeNode.
     *
     * @param node
     * @param minCpuLoadForNode
     * @return
     */
    int _getCPULoad(ComputeNode node, HashMap<ComputeNode, Integer> minCpuLoadForNode)
    {
        if (!minCpuLoadForNode.containsKey(node)) {
            ArrayList<Resource> cpuRes = node.getResourcesByType(Constants.ResourceType.CPU);
            if (cpuRes == null) {
                _Logger.debug("Node " + node.id + " has no resource " + Constants.ResourceType.CPU);
            }

            int minLoad = Integer.MAX_VALUE;
            if (cpuRes != null) {
                for (Resource r : cpuRes) {
                    SatisfierProperty p = r.getProperty(Constants.Property.CPU_LOAD);
                    if (p != null) {
                        Integer load = Utils.TryGetInteger(p.getValue());
                        if (load != null && load < minLoad) {
                            minLoad = load;
                        }
                    }
                }
            }

            if (minLoad == Integer.MAX_VALUE) {
                // Can't find out the real CPU load so lets assume 40%
                minCpuLoadForNode.put(node, 40);
                return 40;
            }
        }

        return minCpuLoadForNode.get(node);
    }


    @Override
    public List<Map.Entry<ResourceAllocationRequest, ComputeNode>> dispatch(Collection<ResourceAllocationRequest> requests, Collection<ComputeNode> availableNodes) {
        ArrayList<Map.Entry<ResourceAllocationRequest, ComputeNode>> mapping = new ArrayList<>();

        // Sort nodes in descending order
        //
        ArrayList<ComputeNode> nodesByCpuLoad = new ArrayList<>();
        nodesByCpuLoad.addAll(availableNodes);
        // A local cache for sorting purposes.
        final HashMap<ComputeNode, Integer> minCpuLoadForNode = new HashMap<>();

        Collections.sort(nodesByCpuLoad, new Comparator<ComputeNode>() {
            @Override
            public int compare(ComputeNode o1, ComputeNode o2) {
                return Integer.compare(_getCPULoad(o2, minCpuLoadForNode), _getCPULoad(o1, minCpuLoadForNode));
            }
        });

        // Then, start from the first request to give better chances in avoiding starvation.
        //
        for (ResourceAllocationRequest request : requests) {
            for (ComputeNode node : nodesByCpuLoad) {
                try {
                    // Even if allocate is unset, we need to allocate resources for a moment, so for other requests
                    // the node resources are partially consumed.
                    if (node.allocate(request.resourceAllocations)) {
                        // Ok, remember mapping and go for another resource.
                        mapping.add(new AbstractMap.SimpleEntry<>(request, node));
                        // FIXME: If the node has more than a single execution thread, adding a request to the node
                        // should change its position on the nodesByCpuLoad list --> the node's CPU is going to be loaded.
                        break;
                    }
                } catch (ResourceNotAvailableException x) {
                    _Logger.debug("Node: " + node.id + " cannot meet requirement for request " + request.id, x);
                }
            }
        }

        return mapping;
    }
}
