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
import java.util.Set;
import java.util.StringTokenizer;

/**
 * The <code>OffsetMap32</code> class implements the abstract methods of 
 * <code>OffsetMap</code>, specializing them for 32-bit architectures.  
 * For reference, <code>OffsetMap32</code> specifies the following 
 * layouts:
 * 
 * <br><b>Object instance layout:</b>
 * <pre>
 *     -8       hashcode
 *     -4       clazz ptr
 *      0       non-static field 0
 *      4       non-static field 1
 *        ...
 *      4*(n-1) non-static field n-1
 * </pre> 
 * 
 * <br><b>Array instance layout:</b>
 * <pre>
 *    -12       length
 *     -8       hashcode
 *     -4       clazz ptr
 *      0       array element 0
 *      4       array element 1
 *        ...
 *      4*(n-1) array element n-1
 * </pre>
 * 
 * <br><b>Static class layout:</b>
 * <pre>
 *     -4*(n+1)           interface method n-1
 *        ...
 *    -16                 interface method 1
 *    -12                 interface method 0
 *     -8                 component type (NULL if not array)
 *     -4                 interface list ptr
 *      0                 display 0
 *      4                 display 1
 *        ...
 *      4*MAXcd           display max class depth
 *      4*MAXcd+4         class method 0
 *      4*MAXcd+8         class method 1
 *        ...
 *      4*MAXcd+4*(n-1)   class method n-1
 * </pre>
 * 
 * <br><i>However</i>, this layout is most definitely subject to change, 
 * so no implementation should rely on it. 
 * 
 *
 * @author   Duncan Bryce <duncan@lcs.mit.edu>
 * @version  $Id: OffsetMap32.java,v 1.1.2.19 1999-08-16 03:44:17 duncan Exp $
 */
public class OffsetMap32 extends OffsetMap
{
    public static final int     WORDSIZE = 4;

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
	
	this.hci = new HClassInfo();

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

    public Set stringConstants() { return strings.keySet(); }
    
    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     *                                                           *
     *            Implementation of offset methods               *
     *                                                           *
     *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    /** Returns the offset of the class pointer */
    public int clazzPtrOffset(HClass hc)   { 
	Util.assert((!hc.isPrimitive()) && (!hc.isInterface())); 
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
	Util.assert((!hc.isPrimitive()) && 
		    (!hc.isArray()) && 
		    (!hc.isInterface()));
	return 0;
    }

    /** Returns the offset of the hashcode of the specified object */
    public int hashCodeOffset(HClass hc) { 
	Util.assert((!hc.isPrimitive()) && (!hc.isInterface()));
	return -2 * WORDSIZE; 
    }

    /** If hc is a class type, or an interface, returns the offset from
     *  the class pointer of the pointer to implemented interfaces */
    public int interfaceListOffset(HClass hc) { 
	Util.assert(!hc.isPrimitive() && !hc.isArray());
	return -2 * WORDSIZE;
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
	int       fieldOrder, offset = 0;
    
	if (!this.fields.containsKey(hf)) { 
	    hc            = hf.getDeclaringClass();
	    hfields       = hc.getDeclaredFields();
	    orderedFields = new HField[hfields.length];
	    for (int i=0; i<hfields.length; i++) 
		if (!hfields[i].isStatic())
		    orderedFields[fm.fieldOrder(hfields[i])] = hfields[i];
	    for (int i=0; i<hfields.length; i++) { 
		HClass type = orderedFields[i].getType();
		this.fields.put(hfields[i], new Integer(offset));
		// No inlining
		offset += ((type==HClass.Long)||(type==HClass.Double)) ? 8 : 4;
	    }
	}

	return ((Integer)this.fields.get(hf)).intValue();
    }


    /** Returns the offset from the class pointer of the specified
     *  non-static method */
    public int offset(HMethod hm) { 
	Util.assert(!hm.isStatic());
	HClass hc = hm.getDeclaringClass(); 
	if (hc.isInterface()) 
	    return -WORDSIZE * (imm.methodOrder(hm)*WORDSIZE) - (2*WORDSIZE);
	else 
	    return (cmm.methodOrder(hm)*WORDSIZE) + displaySize();
    }

    /** Returns the size of the specified class */
    public int size(HClass hc) { 
	Util.assert(!hc.isInterface());

	if (hc.isPrimitive()) { 
	    return hc==HClass.Long || hc==HClass.Double ? 8 : 4; 
	}
	else if (hc.isArray()) { 
	    // The size of an array instance is determined by the number of
	    // elements it has, and therefore cannot be determined by this
	    // method. 
	    Util.assert(false, "Size of array does not depend on its class!");
	    return -1;
	} 
	else { 
	    int size = 2 * WORDSIZE;  // Includes hashcode & classptr
	    HField[] hf = hc.getDeclaredFields();
	    for (int i=0; i<hf.length; i++) {
		HClass type = hf[i].getType();
		size += hf[i].isStatic() ? 0 :
		        (type==HClass.Long || type==HClass.Double) ? 8 : 4;
	    }
	    return size; 
	}
    }    
}
