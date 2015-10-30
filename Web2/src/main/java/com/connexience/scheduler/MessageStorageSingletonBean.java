package com.connexience.scheduler;

import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.jms.Message;
import java.util.ArrayList;

/**
 * Created by naa166 - Anirudh Agarwal on 01/07/2015.
 */

@Singleton
@EJB(name = "java:global/ejb/MessageStorageSingletonBean", beanInterface = MessageStorageSingletonBean.class)
public class MessageStorageSingletonBean {


    @Lock(LockType.READ)
    public static ArrayList<String> getMessageArray() {
        return messageArray;
    }

    private static ArrayList<String> messageArray = new ArrayList<String>();
    private static ArrayList<Message> jmsMessageArray = new ArrayList<Message>();

    public static String getResult() {
        return result;
    }

    public static void setResult(String result) {
        MessageStorageSingletonBean.result = result;
    }

    private static String result;

    private static boolean ThreadRunFlag = false;

    public static boolean isThreadRunFlag() {
        return ThreadRunFlag;
    }

    public static void setThreadRunFlag(boolean threadRunFlag) {
        ThreadRunFlag = threadRunFlag;
    }

    @Lock(LockType.WRITE)
    public void addToMessageArray(String value){
        messageArray.add(value);
    }

    @Lock(LockType.WRITE)
    public void removeFromMessageArray(){
        messageArray.remove(0);
    }


    @Lock(LockType.WRITE)
    public void addMessageToArray(Message message) {
        jmsMessageArray.add(message);
    }

    @Lock
    public Message getMessage(){
        Message message = jmsMessageArray.get(0);
        jmsMessageArray.remove(0);
        return message;
    }
}
