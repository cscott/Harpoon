

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * This is a simple example of an HTTP Servlet.  It responds to the GET
 * and HEAD methods of the HTTP protocol.
 */
public class Hello extends HttpServlet
{ 
    /**
     * Handle the GET and HEAD methods by building a simple web page.
     * HEAD is just like GET, except that the server returns only the
     * headers (including content length) not the body we write.
     */
    public void doGet (HttpServletRequest request,
                       HttpServletResponse response) 
        throws ServletException, IOException
        {
            PrintWriter out;
            String title = "Example Apache JServ Servlet";

            // set content type and other response header fields first
            response.setContentType("text/html");

            // then write the data of the response
            out = response.getWriter();
            
            out.println("<HTML><HEAD><TITLE>");
            out.println(title);
            out.println("</TITLE></HEAD><BODY bgcolor=\"#FFFFFF\">");
            out.println("<H1>" + title + "</H1>");
            out.println("<H2> Congratulations, ApacheJServ 1.1 is working!<br>");
            out.println("</BODY></HTML>");
        }
}
 
