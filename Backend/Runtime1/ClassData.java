// ClassData.java, created Wed Sep  8 14:31:44 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.NameMap;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HDataElement;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATA;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.SEGMENT;
import harpoon.Temp.Label;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * <code>ClassData</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClassData.java,v 1.1.2.2 1999-09-09 05:49:18 cananian Exp $
 */
public class ClassData extends Data {
    /** Creates a <code>ClassData</code>. */
    public ClassData(Frame f, HClass hc, ClassHierarchy ch) {
	super("class-data", hc, f);
	this.root = build(ch);
    }

    private HDataElement build(ClassHierarchy ch) {

	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	//                                                           //
	// Construct layout of static class data                     //
	//                                                           //
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	ArrayList up      = new ArrayList(); // class data above the cls ptr
	ArrayList down    = new ArrayList(); // class data below the cls ptr
	List      iList   = new ArrayList(); // interface list
	List      soList  = new ArrayList(); // static objects
	List      spList  = new ArrayList(); // static primitives
	List      rList   = new ArrayList(); // tables for reflection

	harpoon.Backend.Maps.NameMap nm = frame.getRuntime().nameMap();

	// OffsetMap uses. 
	OffsetMap offm   = frame.getOffsetMap();
	int ws = offm.wordsize();

	// Add interface list pointer
	Label iListPtr = new Label();
	add(offm.interfaceListOffset(this.hc)/ws, _DATA(iListPtr), up, down);

	// Add component type 
	add(offm.componentTypeOffset(this.hc)/ws, 
	    this.hc.isArray() ? 
	    _DATA(offm.label(this.hc.getComponentType())) :
	    _DATA(new CONST(tf, null, 0)),
	    up, 
	    down);

	// FIX: need to lay out GC tables
	// FIX: need tables for static methods


	// Construct null-terminated list of interfaces and
	// add all interface methods
	DEBUGln("reached interface loop");

	for (HClass c=this.hc; c!=null; c=c.getSuperclass()) {
	    HClass[] interfaces = c.getInterfaces();
	    for (int i=0; i<interfaces.length; i++) { 
		HClass    iFace    = interfaces[i];
		HMethod[] iMethods = iFace.getMethods();
		iList.add(_DATA(offm.label(iFace)));  // Add to interface list
		for (int j=0; j<iMethods.length; j++) { 
		    DATA data;
		    HMethod ihm = iMethods[j];
		    HMethod chm = c.getMethod(ihm.getName(),
					      ihm.getParameterTypes());
		    if (ch.callableMethods().contains(chm) &&
			!Modifier.isAbstract(chm.getModifiers())) {
			// Point to class method
			data = _DATA(offm.label(chm));
		    } else {
			data = _DATA(new CONST(tf, null, 0));
		    }
		    add(offm.offset(ihm)/ws, data, up, down);
		}
	    }
	}
	iList.add(_DATA(new CONST(tf, null, 0)));
	iList.add(0, new LABEL(tf, null, iListPtr, false));
	
	DEBUGln("reached display loop");

	// Add display to list
	// CSA: interfaces don't have a display list
	if (this.hc.isInterface())
	    add(offm.offset(HClass.forName("java.lang.Object"))/ws,
		_DATA(offm.label(HClass.forName("java.lang.Object"))),
		up, down);
	else for (HClass sCls=this.hc; sCls!=null; sCls=sCls.getSuperclass()) 
	    add(offm.offset(sCls)/ws,_DATA(offm.label(sCls)),up,down);
	
	DEBUGln("reached static method loop");

	// Add non-static class methods to list 
	// CSA FIXME: omit constructors?
	HMethod[] methods = this.hc.getMethods();
	for (int i=0; i<methods.length; i++) { 
	    HMethod hm = methods[i];
	    if (!methods[i].isStatic()) { 
		DATA data = null;
		if (ch.callableMethods().contains(hm) &&
		    !Modifier.isAbstract(hm.getModifiers())) {
		    // Point to class method
		    data = _DATA(offm.label(hm));
		} else {
		    // null (never used, so don't create reference)
		    data = _DATA(new CONST(tf, null, 0));
		}
		add(offm.offset(hm)/ws, data, up, down);
	    }
	}

	// Reverse the upward list
	Collections.reverse(up);
	down.add(0, new LABEL(tf,null,offm.label(this.hc), true));

	DEBUGln("reached fields loop");

	for (HClass sCls = this.hc; sCls!=null; sCls=sCls.getSuperclass()) { 
	    HField[] hfields  = sCls.getDeclaredFields();
	    for (int i=0; i<hfields.length; i++) { 
		HField hfield = hfields[i];
		if (hfield.isStatic()) { 
		    if (hfield.getType().isPrimitive()) {
			spList.add(new LABEL(tf, null, offm.label(hfield), true));
			spList.add(_DATA(new CONST(tf, null, 0)));
		    }
		    else { 
			soList.add(new LABEL(tf, null, offm.label(hfield), true));
			soList.add(_DATA(new CONST(tf, null, 0)));
		    }
		}
	    }
	}

	// Assign segment types:
	iList  .add(0, new SEGMENT(tf, null, SEGMENT.CLASS));
	up     .add(0, new SEGMENT(tf, null, SEGMENT.CLASS));
	soList .add(0, new SEGMENT(tf, null, SEGMENT.STATIC_OBJECTS));
	spList .add(0, new SEGMENT(tf, null, SEGMENT.STATIC_PRIMITIVES));
	rList  .add(   new SEGMENT(tf, null, SEGMENT.REFLECTION_PTRS));
	rList  .add(   new SEGMENT(tf, null, SEGMENT.REFLECTION_DATA));

	/*
	DEBUGln("building class");

	ESEQ objectData = ObjectBuilder.buildClass(tf,frame,this.hc);
	rList.add(new LABEL(tf,null,offm.jlClass(this.hc),false));
	rList.add(_DATA(objectData.exp));
	rList.add(new SEGMENT(tf,null,SEGMENT.REFLECTION_DATA));
	rList.add(objectData.stm);
	*/

	List result = new ArrayList();
	result.addAll(up);
	result.addAll(down);
	result.addAll(spList);
	result.addAll(soList);
	result.addAll(iList);
	result.addAll(rList);

	DEBUGln("converting result to Stm");

	/*
	this.tree = Stm.toStm(result);

	DEBUGln("almost done; about to compute edges");

	(this.edgeInitializer = new EdgeInitializer()).computeEdges();
	*/
	return (HDataElement) harpoon.IR.Tree.Stm.toStm(result);
    }
    private static final boolean DEBUG = true;
    private static final void DEBUGln(String s) {
	if (DEBUG) System.out.println("ClassData: " + s);
    }
    private static final void DEBUG(String s) {
	if (DEBUG) System.out.print("ClassData: " + s);
    }
}
