/*
 * @(#)HttpSessionBindingEvent.java	1.6 97/10/15
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

import java.util.EventObject;


/**
 * This event is communicated to a HttpSessionBindingListener whenever the 
 * listener is bound to or unbound from a HttpSession value.  
 *
 * <p>The event's
 * source is the HttpSession: binding occurs with a call to 
 * HttpSession.putValue; unbinding occurs with a call to HttpSession.removeValue. 
 *
 * @see HttpSession
 * @see HttpSessionBindingListener
 *
 * @version	1.6, 10/15/97
 */
public
class HttpSessionBindingEvent
extends EventObject
{
    /* The name to which the object is being bound or unbound */
    private String name;

    /**
     * Constructs a new HttpSessionBindingEvent
	 *
     * @param session the session acting as the source of the event
	 * @param name the name to which the object is being bound or 
	 * the name from which the object is being unbound 
     */
    public HttpSessionBindingEvent (HttpSession session, String name)
    {
	super (session);
	this.name = name;
    }

    /**
     * Returns the name to which the object is being bound or the name
     * from which the object is being unbound.
     */
    public String getName ()
    {
	return name;
    }

    /**
     * Returns the session into which the listener is being bound or
     * from which the listener is being unbound.
     */
    public HttpSession getSession ()
    {
	return (HttpSession) getSource ();
    }
}







