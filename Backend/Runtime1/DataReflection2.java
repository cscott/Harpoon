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
 *  <LI>The java access modifiers of the class.
 *  <LI>A sorted map of member signatures to method and field offsets.
 * </UL>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DataReflection2.java,v 1.6 2004-02-08 03:20:58 cananian Exp $
 */
public class DataReflection2 extends Data {
    final TreeBuilder m_tb;
    final NameMap m_nm;
    final boolean pointersAreLong;
    
    /** Creates a <code>DataReflection2</code>. */
    public DataReflection2(Frame f, HClass hc, ClassHierarchy ch) {
        super("reflection-data-2", hc, f);
	this.m_nm = f.getRuntime().getNameMap();
	this.m_tb = (TreeBuilder) f.getRuntime().getTreeBuilder();
	this.pointersAreLong = f.pointersAreLong();
	this.root = build(hc, ch);
    }
    private HDataElement build(HClass hc, ClassHierarchy ch) {
	List members = sortedMembers(hc);
	int ptrsz = pointersAreLong ? 8 : 4;

	List stmlist = new ArrayList(6+7*members.size()/* at least*/);
	stmlist.add(new SEGMENT(tf, null, SEGMENT.REFLECTION_DATA));
	stmlist.add(new ALIGN(tf, null, ptrsz));// align table to word boundary
	stmlist.add(new LABEL(tf, null, m_nm.label(hc, "classinfo"), true));
	// first field: a claz structure pointer.
	stmlist.add(_DATUM(m_nm.label(hc)));
	// next, a name string pointer.
	stmlist.add(_DATUM(m_nm.label(hc, "namestr")));
	// the java access modifiers for this class
	stmlist.add(_DATUM(new CONST(tf, null, hc.getModifiers())));
	// padding for 64-bit platforms.
	if (pointersAreLong)
	    stmlist.add(_DATUM(new CONST(tf, null, 0)));
	// pointer to the end of the lookup table.
	stmlist.add(_DATUM(m_nm.label(hc, "classinfo_end")));
	// okay, now the sorted name, desc, offset table.
	for (Object hmO : members) {
	    HMember hm = (HMember) hmO;
	    if (hm instanceof HMethod && !ch.callableMethods().contains(hm))
		continue; // skip uncallable methods.
	    if (hm.getDeclaringClass().equals(hc))//member info for decl'd only
		stmlist.add(new LABEL(tf, null, memberLabel(hm,"info"), true));
	    stmlist.add(_DATUM(memberLabel(hm, "namestr")));
	    stmlist.add(_DATUM(memberLabel(hm, "descstr")));
	    stmlist.add(_DATUM(memberLabel(hm, "reflectinfo")));
	    if (!memberVirtual(hm))
		stmlist.add(_DATUM(memberLabel(hm, null)));
	    else if (pointersAreLong)
		stmlist.add(_DATUM(new CONST(tf, null,(long)memberOffset(hm))));
	    else
		stmlist.add(_DATUM(new CONST(tf, null,(int) memberOffset(hm))));
	    // count number of argument words for methods
	    emitNargs(stmlist, hm);
	}
	// ok, mark the end of the table.
	stmlist.add(new LABEL(tf, null,
			      m_nm.label(hc, "classinfo_end"), false));
	// We need to put something after the label to keep gcc from
	// moving classinfo_end into the .bss section when compiling with
	// the PreciseC backend.
	stmlist.add(_DATUM(m_nm.label(hc, "classinfo_end")));
	// now make the actual string data bits.
	// (only for members we actually declare)
	for (Object hmO : members) {
	    HMember hm = (HMember) hmO;
	    if (hm instanceof HMethod && !ch.callableMethods().contains(hm))
		continue; // skip uncallable methods.
	    // only make string data for the members we declare
	    if (hm.getDeclaringClass() != hc) continue;
	    // first name string.
	    stmlist.add(new LABEL(tf, null, memberLabel(hm,"namestr"), true));
	    stmlist.add(emitUtf8String(hm.getName()));
	    // then descriptor string.
	    stmlist.add(new LABEL(tf, null, memberLabel(hm,"descstr"), true));
	    stmlist.add(emitUtf8String(hm.getDescriptor()));
	}
	// pad out to full word after last string bit.
	stmlist.add(new ALIGN(tf, null, ptrsz));
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
    /* some methods are both defined in interfaces *and* inherited from
     * java.lang.Object.  Use the java.lang.Object version. */
    private HMethod remap(HMethod hm) {
	try {
	    return linker.forName("java.lang.Object")
		.getMethod(hm.getName(), hm.getDescriptor());
	} catch (NoSuchMethodError nsme) {
	    return hm;
	}
    }
    private int memberOffset(HMember hmf) {
	if (hmf instanceof HField)
	    return  m_tb.OBJ_FZERO_OFF + m_tb.cfm.fieldOffset((HField)hmf);
	HMethod hm = remap((HMethod) hmf);
	if (hm.isInterfaceMethod())
	    return m_tb.CLAZ_INTERFACES_OFF -
		m_tb.POINTER_SIZE * m_tb.imm.methodOrder(hm);
	// virtual method
	return m_tb.CLAZ_METHODS_OFF +
	    m_tb.POINTER_SIZE * m_tb.cmm.methodOrder(hm);
    }
    private void emitNargs(List stmlist, HMember hmf) {
	final int REGS_PER_WORD = 1;
	final int REGS_PER_DOUBLEWORD = (pointersAreLong) ? 1 : 2;
	final int REGS_PER_POINTER = 1;

	int nargs=0;
	if (hmf instanceof HMethod) {
	    if (!((HMethod)hmf).isStatic()) nargs+=REGS_PER_POINTER;
	    String desc=hmf.getDescriptor();
	    for (int i=1; desc.charAt(i)!=')'; i++) { // skip leading '('
		switch (desc.charAt(i)) {
		case 'B': case 'C': case 'F': case 'I': case 'S': case 'Z':
		    nargs+=REGS_PER_WORD; break;
		case 'J': case 'D':
		    nargs+=REGS_PER_DOUBLEWORD; break;
		case 'L':
		    nargs+=REGS_PER_POINTER;
		    i=desc.indexOf(';', i); break;
		case '[':
		    nargs+=REGS_PER_POINTER;
		    do { i++; } while (desc.charAt(i)=='[');
		    if (desc.charAt(i)=='L') i=desc.indexOf(';',i);
		    break;
		default:
		    throw new Error("Illegal signature: "+desc);
		}
	    }
	}
	// output nargs value.
	if (pointersAreLong)
	    stmlist.add(_DATUM(new CONST(tf, null, (long) nargs)));
	else
	    stmlist.add(_DATUM(new CONST(tf, null, (int) nargs)));
    }
    private List sortedMembers(HClass hc) {
	List members = new ArrayList(Arrays.asList(hc.getFields()));
	members.addAll(Arrays.asList(hc.getMethods()));
	// add back private members of superclasses, which
	// getFields()/getMethods() omit. [CSA FIX: 3-28-00]
	for (HClass hcp=hc.getSuperclass();hcp!=null;hcp=hcp.getSuperclass()) {
	    List l = new ArrayList(Arrays.asList(hcp.getDeclaredFields()));
	    l.addAll(Arrays.asList(hcp.getDeclaredMethods()));
	    for (Object hmO : l) {
		HMember hm = (HMember) hmO;
		if (Modifier.isPrivate(hm.getModifiers()))
		    members.add(hm);
	    }
	}
	// add back 'length' field of arrays, which getFields() omits.
	// [CSA FIX: 11-14-00]
	if (hc.isArray()) members.add(hc.getDeclaredField("length"));
	// okay, now sort members.
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
}
