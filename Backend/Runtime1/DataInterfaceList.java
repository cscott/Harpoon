// DataInterfaceList.java, created Mon Oct 11 13:49:57 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HDataElement;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.SEGMENT;
import harpoon.Util.HClassUtil;
import net.cscott.jutil.UniqueVector;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * <code>DataInterfaceList</code> lays out the expanded list of interfaces.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DataInterfaceList.java,v 1.4 2004-02-08 03:20:58 cananian Exp $
 */
public class DataInterfaceList extends Data {
    final TreeBuilder m_tb;
    final NameMap m_nm;
    
    /** Creates a <code>DataInterfaceList</code>. */
    public DataInterfaceList(Frame f, HClass hc, ClassHierarchy ch) {
        super("ilist-data", hc, f);
	this.m_nm = f.getRuntime().getNameMap();
	this.m_tb = (TreeBuilder) f.getRuntime().getTreeBuilder();
	this.root = build(hc, ch);
    }

    private HDataElement build(HClass hc, ClassHierarchy ch) {
	List stmlist = new ArrayList();
	// write the appropriate segment header
	stmlist.add(new SEGMENT(tf, null, SEGMENT.CLASS));
	// word-align.
	stmlist.add(new ALIGN(tf, null, 4));
	// write the list label.
	stmlist.add(new LABEL(tf, null, m_nm.label(hc, "interfaces"), false));
	// okay, now collect all the interfaces that this class implements.
	List in = allInterfaces(hc);
	// add all interfaces of the component class.
	int dims = 0;
	while (hc.isArray()) {
	    hc = hc.getComponentType(); dims++;
	    for (Object hccO : allInterfaces(hc)) {
		HClass hcc = (HClass) hccO;
		in.add(HClassUtil.arrayClass(linker, hcc, dims));
	    }
	}
	// filter out those not in the class hierarchy.
	in.retainAll(ch.classes());
	// and make a list of stms.
	Set done = new HashSet();
	for (Object hccO : in) {
	    HClass hcc = (HClass) hccO;
	    if (done.add(hcc)) // if not already in done...
		stmlist.add(_DATUM(m_nm.label(hcc)));
	}
	// ...and null-terminate the list.
	stmlist.add(_DATUM(new CONST(tf, null)));
	return (HDataElement) Stm.toStm(stmlist);
    }

    private List allInterfaces(HClass hc) {
	// stolen from HClass.isSuperinterfaceOf
	UniqueVector uv = new UniqueVector();//unique in case of circularity 
	// seed with class hierarchy.
	for ( ; hc!=null; hc = hc.getSuperclass())
	    uv.addElement(hc);
	// extend with all interfaces implemented by each element.
	for (int i=0; i<uv.size(); i++) {
	    HClass in[] = ((HClass)uv.elementAt(i)).getInterfaces();
	    uv.addAll(Arrays.asList(in));
	}
	// now filter out those that aren't interfaces.
	List result = new ArrayList(uv.size());
	for (Object nhcO : uv) {
	    HClass nhc = (HClass) nhcO;
	    if (nhc.isInterface()) result.add(nhc);
	}
	return result;
    }
}
