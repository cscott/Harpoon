// CacheEquivalence.java, created Wed Jun  6 15:06:02 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.Analysis.DomTree;
import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Tree.Code;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeDerivation;
import harpoon.IR.Tree.TreeKind;
import harpoon.Temp.Temp;
import harpoon.Util.Environment;
import harpoon.Util.HashEnvironment;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
/**
 * <code>CacheEquivalence</code> creates tag-check equivalence classes
 * for MEM operations in a Tree.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CacheEquivalence.java,v 1.1.2.3 2001-06-12 03:55:12 cananian Exp $
 */
public class CacheEquivalence {
    
    /** Creates a <code>CacheEquivalence</code>. */
    public CacheEquivalence(harpoon.IR.Tree.Code code) {
	TreeDerivation td = code.getTreeDerivation();
	DomTree dt = new DomTree(code, code.getGrapher(), false);
	/* zip down through dominator tree, collecting info */
	Environment e = new HashEnvironment();
	HCodeElement[] roots = dt.roots();
	for (int i=0; i<roots.length; i++)
	    traverseDT((Stm) roots[i], dt, e, td);
	/* okay, done with analysis! */
    }
    /* analyze stms travelling down the dominator tree */
    void traverseDT(Stm stm, DomTree dt, Environment e, TreeDerivation td) {
	/* save environment */
	Environment.Mark mark = e.getMark();
	/* do analysis */
	analyze(stm, e, td);
	/* recurse */
	HCodeElement[] child = dt.children(stm);
	for (int i=0; i<child.length; i++)
	    traverseDT((Stm)child[i], dt, e, td);
	/* restore environment */
	e.undoToMark(mark);
	/* done! */
	return;
    }
    /* analyze one statement in the environment defined by map m */
    void analyze(Stm stm, Map pre, TreeDerivation td) {
	Map post = new HashMap();
	/* first look for all *reads* */
	/* There is NO ORDER defined for any of these. */
	for (ExpList el=stm.kids(); el!=null; el=el.tail) {
	    add(el.head, pre, post, true, td);
	}
	/* now all post mappings get added to pre */
	pre.putAll(post); post.clear();
	/* now look for writes (which must happen after reads) */
	if (stm.kind()==TreeKind.MOVE)
	    add(((MOVE)stm).getDst(), pre, post, false, td);
	pre.putAll(post); post.clear();
    }
    void add(Exp e, Map pre, Map post, boolean recurse, TreeDerivation td) {
	if (e.kind()==TreeKind.MEM) {
	    MEM mem = (MEM) e;  Exp memexp = mem.getExp();
	    /* three cases: 1) a temp, 2) derivation from a temp,
	     * 3) a random temporary value. */
	    Temp t = null;
	    if (memexp.kind()==TreeKind.TEMP) { /* case 1 */
		t = ((TEMP)memexp).temp;
	    } else if (td.typeMap(memexp)==null) { /* case 2 */
		DList dl = td.derivation(memexp);
		if (dl.next==null && dl.sign)
		    t = dl.base;
	    }
	    if (t!=null) {
		CacheEquivSet ces = (CacheEquivSet) pre.get(t);
		if (ces==null) ces = new CacheEquivSet(mem);
		else ces.others.add(mem);
		cache_equiv.put(mem, ces);
		post.put(t, ces);
	    } else { /* case 3 */
		cache_equiv.put(mem, new CacheEquivSet(mem));
	    }
	}
	if (recurse)
	    for (Tree tp=e.getFirstChild(); tp!=null; tp=tp.getSibling())
		add((Exp)tp, pre, post, recurse, td);
    }

    static class CacheEquivSet {
	public final MEM first;
	public final Set others = new HashSet();
	public CacheEquivSet(MEM mem) { this.first = mem; }
    }
    final Map cache_equiv = new HashMap();

    /** Returns the number of memory operations which share the same
     *  tag as this memory operation.  1 indicates no sharing possible. */
    public int num_using_this_tag(MEM mem) {
	return ((CacheEquivSet) cache_equiv.get(mem)).others.size() + 1;
    }
    /** Returns 'true' if this operation requires a tag check.  If
     *  ops_using_this_tag(mem) is also true, then you should store the
     *  result of the tag check some where for further use. */
    public boolean needs_tag_check(MEM mem) {
	return whose_tag_check(mem) == mem;
    }
    /** Returns the MEM operation which should have stored the
     *  necessary tag information for this MEM operation. */
    public MEM whose_tag_check(MEM mem) {
	return ((CacheEquivSet) cache_equiv.get(mem)).first;
    }
    /** Returns all the MEM operations which use the tag defined
     *  by whose_tag_check(mem) */
    public Set ops_using_this_tag(MEM mem) {
	return Collections.unmodifiableSet
	    (((CacheEquivSet) cache_equiv.get(mem)).others);
    }
}
