package com.connexience.scheduler;

import com.connexience.model.EngineInformation;
import com.connexience.model.InvocationInformation;
import com.connexience.performance.model.WorkflowEngineInstance;
import com.connexience.restImplimentations.engineConfiguration;

import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.jms.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by naa166 - Anirudh Agarwal on 06/07/2015.
 * This is a singleton class which is used to store and manage the mapping b/w the engine and it's information for the scheduler.
 */

@Singleton
@EJB(name = "java:global/ejb/EngineInformationManager", beanInterface = EngineInformationManager.class)
public class EngineInformationManager {

    public static HashMap<String, EngineInformation> engineThreadMapping = new HashMap<String, EngineInformation>();
    public static HashMap<String, InvocationInformation> resourceInformation = new HashMap<String, InvocationInformation>();

    public void setEngineStatus(String engineIp, EngineInformation information){
        engineThreadMapping.put(engineIp,information);
    }

    public void setEngineStatus(String engineIp){

        int threadCount;
        long diskFreeSpace ;
        long freeRam;
        long PhysicalRam;
        double cpuPercentUsed;
        int maxConcurrentWorkflowInvocation;
        long freeRAM;
        EngineInformation information = new EngineInformation();
        List<WorkflowEngineInstance> workflowEngineInstances = engineConfiguration.getEngineStatus();
        for (int i = 0; i < workflowEngineInstances.size(); i++) {

            if (workflowEngineInstances.get(i).getIpAddress().equalsIgnoreCase(engineIp)) {

                threadCount = workflowEngineInstances.get(i).getMaxConcurrentWorkflowInvocation();
                diskFreeSpace = workflowEngineInstances.get(i).getDiskFreeSpace();
                freeRam = workflowEngineInstances.get(i).getFreeRam();
                PhysicalRam = workflowEngineInstances.get(i).getPhysicalRam();
                cpuPercentUsed = workflowEngineInstances.get(i).getCpuPercentUsed();
                maxConcurrentWorkflowInvocation = workflowEngineInstances.get(i).getMaxConcurrentWorkflowInvocation();
                freeRAM = workflowEngineInstances.get(i).getFreeRam();

                information.setDiskFreeSpace(diskFreeSpace);
                information.setFreeRam(freeRam);
                information.setThreadCount(threadCount);
                information.setPhysicalRam(PhysicalRam);
                information.setCpuPercentUsed(cpuPercentUsed);
                information.setMaxConcurrentWorkflowInvocation(maxConcurrentWorkflowInvocation);
                information.setFreeRam(freeRAM);
            }
        }
        engineThreadMapping.put(engineIp,information);
    }

   public int getEngineCurrentThreadCount(String EngineIp){
        return engineThreadMapping.get(EngineIp).getThreadCount();
    }

    public EngineInformation getEngineInformationObject(String engineIp){
        return engineThreadMapping.get(engineIp);
    }

    public InvocationInformation getInvocationInformationObject(String invocationID){
        return resourceInformation.get(invocationID);
    }

    public String displayHashMap() {
        String result = "";
        for (Object key : engineThreadMapping.keySet()) {

            result = result + key.toString() + " :" + engineThreadMapping.get(key).getThreadCount() +
                    " - Free RAM:   " + engineThreadMapping.get(key).getFreeRam() +  " - Free Disk Space:   " + engineThreadMapping.get(key).getDiskFreeSpace();

        }

        return result;
    }

    public String displayResourceInformation() {
        String result = "";
        for (Object key : resourceInformation.keySet()) {

            result = result + key.toString() + " :" + resourceInformation.get(key).getEngineIp()
                    + " " + resourceInformation.get(key).getPhysicalRAM() + " " + resourceInformation.get(key).getDiskSpace();

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

    public void reserveResourceInformation(String engineIp, long physicalRAMRequired, String invocationId, long diskSpaceRequired){

        InvocationInformation invocationInformation = new InvocationInformation();
        invocationInformation.setEngineIp(engineIp);
        invocationInformation.setInvocationId(invocationId);
        //Parameter to add the physical Ram required by the workflow
        invocationInformation.setPhysicalRAM(physicalRAMRequired);
        //Parameter to add the disk space required by the workflow
        invocationInformation.setDiskSpace(diskSpaceRequired);
        resourceInformation.put(invocationId, invocationInformation);




        EngineInformation information = getEngineInformationObject(engineIp);
        long freeRam = information.getFreeRam();
        long req = physicalRAMRequired;

        long freeDiskSPace = information.getDiskFreeSpace();
        long diskSpaceReq = diskSpaceRequired;
        information.setFreeRam(freeRam - req);
        information.setDiskFreeSpace(freeDiskSPace - diskSpaceReq);
        setEngineStatus(engineIp, information);

    }

    public void freeResourceInformation(String invocationId){

        InvocationInformation invocationInformation = getInvocationInformationObject(invocationId);
        String engineIp = invocationInformation.getEngineIp();
        Long physicalRAMRequired = invocationInformation.getPhysicalRAM();
        long diskSpaceRequired = invocationInformation.getDiskSpace();
        resourceInformation.remove(invocationId);

        EngineInformation information = getEngineInformationObject(engineIp);
        long free = information.getFreeRam();
        long req = physicalRAMRequired;

        long freeDiskSpace = information.getDiskFreeSpace();
        long reqDiskSpace = diskSpaceRequired;
        information.setFreeRam(free + req);
        information.setDiskFreeSpace(freeDiskSpace + reqDiskSpace);
        System.out.println("updating resource information");
        setEngineStatus(engineIp, information);
        System.out.println(displayHashMap());
    }

    public void updateEngineStatus(String engineIp, boolean invocationStarted, boolean invocationFinished){

        if(invocationStarted){
            int currentThreadValue = getEngineCurrentThreadCount(engineIp);
            if(currentThreadValue > 0){
                int updateThreadValue = currentThreadValue - 1;
                EngineInformation information = getEngineInformationObject(engineIp);
                information.setThreadCount(updateThreadValue);
                setEngineStatus(engineIp, information);
            }
            else
                throw new IllegalArgumentException("Thread value Mismatch");
        }

        if(invocationFinished){

            int currentThreadValue = getEngineCurrentThreadCount(engineIp);
            int updateThreadValue = currentThreadValue + 1;
            EngineInformation information = getEngineInformationObject(engineIp);
            information.setThreadCount(updateThreadValue);
            setEngineStatus(engineIp, information);

            checkForWaitingJob(engineIp);
        }
    }

    public void checkForWaitingJob(String engineIp) {
        System.out.println("Before if loop of check for waiting job");
        if(!jmsMessageArray.isEmpty()){
            int size = jmsMessageArray.size();
            System.out.println("inside if loop of check for waiting job with array size = " + size);
               // manager.updateEngineStatus(engineIp,true,false);
            List<WorkflowEngineInstance> workflowEngineInstances  = engineConfiguration.getEngineStatus();
            workflowEngineInstances = MediatorMDB.sortAvailableEngines(workflowEngineInstances, this);
            //MediatorMDB.schedule(MediatorMDB.getDefinitionArrayList(getMessage()),this,workflowEngineInstances);
            Message message = getMessage();
            MediatorMDB.selectCorrectEngine(workflowEngineInstances,MediatorMDB.getDefinitionArrayList(message),this,message);


            //String QueueName = engineConfiguration.fetchQueueName(engineIp);
            //updateEngineStatus(engineIp, true, false);
            // ForwardMessageToCorrectQueue.pushMessage(QueueName,getMessage());
            if(getEngineCurrentThreadCount(engineIp) > 0 && jmsMessageArray.size() < size){
               checkForWaitingJob(engineIp);
            }
        }
        /*else {
            setEngineStatus(engineIp);
        }*/
        /*else{
            engineConfiguration.updateEngineInformationManager();
        }*/
        /*else if(getEngineCurrentThreadCount(engineIp) ==  engineThreadMapping.get(engineIp).getMaxConcurrentWorkflowInvocation()){

        }*/

    }
}
