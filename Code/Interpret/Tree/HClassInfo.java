// HClassInfo.java, created Wed Jan 13 17:09:19 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMember;
import harpoon.ClassFile.HMethod;
import harpoon.Util.HClassUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * The <code>HClassInfo</code> class provides various useful bits of 
 * information about an <code>HClass</code>. 
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: HClassInfo.java,v 1.2 2002-02-25 21:05:50 cananian Exp $
 * @see     harpoon.ClassFile.HClass
 */
class HClassInfo
{
    private HCIMap m_hcim = new HCIMap();

    /**
     * @return the depth of <code>hc</code> in the class hierarchy 
     */
    public int depth(HClass hc) {
	return m_hcim.get(hc).depth();
    }

    /**
     * @return the offset of of <code>hf</code> from the initial field.
     *         For instance, if <code>hf</code> is the 3rd field in its class, 
     *         <code>getFieldOffset</code> returns 2.
     */
    public int getFieldOffset(HField hf) {
	return m_hcim.get(hf.getDeclaringClass()).getOffset(hf);
    }

    /**
     * @return the offset of of <code>hm</code> from the initial method.
     *         For instance, if <code>hm</code> is the 3rd method in its class, 
     *         <code>getMethodOffset()</code> returns 2.
     */
    public int getMethodOffset(HMethod hm) {
	return m_hcim.get(hm.getDeclaringClass()).getOffset(hm);
    }

    /**
     * @return a String representation of this HClassInfo object
     */
    public String toString() { return m_hcim.toString(); }

    /**
     * @return a String representation of the information stored
     *         about <code>hc</code>
     */
    public String toString(HClass hc) { return m_hcim.get(hc).toString(); }

    final class HCIMap {
	private Map m_table = new HashMap();

	HCIUnit get(HClass hc) {
	    HCIUnit     hciu;
	    HClass      superclass;
	    HCIUnit     superclassInfo;
	
	    hciu = (HCIUnit)m_table.get(hc);
	    if (hciu == null) {
		if (hc.isArray())  { // Treat arrays differently
		    int    dims      = HClassUtil.dims(hc);
		    HClass baseclass = HClassUtil.baseClass(hc);
		    // hc is not primitive, so hc.getLinker() is safe.
		    superclass = 
			baseclass.isPrimitive() ? 
			hc.getLinker().forName("java.lang.Object")
			: (baseclass.getDescriptor()
			   .equals("Ljava/lang/Object;") ?
			   (HClassUtil.arrayClass(hc.getLinker(),
						  baseclass, dims-1))
			   : HClassUtil.arrayClass(hc.getLinker(),
						   baseclass.getSuperclass(),
						   dims));
		} 
		else { superclass = hc.getSuperclass(); } 
		if (superclass != null) {
		    superclassInfo = get(superclass);
		    hciu = new HCIUnit(hc, superclassInfo);
		}
		else {
		    hciu = new HCIUnit(hc);
		}
		m_table.put(hc, hciu);
	    }
	    return hciu;
	}
    
	public String toString() { return m_table.toString(); }
    }
}


class HCIUnit {

    private HMemberMap m_memberMap;
    private int        m_currentFieldOffset          = 0;
    private int        m_currentMethodOffset         = 0;
    private static int m_currentStaticFieldOffset    = 0;
    private int        m_depth;

    // Constructor used for classes extending another class
    // (i.e. anything other than class Object)
    HCIUnit(HClass hc, HCIUnit scInfo) {
	m_depth     = scInfo.depth() + 1;
	m_currentMethodOffset = scInfo.m_currentMethodOffset;
	m_currentFieldOffset  = scInfo.m_currentFieldOffset;
	m_memberMap = new HMemberMap(scInfo.m_memberMap);
	extend(hc);
    }
    
    // Constructor used for classes which don't extend anything
    // (Object)
    HCIUnit(HClass hc) {
	m_depth = 0;      
	m_memberMap = new HMemberMap();
	extend(hc);
    }

    int depth() { return m_depth; }

    int getOffset(HMember hm) { return m_memberMap.get(hm); }

    private void extend(HClass hc) {
	HField[] hFields = hc.getDeclaredFields();
	for (int i = 0; i < hFields.length; i++) {
	    if (hFields[i].isStatic())
		m_currentStaticFieldOffset =
		    m_memberMap.map(hFields[i], m_currentStaticFieldOffset);
	    else
		m_currentFieldOffset = 
		    m_memberMap.map(hFields[i], m_currentFieldOffset);
	}

	HMethod[] hMethods = hc.getDeclaredMethods();
	for (int i = 0; i < hMethods.length; i++) {
	    m_currentMethodOffset = 
		m_memberMap.map(hMethods[i], m_currentMethodOffset);
	}
    }

    public String toString() { return m_memberMap.toString(); }

    // Used to map HMethods and HFields to offsets
    final class HMemberMap {
	private Map m_table;

	HMemberMap() { m_table = new HashMap(); }

	HMemberMap(HMemberMap map) { m_table = new HashMap(map.m_table); } 

	int map(HField hf, int next) {
	    m_table.put(hf, new Integer(next++));
	    return next;
	}

	int map(HMethod hm, int next) {
	    int    offset;
	    String sig = getSignature(hm);
	
	    if (m_table.get(sig) == null) { 
		// Method does not override anything
		offset = next++;
	    }
	    else {
		HMethod scMethod = (HMethod)(m_table.get(sig));
		offset           = ((Integer)m_table.get(scMethod)).intValue();
	    }

	    m_table.put(sig, hm);
	    m_table.put(hm, new Integer(offset));

	    return next;
	}
    
	int get(HMember hm) {
	    return (m_table.containsKey(hm) ? 
		    ((Integer)m_table.get(hm)).intValue() : 
		    Integer.MIN_VALUE);
	}

	private String getSignature(HMethod hm) {
	    HClass[] paramTypes;
	    StringBuffer sb;
	
	    sb = new StringBuffer("");
	    sb.append(hm.getName());
	    paramTypes = hm.getParameterTypes();
	    for (int i = 0; i < paramTypes.length; i++)
		sb.append(paramTypes[i].toString());
	    return sb.toString();
	}
    
	public String toString() { return m_table.toString(); }
    }
}
