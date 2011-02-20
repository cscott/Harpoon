// ObjectContinuation.java, created Fri Nov  5 14:44:17 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.ContBuilder;

/**
 * <code>ObjectContinuation</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: ObjectContinuation.java,v 1.3 2000-03-17 20:49:19 bdemsky Exp $
 */
public abstract class ObjectContinuation implements Continuation {
    protected ObjectResultContinuation next;

    public void setNext(ObjectResultContinuation next) {
	this.next = next;
    }

    public Object result;
    public boolean done;    
}
