// DataStaticFields.java, created Mon Oct 11 23:46:16 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.FieldMap;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HDataElement;
import harpoon.ClassFile.HField;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.SEGMENT;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * <code>DataStaticFields</code> lays out the static fields of a class.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DataStaticFields.java,v 1.5 2003-10-21 02:11:02 cananian Exp $
 */
public class DataStaticFields extends Data {
    final NameMap m_nm;
    final FieldMap m_fm;
    
    /** Creates a <code>DataStaticFields</code>. */
    public DataStaticFields(Frame f, HClass hc) {
        super("static-fields", hc, f);
	this.m_nm = f.getRuntime().getNameMap();
	// note that technically this is a *class* field map, and so
	// we can't be sure that it will work on static fields.  but
	// in fact, we wrote the code, so we know it will.  Slight
	// violation of abstraction, but it makes everything more
	// maintainable *not* to duplicate the code here.
	this.m_fm = ((TreeBuilder)f.getRuntime().getTreeBuilder()).cfm;
	this.root = build(hc, f.pointersAreLong());
    }
    private HDataElement build(HClass hc, boolean pointersAreLong) {
	HField[] fields = (HField[]) hc.getDeclaredFields().clone();
	// first, sort fields by size to pack 'em in as tight as we can.
	Collections.sort(Arrays.asList(fields), new Comparator() {
	    public int compare(Object o1, Object o2) {
		return m_fm.fieldSize((HField)o1) - m_fm.fieldSize((HField)o2);
	    }
	});
	List stmlist = new ArrayList(2+3*fields.length/*at most*/);
	// first do static fields with non-primitive type
	stmlist.add(new SEGMENT(tf, null, SEGMENT.STATIC_OBJECTS));
	stmlist.add(new ALIGN(tf, null, // align pointer fields to pointer size
			      pointersAreLong ? 8 : 4));
	for (int i=0; i<fields.length; i++) {
	    if (!fields[i].isStatic() || fields[i].getType().isPrimitive())
		continue;
	    stmlist.add(new LABEL(tf, null, m_nm.label(fields[i]), true));
	    stmlist.add(_DATUM(new CONST(tf, null))); // null pointer.
	}
	// next do static fields with primitive types
	stmlist.add(new SEGMENT(tf, null, SEGMENT.STATIC_PRIMITIVES));
	for (int i=0; i<fields.length; i++) {
	    if (!fields[i].isStatic() || !fields[i].getType().isPrimitive())
		continue;
	    // align to field size.
	    stmlist.add(new ALIGN(tf, null, m_fm.fieldSize(fields[i])));
	    stmlist.add(new LABEL(tf, null, m_nm.label(fields[i]), true));
	    stmlist.add(_DATUM(fieldInitializer(fields[i])));
	}
	return (HDataElement) Stm.toStm(stmlist);
    }
    Exp fieldInitializer(HField f) {
	HClass ty = f.getType();
	assert ty.isPrimitive();
	Object cvo = f.getConstant();
	if (ty==HClass.Int)
	    return new CONST(tf, null, (int)
			     (cvo==null?0:((Integer)cvo).intValue()));
	if (ty==HClass.Long)
	    return new CONST(tf, null, (long)
			     (cvo==null?0:((Long)cvo).longValue()));
	if (ty==HClass.Float)
	    return new CONST(tf, null, (float)
			     (cvo==null?0:((Float)cvo).floatValue()));
	if (ty==HClass.Double)
	    return new CONST(tf, null, (double)
			     (cvo==null?0:((Double)cvo).doubleValue()));
	// sub-integer types represented by Integer wrapped values.
	if (ty==HClass.Boolean)
	    return new CONST(tf, null, 8, false, 
			     (cvo==null?0:((Number)cvo).intValue()));
	if (ty==HClass.Byte)
	    return new CONST(tf, null, 8, true,
			     (cvo==null?0:((Number)cvo).intValue()));
	if (ty==HClass.Char)
	    return new CONST(tf, null, 16, false,
			     (cvo==null?0:((Number)cvo).intValue()));
	if (ty==HClass.Short)
	    return new CONST(tf, null, 16, true,
			     (cvo==null?0:((Number)cvo).intValue()));
	throw new Error("Unknown primitive type");
    }
}
