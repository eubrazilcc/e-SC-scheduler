package com.connexience.restImplimentations;

import com.connexience.performance.model.WorkflowEngineInstance;
//import com.connexience.scheduler.CheckForFreeEngines;
import com.connexience.scheduler.CalculateRTT;
import com.connexience.scheduler.EngineInformationManager;
import com.connexience.scheduler.MessageStorageSingletonBean;
import com.connexience.test.APITest;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;

/**
 * This class will contain the information which would be called by the engine to check whether the scheduler is deployed
 * in the server. If the scheduler is deployed, it will create and return the name of the queue back to the engine which
 * will be configured accordingly.
 * Created by naa166 - Anirudh Agarwal on 22/05/2015.
 */



@Path("/rest")
public class SchedularStatus {

    /*
    PerformanceWorkflowEngineBean engineBean;*/

    @GET
    @Path("/status/{hostName}")
    //@Path("/status")
    @Produces("application/json")
    public String getSchedularStatus(@PathParam(value = "hostName")String hostName){

        String status = "available";
        int count = engineConfiguration.getCount();
        count = count + 1;
        engineConfiguration.putCount(count);

        String queueName = "engine" + count;

        //engineConfiguration.createQueue(queueName);

        // InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("EngineQueueMapping.json");
        //BufferedReader br = new BufferedReader(new InputStreamReader(input));
        String finalQueueName = engineConfiguration.updateEngineQueueMapping(hostName,queueName);

        try {
            Context c = new InitialContext();
            EngineInformationManager manager = (EngineInformationManager) c.lookup("java:global/ejb/EngineInformationManager");
            manager.checkForWaitingJob(hostName);
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return status + "," + finalQueueName;
    }

    @GET
    @Path("/invocationStarted/{hostName}")
    @Produces("application/json")
    public void invocationStarted(@PathParam(value = "hostName")String EngineIp){

        try {
            Context c = new InitialContext();
            EngineInformationManager manager = (EngineInformationManager) c.lookup("java:global/ejb/EngineInformationManager");
            /*for(int i=0 ; i<Integer.parseInt(difference);i++) {
                manager.updateEngineStatus(EngineIp, false, true);
                manager.checkForWaitingJob(EngineIp,manager);
            }*/
            manager.updateEngineStatus(EngineIp, true, false);
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("/invocationFinished/{hostName}")
    @Produces("application/json")
    public void invocationFinished(@PathParam(value = "hostName")String EngineIp){

        try {
            Context c = new InitialContext();
            EngineInformationManager manager = (EngineInformationManager) c.lookup("java:global/ejb/EngineInformationManager");
            /*for(int i=0 ; i<Integer.parseInt(difference);i++) {
                manager.updateEngineStatus(EngineIp, false, true);
                manager.checkForWaitingJob(EngineIp,manager);
            }*/
            manager.updateEngineStatus(EngineIp,false,true);
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("/updateEngineResourceInformations/{invocationId}")
    @Produces("application/json")
    public void updateEngineResourceInformation(@PathParam(value = "invocationId")String invocationId){
        System.out.println("updating engine information");

            //Context c = new InitialContext();
            EngineInformationManager manager = new EngineInformationManager();
            manager.freeResourceInformation(invocationId);

    }

    @GET
    @Path("/engineIp")
    @Produces("application/json")
    public String getEngineInformation(){
        String result = "calling method atleast";
        try {
            //WorkflowEngineInstance engine = engineBean.getEngine(ipaddress);
            //String result = engine.getIpAddress();

            List<WorkflowEngineInstance> workflowEngineInstances = engineConfiguration.getEngineStatus();
            result = workflowEngineInstances.get(0).getIpAddress() + "   " + workflowEngineInstances.get(0).getCpuSpeed();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @GET
    @Path("/updateEngineInformationManager")
    @Produces("application/json")
    public String updateEngineInformationManager(){
        String result = "successfully updated";

        engineConfiguration.updateEngineInformationManager();

        return result;
    }

    @GET
    @Path("/removeFromArray")
    @Produces("application/json")
    public void removeFromArray(){
        MessageStorageSingletonBean testSingleton = new MessageStorageSingletonBean();
        testSingleton.removeFromMessageArray();
    }

    @GET
    @Path("/addToArray/{value}")
    @Produces("application/json")
    public void addToArray(@PathParam(value = "value")String value){
        MessageStorageSingletonBean testSingleton = new MessageStorageSingletonBean();
        testSingleton.addToMessageArray(value);
    }


    @GET
    @Path("/displayArray")
    @Produces("application/json")
    public String displayArray(){
        return MessageStorageSingletonBean.getMessageArray().toString();
    }

    @GET
    @Path("/displayResult")
    @Produces("application/json")
    public String displayResult(){
        return MessageStorageSingletonBean.getResult();
    }

    @GET
    @Path("/displayThreadArray")
    @Produces("application/json")
    public String displayThreadArray(){
        // MessageStorageSingletonBean testSingleton = new MessageStorageSingletonBean();
        //return testSingleton.getMessageArray().toString();

            return  EngineInformationManager.getJmsMessageArray().toString();
    }

    @GET
    @Path("/getEngineThreadCount/{ip}")
    @Produces("application/json")
    public String getEngineThreadCount(@PathParam(value = "ip")String ip){
        // MessageStorageSingletonBean testSingleton = new MessageStorageSingletonBean();
        //return testSingleton.getMessageArray().toString();

        try {
            Context c = new InitialContext();
            EngineInformationManager manager = (EngineInformationManager) c.lookup("java:global/ejb/EngineInformationManager");
            return  "" + manager.getEngineCurrentThreadCount(ip);
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return "not working :(";
    }

    @GET
    @Path("/displayHash")
    @Produces("application/json")
    public String displayHash(){

        EngineInformationManager manager = new EngineInformationManager();
        return manager.displayHashMap();

    }

    @GET
    @Path("/displayInvocationInformation")
    @Produces("application/json")
    public String displayInvocationInformation(){

        EngineInformationManager manager = new EngineInformationManager();
        return manager.displayResourceInformation();

    }

    @GET
    @Path("/findRTT")
    @Produces("application/json")
    public String findRTT(){


        String result = null;
        try {
            result = CalculateRTT.findRTT("192.168.56.102" , "6789");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @GET
    @Path("/apiTest")
    @Produces("application/json")
    public void apiTest(){

        APITest.test();

    }


}
