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

package org.apache.java.net;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.java.lang.*;
import org.apache.java.security.*;

/**
 * This class implements an authenticated server socket that binds to
 * port and listens for authenticated connections. A socket connection to
 * be authenticated must come from an IP address contained into a address
 * filter list and go pass the authentication handshake defined in AJPv1.1
 * protocol specification.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1 $ $Date: 2000-07-18 22:07:21 $
 * @see     java.net.Socket
 * @see     org.apache.java.net.AuthenticatedSocket
 * @see     org.apache.java.security.MessageDigest
 */
public class AuthenticatedServerSocket extends ServerSocket {

    private byte[] secret;
    private long seed;
    private MessageDigest md;
    private Random random;
    private int challengeSize;
    private int maxConnections;
    private Vector filterList;

    /**
     * Construct the authenticated socket listening to the specified port
     * but with authentication disabled.
     * This is used when IP filtering is needed, but the authentication overhead
     * must be avoided (for example for localhost connections where only 127.0.0.1 is
     * allowed). It will attempt to bind the socket to localhost.
     */
    public AuthenticatedServerSocket(int port, int maxConnections, Vector filterList)
        throws IOException
    {
        this(port, maxConnections, filterList, null, null, 0,
            InetAddress.getLocalHost());
    }

    /**
     * Construct the authenticated socket listening to the specified port
     * but with authentication disabled.
     * This is used when IP filtering is needed, but the authentication overhead
     * must be avoided (for example for localhost connections where only 127.0.0.1 is
     * allowed). It will attempt to bind the socket to the value in ia.
     */
    public AuthenticatedServerSocket(int port, int maxConnections, Vector filterList,
        InetAddress ia)
        throws IOException
    {
        this(port, maxConnections, filterList, null, null, 0, ia);
    }

    /**
     * Construct the authenticated socket listening to the specified port.
     * It will attempt to bind the socket to the value in ia.
     * filterList can be null if security.allowedAddresses=DISABLED
     */
    public AuthenticatedServerSocket(int port, int maxConnections,
            Vector filterList, MessageDigest md, byte[] secret, int challengeSize,
                InetAddress ia)
        throws IOException
    {
        super(port, maxConnections, ia);

        this.maxConnections = maxConnections;

//        if (filterList == null) {
//            throw new IOException("Filter list cannot be null");
//        } else {
//            this.filterList = filterList;
//        }

        this.md = md;
        this.secret = secret;
        // SECURITY: random seeds coming from current time are considered
        // security hazards but this is not our case since the client
        // does not need to guess the random number since the challenge
        // string is sent clear.
        this.random = new Random(System.currentTimeMillis());

        // Check to see if challenge size is big enough
        this.challengeSize = (challengeSize < 5) ? 5 : challengeSize;
    }

    /**
     * Blocks until a connection is made to the port.
     * If the connection is authenticated is passed to the caller,
     * otherwise an AuthenticationException is thrown.
     * @exception AuthenticationException when the connection
     * is not authenticated.
     */
    public Socket accept() throws AuthenticationException {
        Socket s;
        InetAddress i;

        while (true) {
            try {
                s = super.accept();
                i = s.getInetAddress();
                break;
            } catch(IOException ignored) {}
        }

        try {
            if (filterList==null || (filterList!=null && filterList.contains(i))) {
                if (isAuthenticated(s)) {
                    return s;
                } else {
                    s.close();
                    throw new AuthenticationException("Connection from " 
                        + i + " refused due to authentication failure");
                }
            } else {
                throw new AuthenticationException("Connections from " 
                    + i + " are not allowed");
            }
        } catch (IOException e) {
            if (e instanceof AuthenticationException) {
                throw (AuthenticationException) e;
            } else {
                throw new AuthenticationException("Connection from " + i 
                    + " refused due to IO problems: " + e.getMessage());
            }
        }
    }

    /**
     * Used internally to authenticate a newly connected socket
     */
    private boolean isAuthenticated(Socket socket) {
        // authentication is disabled
        if (md == null) return true;

        try {
            OutputStream output = socket.getOutputStream();
            InputStream input = socket.getInputStream();

            // Generate random challenge string
            byte[] challenge = new byte[challengeSize];
            random.nextBytes(challenge);

            // Get current time in milliseconds since 01/01/1970
            long currentTime = System.currentTimeMillis();
            byte[] time = Bytes.toBytes(currentTime);

            // Concatenate random bytes with 64 bit time
            challenge = Bytes.append(challenge, time);

            // Transform challenge size into a 4 byte array
            byte[] challengeSize = Bytes.toBytes((int) challenge.length);
            // Send challenge size to client to let it know
            // the following challenge size
            output.write(challengeSize);
            // Send the random challenge string
            output.write(challenge);

            // Calculate server message digest
            byte[] serverHash = md.digest(Bytes.append(challenge, secret));

            // Receive client message digest
            byte[] clientHash = new byte[16];
            input.read(clientHash);

            return Bytes.areEqual(serverHash, clientHash);
        } catch (Throwable ouch) {
            // Anything wrong that may happened
            return false;
        }
    }

    /**
     * Return the IP address filter list used by this server socket.
     */
    public Vector getFilterList() {
        return filterList;
    }

    /**
     * Return the currently used challenge size.
     */
    public int getChallengeSize() {
        return challengeSize;
    }

    /**
     * Return maximum number of connections this socket can handle.
     */
    public int getMaxConnections() {
        return maxConnections;
    }
}
