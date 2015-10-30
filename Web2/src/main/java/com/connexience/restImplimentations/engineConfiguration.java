package com.connexience.restImplimentations;

import com.connexience.model.EngineQueueMapping;
import com.connexience.performance.model.WorkflowEngineInstance;
import com.connexience.scheduler.EngineInformationManager;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by naa166 - Anirudh Agarwal on 22/05/2015.
 */


public class engineConfiguration {

   /* private static final String queue_prefix = "engine";
    private static String finalQueueName;
*/
   /* public static String generateEngineName(String engineId){

        finalQueueName = queue_prefix + engineId;

        createQueue(finalQueueName);

        return finalQueueName;

    }*/

    public static int getCount(){
        int count = 0;
        try {
            //Create a File manually as it does not get the permission to write when the file is created using java code
            //if ( !new File("/tmp/myCount.txt").exists())
            if ( !new File("/home/anirudh/scheduler/myCount.txt").exists())
                return 1;
            else {
                //BufferedReader br = new BufferedReader(new FileReader(new File("/tmp/myCount.txt")));
                BufferedReader br = new BufferedReader(new FileReader(new File("/home/anirudh/scheduler/myCount.txt")));
                String s = br.readLine();
                count = Integer.parseInt(s);
                br.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public static void putCount(int count) {
        try {
            //BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/tmp/myCount.txt")));
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/anirudh/scheduler/myCount.txt")));
            bw.write(Integer.toString(count));
            bw.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void createQueue(String queueName){

        ModelControllerClient client = null;
        ModelNode op = new ModelNode();
        ModelNode address = op.get(ClientConstants.OP_ADDR);
        address.add("subsystem", "messaging");
        address.add("hornetq-server", "default");


        // the name of the queue
        String queue = queueName;
        address.add("jms-queue", queue);


        // the JNDI entries
        ModelNode entries = op.get("entries");
        entries.add(queue);
        entries.add("jboss/exported/" + queue);


        op.get(ClientConstants.OP).set(ClientConstants.ADD);


        try
        {
            client = ModelControllerClient.Factory.create(InetAddress.getByName("localhost"), 9999);
            ModelNode result = client.execute(op);
            if(result.toString().contains("SUCCESS")){

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally
        {
            if (client != null)
            {
                try
                {
                    client.close();
                }
                catch (Exception e)
                {
                    // no-op
                }

            }
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static List<WorkflowEngineInstance> getEngineStatus() {

        List<WorkflowEngineInstance> workflowEngineInstances = null;
        try {

            URL url = new URL("http://localhost:8080/monitor/engines/listActive");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            if (connection.getResponseCode() != 200) {
            } else {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response;
                response = readAll(bufferedReader);

                ObjectMapper mapper = new ObjectMapper();
                workflowEngineInstances = mapper.readValue(response, new TypeReference<ArrayList<WorkflowEngineInstance>>() {
                });



                //result = result + "," + response;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return workflowEngineInstances;
    }

    public static String test(BufferedReader br) {
        String result = "in test";
        try {
            result = readAll(br);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return  result;


    }

    public static void temporaryMethodToCheckHostName(String filename,String hostName){
        try {
            //BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/tmp/" + filename)));
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/anirudh/scheduler/" + filename)));
            bw.write(hostName);
            //bw.write(Integer.toString(count));
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public static String updateEngineQueueMapping(String hostName, String queueName){

        String returnQueueName = null;
        //temporaryMethodToCheckHostName("hosts.txt",hostName);
        List<EngineQueueMapping> engineQueueMappings = new ArrayList<EngineQueueMapping>();
        ObjectMapper mapper = new ObjectMapper();
        EngineQueueMapping queueMapping = new EngineQueueMapping();
        //File file = new File("/tmp/mappings.json");
        File file = new File("/home/anirudh/scheduler/mappings.json");
        boolean flagEngineDetailsPresent = false;
        try {

            BufferedReader reader = new BufferedReader(new FileReader(file));

            String res = readAll(reader);
            if(!res.equalsIgnoreCase("")) {
                engineQueueMappings = mapper.readValue(res, new TypeReference<ArrayList<EngineQueueMapping>>() {
                });


                for (EngineQueueMapping engineQueue : engineQueueMappings) {
                    if (engineQueue.getIpAddress().equalsIgnoreCase(hostName)) {
                        flagEngineDetailsPresent = true;
                        returnQueueName = engineQueue.getQueueName();
                        break;
                    }
                }
            }
            if (!flagEngineDetailsPresent){
                createQueue(queueName);
                queueMapping.setIpAddress(hostName);
                queueMapping.setQueueName(queueName);
                engineQueueMappings.add(queueMapping);
                mapper.writeValue(file, engineQueueMappings);
                returnQueueName = queueName;


            }

            setEngineMaxThreads(hostName);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnQueueName;
    }

    public static void setEngineMaxThreads(String hostName){

        try {
            javax.naming.Context c = new InitialContext();
            EngineInformationManager manager= (EngineInformationManager) c.lookup("java:global/ejb/EngineInformationManager");

            manager.setEngineStatus(hostName);

        } catch (NamingException e) {
            e.printStackTrace();
        }

    }

    public static String fetchQueueName(String ipAddress){
        ObjectMapper mapper = new ObjectMapper();
        //File file = new File("/tmp/mappings.json");
        File file = new File("/home/anirudh/scheduler/mappings.json");
        List<EngineQueueMapping> engineQueueMappings = new ArrayList<EngineQueueMapping>();
        String returnQueueName = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String res = readAll(reader);
            if(!res.equalsIgnoreCase("")) {
                engineQueueMappings = mapper.readValue(res, new TypeReference<ArrayList<EngineQueueMapping>>() {
                });
            }

            for (EngineQueueMapping engineQueue : engineQueueMappings) {
                if (engineQueue.getIpAddress().equalsIgnoreCase(ipAddress)) {
                    returnQueueName = engineQueue.getQueueName();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  returnQueueName;
    }

    public static void updateEngineInformationManager(){
        List<WorkflowEngineInstance> workflowEngineInstances = getEngineStatus();
        for(int i = 0 ; i < workflowEngineInstances.size(); i++){
            if (workflowEngineInstances.get(i).getStatus() == 1)
                setEngineMaxThreads(workflowEngineInstances.get(i).getIpAddress());
        }
    }
}
