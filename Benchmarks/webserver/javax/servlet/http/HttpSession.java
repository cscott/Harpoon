/*
 * @(#)HttpSession.java	1.15 97/10/28
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

/**
 * The HttpSession interface is implemented by services to provide an 
 * association
 * between an HTTP client and HTTP server. This association, or 
 * <em>session</em>, 
 * persists over multiple connections and/or requests during a given 
 * time period. 
 * Sessions are used to
 * maintain state and user identity across multiple page requests.  
 *
 * <P>A session can be maintained either by using cookies or by URL 
 * rewriting. To expose whether the client supports 
 * cookies, HttpSession 
 * defines an isCookieSupportDetermined method and an isUsingCookies method. 
 *
 * <P>HttpSession defines methods which store these types of data:
 * <UL>
 * <LI>Standard session properties, such as an identifier for the session,
 * and the context for the session.
 * <LI>Application layer data, accessed using this interface
 * and stored using a dictionary-like interface.
 * </UL>
 * <P>The following code snippet illustrates getting and setting the the 
 * session data value.
 *
 * <pre>
 *       
 * //Get the session object - "request" represents the HTTP servlet request
 * HttpSession session = request.getSession(true);
 * <BR>
 * //Get the session data value - an Integer object is read from 
 * //the session, incremented, then written back to the session.
 * //sessiontest.counter identifies values in the session
 * Integer ival = (Integer) session.getValue("sessiontest.counter");
 * if (ival==null) 
 *     ival = new Integer(1);
 * else 
 *     ival = new Integer(ival.intValue() + 1);
 * session.putValue("sessiontest.counter", ival);
 *
 * </pre>
 *
 * <P> When an application layer stores or removes data from the
 * session, the session layer checks whether the object implements
 * HttpSessionBindingListener.  If it does, then the object is notified
 * that it has been bound or unbound from the session.
 *
 * <P>An implementation of HttpSession represents the server's view
 * of the session. The server considers a session to be new until 
 * it has been joined by the 
 * client.  Until the client joins the session, the isNew method  
 * returns true. A value of true can indicate one of these three cases:
 * <UL>
 * <LI>the client does not yet know about the session
 * <LI>the session has not yet begun
 * <LI>the client chooses not to join the session. This case will occur 
 * if the client supports 
 * only cookies and chooses to reject any cookies sent by the server. 
 * If the server supports URL rewriting, this case will not commonly occur.
 * </UL>
 *
 * <P>It is the responsibility of developers 
 * to design their applications to account for situations where a client  
 * has not joined a session. For example, in the following code  
 * snippet isNew is called to determine whether a session is new. If it 
 * is, the server will require the client to start a session by directing 
 * the client to a welcome page <tt>welcomeURL</tt> where
 * a user might be required to enter some information and send it to the 
 * server before gaining access to 
 * subsequent pages. 
 *
 * <pre>
 * //Get the session object - "request" represents the HTTP servlet request
 * HttpSession session = request.getSession(true);
 * <BR>
 * //insist that the client starts a session
 * //before access to data is allowed
 * //"response" represents the HTTP servlet response
 * if (session.isNew()) {
 *     response.sendRedirect (welcomeURL);
 * }
 *
 * </pre>
 *
 * @see HttpSessionBindingListener
 * @see HttpSessionContext
 *
 * @version	1.15, 10/28/97
 */
   public
   interface HttpSession
{
/**
 * Returns the identifier assigned to this session. An HttpSession's 
 * identifier is a unique string that is created and maintained by 
 * HttpSessionContext.
 *
 * @return the identifier assigned to this session
 * @exception IllegalStateException if an attempt is made to access  
 * session data after the session has been invalidated
 */
   public String getId ();

/**
 * Returns the context in which this session is bound. 
 *
 * @return the name of the context in which this session is bound  
 * @exception IllegalStateException if an attempt is made to access 
 * session data after the session has been invalidated
 */
   public HttpSessionContext getSessionContext ();

/**
 * Returns the time at which this session representation was created,
 * in milliseconds since midnight, January 1, 1970 UTC.
 * 
 * @return the time when the session was created
 * @exception IllegalStateException if an attempt is made to access  
 * session data after the session has been invalidated
 */
   public long getCreationTime ();

/**
 * Returns the last time the client sent a request carrying the identifier
 * assigned to the session. Time is expressed
 * as milliseconds since midnight, January 1, 
 * 1970 UTC. 
 * Application level operations, such as getting or setting a value
 * associated with the session, does not affect the access time.
 *
 * <P> This information is particularly useful in session management
 * policies.  For example,
 * <UL>
 * <LI>a session manager could leave all sessions
 * which have not been used in a long time 
 * in a given context.
 * <LI>the sessions can be sorted according to age to optimize some task.
 * </UL>
 *
 * @return the last time the client sent a request carrying the identifier 
 * assigned to the session
 * @exception IllegalStateException if an attempt is made to access 
 * session data after the session has been invalidated
 */
   public long getLastAccessedTime ();

/**
 * Causes this representation of the session to be invalidated and removed 
 * from its context.  
 *
 * @exception IllegalStateException if an attempt is made to access  
 * session data after the session has been invalidated
 */
   public void invalidate ();

/**
 * Binds the specified object into the session's application layer data
 * with the given name.  Any existing binding with the same name is
 * replaced.  New (or existing) values that implement the
 * HttpSessionBindingListener interface will call its  
 * valueBound() method.
 *
 * @param name the name to which the data object will be bound.  This
 * parameter cannot be null.
 * @param value the data object to be bound.  This parameter cannot be null. 
 * @exception IllegalStateException if an attempt is made to access  
 * session data after the session has been invalidated
 */
   public void putValue (String name, Object value);

/**
 * Returns the object bound to the given name in the session's
 * application layer data.  Returns null if there is no such binding.
 *
 * @param name the name of the binding to find
 * @return the value bound to that name, or null if the binding does
 * not exist.
 * @exception IllegalStateException if an attempt is made to access 
 * HttpSession's session data after it has been invalidated
 */
   public Object getValue (String name);

/**
 * Removes the object bound to the given name in the session's
 * application layer data.  Does nothing if there is no object
 * bound to the given name.  The value that implements the
 * HttpSessionBindingListener interface will call its
 * valueUnbound() method.
 *
 * @param name the name of the object to remove
 * @exception IllegalStateException if an attempt is made to access 
 * session data after the session has been invalidated
 */
   public void removeValue (String name);

/**
 * Returns an array of the names of all the application layer
 * data objects bound into the session. For example, if you want to delete
 * all of the data objects bound into the session, use this method to 
 * obtain their names. 
 * 
 * @return an array containing the names of all of the application layer 
 * data objects bound into the session
 * @exception IllegalStateException if an attempt is made to access  
 * session data after the session has been invalidated
 */
   public String [] getValueNames ();

/**
 * A session is considered to be "new" if it has been created by the server, 
 * but the client has not yet acknowledged joining the session. For example,
 * if the server supported only cookie-based sessions and the client had 
 * completely disabled the use of cookies, then calls to
 * HttpServletRequest.getSession() would 
 * always return "new" sessions. 
 *
 * @return true if the session has been created by the server but the 
 * client has not yet acknowledged joining the session; false otherwise
 * @exception IllegalStateException if an attempt is made to access  
 * session data after the session has been invalidated
 */
   public boolean isNew ();

}

