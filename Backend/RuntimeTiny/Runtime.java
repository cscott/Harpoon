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
 * @version $Id: Runtime.java,v 1.1.2.6 2002-03-21 15:09:55 cananian Exp $
 */
public class Runtime extends harpoon.Backend.Runtime1.Runtime {
    // options.
    /* turning this on reduces the size of the claz pointer (by using an
     * index) at the expense of an extra dereference every time the claz is
     * used. */
    protected static final boolean clazShrink =
	!Boolean.getBoolean("harpoon.runtime.tiny.no-claz-shrink");
    protected static final boolean hashlockShrink =
	!Boolean.getBoolean("harpoon.runtime.tiny.no-hashlock-shrink");
    protected static final boolean byteAlign =
	System.getProperty("harpoon.runtime.tiny.field-align","byte")
	.equalsIgnoreCase("byte");
    protected final static boolean fixAlign =
	Boolean.getBoolean("harpoon.runtime.tiny.fix-align");
    static {
	// report the runtime settings, just to double-check w/ the user.
	System.out.print("TINY RUNTIME: ");
	if (clazShrink) System.out.print("[CLAZ-SHRINK] ");
	if (hashlockShrink) System.out.print("[HASH-SHRINK] ");
	if (byteAlign) System.out.print("[BYTE-ALIGN] ");
	if (fixAlign) System.out.print("[FIX-ALIGN] ");
	System.out.println();
    }

    // local fields
    protected ClazNumbering cn;
    int clazBytes;

    public Runtime(Frame frame, AllocationStrategy as,
		   HMethod main, boolean prependUnderscore) {
	this(frame, as, main, prependUnderscore, null);
    }

    public Runtime(Frame frame, AllocationStrategy as,
		   HMethod main, 
		   boolean prependUnderscore, RootOracle rootOracle) {
	super(frame,as,main,prependUnderscore,rootOracle);
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
	// do class numbering w/ this classhierarchy
	this.cn = new CompleteClazNumbering(ch);
	this.clazBytes = !clazShrink ? 4 :
	    (Util.log2c(ch.instantiatedClasses().size())+7)/8;
	// reset treebuilder, etc.
	super.setClassHierarchy(ch);
    }
    // we're going to hack in our own codefactory in w/ the
    // nativetreecodefactory.  beware: tree is not yet canonicalized!
    public HCodeFactory nativeTreeCodeFactory(final HCodeFactory hcf) {
	HCodeFactory parent = super.nativeTreeCodeFactory(hcf);
	if (!byteAlign) return parent;
	if (!fixAlign) return parent;
	return FixUnaligned.codeFactory
	    (harpoon.IR.Tree.CanonicalTreeCode.codeFactory(parent, frame));
    }

    public List<HData> classData(HClass hc) {
	// assume classhierarchy is frozen by time this is called.
	if (clazShrink)
	    configurationSet.add
		("check_with_claz_shrink_should_be_"+clazBytes);
	else
	    configurationSet.add
		("check_with_claz_shrink_not_needed");
	configurationSet.add
	    (hashlockShrink ? "check_with_hashlock_shrink_needed" :
	     "check_with_hashlock_shrink_not_needed");
	// end configset
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
