// DoubleContinuation.java, created Fri Nov  5 14:44:17 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.ContBuilder;

/**
 * <code>DoubleContinuation</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: DoubleContinuation.java,v 1.1 2000-03-15 17:52:35 bdemsky Exp $
 */
public abstract class DoubleContinuation implements Continuation {
    protected DoubleResultContinuation next;

    public void setNext(DoubleResultContinuation next) {
	this.next = next;
    }
}
