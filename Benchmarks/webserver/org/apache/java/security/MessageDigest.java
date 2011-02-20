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

package org.apache.java.security;

/**
 * This interface abstracts a message digest algorithm.
 *
 * <p><b>Note:</b> even if standard Java 1.1 APIs already provide
 * a message digest implementation, this class is used on those
 * Java runtime environments (like Kaffe) where the package
 * <code>java.security</code> is highly improbable to be found.
 *
 * @author Stefano Mazzocchi <mazzocchi@mbox.systemy.it>
 * @version $Revision: 1.1 $ $Date: 2000-07-18 22:07:42 $
 */
public abstract class MessageDigest {

    /**
     * Creates the algorithm and reset its state.
     */
    public MessageDigest() {
        this.reset();
    }

    /**
     * Resets the state of the class. <b>Beware</b>: calling this method
     * erases all data previously inserted.
     */
    public abstract void reset();

    /**
     * Append another block to the message.
     */
    public void append(byte[] block) {
        this.append(block, 0, block.length);
    }

    /**
     * Append another block of specified length to the message.
     */
    public void append(byte[] block, int length) {
        this.append(block, 0, length);
    }

    /**
     * Append another block of specified length to the message
     * starting at the given offset.
     */
    public abstract void append(byte[] block, int offset, int length);

    /**
     * Appends a message block and return its message digest.
     */
    public byte[] digest(byte[] block) {
        return this.digest(block, 0, block.length);
    }

    /**
     * Appends a message block with specified length
     * and return its message digest.
     */
    public byte[] digest(byte[] block, int length) {
        return this.digest(block, 0, length);
    }

    /**
     * Appends a message block with specified length starting
     * from the given offset and return its message digest.
     */
    public abstract byte[] digest(byte[] message, int offset, int length);
}
