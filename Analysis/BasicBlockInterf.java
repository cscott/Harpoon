// BasicBlockInterf.java, created Fri Dec 14 19:37:55 2001 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import java.util.Set;
import java.util.List;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;

/**
 * <code>BasicBlockInterf</code> is the interface that needs to be
 * implemented by any &quot;basic block&quot;-like structure. Basic
 * block views of the code groups together lists of consecutive
 * instructions in basic blocks. For each basic block, we have a list
 * of statements contained by it, a set of predecessor basic blocks
 * and a set of successor basic blocks. Different implementations of 
 * <code>BasicBlockInterf</code> respect different sets of constraints.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: BasicBlockInterf.java,v 1.1.2.1 2001-12-16 05:00:05 salcianu Exp $ */
public interface BasicBlockInterf {
    
    /** Returns the first statement of the basic block. */
    public HCodeElement getFirst();

    /** Returns the last statement of the basic block. */
    public HCodeElement getLast();

    /** Returns <i>all</i> the predecessors of the basic block,
        according to the normal <i>and</i> the exceptional control flow. */
    public Set prevSet();

    /** Returns <i>all</i> the successors of the basic block,
        according to the normal <i>and</i> the exceptional control flow. */
    public Set nextSet();

    /** Returns the list of the statements composing the basic
	block. */
    public List statements();

    /** Calls the appropriate <code>visit</code> method from
	<code>visitor</code>. The concept of
	<code>BasicBlockInterfVisitor</code> is similar to the concept
	of <code>QuadVisitor</code>. Both of them were introduced to
	allow pure object oriented programming, that is virtual
	methods instead of <code>instanceof</code> tests. */
    public void accept(BasicBlockInterfVisitor visitor);
}
