package com.connexience.scheduler;

import com.connexience.performance.model.WorkflowEngineInstance;
import com.connexience.restImplimentations.engineConfiguration;

import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.jms.Message;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by naa166 - Anirudh Agarwal on 06/07/2015.
 */

@Singleton
@EJB(name = "java:global/ejb/EngineInformationManager", beanInterface = EngineInformationManager.class)
public class EngineInformationManager {


    public static HashMap<String, Integer> engineThreadMapping = new HashMap<String, Integer>();

    public void setEngineStatus(String engineIp, int threadCount){
        engineThreadMapping.put(engineIp,threadCount);
    }

    public void setEngineStatus(String engineIp){

        int threadCount = 0;
        List<WorkflowEngineInstance> workflowEngineInstances = engineConfiguration.getEngineStatus();
        for (int i = 0; i < workflowEngineInstances.size(); i++) {
            if (workflowEngineInstances.get(i).getIpAddress().equalsIgnoreCase(engineIp))
            threadCount =workflowEngineInstances.get(i).getMaxConcurrentWorkflowInvocation();
        }
        engineThreadMapping.put(engineIp,threadCount);
    }

   public int getEngineCurrentThreadCount(String EngineIp){
        return engineThreadMapping.get(EngineIp);
    }

    public String displayHashMap() {
        String result = "";
        for (Object key : engineThreadMapping.keySet()) {

            result = result + key.toString() + " :" + engineThreadMapping.get(key) + "    ";

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
    private static Message getMessage(){
        Message message = jmsMessageArray.get(0);
        jmsMessageArray.remove(0);
        return message;
    }

    public void updateEngineStatus(String EngineIp, boolean invocationStarted, boolean invocationFinished){

        if(invocationStarted){
            int currentThreadValue = getEngineCurrentThreadCount(EngineIp);
            if(currentThreadValue > 0){
                int updateThreadValue = currentThreadValue - 1;
                setEngineStatus(EngineIp, updateThreadValue );
            }
            else
                throw new IllegalArgumentException("Thread value Mismatch");
        }

        if(invocationFinished){
            int currentThreadValue = getEngineCurrentThreadCount(EngineIp);
            int updateThreadValue = currentThreadValue + 1;
            setEngineStatus(EngineIp, updateThreadValue);
            checkForWaitingJob(EngineIp);
        }
    }

    public void checkForWaitingJob(String engineIp) {
        if(!jmsMessageArray.isEmpty()){
                String QueueName = engineConfiguration.fetchQueueName(engineIp);
               // manager.updateEngineStatus(engineIp,true,false);
            updateEngineStatus(engineIp,true,false);
                ForwardMessageToCorrectQueue.pushMessage(QueueName,getMessage());
        }
    }
}
