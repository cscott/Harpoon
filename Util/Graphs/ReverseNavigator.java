// ReverseNavigator.java, created Wed Apr 30 16:39:32 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Graphs;

/**
 * <code>ReverseNavigator</code> is a navigator that reverses an
 * existent navigator <code>old_nav</code>. I.e., its <code>next<code>
 * method returns the same result as <code>old_nav.prev</code>, and
 * its <code>prev<code> method returns the same result as
 * <code>old_nav.next</code>.
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: ReverseNavigator.java,v 1.2 2004-02-08 04:53:35 cananian Exp $ */
public class ReverseNavigator<Vertex> implements Navigator<Vertex> {
    
    /** Creates a <code>ReverseNavigator</code> that is based on the
        existent navigator <code>old_nav</code>, but traverses the
        graph in reverse direction. */
    public ReverseNavigator(Navigator<Vertex> old_nav) {
        this.old_nav = old_nav;
    }
    private final Navigator<Vertex> old_nav;

    public Vertex[] next(Vertex node) { return old_nav.prev(node); }
    public Vertex[] prev(Vertex node) { return old_nav.next(node); }
}
