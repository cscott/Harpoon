/*
 * @(#)SingleThreadModel.java	1.3 97/11/17
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
 * Defines a "single" thread model for servlet execution.
 * This empty interface allows servlet implementers to specify how
 * the system should handle concurrent calls to the same servlet.
 * <p>If the target servlet is flagged with this interface, the servlet 
 * programmer is <strong>guaranteed</strong> that no two threads will execute
 * concurrently the <tt>service</tt> method of that servlet. This guarantee 
 * is ensured by maintaining a pool of servlet instances for each such servlet, 
 * and dispatching each <tt>service</tt> call to a <em>free</em> servlet.
 * <p>In essence, if the servlet implements this interface, the servlet will
 * be thread safe.
 *
 * @version     1.3, 11/17/97
 */

public interface SingleThreadModel {
}
