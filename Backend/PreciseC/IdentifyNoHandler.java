// IdentifyNoHandler.java, created Wed Jul 12 18:26:16 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.PreciseC;

import harpoon.ClassFile.HCodeEdge;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.JUMP;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.THROW;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Util;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
/**
 * <code>IdentifyNoHandler</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IdentifyNoHandler.java,v 1.3 2002-02-26 22:44:26 cananian Exp $
 */
public class IdentifyNoHandler {
    private Set nohandlercalls = new HashSet();
    /** Creates a <code>IdentifyNoHandler</code>. */
    public IdentifyNoHandler(harpoon.IR.Tree.Code c) {
	// post-order DFS.
	CFGrapher gr = c.getGrapher();
	dfs((Stm)gr.getFirstElement(c),gr,new HashSet(), new Visitor(gr));
	// done with init.
    }
    public boolean requiresHandler(CALL call) {
	return !nohandlercalls.contains(call);
    }
    private static void dfs(Stm stm, CFGrapher gr, Set seen, TreeVisitor v) {
	Util.ASSERT(!seen.contains(stm));
	seen.add(stm);
	for (Iterator it=gr.succC(stm).iterator(); it.hasNext(); ) {
	    Stm succ = (Stm) ((HCodeEdge)it.next()).to();
	    if (!seen.contains(succ))
		dfs(succ, gr, seen, v);
	}
	// post-order visit.
	stm.accept(v);
    }

    /* this is like a simple reaching-def analysis, except we want to
     * make sure that the exception isn't tested-and-branched-on before
     * it is thrown. */
    private class Visitor extends TreeVisitor {
	final CFGrapher gr;
	final MultiMap directlyThrows = new GenericMultiMap();
	Visitor(CFGrapher gr) { this.gr = gr; }
	public void visit(Tree t) {
	    Util.ASSERT(false, "should only visit stms");
	}
	public void visit(Stm t) { // only propagate if t is a no-op.
	    if (Stm.isNop(t)) propagate(t);
	}
	public void visit(THROW t) {
	    if (t.getRetex() instanceof TEMP)
		directlyThrows.put(t, ((TEMP)t.getRetex()).temp);
	}
	public void visit(MOVE t) {
	    propagate(t);
	    if (t.getDst() instanceof TEMP) {
		Temp dst = ((TEMP)t.getDst()).temp;
		if (directlyThrows.contains(t, dst)) { // kill
		    directlyThrows.remove(t, dst);
		    if (t.getSrc() instanceof TEMP) // gen
			directlyThrows.add(t, ((TEMP)t.getSrc()).temp);
		}
	    }
	}
	public void visit(CALL t) {
	    for (Iterator it=gr.succC(t).iterator(); it.hasNext(); ) {
		Stm stm = (Stm) ((HCodeEdge)it.next()).to();
		if (!(stm instanceof LABEL)) continue;
		if (!t.getHandler().label.equals(((LABEL)stm).label)) continue;
		// oh boy, this is the exception edge!
		Set thrown = (Set) directlyThrows.getValues(stm);
		if (thrown.contains(t.getRetex().temp))
		    // and we directly throw the retex!
		    nohandlercalls.add(t); // this is a no-handler call!
	    }
	}
	// unconditional JUMP has no effect on thrown set
	public void visit(JUMP t) {
	    if (t.getExp() instanceof NAME) propagate(t);
	}
	// LABEL has no effect on thrown set
	public void visit(LABEL t) { propagate(t); }
	
	// in set == out set.
	private void propagate(Tree t) {
	    Collection c = gr.succC(t);
	    Util.ASSERT(c.size()==1);
	    Stm next = (Stm) ((HCodeEdge)c.iterator().next()).to();
	    // note that thrown set is already defined because we're
	    // doing this is dfs post-order.
	    Set thrown = (Set) directlyThrows.getValues(next);
	    directlyThrows.addAll(t, thrown);
	}
    }
}
