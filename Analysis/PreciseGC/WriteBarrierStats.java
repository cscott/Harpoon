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

import java.io.PrintStream;

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
 * @version $Id: WriteBarrierStats.java,v 1.4 2002-04-10 03:00:53 cananian Exp $
 */
public class WriteBarrierStats {
    
    private final CachingCodeFactory ccf;
    private final HashMap m;
    private final HashMap cleared;
    private final CountBarrierVisitor tv;
    private final PrintStream out;

    /** Creates a <code>WriteBarrierStats</code>, and
     *  performs conversion on all callable methods.
     */
    protected WriteBarrierStats(Frame f, HCodeFactory parent,
				ClassHierarchy ch, 
				HMethod arraySC,
				HMethod fieldSC,
				PrintStream out) {
	this.ccf = new CachingCodeFactory(parent);
	this.m = new HashMap();
	this.cleared = new HashMap();
	this.out = out;
	NameMap nm = f.getRuntime().getNameMap();
	this.tv = new CountBarrierVisitor(nm.label(arraySC), 
					  nm.label(fieldSC), out);
	// push all methods through
	for(Iterator it = ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    convertOne(ccf, tv, m, hm, out);
	}
    }

    protected HCodeFactory codeFactory() {
	return new HCodeFactory() {
	    public HCode convert(HMethod hm) {
		Code c = (Code) ccf.convert(hm);
		// push through conversion if not yet converted
		if (m.get(hm) == null) {
		    convertOne(ccf, tv, m, hm, out);
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
				   Map m, HMethod hm,
				   PrintStream out) {
	Code c = (Code)ccf.convert(hm);
	if (c != null) {
	    // grab the count before this method
	    int base = tv.count;
	    Object[] elements = c.getGrapher().getElements(c).toArray();
	    for(int i = 0; i < elements.length; i++)
		((Tree)elements[i]).accept(tv);
	    Object o = m.put(hm, new Integer(tv.count - base)); 
	    assert o == null;
	    if (out != null && base != tv.count) {
		out.println(hm.getDeclaringClass().getName()+"."+
			    hm.getName()+hm.getDescriptor());
	    }
	}	
    }
   
    // examine Trees
    private static class CountBarrierVisitor extends TreeVisitor {
	private final Label LarraySC;
	private final Label LfieldSC;
	private final PrintStream out;

	int count = 0;

	CountBarrierVisitor(Label LarraySC, Label LfieldSC, PrintStream out) {
	    this.LarraySC = LarraySC;
	    this.LfieldSC = LfieldSC;
	    this.out = out;
	}

	public void visit (Tree t) { }

	// only CALLs are relevant
	public void visit (CALL c) {
	    if (c.getFunc().kind() == TreeKind.NAME) {
		NAME n = (NAME) c.getFunc();
		if (n.label.equals(LarraySC) || n.label.equals(LfieldSC)) {
		    // get last argument
		    ExpList arg = c.getArgs().tail.tail.tail;
		    assert arg.tail == null;
		    if (out != null) {
			out.println("ID "+count+"\t"+c.getSourceFile()+
				    "\tline "+c.getLineNumber());
		    }
		    c.setArgs(ExpList.replace(c.getArgs(), arg.head, new CONST
					      (c.getFactory(), c, count++)));
		}
	    }
	}
    }
}
