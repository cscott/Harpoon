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
 * @version $Id: ReverseNavigator.java,v 1.1 2003-04-30 21:06:21 salcianu Exp $ */
public class ReverseNavigator implements Navigator {
    
    /** Creates a <code>ReverseNavigator</code> that is based on the
        existent navigator <code>old_nav</code>, but traverses the
        graph in reverse direction. */
    public ReverseNavigator(Navigator old_nav) {
        this.old_nav = old_nav;
    }
    private final Navigator old_nav;

    public Object[] next(Object node) { return old_nav.prev(node); }
    public Object[] prev(Object node) { return old_nav.next(node); }
}
