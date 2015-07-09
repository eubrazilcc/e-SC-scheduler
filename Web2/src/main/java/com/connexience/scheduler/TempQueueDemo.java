package com.connexience.scheduler;


import javax.jms.Message;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by naa166 - Anirudh Agarwal on 13/05/2015.
 */
public class TempQueueDemo extends HttpServlet{

    public static Message test;
   // public static ClientMessage reply;


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

            JMSQueueListner.tempQueueAccess();

        PrintWriter out = response.getWriter();

        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Demo Class</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Finder at " + request.getContextPath() + "</h1>");
            out.println("<h1>This is for the temp queue </h1>");
           // out.println("<h1> " + reply + "</h1>");
            out.println("<h1> " + test + "</h1>");
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

}
