package com.connexience.scheduler;

import com.connexience.performance.model.WorkflowEngineStats;
import com.connexience.server.util.SerializationUtils;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import java.io.*;
import java.util.Calendar;
import java.util.logging.Logger;

/**
 * Created by naa166 - Anirudh Agarwal on 20/07/2015.
 */



@MessageDriven(name = "WorkflowStatusMDB",
        activationConfig
                = {
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
                @ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/workflowstatus")
        }, mappedName = "WorkflowStatus")
public class WorkflowStatusMDB implements MessageListener{

    @Inject
    private EntityManager em;

    @Inject
    private Logger logger;

    @Resource
    private javax.ejb.MessageDrivenContext mdc;



    @Override
    public void onMessage(Message message) {

        Calendar calendar = Calendar.getInstance();
        System.out.println("Inside Scheduler WorkflowStatusMDB");

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
                    manager.updateEngineStatus(engineIp,false,true);
                }


                String filename1 = "/home/anirudh/temp/InsideSchedulerMDB" + calendar.getTime();
                File file1 = new File(filename1);
                try {

                    if(!file1.exists())
                        try {
                            file1.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                } catch (Exception e) {
                    e.printStackTrace();
                }
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
