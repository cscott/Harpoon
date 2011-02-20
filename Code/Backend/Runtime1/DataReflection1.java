// DataReflection1.java, created Sat Oct 16 13:43:17 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Runtime.ObjectBuilder.ObjectInfo;
import harpoon.Backend.Generic.Runtime.ObjectBuilder;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HDataElement;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.SEGMENT;
import harpoon.Temp.Label;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
/**
 * <code>DataReflection1</code> creates tables which the JNI interface
 * will use.  This includes:<OL>
 * <LI>a table to map class names to <code>java.lang.Class</code> objects,
 *     sorted in order of the UTF-8 encodings of the class names.
 *     (begins at <code>name2class_start</code>, ends at
 *      <code>name2class_end</code>)
 * <LI>a table to map <code>java.lang.Class</code> objects to class
 *     information structures, sorted in order of the (non-relocatable)
 *     <code>Class</code> object address.
 *     (begins at <code>class2info_start</code>, ends at
 *      <code>class2info_end</code>)
 * <LI>UTF-8 encoded class name strings, used by the first table as
 *     well as by the class information structures.
 * <LI>Static <Code>java.lang.Class</code> objects.  As the JDK dictates,
 *     these contain no actual class data; however, table #2 above can
 *     be keyed by the object address to fetch the actual class information
 *     structures.
 * </OL>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DataReflection1.java,v 1.4 2003-07-04 03:41:54 cananian Exp $
 */
public class DataReflection1 extends Data {
    final NameMap m_nm;
    final ObjectBuilder m_ob;
    
    /** Creates a <code>DataReflection1</code>. */
    public DataReflection1(Frame f, HClass hc, ClassHierarchy ch) {
        super("reflection-data-1", hc, f);
	this.m_nm = f.getRuntime().getNameMap();
	this.m_ob = ((Runtime) f.getRuntime()).ob;
	// only build one of these (so we can make sure the
	// table is properly sorted); wait until hc is
	// java.lang.Object
	this.root = (hc==linker.forName("java.lang.Object")) ?
	    build(ch) : null;
    }
    private HDataElement build(ClassHierarchy ch) {
	// okay, some preliminaries first: build properly sorted list of
	// classes.
	List sorted = new ArrayList(ch.classes());
	Collections.sort(sorted, new Comparator() {
	    public int compare(Object o1, Object o2) {
		// compare two classes by ordering the UTF-8 encodings of
		// their names.
		byte[] b1 = toUTF8(((HClass)o1).getName().replace('.','/'));
		byte[] b2 = toUTF8(((HClass)o2).getName().replace('.','/'));
		for (int i=0; i<b1.length && i<b2.length; i++)
		    if (b1[i] != b2[i])
			// ack.  we want an unsigned comparison
			return (((int)b1[i])&0xFF) - (((int)b2[i])&0xFF);
		// okay, they're equal, up to minlen.
		return b1.length - b2.length;
	    }
	});
	// yay, team.  we've got a properly sorted list.

	List stmlist = new ArrayList(6);
	// change to table data segment.
	stmlist.add(new SEGMENT(tf, null, SEGMENT.REFLECTION_DATA));
	// build class name string -> class object table.
	stmlist.add(buildStr2Class(sorted));
	// build class object -> class info table.
	stmlist.add(buildClass2Info(sorted));
	// build class name strings
	stmlist.add(buildStrings(sorted));
	// change to object data segment.
	stmlist.add(new SEGMENT(tf, null, SEGMENT.REFLECTION_OBJECTS));
	// build actual class objects
	stmlist.add(buildClassObjects(sorted));
	// yay, done.
	return (HDataElement) Stm.toStm(stmlist);
    }
    private Stm buildStr2Class(List sorted) {
	Label ls, le;
	List stmlist = new ArrayList(3+2*sorted.size());
	// make a sorted table mapping name strings to class objects.
	stmlist.add(new ALIGN(tf, null, 8)); // align table to double-word boundary
	stmlist.add(new LABEL(tf, null,
			      ls = new Label(m_nm.c_function_name
					     ("name2class_start")), true));
	for (Iterator it=sorted.iterator(); it.hasNext(); ) {
	    HClass hc = (HClass) it.next();
	    stmlist.add(_DATUM(m_nm.label(hc, "namestr")));
	    stmlist.add(_DATUM(m_nm.label(hc, "classobj")));
	}
	stmlist.add(new LABEL(tf, null,
			      le = new Label(m_nm.c_function_name
					     ("name2class_end")), true));
	// add two bogus words to keep gcc from putting class2info_end into
	// the BSS segment when compiling with PreciseC and NO_SECTION_SUPPORT
	stmlist.add(_DATUM(ls));
	stmlist.add(_DATUM(le));
	// okay, done now.
	return Stm.toStm(stmlist);
    }
    private Stm buildClass2Info(List sorted) {
	Label ls, le;
	List stmlist = new ArrayList(3+2*sorted.size());
	// make a sorted table mapping class objects to class info structures.
	stmlist.add(new ALIGN(tf, null, 4)); // align table to word boundary
	stmlist.add(new LABEL(tf, null,
			      ls = new Label(m_nm.c_function_name
					     ("class2info_start")), true));
	for (Iterator it=sorted.iterator(); it.hasNext(); ) {
	    HClass hc = (HClass) it.next();
	    stmlist.add(_DATUM(m_nm.label(hc, "classobj")));
	    stmlist.add(_DATUM(m_nm.label(hc, "classinfo")));
	}
	stmlist.add(new LABEL(tf, null,
			      le = new Label(m_nm.c_function_name
					     ("class2info_end")), true));
	// add two bogus words to keep gcc from putting class2info_end into
	// the BSS segment when compiling with PreciseC and NO_SECTION_SUPPORT
	stmlist.add(_DATUM(ls));
	stmlist.add(_DATUM(le));
	// okay, done now.
	return Stm.toStm(stmlist);
    }
    private Stm buildStrings(List sorted) {
	List stmlist = new ArrayList(1+2*sorted.size());
	// build actual c-style string data from UTF-8 encoded class name
	for (Iterator it=sorted.iterator(); it.hasNext(); ) {
	    HClass hc = (HClass) it.next();
	    stmlist.add(new LABEL(tf, null, m_nm.label(hc, "namestr"), true));
	    stmlist.add(emitUtf8String(hc.getName().replace('.','/')));
	}
	// pad out to a full word after the last byte.
	stmlist.add(new ALIGN(tf, null, 4));
	return Stm.toStm(stmlist);
    }
    private Stm buildClassObjects(List sorted) {
	List stmlist = new ArrayList(sorted.size());
	final HClass HCclass = linker.forName("java.lang.Class");
	for (Iterator it=sorted.iterator(); it.hasNext(); ) {
	    final HClass hc = (HClass) it.next();
	    // make an ObjectInfo -- that doesn't actual provide any info.
	    ObjectInfo info = new ObjectInfo() {
		public HClass type() { return HCclass; }
		public Label label() { return m_nm.label(hc, "classobj"); }
		public Object get(HField hf) {
		    if (hf.getDeclaringClass().equals(HCclass) &&
			(hf.getName().equals("signers") ||
			 hf.getName().equals("pd")))
			return null;
		    throw new Error("Unknown field of Class object: "+hf);
		}
	    };
	    stmlist.add(m_ob.buildObject(tf, info, true));
	}
	return Stm.toStm(stmlist);
    }
}
