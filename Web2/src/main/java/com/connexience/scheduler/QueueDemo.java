package com.connexience.scheduler;

import org.jboss.dmr.ModelNode;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;

/**
 * Created by naa166 - Anirudh Agarwal on 13/05/2015.
 */
public class QueueDemo extends HttpServlet{

    public static Message test;
    public static ModelNode modelNode;


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        try {
            JMSQueueListner.initialSetup();
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
        PrintWriter out = response.getWriter();

        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Demo Class</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Finder at " + request.getContextPath() + "</h1>");
            out.println("<h1> " + test + "</h1>");
            out.println("<h1> Trying to create temporary queue and sending response to it</h1>");


            /*try {
                JMSQueueListner.pushMessageToTemporaryQueue(test);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }*/
            out.println("<h1> The method to create temporary queue and push message to it has been called</h1>");

            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

}
