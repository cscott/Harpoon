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
 * @version $Id: DataClaz.java,v 1.6 2003-10-21 02:11:02 cananian Exp $
 */
public class DataClaz extends Data {
    final TreeBuilder m_tb;
    final NameMap m_nm;
    
    /** Creates a <code>ClassData</code>. */
    public DataClaz(Frame f, HClass hc, ClassHierarchy ch,
		    Runtime.ExtraClazInfo eci) {
        super("class-data", hc, f);
	this.m_nm = f.getRuntime().getNameMap();
	this.m_tb = (TreeBuilder) f.getRuntime().getTreeBuilder();
	this.BITS_IN_GC_BITMAP = 8 * m_tb.POINTER_SIZE;
	this.root = build(f, hc, ch, eci);
    }

    private HDataElement build(Frame f, HClass hc, ClassHierarchy ch,
			       Runtime.ExtraClazInfo eci) {
	List<Stm> stmlist = new ArrayList<Stm>();
	// write the appropriate segment header
	stmlist.add(new SEGMENT(tf, null, SEGMENT.CLASS));
	// align things on pointer-size boundary.
	stmlist.add(new ALIGN(tf, null, f.pointersAreLong() ? 8 : 4));
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
	// class depth.
	int depth = m_tb.cdm.classDepth(hc);
	if (HClassUtil.baseClass(hc).isInterface()) depth=0;// mark interface[]
	stmlist.add(_DATUM(new CONST(tf, null, m_tb.POINTER_SIZE * depth)));
	// bitmap for gc or pointer to bitmap
      	stmlist.add(gc(f, hc));
	// extra claz info (defined by runtime's subclass, if any)
	if (eci!=null) {
	    Stm s = eci.emit(tf, f, hc, ch);
	    if (s!=null) stmlist.add(s);
	}
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
    private Stm gc(Frame f, HClass hc) {
	// use object size (w/ header) to determine how many bits we need (round up)
	int bitsNeeded = (m_tb.objectSize(hc) + m_tb.OBJECT_HEADER_SIZE + m_tb.POINTER_SIZE - 1)/m_tb.POINTER_SIZE;
	// for arrays we keep an extra bit for the array elements
	if (hc.isArray())
	    bitsNeeded++;
	if (bitsNeeded > BITS_IN_GC_BITMAP) { // auxiliary table for large objects
	    return gcaux(f, hc, bitsNeeded);
	} else { // in-line bitmap for small objects
	    final List<HField> fields = m_tb.cfm.fieldList(hc);
	    long bitmap = 0;
	    for (Iterator<HField> it=fields.iterator(); it.hasNext(); ) {
		final HField hf = it.next();
		final HClass type = hf.getType();
		final int fieldOffset = m_tb.cfm.fieldOffset(hf) + m_tb.OBJECT_HEADER_SIZE;
		// non-aligned objects should never be pointers
		if (fieldOffset%m_tb.POINTER_SIZE != 0) {
		    assert type.isPrimitive();
		    continue;
		}
		if (!type.isPrimitive()) {
		    final int i = fieldOffset/m_tb.POINTER_SIZE;
		    assert i >= 0 && i < BITS_IN_GC_BITMAP;
		    bitmap |= (1 << i);
		}
	    }
	    // for arrays, we use the bit at the end of the bitmap
	    // for the array elements so the GC doesn't have to look 
	    // into the component claz
	    if (hc.isArray() && !hc.getComponentType().isPrimitive())
		bitmap |= (1 << (bitsNeeded - 1));
	    // write out in-line bitmap
	    final List<Stm> stmlist = new ArrayList<Stm>();
	    if (f.pointersAreLong()) {
		stmlist.add(_DATUM(new CONST(tf, null, bitmap)));
	    } else {
		stmlist.add(_DATUM(new CONST(tf, null, (int)bitmap)));
	    }
	    return Stm.toStm(stmlist);
	}
    }
    // Make auxiliary gc bitmap
    private Stm gcaux(Frame f, HClass hc, int bitsNeeded) {
	// calculate how many bitmaps we need
	final int bitmapsNeeded = (bitsNeeded + BITS_IN_GC_BITMAP - 1)/BITS_IN_GC_BITMAP;
	// create an array containing the needed bitmaps
	final long bitmaps[] = new long[bitmapsNeeded];
	// iterate through the fields
	final List<HField> fields = m_tb.cfm.fieldList(hc);
	for (Iterator<HField> it = fields.iterator(); it.hasNext(); ) {
	    final HField hf = it.next();
	    final HClass type =  hf.getType();
	    if (!type.isPrimitive()) {
		final int fo = m_tb.cfm.fieldOffset(hf) + m_tb.OBJECT_HEADER_SIZE;
		// non-primitive fields contain pointers and should be aligned
		assert fo%m_tb.POINTER_SIZE == 0;
		// calculate the bit position corresponding to this field
		final int bp = fo/m_tb.POINTER_SIZE;
		assert bp < bitsNeeded;
		bitmaps[bp/BITS_IN_GC_BITMAP] |= (1 << (bp%BITS_IN_GC_BITMAP));
	    }
	}
	// handle arrays
	if (hc.isArray() && !hc.getComponentType().isPrimitive())
	    bitmaps[bitmapsNeeded-1] |= (1 << ((bitsNeeded - 1)%BITS_IN_GC_BITMAP));
	// check whether there are any pointers
	boolean atomic = true;
	for (int i = 0; i < bitmapsNeeded; i++) {
	    if (bitmaps[i] != 0) {
		atomic = false;
		break;
	    }
	}
	final List<Stm> stmlist = new ArrayList<Stm>();
	if (atomic) {
	    // write NULL to the in-line bitmap to indicate no pointers
	    if (f.pointersAreLong())
		stmlist.add(_DATUM(new CONST(tf, null, (long)0)));
	    else
		stmlist.add(_DATUM(new CONST(tf, null, (int)0)));
	} else {
	    // large object, encoded in auxiliary table
	    stmlist.add(_DATUM(m_nm.label(hc, "gc_aux")));
	    // switch to GC segment
	    stmlist.add(new SEGMENT(tf, null, SEGMENT.GC));
	    // align things on word boundary.
	    stmlist.add(new ALIGN(tf, null, f.pointersAreLong() ? 8 : 4));
	    stmlist.add(new LABEL(tf, null, m_nm.label(hc, "gc_aux"), true));
	    // write out the bitmaps
	    if (f.pointersAreLong()) {
		for (int i = 0; i < bitmapsNeeded; i++)
		    stmlist.add(_DATUM(new CONST(tf, null, bitmaps[i])));
	    } else {
		for (int i = 0; i < bitmapsNeeded; i++)
		    stmlist.add(_DATUM(new CONST(tf, null, (int)bitmaps[i])));
	    }
	    // switch back to CLASS segment
	    stmlist.add(new SEGMENT(tf, null, SEGMENT.CLASS));
	    // align things on word boundary.
	    stmlist.add(new ALIGN(tf, null, f.pointersAreLong() ? 8 : 4));
	}
	return Stm.toStm(stmlist);
    }

    /** Make class display table. */
    private Stm display(HClass hc, ClassHierarchy ch) {
	List<HClass> clslist = new ArrayList<HClass>();
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
	assert hc.isInterface() || hc.isPrimitive() ||
		    clslist.get(0)==linker.forName("java.lang.Object");
	// make statements.
	List<Stm> stmlist = new ArrayList<Stm>(m_tb.cdm.maxDepth()+1);
	for (Iterator<HClass> it=clslist.iterator(); it.hasNext(); )
	    stmlist.add(_DATUM(m_nm.label(it.next())));
	while (stmlist.size() <= m_tb.cdm.maxDepth())
	    stmlist.add(_DATUM(new CONST(tf, null))); // pad with nulls.
	assert stmlist.size() == m_tb.cdm.maxDepth()+1;
	return Stm.toStm(stmlist);
    }
    /** Make class methods table. */
    private Stm classMethods(HClass hc, ClassHierarchy ch) {
	// collect all the methods.
	List<HMethod> methods =
	    new ArrayList<HMethod>(Arrays.asList(hc.getMethods()));
	// weed out non-virtual methods.
	for (Iterator<HMethod> it=methods.iterator(); it.hasNext(); ) {
	    HMethod hm = it.next();
	    assert !hm.isInterfaceMethod() :
		"getMethods() on "+hc+" returned an *interface* method: "+hm+
		" / method list: "+methods;
	    if (hm.isStatic() || hm instanceof HConstructor ||
		Modifier.isPrivate(hm.getModifiers()))
		it.remove();
	}
	// sort the methods using class method map.
	final MethodMap cmm = m_tb.cmm;
	Collections.sort(methods, new Comparator<HMethod>() {
	    public int compare(HMethod hm1, HMethod hm2) {
		int i1 = cmm.methodOrder(hm1);
		int i2 = cmm.methodOrder(hm2);
		return i1 - i2;
	    }
	});
	// make stms.
	List<Stm> stmlist = new ArrayList<Stm>(methods.size());
	Set<HMethod> callable = ch.callableMethods();
	int order=0;
	for (Iterator<HMethod> it=methods.iterator(); it.hasNext(); order++) {
	    HMethod hm = it.next();
	    assert cmm.methodOrder(hm)==order; // should be no gaps.
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
	Set<HClass> interfaces = new HashSet<HClass>();
	for (HClass hcp=hc; hcp!=null; hcp=hcp.getSuperclass())
	    interfaces.addAll(Arrays.asList(hcp.getInterfaces()));
	if (!relinkerHack) // XXX EVIL: see above.
	    interfaces.retainAll(ch.classes());
	// all methods included in these interfaces.
	Set<HMethod> methods = new HashSet<HMethod>();
	for (Iterator<HClass> it=interfaces.iterator(); it.hasNext(); )
	    methods.addAll(Arrays.asList(it.next().getMethods()));
	if (!relinkerHack) // XXX EVIL: see above.
	    methods.retainAll(ch.callableMethods());
	// double-check that these are all interface methods
	// (also discard class initializers from the list)
	// (also discard inherited methods of java.lang.Object)
	for (Iterator<HMethod> it=methods.iterator(); it.hasNext(); ) {
	    HMethod hm = it.next();
	    if (hm instanceof HInitializer) it.remove();
	    else if (hm.getDeclaringClass().getName()
		     .equals("java.lang.Object")) it.remove();
	    else assert hm.isInterfaceMethod();
	}
	// remove duplicates (two methods with same signature)
	// pre-load sigs with methods of java.lang.Object to make sure
	// inherited methods of Object don't make it into the methods
	// set.
	Set<String> sigs = new HashSet<String>();
	for (Iterator<HMethod> it=Arrays.asList
		 (linker.forName("java.lang.Object").getMethods()).iterator();
	     it.hasNext(); ) {
	    HMethod hm = it.next();
	    String sig = hm.getName() + hm.getDescriptor();
	    sigs.add(sig);
	}	    
	for (Iterator<HMethod> it=methods.iterator(); it.hasNext(); ) {
	    HMethod hm = it.next();
	    String sig = hm.getName() + hm.getDescriptor();
	    if (sigs.contains(sig)) it.remove();
	    else sigs.add(sig);
	}
	// remove methods which are not actually callable in this class.
	for (Iterator<HMethod> it=methods.iterator(); it.hasNext(); ) {
	    HMethod hm = it.next();
	    HMethod cm = hc.getMethod(hm.getName(), hm.getDescriptor());
	    if (!ch.callableMethods().contains(cm) ||
		Modifier.isAbstract(cm.getModifiers()))
		it.remove();
	}
	// okay, now sort by InterfaceMethodMap ordering.
	final MethodMap imm = m_tb.imm;
	List<HMethod> ordered = new ArrayList<HMethod>(methods);
	Collections.sort(ordered, new Comparator<HMethod>() {
	    public int compare(HMethod hm1, HMethod hm2) {
		int i1 = imm.methodOrder(hm1);
		int i2 = imm.methodOrder(hm2);
		return i1 - i2;
	    }
	});
	// okay, output in reverse order:
	List<Stm> stmlist = new ArrayList<Stm>(ordered.size());
	int last_order = -1;
	for (ListIterator<HMethod> it=ordered.listIterator(ordered.size());
	     it.hasPrevious(); ) {
	    HMethod hm = it.previous();
	    int this_order = imm.methodOrder(hm);
	    if (last_order!=-1) {
		assert this_order < last_order; // else not ordered
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
	    assert last_order>=0;
	    for (int i=last_order; i > 0; i--)
		stmlist.add(_DATUM(new CONST(tf, null)));
	}
	// done!
	return Stm.toStm(stmlist);
    }
}
