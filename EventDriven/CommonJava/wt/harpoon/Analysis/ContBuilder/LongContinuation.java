// LongContinuation.java, created Fri Nov  5 14:44:17 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.ContBuilder;

/**
 * <code>LongContinuation</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: LongContinuation.java,v 1.1 2000-03-24 02:32:25 govereau Exp $
 */
public abstract class LongContinuation implements Continuation {
    protected LongResultContinuation next;

    public void setNext(LongResultContinuation next) {
	this.next = next;
    }
    public long result;
    public boolean done;
}
