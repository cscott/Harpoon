// DataClaz.java, created Mon Oct 11 12:01:55 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.FieldMap;
import harpoon.Backend.Maps.MethodMap;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HDataElement;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HInitializer;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.SEGMENT;
import harpoon.Util.HClassUtil;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Set;
/**
 * <code>DataClaz</code> lays out the claz tables, including the
 * interface and class method dispatch tables.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DataClaz.java,v 1.1.4.26 2001-06-27 02:36:41 kkz Exp $
 */
public class DataClaz extends Data {
    final TreeBuilder m_tb;
    final NameMap m_nm;
    
    /** Creates a <code>ClassData</code>. */
    public DataClaz(Frame f, HClass hc, ClassHierarchy ch) {
        super("class-data", hc, f);
	this.m_nm = f.getRuntime().nameMap;
	this.m_tb = (TreeBuilder) f.getRuntime().treeBuilder;
	this.BITS_IN_GC_BITMAP = 8 * m_tb.POINTER_SIZE;
	this.root = build(f, hc, ch);
    }

    private HDataElement build(Frame f, HClass hc, ClassHierarchy ch) {
	List stmlist = new ArrayList();
	// write the appropriate segment header
	stmlist.add(new SEGMENT(tf, null, SEGMENT.CLASS));
	// align things on word boundary.
	stmlist.add(new ALIGN(tf, null, 4));
	// first comes the interface method table.
	if (!hc.isInterface()) {
	    Stm s = interfaceMethods(hc, ch);
	    if (s!=null) stmlist.add(s);
	}
	// this is where the class pointer points.
	stmlist.add(new LABEL(tf, null, m_nm.label(hc), true));
	// class info points at a class object for this class
	// (which is generated in DataReflection1)
	stmlist.add(_DATUM(m_nm.label(hc, "classobj")));
	// component type pointer.
	if (hc.isArray())
	    stmlist.add(_DATUM(m_nm.label(hc.getComponentType())));
	else
	    stmlist.add(_DATUM(new CONST(tf, null)));
	// the interface list is generated elsewhere
	stmlist.add(_DATUM(m_nm.label(hc, "interfaces")));
	// object size.
	int size = 
	    (!hc.isPrimitive()) ? 
	    (m_tb.objectSize(hc) + m_tb.OBJECT_HEADER_SIZE) :
	    (hc==HClass.Double||hc==HClass.Long) ? m_tb.LONG_WORD_SIZE :
	    (hc==HClass.Int||hc==HClass.Float) ? m_tb.WORD_SIZE :
	    (hc==HClass.Short||hc==HClass.Char) ? 2 : 1;
	stmlist.add(_DATUM(new CONST(tf, null, size)));
	// bitmap for gc or pointer to bitmap
      	stmlist.add(gc(f, ch));
	// class depth.
	int depth = m_tb.cdm.classDepth(hc);
	stmlist.add(_DATUM(new CONST(tf, null, m_tb.POINTER_SIZE * depth)));
	// class display
	stmlist.add(display(hc, ch));
	// now class method table.
	if (!hc.isInterface()) {
	    Stm s = classMethods(hc, ch);
	    if (s!=null) stmlist.add(s);
	}
	return (HDataElement) Stm.toStm(stmlist);
    }

    // the number of bits in the in-line gc bitmap is platform-dependent
    final int BITS_IN_GC_BITMAP;

    /** Make gc bitmap or pointer to bitmap. */
    private Stm gc(Frame f, ClassHierarchy ch) {
	// use object size (w/o header) to determine how many bits we need (round up)
	int bitsNeeded = (m_tb.objectSize(hc) + m_tb.POINTER_SIZE - 1)/m_tb.POINTER_SIZE;
	// for arrays we keep an extra bit for the array elements
	if (hc.isArray())
	    bitsNeeded++;
	if (bitsNeeded > BITS_IN_GC_BITMAP) { // auxiliary table for large objects
	    return gcaux(f, ch, bitsNeeded);
	} else { // in-line bitmap for small objects
	    final List stmlist = new ArrayList();
	    final List fields = m_tb.cfm.fieldList(hc);
	    long bitmap = 0;
	    for (Iterator it=fields.iterator(); it.hasNext(); ) {
		final HField hf = (HField)it.next();
		final HClass type = hf.getType();
		final int fieldOffset = m_tb.cfm.fieldOffset(hf);
		// non-aligned objects should never be pointers
		if (fieldOffset%m_tb.POINTER_SIZE != 0) {
		    Util.assert(type.isPrimitive());
		    continue;
		}
		if (!type.isPrimitive()) {
		    final int i = fieldOffset/m_tb.POINTER_SIZE;
		    Util.assert(i >= 0 && i < BITS_IN_GC_BITMAP);
		    bitmap |= (1 << i);
		}
	    }
	    // for arrays, we use the bit at the end of the bitmap
	    // for the array elements so the GC doesn't have to look 
	    // into the component claz
	    if (hc.isArray() && !hc.getComponentType().isPrimitive())
		bitmap |= (1 << (bitsNeeded - 1));
	    // write out in-line bitmap
	    if (f.pointersAreLong()) {
		//System.out.println("Compact: " + Long.toBinaryString(bitmap));
		stmlist.add(_DATUM(new CONST(tf, null, bitmap)));
	    } else {
		stmlist.add(_DATUM(new CONST(tf, null, (int)bitmap)));
		//System.out.println("Compact: " + 
		//		   Integer.toBinaryString((int)bitmap));
	    }
	    return Stm.toStm(stmlist);
	}
    }
    // Make auxiliary gc bitmap
    private Stm gcaux(Frame f, ClassHierarchy ch, int bitsNeeded) {
	final List stmlist = new ArrayList();
	// large object, encoded in auxiliary table
	stmlist.add(_DATUM(m_nm.label(hc, "gc_aux")));
	// switch to GC segment
	stmlist.add(new SEGMENT(tf, null, SEGMENT.GC));
	// align things on word boundary.
	stmlist.add(new ALIGN(tf, null, 4));
	stmlist.add(new LABEL(tf, null, m_nm.label(hc, "gc_aux"), true));
	final List fields = m_tb.cfm.fieldList(hc);
	long bitmap = 0;
	int numBitmaps = 0; // keep track of number of bitmaps created
	for (Iterator it = fields.iterator(); it.hasNext(); ) {
	    HField hf = (HField)it.next();
	    final int fo = m_tb.cfm.fieldOffset(hf);
	    final int bitPosition = fo/m_tb.POINTER_SIZE;
	    Util.assert(numBitmaps <= bitPosition/BITS_IN_GC_BITMAP);
	    final HClass type = hf.getType();
	    // unaligned fields should never contain pointers
	    if (fo%m_tb.POINTER_SIZE != 0)  {
		Util.assert(type.isPrimitive());
		continue;
	    }
	    // write out completed bitmaps
	    while(numBitmaps < bitPosition/BITS_IN_GC_BITMAP) {
		if (f.pointersAreLong()) {
		    //System.out.println("Aux: " + 
		    //		       Long.toBinaryString(bitmap));
		    stmlist.add(_DATUM(new CONST(tf, null, bitmap)));
		} else {
		    //System.out.println("Aux: " + 
		    //		       Integer.toBinaryString((int)bitmap));
		    stmlist.add(_DATUM(new CONST(tf, null, (int)bitmap)));
		}
		bitmap = 0; numBitmaps++; // clear bitmap
	    }
	    if (!type.isPrimitive()) {
		final int bp = bitPosition%BITS_IN_GC_BITMAP;
		Util.assert(bp >= 0 && bp < BITS_IN_GC_BITMAP);
		bitmap |= (1 << bp);
	    }
	}
	// write out remaining bitmaps
	final int bitmapsNeeded = (bitsNeeded + BITS_IN_GC_BITMAP - 1)/BITS_IN_GC_BITMAP;
	while(numBitmaps < bitmapsNeeded) {
	    // use last bit of bitmap for array elements
	    if ((numBitmaps == (bitmapsNeeded - 1)) && hc.isArray() && 
		!hc.getComponentType().isPrimitive())
		bitmap |= (1 << ((bitsNeeded - 1)%BITS_IN_GC_BITMAP));		
	    if (f.pointersAreLong()) {
		//System.out.println("Aux: " + 
		//		   Long.toBinaryString(bitmap));
		stmlist.add(_DATUM(new CONST(tf, null, bitmap)));
	    } else {
		//System.out.println("Aux: " + 
		//		   Integer.toBinaryString((int)bitmap));
		stmlist.add(_DATUM(new CONST(tf, null, (int)bitmap)));
	    }
	    bitmap = 0; numBitmaps++; // clear bitmap
	}
	// switch back to CLASS segment
	stmlist.add(new SEGMENT(tf, null, SEGMENT.CLASS));
	// align things on word boundary.
	stmlist.add(new ALIGN(tf, null, 4));
	return Stm.toStm(stmlist);
    }

    /** Make class display table. */
    private Stm display(HClass hc, ClassHierarchy ch) {
	List clslist = new ArrayList();
	// we're going to build the list top-down and then reverse it.
	if (hc.isArray()) { // arrays are special.
	    HClass base = HClassUtil.baseClass(hc);
	    int dims = HClassUtil.dims(hc);
	    // (interface arrays inherit from Object[]..[], but
	    //  the interface array itself shouldn't appear in the display)
	    if (base.isInterface()) base=linker.forName("java.lang.Object");
	    
	    // first step down the base class inheritance hierarchy.
	    for (HClass hcp = base; hcp!=null; hcp=hcp.getSuperclass())
		clslist.add(HClassUtil.arrayClass(linker, hcp, dims));
	    // now down the Object array hierarchy.
	    HClass hcO = linker.forName("java.lang.Object");
	    for (dims--; dims>=0; dims--)
		clslist.add(HClassUtil.arrayClass(linker, hcO, dims));
	    // done.
	} else if (!hc.isInterface() && !hc.isPrimitive()) {
	    // step down the inheritance hierarchy.
	    for (HClass hcp = hc; hcp!=null; hcp=hcp.getSuperclass())
		clslist.add(hcp);
	}
	// now reverse list.
	Collections.reverse(clslist);
	// okay, root should always be java.lang.Object.
	Util.assert(hc.isInterface() || hc.isPrimitive() ||
		    clslist.get(0)==linker.forName("java.lang.Object"));
	// make statements.
	List stmlist = new ArrayList(m_tb.cdm.maxDepth()+1);
	for (Iterator it=clslist.iterator(); it.hasNext(); )
	    stmlist.add(_DATUM(m_nm.label((HClass)it.next())));
	while (stmlist.size() <= m_tb.cdm.maxDepth())
	    stmlist.add(_DATUM(new CONST(tf, null))); // pad with nulls.
	Util.assert(stmlist.size() == m_tb.cdm.maxDepth()+1);
	return Stm.toStm(stmlist);
    }
    /** Make class methods table. */
    private Stm classMethods(HClass hc, ClassHierarchy ch) {
	// collect all the methods.
	List methods = new ArrayList(Arrays.asList(hc.getMethods()));
	// weed out non-virtual methods.
	for (Iterator it=methods.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    if (hm.isStatic() || hm instanceof HConstructor ||
		Modifier.isPrivate(hm.getModifiers()))
		it.remove();
	}
	// sort the methods using class method map.
	final MethodMap cmm = m_tb.cmm;
	Collections.sort(methods, new Comparator() {
	    public int compare(Object o1, Object o2) {
		HMethod hm1 = (HMethod) o1, hm2 = (HMethod) o2;
		int i1 = cmm.methodOrder(hm1);
		int i2 = cmm.methodOrder(hm2);
		return i1 - i2;
	    }
	});
	// make stms.
	List stmlist = new ArrayList(methods.size());
	Set callable = ch.callableMethods();
	int order=0;
	for (Iterator it=methods.iterator(); it.hasNext(); order++) {
	    HMethod hm = (HMethod) it.next();
	    Util.assert(cmm.methodOrder(hm)==order); // should be no gaps.
	    if (callable.contains(hm) &&
		!Modifier.isAbstract(hm.getModifiers()))
		stmlist.add(_DATUM(m_nm.label(hm)));
	    else
		stmlist.add(_DATUM(new CONST(tf, null))); // null pointer
	}
	return Stm.toStm(stmlist);
    }
    /* XXX UGLY UGLY: some bug in the relinker makes methods comparisons
     * bogus sometimes.  This is a hack to work around the problem so that
     * we can benchmark properly: we should really fix the relinker.
     * Even the property we're using here is stolen from ClassFile.Loader,
     * where it specifies that Linkers should be re-serialized as Relinkers,
     * another hack designed to avoid needing to run Alex's analysis using
     * a (historically buggy) relinker. CSA. */
    private static final boolean relinkerHack =
	System.getProperty("harpoon.relinker.hack", "no")
	.equalsIgnoreCase("yes");
    /** Make interface methods table. */
    private Stm interfaceMethods(HClass hc, ClassHierarchy ch) {
	// collect all interfaces implemented by this class
	Set interfaces = new HashSet();
	for (HClass hcp=hc; hcp!=null; hcp=hcp.getSuperclass())
	    interfaces.addAll(Arrays.asList(hcp.getInterfaces()));
	if (!relinkerHack) // XXX EVIL: see above.
	    interfaces.retainAll(ch.classes());
	// all methods included in these interfaces.
	Set methods = new HashSet();
	for (Iterator it=interfaces.iterator(); it.hasNext(); )
	    methods.addAll(Arrays.asList(((HClass)it.next()).getMethods()));
	if (!relinkerHack) // XXX EVIL: see above.
	    methods.retainAll(ch.callableMethods());
	// double-check that these are all interface methods
	// (also discard class initializers from the list)
	// (also discard inherited methods of java.lang.Object)
	for (Iterator it=methods.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    if (hm instanceof HInitializer) it.remove();
	    else if (hm.getDeclaringClass().getName()
		     .equals("java.lang.Object")) it.remove();
	    else Util.assert(hm.isInterfaceMethod());
	}
	// remove duplicates (two methods with same signature)
	Set sigs = new HashSet();
	for (Iterator it=methods.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    String sig = hm.getName() + hm.getDescriptor();
	    if (sigs.contains(sig)) it.remove();
	    else sigs.add(sig);
	}
	// remove methods which are not actually callable in this class.
	for (Iterator it=methods.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    HMethod cm = hc.getMethod(hm.getName(), hm.getDescriptor());
	    if (!ch.callableMethods().contains(cm) ||
		Modifier.isAbstract(cm.getModifiers()))
		it.remove();
	}
	// okay, now sort by InterfaceMethodMap ordering.
	final MethodMap imm = m_tb.imm;
	List ordered = new ArrayList(methods);
	Collections.sort(ordered, new Comparator() {
	    public int compare(Object o1, Object o2) {
		HMethod hm1 = (HMethod) o1, hm2 = (HMethod) o2;
		int i1 = imm.methodOrder(hm1);
		int i2 = imm.methodOrder(hm2);
		return i1 - i2;
	    }
	});
	// okay, output in reverse order:
	List stmlist = new ArrayList(ordered.size());
	int last_order = -1;
	for (ListIterator it=ordered.listIterator(ordered.size());
	     it.hasPrevious(); ) {
	    HMethod hm = (HMethod) it.previous();
	    int this_order = imm.methodOrder(hm);
	    if (last_order!=-1) {
		Util.assert(this_order < last_order); // else not ordered
		for (int i=last_order-1; i > this_order; i--)
		    stmlist.add(_DATUM(new CONST(tf, null))); // null
	    }
	    // look up name of class method with this signature
	    HMethod cm = hc.getMethod(hm.getName(), hm.getDescriptor());
	    // add entry for this method to table.
	    stmlist.add(_DATUM(m_nm.label(cm)));
	    last_order = this_order;
	}
	if (last_order!=-1) {
	    Util.assert(last_order>=0);
	    for (int i=last_order; i > 0; i--)
		stmlist.add(_DATUM(new CONST(tf, null)));
	}
	// done!
	return Stm.toStm(stmlist);
    }
}
