package com.connexience.scheduler;

import com.connexience.performance.model.WorkflowEngineInstance;
import com.connexience.restImplimentations.engineConfiguration;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.Context;
import java.io.*;
import java.util.*;

/**
 * Created by naa166 - Anirudh Agarwal on 01/06/2015.
 */

@MessageDriven(name = "MediatorMDB",
        activationConfig =
                {
                        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
                        @ActivationConfigProperty(propertyName = "destination", propertyValue = "Workflow")
                }, mappedName = "Workflow")
public class MediatorMDB implements MessageListener{

    //private static String MEDIATOR_QUEUE = "mediatorQueue";

    public void onMessage(Message message) {
        HashMap<String, Double> engineCompare = new HashMap<String, Double>();

        /* Call the restAPI to get the information about the engines */
        List<WorkflowEngineInstance> workflowEngineInstances  = engineConfiguration.getEngineStatus();
        try {
            javax.naming.Context c = new InitialContext();
            EngineInformationManager manager = (EngineInformationManager)c.lookup("java:global/ejb/EngineInformationManager");
           // CheckForFreeEngines checkForFreeEngines= (CheckForFreeEngines) c.lookup("java:global/ejb/CheckForFreeEngines");
        /*put the ip address and the CpuPercent used in a HashMap */
                //toSort.add(workflowEngineInstances.get(i).getCpuPercentUsed());
                //if(workflowEngineInstances.get(i).getStatus() == 1 && workflowEngineInstances.get(i).getCpuPercentUsed() < 80 && workflowEngineInstances.get(i).getRunningWorkflowCount() == 0)



            for(int i = 0 ; i < workflowEngineInstances.size(); i++){
                if(workflowEngineInstances.get(i).getStatus() == 1 && workflowEngineInstances.get(i).getCpuPercentUsed() < 80 )
                    if (manager.getEngineCurrentThreadCount(workflowEngineInstances.get(i).getIpAddress()) > 0)
                        engineCompare.put(workflowEngineInstances.get(i).getIpAddress(), workflowEngineInstances.get(i).getCpuPercentUsed());
            }





            int numberOfFreeEngines = engineCompare.size();
            if(numberOfFreeEngines == 1){
                Map.Entry<String,Double> firstEntry = engineCompare.entrySet().iterator().next();
                String winningIpAddress = firstEntry.getKey();
                String winningQueueName = engineConfiguration.fetchQueueName(winningIpAddress);
                //checkForFreeEngines.updateEngineStatus(winningIpAddress, false);
                manager.updateEngineStatus(winningIpAddress,true,false);
                ForwardMessageToCorrectQueue.pushMessage(winningQueueName,message);
            }
            else if (numberOfFreeEngines > 1 ){
                HashMap<String, Double> sortedMap = sortByValues(engineCompare);
                Map.Entry<String,Double> firstEntry = sortedMap.entrySet().iterator().next();
                String winningIpAddress = firstEntry.getKey();
                String winningQueueName = engineConfiguration.fetchQueueName(winningIpAddress);
                manager.updateEngineStatus(winningIpAddress,true,false);
                //checkForFreeEngines.updateEngineStatus(winningIpAddress, false);
                ForwardMessageToCorrectQueue.pushMessage(winningQueueName,message);
            }
            else if (numberOfFreeEngines < 1){
                //push message to mediator queue
                //ForwardMessageToCorrectQueue.pushMessage(MEDIATOR_QUEUE, message);
                //checkForFreeEngines.addMessageToArray(message);
                manager.addMessageToArray(message);
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
            /*CheckForFreeEngines checkForFreeEngines = new CheckForFreeEngines();

            if(checkForFreeEngines.isAlive()){
                System.out.println("hello");
            }
            else{
                checkForFreeEngines.setName("checkForFreeEnginesThread");
                checkForFreeEngines.start();
            }*/
        //start thread to check when an engine becomes free
        //when an engine becomes free, Consume message from the mediator queue
        //push the message to the queue
        //stop the thread

        //Collections.sort(toSort, Collections.reverseOrder());

        /*Sort the HashMap in ascending order *//*
        *//* sort the engines based on their CpuPercentUsed, for primitive prototype scheduler *//*
        HashMap<String, Double> sortedMap = sortByValues(engineCompare);

        *//*Get the engine with the least CpuPercent being used *//*
        Map.Entry<String,Double> firstEntry = sortedMap.entrySet().iterator().next();
        String winningIpAddress = firstEntry.getKey();

        *//*Get the queue name from the engineQueueMapping files from the corresponding winning IP address *//*
        String winningQueueName = engineConfiguration.fetchQueueName(winningIpAddress);

        *//*Push the message to the winning queue *//*
        ForwardMessageToCorrectQueue.pushMessage(winningQueueName,message);*/

    }

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
