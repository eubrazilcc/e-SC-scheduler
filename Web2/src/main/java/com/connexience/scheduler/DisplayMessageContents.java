package com.connexience.scheduler;

import com.mongodb.*;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by naa166 - Anirudh Agarwal on 19/05/2015.
 */
public class DisplayMessageContents extends HttpServlet{

    public static Message message = ListnerInterface.saveMessage;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");


        PrintWriter out = response.getWriter();


        try {

            MongoClient mongo = new MongoClient("localhost", 27017);

            // if database doesn't exists, MongoDB will create it
            DB db = mongo.getDB("testdb");

            // if collection doesn't exists, MongoDB will create it for you
            DBCollection table = db.getCollection("user");

            out.println("<html>");
            out.println("<head>");
            out.println("<title>Demo Class</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1> Display the message contents</h1>");

            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("testString", "This is a test String");

            DBCursor cursor = table.find(searchQuery);

            while (cursor.hasNext()) {
                out.println("<h4>" + cursor.next() + "</h4>");
            }


            //out.println("<h1>" + message + "</h1>");
            //out.println("<h1> The Message type is : " + message.getClass().toString() + "</h1>");
            //out.println("<h1> If it is of type text : " + textMessage.getText() + "</h1>");
            out.println("<h1> End of Method</h1>");
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }
}
