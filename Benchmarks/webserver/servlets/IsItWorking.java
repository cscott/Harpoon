/*
 * Copyright (c) 1997-1999 The Java Apache Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * 4. The names "Apache JServ", "Apache JServ Servlet Engine" and
 *    "Java Apache Project" must not be used to endorse or promote products
 *    derived from this software without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JServ"
 *    nor may "Apache" nor "Apache JServ" appear in their names without
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Group. For more information
 * on the Java Apache Project and the Apache JServ Servlet Engine project,
 * please see <http://java.apache.org/>.
 *
 */

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This servlet is used to tell the if the Apache JServ installation was
 * successful.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 */

public class IsItWorking extends HttpServlet {

    public static final String TITLE = "Yes, It's working!";

    public void service (HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        // set content type and other response header fields first
        response.setContentType("text/html");

        // get the communication channel with the requesting client
        PrintWriter out = response.getWriter();

        // get the server identification
        String server = getServletConfig().getServletContext().getServerInfo();

        // write the data
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">"
            + "<HTML>"
            + "<HEAD>"
            + " <TITLE>" + TITLE + "</TITLE>"
            + " <META NAME=\"Author\" CONTENT=\"" + server + "\">"
            + "</HEAD>"
            + "<BODY BGCOLOR=\"#FFFFFF\">"
            + " <CENTER>"
            + "  <IMG "
//default configuration just serves /jserv from localhost for security reasons:
	    + "ALT=\"[ Picture broken ? Access this from localhost ]\" "
	    + "SRC=\"/jserv/status?image\" BORDER=\"0\">"
            + "  <H1>" + TITLE + "</H1>"
            + "  <H2>Congratulations, " + server + " is working!</H2>"
            + "  <H3>[ local time is <font color='#FF9900'>" 
	    + new java.util.Date() + "</font> ]</H3>"
            + "  <FONT SIZE=\"-1\">Copyright (c) 1997-99"
            + "  <A HREF=\"http://java.apache.org/\">The Java Apache Project</a><br>"
            + "  All rights reserved.</FONT>"
            + " </CENTER>"
            + "</BODY>"
            + "</HTML>");
    }
}

