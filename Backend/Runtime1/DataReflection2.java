// DataReflection2.java, created Sat Oct 16 15:48:14 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HDataElement;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMember;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.SEGMENT;
import harpoon.Temp.Label;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
/**
 * <code>DataReflection2</code> generates class information tables
 * for each class, with lots of juicy information needed by JNI and
 * java language reflection.  The class information table includes:
 * <UL>
 *  <LI>A pointer to a UTF-8 encoded string naming the class.
 *  <LI>A pointer to the claz structure containing the dispatch
 *      tables & etc. (See <code>DataClaz</code>.)
 *  <LI>A sorted map of member signatures to method and field offsets.
 * </UL>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DataReflection2.java,v 1.1.2.3 1999-10-20 07:05:57 cananian Exp $
 */
public class DataReflection2 extends Data {
    final TreeBuilder m_tb;
    final NameMap m_nm;
    boolean pointersAreLong;
    
    /** Creates a <code>DataReflection2</code>. */
    public DataReflection2(Frame f, HClass hc, ClassHierarchy ch,
			   boolean pointersAreLong) {
        super("reflection-data-2", hc, f);
	this.m_nm = f.getRuntime().nameMap;
	this.m_tb = (TreeBuilder) f.getRuntime().treeBuilder;
	this.pointersAreLong = pointersAreLong;
	this.root = build(hc, ch);
    }
    private HDataElement build(HClass hc, ClassHierarchy ch) {
	List members = sortedMembers(hc);

	List stmlist = new ArrayList(6+7*members.size()/* at least*/);
	stmlist.add(new SEGMENT(tf, null, SEGMENT.REFLECTION_DATA));
	stmlist.add(new ALIGN(tf, null, 4)); // align table to word boundary
	stmlist.add(new LABEL(tf, null, m_nm.label(hc, "classinfo"), true));
	// first field: a claz structure pointer.
	stmlist.add(_DATA(m_nm.label(hc)));
	// next, a name string pointer.
	stmlist.add(_DATA(m_nm.label(hc, "namestr")));
	// pointer to the end of the lookup table.
	stmlist.add(_DATA(m_nm.label(hc, "classinfo_end")));
	// okay, now the sorted name, desc, offset table.
	for (Iterator it=members.iterator(); it.hasNext(); ) {
	    HMember hm = (HMember) it.next();
	    if (hm instanceof HMethod && !ch.callableMethods().contains(hm))
		continue; // skip uncallable methods.
	    stmlist.add(_DATA(memberLabel(hm, "namestr")));
	    stmlist.add(_DATA(memberLabel(hm, "descstr")));
	    if (!memberVirtual(hm))
		stmlist.add(_DATA(memberLabel(hm, null)));
	    else if (pointersAreLong)
		stmlist.add(_DATA(new CONST(tf, null,(long)memberOffset(hm))));
	    else
		stmlist.add(_DATA(new CONST(tf, null,(int) memberOffset(hm))));
	}
	// ok, mark the end of the table.
	stmlist.add(new LABEL(tf, null,
			      m_nm.label(hc, "classinfo_end"), false));
	// now make the actual string data bits.
	// (only for members we actually declare)
	for (Iterator it=members.iterator(); it.hasNext(); ) {
	    HMember hm = (HMember) it.next();
	    if (hm instanceof HMethod && !ch.callableMethods().contains(hm))
		continue; // skip uncallable methods.
	    // only make string data for the members we declare
	    if (hm.getDeclaringClass() != hc) continue;
	    // first name string.
	    stmlist.add(new LABEL(tf, null, memberLabel(hm,"namestr"), false));
	    emitString(stmlist, hm.getName());
	    // then descriptor string.
	    stmlist.add(new LABEL(tf, null, memberLabel(hm,"descstr"), false));
	    emitString(stmlist, hm.getDescriptor());
	}
	// pad out to full word after last string bit.
	stmlist.add(new ALIGN(tf, null, 4));
	// done, yay, whee.
	return (HDataElement) Stm.toStm(stmlist);
    }
    private boolean memberVirtual(HMember hmf) {
	if (hmf instanceof HField) return !((HField)hmf).isStatic();
	HMethod hm = (HMethod) hmf;
	if (hm instanceof HConstructor) return false;
	if (hm.isStatic()) return false;
	if (Modifier.isPrivate(hm.getModifiers())) return false;
	return true;
    }
    private Label memberLabel(HMember hm, String suffix) {
	if (hm instanceof HField)
	    return m_nm.label((HField)hm, suffix);
	else return m_nm.label((HMethod)hm, suffix);
    }
    private int memberOffset(HMember hmf) {
	if (hmf instanceof HField)
	    return  m_tb.cfm.fieldOffset((HField)hmf);
	HMethod hm = (HMethod) hmf;
	int off = hm.isInterfaceMethod() ?
	    m_tb.imm.methodOrder(hm) : m_tb.cmm.methodOrder(hm);
	return off * m_tb.POINTER_SIZE;
    }
    private void emitString(List stmlist, String str) {
	byte[] bytes = toUTF8(str);
	for (int i=0; i<bytes.length; i++)
	    stmlist.add(_DATA(new CONST(tf, null, 8, false,
					((int)bytes[i])&0xFF)));
	stmlist.add(_DATA(new CONST(tf, null, 8, false, 0))); // null-terminate
    }
    private List sortedMembers(HClass hc) {
	List members = new ArrayList(Arrays.asList(hc.getFields()));
	members.addAll(Arrays.asList(hc.getMethods()));
	Collections.sort(members, new Comparator() {
	    public int compare(Object o1, Object o2) {
		HMember hm1 = (HMember) o1, hm2 = (HMember) o2;
		int r = compareUTF8(hm1.getName(), hm2.getName());
		return (r!=0) ? r :
		    compareUTF8(hm1.getDescriptor(), hm2.getDescriptor());
	    }
	    private int compareUTF8(String s1, String s2) {
		byte[] b1 = toUTF8(s1), b2 = toUTF8(s2);
		for (int i=0; i<b1.length && i<b2.length; i++)
		    if (b1[i] != b2[i])
			// ack.  we want an unsigned comparison
			return (((int)b1[i])&0xFF) - (((int)b2[i])&0xFF);
		// okay, they're equal, up to minlen.
		return b1.length - b2.length;
	    }		
	});
	return Collections.unmodifiableList(members);
    }

    // STUB
    /** Make a java-style UTF-8 encoded byte array for a string. */
    public static byte[] toUTF8(String str) {
	return DataReflection1.toUTF8(str);
    }
}
