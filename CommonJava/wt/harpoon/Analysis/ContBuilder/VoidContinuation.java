// IntContinuation.java, created Fri Nov  5 14:44:17 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.ContBuilder;

/**
 * <code>VoidContinuation</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: VoidContinuation.java,v 1.1 2000-03-24 02:32:25 govereau Exp $
 */
public abstract class VoidContinuation implements Continuation {
    protected VoidResultContinuation next;

    public void setNext(VoidResultContinuation next) {
	this.next = next;
    }
    public boolean done;
}





