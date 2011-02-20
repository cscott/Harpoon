// ByteContinuation.java, created Fri Nov  5 14:44:17 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.ContBuilder;

/**
 * <code>ByteContinuation</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: ByteContinuation.java,v 1.2 2000-03-17 19:41:19 bdemsky Exp $
 */
public abstract class ByteContinuation implements Continuation {
    protected ByteResultContinuation next;

    public void setNext(ByteResultContinuation next) {
	this.next = next;
    }
    public byte result;
    public boolean done;
}
