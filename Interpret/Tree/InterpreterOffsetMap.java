// InterpreterOffsetMap.java, created Sat Mar 27 17:05:09 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Analysis.InterfaceMethodMap;
import harpoon.Backend.Maps.ClassDepthMap;
import harpoon.Backend.Maps.MethodMap;
import harpoon.Backend.Maps.DefaultNameMap;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * A simple OffsetMap, used by the Tree Interpreter
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: InterpreterOffsetMap.java,v 1.5 2004-02-08 01:58:09 cananian Exp $
 */
public class InterpreterOffsetMap extends OffsetMap {

    private ClassDepthMap       m_cdm;
    private FieldMap            m_fm;
    private Map                 m_field   = new HashMap();
    private Map                 m_labels  = new HashMap(); 
    private Map                 m_strings = new HashMap();
    private HClassInfo          m_hci;
    private InterfaceMethodMap  m_imm; 
    private MethodMap           m_cmm;
    private NameMap             m_nm;

    public InterpreterOffsetMap(ClassHierarchy ch) {
	this(ch, new DefaultNameMap(false));
    }

    /** Class constructor */
    public InterpreterOffsetMap(ClassHierarchy ch, NameMap nm) {
	m_hci = new HClassInfo();
	m_hci = new HClassInfo();
	int max = 0, depth;
	for (Iterator it=ch.classes().iterator();it.hasNext();){
	    depth = m_hci.depth((HClass) it.next());
	    max   = (depth>max) ? depth : max;
	}
	final int maxDepth = max;

	m_cmm = new MethodMap() {
	    public int methodOrder(HMethod hm) { 
		return m_hci.getMethodOffset(hm); }
	};
	m_cdm = new ClassDepthMap() {
	    public int classDepth(HClass hc) { return m_hci.depth(hc); }
	    public int maxDepth() { return maxDepth; } 
	};
	m_fm  = new FieldMap() {
	    public int fieldOrder(HField hf) { 
		return m_hci.getFieldOffset(hf); }
	};
	m_imm = new InterfaceMethodMap
	    (new net.cscott.jutil.IteratorEnumerator(ch.classes().iterator()));
	m_nm  = nm;
    }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     *                                                           *
     *            Implementation of label mappings               *
     *                                                           *
     *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    public Object decodeLabel(Label label) {
	if (Debug.DEBUG) Debug.db("DECODING: " + label);
	for (Iterator i = m_labels.keySet().iterator(); i.hasNext();) {
	    Object next = i.next();
	    Label lNext = (Label)m_labels.get(next);
	    if (lNext.toString().equals(label.toString())) { 
		if (Debug.DEBUG) Debug.db("Returning: " + next);
		return next;
	    }
	}
	for (Iterator i = m_strings.keySet().iterator(); i.hasNext();) { 
	    Object next = i.next();
	    Label lNext = (Label)m_strings.get(next);
	    if (lNext.toString().equals(label.toString())) { 
		if (Debug.DEBUG) Debug.db("Returning: " + next);
		return next;
	    }
	}
	    
	throw new Error("Label not found in map: " + label);
    }
    
    public Label jlClass(HClass hc) { return null; } 

    /** Returns the label corresponding to the specified HClass */
    public Label label(HClass hc) { 
	if (!m_labels.containsKey(hc)) {
	    m_labels.put(hc, new Label(m_nm.mangle(hc)));
}
	return (Label)m_labels.get(hc);
    }
	    
    /** Returns the label corresponding to the specified static field */
    public Label label(HField hf) { 
	assert hf.isStatic();
	if (!m_labels.containsKey(hf)) {
	    m_labels.put(hf, new Label(m_nm.mangle(hf)));
	}
	return (Label)m_labels.get(hf);
    }

    /** Returns the label corrensponding to the specified method.  This
     *  method is not necessarily static */
    public Label label(HMethod hm) { 
	if (!m_labels.containsKey(hm)) {
	    m_labels.put(hm, new Label(m_nm.mangle(hm))); 
	}
	return (Label)m_labels.get(hm);
    }

    /** Returns the label corresponding to the specified String constant */
    public Label label(String stringConstant) { 
	if (!m_strings.containsKey(stringConstant)) {
	    m_strings.put(stringConstant, 
			  new Label(m_nm.mangle(stringConstant)));
	}
	return (Label)m_strings.get(stringConstant);
    }

    public Set stringConstants() { 
	return m_strings.keySet();
    }

    public Map stringConstantMap() { 
	return m_strings;
    }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     *                                                           *
     *            Implementation of offset methods               *
     *                                                           *
     *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/


    /** Returns the offset of the class pointer */
    public int clazzPtrOffset(HClass hc)   { 
	assert !hc.isPrimitive() : "" + hc;
	return -1;
    }

    /** If hc is an array type, returns the offset of its component
     *  type's class pointer */
    public int componentTypeOffset(HClass hc) { 
	assert hc.isArray();
	return -1;
    }

    /** Returns the size of the display information of the specified class */
    public int displaySize() {
	return m_cdm.maxDepth();
    }

    /** Returns the offset of the first array element if hc is an array
     *  type, otherwise generates an assertion failure */
    public int elementsOffset(HClass hc) { 
	assert hc.isArray();
	return 0; 
    }

    /** Returns the offset of the first field in an object of the
     *  specified type */
    public int fieldsOffset(HClass hc) { 
	assert (!hc.isPrimitive()) && (!hc.isArray());
	return 0;
    }

    /** Returns the offset of the hashcode of the specified object */
    public int hashCodeOffset(HClass hc) { 
	assert !hc.isPrimitive();
	return -2; 
    }

    /** If hc is a class type, or an interface, returns the offset from
     *  the class pointer of the pointer to implemented interfaces */
    public int interfaceListOffset(HClass hc) { 
	assert !hc.isPrimitive() && !hc.isArray();
	return -4;
    }

    /** If hc is an array type, returns the offset of its length field */
    public int lengthOffset(HClass hc) { 
	assert hc.isArray();
	return -3; 
    }

    /** Returns the offset from the class pointer of this class's pointer
     *  in the display */
    public int offset(HClass hc) { 
	assert !hc.isPrimitive() && !hc.isInterface();
	return m_cdm.classDepth(hc);
    }

    /** Returns the offset from the object reference of the specified 
     *  non-static field */
    public int offset(HField hf) { 
	assert !hf.isStatic();
	return m_fm.fieldOrder(hf);
    }

    /** Returns the offset from the class pointer of the specified
     *  non-static method */
    public int offset(HMethod hm) { 
	assert !hm.isStatic();
	HClass hc = hm.getDeclaringClass(); 
    
	if (hc.isInterface()) return -m_imm.methodOrder(hm) +
				  interfaceListOffset(hc) - 1;
	else return m_cmm.methodOrder(hm) + displaySize();
    }

    /** Returns the size of the specified class */
    public int size(HClass hc) { 
	int size;
	return 1;
    }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     *                                                           *
     *                    Utility methods                        *
     *                                                           *
     *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/


  private String getFieldSignature(HField hf)
    {
      String token = null;
      for (StringTokenizer st = new StringTokenizer(hf.toString());
	   st.hasMoreTokens();)
	{
	  token = st.nextToken();
	}
      return hf.getDeclaringClass() + "_" + token;
    }

    public int wordsize() { return 1; } 

    // stub for old FieldMap interface
    abstract class FieldMap {
	public abstract int fieldOrder(HField hf);
    }
}



