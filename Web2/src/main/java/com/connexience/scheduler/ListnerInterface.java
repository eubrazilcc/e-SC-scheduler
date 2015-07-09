package com.connexience.scheduler;

import com.connexience.server.model.workflow.WorkflowInvocationMessage;
import com.connexience.server.util.SerializationUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.jms.client.HornetQBytesMessage;
import org.hornetq.jms.client.HornetQMessage;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.io.*;
import java.net.UnknownHostException;
import java.util.Calendar;

/**
 * Created by naa166 - Anirudh Agarwal on 13/05/2015.
 */
public class ListnerInterface implements MessageListener {


    public static Message saveMessage;


    public static void saveMessageToMongoDB(Message msg){

        try {
            MongoClient mongo = new MongoClient("localhost", 27017);

            // if database doesn't exists, MongoDB will create it
            DB db = mongo.getDB("testdb");

            // if collection doesn't exists, MongoDB will create it for you
            DBCollection table = db.getCollection("user");

            // create a document to store key and value
            BasicDBObject document = new BasicDBObject();
            document.put("messageType", msg.getClass().toString());
            document.put("testString", "This is a test String");

                /*int size = (int) ((BytesMessage) msg).getBodyLength();
                byte[] rawContent = new byte[size];

                msg.readBytes(rawContent);*/


            BytesMessage bm = (BytesMessage) msg;
            byte[] messageData = new byte[(int) bm.getBodyLength()];
            bm.readBytes(messageData);
            Object payload = SerializationUtils.deserialize(messageData);
            /*for (int i = 0; i < (int) byteMessage.getBodyLength(); i++) {
                byteArr[i] = byteMessage.readByte();
            }
            String finalMsg = new String(byteArr);*/

            WorkflowInvocationMessage workflowInvocationMessage = (WorkflowInvocationMessage) payload;
            String workflowId = workflowInvocationMessage.getWorkflowId();

            //String finalMsg = byteMessage.re;
            String finalMsg = payload.toString();
            document.put("messageBody", finalMsg);
            document.put("workflowId", workflowId);

            table.insert(document);


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onMessage(Message message) {

        Calendar calendar = Calendar.getInstance();
        //DisplayMessageContents.message = message;

        saveMessageToMongoDB(message);
        saveMessage = message;
        String filename = "/home/anirudh/temp/test" + calendar.getTime();
        QueueDemo.test = message;
        CreateTemporaryQueue.message = message;
        File file = new File(filename);
        try {

            if(!file.exists())
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            PrintWriter printWriter = new PrintWriter(filename,"utf-8");
            printWriter.println(message.getClass().toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            JMSQueueListner.pushMessageToTemporaryQueue(message);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
