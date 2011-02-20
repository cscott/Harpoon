/*
 * @(#)HttpSessionBindingListener.java	1.6 97/10/15
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

import java.util.EventListener;

/**
 * Objects implement this interface so that they can be notified  
 * when
 * they are being bound or unbound from a HttpSession. When a binding occurs 
 * (using HttpSession.putValue) HttpSessionBindingEvent communicates the event 
 * and identifies
 * the session into which the object is bound. 
 * 
 * <p>Similarly, when an unbinding occurs (using HttpSession.removeValue)
 * HttpSessionBindingEvent communicates the event and identifies the
 * session from which the object is unbound. 
 *
 * @see HttpSession
 * @see HttpSessionBindingEvent
 *
 * @version	1.6, 10/15/97
 */
public
interface HttpSessionBindingListener
extends EventListener
{
    /**
     * Notifies the listener that it is being bound into
     * a session.
     *
     * @param event the event identifying the session into
     * which the listener is being bound.
     */
    public void valueBound (HttpSessionBindingEvent event);

    /**
     * Notifies the listener that it is being unbound
     * from a session.
     *
     * @param event the event identifying the session from
     * which the listener is being unbound.
     */
    public void valueUnbound (HttpSessionBindingEvent event);
}

