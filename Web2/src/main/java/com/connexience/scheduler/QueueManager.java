package com.connexience.scheduler;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Jacek on 15/12/2015.
 */
public abstract class QueueManager
{
    private static final Logger _Logger = LoggerFactory.getLogger(QueueManager.class);


    // TODO: Think where is the best place to put this method
    public static String GetQueueNameForEngine(String engineId)
    {
        return "engine-" + engineId;
    }


    private static boolean _WasSuccess(ModelNode node)
    {
        return ClientConstants.SUCCESS.equals(node.get(ClientConstants.OUTCOME).asString());
    }


    private static boolean _ResourceExists(ModelNode resource, ModelControllerClient client) throws IOException
    {
        ModelNode op = new ModelNode();
        op.get(ClientConstants.OP).set("read-resource-description");
        op.get(ClientConstants.OP_ADDR).set(resource.get(ClientConstants.OP_ADDR));
        return _WasSuccess(client.execute(op));
    }


    /**
     *
     * @param queueName
     * @return true iff the queue was successfully created, false if the queue already exists
     * @throws IOException if creating a queue is impossible
     */
    public static boolean CreateQueue(String queueName) throws IOException
    {
        ModelNode op = new ModelNode();

        ModelNode address = op.get(ClientConstants.OP_ADDR);
        address.add("subsystem", "messaging");
        address.add("hornetq-server", "default");
        address.add("jms-queue", queueName);

        // the JNDI entries
        ModelNode entries = op.get("entries");
        entries.add(queueName);
        entries.add("jboss/exported/" + queueName);

        try (ModelControllerClient client = ModelControllerClient.Factory.create(InetAddress.getByName("localhost"), 9999)) {
            // TODO: Checking whether the queue exists seems not to work properly...
            //if (_ResourceExists(op, client)) {
            //    _Logger.info("Queue {} already exists", queueName);
            //    return false;
            //}

            op.get(ClientConstants.OP).set(ClientConstants.ADD);
            ModelNode result = client.execute(op);
            _Logger.debug("Response from jBoss AS: {}", result.toString());

            if (_WasSuccess(result)) {
                _Logger.info("Queue {} successfully created", queueName);
                return true;
            } else {
                _Logger.error("Cannot create queue {}: response from jBoss AS: {}", queueName, result.toString());
            }
        }

        return false;
    }


    public static boolean DestroyQueue(String queueName) throws IOException
    {
        ModelNode op = new ModelNode();

        ModelNode address = op.get(ClientConstants.OP_ADDR);
        address.add("subsystem", "messaging");
        address.add("hornetq-server", "default");
        address.add("jms-queue", queueName);

        op.get(ClientConstants.OP).set("remove");

        try (ModelControllerClient client = ModelControllerClient.Factory.create(InetAddress.getByName("localhost"), 9999)) {
            ModelNode result = client.execute(op);
            _Logger.debug("Response from jBoss AS: {}", result.toString());

            if (_WasSuccess(result)) {
                _Logger.info("Queue {} successfully removed", queueName);
                return true;
            } else {
                _Logger.error("Cannot remove queue {}: response from jBoss AS: {}", queueName, result.toString());
            }
        }

        return false;
    }


    public static void PushMessage(ConnectionFactory jmsConnectionFactory, String queueName, Message message)
    throws JMSException, NamingException
    {
        Context context = new InitialContext();
        Queue targetQueue = (Queue) context.lookup(queueName);

        Connection connection = null;
        Session session = null;
        MessageProducer messageProducer = null;

        try {
            connection = jmsConnectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            messageProducer = session.createProducer(targetQueue);

            // messageProducer.setPriority(10);
            messageProducer.send(message);
        } finally {
            if (messageProducer != null) {
                try {
                    messageProducer.close();
                } catch (JMSException x) {
                    _Logger.warn("Cannot close message produces for queue: " + queueName, x);
                }
            }

            if (session != null) {
                try {
                    session.close();
                } catch (JMSException x) {
                    _Logger.warn("Cannot close session for queue: " + queueName, x);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException x) {
                    _Logger.warn("Cannot close connection for queue: " + queueName, x);
                }
            }
        }
    }
}