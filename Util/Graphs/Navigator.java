// Navigator.java, created Mon Apr  1 23:43:47 2002 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Graphs;

/** The <code>Navigator</code> interface allows graph algorithms to
    detect (and use) the arcs from and to a certain node. This allows
    the use of many graph algorithms (eg construction of strongly
    connected components) even for very general graphs where the arcs
    model only a subtle semantic relation (eg caller-callee) that is
    not directly stored in the structure of the nodes.

   @author  Alexandru SALCIANU <salcianu@MIT.EDU>
   @version $Id: Navigator.java,v 1.1 2002-04-02 23:48:44 salcianu Exp $ */
public interface Navigator {
    
    /** Returns the predecessors of <code>node</code>. */
    public Object[] next(Object node);

    /** Returns the successors of <code>node</code>. */
    public Object[] prev(Object node);
    
}
