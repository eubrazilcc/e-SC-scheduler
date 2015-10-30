package com.connexience.scheduler;

import com.connexience.performance.model.WorkflowEngineStats;
import com.connexience.server.util.SerializationUtils;
import com.connexience.server.workflow.engine.WorkflowInvocation;

import javax.ejb.*;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.*;

/**
 * Created by naa166 - Anirudh Agarwal on 20/07/2015.
 */



@MessageDriven(name = "WorkflowStatusMDB",
        activationConfig
                = {
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
                @ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/workflowstatus")
        }, mappedName = "WorkflowStatus")
public class WorkflowStatusMDB implements MessageListener {




    @Override
    public void onMessage(Message message) {


        try {
            BytesMessage bm = (BytesMessage) message;

            //message.acknowledge();

            bm.reset();
            byte[] messageData = new byte[(int) bm.getBodyLength()];
            bm.readBytes(messageData);

            //Deserialise and interpret the object
            Object payload = SerializationUtils.deserialize(messageData);

            if (payload instanceof WorkflowEngineStats){
                //em.persist((WorkflowEngineStats)payload);
                //em.flush();

                if(((WorkflowEngineStats) payload).isInvocationFinished()){
                    Context c = new InitialContext();
                    EngineInformationManager manager = (EngineInformationManager) c.lookup("java:global/ejb/EngineInformationManager");

                    String engineIp = ((WorkflowEngineStats) payload).getIpAddress();
                    System.out.println("in scheduler workflowStatusMDB, calling updateEngineStats");
                    manager.updateEngineStatus(engineIp,false,true);
                }
            }
            else if(payload instanceof WorkflowInvocation){
                System.out.println("got Workflow invocation message");
                WorkflowInvocation invocation = (WorkflowInvocation)payload;
                EngineInformationManager manager = new EngineInformationManager();
                manager.freeResourceInformation(invocation.getInvocationId());
            }

        } catch (JMSException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
