// DataStrings.java, created Mon Oct 11 20:37:16 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Backend.Generic.Runtime.ObjectBuilder.ArrayInfo;
import harpoon.Backend.Generic.Runtime.ObjectBuilder.ObjectInfo;
import harpoon.Backend.Generic.Runtime.ObjectBuilder;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HDataElement;
import harpoon.ClassFile.HField;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.SEGMENT;
import harpoon.Temp.Label;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * <code>DataStrings</code> lays out string constant objects.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DataStrings.java,v 1.4 2002-08-06 20:01:32 cananian Exp $
 */
public class DataStrings extends Data {
    final NameMap m_nm;
    final ObjectBuilder m_ob;
    
    /** Creates a <code>DataStrings</code> containing tables corresponding
     *  to the given set of strings. */
    public DataStrings(Frame f, HClass hc, Set<String> strings) {
	super("string-data", hc, f);
	this.m_nm = f.getRuntime().getNameMap();
	this.m_ob = ((Runtime) f.getRuntime()).ob;
        this.root = build(strings);
    }
    private HDataElement build(Set<String> strings) {
	List<Stm> stmlist = new ArrayList<Stm>(strings.size()+1);
	for (Iterator<String> it=strings.iterator(); it.hasNext(); )
	    stmlist.add(buildOne(it.next()));
	return (HDataElement) Stm.toStm(stmlist);
    }
    private Stm buildOne(final String str) {
	final ArrayInfo charArray = new ArrayInfo() {
	    public HClass type() { return HCcharA; }
	    public Label  label() { return m_nm.label(str, "chararray"); }
	    public int length() { return str.length(); }
	    public Object get(int i) { return new Character(str.charAt(i)); }
	    final HClass HCcharA = linker.forDescriptor("[C");
	};
	final ObjectInfo strObject = new ObjectInfo() {
	    public HClass type() { return HCstr; }
	    public Label label() { return m_nm.label(str); }
	    public Object get(HField hf) {
		if (HFval.equals(hf)) return charArray;
		if (HFcnt.equals(hf)) return new Integer(str.length());
		// this test is different because size optimizations
		// may eliminate the 'offset' field as a compile-time
		// constant.
		if (hf.getName().equals("offset") &&
		    hf.getDeclaringClass().equals(HCstr))
			return new Integer(0);
		// special test for GNU classpath libraries.
		if (hf.getName().equals("cachedHashCode") &&
		    hf.getDeclaringClass().equals(HCstr))
		    return new Integer(str.hashCode());
		throw new Error("Unknown field "+hf+" of string object.");
	    }
	    final HClass HCstr = linker.forName("java.lang.String");
	    final HField HFval = HCstr.getField("value");
	    final HField HFcnt = HCstr.getField("count");
	};
	List stmlist = new ArrayList(4);
	stmlist.add(new SEGMENT(tf, null, SEGMENT.STRING_CONSTANTS));
	stmlist.add(m_ob.buildObject(tf, strObject, true));
	stmlist.add(new SEGMENT(tf, null, SEGMENT.STRING_DATA));
	stmlist.add(m_ob.buildArray (tf, charArray, false));
	return Stm.toStm(stmlist);
    }
}
