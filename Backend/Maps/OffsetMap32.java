package harpoon.Backend.Maps;

import harpoon.Analysis.InterfaceMethodMap;
import harpoon.Analysis.QuadSSA.ClassHierarchy;
import harpoon.Backend.Analysis.DisplayInfo.HClassInfo;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * The OffsetMap32 class implements the abstract methods of OffsetMap,
 * specializing them for 32-bit architectures.
 *
 * @author   Duncan Bryce <duncan@lcs.mit.edu>
 * @version  $Id: OffsetMap32.java,v 1.1.2.9 1999-05-17 19:58:37 andyb Exp $
 */
public class OffsetMap32 extends OffsetMap
{
    private static int WORDSIZE = 4;

    private ClassDepthMap       m_cdm;   
    private FieldMap            m_fm;     
    private Hashtable           m_fields; // Cache of field-orderings
    private Hashtable           m_labels; // Cache of label mappings
    private HClassInfo          m_hci;
    private InterfaceMethodMap  m_imm; 
    private MethodMap           m_cmm;

    /** Class constructor */
    public OffsetMap32(ClassHierarchy ch) {
	// Util.assert(ch!=null);
    
	m_cmm     = new MethodMap() {
	    public int methodOrder(HMethod hm) { 
		return m_hci.getMethodOffset(hm); 
	    }
	};
	m_cdm     = new ClassDepthMap() {
	    public int classDepth(HClass hc) { return m_hci.depth(hc); }
	    public int maxDepth() { throw new Error("Not impl:  maxDepth()"); }
	};
	m_fm      = new FieldMap() {
	    public int fieldOrder(HField hf) { 
		return m_hci.getFieldOffset(hf); 
	    }
	};
	m_fields  = new Hashtable();
	m_labels  = new Hashtable();
	m_hci     = new HClassInfo();
	// m_imm     = new InterfaceMethodMap(ch.classes());
    }


    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     *                                                           *
     *              Implementation of type tags                  *
     *                                                           *
     *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/


    public int arrayTag()     { return 0; }
    public int classTag()     { return 1; }
    public int interfaceTag() { return 2; }
    public int primitiveTag() { return 3; }


    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     *                                                           *
     *            Implementation of label mappings               *
     *                                                           *
     *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/


    /** Returns the label corresponding to the specified HClass */
    public Label label(HClass hc) { 
	if (m_labels.containsKey(hc)) {
	    return (Label)m_labels.get(hc);
	}
	else {
	    Label newLabel; StringBuffer sb;
	    sb = new StringBuffer("CLASS_");
	    sb.append(hc.getName());
	    newLabel = new Label(sb.toString());
	    m_labels.put(hc, newLabel);
	    return newLabel;
	}
    }
	    
    /** Returns the label corresponding to the specified static field */
    public Label label(HField hf) { 
	Util.assert(hf.isStatic());

	if (m_labels.containsKey(hf)) {
	    return (Label)m_labels.get(hf);
	}
	else {
	    Label newLabel; StringBuffer sb;
	    sb = new StringBuffer("S_FIELD_");
	    sb.append(getFieldSignature(hf));
	    newLabel = new Label(sb.toString());
	    m_labels.put(hf, newLabel);
	    return newLabel;
	}
    }

    /** Returns the label corrensponding to the specified method.  This
     *  method is not necessarily static */
    public Label label(HMethod hm) { 
	if (m_labels.containsKey(hm)) {
	    return (Label)m_labels.get(hm);
	}
	else {
	    Label newLabel; StringBuffer sb;
	    sb = new StringBuffer("METHOD_");
	    sb.append(getMethodSignature(hm));
	    newLabel = new Label(sb.toString());
	    m_labels.put(hm, newLabel);
	    return newLabel;
	}
    }

    /** Returns the label corresponding to the specified String constant */
    public Label label(String stringConstant) { 
	if (m_labels.containsKey(stringConstant)) {
	    return (Label)m_labels.get(stringConstant);
	}
	else {
	    Label newLabel; StringBuffer sb;
	    sb = new StringBuffer("STRING_CONST_");
	    sb.append(stringConstant);
	    newLabel = new Label(sb.toString());
	    m_labels.put(stringConstant, newLabel);
	    return newLabel;
	}
    }

    
    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     *                                                           *
     *            Implementation of offset methods               *
     *                                                           *
     *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/


    /** Returns the offset of the class pointer */
    public int classOffset(HClass hc)   { 
	Util.assert(!hc.isPrimitive());
	return -1 * WORDSIZE;
    }

    /** If hc is an array type, returns the offset of its component
     *  type's class pointer */
    public int componentTypeOffset(HClass hc) { 
	Util.assert(hc.isArray());
	return -1 * WORDSIZE;
    }

    /** Returns the offset from the class pointer of this class's pointer
     *  in the display */
    public int displayOffset(HClass hc) { 
	Util.assert(!hc.isPrimitive() && !hc.isInterface());
	return m_cdm.classDepth(hc) * WORDSIZE;
    }

    /** Returns the size of the display information of the specified class */
    public int displaySize(HClass hc) {
	Util.assert(!hc.isPrimitive() && !hc.isInterface());
	
	// Arrays always extend _only_ Object
	return hc.isArray()?2*WORDSIZE:64*WORDSIZE;
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
	return -3 * WORDSIZE;
    }

    /** If hc is an array type, returns the offset of its length field */
    public int lengthOffset(HClass hc) { 
	Util.assert(hc.isArray());
	return -3 * WORDSIZE; 
    }

    /** Returns the offset from the object reference of the specified 
     *  non-static field */
    public int offset(HField hf) {
	Util.assert(!hf.isStatic());

	HClass    hc;
	HField[]  fields, orderedFields;
	int       fieldOrder, offset;
    
	hc = hf.getDeclaringClass();
	if (!m_fields.containsKey(hc)) {
	    fields         = hc.getDeclaredFields();
	    orderedFields  = new HField[fields.length];
	    for (int i=0; i<fields.length; i++) 
		orderedFields[m_fm.fieldOrder(fields[i])] = fields[i];
	    // Cache field ordering
	    m_fields.put(hc, orderedFields);
	}
	else 
	    orderedFields  = (HField[])m_fields.get(hc);

	fieldOrder = m_fm.fieldOrder(hf);
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
    
	if (hc.isInterface()) return (-m_imm.methodOrder(hm) - 4) * WORDSIZE;
	else return (m_cmm.methodOrder(hm) + displaySize(hc)) * WORDSIZE;
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
	    throw new Error("Size of array does not depend on its class!");
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

    /** Returns the offset from the class pointer of the tag 
     *  specifying the type of data hc is */
    public int tagOffset(HClass hc) { 
	return -2;
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

    private String getMethodSignature(HMethod hm)
	{
	    HClass[] paramTypes;
	    StringBuffer sb = new StringBuffer("");
      
	    paramTypes = hm.getParameterTypes();
	    for (int i = 0; i < paramTypes.length; i++)
		{
		    sb.append(paramTypes[i].toString());
		    sb.append("_");
		}
	    sb.append(hm.getDeclaringClass().getName());
	    sb.append("_");
	    sb.append(hm.getName());
      
	    return sb.toString().replace(' ', '_');

	}

}
