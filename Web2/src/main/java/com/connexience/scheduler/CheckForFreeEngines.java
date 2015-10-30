/*
package com.connexience.scheduler;

import com.connexience.performance.model.WorkflowEngineInstance;
import com.connexience.restImplimentations.engineConfiguration;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.jms.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

*/
/**
 * Created by naa166 - Anirudh Agarwal on 16/06/2015.
 *//*


@Singleton
@Startup
@EJB(name = "java:global/ejb/CheckForFreeEngines", beanInterface = CheckForFreeEngines.class)
public class CheckForFreeEngines extends Thread {


    @PostConstruct
    public void startup() {
        this.start();
    }

    public static String dunno = null;

    public static String getDunno() {
        return dunno;
    }

    public static void setDunno(String dunno) {
        CheckForFreeEngines.dunno = dunno;
    }

    public static HashMap<String, Boolean> engineStatus = new HashMap<String, Boolean>();

    public static HashMap<String, Boolean> getEngineStatus() {
        return engineStatus;
    }

    public static void setEngineStatus(HashMap<String, Boolean> engineStatus) {
        CheckForFreeEngines.engineStatus = engineStatus;
    }

    public void updateEngineStatus(String engineIp, boolean status) {
        engineStatus.put(engineIp, status);
    }

    public String displayHashMap() {
        String result = "";
        for (Object key : engineStatus.keySet()) {

            result = result + key.toString() + " :" + engineStatus.get(key) + "    ";

        }

        return result;
    }

    private static ArrayList<Message> jmsMessageArray = new ArrayList<Message>();

    public static ArrayList<Message> getJmsMessageArray() {
        return jmsMessageArray;
    }

    @Lock(LockType.WRITE)
    public void addMessageToArray(Message message) {
        jmsMessageArray.add(message);
    }

    @Lock
    private static Message getMessage() {
        Message message = jmsMessageArray.get(0);
        jmsMessageArray.remove(0);
        return message;


    }

    @Override
    public void run() {

        //if mediator queue contains any message then only proceed with the logic
        while (true) {

            if (!engineStatus.isEmpty()) {

                //Below snippet is used to hold information about the availability of an engine
                for (Object key : engineStatus.keySet()) {

                    //if (!engineStatus.get(key)) {
                    List<WorkflowEngineInstance> workflowEngineInstances = engineConfiguration.getEngineStatus();
                    for (int i = 0; i < workflowEngineInstances.size(); i++) {
                        if (workflowEngineInstances.get(i).getIpAddress().equalsIgnoreCase(key.toString()) && workflowEngineInstances.get(i).getStatus() == 1 && workflowEngineInstances.get(i).getRunningWorkflowCount() == 0) {
                            dunno = "reached inside if loop";
                            updateEngineStatus(key.toString(), true);
                        }
                        if (workflowEngineInstances.get(i).getIpAddress().equalsIgnoreCase(key.toString()) && workflowEngineInstances.get(i).getStatus() == 1 && workflowEngineInstances.get(i).getRunningWorkflowCount() == 1) {
                            updateEngineStatus(key.toString(), false);
                        }
                    }
                    //}

                }


                //Below snippet is used if any message is waiting
                if (jmsMessageArray.size() > 0) {
                    boolean freeEngineAvailable = false;
                    String freeEngineIpAddress = null;
                    for (Object key : engineStatus.keySet()) {
                        if (engineStatus.get(key)) {
                            freeEngineAvailable = true;
                            freeEngineIpAddress = key.toString();
                        }
                    }

                    if (freeEngineAvailable) {

                        List<WorkflowEngineInstance> workflowEngineInstances = engineConfiguration.getEngineStatus();

                        for (int i = 0; i < workflowEngineInstances.size(); i++) {
                            if (workflowEngineInstances.get(i).getRunningWorkflowCount() != 0)
                                workflowEngineInstances.remove(i);
                        }
                        //Check over here if an engine has become free
                        for (int i = 0; i < workflowEngineInstances.size(); i++) {
                            if (workflowEngineInstances.get(i).getStatus() == 1 && workflowEngineInstances.get(i).getCpuPercentUsed() < 80 && workflowEngineInstances.get(i).getRunningWorkflowCount() == 0) {
                                String winningIpAddress = workflowEngineInstances.get(i).getIpAddress();
                                //Method over here to consume the message from the mediator queue
                                String winningQueueName = engineConfiguration.fetchQueueName(winningIpAddress);
                                Message message = getMessage();
                                ForwardMessageToCorrectQueue.pushMessage(winningQueueName, message);
                                break;
                            }
                        }
                        String winningQueueName = engineConfiguration.fetchQueueName(freeEngineIpAddress);
                        Message message = getMessage();
                        updateEngineStatus(freeEngineIpAddress, false);
                        ForwardMessageToCorrectQueue.pushMessage(winningQueueName, message);
                    }

                }
                //check if Mediator queue contains more message
                //if it contains more message
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }
}
//}
*/
