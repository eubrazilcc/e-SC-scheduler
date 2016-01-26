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

import com.connexience.scheduler.ResourceNotAvailableException;
import com.connexience.scheduler.api.InvocationSchedulerLocal;
import com.connexience.scheduler.model.Constants;
import com.connexience.scheduler.model.Property;
import com.connexience.scheduler.model.ResourceAllocation;
import com.connexience.scheduler.model.ResourceAllocationRequest;
import com.connexience.server.model.document.DocumentRecord;
import com.connexience.server.model.workflow.WorkflowInvocationMessage;
import com.connexience.server.util.SerializationUtils;
import com.connexience.server.workflow.api.API;
import com.connexience.server.workflow.api.ApiProvider;
import com.connexience.server.workflow.blocks.processor.DataProcessorBlock;
import com.connexience.server.workflow.cloud.download.WorkflowDataFetcher;
import com.connexience.server.workflow.service.DataProcessorRequirementsDefinition;
import org.pipeline.core.drawing.BlockModel;
import org.pipeline.core.drawing.model.DefaultDrawingModel;
import org.pipeline.core.xmlstorage.XmlDataStore;
import org.pipeline.core.xmlstorage.XmlStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by naa166 - Anirudh Agarwal on 01/06/2015.
 * This class gets the workflowInvocation Message from e-SC and passes it to the correct engine
 */

@MessageDriven(name = "MediatorMDB",
        activationConfig =
                {
                        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
                        @ActivationConfigProperty(propertyName = "destination", propertyValue = "Workflow")
                }, mappedName = "Workflow")
public class MediatorMDB implements MessageListener
{
    private static final Logger _Logger = LoggerFactory.getLogger(MediatorMDB.class);

    @EJB
    InvocationSchedulerLocal _schedulerBean;

    private ApiProvider _apiProvider;

    //@Inject
    //private EngineInformationManager _manager;

    //static String invocationId;

    //private static List<WorkflowEngineInstance> minKeyList = new ArrayList<WorkflowEngineInstance>();

    public MediatorMDB()
    {
        // Create and configure the API provider.
        _apiProvider = new ApiProvider();

        // FIXME: All this should be read from a configuration file.
        try {
            _apiProvider.setServerBaseURL("http://localhost:8080");
            _apiProvider.setUseRmi(true);
        } catch (URISyntaxException x) {
            _Logger.error("Invalid server url", x);
        }

    }

    /**
     * For performance Oriented, get the engine which has the maximum amount of threads available or the minimum amount of working threads
     * @param definition Information about the blocks
     * @param workflowEngineInstances Instance of WorkflowEngineInstances class
     * @param manager Instance of EngineInformationManager class
     * @return list of engine suitable for the task
     */
    /*
    private static List<WorkflowEngineInstance> getMinimumThreadEngines(ArrayList<DataProcessorRequirementsDefinition> definition, List<WorkflowEngineInstance> workflowEngineInstances, EngineInformationManager manager){
        HashMap<WorkflowEngineInstance, Integer> tempMap = new HashMap<WorkflowEngineInstance, Integer>();
        long requiredRAM = getMinPhysicalRam(definition);
        long requiredDiskSpace = getMinDiskSpace(definition);
        System.out.println("from mediatorMDB" + manager.displayHashMap());

        int maxThreadCount = 0;
        ArrayList<WorkflowEngineInstance> maxEngineList = new ArrayList<>();

        for (WorkflowEngineInstance engine : workflowEngineInstances) {
            //System.out.println(workflowEngineInstances.get(i).getIpAddress() + " RAM  : " + workflowEngineInstances.get(i).getFreeRam());
            //if (workflowEngineInstances.get(i).getFreeRam() >  requiredRAM && checkFreeRam(workflowEngineInstances.get(i), manager, requiredRAM)) {
            if (checkFreeResources(engine, manager, requiredRAM, requiredDiskSpace)) {
                int threadCount = manager.getEngineCurrentThreadCount(engine.getIpAddress());
                if (threadCount > maxThreadCount) {
                    maxThreadCount = threadCount;
                    maxEngineList.clear();
                    maxEngineList.add(engine);
                } else if (threadCount == maxThreadCount) {
                    maxEngineList.add(engine);
                }
            }
        }

//        Map.Entry<WorkflowEngineInstance, Integer> max = null;
//
//        //Get the engine with the maximum thread counts
//
//        List<WorkflowEngineInstance> minKeyList = new ArrayList<WorkflowEngineInstance>();
//        for(Map.Entry<WorkflowEngineInstance, Integer> entry : tempMap.entrySet()) {
//
//            if (max == null || max.getValue() <= entry.getValue()) {
//                if(max == null || max.getValue() < entry.getValue()){
//                    max = entry;
//                    minKeyList.clear();
//                }
//                minKeyList.add(entry.getKey());
//            }
//        }
//
//        return minKeyList;

        return maxEngineList;
    }

    private static boolean checkFreeResources(WorkflowEngineInstance workflowEngineInstance, EngineInformationManager manager, long requiredRAM, long requiredDiskSpace) {

        EngineInformation information = manager.getEngineInformationObject(workflowEngineInstance.getIpAddress());
        long freeRAM = information.getFreeRam();
        long freeDiskSpace = information.getDiskFreeSpace();
        if(freeRAM < requiredRAM && freeDiskSpace < requiredDiskSpace) {
            return false;
        }
        else
            return true;
    }

    private static long getMinPhysicalRam(ArrayList<DataProcessorRequirementsDefinition> definition) {
        long min = 0;
        if (definition.size() == 1)
            min = Long.parseLong(definition.get(0).getMinimumPhysicalRAMRequired());

        // Get the max out of the min physical RAM required
        if(definition.size() > 1)
            for(int i =1 ; i < definition.size() ; i++){
                if(Long.parseLong(definition.get(i).getMinimumPhysicalRAMRequired()) < min)
                    min = Long.parseLong(definition.get(i).getMinimumPhysicalRAMRequired());
            }
        return  min;

    }

    private static long getMinDiskSpace(ArrayList<DataProcessorRequirementsDefinition> definition) {
        long min = 0;
        if (definition.size() == 1)
            min = Long.parseLong(definition.get(0).getDiskFreeSpaceRequired());

        //Get the max out of the min physical RAM required
        if(definition.size() > 1) {
            for (int i = 1; i < definition.size(); i++) {
                if (Long.parseLong(definition.get(i).getDiskFreeSpaceRequired()) < min)
                    min = Long.parseLong(definition.get(i).getDiskFreeSpaceRequired());
            }
        }
        return  min;

    }
    */

    /*
    public static List<WorkflowEngineInstance> schedule(ArrayList<DataProcessorRequirementsDefinition> definition, EngineInformationManager manager,
                                                        List<WorkflowEngineInstance> workflowEngineInstances){
        boolean performanceOriented = true;
        boolean costOriented = false;

        if  (costOriented) {
            throw new UnsupportedOperationException("Cost oriented scheduling is currently unsupported.");
        }
        else if (performanceOriented) {
            return getMinimumThreadEngines(definition,workflowEngineInstances, manager);
        }
        else {
            throw new UnsupportedOperationException();
        }
    }
    */


    /**
     * Get the requirements XML from the database for all the blocks which have it available.
     *
     * @param invocationMessage the workflow invocation message
     * @return the information of each blocks that is present in the requirements XML
     */
    /*
    public static ArrayList<DataProcessorRequirementsDefinition> _getDefinitionArrayList(WorkflowInvocationMessage invocationMessage) {
        ArrayList<DataProcessorRequirementsDefinition> definitionArrayList = new ArrayList<DataProcessorRequirementsDefinition>();

        invocationId = invocationMessage.getInvocationId();
        RetrieveRequirementXMLForAllBlocks xmlForAllBlocks = new RetrieveRequirementXMLForAllBlocks();
        definitionArrayList = xmlForAllBlocks.getXMLForAllBlocks(invocationMessage);
        return definitionArrayList;
    }
*/

    /**
     * Initial check to dismiss the engines which are not running or have more than 80 percent CPU used.
     *
     * @param workflowEngineInstances Instance of WorkflowEngineInstances class from the performance monitor
     * @param manager - Instance of EngineInformationManager class
     * @return List of engines that are available to be scheduled
     */
    /*
    public static List<WorkflowEngineInstance> filterAvailableEngines(List<WorkflowEngineInstance> workflowEngineInstances, EngineInformationManager manager)
    {
        Iterator<WorkflowEngineInstance> iter = workflowEngineInstances.iterator();
        while (iter.hasNext()) {
            WorkflowEngineInstance engine = iter.next();

            if (engine.getStatus() == WorkflowEngineInstance.ENGINE_RUNNING && engine.getCpuPercentUsed() < 80) {
                if (manager.getEngineCurrentThreadCount(engine.getIpAddress()) == 0) {
                    iter.remove();
                }
            } else {
                iter.remove();
            }
        }

        return workflowEngineInstances;
    }
*/
    /*
    public static boolean selectCorrectEngine(List<WorkflowEngineInstance> workflowEngineInstances,
                                           ArrayList<DataProcessorRequirementsDefinition> definitionArrayList,
                                           EngineInformationManager manager,Message message)
    {
        HashMap<String, Double> engineCompare = new HashMap<>();
        int numberOfFreeEngines = workflowEngineInstances.size();
        // If number of engines is greater than one "or equal to one" then select The best possible engine , need to run scheduling algorithm
        if (numberOfFreeEngines >= 1 ){

            List<WorkflowEngineInstance> minKeyList = schedule(definitionArrayList, manager, workflowEngineInstances);
            engineCompare.clear();

            if (minKeyList.size() >= 1){
                for (WorkflowEngineInstance aMinKeyList : minKeyList) {
                    engineCompare.put(aMinKeyList.getIpAddress(), aMinKeyList.getCpuPercentUsed());
                }
                HashMap<String, Double> sortedMap = sortByValues(engineCompare);
                Map.Entry<String,Double> firstEntry = sortedMap.entrySet().iterator().next();
                String winningIpAddress = firstEntry.getKey();
                String winningQueueName = EngineConfiguration.GetQueueName(winningIpAddress);
                manager.updateEngineStatus(winningIpAddress, true, false);
                manager.reserveResourceInformation(winningIpAddress, getMinPhysicalRam(definitionArrayList), invocationId, getMinDiskSpace(definitionArrayList));
                //checkForFreeEngines.updateEngineStatus(winningIpAddress, false);
                ForwardMessageToCorrectQueue.pushMessage(winningQueueName,message);
                return true;
            }
        }

        manager.addMessageToArray(message);
        return false;
    }
    */

    @Override
    public void onMessage(Message message)
    {
        try {
            WorkflowInvocationMessage msg = _getInvocationMessage(message);
            if (msg == null) {
                _Logger.warn("[MediatorMDB] Received unsupported message: " + message.getJMSMessageID());
            }

            _schedulerBean.requestResources(_generateAllocationRequest(msg, message));
        } catch (Exception e) {
            _Logger.error("Unable to process the message", e);
        }

        // TODO: Things below are to be removed...
        //ArrayList<DataProcessorRequirementsDefinition> definitionArrayList = getDefinitionArrayList(message);
        /** Call the restAPI to get the information about the engines */
        //List<WorkflowEngineInstance> workflowEngineInstances = EngineConfiguration.getEngineStatus();
        /** Filter out engines which are loaded or have not available threads */
        //workflowEngineInstances = filterAvailableEngines(workflowEngineInstances, _manager);
        /**if number of free engines is one , Then also we need to run the scheduling algorithm */
        //selectCorrectEngine(workflowEngineInstances, definitionArrayList, _manager, message);
    }


    private WorkflowInvocationMessage _getInvocationMessage(Message message) throws JMSException, IOException, ClassNotFoundException
    {
        if (message instanceof BytesMessage) {
            BytesMessage bm = (BytesMessage) message;
            bm.reset();
            byte[] data = new byte[(int) bm.getBodyLength()];
            bm.readBytes(data);
            Object payload = SerializationUtils.deserialize(data);
            if (payload instanceof WorkflowInvocationMessage) {
                return (WorkflowInvocationMessage) payload;
            }
        }

        return null;
    }


    /**This is to compare the engines and sort them in ascending order*/
    /*
    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }
    */


    private ArrayList<DataProcessorRequirementsDefinition> _getRequirementsForAllBlocks(WorkflowInvocationMessage message) {
        ArrayList<DataProcessorRequirementsDefinition> requirements = new ArrayList<>();
        API apiLink = null;
        //String serviceVersionId;DocumentVersion latestVersion = EJBLocator.lookupStorageBean().getLatestVersion(ticket, serviceId);
        try {
            apiLink = _apiProvider.createApi(message.getTicket());

            /*try {
                DocumentRecord documentRecord = apiLink.getDocument(message.getWorkflowId());

                String latestVersionId = apiLink.getLatestVersionId(documentRecord.getId());

                // TODO : Write logic to store the retrieved xml in memory cache

            } catch (ConnexienceException e) {
                e.printStackTrace();
            }*/

            //Instantiate WorkflowDataFetcher
            WorkflowDataFetcher fetcher;
            if (message.isUseLatest()) {
                fetcher = new WorkflowDataFetcher(message.getWorkflowId(), apiLink);
            } else {
                fetcher = new WorkflowDataFetcher(message.getWorkflowId(), message.getVersionId(), apiLink);
            }
            XmlDataStore workflowData = fetcher.download();

            DefaultDrawingModel drawing = new DefaultDrawingModel();
            drawing.recreateObject(workflowData);
            //drawingData = workflowData;
            //((DefaultDrawingModel) drawing).recreateObject(drawingData);

            Enumeration blocks = drawing.blocks();
            BlockModel block;
            DocumentRecord serviceDoc;

            while (blocks.hasMoreElements()) {
                block = (BlockModel) blocks.nextElement();

                if (block instanceof DataProcessorBlock) {
                    DataProcessorBlock dpb = (DataProcessorBlock) block;
                    serviceDoc = new DocumentRecord();
                    serviceDoc.setId(dpb.getServiceId());

                    String latestVersionId = apiLink.getLatestVersionId(serviceDoc.getId());
                    DataProcessorRequirementsDefinition def;

                    def = apiLink.getRequirements(dpb.getServiceId(), latestVersionId);
                    if (def.isRequirementsFilePresent()) {
                        requirements.add(def);
                    } else {
                        _Logger.info("The requirements XML file not found for service: {}", dpb.getServiceId());
                    }
                }
            }
        } catch (XmlStorageException x) {
            _Logger.error("Error parsing workflow data for invocation: {}", message.getInvocationId(), x);
        } catch (Exception x) {
            _Logger.error("Error retrieving block requirements", x);
        } finally {
            if (apiLink != null) {
                apiLink.terminate();
            }
        }

        return requirements;
    }


    private ResourceAllocationRequest _generateAllocationRequest(WorkflowInvocationMessage invocationMessage, Message jmsMessage) throws ResourceNotAvailableException
    {
        ResourceAllocationRequest request = new ResourceAllocationRequest();
        request.id = invocationMessage.getInvocationId();
        request.type = Constants.Request.REQUEST_TYPE_INVOCATION;
        request.content = jmsMessage;
        // TODO: Currently, there's no way to get the size of a workflow invocation.
        // Later we can think of attaching a performance model to the allocation request.
        //request.size = ?
        request.resourceAllocations = new ArrayList<>();

        ArrayList<DataProcessorRequirementsDefinition> requirementsDefinitions = _getRequirementsForAllBlocks(invocationMessage);

        // Each invocation requires one invocation thread at least...
        request.resourceAllocations.add(new ResourceAllocation(Constants.ResourceType.INVOCATION_THREAD, new Property(Constants.Property.AVAILABLE, 1)));

        // but may have some other requirements, too.
        String cpuArch = null;
        long maxMemory = -1;
        long maxDiskSpace = -1;

        for (DataProcessorRequirementsDefinition def : requirementsDefinitions) {
            long memory = Long.parseLong(def.getMinimumPhysicalRAMRequired());
            if (memory > maxMemory) {
                maxMemory = memory;
            }

            long disk = Long.parseLong(def.getDiskFreeSpaceRequired());
            if (disk > maxDiskSpace) {
                maxDiskSpace = disk;
            }

            if (cpuArch != null) {
                if (!cpuArch.equals(def.getCPUArchitecture())) {
                    throw new ResourceNotAvailableException("Mismatch in CPU architecture between blocks: %s != %s", cpuArch, def.getCPUArchitecture());
                }
            } else {
                cpuArch = def.getCPUArchitecture();
            }
        }

        if (maxMemory > 0) {
            request.resourceAllocations.add(new ResourceAllocation(Constants.ResourceType.MEMORY, new Property(Constants.Property.AVAILABLE, maxMemory)));
        }

        if (maxDiskSpace > 0) {
            request.resourceAllocations.add(new ResourceAllocation(Constants.ResourceType.DISK, new Property(Constants.Property.AVAILABLE, maxDiskSpace)));
        }

        if (cpuArch != null) {
            request.resourceAllocations.add(new ResourceAllocation(Constants.ResourceType.CPU, new Property(Constants.Property.CPU_ARCHITECTURE, cpuArch)));
        }

        return request;
    }
}
