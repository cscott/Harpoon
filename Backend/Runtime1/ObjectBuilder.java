// ObjectBuilder.java, created Mon Oct 11 18:56:52 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Backend.Generic.Runtime.ObjectBuilder.Info;
import harpoon.Backend.Generic.Runtime.ObjectBuilder.ArrayInfo;
import harpoon.Backend.Generic.Runtime.ObjectBuilder.ObjectInfo;
import harpoon.Backend.Maps.FieldMap;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATA;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.SEGMENT;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * <code>ObjectBuilder</code> is an implementation of
 * <code>harpoon.Backend.Generic.Runtime.ObjectBuilder</code> for the
 * <code>Runtime1</code> runtime.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ObjectBuilder.java,v 1.1.4.2 1999-10-20 07:05:57 cananian Exp $
 */
public class ObjectBuilder
    extends harpoon.Backend.Generic.Runtime.ObjectBuilder {
    final NameMap nm;
    final FieldMap cfm;

    /** Creates a <code>ObjectBuilder</code>. */
    public ObjectBuilder(Runtime runtime) {
	this.nm = runtime.nameMap;
	this.cfm= ((TreeBuilder) runtime.treeBuilder).cfm;
    }

    public Stm buildObject(TreeFactory tf, ObjectInfo info,
			   boolean exported) {
	Util.assert(!info.type().isArray());
	Util.assert(!info.type().isPrimitive());
	List stmlist = new ArrayList();
	// header
	stmlist.add(makeHeader(tf, info, exported));
	// fields, in field order
	List l = cfm.fieldList(info.type());
	for (Iterator it = l.iterator(); it.hasNext(); )
	    stmlist.add(makeDatum(tf, info.get((HField)it.next())));
	// done -- ta da!
	return Stm.toStm(stmlist);
    }
    public Stm buildArray(TreeFactory tf, ArrayInfo info,
			  boolean exported) {
	Util.assert(info.type().isArray());
	HClass cType = info.type().getComponentType();
	List stmlist = new ArrayList(info.length()+2);
	// header
	stmlist.add(makeHeader(tf, info, exported));
	// length
	stmlist.add(_DATA(tf, new CONST(tf, null, info.length())));
	// data
	for (int i=0; i<info.length(); i++)
	    stmlist.add(makeDatum(tf, info.get(i)));
	// done -- ta da!
	return Stm.toStm(stmlist);
    }
    Stm makeHeader(TreeFactory tf, Info info, boolean exported)
    {
	List stmlist = new ArrayList(4);
	// align to word boundary.
	stmlist.add(new ALIGN(tf, null, 4));
	// hash code.
	stmlist.add(_DATA(tf, info.label()));
	// label:
	stmlist.add(new LABEL(tf, null, info.label(), exported));
	// claz pointer
	stmlist.add(_DATA(tf, nm.label(info.type())));
	// okay, done with header.
	return Stm.toStm(stmlist);
    }
    Stm makeDatum(TreeFactory tf, Object datum) {
	if (datum instanceof Integer)
	    return _DATA(tf, new CONST(tf, null,
				   ((Integer)datum).intValue()));
	else if (datum instanceof Long)
	    return _DATA(tf, new CONST(tf, null,
				   ((Long)datum).longValue()));
	else if (datum instanceof Float)
	    return _DATA(tf, new CONST(tf, null,
				   ((Float)datum).floatValue()));
	else if (datum instanceof Double)
	    return _DATA(tf, new CONST(tf, null,
				   ((Double)datum).doubleValue()));
	else if (datum instanceof Boolean)
	    return _DATA(tf, new CONST(tf, null, 8, false,
				   ((Boolean)datum).booleanValue()?1:0));
	else if (datum instanceof Byte)
	    return _DATA(tf, new CONST(tf, null, 8, true,
				   ((Byte)datum).intValue()));
	else if (datum instanceof Short)
	    return _DATA(tf, new CONST(tf, null,16, true,
				   ((Short)datum).intValue()));
	else if (datum instanceof Character)
	    return _DATA(tf, new CONST(tf, null,16, false,
				   ((Character)datum).charValue()));
	else if (datum instanceof Info)
	    return _DATA(tf, ((Info)datum).label());
	else throw new Error("ILLEGAL DATA TYPE");
    }
    DATA _DATA(TreeFactory tf, Exp e) { 
	return new DATA(tf, null, e); 
    }
    DATA _DATA(TreeFactory tf, Label l) {
	return new DATA(tf,null,new NAME(tf,null,l));
    }
}
