// ************************************************************************
//    $Id: EventVariable.java,v 1.1 2002-07-02 15:35:18 wbeebee Exp $
// ************************************************************************
//
//                               jTools
//
//               Copyright (C) 2001-2002 by Angelo Corsaro.
//                         <corsaro@ece.uci.edu>
//                          All Rights Reserved.
//
//   Permission to use, copy, modify, and distribute this software and
//   its  documentation for any purpose is hereby  granted without fee,
//   provided that the above copyright notice appear in all copies and
//   that both that copyright notice and this permission notice appear
//   in  supporting  documentation. I don't make  any  representations
//   about the  suitability  of this  software for any  purpose. It is
//   provided "as is" without express or implied warranty.
//
//
// *************************************************************************
//  
// *************************************************************************
package edu.uci.ece.ac.concurrent;

/**
 * This class represent an <em>Event Variable</em> that can be used to
 * synchronize concurrent thread on the happening of a given event. 
 * This implementation of event variable actually laches the
 * <code>signal</code>, and make those available to later invocation
 * of <code>await</code>. This means that, an invocation of the method
 * <code>await</code> would not block if an invocation of the method
 * <code>signal</code> had preceeded it.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class EventVariable {

    private boolean signaled;

    public EventVariable() {
        this(false);
    }
    
    public EventVariable(boolean signaled) {
        this.signaled = signaled;
    }
    
    public synchronized void await() throws InterruptedException {
        if (!this.signaled)
            this.wait();
        this.signaled = false;
    }

    public synchronized void await(long timeoutMillis) throws InterruptedException {
        if (!this.signaled)
            super.wait(timeoutMillis);
        this.signaled = false;
    }

    public synchronized void await(long timeoutMillis, int timeoutNanos) throws InterruptedException {
        if (!this.signaled)
            this.wait(timeoutMillis, timeoutNanos);
        this.signaled = false;
    }

    public synchronized void signal() {
        this.signaled = true;
        this.notify();
    }

    public synchronized void broadCastSignal() {
        this.signaled = true;
        this.notifyAll();
    }
}
