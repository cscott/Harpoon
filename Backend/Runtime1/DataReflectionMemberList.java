// DataReflectionMemberList.java, created Sat Oct 16 13:43:17 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Runtime.ObjectBuilder.ObjectInfo;
import harpoon.Backend.Generic.Runtime.ObjectBuilder;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
/**
 * <code>DataReflectionMemberList</code> creates tables which the JNI interface
 * will use.  This includes:<OL>
 * <LI>a table to map <code>java.lang.reflect.Field</code> objects to field
 *     information structures, sorted in order of the (non-relocatable)
 *     <code>Field</code> object address.
 *     (begins at <code>field2info_start</code>, ends at
 *      <code>field2info_end</code>)
 * <LI>a table to map <code>java.lang.reflect.Method</code> objects to method
 *     information structures, sorted in order of the (non-relocatable)
 *     <code>Method</code> object address.
 *     (begins at <code>method2info_start</code>, ends at
 *      <code>method2info_end</code>)
 * <LI>Static <Code>java.lang.reflect.Field</code> objects.  As the JDK
 *     dictates, these contain no actual field data; however, table #1 above
 *     is keyed by the object address to fetch the actual field information
 *     structures.
 * <LI>Static <Code>java.lang.reflect.Method</code> objects.  As the JDK
 *     dictates, these contain no actual method data; however, table #2 above
 *     is keyed by the object address to fetch the actual method information
 *     structures.
 * </OL>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DataReflectionMemberList.java,v 1.3 2003-07-04 03:41:54 cananian Exp $
 */
public class DataReflectionMemberList extends Data {
    final NameMap m_nm;
    final ObjectBuilder m_ob;
    
    /** Creates a <code>DataReflectionMemberList</code>. */
    public DataReflectionMemberList(Frame f, HClass hc, ClassHierarchy ch) {
        super("reflection-data-method-list", hc, f);
	this.m_nm = f.getRuntime().getNameMap();
	this.m_ob = ((Runtime) f.getRuntime()).ob;
	// only build one of these (so we can make sure the
	// table is properly sorted); wait until hc is
	// java.lang.Object
	this.root = (hc==linker.forName("java.lang.Object")) ?
	    build(ch) : null;
    }
    private HDataElement build(ClassHierarchy ch) {
	// okay, some preliminaries first: build ordered list of
	// methods.
	List orderedMethods = new ArrayList(ch.callableMethods());
	List orderedFields = new ArrayList();
	for (Iterator it=ch.classes().iterator(); it.hasNext(); ) {
	    HClass hc = (HClass) it.next();
	    orderedFields.addAll(Arrays.asList(hc.getDeclaredFields()));
	}

	List stmlist = new ArrayList(6);
	// change to table data segment.
	stmlist.add(new SEGMENT(tf, null, SEGMENT.REFLECTION_DATA));
	// build field object -> field info table.
	stmlist.add(buildMember2Info(orderedFields, false));
	// build method object -> method info table.
	stmlist.add(buildMember2Info(orderedMethods, true));
	// change to object data segment.
	stmlist.add(new SEGMENT(tf, null, SEGMENT.REFLECTION_OBJECTS));
	// build actual field objects
	stmlist.add(buildMemberObjects(orderedFields));
	// build actual constructor/method objects
	stmlist.add(buildMemberObjects(orderedMethods));
	// yay, done.
	return (HDataElement) Stm.toStm(stmlist);
    }
    private Label memberLabel(HMember hm, String suffix) {
	if (hm instanceof HField)
	    return m_nm.label((HField)hm, suffix);
	else return m_nm.label((HMethod)hm, suffix);
    }
    private Stm buildMember2Info(List ordered, boolean membersAreMethods) {
	String member = membersAreMethods ? "method" : "field";
	List stmlist = new ArrayList(3+2*ordered.size());
	// make a ordered table mapping Field objects to field info structures.
	stmlist.add(new ALIGN(tf, null, 4)); // align table to word boundary
	stmlist.add(new LABEL(tf, null,
			      new Label(m_nm.c_function_name
					(member+"2info_start")), true));
	for (Iterator it=ordered.iterator(); it.hasNext(); ) {
	    HMember hm = (HMember) it.next();
	    stmlist.add(new LABEL(tf, null,
				  memberLabel(hm, "reflectinfo"), true));
	    stmlist.add(_DATUM(memberLabel(hm, "obj")));
	    stmlist.add(_DATUM(memberLabel(hm, "info")));
	    stmlist.add(_DATUM(m_nm.label(hm.getDeclaringClass(),"classobj")));
	    stmlist.add(_DATUM(new CONST(tf, null, hm.getModifiers())));
	}
	stmlist.add(new LABEL(tf, null,
			      new Label(m_nm.c_function_name
					(member+"2info_end")), true));
	// We need to put something after the label to keep gcc from
	// moving <xxx>2info_end into the .bss section when compiling with
	// the PreciseC backend.
	stmlist.add(_DATUM(new Label(m_nm.c_function_name
				     (member+"2info_end"))));
	// okay, done.
	return Stm.toStm(stmlist);
    }
    private Stm buildMemberObjects(List ordered) {
	List stmlist = new ArrayList(ordered.size());
	final HClass HCclass = linker.forName("java.lang.Class");
	for (Iterator it=ordered.iterator(); it.hasNext(); ) {
	    final HMember hm = (HMember) it.next();
	    final HClass type = linker.forName
		("java.lang.reflect." +
		 (hm instanceof HField ? "Field" :
		  hm instanceof HConstructor ? "Constructor" : "Method"));
	    // make an ObjectInfo -- that doesn't actual provide any info.
	    ObjectInfo info = new ObjectInfo() {
		public HClass type() { return type; }
		public Label label() { return memberLabel(hm, "obj"); }
		public Object get(HField hf) {
		    if (hf.equals(HFclazz)) {
			final HClass hc = hf.getDeclaringClass();
			return new ObjectInfo() {
			    public HClass type() { return HCclass; }
			    public Label label() {
				return m_nm.label(hc, "classobj");
			    }
			    public Object get(HField hff) {
				throw new Error("Not building this object");
			    }
			};
		    }
		    //XXX: Methods and Field objects have lots of fields.
		    //we basically zero-fill everything.
		    if (hf.equals(HFslot)) return new Integer(0);
		    return null;
		}
		HField HFclazz = null, HFslot = null;
		{
		    // our hacked reflection classes don't have these fields,
		    // don't have a cow.
		    try {
			HFclazz=type.getField("clazz");
			HFslot=type.getField("slot");
		    } catch (NoSuchFieldError e) { /* ignore */ }
		}
	    };
	    stmlist.add(m_ob.buildObject(tf, info, true));
	}
	return Stm.toStm(stmlist);
    }
}
