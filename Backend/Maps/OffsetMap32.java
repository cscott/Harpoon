// OffsetMap32.java, created Wed Feb  3 18:43:47 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

import harpoon.Analysis.InterfaceMethodMap;
import harpoon.Analysis.QuadSSA.ClassHierarchy;
import harpoon.Backend.Analysis.DisplayInfo.HClassInfo;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * The OffsetMap32 class implements the abstract methods of OffsetMap,
 * specializing them for 32-bit architectures.
 *
 * @author   Duncan Bryce <duncan@lcs.mit.edu>
 * @version  $Id: OffsetMap32.java,v 1.1.2.18 1999-08-11 10:41:14 duncan Exp $
 */
public class OffsetMap32 extends OffsetMap
{
    private static int WORDSIZE = 4;

    private ClassDepthMap       cdm;   
    private FieldMap            fm;     
    private HClassInfo          hci;
    private InterfaceMethodMap  imm; 
    private MethodMap           cmm;
    private NameMap             nm;

    private Map fields  = new HashMap();
    private Map labels  = new HashMap();
    private Map strings = new HashMap();

    /** Class constructor */
    public OffsetMap32(ClassHierarchy ch, NameMap nm) {
	Util.assert(ch!=null, "Class hierarchy must be non-null");
	
	int _maxDepth = 0;
	for (Iterator it=ch.classes().iterator();it.hasNext();){
	    HClass hc    = (HClass)it.next();
	    int    depth = hci.depth(hc);
	    _maxDepth    = (depth>_maxDepth) ? depth : _maxDepth;
	}
	final int maxDepth = _maxDepth;

	this.cdm = new ClassDepthMap() {
	    public int classDepth(HClass hc) { return hci.depth(hc); }
	    public int maxDepth() { return maxDepth; } 
	};
	this.fm = new FieldMap() {
	    public int fieldOrder(HField hf) {return hci.getFieldOffset(hf);}
	};
	this.hci = new HClassInfo();
	this.imm = new InterfaceMethodMap
	    (new harpoon.Util.IteratorEnumerator(ch.classes().iterator()));
	this.cmm = new MethodMap() {
	    public int methodOrder(HMethod hm){return hci.getMethodOffset(hm);}
	};
	this.nm  = nm;
    }
    
    /** Create an <code>OffsetMap32</code> using a
     *  <code>DefaultNameMap</code>. */
    public OffsetMap32(ClassHierarchy ch) {
	this(ch, new DefaultNameMap());
    }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     *                                                           *
     *            Implementation of label mappings               *
     *                                                           *
     *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/


    /** Returns the label corresponding to the specified HClass */
    public Label label(HClass hc) { 
	if (!labels.containsKey(hc)) {
	    labels.put(hc, new Label(nm.mangle(hc)));
	}
	return (Label)labels.get(hc);
    }
	    
    /** Returns the label corresponding to the specified static field */
    public Label label(HField hf) { 
	Util.assert(hf.isStatic());
	if (!labels.containsKey(hf)) {
	    labels.put(hf, new Label(nm.mangle(hf)));
	}
	return (Label)labels.get(hf);
    }

    /** Returns the label corrensponding to the specified method.  This
     *  method is not necessarily static */
    public Label label(HMethod hm) { 
	if (!labels.containsKey(hm)) {
	    labels.put(hm, new Label(nm.mangle(hm))); 
	}
	return (Label)labels.get(hm);
    }

    /** Returns the label corresponding to the specified String constant */
    public Label label(String stringConstant) { 
	if (!strings.containsKey(stringConstant)) {
	    strings.put(stringConstant, new Label(nm.mangle(stringConstant)));
	}
	return (Label)strings.get(stringConstant);
    }

    public Iterator stringConstants() {	return strings.keySet().iterator(); }
    
    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     *                                                           *
     *            Implementation of offset methods               *
     *                                                           *
     *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/


    /** Returns the offset of the class pointer */
    public int clazzPtrOffset(HClass hc)   { 
	Util.assert(!hc.isPrimitive());
	return -1 * WORDSIZE;
    }

    /** If hc is an array type, returns the offset of its component
     *  type's class pointer */
    public int componentTypeOffset(HClass hc) { 
	Util.assert(hc.isArray());
	return -1 * WORDSIZE;
    }

    /** Returns the size of the display information */
    public int displaySize() {
	return cdm.maxDepth() * WORDSIZE;
    }

    /** Returns the offset of the first array element if hc is an array
     *  type, otherwise generates an assertion failure */
    public int elementsOffset(HClass hc) { 
	Util.assert(hc.isArray());
	return 0; 
    }

    /** Returns the offset of the first field in an object of the
     *  specified type */
    public int fieldsOffset(HClass hc) { 
	Util.assert((!hc.isPrimitive()) && (!hc.isArray()));
	return 0;
    }

    /** Returns the offset of the hashcode of the specified object */
    public int hashCodeOffset(HClass hc) { 
	Util.assert(!hc.isPrimitive());
	return -2 * WORDSIZE; 
    }

    /** If hc is a class type, or an interface, returns the offset from
     *  the class pointer of the pointer to implemented interfaces */
    public int interfaceListOffset(HClass hc) { 
	Util.assert(!hc.isPrimitive() && !hc.isArray());
	return -4 * WORDSIZE;
    }

    /** If hc is an array type, returns the offset of its length field */
    public int lengthOffset(HClass hc) { 
	Util.assert(hc.isArray());
	return -3 * WORDSIZE; 
    }

    /** Returns the offset from the class pointer of this class's pointer
     *  in the display */
    public int offset(HClass hc) { 
	Util.assert(!hc.isPrimitive() && !hc.isInterface());
	return cdm.classDepth(hc) * WORDSIZE;
    }

    /** Returns the offset from the object reference of the specified 
     *  non-static field */
    public int offset(HField hf) {
	Util.assert(!hf.isStatic());

	HClass    hc;
	HField[]  hfields, orderedFields;
	int       fieldOrder, offset;
    
	hc = hf.getDeclaringClass();
	if (!this.fields.containsKey(hc)) {
	    hfields        = hc.getDeclaredFields();
	    orderedFields  = new HField[hfields.length];
	    for (int i=0; i<hfields.length; i++) 
		orderedFields[fm.fieldOrder(hfields[i])] = hfields[i];
	    // Cache field ordering
	    this.fields.put(hc, orderedFields);
	}
	else 
	    orderedFields  = (HField[])this.fields.get(hc);

	fieldOrder = fm.fieldOrder(hf);
	offset     = fieldsOffset(hc);
    
	for (int i=0; i<fieldOrder; i++)
	    offset += size(orderedFields[i].getType(), false); // no inlining

	return offset;
    }


    /** Returns the offset from the class pointer of the specified
     *  non-static method */
    public int offset(HMethod hm) { 
	Util.assert(!hm.isStatic());
	HClass hc = hm.getDeclaringClass(); 
	if (hc.isInterface()) return (-imm.methodOrder(hm) - 4) * WORDSIZE;
	else return (cmm.methodOrder(hm)*WORDSIZE) + displaySize();
    }

    /** Returns the size of the specified class */
    public int size(HClass hc) {
	return size(hc, true);
    }
  
    /** Returns the size of the specified class */
    private int size(HClass hc, boolean inline) { 
	int size;

	if (hc.isPrimitive()) { 
	    if ((hc==HClass.Long)||(hc==HClass.Double)) size = 2 * WORDSIZE;
	    else size = WORDSIZE;
	}
	else if (hc.isArray()) { 
	    Util.assert(false, "Size of array does not depend on its class!");
	    return -1;
	} 
	else { 
	    if (inline) {
		size = 2 * WORDSIZE;  // Includes hashcode & classptr
	    
		HField[] hf = hc.getDeclaredFields();
		for (int i=0; i<hf.length; i++) {
		    size += hf[i].isStatic()?0:size(hf[i].getType(), false);
		}
	    }
	    else { size = WORDSIZE; }
	}
	
	return size; 
    }
}
