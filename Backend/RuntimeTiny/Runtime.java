// Runtime.java, created Wed Sep  8 14:30:28 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.RuntimeTiny;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.CallGraph;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.ClassDepthMap;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HData;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Util.Util;

import harpoon.Backend.Runtime1.AllocationStrategy;
import harpoon.Backend.Runtime1.ObjectBuilder.RootOracle;

import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * <code>RuntimeTiny.Runtime</code> is a size-optimized version of the
 * FLEX backend.  It inherits most of the implementation of Runtime1,
 * but uses indices rather than direct pointers to compress the claz
 * field and (will) support byte- and bit-aligned (i.e. "unaligned")
 * fields in object layouts.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Runtime.java,v 1.1.2.2 2002-03-11 21:18:02 cananian Exp $
 */
public class Runtime extends harpoon.Backend.Runtime1.Runtime {
    // options.
    /* turning this on reduces the size of the claz pointer (by using an
     * index) at the expense of an extra dereference every time the claz is
     * used. */
    protected static final boolean clazShrink =
	!Boolean.getBoolean("harpoon.runtime1.no-claz-shrink");

    // local fields
    protected ClazNumbering cn;

    public Runtime(Frame frame, AllocationStrategy as,
		   HMethod main, boolean prependUnderscore) {
	this(frame, as, main, prependUnderscore, null);
    }

    public Runtime(Frame frame, AllocationStrategy as,
		   HMethod main, 
		   boolean prependUnderscore, RootOracle rootOracle) {
	super(frame,as,main,prependUnderscore,rootOracle);
	if (clazShrink)
	    configurationSet.add("check_with_claz_shrink_needed");
    }
    protected ObjectBuilder initObjectBuilder(RootOracle ro) {
	if (ro==null)
	    return new harpoon.Backend.RuntimeTiny.ObjectBuilder(this);
	return new harpoon.Backend.RuntimeTiny.ObjectBuilder(this, ro);
    }
    protected harpoon.Backend.RuntimeTiny.TreeBuilder initTreeBuilder() {
	return new harpoon.Backend.RuntimeTiny.TreeBuilder
	    (this, frame.getLinker(), as, frame.pointersAreLong());
    }
    public void setClassHierarchy(ClassHierarchy ch) {
	super.setClassHierarchy(ch);
	// do class numbering w/ this classhierarchy
	this.cn = new CompleteClazNumbering(ch);
    }
    public List<HData> classData(HClass hc) {
	List<HData> r = new ArrayList<HData>(super.classData(hc));
	r.add(new DataClazTable(frame,hc,ch,cn));
	return r;
    }
    // add new field to DataClaz:
    protected ExtraClazInfo getExtraClazInfo() {
	final ExtraClazInfo eci = super.getExtraClazInfo();
	if (!clazShrink) return eci;
	return new ExtraClazInfo() {
		public int fields_size() {
		    return
			// first the superclass' fields.
			eci.fields_size() +
			// now ours.
			4 /*getTreeBuilder().WORD_SIZE*/;
		}
		public Stm emit(TreeFactory tf, Frame f, HClass hc,
				ClassHierarchy ch) {
		    List<Stm> stmlist = new ArrayList<Stm>();
		    // first the superclass' fields.
		    Stm s = eci.emit(tf,f,hc,ch);
		    if (s!=null) stmlist.add(s);
		    // now ours.
		    int num = cn.clazNumber(hc);// good thing this is complete!
		    stmlist.add(new DATUM(tf, null, new CONST(tf, null, num)));
		    // ta-da!
		    return Stm.toStm(stmlist);
		}
	    };
    }
}