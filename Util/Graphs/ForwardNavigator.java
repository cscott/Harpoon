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
 * @version $Id: ForwardNavigator.java,v 1.1 2003-05-09 15:44:13 salcianu Exp $ */
public interface ForwardNavigator/*<Vertex extends Object>*/ {
    
    /** Returns the successors of <code>vertex</code>. */
    Object[] next(Object vertex);

}
