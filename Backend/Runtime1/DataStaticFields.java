// DataStaticFields.java, created Mon Oct 11 23:46:16 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HDataElement;
import harpoon.ClassFile.HField;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATA;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.SEGMENT;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * <code>DataStaticFields</code> lays out the static fields of a class.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DataStaticFields.java,v 1.1.4.1 1999-10-12 20:04:50 cananian Exp $
 */
public class DataStaticFields extends Data {
    final NameMap m_nm;
    
    /** Creates a <code>DataStaticFields</code>. */
    public DataStaticFields(Frame f, HClass hc) {
        super("static-fields", hc, f);
	this.m_nm = f.getRuntime().nameMap;
	this.root = build(hc);
    }
    private HDataElement build(HClass hc) {
	HField[] fields = hc.getDeclaredFields();
	List stmlist = new ArrayList();
	// first do static fields with non-primitive type
	stmlist.add(new SEGMENT(tf, null, SEGMENT.STATIC_OBJECTS));
	for (int i=0; i<fields.length; i++) {
	    if (!fields[i].isStatic() || fields[i].getType().isPrimitive())
		continue;
	    stmlist.add(new LABEL(tf, null, m_nm.label(fields[i]), true));
	    stmlist.add(_DATA(new CONST(tf, null))); // null pointer.
	}
	// next do static fields with primitive types
	stmlist.add(new SEGMENT(tf, null, SEGMENT.STATIC_PRIMITIVES));
	for (int i=0; i<fields.length; i++) {
	    if (!fields[i].isStatic() || !fields[i].getType().isPrimitive())
		continue;
	    stmlist.add(new LABEL(tf, null, m_nm.label(fields[i]), true));
	    stmlist.add(_DATA(fieldInitializer(fields[i])));
	}
	return (HDataElement) Stm.toStm(stmlist);
    }
    Exp fieldInitializer(HField f) {
	HClass ty = f.getType();
	Util.assert(ty.isPrimitive());
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
	if (ty==HClass.Boolean)
	    return new CONST(tf, null, 8, false, 
			     (cvo==null?0:((Boolean)cvo).booleanValue()?1:0));
	if (ty==HClass.Byte)
	    return new CONST(tf, null, 8, true,
			     (cvo==null?0:((Byte)cvo).intValue()));
	if (ty==HClass.Char)
	    return new CONST(tf, null, 16, false,
			     (cvo==null?0:((Character)cvo).charValue()));
	if (ty==HClass.Short)
	    return new CONST(tf, null, 16, true,
			     (cvo==null?0:((Short)cvo).intValue()));
	throw new Error("Unknown primitive type");
    }
}
