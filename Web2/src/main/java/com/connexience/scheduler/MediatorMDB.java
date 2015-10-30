package com.connexience.scheduler;

import com.connexience.model.EngineInformation;
import com.connexience.performance.model.WorkflowEngineInstance;
import com.connexience.restImplimentations.engineConfiguration;
import com.connexience.server.model.workflow.WorkflowInvocationMessage;
import com.connexience.server.util.SerializationUtils;
import com.connexience.server.workflow.service.DataProcessorReuirementsDefinition;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.*;

/**
 * Created by naa166 - Anirudh Agarwal on 01/06/2015.
 * This class gets the workflowInvocation Message from e-Sc and passes it to the correct engine
 */

@MessageDriven(name = "MediatorMDB",
        activationConfig =
                {
                        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
                        @ActivationConfigProperty(propertyName = "destination", propertyValue = "Workflow")
                }, mappedName = "Workflow")
public class MediatorMDB implements MessageListener{


    static String invocationId;

    //private static List<WorkflowEngineInstance> minKeyList = new ArrayList<WorkflowEngineInstance>();


    /**
     * For performance Oriented, get the engine which has the maximum amount of threads available or the minimum amount of working threads
     * @param definition Information about the blocks
     * @param workflowEngineInstances Instance of WorkflowEngineInstances class
     * @param manager Instance of EngineInformationManager class
     * @return list of engine suitable for the task
     */
    private static List<WorkflowEngineInstance> getMinimumThreadEngines(ArrayList<DataProcessorReuirementsDefinition> definition, List<WorkflowEngineInstance> workflowEngineInstances, EngineInformationManager manager){
        HashMap<WorkflowEngineInstance, Integer> tempMap = new HashMap<WorkflowEngineInstance, Integer>();
        long requiredRAM = getMinPhysicalRam(definition);
        long requiredDiskSpace = getMinDiskSpace(definition);
        System.out.println("from mediatorMDB" + manager.displayHashMap());
        for(int i=0 ; i <workflowEngineInstances.size() ; i++){
            //System.out.println(workflowEngineInstances.get(i).getIpAddress() + " RAM  : " + workflowEngineInstances.get(i).getFreeRam());
            //if (workflowEngineInstances.get(i).getFreeRam() >  requiredRAM && checkFreeRam(workflowEngineInstances.get(i), manager, requiredRAM)) {
            if (checkFreeResources(workflowEngineInstances.get(i), manager, requiredRAM, requiredDiskSpace)) {
                tempMap.put(workflowEngineInstances.get(i), manager.getEngineCurrentThreadCount(workflowEngineInstances.get(i).getIpAddress()));
            }
        }

        Map.Entry<WorkflowEngineInstance, Integer> max = null;

        /**Get the engine with the maximum thread counts */

        List<WorkflowEngineInstance> minKeyList = new ArrayList<WorkflowEngineInstance>();
        for(Map.Entry<WorkflowEngineInstance, Integer> entry : tempMap.entrySet()) {

            if (max == null || max.getValue() <= entry.getValue()) {
                if(max == null || max.getValue() < entry.getValue()){
                    max = entry;
                    minKeyList.clear();
                }
                minKeyList.add(entry.getKey());
            }
        }

        /**get the engine with the maximum physical memory */

        return minKeyList;
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

    private static long getMinPhysicalRam(ArrayList<DataProcessorReuirementsDefinition> definition) {
        long min = 0;
        if (definition.size() == 1)
            min = Long.parseLong(definition.get(0).getMinimumPhysicalRAMRequired());

        /**Get the max out of the min physical RAM required */
        if(definition.size() > 1)
            for(int i =1 ; i < definition.size() ; i++){
                if(Long.parseLong(definition.get(i).getMinimumPhysicalRAMRequired()) < min)
                    min = Long.parseLong(definition.get(i).getMinimumPhysicalRAMRequired());
            }
        return  min;

    }

    private static long getMinDiskSpace(ArrayList<DataProcessorReuirementsDefinition> definition) {
        long min = 0;
        if (definition.size() == 1)
            min = Long.parseLong(definition.get(0).getDiskFreeSpaceRequired());

        /**Get the max out of the min physical RAM required */
        if(definition.size() > 1) {
            for (int i = 1; i < definition.size(); i++) {
                if (Long.parseLong(definition.get(i).getDiskFreeSpaceRequired()) < min)
                    min = Long.parseLong(definition.get(i).getDiskFreeSpaceRequired());
            }
        }
        return  min;

    }


    public static List<WorkflowEngineInstance> schedule(ArrayList<DataProcessorReuirementsDefinition> definition, EngineInformationManager manager,
                                                        List<WorkflowEngineInstance> workflowEngineInstances){
        boolean performanceOriented = true;
        boolean costOriented = false;
        if(costOriented){

        }
        else if (performanceOriented){

            return getMinimumThreadEngines(definition,workflowEngineInstances, manager);
            //return result;
        }
        else {

        }
        return null;
    }

    /**
     * Get the requirements XML from the database for all the blocks which have it available.
     * @param message the workflow invocation message
     * @return the information of each blocks that is present in the requirements XML
     */
    public static ArrayList<DataProcessorReuirementsDefinition> getDefinitionArrayList(Message message){

        ArrayList<DataProcessorReuirementsDefinition> definitionArrayList = new ArrayList<DataProcessorReuirementsDefinition>();

        if (message instanceof BytesMessage) {
            BytesMessage bm = (BytesMessage) message;
            try {
                bm.reset();
                byte[] data = new byte[(int) bm.getBodyLength()];
                bm.readBytes(data);
                Object payload = SerializationUtils.deserialize(data);
                if (payload instanceof WorkflowInvocationMessage) {
                    WorkflowInvocationMessage invocationMessage = (WorkflowInvocationMessage) payload;
                    invocationId = invocationMessage.getInvocationId();
                    RetrieveRequirementXMLForAllBlocks xmlForAllBlocks = new RetrieveRequirementXMLForAllBlocks();
                    definitionArrayList = xmlForAllBlocks.getXMLForAllBlocks(invocationMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return definitionArrayList;
    }

    /**
     * Initial check to dismiss the engines which are not running or have more than 80 percent CPU used.
     * @param workflowEngineInstances Instance of WorkflowEngineInstances class from the performance monitor
     * @param manager - Instance of EngineInformationManager class
     * @return List of engines that are available to be scheduled
     */
    public static List<WorkflowEngineInstance> sortAvailableEngines(List<WorkflowEngineInstance> workflowEngineInstances
            , EngineInformationManager manager){

        for(int i = 0 ; i < workflowEngineInstances.size(); i++){
            if(workflowEngineInstances.get(i).getStatus() == 1 && workflowEngineInstances.get(i).getCpuPercentUsed() < 80 ) {
                if (manager.getEngineCurrentThreadCount(workflowEngineInstances.get(i).getIpAddress()) == 0)
                    workflowEngineInstances.remove(i);
            }
            else
                workflowEngineInstances.remove(i);

        }
        return workflowEngineInstances;
    }

    public static void selectCorrectEngine(List<WorkflowEngineInstance> workflowEngineInstances,
                                           ArrayList<DataProcessorReuirementsDefinition> definitionArrayList,
                                           EngineInformationManager manager,Message message){
        HashMap<String, Double> engineCompare = new HashMap<String, Double>();
        int numberOfFreeEngines = workflowEngineInstances.size();
        /** If number of engines is greater than one "or equal to one" then select The best possible engine , need to run scheduling algorithm*/
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
                String winningQueueName = engineConfiguration.fetchQueueName(winningIpAddress);
                manager.updateEngineStatus(winningIpAddress, true, false);
                manager.reserveResourceInformation(winningIpAddress, getMinPhysicalRam(definitionArrayList), invocationId, getMinDiskSpace(definitionArrayList));
                //checkForFreeEngines.updateEngineStatus(winningIpAddress, false);
                ForwardMessageToCorrectQueue.pushMessage(winningQueueName,message);
            }
            else{
                manager.addMessageToArray(message);
            }
        }
        /**if number of free engines is less than one then put the message in the waiting queue*/
        else if (numberOfFreeEngines < 1){
            manager.addMessageToArray(message);
        }
    }

    public void onMessage(Message message) {

        /**Test method to call the prediction method */
        ArrayList<DataProcessorReuirementsDefinition> definitionArrayList;


        definitionArrayList = getDefinitionArrayList(message);

        /** Call the restAPI to get the information about the engines */
        List<WorkflowEngineInstance> workflowEngineInstances  = engineConfiguration.getEngineStatus();
        try {
            //javax.naming.Context c = new InitialContext();
            EngineInformationManager manager = new EngineInformationManager();


            /**Check if the engines are usable because any engine with high cpu percentage should not be used*/
            workflowEngineInstances = sortAvailableEngines(workflowEngineInstances,manager);


            /**if number of free engines is one , Then also we need to run the scheduling algorithm */
            selectCorrectEngine(workflowEngineInstances,definitionArrayList,manager,message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**This is to compare the engines and sort them in ascending order*/
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
}
