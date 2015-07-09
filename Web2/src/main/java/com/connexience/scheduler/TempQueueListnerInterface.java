package com.connexience.scheduler;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.io.File;
import java.io.IOException;

/**
 * Created by naa166 - Anirudh Agarwal on 13/05/2015.
 */
public class TempQueueListnerInterface implements MessageListener {

    public void onMessage(Message message) {


        TempQueueDemo.test = message;
    File file = new File("/home/anirudh/temp/testTemp");
    if(!file.exists())
            try {
        file.createNewFile();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
}
