// FloatContinuation.java, created Fri Nov  5 14:44:17 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.ContBuilder;

/**
 * <code>FloatContinuation</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: FloatContinuation.java,v 1.1.2.2 1999-11-12 05:18:37 kkz Exp $
 */
public abstract class FloatContinuation implements Continuation {
    protected FloatResultContinuation next;

    public void setNext(FloatResultContinuation next) {
	this.next = next;
    }
}
