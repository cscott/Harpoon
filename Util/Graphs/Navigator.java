// Navigator.java, created Mon Apr  1 23:43:47 2002 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Graphs;

/** The <code>Navigator</code> interface allows graph algorithms to
    detect (and use) the arcs from and to a certain vertex.  This allows
    the use of many graph algorithms (eg construction of strongly
    connected components) even for very general graphs where the arcs
    model only a subtle semantic relation (eg caller-callee) that is
    not directly stored in the structure of the vertices.

   @author  Alexandru SALCIANU <salcianu@MIT.EDU>
   @version $Id: Navigator.java,v 1.3 2004-02-08 04:53:35 cananian Exp $ */
public interface Navigator<Vertex> extends ForwardNavigator<Vertex> {
    
    /** Returns the predecessors of <code>vertex</code>. */
    Vertex[] prev(Vertex vertex);
    
}
