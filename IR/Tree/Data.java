// Data.java, created Wed Jul 28 14:17:31 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HData;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Analysis.QuadSSA.ClassHierarchy;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.IR.Properties.Derivation;
import harpoon.IR.Properties.Derivation.DList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The <code>Data</code> codeview is an implementation of the 
 * <code>HData</code> interface which exposes an <code>HClass</code>'s 
 * static data.  Unlike other codeviews which are associated with particular
 * methods, the each <code>Data</code> codeview is associated with an
 * class.  
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: Data.java,v 1.1.2.15 1999-09-02 21:28:11 pnkfelix Exp $
 */
public class Data extends Code implements HData { 
    public static final String codename = "tree-data";

    private /*final*/ EdgeInitializer  edgeInitializer;
    private /*final*/ HClass           cls;
    
    private ClassHierarchy classHierarchy;

    public Data(HClass cls, Frame frame, ClassHierarchy ch) { 
	super(cls.getMethods()[0], null, frame);
	this.cls = cls;
	this.classHierarchy = ch;

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
	// Shouldn't get DefaultNameMap, could be different than what 
	harpoon.Backend.Maps.NameMap nm = 
	    new harpoon.Backend.Maps.DefaultNameMap();

	// OffsetMap uses. 
	OffsetMap offm   = frame.getOffsetMap();
	int ws = offm.wordsize();

	// Add interface list pointer
	Label iListPtr = new Label();
	add(offm.interfaceListOffset(cls)/ws, _DATA(iListPtr), up, down);

	// Add component type 
	add(offm.componentTypeOffset(cls)/ws, 
	    cls.isArray() ? 
	    _DATA(offm.label(cls.getComponentType())) :
	    _DATA(new CONST(tf, null, 0)),
	    up, 
	    down);

	// FIX: need to lay out GC tables
	// FIX: need tables for static methods


	// Construct null-terminated list of interfaces and
	// add all interface methods
	HClass[] interfaces = cls.getInterfaces();
	for (int i=0; i<interfaces.length; i++) { 
	    HClass    iFace    = interfaces[i];
	    HMethod[] iMethods = iFace.getMethods();
	    iList.add(_DATA(offm.label(iFace)));  // Add to interface list
	    for (int j=0; j<iMethods.length; j++) { 
		HMethod hm = iMethods[j];
		DATA data = null;
		if (classHierarchy.callableMethods().contains(hm)) {
		    // Point to class method
		    data = _DATA(offm.label(cls.getMethod  
					    (hm.getName(),
					     hm.getParameterTypes())));
		    //System.out.println("output "+data+" for " + hm);
		} else {
		    // null (never used, so don't create reference)
		    data = _DATA(new CONST(tf, null, 0));
		    //System.out.println("output "+data+" for " + hm);
		}
		add(offm.offset(hm)/ws, data, up, down);
	    }
	}
	iList.add(_DATA(new CONST(tf, null, 0)));
	iList.add(0, new LABEL(tf, null, iListPtr, false));
	

	// Add display to list
	for (HClass sCls=cls; sCls!=null; sCls=sCls.getSuperclass()) 
	    add(offm.offset(sCls)/ws,_DATA(offm.label(sCls)),up,down);
	
	// Add non-static class methods to list 
	HMethod[] methods = cls.getMethods();
	for (int i=0; i<methods.length; i++) { 
	    HMethod hm = methods[i];
	    if (!hm.isStatic()) { 
		DATA data = null;
		if (classHierarchy.callableMethods().contains(hm)) {
		    // Point to class method
		    data = _DATA(offm.label(hm));
		    //System.out.println("output "+data+" for " + hm);
		} else {
		    // null (never used, so don't create reference)
		    data = _DATA(new CONST(tf, null, 0));
		    //System.out.println("output "+data+" for " + hm);
		}
		add(offm.offset(hm)/ws, data, up, down);
	    }
	}

	// Reverse the upward list
	Collections.reverse(up);
	down.add(0, new LABEL(tf,null,offm.label(cls),true));

	for (HClass sCls = cls; sCls!=null; sCls=sCls.getSuperclass()) { 
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
	rList  .add(   new SEGMENT(tf, null, SEGMENT.REFLECTION_DATA));
	//rList  .add(ObjectBuilder.makeClass(tf,frame,this.cls).stm);

	// At last, assign the root element
	this.tree = 
	    Stm.toStm
	    (Arrays.asList
	     (new Stm[] { 
		 Stm.toStm(up),
		 Stm.toStm(down),
		 Stm.toStm(spList),
		 Stm.toStm(soList),
		 Stm.toStm(iList)
		 // Stm.toStm(rList)
	     }));
	     
	// Compute the edges of this HData
	(this.edgeInitializer = new EdgeInitializer()).computeEdges();
    }
    
    // Copy constructor, should only be used by the clone() method 
    private Data(HClass cls, Tree tree, Frame frame, ClassHierarchy ch) { 
	super(cls.getMethods()[0], tree, frame);
	this.cls = cls;
	this.classHierarchy = ch;
	final CloningTempMap ctm = 
	    new CloningTempMap
	    (tree.getFactory().tempFactory(), this.tf.tempFactory());
	this.tree = (Tree)Tree.clone(this.tf, ctm, tree);
	(this.edgeInitializer = new EdgeInitializer()).computeEdges();
    }
    
    /** Clone this data representation. The clone has its own copy
     *  of the Tree */
    public HData clone(HClass cls) { 
	return new Data(cls, this.tree, this.frame, this.classHierarchy);
    }

    /** Return the <code>HClass</code> that this data view belongs to */
    public HClass getHClass() { return this.cls; } 

    /** Return the name of this data view. */
    public String getName() { return codename; }  
    
    /** Returns <code>true</code>.  */
    public boolean isCanonical() { return true; } 

    public void print(java.io.PrintWriter pw) {
	Print.print(pw,this, null);
    } 

    /** 
     * Recomputes the control-flow graph exposed through this codeview
     * by the <code>HasEdges</code> interface of its elements.  
     * This method should be called whenever the tree structure of this
     * codeview is modified. 
     */
    public void recomputeEdges() { this.edgeInitializer.computeEdges(); }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
     *                                                          *
     *                   Disallowed methods                     *
     *                                                          *
     *+++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    /** Throws an <code>Error</code>.  Use 
     *  <code>clone(harpoon.ClassFile.HClass)</code> instead.  */
    public HCode clone(HMethod method, Frame frame) { 
	throw new Error("Use clone(harpoon.ClassFile.HClass) instead");
    }
    
    /** Throws an <code>Error</code>.  */
    public DList derivation(HCodeElement hce, Temp t) { 
	throw new Error("No derivation information for TreeData objects");
    }

    /** Throws an <code>Error</code>.  Static class data is not associated
     *  with a particular method.  */
    public HMethod getMethod() { 
	throw new Error("No method associated with TreeData objects");
    }

    /** Throws an <code>Error</code>. */
    public HClass typeMap(HCodeElement hce, Temp t) { 
	throw new Error("No typemap information for TreeData objects");
    }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
     *                                                          *
     *                    Utility methods                       *
     *                                                          *
     *+++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    private void add(int index, Tree elem, ArrayList up, ArrayList down) { 
	int size, requiredSize;
	if (index<0) { 
	    requiredSize = (-index);
	    if (requiredSize > up.size()) { 
		up.ensureCapacity(requiredSize);
		for (int i=up.size(); i<requiredSize; i++) 
		    up.add(new DATA(elem.getFactory(), elem));
	    }	    
	    up.set(-index-1, elem);
	}
	else {
	    requiredSize = index+1;
	    if (requiredSize > down.size()) { 
		down.ensureCapacity(requiredSize);
		for (int i=down.size(); i<requiredSize; i++) 
		    down.add(new DATA(elem.getFactory(), elem));
	    }	    
	    down.set(index, elem);
	}
    }
  
    private DATA _DATA(Exp e) { 
	return new DATA(tf, null, e); 
    }

    private DATA _DATA(Label l) {
	return new DATA(tf,null,new NAME(tf,null,l));
    }

    private int wordsize() { return frame.pointersAreLong() ? 8 : 4; }
}

