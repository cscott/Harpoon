// OffsetMap32.java, created Wed Feb  3 18:43:47 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Analysis.InterfaceMethodMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Backend.Maps.ClassDepthMap;
import harpoon.Backend.Maps.MethodMap;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
 * @version  $Id: OffsetMap32.java,v 1.5 2004-02-08 01:58:09 cananian Exp $
 */
class OffsetMap32 extends OffsetMap
{
    public static final int     WORDSIZE = 4;

    private ClassDepthMap       cdm;   
    private FieldMap            fm;     
    private HClassInfo          hci;
    private InterfaceMethodMap  imm; 
    private MethodMap           cmm;

    private Map fields  = new HashMap();

    /** Class constructor */
    public OffsetMap32(ClassHierarchy ch) {
	assert ch!=null : "Class hierarchy must be non-null";
	
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
	    (new net.cscott.jutil.IteratorEnumerator(ch.classes().iterator()));
	this.cmm = new MethodMap() {
	    public int methodOrder(HMethod hm){return hci.getMethodOffset(hm);}
	};
    }
    
    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     *                                                           *
     *            Implementation of offset methods               *
     *                                                           *
     *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    /** Returns the offset of the class pointer */
    public int clazzPtrOffset(HClass hc)   { 
	assert !hc.isPrimitive();
	return -1 * WORDSIZE;
    }

    /** If hc is an array type, returns the offset of its component
     *  type's class pointer */
    public int componentTypeOffset(HClass hc) { 
	return -1 * WORDSIZE;
    }

    /** Returns the size of the display information */
    public int displaySize() {
	return cdm.maxDepth() * WORDSIZE;
    }

    /** Returns the offset of the first array element if hc is an array
     *  type, otherwise generates an assertion failure */
    public int elementsOffset(HClass hc) { 
	assert hc.isArray();
	return 1 * WORDSIZE; 
    }

    /** Returns the offset of the first field in an object of the
     *  specified type */
    public int fieldsOffset(HClass hc) { 
	assert (!hc.isPrimitive()) && 
		    (!hc.isArray()) && 
		    (!hc.isInterface());
	return 0;
    }

    /** Returns the offset of the hashcode of the specified object */
    public int hashCodeOffset(HClass hc) { 
	assert !hc.isPrimitive();
	return -2 * WORDSIZE; 
    }

    /** If hc is a class type, or an interface, returns the offset from
     *  the class pointer of the pointer to implemented interfaces */
    public int interfaceListOffset(HClass hc) { 
	assert !hc.isPrimitive();
	return -2 * WORDSIZE;
    }

    /** If hc is an array type, returns the offset of its length field */
    public int lengthOffset(HClass hc) { 
	assert hc.isArray() || hc.isInterface();
	return 0;
    }

    /** Returns the offset from the class pointer of this class's pointer
     *  in the display */
    public int offset(HClass hc) { 
	assert !hc.isPrimitive() && !hc.isInterface();
	return cdm.classDepth(hc) * WORDSIZE;
    }

    private interface ComparableHField extends Comparable { 
	public HField getField();
    }

    /** Returns the offset from the object reference of the specified 
     *  non-static field */
    public int offset(HField hf) {
	assert !hf.isStatic();

	final HClass   hc;
	Iterator       allFields;
	List           orderedFields;
	int            offset = 0;
    
	if (!this.fields.containsKey(hf)) { 
	    hc         = hf.getDeclaringClass();
	    allFields  = new Iterator() { 
		private HClass   cls    = hc;
		private HField[] fields = hc.getDeclaredFields();
		private int      index  = 0;
		public void remove() 
		{ throw new UnsupportedOperationException(); } 
		public boolean hasNext() { 
		    if (index<fields.length) return true; 
		    else { 
			if (cls==null) return false;
			for (HClass sCls=cls.getSuperclass(); sCls!=null; 
			     sCls=sCls.getSuperclass()) 
			    if (sCls.getDeclaredFields().length != 0)
			         return true;
			return false;
		    }
		}
		public Object next() { 
		    if (index<fields.length) { 
			return fields[index++];
		    }
		    else { 
			cls = cls.getSuperclass();
			if (cls==null) throw new NoSuchElementException();
			else { index=0; fields = cls.getDeclaredFields(); 
			       return next(); }
		    }
		}
	    };
	    orderedFields = new ArrayList();
	    for (Iterator i=allFields; i.hasNext();) { 
		final HField nextField = (HField)i.next();
		if (!nextField.isStatic()) 
		    orderedFields.add(new ComparableHField() { 
			public HField getField() { return nextField; } 
			public int compareTo(Object o) { 
			    HField hfo = ((ComparableHField)o).getField();
			    int    thisOrder = fm.fieldOrder(nextField);
			    int    hfOrder   = fm.fieldOrder(hfo);
			    return (thisOrder>hfOrder) ? 1 : 
				(thisOrder==hfOrder) ? 0 : -1;
			}
		    });
	    }
	    Collections.sort(orderedFields);
	    for (Iterator i=orderedFields.iterator(); i.hasNext();) { 
		HField hfield = ((ComparableHField)i.next()).getField();
		HClass type   = hfield.getType();
		this.fields.put(hfield, new Integer(offset));
		// No inlining
		offset += ((type==HClass.Long)||(type==HClass.Double)) ? 8 : 4;
	    }
	}

	return ((Integer)this.fields.get(hf)).intValue();
    }


    /** Returns the offset from the class pointer of the specified
     *  non-static method */
    public int offset(HMethod hm) { 
	assert hm != null && !hm.isStatic();
	HClass hc = hm.getDeclaringClass(); 
	if (hc.isInterface()) 
	    return -WORDSIZE * (imm.methodOrder(hm)*WORDSIZE) - (2*WORDSIZE);
	else 
	    return (cmm.methodOrder(hm)*WORDSIZE) + displaySize();
    }

    /** Returns the size of the specified class */
    public int size(HClass hc) { 
	assert !hc.isInterface();

	if (hc.isPrimitive()) { 
	    return hc==HClass.Long || hc==HClass.Double ? 8 : 4; 
	}
	else if (hc.isArray()) { 
	    // The size of an array instance is determined by the number of
	    // elements it has, and therefore cannot be determined by this
	    // method. 
	    assert false : "Size of array does not depend on its class!";
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

    public int wordsize() { return WORDSIZE; } 

    // stub for old FieldMap interface
    abstract class FieldMap {
	public abstract int fieldOrder(HField hf);
    }
}
