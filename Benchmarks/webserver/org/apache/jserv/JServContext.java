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

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import java.util.Vector;
import java.util.Stack;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.java.lang.Lock;

/*
 * FIXME: Is there any good reason why ServletConfig and ServletContext
 * are implemented in the same class? <mbp@pharos.com.au>
 */

/**
 * This class implements the parts of the servlet that are longer-lived
 * than a single request, ServletConfig and ServletContext.
 *
 * <P>There is one <CODE>JServContext</CODE> object for each servlet,
 * and they are managed by <CODE>JServServletManager</CODE>.
 *
 * @author Alexei Kosut
 * @author Francis J. Lacoste
 * @version $Revision: 1.1 $ $Date: 2000-07-18 22:01:52 $
 */
class JServContext implements ServletConfig, ServletContext, JServSendError, JServLogChannels {
    private JServServletManager servletMgr;

    // The servlet itself
    Servlet servlet;

    // Servlet metadata
    private Properties initargs;

    Lock lock;
    
    String aliasName;

    // Constructor
    public JServContext(Servlet servlet, JServServletManager manager,
            Properties initargs, String aliasName) {
        this.servlet = servlet;

        this.servletMgr = manager;
        this.initargs = initargs;
        
        this.aliasName = aliasName;

        lock = new Lock();
    }


    //----------------------------------------- Implementation of ServletConfig
    /**
     * Returns the context for the servlet.
     */
    public ServletContext getServletContext() {
        return this;
    }

    /**
     * Returns a string containing the value of the named
     * initialization parameter of the servlet, or null if the
     * parameter does not exist.  Init parameters have a single string
     * value; it is the responsibility of the servlet writer to
     * interpret the string.
     * @param name the name of the parameter whose value is requested.
     */
    public String getInitParameter(String name) {
        return initargs.getProperty(name);
    }

    /**
     * Returns the names of the servlet's initialization parameters
     * as an enumeration of strings, or an empty enumeration if there
     * are no initialization parameters.
     */
    public Enumeration getInitParameterNames() {
        return initargs.propertyNames();
    }

    //------------------------------------ Implementation of ServletContext

    /**
     * Returns the servlet of the specified name, or null if not
     * found.  When the servlet is returned it is initialized and
     * ready to accept service requests.
     * <p>
     * <i>Note:</i> This is a <b>dangerous</b> method to call for the
     * following reasons.
     * <p>
     * <UL><LI> When this method is called the state of the servlet may not
     *      be known, and this could cause problems with the server's
     *      servlet state machine.
     * <LI> It is a security risk to allow any servlet to be able to
     *      access the methods of another servlet.
     * </UL>
     * @param name the name of the desired servlet.
     * @excpeption if the servlet could not be initialized
     */
    public Servlet getServlet(String name) throws ServletException {
        JServContext s = servletMgr.loadServlet( name, this );
        return ( s != null ) ? s.servlet : null;
    }

    /**
     * Returns an enumeration of the Servlet objects in this server.
     * Only servlets that are accessible (i.e., from the same namespace)
     * will be returned.  The enumeration always includes the servlet
     * itself.
     * <p>
     * <i>Note:</i> This is a <b>dangerous</b> method to call for the
     * following reasons.
     * <p>
     * <UL><LI> When this method is called the state of the servlet may not
     *      be known, and this could cause problems with the server's
     *      servlet state machine.
     * <LI> It is a security risk to allow any servlet to be able to
     *      access the methods of another servlet.
     * </UL>
     * @see #getServletNames
     * @see #getServlet
     * @deprecated Please use getServletNames in conjunction with getServlet
     */
    public Enumeration getServlets() {
        return servletMgr.getLoadedServlets();
    }

    /**
     * Returns an enumeration of the Servlet object names in this server.
     * Only servlets that are accessible (i.e., from the same namespace)
     * will be returned.  The enumeration always includes the servlet
     * itself.
     * <p>
     * <i>Note:</i> This is a <b>dangerous</b> method to call for the
     * following reasons.
     * <p>
     * <UL><LI> When this method is called the state of the servlet may not
     *      be known, and this could cause problems with the server's
     *      servlet state machine.
     * <LI> It is a security risk to allow any servlet to be able to
     *      access the methods of another servlet.
     * </UL>
     */
    public Enumeration getServletNames() {
        return servletMgr.getServletNames();
    }

    /**
     * Writes the given message string to the servlet log file.
     * The name of the servlet log file is server specific; it
     * is normally an event log.
     * @param msg the message to be written
     */
    public void log(String msg) {
        JServ.log.log(CH_SERVLET_LOG, ((aliasName == null) ? "" : aliasName + "/" ) 
            + msg);
    }

    /**
     * Write the stacktrace and the given message string to the
     * servlet log file. The name of the servlet log file is
     * server specific; it is normally an event log.
     * 
     * @param exception the exception to be written.
     * @param msg the message to be written
     */
    public void log( Exception ex, String msg ) {
        log(msg, ex);
    }
    
    /**
     * Write the given message string and the stacktrace to the servlet log
     * file. The name of the servlet log file is server specific; it is
     * normally an event log.
     *
     * @param msg the message to be written
     * @param t the exception to be written.  
     * @since JSDK 2.1
     */
    public void log( String msg, Throwable t ) {
        CharArrayWriter buf = new CharArrayWriter();
        PrintWriter writer = new PrintWriter( buf );
        writer.println( msg );
        t.printStackTrace( writer );
        log( buf.toString() );
    }

    /**
     * Applies alias rules to the specified virtual path and returns the
     * corresponding real path.  For example, in an HTTP servlet,
     * this method would resolve the path against the HTTP service's
     * docroot.  Returns null if virtual paths are not supported, or if the
     * translation could not be performed for any reason.
     * @param path the virtual path to be translated into a real path
     */
    public String getRealPath(String path) {
        // FIXME: See the note under JServConnection.getRealPath()
        // Until then, we return null for "not supported"
        return null;
    }

    /**
     * Returns the mime type of the specified file, or null if not known.
     * @param file name of the file whose mime type is required
     */
    public String getMimeType(String f) {

        // ignore case
        String file = f.toLowerCase();

        // Text
        if (file.endsWith(".html")
        || file.endsWith(".htm")) return "text/html";
        if (file.endsWith(".txt")) return "text/plain";
        if (file.endsWith(".css")) return "text/css";
        if (file.endsWith(".sgml")
        || file.endsWith(".sgm")) return "text/x-sgml";
        // Image
        if (file.endsWith(".gif")) return "image/gif";
        if (file.endsWith(".jpg")
        || file.endsWith(".jpeg")
        || file.endsWith(".jpe")) return "image/jpeg";
        if (file.endsWith(".png")) return "image/png";
        if (file.endsWith(".tif")
        || file.endsWith(".tiff")) return "image/tiff";
        if (file.endsWith(".rgb")) return "image/x-rgb";
        if (file.endsWith(".xbm")) return "image/x-xbitmap";
        if (file.endsWith(".xpm")) return "image/x-xpixmap";
        // Audio
        if (file.endsWith(".au")
        || file.endsWith(".snd")) return "audio/basic";
        if (file.endsWith(".mid")
        || file.endsWith(".midi")
        || file.endsWith(".rmi")
        || file.endsWith(".kar")) return "audio/mid";
        if (file.endsWith(".mpga")
        || file.endsWith(".mp2")
        || file.endsWith(".mp3")) return "audio/mpeg";
        if (file.endsWith(".wav")) return "audio/wav";
        if (file.endsWith(".aiff")
        || file.endsWith(".aifc")) return "audio/aiff";
        if (file.endsWith(".aif")) return "audio/x-aiff";
        if (file.endsWith(".ra")) return "audio/x-realaudio";
        if (file.endsWith(".ram")) return "audio/x-pn-realaudio";
        if (file.endsWith(".rpm")) return "audio/x-pn-realaudio-plugin";
        if (file.endsWith(".sd2")) return "audio/x-sd2";
        // Application
        if (file.endsWith(".bin")
        || file.endsWith(".dms")
        || file.endsWith(".lha")
        || file.endsWith(".lzh")
        || file.endsWith(".exe")
        || file.endsWith(".class")) return "application/octet-stream";
        if (file.endsWith(".hqx")) return "application/mac-binhex40";
        if (file.endsWith(".ps")
        || file.endsWith(".ai")
        || file.endsWith(".eps")) return "application/postscript";
        if (file.endsWith(".pdf")) return "application/pdf";
        if (file.endsWith(".rtf")) return "application/rtf";
        if (file.endsWith(".doc")) return "application/msword";
        if (file.endsWith(".ppt")) return "application/powerpoint";
        if (file.endsWith(".fif")) return "application/fractals";
        if (file.endsWith(".p7c")) return "application/pkcs7-mime";
        // Application/x
        if (file.endsWith(".js")) return "application/x-javascript";
        if (file.endsWith(".z")) return "application/x-compress";
        if (file.endsWith(".gz")) return "application/x-gzip";
        if (file.endsWith(".tar")) return "application/x-tar";
        if (file.endsWith(".tgz")) return "application/x-compressed";
        if (file.endsWith(".zip")) return "application/x-zip-compressed";
        if (file.endsWith(".dir")
        || file.endsWith(".dcr")
        || file.endsWith(".dxr")) return "application/x-director";
        if (file.endsWith(".dvi")) return "application/x-dvi";
        if (file.endsWith(".tex")) return "application/x-tex";
        if (file.endsWith(".latex")) return "application/x-latex";
        if (file.endsWith(".tcl")) return "application/x-tcl";
        if (file.endsWith(".cer")
        || file.endsWith(".crt")
        || file.endsWith(".der")) return "application/x-x509-ca-cert";
        // Video
        if (file.endsWith(".mpg")
        || file.endsWith(".mpe")
        || file.endsWith(".mpeg")) return "video/mpeg";
        if (file.endsWith(".qt")
        || file.endsWith(".mov")) return "video/quicktime";
        if (file.endsWith(".avi")) return "video/x-msvideo";
        if (file.endsWith(".movie")) return "video/x-sgi-movie";
        // Chemical
        if (file.endsWith(".pdb")
        || file.endsWith(".xyz")) return "chemical/x-pdb";
        // X-
        if (file.endsWith(".ice")) return "x-conference/x-cooltalk";
        if (file.endsWith(".wrl")
        || file.endsWith(".vrml")) return "x-world/x-vrml";

        return null;
    }

    /**
     * Returns the name and version of the network service under which
     * the servlet is running. For example, if the network service was
     * an HTTP service, then this would be the same as the CGI variable
     * SERVER_SOFTWARE.
     *
     */
    public String getServerInfo() {
        // This isn't quite SERVER_INFO, but I guess it's the
        // most direct 'server' to the servlet.
        return JServDefs.PACKAGE + "/" + JServDefs.VERSION;
    }

    /**
     * Returns the value of the named attribute of the network service,
     * or null if the attribute does not exist.  This method allows
     * access to additional information about the service, not already
     * provided by the other methods in this interface. Attribute names
     * should follow the same convention as package names.  The package
     * names java.* and javax.* are reserved for use by Javasoft, and
     * com.sun.* is reserved for use by Sun Microsystems.
     * @param name the name of the attribute whose value is required
     * @return the value of the attribute, or null if the attribute
     * does not exist.
     */
    public Object getAttribute(String name) {
        // We don't have any attributes for the network service
        return null;
    }

    //-------------------------------------- Implementation of JServSendError
    public void sendError(int sc, String msg) {
        log(JServConnection.findStatusString( sc ) + ": " + msg );
    }

    public void sendError(Throwable ex) {
        CharArrayWriter buf = new CharArrayWriter();
        PrintWriter writer = new PrintWriter( buf );
        ex.printStackTrace( writer );
        log( buf.toString() );
    }

    // access function for the servlet
    public Servlet getServlet() {
            return servlet;
    }
}