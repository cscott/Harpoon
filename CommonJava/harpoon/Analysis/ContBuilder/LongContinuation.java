// LongContinuation.java, created Fri Nov  5 14:44:17 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.ContBuilder;

/**
 * <code>LongContinuation</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: LongContinuation.java,v 1.2 2000-03-17 19:41:20 bdemsky Exp $
 */
public abstract class LongContinuation implements Continuation {
    protected LongResultContinuation next;

    public void setNext(LongResultContinuation next) {
	this.next = next;
    }
    public long result;
    public boolean done;
}
