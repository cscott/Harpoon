// BooleanContinuation.java, created Fri Nov  5 14:44:17 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.ContBuilder;

/**
 * <code>BooleanContinuation</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: BooleanContinuation.java,v 1.2 2000-03-17 20:49:19 bdemsky Exp $
 */
public abstract class BooleanContinuation implements Continuation {
    protected BooleanResultContinuation next;
    
    public void setNext(BooleanResultContinuation next) {
	this.next = next;
    }
    public boolean result;
    public boolean done;
}
