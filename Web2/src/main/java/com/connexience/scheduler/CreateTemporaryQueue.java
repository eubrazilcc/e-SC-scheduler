package com.connexience.scheduler;

import org.jboss.dmr.ModelNode;

import javax.jms.Message;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.UnknownHostException;

/**
 * Created by naa166 - Anirudh Agarwal on 18/05/2015.
 */
public class CreateTemporaryQueue extends HttpServlet{

    public static Message message;


    public static ModelNode modelNode;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        JMSQueueListner.createTemporaryQueue();

        PrintWriter out = response.getWriter();



        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Demo Class</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1> Creating the temporary queue</h1>");
            out.println("<h1> Result = " + modelNode + "</h1>");
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

}
