// CacheEquivalence.java, created Wed Jun  6 15:06:02 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.DomTree;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsAltImpl;
import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.Backend.Generic.Runtime.TreeBuilder;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCode.PrintCallback;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.Code;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.INVOCATION;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.METHOD;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.Print;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeDerivation;
import harpoon.IR.Tree.TreeKind;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.UNOP;
import harpoon.IR.Tree.Uop;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Default;
import harpoon.Util.Collections.Environment;
import harpoon.Util.Collections.HashEnvironment;
import harpoon.Util.Util;
import harpoon.Util.Collections.WorkSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>CacheEquivalence</code> creates tag-check equivalence classes
 * for MEM operations in a Tree.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CacheEquivalence.java,v 1.3.2.3 2002-03-14 19:55:08 cananian Exp $
 */
public class CacheEquivalence {
    private static final boolean DEBUG=false;
    private static final int CACHE_LINE_SIZE = 32; /* bytes */

    /** Creates a <code>CacheEquivalence</code>. */
    public CacheEquivalence(harpoon.IR.Tree.Code code, ClassHierarchy ch) {
	CFGrapher<Tree> cfg = code.getGrapher();
	UseDefer<Tree> udr = code.getUseDefer();
	TreeDerivation td = code.getTreeDerivation();
	/* new analysis */
	final AlignmentAnalysis df = new AlignmentAnalysis(code, cfg, udr, td);
	/*- construct cache eq -*/
	new TagDominate(code, cfg, td, ch, df);
	/* debugging information dump */
	if (DEBUG) {
	    harpoon.IR.Tree.Print.print(new java.io.PrintWriter(System.out),
					code, new PrintCallback() {
		public void printAfter(java.io.PrintWriter pw,
				       HCodeElement hce) {
		    if (hce instanceof Exp) {
			Exp e = (Exp) hce;
			Tree t = e;
			while (!(t instanceof Stm))
			    t = t.getParent();
			pw.print(" [VAL: "+df.valueOf(e, (Stm)t)+"]");
		    }
		    if (hce instanceof MEM) {
			pw.print(" [CQ: "+cache_equiv.get(hce)+"]");
		    }
		}
	    });
	}
    }
    /* -------- cache equivalence pass ------- */
    private class TagDominate {
	final ClassHierarchy ch;
	final TreeDerivation td;
	final AlignmentAnalysis df;
	final DomTree dt;
	final TreeBuilder tb;
	TagDominate(Code c, CFGrapher cfg, TreeDerivation td,
		    ClassHierarchy ch,  AlignmentAnalysis df) {
	    this.ch = ch;
	    this.td = td;
	    this.df = df;
	    this.tb = c.getFrame().getRuntime().getTreeBuilder();
	    this.dt = new DomTree(c, cfg, false);

	    final Environment e = new HashEnvironment();
	    HCodeElement[] roots = dt.roots();
	    for (int i=0; i<roots.length; i++)
		traverseDT((Stm) roots[i], e);
	}
	/* analyze stms travelling down the dominator tree */
	void traverseDT(Stm stm, Environment e) {
	    /* save environment */
	    Environment.Mark mark = e.getMark();
	    /* do analysis */
	    analyze(stm, e);
	    /* recurse */
	    HCodeElement[] child = dt.children(stm);
	    for (int i=0; i<child.length; i++)
		traverseDT((Stm)child[i], e);
	    /* restore environment */
	    e.undoToMark(mark);
	    /* done! */
	    return;
	}
	/* analyze one statement in the environment defined by map m */
	void analyze(Stm stm, Map pre) {
	    Map post = new HashMap();
	    /* first look for all *reads* */
	    /* There is NO ORDER defined for any of these. */
	    for (ExpList el=stm.kids(); el!=null; el=el.tail) {
		add(stm, el.head, pre, post, true);
	    }
	    /* now all post mappings get added to pre */
	    pre.putAll(post); post.clear();
	    /* now look for writes (which must happen after reads) */
	    if (stm.kind()==TreeKind.MOVE)
		add(stm, ((MOVE)stm).getDst(), pre, post, false);
	    pre.putAll(post); post.clear();
	}
	void add(Stm root, Exp e, Map pre, Map post, boolean recurse) {
	    if (e.kind()==TreeKind.MEM) {
		MEM mem = (MEM) e;  Exp memexp = mem.getExp();
		AlignmentAnalysis.Value v = df.valueOf(memexp, root);
		// cases:
		//  1a) known base & known offset.
		//  1b) known base & offset mod N, in a kgroup.
		//      (N mod CACHE_LINE_SIZE must be zero)
		//  1c) known base & offset mod N, in a kgroup.
		//      (where CACHE_LINE_SIZE mod N must be zero)
		//  2) known base & unknown offset, but object is smaller
		//     than cache line size.
		//  3) all others.
		AlignmentAnalysis.DefPoint dp = null;
		AlignmentAnalysis.KGroup kgroup = null;
		long line=0; long modulus=0;
		if (v.isBaseKnown() &&
		    ((AlignmentAnalysis.BaseAndOffset)v).def.isWellTyped()) {
		    AlignmentAnalysis.BaseAndOffset bao = (AlignmentAnalysis.BaseAndOffset) v;
		    if (maxObjSize(bao.def.type()) <= CACHE_LINE_SIZE
			// arrays can't count as small because
			// length is not statically known.
			&& !bao.def.type().isArray()) {
			/* case 2 */
			assert (bao.offset instanceof AlignmentAnalysis.Constant ?
				    ((AlignmentAnalysis.Constant)bao.offset)
				    .number < CACHE_LINE_SIZE : true);
			dp = bao.def; line = 0; // case 2
		    } else if (bao.offset instanceof AlignmentAnalysis.Constant) {
			/* case 1a */
			AlignmentAnalysis.Constant c = (AlignmentAnalysis.Constant) bao.offset;
			dp = bao.def; line = c.number/CACHE_LINE_SIZE;
		    } else if (bao.offset instanceof AlignmentAnalysis.ConstantModuloN){
			AlignmentAnalysis.ConstantModuloN cmn =
			    (AlignmentAnalysis.ConstantModuloN) bao.offset;
			if (cmn.kgroup!=null) {
			    if (0==(cmn.modulus % CACHE_LINE_SIZE)) {
				/* case 1b */
				dp = bao.def; kgroup=cmn.kgroup;
				line = cmn.number / CACHE_LINE_SIZE;
				modulus = cmn.modulus;
			    } else if (0==(CACHE_LINE_SIZE % cmn.modulus)) {
				/* case 1c */
				dp = bao.def; kgroup=cmn.kgroup;
				// note that we can't guarantee that
				// k*modulus is on a cache line boundary.
				line = cmn.number / cmn.modulus;
				modulus = cmn.modulus;
			    }
			}
		    }
		}
		if (dp!=null) { // cases 1abc and 2
		    List key = Arrays.asList(new Object[] {
			dp, new Long(line), new Long(modulus), kgroup
		    });
		    CacheEquivSet ces = (CacheEquivSet) pre.get(key);
		    if (ces==null) ces = new CacheEquivSet(mem);
		    else ces.others.add(mem);
		    cache_equiv.put(mem, ces);
		    post.put(key, ces);
		} else { // case 3
		    cache_equiv.put(mem, new CacheEquivSet(mem));
		}
	    }
	    if (recurse)
		for (Tree tp=e.getFirstChild(); tp!=null; tp=tp.getSibling())
		    add(root, (Exp)tp, pre, post, recurse);
	}
	// returns the size of an object of the specified class.
	int objSize(HClass hc) { return tb.headerSize(hc)+tb.objectSize(hc); }
	// returns the maximum size of an object of the specified type.
	// this must account for any subclasses of the type, which likely
	// are larger than it is.
	int maxObjSize(HClass hc) {
	    if (!sizeCache.containsKey(hc)) {
		// compute maximum object size recursively.
		int size = objSize(hc);
		for (Iterator<HClass> it=ch.children(hc).iterator();
		     it.hasNext(); )
		    size = Math.max(size, maxObjSize(it.next()));
		sizeCache.put(hc, new Integer(size));
	    }
	    return sizeCache.get(hc).intValue();
	}
	// keep an object size cache for speed.
	private final Map<HClass,Integer> sizeCache =
	    new HashMap<HClass,Integer>();
    }

    /*----------------------------------------------------------------*/

    /** defines the properties of cache-equivalence sets */
    static class CacheEquivSet {
	public final MEM first;
	public final Set others = new HashSet();
	public CacheEquivSet(MEM mem) { this.first = mem; }
	public String toString() {
	    return "<TAG DEF:"+first+"; USE:"+others+">";
	}
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
