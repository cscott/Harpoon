/*
 * @(#)ServerSocket.java	1.34 99/04/22
 *
 * Copyright 1995-1999 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

package java.net;

import java.io.IOException;
import java.io.FileDescriptor;

import java.io.NativeIO;
import harpoon.Analysis.ContBuilder.*;

/**
 * This class implements server sockets. A server socket waits for 
 * requests to come in over the network. It performs some operation 
 * based on that request, and then possibly returns a result to the requester.
 * <p>
 * The actual work of the server socket is performed by an instance 
 * of the <code>SocketImpl</code> class. An application can 
 * change the socket factory that creates the socket 
 * implementation to configure itself to create sockets 
 * appropriate to the local firewall. 
 *
 * @author  unascribed
 * @version 1.34, 04/22/99
 * @see     java.net.SocketImpl
 * @see     java.net.ServerSocket#setSocketFactory(java.net.SocketImplFactory)
 * @since   JDK1.0
 */
public 
class ServerSocket {
    /**
     * The implementation of this Socket.
     */
    private SocketImpl impl;

    /**
     * Creates an unconnected server socket. Note: this method
     * should not be public.
     * @exception IOException IO error when opening the socket.
     */
    private ServerSocket() throws IOException {
	impl = (factory != null) ? factory.createSocketImpl() : 
	    new PlainSocketImpl();
    }

    /**
     * Creates a server socket on a specified port. A port of 
     * <code>0</code> creates a socket on any free port. 
     * <p>
     * The maximum queue length for incoming connection indications (a 
     * request to connect) is set to <code>50</code>. If a connection 
     * indication arrives when the queue is full, the connection is refused.
     * <p>
     * If the application has specified a server socket factory, that 
     * factory's <code>createSocketImpl</code> method is called to create 
     * the actual socket implementation. Otherwise a "plain" socket is created.
     * <p>
     * If there is a security manager, 
     * its <code>checkListen</code> method is called
     * with the <code>port</code> argument
     * as its argument to ensure the operation is allowed. 
     * This could result in a SecurityException.
     *
     * @param      port  the port number, or <code>0</code> to use any
     *                   free port.
     * 
     * @exception  IOException  if an I/O error occurs when opening the socket.
     * @exception  SecurityException
     * if a security manager exists and its <code>checkListen</code> 
     * method doesn't allow the operation.
     * 
     * @see        java.net.SocketImpl
     * @see        java.net.SocketImplFactory#createSocketImpl()
     * @see        java.net.ServerSocket#setSocketFactory(java.net.SocketImplFactory)
     * @see        SecurityManager#checkListen
     */
    public ServerSocket(int port) throws IOException {
	this(port, 50, null);
    }

    /**
     * Creates a server socket and binds it to the specified local port 
     * number. A port number of <code>0</code> creates a socket on any 
     * free port. 
     * <p>
     * The maximum queue length for incoming connection indications (a 
     * request to connect) is set to the <code>backlog</code> parameter. If 
     * a connection indication arrives when the queue is full, the 
     * connection is refused. 
     * <p>
     * If the application has specified a server socket factory, that 
     * factory's <code>createSocketImpl</code> method is called to create 
     * the actual socket implementation. Otherwise a "plain" socket is created.
     * <p>
     * If there is a security manager, 
     * its <code>checkListen</code> method is called
     * with the <code>port</code> argument
     * as its argument to ensure the operation is allowed. 
     * This could result in a SecurityException.
     *
     * @param      port     the specified port, or <code>0</code> to use
     *                      any free port.
     * @param      backlog  the maximum length of the queue.
     * 
     * @exception  IOException  if an I/O error occurs when opening the socket.
     * @exception  SecurityException
     * if a security manager exists and its <code>checkListen</code> 
     * method doesn't allow the operation.
     * 
     * @see        java.net.SocketImpl
     * @see        java.net.SocketImplFactory#createSocketImpl()
     * @see        java.net.ServerSocket#setSocketFactory(java.net.SocketImplFactory)
     * @see        SecurityManager#checkListen
     */
    public ServerSocket(int port, int backlog) throws IOException {
	this(port, backlog, null);
    }

    /** 
     * Create a server with the specified port, listen backlog, and 
     * local IP address to bind to.  The <i>bindAddr</i> argument
     * can be used on a multi-homed host for a ServerSocket that
     * will only accept connect requests to one of its addresses.
     * If <i>bindAddr</i> is null, it will default accepting
     * connections on any/all local addresses.
     * The port must be between 0 and 65535, inclusive.
     * 
     * <P>If there is a security manager, this method 
     * calls its <code>checkListen</code> method
     * with the <code>port</code> argument
     * as its argument to ensure the operation is allowed. 
     * This could result in a SecurityException.
     * 
     * <P>
     * @param port the local TCP port
     * @param backlog the listen backlog
     * @param bindAddr the local InetAddress the server will bind to
     * 
     * @throws  SecurityException
     *        if a security manager exists and its <code>checkListen</code> 
     *        method doesn't allow the operation.
     * 
     * @see SocketConstants
     * @see SocketOption
     * @see SocketImpl
     * @see SecurityManager#checkListen
     * @since   JDK1.1
     */
    public ServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
    	this();

	if (port < 0 || port > 0xFFFF)
	    throw new IllegalArgumentException(
		       "Port value out of range: " + port);
	try {
	    SecurityManager security = System.getSecurityManager();
	    if (security != null) {
		security.checkListen(port);
	    }

	    impl.create(true); // a stream socket
	    if (bindAddr == null)
		bindAddr = InetAddress.anyLocalAddress;	

	    impl.bind(bindAddr, port);
	    impl.listen(backlog);

	} catch(SecurityException e) {
	    impl.close();
	    throw e;
	} catch(IOException e) {
	    impl.close();
	    throw e;
	}

    }

    public void makeAsync()
    {
	NativeIO.makeNonBlockJNI(impl.fd.fd);
    }


    /**
     * Returns the local address of this server socket.
     *
     * @return  the address to which this socket is connected,
     *          or <code>null</code> if the socket is not yet connected.
     */
    public InetAddress getInetAddress() {
	return impl.getInetAddress();
    }

    /**
     * Returns the port on which this socket is listening.
     *
     * @return  the port number to which this socket is listening.
     */
    public int getLocalPort() {
	return impl.getLocalPort();
    }

    /**
     * Listens for a connection to be made to this socket and accepts 
     * it. The method blocks until a connection is made. 
     *
     * <p>A new Socket <code>s</code> is created and, if there is a security manager, 
     * the security manager's <code>checkAccept</code> method is called
     * with <code>s.getInetAddress().getHostAddress()</code> and
     * <code>s.getPort()</code>
     * as its arguments to ensure the operation is allowed. 
     * This could result in a SecurityException.
     * 
     * @exception  IOException  if an I/O error occurs when waiting for a
     *               connection.
     * @exception  SecurityException  if a security manager exists and its  
     *             <code>checkListen</code> method doesn't allow the operation.
     * 
     * @see SecurityManager#checkAccept
     */
    public Socket accept() throws IOException {
	Socket s = new Socket();
	implAccept(s);
	return s;
    }
    
    // optimistic version
    public ObjectContinuation acceptAsyncO() throws IOException {
	//System.out.println("Calling canAccept...");
	if (NativeIO.canAcceptJNI(impl.fd.fd)) {
	    //System.out.println("Calling accept 1... ");
	    return new ObjectContinuationOpt(accept()); 
	    //System.out.println("Accept done.");
	}
	//System.out.println("doing continuations");
	return new acceptAsyncC();
    }
    
    class acceptAsyncC extends ObjectContinuation implements IOContinuation {
    	Socket s;

	public void exception(Throwable t) {};

    	public acceptAsyncC() {
    		s= new Socket();
    		Scheduler.addRead(this);
    	}
    	
    	public void resume()
    	{
    	  try{
  	      // System.out.println("Calling accept 2... ");
	      implAccept(s);
	      // System.out.println("Accept done.");
	      next.resume(s);
    	  } catch (IOException e) { next.exception(e); }
    	}
    	
    	public java.io.FileDescriptor getFD()
    	{
    		return impl.fd;
    	}
   
    }

    // pesimistic version
    public ObjectContinuation acceptAsync()
    {
	try {
	    return ObjectDoneContinuation.pesimistic(acceptAsyncO());
	} catch (IOException e) {
	    return new ObjectDoneContinuation(e);
	}
    }
    /**
     * Subclasses of ServerSocket use this method to override accept()
     * to return their own subclass of socket.  So a FooServerSocket
     * will typically hand this method an <i>empty</i> FooSocket().  On
     * return from implAccept the FooSocket will be connected to a client.
     *
     * @since   JDK1.1
     */
    protected final void implAccept(Socket s) throws IOException {
	try {
	    s.impl.address = new InetAddress();
	    s.impl.fd = new FileDescriptor();
	    impl.accept(s.impl);
	    
	    SecurityManager security = System.getSecurityManager();
	    if (security != null) {
		security.checkAccept(s.impl.getInetAddress().getHostAddress(),
				     s.impl.getPort());
	    }
	} catch (IOException e) {
	    s.impl.close();
	    throw e;
	} catch (SecurityException e) {
	    s.impl.close();
	    throw e;
	}
    }

    /**
     * Closes this socket. 
     *
     * @exception  IOException  if an I/O error occurs when closing the socket.
     */
    public void close() throws IOException {
	impl.close();
    }

    /** Enable/disable SO_TIMEOUT with the specified timeout, in
     *  milliseconds.  With this option set to a non-zero timeout,
     *  a call to accept() for this ServerSocket
     *  will block for only this amount of time.  If the timeout expires,
     *  a <B>java.io.InterruptedIOException</B> is raised, though the
     *  ServerSocket is still valid.  The option <B>must</B> be enabled
     *  prior to entering the blocking operation to have effect.  The 
     *  timeout must be > 0.
     *  A timeout of zero is interpreted as an infinite timeout.  
     *
     * @since   JDK1.1
     */
    public synchronized void setSoTimeout(int timeout) throws SocketException {
	impl.setOption(SocketOptions.SO_TIMEOUT, new Integer(timeout));
    }

    /** Retrive setting for SO_TIMEOUT.  0 returns implies that the
     *  option is disabled (i.e., timeout of infinity).
     *
     * @since   JDK1.1
     */
    public synchronized int getSoTimeout() throws IOException {
	Object o = impl.getOption(SocketOptions.SO_TIMEOUT);
	/* extra type safety */
	if (o instanceof Integer) {
	    return ((Integer) o).intValue();
	} else {
	    return 0;
	}
    }

    /**
     * Returns the implementation address and implementation port of 
     * this socket as a <code>String</code>.
     *
     * @return  a string representation of this socket.
     */
    public String toString() {
	return "ServerSocket[addr=" + impl.getInetAddress() + 
		",port=" + impl.getPort() + 
		",localport=" + impl.getLocalPort()  + "]";
    }

    /**
     * The factory for all server sockets.
     */
    private static SocketImplFactory factory;

    /**
     * Sets the server socket implementation factory for the 
     * application. The factory can be specified only once. 
     * <p>
     * When an application creates a new server socket, the socket 
     * implementation factory's <code>createSocketImpl</code> method is 
     * called to create the actual socket implementation. 
     * <p>
     * If there is a security manager, this method first calls
     * the security manager's <code>checkSetFactory</code> method 
     * to ensure the operation is allowed. 
     * This could result in a SecurityException.
     *
     * @param      fac   the desired factory.
     * @exception  IOException  if an I/O error occurs when setting the
     *               socket factory.
     * @exception  SocketException  if the factory has already been defined.
     * @exception  SecurityException  if a security manager exists and its  
     *             <code>checkSetFactory</code> method doesn't allow the operation.
     * @see        java.net.SocketImplFactory#createSocketImpl()
     * @see        SecurityManager#checkSetFactory
     */
    public static synchronized void setSocketFactory(SocketImplFactory fac) throws IOException {
	if (factory != null) {
	    throw new SocketException("factory already defined");
	}
	SecurityManager security = System.getSecurityManager();
	if (security != null) {
	    security.checkSetFactory();
	}
	factory = fac;
    }
}
