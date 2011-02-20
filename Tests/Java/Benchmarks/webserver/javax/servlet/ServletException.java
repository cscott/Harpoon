/*
 * @(#)ServletException.java	1.6 97/10/08
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
 * 
 * CopyrightVersion 1.0
 */

package javax.servlet;


/**
 * This exception is thrown to indicate a servlet problem.
 *
 * @version	1.6, 10/08/97
 */
public 
class ServletException extends Exception {

    /**
     * Constructs a new ServletException with the specified
     * descriptive message.
     * 
     * @param msg the string describing the exception
     */
    public ServletException(String msg) {
	super(msg);
    }

    /**
     * Constructs a new ServletException.
     * 
     */
    public ServletException() {
	super();
    }
}

