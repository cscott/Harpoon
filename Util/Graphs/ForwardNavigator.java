// ForwardNavigator.java, created Wed May  7 10:47:23 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Graphs;

/**
 * <code>ForwardNavigator</code> is a forward-only graph navigator:
 * given a vertex, it returns its successors in the graph.  It is
 * extended by the <code>Navigator</code> interface which is a
 * bi-directional graph navigator.
 *
 * @see Navigator
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: ForwardNavigator.java,v 1.2 2004-02-08 04:53:35 cananian Exp $ */
public interface ForwardNavigator<Vertex> {
    
    /** Returns the successors of <code>vertex</code>. */
    Vertex[] next(Vertex vertex);

}
