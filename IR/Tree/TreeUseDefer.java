// TreeUseDefer.java, created Wed Feb 16 16:04:18 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Util.Default;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
/**
 * <code>TreeUseDefer</code> implements the <code>Properties.UseDefer</code>
 * interface for non-<code>SEQ</code> <code>Stm</code>s of a tree.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TreeUseDefer.java,v 1.2 2002-02-25 21:05:42 cananian Exp $
 */
public class TreeUseDefer extends harpoon.IR.Properties.UseDefer {
    private final Code code;
    
    /** Creates a <code>UseDefer</code>. */
    public TreeUseDefer(Code code) { this.code = code; }

    /** Returns a collection of <code>Temp</code>s defined by <code>hce</code>.
     *  <p>
     *  The only <code>Tree.Tree</code>s which define <code>Temp</code>s
     *  are <code>CALL</code>, <code>NATIVECALL</code>, <code>METHOD</code>,
     *  and <code>MOVE</code> when the destination expression is a
     *  <code>TEMP</code>.  For all other elements, this method returns
     *  a zero-element collection. */
    public Collection defC(HCodeElement hce) {
	Util.assert((hce instanceof Stm) && ! (hce instanceof SEQ));
	Tree tree = (Tree) hce;
	if (tree instanceof INVOCATION) {
	    INVOCATION invok = (INVOCATION) tree;
	    Collection c = new ArrayList(2);
	    if (invok.getRetval()!=null) c.add(invok.getRetval().temp);
	    if (invok instanceof CALL) c.add(((CALL)invok).getRetex().temp);
	    return c;
	}
	if (tree instanceof METHOD) {
	    TEMP[] rT = ((METHOD) tree).getParams();
	    Temp[] rt = new Temp[rT.length];
	    for (int i=0; i<rt.length; i++)
		rt[i] = rT[i].temp;
	    return Arrays.asList(rt);
	}
	if (tree instanceof MOVE && ((MOVE)tree).getDst() instanceof TEMP)
	    return Collections.singleton(((TEMP) ((MOVE)tree).getDst()).temp);
	// okay, no definitions then.
	return Collections.EMPTY_SET;
    }
    /** Returns a collection of <code>Temp</code>s which are used by the
     *  statement/expression subtree rooted at <code>hce</code>. */
    public Collection useC(HCodeElement hce) {
	Util.assert((hce instanceof Stm) && ! (hce instanceof SEQ));
	Set s = new HashSet();
	addUses(s, ((Tree)hce).kids());
	return s;
    }
    private static void addUses(Set uses, ExpList el) {
	for (ExpList elp = el; elp!=null; elp=elp.tail)
	    addUses(uses, elp.head);
    }
    private static void addUses(Set uses, Exp e) {
	if (e instanceof TEMP)
	    uses.add(((TEMP)e).temp);
	addUses(uses, e.kids());
    }
}
