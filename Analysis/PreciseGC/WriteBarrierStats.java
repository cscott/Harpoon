// WriteBarrierStats.java, created Wed Aug 29 16:44:59 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.Code;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.METHOD;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.Print;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.TreeKind;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * <code>WriteBarrierStats</code> emits data needed for gathering
 * write barrier statistics. Must be run before
 * <code>WriteBarrierTreePass</code> to have any effect.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: WriteBarrierStats.java,v 1.1.2.1 2001-08-30 23:08:15 kkz Exp $
 */
public class WriteBarrierStats {
    
    private final CachingCodeFactory ccf;
    private final HashMap m;
    private final HashMap cleared;
    private final CountBarrierVisitor tv;

    /** Creates a <code>WriteBarrierStats</code>, and
     *  performs conversion on all callable methods.
     */
    protected WriteBarrierStats(Frame f, HCodeFactory parent,
				ClassHierarchy ch, 
				HMethod arraySC,
				HMethod fieldSC) {
	this.ccf = new CachingCodeFactory(parent);
	this.m = new HashMap();
	this.cleared = new HashMap();
	NameMap nm = f.getRuntime().getNameMap();
	this.tv = new CountBarrierVisitor(nm.label(arraySC), 
					  nm.label(fieldSC));
	// push all methods through
	for(Iterator it = ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    convertOne(ccf, tv, m, hm);
	}
    }

    protected HCodeFactory codeFactory() {
	return new HCodeFactory() {
	    public HCode convert(HMethod hm) {
		Code c = (Code) ccf.convert(hm);
		// push through conversion if not yet converted
		if (m.get(hm) == null) {
		    convertOne(ccf, tv, m, hm);
		    // if cleared, fix up count
		    Integer val = (Integer)cleared.remove(hm);
		    if (val != null) tv.count -= val.intValue();
		}
		return c;
	    }
	    public String getCodeName() { return ccf.getCodeName(); }
	    public void clear(HMethod hm) { 
		ccf.clear(hm);
		// keep track of cleared
		Integer val = (Integer)m.remove(hm);
		if (val != null) cleared.put(hm, val);
	    }
	};
    }

    protected int getCount() {
	return tv.count;
    }

    // performs a single conversion
    private static void convertOne(CachingCodeFactory ccf, 
				   CountBarrierVisitor tv,
				   Map m, HMethod hm) {
	Code c = (Code)ccf.convert(hm);
	if (c != null) {
	    // grab the count before this method
	    int base = tv.count;
	    Object[] elements = c.getGrapher().getElements(c).toArray();
	    for(int i = 0; i < elements.length; i++)
		((Tree)elements[i]).accept(tv);
	    Object o = m.put(hm, new Integer(tv.count - base)); 
	    Util.assert(o == null);
	}	
    }

    /** Code factory for applying <code>WriteBarrierStats</code>
     *  to a tree.  Clones the tree before doing transformation 
     *  in-place. */
    /*
      static HCodeFactory codeFactory(final Frame f,
				    final HCodeFactory parent, 
				    final ClassHierarchy ch,
				    final HMethod main,
				    final HMethod arraySC,
				    final HMethod fieldSC) {
	final HCodeFactory ccf = new CachingCodeFactory(parent);
	return new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode hc = ccf.convert(m);
		if (m.equals(main)) {
		    Util.assert(hc != null);
		    // first get Labels corresponding to the
		    // HMethods in which we are interested
		    NameMap nm = f.getRuntime().getNameMap();
		    Label LarraySC = nm.label(arraySC);
		    Label LfieldSC = nm.label(fieldSC);
		    CountBarrierVisitor tv = new CountBarrierVisitor
			(LarraySC, LfieldSC);
		    // push all methods through
		    for(Iterator it = ch.callableMethods().iterator(); 
			it.hasNext(); ) {
			Code c = (Code)ccf.convert((HMethod) it.next());
			if (c != null) {
			    Object[] elements = 
				c.getGrapher().getElements(c).toArray();
			    for(int j = 0; j < elements.length; j++)
				((Tree)elements[j]).accept(tv);
			}
		    }
		    harpoon.IR.Tree.Code code = (harpoon.IR.Tree.Code) hc;
		    // clone code...
		    code = (harpoon.IR.Tree.Code) code.clone(m).hcode();
		    DerivationGenerator dg = null;
		    try {
			dg = (DerivationGenerator) code.getTreeDerivation();
		    } catch (ClassCastException ex) { }
		    // ...do analysis and modify cloned code in-place.
		    simplify((Stm)code.getRootElement(), dg, 
			     HCE_RULES(tv.count));
		    hc = code;
		}
		return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	};
	}
    */
    
    // examine Trees
    private static class CountBarrierVisitor extends TreeVisitor {
	final private Label LarraySC;
	final private Label LfieldSC;
	int count = 0;

	CountBarrierVisitor(Label LarraySC, Label LfieldSC) {
	    this.LarraySC = LarraySC;
	    this.LfieldSC = LfieldSC;
	}

	public void visit (Tree t) { }

	// only CALLs are relevant
	public void visit (CALL c) {
	    if (c.getFunc().kind() == TreeKind.NAME) {
		NAME n = (NAME) c.getFunc();
		if (n.label.equals(LarraySC) || n.label.equals(LfieldSC)) {
		    // get last argument
		    ExpList arg = c.getArgs().tail.tail.tail;
		    Util.assert(arg.tail == null);
		    c.setArgs(ExpList.replace(c.getArgs(), arg.head, new CONST
					      (c.getFactory(), c, count++)));
		    System.out.print('.');
		}
	    }
	}
    }
}
