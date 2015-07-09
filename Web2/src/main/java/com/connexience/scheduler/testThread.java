/*
package com.connexience.scheduler;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

*/
/**
 * Created by naa166 - Anirudh Agarwal on 01/07/2015.
 *//*


@Singleton
@Startup
@EJB(name = "java:global/ejb/testThread", beanInterface = testThread.class)
public class testThread extends Thread {



    private static ArrayList<String> messageArray = new ArrayList<String>();

    public static ArrayList<String> getMessageArray() {
        return messageArray;
    }


    @PostConstruct
    public void startup(){
        this.start();
    }
    @Override
    public void run(){


        while (true){
        try {
            */
/*javax.naming.Context c = new InitialContext();
            MessageStorageSingletonBean messageStorageBean = (MessageStorageSingletonBean) c.lookup("");
            messageStorageBean.setThreadRunFlag(true);*//*


            Date date = new Date();
            if(messageArray.size() <11) {
                messageArray.add("hello" + date.toString());
            }

            try{
                Thread.sleep(10*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }}
}
*/
