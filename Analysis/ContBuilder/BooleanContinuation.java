// BooleanContinuation.java, created Fri Nov  5 14:44:17 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.ContBuilder;

/**
 * <code>BooleanContinuation</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: BooleanContinuation.java,v 1.1.2.1 1999-11-06 05:28:23 kkz Exp $
 */
public abstract class BooleanContinuation extends Continuation {
    protected BooleanResultContinuation next;
    
    public void setNext(BooleanResultContinuation next) {
	this.next = next;
    }
}
