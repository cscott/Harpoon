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

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.java.util.*;

/**
 * This class is used to store a set of JServContexts for servlets which
 * implement the javax.servlet.SingleThreadModel interface. It's a kind
 * of pool designed to work with JServServletManager.
 *
 * @author Radu Greab
 * @version $Revision: 1.1 $ $Date: 2000-06-29 01:41:56 $
 */
class JServSTMStore implements JServLogChannels {

    /**
     * The Servlet Manager which must create in behalf of us new servlet
     * instances when needed.
     */
    private JServServletManager servletManager;

    /**
     * The name (or class name or ...) of the servlet stored here.
     */
    private String servletName;

    /**
     * The array of free contexts. We use this array like a stack, where the
     * top of the stack is determined by <code>indexFree</code>.
     */
    private JServContext freeContexts[] = null;

    /**
     * The index used in the array of free contexts.
     */
    private int indexFree = -1;

    /**
     * We need a copy of all the contexts, busy or free. We must provide
     * to the servlet manager the list of all instances in this set.
     * We also need this copy when we check a returned context.
     */
    private JServContext allContexts[] = null;

    /**
     * The index in the <code>allContexts</code> array. This index is
     * incremented as new instances are added. It also shows how many
     * instances we have in this pool.
     */
    private int indexAll = -1;

    /**
     * The initial capacity of this store
     */
    private int initialCapacity;

    /**
     * The increment capacity parameter. It shows with how many elements
     * to grow the array.
     */
    private int incrementCapacity;

    /**
     * The maximum size of this pool.
     */
    private int maximumCapacity;

    /**
     * Constructs a store fill it with contexts.
     *
     * @param confs Configuration parameters for this class
     * @param servletManager
     * @param servletName
     * @param sendError
     * @param firstInstance An already created and inited
     * instance of the servlet
     * @throws ServletException if an error occurs.
     */
    JServSTMStore(Configurations confs, JServServletManager servletManager,
        String servletName, JServSendError sendError,
        JServContext firstInstance)
    throws ServletException {

        this.servletManager = servletManager;
        this.servletName = servletName;

        this.initialCapacity =
            confs.getInteger("singleThreadModelServlet.initialCapacity", 5);
        this.incrementCapacity =
            confs.getInteger("singleThreadModelServlet.incrementCapacity", 5);
        this.maximumCapacity =
            confs.getInteger("singleThreadModelServlet.maximumCapacity", 10);

        // don't exceed the maximum capacity
        int size = (initialCapacity < maximumCapacity)
            ? initialCapacity : maximumCapacity;

        if (size <= 0) size = 1; // we have one servlet already

        freeContexts = new JServContext[size];
        allContexts = new JServContext[size];

        freeContexts[++indexFree] = firstInstance;
        allContexts[++indexAll] = firstInstance;
        size--;

        if (!JServ.TURBO && JServ.log.active) {
            JServ.log.log(CH_DEBUG,
                "Created SingleThreadModel store for servlet \""
                + servletName + "\" with initial capacity "
                + (size + 1) + " and added a context.");
        }

        while (size > 0) {
            addContext(sendError);
            size--;
        }
    }

    /**
     * Add a new created context.
     *
     * @param sendError The sendError handler to report errors.
     * @throws ServletException If an error occurs when creating the servlet.
     */
    private void addContext(JServSendError sendError) throws ServletException {

        if (indexAll + 1 >= allContexts.length) {
            // we need to grow
            int newSize = (indexAll + 1 + incrementCapacity < maximumCapacity ?
                indexAll + 1 + incrementCapacity : maximumCapacity);
            if (newSize <= indexAll + 1) {
                return;
            }

            JServContext tmp[] = new JServContext[newSize];
            System.arraycopy(allContexts, 0, tmp, 0, allContexts.length);
            allContexts = tmp;

            tmp = new JServContext[newSize];
            System.arraycopy(freeContexts, 0, tmp, 0, freeContexts.length);
            freeContexts = tmp;

            if (!JServ.TURBO && JServ.log.active) {
                JServ.log.log(CH_DEBUG,
                    "Incremented SingleThreadModel servlet \""
                    + servletName + "\" store size to " + newSize);
            }
        }

        JServContext context = servletManager.load_init(servletName, sendError);
        if (context != null) {
            freeContexts[++indexFree] = context;
            allContexts[++indexAll] = context;
            if (!JServ.TURBO && JServ.log.active) {
                JServ.log.log(CH_DEBUG,
                    "Added a new SingleThreadModel servlet \"" + servletName
                    + "\" in the store. Total servlets: " + (indexAll + 1)
                    + ". Free servlets: " + (indexFree + 1) + ".");
            }
        }
    }

    /**
     * Get a free context (servlet) from the pool. If no context is
     * available and we have not reached the maximumCapacity we will
     * first expand the store with new servlets.
     *
     * @param sendError The error handler used to report errors.
     * @return A free context or null if no free context is available.
     * @throws javax.servlet.ServletException if an error occurs when
     * we create a new servlet.
     */
    synchronized JServContext getContext(JServSendError sendError)
        throws ServletException {

        JServContext context;

        if (indexFree < 0) {
            // should we grow?
            if (indexAll + 1 >= maximumCapacity) {
                sendError.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "The servlet named \"" + servletName + "\" is a "
                    + "javax.servlet.SingleThreadModel servlet and no "
                    + "instance is available. Try again later.");
                if (!JServ.TURBO && JServ.log.active) {
                    JServ.log.log(CH_WARNING,
                        "Maximum number of instances for SingleThreadModel servlet \""
                        + servletName + "\" reached. All of them are busy.");
                    }
                return null;
            }

            // add new servlets
            int newServlets = (indexAll + 1 + incrementCapacity <= maximumCapacity)
                ? incrementCapacity : maximumCapacity - indexAll - 1;
            while (newServlets > 0) {
                addContext(sendError);
                newServlets--;
            }

            // have we now free servlets?
            if (indexFree < 0) {
                sendError.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "The servlet named \"" + servletName + "\" isn't"
                    + "free right now. Try again later.");
                return null;
            }
        }

        context = freeContexts[indexFree--];
        if (!JServ.TURBO && JServ.log.active) {
            JServ.log.log(CH_DEBUG,
                "Giving a free context for SingleThreadModel servlet \""
                + servletName + "\". Remaining free contexts: "
                + (indexFree + 1) + ".");
        }

        return context;
    }

    /**
     * Return a free context to the pool after it was used.
     *
     * @param context the context to free
     */
    synchronized void returnContext(JServContext context) {
        if (!JServ.TURBO && JServ.log.active) {
            JServ.log.log(CH_DEBUG,
                "Got back a context for SingleThreadModel servlet \"" +
                servletName + "\".");
        }

        // check to see if the context is valid for us
        boolean validContext = false;
        for (int i = 0; i <= indexAll && !validContext; i++) {
            if (context == allContexts[i]) {
                validContext = true;
            }
        }

        if (!validContext) {
            if (!JServ.TURBO && JServ.log.active) {
                JServ.log.log(CH_DEBUG,
                "The context is not valid. Maybe it's an old context.");
            }
            return;
        }

        // check to see if the context is free already
        validContext = true;
        for (int i = 0; i <= indexFree && validContext; i++) {
            if (context == freeContexts[i]) {
                validContext = false;
            }
        }

        if (!validContext) {
            if (!JServ.TURBO && JServ.log.active) {
                JServ.log.log(CH_DEBUG,
                    "The context is already free.");
            }
            return;
        }

        if (indexFree - 1 >= freeContexts.length) {
            // we are in trouble, somebody has returned to many contexts.
            // give up.
            if (!JServ.TURBO && JServ.log.active) {
                JServ.log.log(CH_WARNING,
                    "Deep trouble: no free slots?");
            }
            return;
        }

        freeContexts[++indexFree] = context;
        if (!JServ.TURBO && JServ.log.active) {
            JServ.log.log(CH_DEBUG,
                "Marked this context as free. Free contexts: "
                + (indexFree + 1) + ".");
        }
    }

    /**
     * Return the number of total contexts stored in this poll.
     *
     * @return The total number of contexts stored here.
     */
    int size() {
        return indexAll + 1;
    }

    /**
     * Return the servlets stored in this pool.
     *
     * @return An array with all the servlets stored here.
     */
    synchronized Servlet[] getServlets() {

        Servlet servlets[];

        if (allContexts == null) {
            servlets = new Servlet[0];
        } else {
            servlets = new Servlet[indexAll + 1];
        }

        for (int i = indexAll; i >= 0; i--) {
            servlets[i] = allContexts[i].servlet;
        }

        return servlets;
    }

    /**
     * Return all the contexts stored here and clear the content of the pool.
     *
     * @return An array with all the contexts stored here.
     */
    synchronized JServContext[] clear() {

        if (!JServ.TURBO && JServ.log.active) {
            JServ.log.log(CH_DEBUG,
                "Clearing store for SingleThreadModel servlet \""
                + servletName + "\".");
        }

        JServContext contexts[] = new JServContext[indexAll + 1];
        System.arraycopy(allContexts, 0, contexts, 0, indexAll + 1);
        allContexts = freeContexts = null;
        indexAll = indexFree = -1;

        return contexts;
    }
}
