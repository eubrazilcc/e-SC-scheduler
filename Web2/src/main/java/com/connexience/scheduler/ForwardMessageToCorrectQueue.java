package com.connexience.scheduler;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Created by naa166 - Anirudh Agarwal on 10/06/2015.
 */
public class ForwardMessageToCorrectQueue {

    public static void pushMessage(String queueName,Message message){
        try {
            Context context = new InitialContext();

            Queue sendQueue = (Queue) context.lookup(queueName);
            ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("ConnectionFactory");

            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = sendQueue;
            MessageProducer messageProducer = session.createProducer(destination);

           // messageProducer.setPriority(10);


            messageProducer.send(message);

            connection.close();
            session.close();

        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
