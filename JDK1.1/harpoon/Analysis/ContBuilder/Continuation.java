// Continuation.java, created Wed Nov  3 20:30:45 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.ContBuilder;

/**
 * <code>Continuation</code> is a do-nothing interface that enables us to
 * deal with <code>Continuation</code>s together.
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: Continuation.java,v 1.1 2000-03-15 17:52:35 bdemsky Exp $
 */
public interface Continuation {
    public void exception(Throwable t);

    public void setLink(Continuation newLink);

    public Continuation getLink();
}
