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

package org.apache.jserv;

/**
 * This interface enumerates all string constants used in JServ as channel
 * identifiers. This is done to (1) ensure the consistency and eliminate the
 * possibility of introducing subtle bugs that cannot be traced by the
 * compiler (2) minimize the number of String objects in the code.
 * @author <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a>
 * @version $Revision: 1.1 $ $Date: 2000-07-18 22:01:52 $
 */

/**
 * Changes made before 1.0.1 release to log messages by severity, not by
   the portion of code they occur in.
   (Michal) 
 */

public interface JServLogChannels {

    /**
     * Debugging messages
     */
    public static final String CH_DEBUG = "debug";

    /**
     * Informational messages
     */
    public static final String CH_INFO = "info";

    /**
     * Exceptions internal to servlets (thrown in servlet.service())
     */
    public static final String CH_SERVLET_EXCEPTION = "servletException";

    /**
     * Container (i.e. jserv) level exceptions
     */
    public static final String CH_CONTAINER_EXCEPTION = "jservException";

    /**
     * Warnings, i.e. messages of "something wrong" style that however don't stop JServ
     */
    public static final String CH_WARNING = "warning";

    /**
     * Servlet log channel identifier.
     *
     * Messages logged by the actual servlets are logged using this channel.
     */
    public static final String CH_SERVLET_LOG = "servletLog";

    /**
     * Critical messages that stop further processing.
     */
    public static final String CH_CRITICAL = "critical";

}