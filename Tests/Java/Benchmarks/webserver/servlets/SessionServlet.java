/*
 * @(#)SessionServlet.java	1.21 97/05/22
 * 
 * Copyright (c) 1996-1997 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * CopyrightVersion 1.0
 */

import java.io.*;
import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * This is a simple example of an HTTP Servlet that uses the HttpSession
 * class
 *
 * Note that in order to gaurentee that session response headers are
 * set correctly, the session must be retrieved before any output is
 * sent to the client.
 */
public class SessionServlet extends HttpServlet { 

    public void doGet (HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
      {
       	  //Get the session object
	  HttpSession session = req.getSession(true);
	  
	  // set content type and other response header fields first
	  res.setContentType("text/html");

	  // then write the data of the response
	  PrintWriter out = res.getWriter();

	  out.println("<HEAD><TITLE> " + "SessionServlet Output " +
		      "</TITLE></HEAD><BODY>");
	  out.println("<h1> SessionServlet Output </h1>");
	  
	  Integer ival = (Integer) session.getValue("sessiontest.counter");
	  if (ival==null) ival = new Integer(1);
	  else ival = new Integer(ival.intValue() + 1);
	  session.putValue("sessiontest.counter", ival);
	  out.println("You have hit this page <b>" + ival + "</b> times.<p>");
	  out.println("Click <a href=" + res.encodeUrl("/servlet/session") +
		      ">here</a>");
	  out.println(" to ensure that session tracking is working even " +
		      "if cookies aren't supported.<br>");
	  out.println("Note that by default URL rewriting is not enabled" +
		      "due to it's expensive overhead");
	  out.println("<p>");
	  
	  out.println("<h3>Request and Session Data:</h3>");
	  out.println("Session ID in Request: " +
		      req.getRequestedSessionId());
	  out.println("<br>Session ID in Request from Cookie: " +
		      req.isRequestedSessionIdFromCookie());
	  out.println("<br>Session ID in Request from URL: " +
		      req.isRequestedSessionIdFromUrl());
	  out.println("<br>Valid Session ID: " +
		      req.isRequestedSessionIdValid());
	  out.println("<h3>Session Data:</h3>");
	  out.println("New Session: " + session.isNew());
	  out.println("<br>Session ID: " + session.getId());
	  out.println("<br>Creation Time: " + session.getCreationTime());
	  out.println("<br>Last Accessed Time: " +
		      session.getLastAccessedTime());
	  out.println("<h3>Session Context Data:</h3>");
	  HttpSessionContext context = session.getSessionContext();

	  for (Enumeration e = context.getIds(); e.hasMoreElements() ;) {
	      out.println("Valid Session: " +
			  (String)e.nextElement()+ "<br>");
	  }
	  
	  out.println("</BODY>");
	  out.close();
      }
    
    public String getServletInfo() {
        return "A simple servlet";
    }
}
