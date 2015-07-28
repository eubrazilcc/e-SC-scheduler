package com.connexience.restImplimentations;

import com.connexience.performance.model.WorkflowEngineInstance;
//import com.connexience.scheduler.CheckForFreeEngines;
import com.connexience.scheduler.CalculateRTT;
import com.connexience.scheduler.EngineInformationManager;
import com.connexience.scheduler.MessageStorageSingletonBean;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * This class will contain the information which would be called by the engine to check whether the scheduler is deployed
 * in the server. If the scheduler is deployed, it will create and return the name of the queue back to the engine which
 * will be configured accordingly.
 * Created by naa166 - Anirudh Agarwal on 22/05/2015.
 */



@Path("/rest")
public class SchedularStatus {

    private int count=0;
    private String engineID;

    /*
    PerformanceWorkflowEngineBean engineBean;*/

    @GET
    @Path("/status/{hostName}")
    //@Path("/status")
    @Produces("application/json")
    public String getSchedularStatus(@PathParam(value = "hostName")String hostName){

        String status = "available";
        count=engineConfiguration.getCount();
        count = count + 1;
        engineConfiguration.putCount(count);

        String queueName = "engine" + count;

        //engineConfiguration.createQueue(queueName);

        // InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("EngineQueueMapping.json");
        //BufferedReader br = new BufferedReader(new InputStreamReader(input));
        String finalQueueName = engineConfiguration.updateEngineQueueMapping(hostName,queueName);

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
            manager.updateEngineStatus(EngineIp,true,false);
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
    @Path("/test")
    @Produces("application/json")
    public String test(){
        String result = "calling method atleast";

        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        result = engineConfiguration.test(br);
        return result;
    }

    /*@GET
    // @Path("/status/{engineid}")
    @Path("/engineIp/{ipaddress}")
    @Produces("application/json")
    public String getEngineInformation(@PathParam(value = "ipaddress")String ipaddress){

        try {
            WorkflowEngineInstance engine = engineBean.getEngine(ipaddress);
            String result = engine.getIpAddress();

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "nothing found";
    }*/



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
        MessageStorageSingletonBean testSingleton = new MessageStorageSingletonBean();
        return testSingleton.getMessageArray().toString();
    }

    /*@GET
    @Path("/displayHash")
    @Produces("application/json")
    public String displayHash(){

        CheckForFreeEngines checkForFreeEngines = new CheckForFreeEngines();
        return checkForFreeEngines.displayHashMap();

    }*/

    /*@GET
    @Path("/displaySomething")
    @Produces("application/json")
    public String displaySomething(){

        CheckForFreeEngines checkForFreeEngines = new CheckForFreeEngines();
        return checkForFreeEngines.getDunno();

    }*/

    @GET
    @Path("/displayThreadArray")
    @Produces("application/json")
    public String displayThreadArray(){
        // MessageStorageSingletonBean testSingleton = new MessageStorageSingletonBean();
        //return testSingleton.getMessageArray().toString();

        try {
            Context c = new InitialContext();
            EngineInformationManager manager = (EngineInformationManager) c.lookup("java:global/ejb/EngineInformationManager");
            return  manager.getJmsMessageArray().toString();
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

   /*@GET
    @Path("/displayThreadArray")
    @Produces("application/json")
    public String displayThreadArray(){
        // MessageStorageSingletonBean testSingleton = new MessageStorageSingletonBean();
        //return testSingleton.getMessageArray().toString();

        try {
            Context c = new InitialContext();
            EngineInformationManager testThread = (EngineInformationManager) c.lookup("java:global/ejb/EngineInformationManager");
            return  testThread.getJmsMessageArray().toString();
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return "not working :(";
    }*/

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


}
