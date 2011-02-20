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
 * filter list and go pass the authentication handshake.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1 $ $Date: 2000-06-29 01:41:41 $
 * @see org.apache.java.net.AuthenticatedServerSocket
 */

public class AuthenticatedSocket extends Socket {

    public AuthenticatedSocket(InetAddress ia, int port, MessageDigest md, 
            byte[] secret)
        throws IOException, AuthenticationException {
        super(ia, port);

        try {
            OutputStream output = this.getOutputStream();
            InputStream input = this.getInputStream();

            // Get challenge length
            byte[] challengeSize = new byte[4];
            input.read(challengeSize);

            // Receive random challenge string
            byte[] challenge = new byte[Bytes.toInt(challengeSize)];
            input.read(challenge);

            // Generate MD5 hash
            byte[] append = Bytes.append(challenge, secret);
            byte[] hash = md.digest(append);

            // Send time and hash strings
            output.write(hash);
        } catch (Exception e) {
            throw new AuthenticationException("Authentication failed: "
                + e.getMessage());
        }
    }

    public AuthenticatedSocket(String address, int port, MessageDigest md, 
            byte[] secret)
        throws IOException, AuthenticationException {

        super(address, port);

        try {
            OutputStream output = this.getOutputStream();
            InputStream input = this.getInputStream();

            // Get challenge length
            byte[] challengeSize = new byte[4];
            input.read(challengeSize);

            // Receive random challenge string
            byte[] challenge = new byte[Bytes.toInt(challengeSize)];
            input.read(challenge);

            // Generate MD5 hash
            byte[] append = Bytes.append(challenge, secret);
            byte[] hash = md.digest(append);

            // Send time and hash strings
            output.write(hash);
        } catch (Exception e) {
            throw new AuthenticationException("Authentication failed: " 
                + e.getMessage());
        }
    }
    
    
    public AuthenticatedSocket(String address, int port) throws IOException{
        super(address, port);
    }

    public AuthenticatedSocket(InetAddress ia, int port) throws IOException{
        super(ia, port);
    }
}
