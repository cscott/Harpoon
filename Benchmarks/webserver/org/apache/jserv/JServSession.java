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
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.*;
import javax.servlet.http.*;

/**
 * This is the object that encapsulates a session.
 *
 * @author Francis J. Lacoste
 * @author Vincent Partington
 * @version $Revision: 1.1 $ $Date: 2000-07-18 22:01:52 $
 */
public class JServSession implements HttpSession, JServLogChannels, Serializable {

    /**
     * The session id
     * @serial the id
     */
    protected String id;

    /**
     * The time at which this session has been created
     * @serial the creation time
     */
    protected long creationTime;

    /**
     * The last time the session was accessed.
     * @serial last access time
     */
    protected long lastAccessTime;

    /**
     * The session data
     * @serial the session data
     */
    private Hashtable sessionData;

    /**
     * The session context
     * @serial the context
     */
    private JServServletManager context;

    /**
     * Is this session valid
     * @serial is session valid
     */
    private boolean valid;

    /**
     * Is this session new
     * @serial is session new
     */
    private boolean isNew;

    /**
     * Creates a new session.
     *
     * @param id The id of the session.
     * @param context The context of the session.
     */
    public JServSession(String id, JServServletManager context) {
        this.valid = true;
        this.id = id;
        this.context = context;
        this.sessionData = new Hashtable();
        this.creationTime = System.currentTimeMillis();
        this.lastAccessTime = creationTime;
        this.isNew = true;
    }

    /**
     * Returns the identifier assigned to this session. An HttpSession's
     * identifier is a unique string that is created and maintained by
     * HttpSessionContext.
     *
     * @return the identifier assigned to this session
     * @exception IllegalStateException if an attempt is made to access
     * session data after the session has been invalidated
     */
    public synchronized String getId() {
        checkState();
        return id;
    }

    /**
     * Returns the context in which this session is bound.
     *
     * @return the name of the context in which this session is bound
     * @exception IllegalStateException if an attempt is made to access
     * session data after the session has been invalidated
     */
    public synchronized HttpSessionContext getSessionContext() {
        checkState();
        return context;
    }

    /**
     * Sets the context in which this session is bound.
     * <p>
     * This is used by JServServletManager.init() to restore the 
     * contexts to the sessions after they have been serialized out.
     */
    synchronized void setSessionContext(JServServletManager context) {
        this.context = context;
    }

    /**
     * Returns the time at which this session representation was created,
     * in milliseconds since midnight, January 1, 1970 UTC.
     * @return the time when the session was created.
     * @exception IllegalStateException if an attempt is made to access
     * session data after the session has been invalidated
     */
    public synchronized long getCreationTime() {
        checkState();
        return creationTime;
    }

    /**
     * Returns the last time the client sent a request carrying the identifier
     * assigned to the session. Time is expressed
     * as milliseconds since midnight, January 1,
     * 1970 UTC.
     * Application level operations, such as getting or setting a value
     * associated with the session, does not affect the access time.
     *
     * <P> This information is particularly useful in session management
     * policies.  For example,
     * <UL>
     * <LI>a session manager could leave all sessions
     * which have not been used in a long time
     * in a given context.
     * <LI>the sessions can be sorted according to age to optimize some task.
     * </UL>
     *
     * @return the last time the client sent a request carrying the identifier
     * assigned to the session.
     * @exception IllegalStateException if an attempt is made to access
     * session data after the session has been invalidated
     */
    public synchronized long getLastAccessedTime() {
        checkState();
        return lastAccessTime;
    }

    /**
     * Causes this representation of the session to be invalidated and removed
     * from its context.
     * @exception IllegalStateException if an attempt is made to access
     * session data after the session has been invalidated
     */
    public synchronized void invalidate() {
        checkState();

        // deliver HttpSessionBindingEvent to all values that want it
        Enumeration namesEnum;
        Object name, value;

        namesEnum = sessionData.keys();
        while(namesEnum.hasMoreElements()) {
            name = namesEnum.nextElement();
            value = sessionData.get(name);
            if(value instanceof HttpSessionBindingListener) {
                HttpSessionBindingListener listener =
                    (HttpSessionBindingListener) value;
		try {
		    listener.valueUnbound(new HttpSessionBindingEvent(this,(String) name));
		} catch (Throwable t) {
		    log(CH_DEBUG,"JServSession.invalidate", t);
		}
            }
        }

        valid = false;

        context.removeSession(this);
    }

    /**
     * Binds the specified object into the session's application layer data
     * with the given name.  Any existing binding with the same name is
     * replaced.  New (or existing) values that implement the
     * HttpSessionBindingListener interface will call its
     * valueBound() method.
     *
     * @param name the name to which the data object will be bound.  This
     * parameter cannot be null.
     * @param value the data object to be bound.  This parameter cannot be null.
     * @exception IllegalStateException if an attempt is made to access
     * session data after the session has been invalidated
     */
    public synchronized void putValue(String name, Object value) {
        checkState();
        removeValue(name);
        sessionData.put(name, value);
        if (value instanceof HttpSessionBindingListener) {
            HttpSessionBindingListener listener =
                (HttpSessionBindingListener) value;
	    try {
		listener.valueBound(new HttpSessionBindingEvent(this, name));
	    } catch (Throwable t) {
		log(CH_DEBUG, "JServSession.putValue", t);
	    }
        }
    }

    /**
     * Returns the object bound to the given name in the session's
     * application layer data.  Returns null if there is no such binding.
     *
     * @param name the name of the binding to find
     * @return the value bound to that name, or null if the binding does
     * not exist.
     * @exception IllegalStateException if an attempt is made to access
     * session data after the session has been invalidated
     */
    public synchronized Object getValue(String name) {
        checkState();
        return sessionData.get(name);
    }

    /**
     * Removes the object bound to the given name in the session's
     * application layer data.  Does nothing if there is no object
     * bound to the given name.  The value that implements the
     * HttpSessionBindingListener interface will call its
     * valueUnbound() method.
     *
     * @param name the name of the object to remove
     * @exception IllegalStateException if an attempt is made to access
     * session data after the session has been invalidated
     */
    public synchronized void removeValue(String name) {
        checkState();
        Object value = null;
        try
        {
            value = sessionData.get(name);
            if (value instanceof HttpSessionBindingListener) {
                HttpSessionBindingListener listener =
                    (HttpSessionBindingListener)value;
	        try {
		    listener.valueUnbound(
                        new HttpSessionBindingEvent(this, name));
	        } catch (Throwable t) {
		    log(CH_DEBUG, "JServSession.removeValue", t);
	        }
            }
        } finally {
            sessionData.remove(name);
        }
    }

    /**
     * Returns an array of the names of all the application layer
     * data objects bound into the session. For example, if you want to delete
     * all of the data objects bound into the session, use this method to
     * obtain their names.
     * @return an array containing the names of all of the application layer
     * data objects bound into the session
     * @exception IllegalStateException if an attempt is made to access
     * session data after the session has been invalidated
     */
    public synchronized String[] getValueNames() {
        checkState();
        Vector buf = new Vector();
        Enumeration namesEnum = sessionData.keys();
        while (namesEnum.hasMoreElements()) {
            buf.addElement(namesEnum.nextElement());
        }
        String[] names = new String[buf.size()];
        buf.copyInto(names);
        return names;
    }

    /**
     * A session is considered to be "new" if it has been created by the server,
     * but the client has not yet acknowledged joining the session. For example,
     * if the server supported only cookie-based sessions and the client had
     * completely disabled the use of cookies, then calls to
     * HttpServletRequest.getSession() would
     * always return "new" sessions.
     * @return true if the session has been created by the server but the
     * client has not yet acknowledged joining the session; false otherwise
     */
    public synchronized boolean isNew() {
        checkState();
        return isNew;
    }

    /**
     * Has the session been invalidated.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Tells the session that it has been accessed
     */
    public synchronized void access() {
        lastAccessTime = System.currentTimeMillis();
        isNew = false;
    }

    /**
     * Throws an IllegalStateException when the session is no longer valid.
     */
    private void checkState() {
        if (!valid) {
            throw new IllegalStateException("Session "
                + id + " has been invalidated.");
        }
    }

    /**
     * Logs an exception report to the servlet log file.
     *
     * @param msg The message to be written
     * @param t The exception to be written
     */
    private void log(String channel, String msg, Throwable t) {

	CharArrayWriter buf = new CharArrayWriter();
	PrintWriter writer = new PrintWriter(buf);
	writer.println(msg);
	t.printStackTrace(writer);
	JServ.log.log(channel, buf.toString());

    }

    /**
     * Read the instance variables for this object from the specified
     * object input stream.
     *
     * @param stream The stream from which to read
     *
     * @exception ClassNotFoundException if a required class cannot be
     *  found while deserializing
     * @exception IOException if an input error occurs while reading
     */
    private void readObject(ObjectInputStream stream)
        throws ClassNotFoundException, IOException {

        // Read the session identifier
        id = (String) stream.readObject();

        // Read the other scalar instance variables
        creationTime = ((Date) stream.readObject()).getTime();
        lastAccessTime = ((Date) stream.readObject()).getTime();
        valid = ((Boolean) stream.readObject()).booleanValue();
        isNew = ((Boolean) stream.readObject()).booleanValue();

        sessionData = (Hashtable) stream.readObject();
    }

    /**
     * Write the instance variables for this object to the specified
     * object output stream.
     *
     * @param stream The stream on which to write
     *
     * @exception IOException if an output error occurs while writing
     */
    private synchronized void writeObject(ObjectOutputStream stream)
        throws IOException {

        // Write the session identifier
        stream.writeObject(id);

        // Write the other scalar instance variables
        stream.writeObject(new Date(creationTime));
        stream.writeObject(new Date(lastAccessTime));
        stream.writeObject(new Boolean(valid));
        stream.writeObject(new Boolean(isNew));

        // write out the user session data that is Serializable
        Hashtable saveData = new Hashtable(sessionData.size());
        String key = null;
        Object value = null;
        
        Enumeration keys = sessionData.keys();
        while(keys.hasMoreElements())
        {
            key = (String) keys.nextElement();
            value = sessionData.get(key);
            if (value instanceof Serializable) {
                saveData.put(key, value);
            }
            // if we can't serialize the object stored in 
            // the session, then check to see if it implements 
            // HttpSessionBindingListener and then call its 
            // valueUnbound method, allowing it to save its state
            // correctly instead of just being lost into the etherworld
            else if (value instanceof HttpSessionBindingListener ) {
                try {
                    HttpSessionBindingListener event = 
                        (HttpSessionBindingListener) sessionData.get(key);
                    event.valueUnbound(new HttpSessionBindingEvent(this, key));
                } catch (Exception e) {
                }
            }
        }
        stream.writeObject(saveData);
    }
}
