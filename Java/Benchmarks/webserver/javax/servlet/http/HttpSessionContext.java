/*
 * @(#)HttpSessionContext.java	1.9 97/10/15
 * 
 * Copyright (c) 1997 Sun Microsystems, Inc. All Rights Reserved.
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


package javax.servlet.http;

import java.util.Enumeration;

/**
 * A HttpSessionContext is a grouping of HttpSessions associated with a single
 * entity. This interface gives servlets access to  
 * methods for listing the IDs and for retrieving a session based on its ID.
 *
 * <p>Servlets get the HttpSessionContext object by calling the getSessionContext() 
 * method of HttpSession. 
 *
 * @see HttpSession
 *
 * @version	1.9, 10/15/97
 */
public
interface HttpSessionContext
{
    /**
     * Returns the session bound to the specified session ID. 
	 *
	 * @param sessionID the ID of a particular session object
	 * @return the session name. Returns null if the session ID does not refer
	 * to a valid session.
     */
    public HttpSession getSession (String sessionId);
  
    /**
     * Returns an enumeration of all of the session IDs in this context.
	 *
	 * @return an enumeration of all session IDs in this context
     */
    public Enumeration getIds ();
}



