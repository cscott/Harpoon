/*
 * @(#)HttpServlet.java	1.32 97/11/21
 * 
 * Copyright (c) 1996-1997 Sun Microsystems, Inc. All Rights Reserved.
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

package javax.servlet.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Enumeration;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


/**
 * An abstract class that simplifies writing HTTP servlets.  It extends
 * the <code>GenericServlet</code> base class and provides an framework
 * for handling the HTTP protocol.  Because it is an abstract class,
 * servlet writers must subclass it and override at least one method.
 * The methods normally overridden are:
 * 
 * <ul>
 *      <li> <code>doGet</code>, if HTTP GET requests are supported.
 *	Overriding the <code>doGet</code> method automatically also
 *	provides support for the HEAD and conditional GET operations.
 *	Where practical, the <code>getLastModified</code> method should
 *	also be overridden, to facilitate caching the HTTP response
 *	data.  This improves performance by enabling smarter
 *	conditional GET support.
 *
 *	<li> <code>doPost</code>, if HTTP POST requests are supported.
 *      <li> <code>doPut</code>, if HTTP PUT requests are supported.
 *      <li> <code>doDelete</code>, if HTTP DELETE requests are supported.
 *
 *	<li> The lifecycle methods <code>init</code> and
 *	<code>destroy</code>, if the servlet writer needs to manage
 *	resources that are held for the lifetime of the servlet.
 *	Servlets that do not manage resources do not need to specialize
 *	these methods.
 *	
 *	<li> <code>getServletInfo</code>, to provide descriptive
 *	information through a service's administrative interfaces.
 *      </ul>
 * 
 * <P>Notice that the <code>service</code> method is not typically
 * overridden.  The <code>service</code> method, as provided, supports
 * standard HTTP requests by dispatching them to appropriate methods,
 * such as the methods listed above that have the prefix "do".  In
 * addition, the service method also supports the HTTP 1.1 protocol's
 * TRACE and OPTIONS methods by dispatching to the <code>doTrace</code>
 * and <code>doOptions</code> methods.  The <code>doTrace</code> and
 * <code>doOptions</code> methods are not typically overridden.
 *
 * <P>Servlets typically run inside multi-threaded servers; servlets
 * must be written to handle multiple service requests simultaneously.
 * It is the servlet writer's responsibility to synchronize access to
 * any shared resources.  Such resources include in-memory data such as
 * instance or class variables of the servlet, as well as external
 * components such as files, database and network connections.
 * Information on multithreaded programming in Java can be found in the
 * <a
 * href="http://java.sun.com/Series/Tutorial/java/threads/multithreaded.html">
 * Java Tutorial on Multithreaded Programming</a>.
 *
 * @version 1.32, 11/21/97
 */

public abstract class HttpServlet extends GenericServlet 
    implements java.io.Serializable {

    /**
     * The default constructor does nothing.
     */
    public HttpServlet () { }


    /**
     * Performs the HTTP GET operation; the default implementation
     * reports an HTTP BAD_REQUEST error.  Overriding this method to
     * support the GET operation also automatically supports the HEAD
     * operation.  (HEAD is a GET that returns no body in the response;
     * it just returns the request HEADer fields.)
     *
     * <p>Servlet writers who override this method should read any data
     * from the request, set entity headers in the response, access the
     * writer or output stream, and, finally, write any response data.
     * The headers that are set should include content type, and
     * encoding.  If a writer is to be used to write response data, the
     * content type must be set before the writer is accessed.  In
     * general, the servlet implementor must write the headers before
     * the response data because the headers can be flushed at any time
     * after the data starts to be written.
     * 
     * <p>Setting content length allows the servlet to take advantage
     * of HTTP "connection keep alive".  If content length can not be
     * set in advance, the performance penalties associated with not
     * using keep alives will sometimes be avoided if the response
     * entity fits in an internal buffer.
     *
     * <p>Entity data written for a HEAD request is ignored.  Servlet
     * writers can, as a simple performance optimization, omit writing
     * response data for HEAD methods.  If no response data is to be
     * written, then the content length field must be set explicitly.
     *
     * <P>The GET operation is expected to be safe: without any side
     * effects for which users might be held responsible.  For example,
     * most form queries have no side effects.  Requests intended to
     * change stored data should use some other HTTP method.  (There
     * have been cases of significant security breaches reported
     * because web-based applications used GET inappropriately.)
     *
     * <P> The GET operation is also expected to be idempotent: it can
     * safely be repeated.  This is not quite the same as being safe,
     * but in some common examples the requirements have the same
     * result.  For example, repeating queries is both safe and
     * idempotent (unless payment is required!), but buying something
     * or modifying data is neither safe nor idempotent.
     *
     * @param req HttpServletRequest that encapsulates the request to
     * the servlet 
     * @param resp HttpServletResponse that encapsulates the response
     * from the servlet
     * 
     * @exception IOException if detected when handling the request
     * @exception ServletException if the request could not be handled
     * 
     * @see javax.servlet.ServletResponse#setContentType
     */
    protected void doGet (HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException
      {
	  resp.sendError (HttpServletResponse.SC_BAD_REQUEST,
			  "GET is not supported by this URL");
      }


    /**
     * Gets the time the requested entity was last modified; the
     * default implementation returns a negative number, indicating
     * that the modification time is unknown and hence should not be
     * used for conditional GET operations or for other cache control
     * operations as this implementation will always return the contents. 
     *
     * <P> Implementations supporting the GET request should override
     * this method to provide an accurate object modification time.
     * This makes browser and proxy caches work more effectively,
     * reducing the load on server and network resources.
     *
     * @param req HttpServletRequest that encapsulates the request to
     * the servlet 
     * @return the time the requested entity was last modified, as
     * the difference, measured in milliseconds, between that
     * time and midnight, January 1, 1970 UTC.  Negative numbers
     * indicate this time is unknown.
     */
    protected long getLastModified (HttpServletRequest req)
      {
	  return -1;
      }


    /*
     * Implements the HTTP HEAD method.  By default, this is done
     * in terms of the unconditional GET method, using a response body
     * which only counts its output bytes (to set Content-Length
     * correctly).  Subclassers could avoid computing the response
     * body, and just set the response headers directly, for improved
     * performance.
     *
     * <P> As with GET, this method should be both "safe" and
     * "idempotent".
     *
     * @param req HttpServletRequest that encapsulates the request to
     * the servlet 
     * @param resp HttpServletResponse that encapsulates the response
     * from the servlet
     * @exception IOException if detected when handling the request
     * @exception ServletException if the request could not be handled
     */
    private void doHead (HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException
      {
	  NoBodyResponse response = new NoBodyResponse(resp);

	  doGet (req, response);
	  response.setContentLength ();
      }


    /**
     *
     * Performs the HTTP POST operation; the default implementation
     * reports an HTTP BAD_REQUEST error.  Servlet writers who override
     * this method should read any data from the request (for example,
     * form parameters), set entity headers in the response, access the
     * writer or output stream and, finally, write any response data
     * using the servlet output stream.  The headers that are set
     * should include content type, and encoding.  If a writer is to be
     * used to write response data, the content type must be set before
     * the writer is accessed.  In general, the servlet implementor
     * must write the headers before the response data because the
     * headers can be flushed at any time after the data starts to be
     * written.
     *
     * <p>If HTTP/1.1 chunked encoding is used (that is, if the
     * transfer-encoding header is present), then the content-length
     * header should not be set.  For HTTP/1.1 communications that do
     * not use chunked encoding and HTTP 1.0 communications, setting
     * content length allows the servlet to take advantage of HTTP
     * "connection keep alive".  For just such communications, if
     * content length can not be set, the performance penalties
     * associated with not using keep alives will sometimes be avoided
     * if the response entity fits in an internal buffer.
     *
     * <P> This method does not need to be either "safe" or
     * "idempotent".  Operations requested through POST can have side
     * effects for which the user can be held accountable.  Specific
     * examples including updating stored data or buying things online.
     *
     * @param req HttpServletRequest that encapsulates the request to
     * the servlet 
     * @param resp HttpServletResponse that encapsulates the response
     * from the servlet
     * 
     * @exception IOException if detected when handling the request
     * @exception ServletException if the request could not be handled
     *
     * @see javax.servlet.ServletResponse#setContentType
     */
    protected void doPost (HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException
      {
	  resp.sendError (HttpServletResponse.SC_BAD_REQUEST,
			  "POST is not supported by this URL");
      }

    /**
     * Performs the HTTP PUT operation; the default implementation
     * reports an HTTP BAD_REQUEST error.  The PUT operation is
     * analogous to sending a file via FTP.
     *
     * <p>Servlet writers who override this method must respect any
     * Content-* headers sent with the request. (These headers include
     * content-length, content-type, content-transfer-encoding,
     * content-encoding, content-base, content-language,
     * content-location, content-MD5, and content-range.) If the
     * subclass cannot honor a content header, then it must issue an
     * error response (501) and discard the request.  For more
     * information, see the <a
     * href="http://info.internet.isi.edu:80/in-notes/rfc/files/rfc2068.txt">
     * HTTP 1.1 RFC</a>.
     *
     * <P> This method does not need to be either "safe" or
     * "idempotent".  Operations requested through PUT can have side
     * effects for which the user can be held accountable.  Although
     * not required, servlet writers who override this method may wish
     * to save a copy of the affected URI in temporary storage.
     * 
     * @param req HttpServletRequest that encapsulates the request to
     * the servlet 
     * @param resp HttpServletResponse that encapsulates the response
     * from the servlet 
     * @exception IOException if detected when handling the request
     * @exception ServletException if the request could not be handled
     */
    protected void doPut (HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException
      {
	  resp.sendError (HttpServletResponse.SC_BAD_REQUEST,
			  "PUT is not supported by this URL");
      }

    /**
     * Performs the HTTP DELETE operation; the default implementation
     * reports an HTTP BAD_REQUEST error. The DELETE operation allows a
     * client to request a URI to be removed from the server.
     *
     * <P> This method does not need to be either "safe" or
     * "idempotent".  Operations requested through DELETE can have
     * side-effects for which users may be held accountable. Although
     * not required, servlet writers who subclass this method may wish
     * to save a copy of the affected URI in temporary storage.
     *
     * @param req HttpServletRequest that encapsulates the request to
     * the servlet 
     * @param resp HttpServletResponse that encapsulates the response
     * from the servlet 
     * @exception IOException if detected when handling the request
     * @exception ServletException if the request could not be handled
     */
    protected void doDelete (HttpServletRequest req,
			     HttpServletResponse resp)
      throws ServletException, IOException
      {
	  resp.sendError (HttpServletResponse.SC_BAD_REQUEST,
			  "DELETE is not supported by this URL");
      }


    private Method [] getAllDeclaredMethods (Class c) {
	if (c.getName().equals("javax.servlet.http.HttpServlet"))
	  return null;
	
	int j=0;
	Method [] parentMethods = getAllDeclaredMethods(c.getSuperclass());
	Method [] thisMethods = c.getDeclaredMethods();

	if (parentMethods!=null) {
	    Method [] allMethods = new Method [parentMethods.length + thisMethods.length];
	    for (int i=0; i<parentMethods.length; i++) {
		allMethods[i]=parentMethods[i];
		j=i;
	    }
	    j++;
	    for (int i=j; i<thisMethods.length+j; i++) {
		allMethods[i] = thisMethods[i-j];
	    }
	    return allMethods;
	}
	return thisMethods;
    }

    /**
     * Performs the HTTP OPTIONS operation; the default implementation
     * of this method automatically determines what HTTP Options are
     * supported.  For example, if a servlet writer subclasses
     * HttpServlet and overrides the <code>doGet</code> method, then
     * this method will return the following header: <p>Allow:
     * GET,HEAD,TRACE,OPTIONS
     * 
     * <p>This method does not need to be overridden unless the servlet
     * implements new methods, beyond those supported by the HTTP/1.1
     * protocol.
     *
     * @param req HttpServletRequest that encapsulates the request to
     * the servlet 
     * @param resp HttpServletResponse that encapsulates the response
     * from the servlet 
     * @exception IOException if detected when handling the request
     * @exception ServletException if the request could not be handled
     */
    protected void doOptions (HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException
      {
	  Method [] methods = getAllDeclaredMethods(this.getClass());
	  
	  boolean ALLOW_GET = false;
	  boolean ALLOW_HEAD = false;
	  boolean ALLOW_POST = false;
	  boolean ALLOW_PUT = false;
	  boolean ALLOW_DELETE = false;
	  boolean ALLOW_TRACE = true;
	  boolean ALLOW_OPTIONS = true;
	  
	  for (int i=0; i<methods.length; i++) {
	      Method m = methods[i];
	      
	      if (m.getName().equals("doGet")) {
		  ALLOW_GET = true;
		  ALLOW_HEAD = true;
	      }
	      if (m.getName().equals("doPost")) 
		ALLOW_POST = true;
	      if (m.getName().equals("doPut"))
		ALLOW_PUT = true;
	      if (m.getName().equals("doDelete"))
		ALLOW_DELETE = true;
	      
	  }
	    
	  String allow = null;
	  if (ALLOW_GET)
	    if (allow==null) allow="GET";
	  if (ALLOW_HEAD)
	    if (allow==null) allow="HEAD";
	    else allow += ", " + "HEAD";
	  if (ALLOW_POST)
	    if (allow==null) allow="POST";
	    else allow += ", " + "POST";
	  if (ALLOW_PUT)
	    if (allow==null) allow="PUT";
	    else allow += ", " + "PUT";
	  if (ALLOW_DELETE)
	    if (allow==null) allow="DELETE";
	    else allow += ", " + "DELETE";
	  if (ALLOW_TRACE)
	    if (allow==null) allow="TRACE";
	    else allow += ", " + "TRACE";
	  if (ALLOW_OPTIONS)
	    if (allow==null) allow="OPTIONS";
	    else allow += ", " + "OPTIONS";

	  resp.setHeader("Allow", allow);
      }

    /**
     * Performs the HTTP TRACE operation; the default implementation of
     * this method causes a response with a message containing all of
     * the headers sent in the trace request.  This method is not
     * typically overridden.
     *
     * @param req HttpServletRequest that encapsulates the request to
     * the servlet 
     * @param resp HttpServletResponse that encapsulates the response
     * from the servlet 
     * @exception IOException if detected when handling the request
     * @exception ServletException if the request could not be handled
     */
    protected void doTrace (HttpServletRequest req, HttpServletResponse resp) 
      throws ServletException, IOException {

	int responseLength;
	
	String CRLF = "\r\n";
	String responseString = "TRACE "+req.getRequestURI()+" " +req.getProtocol();
	
	Enumeration reqHeaderEnum = req.getHeaderNames();
	
	while( reqHeaderEnum.hasMoreElements() )
	  {
	      String headerName = (String)reqHeaderEnum.nextElement();
	      responseString += CRLF + headerName + ": " + req.getHeader(headerName); 
	  }
	
	responseString += CRLF;
	
	responseLength = responseString.length();
	
	resp.setContentType("message/http");
	resp.setContentLength(responseLength);
	ServletOutputStream out = resp.getOutputStream();
	out.print(responseString);	
	out.close();
	return;
    }		


    /**
     * This is an HTTP-specific version of the
     * <code>Servlet.service</code> method, which accepts HTTP specific
     * parameters.  This method is rarely overridden.  Standard HTTP
     * requests are supported by dispatching to Java methods
     * specialized to implement them.
     *
     * @param req HttpServletRequest that encapsulates the request to
     * the servlet 
     * @param resp HttpServletResponse that encapsulates the response
     * from the servlet 
     * @exception IOException if detected when handling the request
     * @exception ServletException if the request could not be handled
     * 
     * @see javax.servlet.Servlet#service
     */
    protected void service (HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException
      {
	  String			method = req.getMethod ();

	  if (method.equals ("GET")) {
	      long		ifModifiedSince;
	      long		lastModified;
	      long		now;

	      //
	      // HTTP 1.0 conditional GET just uses If-Modified-Since fields
	      // in the header.  HTTP 1.1 has more conditional GET options.
	      //
	      // We call getLastModified() only once; it won't be cheap.
	      //
	      ifModifiedSince = req.getDateHeader ("If-Modified-Since");
	      lastModified = getLastModified (req);
	      maybeSetLastModified (resp, lastModified);


	      if (ifModifiedSince == -1 || lastModified == -1)
		doGet (req, resp);
	      else {
		  now = System.currentTimeMillis ();

		  //
		  // Times in the future are invalid ... but we can't treat
		  // them as "hard errors", so for now we accept extra load.
		  //
		  if (now < ifModifiedSince || ifModifiedSince < lastModified)
		    doGet (req, resp);
		  else
		    resp.sendError (HttpServletResponse.SC_NOT_MODIFIED);
	      }

	  } else if (method.equals ("HEAD")) {
	      long		lastModified;

	      lastModified = getLastModified (req);
	      maybeSetLastModified (resp, lastModified);
	      doHead (req, resp);

	  } else if (method.equals ("POST")) {
	      doPost (req, resp);

	  } else if (method.equals ("PUT")) {
	      doPut(req, resp);	

	  } else if (method.equals ("DELETE")) {
	      doDelete(req, resp);

	  } else if (method.equals ("OPTIONS")) {
	      doOptions(req,resp);

	  } else if (method.equals ("TRACE")) {
	      doTrace(req,resp);
	
	  } else {
	      //
	      // Note that this means NO servlet supports whatever
	      // method was requested, anywhere on this server.
	      //
	      resp.sendError (HttpServletResponse.SC_NOT_IMPLEMENTED,
			      "Method '" + method + "' is not defined in RFC 2068");
	  }
      }


    /*
     * Sets the Last-Modified entity header field, if it has not
     * already been set and if the value is meaningful.  Called before
     * doGet, to ensure that headers are set before response data is
     * written.  A subclass might have set this header already, so we
     * check.
     */
    private void maybeSetLastModified (
	HttpServletResponse	resp,
	long			lastModified) {
	if (resp.containsHeader ("Last-Modified"))
	  return;
	if (lastModified >= 0)
	  resp.setDateHeader ("Last-Modified", lastModified);
    }

    /**
     * Implements the high level <code>Servlet.service</code> method by
     * delegating to the HTTP-specific service method.  This method is
     * not normally overriden.
     * 
     * @param req ServletRequest that encapsulates the request to the
     * servlet
     * @param res ServletResponse that encapsulates the response from
     * the servlet
     * @exception IOException if an I/O exception has occurred
     * @exception ServletException if a servlet exception has occurred
     * 
     * @see javax.servlet.Servlet#service
     */
    public void service(ServletRequest req, ServletResponse res)
      throws ServletException, IOException
      {
	  HttpServletRequest	request;
	  HttpServletResponse	response;

	  try {
	      request = (HttpServletRequest) req;
	      response = (HttpServletResponse) res;
	  } catch (ClassCastException e) {
	      throw new ServletException ("non-HTTP request or response");
	  }
	  service (request, response);
      }
}


/*
 * A response that includes no body, for use in (dumb) "HEAD" support.
 * This just swallows that body, counting the bytes in order to set
 * the content length appropriately.  All other methods delegate directly
 * to the HTTP Servlet Response object used to construct this one.
 */
// file private
class NoBodyResponse implements HttpServletResponse {
    private HttpServletResponse		resp;
    private NoBodyOutputStream		noBody;
    private PrintWriter			writer;
    private boolean			didSetContentLength;

    // file private
    NoBodyResponse (HttpServletResponse r) {
	resp = r;
	noBody = new NoBodyOutputStream ();
    }

    // file private
    void setContentLength () {
	if (!didSetContentLength)
	  resp.setContentLength (noBody.getContentLength ());
    }


    // SERVLET RESPONSE interface methods

    public void setContentLength (int len) {
	resp.setContentLength (len);
	didSetContentLength = true;
    }

    public void setContentType (String type)
      { resp.setContentType (type); }

    public ServletOutputStream getOutputStream () throws IOException
      { return noBody; }

    public String getCharacterEncoding ()
	{ return resp.getCharacterEncoding (); }

    public PrintWriter getWriter () throws UnsupportedEncodingException
    {
	if (writer == null) {
	    OutputStreamWriter	w;

	    w = new OutputStreamWriter (noBody, getCharacterEncoding ());
	    writer = new PrintWriter (w);
	}
	return writer;
    }


    // HTTP SERVLET RESPONSE interface methods

    public void addCookie(Cookie cookie)
      { resp.addCookie(cookie); }

    public boolean containsHeader (String name)
      { return resp.containsHeader (name); }

    public void setStatus (int sc, String sm)
      { resp.setStatus (sc, sm); }

    public void setStatus (int sc)
      { resp.setStatus (sc); }

    public void setHeader (String name, String value)
      { resp.setHeader (name, value); }

    public void setIntHeader (String name, int value)
      { resp.setIntHeader (name, value); }

    public void setDateHeader (String name, long date)
      { resp.setDateHeader (name, date); }

    public void sendError (int sc, String msg) throws IOException
      { resp.sendError (sc, msg); }

    public void sendError (int sc) throws IOException
      { resp.sendError (sc); }

    public void sendRedirect (String location) throws IOException
      { resp.sendRedirect (location); }

    public String encodeUrl (String url) 
      { return resp.encodeUrl(url); }

    public String encodeRedirectUrl (String url)
      { return resp.encodeRedirectUrl(url); }
}


/*
 * Servlet output stream that gobbles up all its data.
 */
// file private
class NoBodyOutputStream extends ServletOutputStream {
    private int		contentLength = 0;

    // file private
    NoBodyOutputStream () {}

    // file private
    int getContentLength () {
	return contentLength;
    }

    public void write (int b) {
	contentLength++;
    }

    public void write (byte buf [], int offset, int len)
      throws IOException {
	  if (len >= 0)
	    contentLength += len;
	  else
	    throw new IOException ("negative length");
    }
}
