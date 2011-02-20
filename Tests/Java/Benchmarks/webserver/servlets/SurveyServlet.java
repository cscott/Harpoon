/*
* @(#)SurveyServlet.java
*
* Copyright (c) 1995-1997 Sun Microsystems, Inc. All Rights Reserved.
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
*/

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * A sample single-threaded servlet that takes input from a form
 * and writes it out to a file.  It is single threaded to serialize
 * access to the file.  After the results are written to the file,
 * the servlet returns a "thank you" to the user.
 *
 * <p>You can run the servlet as provided, and only one thread will run
 * a service method at a time.  There are no thread synchronization
 * issues with this type of servlet, even though the service method
 * writes to a file.  (Writing to a file within a service method
 * requires synchronization in a typical servlet.)
 *
 * <p>You can also run the servlet without using the single thread
 * model by removing the <tt>implements</tt> statement.  Because the
 * service method does not synchronize access to the file, multiple
 * threads can write to it at the same time.  When multiple threads try
 * to write to the file concurrently, the data from one survey does not
 * follow the data from another survey in an orderly fashion.
 *
 * <p>To see interaction (or lack of interaction) between threads, use
 * at least two browser windows and have them access the servlet as
 * close to simultaneously as possible.  Expect correct results (that
 * is, expect no interference between threads) only when the servlet
 * implements the <code>SingleThreadedModel</code> interface.
 */

public class SurveyServlet extends HttpServlet 
    implements SingleThreadModel
{
    String resultsDir;
    
    public void init(ServletConfig config)
	throws ServletException
    {
	super.init(config);
        resultsDir = getInitParameter("resultsDir");
	if (resultsDir == null) {
	    Enumeration initParams = getInitParameterNames();
	    System.err.println("The init parameters were: ");
	    while (initParams.hasMoreElements()) {
		System.err.println(initParams.nextElement());
	    }
	    System.err.println("Should have seen one parameter name");
	    throw new UnavailableException (this,
		"Not given a directory to write survey results!");
	}
    }

    /**
     * Write survey results to output file in response to the POSTed
     * form.  Write a "thank you" to the client.     
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
        // first, set the "content type" header of the response
	res.setContentType("text/html");

	//Get the response's PrintWriter to return text to the client.
        PrintWriter toClient = res.getWriter();

        try {
            //Open the file for writing the survey results.
            String surveyName = req.getParameterValues("survey")[0];
            FileWriter resultsFile = new FileWriter(resultsDir
	        + System.getProperty("file.separator")
	        + surveyName + ".txt", true);
            PrintWriter toFile = new PrintWriter(resultsFile);

	    // Get client's form data & store it in the file
            toFile.println("<BEGIN>");
            Enumeration values = req.getParameterNames();
            while(values.hasMoreElements()) {
                String name = (String)values.nextElement();
		String value = req.getParameterValues(name)[0];
                if(name.compareTo("submit") != 0) {
                    toFile.println(name + ": " + value);
                }
            }
            toFile.println("<END>");

	    //Close the file.
            resultsFile.close();

	    // Respond to client with a thank you
	    toClient.println("<html>");
	    toClient.println("<title>Thank you!</title>");
            toClient.println("Thank you for participating");
	    toClient.println("</html>");

        } catch(IOException e) {
            e.printStackTrace();
            toClient.println(
		"A problem occured while recording your answers.  "
		+ "Please try again.");
        }

        // Close the writer; the response is done.
	toClient.close();
    }
}

