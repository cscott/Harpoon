/*
 * @(#)UnavailableException.java	1.5 97/07/15
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
 * 
 * CopyrightVersion 1.0
 */

package javax.servlet;


/**
 * This exception indicates that a servlet is unavailable.  Servlets
 * may report this exception at any time, and the network service
 * running the servlet should behave appropriately.  There are two
 * types of unavailability, and sophisticated services will to deal
 * with these differently: <UL>
 * 
 * <LI> <em>Permanent</em> unavailability.  The servlet will not be
 * able to handle client requests until some administrative action is
 * taken to correct a servlet problem.  For example, the servlet might
 * be misconfigured, or the state of the servlet may be corrupted.
 * Well written servlets will log both the error and the corrective
 * action which an administrator must perform to let the servlet
 * become available.
 *
 * <LI> <em>Temporary</em> unavailability.  The servlet can not handle
 * requests at this moment due to a system-wide problem.  For example,
 * a third tier server might not be accessible, or there may be
 * insufficient memory or disk storage to handle requests.  The
 * problem may be self correcting, such as those due to excessive
 * load, or corrective action may need to be taken by an
 * administrator.
 *
 * </UL>
 *
 * <P> Network services may safely treat both types of exceptions as
 * "permanent", but good treatment of temporary unavailability leads
 * to more robust network services.  Specifically, requests to the
 * servlet might be blocked (or otherwise deferred) for a
 * servlet-suggested amount of time, rather than being rejected until
 * the service itself restarts.
 *
 * @version	1.5
 */
public 
class UnavailableException extends ServletException {
    private Servlet	servlet;		// what's unavailable
    private boolean	permanent;		// needs admin action?
    private int		seconds;		// unavailability estimate


    /**
     * 
     * Constructs a new exception with the specified descriptive
     * message, indicating that the servlet is permanently
     * unavailable.
     *
     * @param servlet the servlet which is unavailable
     * @param msg the descriptive message
     */
    public UnavailableException (Servlet servlet, String msg) {
	super (msg);
	this.servlet = servlet;
	permanent = true;
    }


    /**
     * Constructs a new exception with the specified descriptive message,
     * indicating that the servlet is temporarily unavailable and giving
     * an estimate of how long it will be unavailable.  In some cases, no
     * estimate can be made; this is indicated by a non-positive time.
     * For example, the servlet might know a server it needs is "down",
     * but not be able to report how long it will take to restore it to
     * an adequate level of functionality.
     *
     * @param seconds number of seconds that the servlet is anticipated
     *	to be unavailable.  If negative or zero, no estimate is available.
     * @param servlet the servlet which is unavailable
     * @param msg the descriptive message
     */
    public UnavailableException (int seconds, Servlet servlet, String msg) {
	super (msg);
	this.servlet = servlet;
	if (seconds <= 0)
	    seconds = -1;
	else
	    this.seconds = seconds;
	permanent = false;
    }


    /**
     * Returns true if the servlet is "permanently" unavailable,
     * indicating that the service administrator must take some
     * corrective action to make the servlet be usable.
     */
    public boolean isPermanent () {
	return permanent;
    }


    /**
     * Returns the servlet that is reporting its unavailability.
     */
    public Servlet getServlet () {
	return servlet;
    }


    /**
     * Returns the amount of time the servlet expects to be temporarily
     * unavailable.  If the servlet is permanently unavailable, or no
     * estimate was provided, returns a negative number.  No effort is
     * made to correct for the time elapsed since the exception was
     * first reported.
     */
    public int getUnavailableSeconds () {
	return permanent ? -1 : seconds;
    }
}
