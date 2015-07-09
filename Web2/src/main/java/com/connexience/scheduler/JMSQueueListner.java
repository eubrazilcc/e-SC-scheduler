package com.connexience.scheduler;


import com.connexience.performance.client.PerformanceLoggerClient;
import com.connexience.server.util.SerializationUtils;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.*;
import org.hornetq.api.core.management.ManagementHelper;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.pipeline.core.xmlstorage.prefs.PreferenceManager;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by naa166 - Anirudh Agarwal on 13/05/2015.
 */

public class JMSQueueListner {

    private static String DestType = "Workflow";

    /*@Resource(name = "Connectionfactory")
    private static ConnectionFactory connectionFactory;

    @Resource(lookup = "jms/Workflow")
    private static Queue queue;*/

    public static Destination destination = null;
    public static void initialSetup() throws JMSException, NamingException {

        Context jndiContext = new InitialContext();

        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
        Queue queue = (Queue) jndiContext.lookup("Workflow");


        destination = (Destination)queue;

        Connection connection = connectionFactory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        MessageConsumer messageConsumer = session.createConsumer(destination);

        ListnerInterface listnerInterface = new ListnerInterface();
        messageConsumer.setMessageListener(listnerInterface);

        connection.start();

        /*session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        temporaryQueue = session.createTemporaryQueue();
        jndiContext.bind("jms/testQueue", temporaryQueue);
        Destination tempDestination = temporaryQueue;
        MessageConsumer responseConsumer = session.createConsumer(tempDestination);
        TempQueueListnerInterface tempQueueListnerInterface = new TempQueueListnerInterface();
        responseConsumer.setMessageListener(tempQueueListnerInterface);*/
    }

    public static void tempQueueAccess(){

        try {
            Context jndiContext = new InitialContext();

            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            Queue queue = (Queue) jndiContext.lookup("testQueue");

            destination = (Destination)queue;

            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer messageConsumer = session.createConsumer(destination);

            TempQueueListnerInterface tempListnerInterface = new TempQueueListnerInterface();
            messageConsumer.setMessageListener(tempListnerInterface);

            connection.start();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static void createTemporaryQueue(){


           /* TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName());
            ServerLocator locator = HornetQClient.createServerLocatorWithoutHA(transportConfiguration);
            ClientSessionFactory factory = locator.createSessionFactory();
            ClientSession coreSession = factory.createSession(false, false, false);
            coreSession.createQueue("queue","testQueue","testQueue",true);*/
        //ManagementHelper.putOperationInvocation(clientMessage, "jms.server", "createQueue", "testQueue");

        ModelControllerClient client = null;
        ModelNode op = new ModelNode();
        ModelNode address = op.get(ClientConstants.OP_ADDR);
        address.add("subsystem", "messaging");
        address.add("hornetq-server", "default");


        // the name of the queue
        String queue = "testQueue";
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
            CreateTemporaryQueue.modelNode = result;
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

    public static void pushMessageToTemporaryQueue(Message message) throws UnknownHostException {

        //createTemporaryQueue();

        try {
            Context context = new InitialContext();

            Queue sendQueue = (Queue) context.lookup("testQueue");
            ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("ConnectionFactory");

            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = sendQueue;
            MessageProducer messageProducer = session.createProducer(destination);

            messageProducer.send(message);


        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }

        /*try {
            Context tempJndiContext = new InitialContext();

            ConnectionFactory tempConnectionFactory = (ConnectionFactory) tempJndiContext.lookup("ConnectionFactory");
            Queue temporaryQueue = (Queue) tempJndiContext.lookup("testQueue");
            Connection tempConnection = tempConnectionFactory.createConnection();
            tempConnection.start();
            Session tempSession = tempConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            //Try creating the temporary queue

            //tempJndiContext.bind("testQueue", temporaryQueue);

            //create message producer object and message object
            Destination tempDestination = temporaryQueue;
            MessageProducer tempResponseProducer = tempSession.createProducer(tempDestination);
            ObjectMessage objectMessage = tempSession.createObjectMessage();

            //set the objest message
            objectMessage.setObject((Serializable) message);

            //TempQueueListnerInterface tempQueueListnerInterface = new TempQueueListnerInterface();
            //tempResponseConsumer.setMessageListener(tempQueueListnerInterface);

            //send the message to the temporary queue
            tempResponseProducer.send(message);

            //Close the session and the connection
            tempSession.close();
            tempConnection.close();

        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {


        }*/

        /*try {
            sendMessage("testQueue", (Serializable) message);
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
    }


   /* *//**
     * Send the message
     *
     * @param queueName queuename
     * @param message graph operation
     * @throws Exception something went wrong
     *//*
    public static void sendMessage(String queueName, Serializable message) throws Exception {
        if (PerformanceLoggerClient.enabled) {
            try {
                Connection connection = null;
                try {
                    Queue queue = HornetQJMSClient.createQueue(queueName);

                    String jMSServerURLHost = PreferenceManager.getSystemPropertyGroup("Performance").stringValue("JMSServerHost", "localhost");
                    int jMSServerURLPort = PreferenceManager.getSystemPropertyGroup("Performance").intValue("JMSServerPort", 5445);

                    Map<String, Object> connectionParams = new HashMap();
                    connectionParams.put(TransportConstants.HOST_PROP_NAME, jMSServerURLHost);
                    connectionParams.put(TransportConstants.PORT_PROP_NAME, jMSServerURLPort);

                    TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName(),
                            connectionParams);

                    HornetQConnectionFactory cf = HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.QUEUE_CF, transportConfiguration);

                    String jMSUser = PreferenceManager.getSystemPropertyGroup("Performance").stringValue("JMSUser", "connexience");
                    String jMSPassword = PreferenceManager.getSystemPropertyGroup("Performance").stringValue("JMSPassword", "1234");

                    connection = cf.createConnection(jMSUser, jMSPassword);

                    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                    MessageProducer producer = session.createProducer(queue);
                    producer.setDeliveryMode(DeliveryMode.PERSISTENT);

                    BytesMessage bm = session.createBytesMessage();

                    byte[] data = SerializationUtils.serialize(message);
                    bm.writeBytes(data);
                    producer.send(bm);

                } finally {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (Exception e) {
                PerformanceLoggerClient.enabled = false;
            }
        }
    }
*/
}
