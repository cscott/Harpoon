// DataReflection2.java, created Sat Oct 16 15:48:14 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HDataElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.SEGMENT;
import harpoon.Temp.Label;

import java.util.ArrayList;
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
 * @version $Id: DataReflection2.java,v 1.1.2.1 1999-10-16 20:12:41 cananian Exp $
 */
public class DataReflection2 extends Data {
    final TreeBuilder m_tb;
    final NameMap m_nm;
    
    /** Creates a <code>DataReflection2</code>. */
    public DataReflection2(Frame f, HClass hc, ClassHierarchy ch) {
        super("reflection-data-2", hc, f);
	this.m_nm = f.getRuntime().nameMap;
	this.m_tb = (TreeBuilder) f.getRuntime().treeBuilder;
	this.root = build(hc, ch);
    }
    private HDataElement build(HClass hc, ClassHierarchy ch) {
	List stmlist = new ArrayList(4);
	stmlist.add(new SEGMENT(tf, null, SEGMENT.REFLECTION_DATA));
	stmlist.add(new LABEL(tf, null, m_nm.label(hc, "classinfo"), true));
	// first field: a claz structure pointer.
	stmlist.add(_DATA(m_nm.label(hc)));
	// next, a name string pointer.
	stmlist.add(_DATA(m_nm.label(hc, "namestr")));
	// scott's too lazy to add the rest at the moment...
	return (HDataElement) Stm.toStm(stmlist);
    }
}
