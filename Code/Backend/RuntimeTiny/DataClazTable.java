// DataClazTable.java, created Sun Mar 10 05:16:36 2002 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.RuntimeTiny;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.NameMap;
import harpoon.Backend.Runtime1.Data;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HDataElement;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.Tree;
import harpoon.Temp.Label;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * <code>DataClazTable</code> outputs an indirection table listing
 * all the claz structures used in the program.  This allows us
 * to use a (short) index into this table to dereference the
 * claz rather than a (long) direct pointer.  Note that this
 * table *only* contains *instantiated* types --- ie, claz
 * descriptors which could actually appear in the claz field
 * of an instantiated object.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DataClazTable.java,v 1.2 2002-04-10 03:03:43 cananian Exp $
 */
public class DataClazTable extends Data {
    final NameMap m_nm;

    /** Creates a <code>DataClazTable</code>. */
    public DataClazTable(Frame f, HClass hc,
			 ClassHierarchy ch, ClazNumbering cn) {
       super("claz-table", hc, f);
	this.m_nm = f.getRuntime().getNameMap();
	// only build one of these; wait until hc is java.lang.Object.
	this.root = (hc==f.getLinker().forName("java.lang.Object")) ?
	    build(cn,ch) : null;
    }
    private HDataElement build(final ClazNumbering cn, ClassHierarchy ch) {
	List<Stm> stmlist = new ArrayList<Stm>
	    (ch.instantiatedClasses().size()+3);
	stmlist.add(new SEGMENT(tf, null, SEGMENT.TEXT));
	stmlist.add(new ALIGN(tf, null, 4)); // word-align.
	stmlist.add(new LABEL(tf, null, new Label(m_nm.c_function_name
						  ("FNI_claz_table")),true));
	// make a list of all instantiated classes.
	List<HClass> all = new ArrayList<HClass>(ch.instantiatedClasses());
	for (Iterator<HClass> it=all.iterator(); it.hasNext(); ) {
	    HClass hc = it.next();
	    assert !hc.isInterface();
	}
	// sort these by clazNumber
	Collections.sort(all, new Comparator<HClass>() {
	    public int compare(HClass a, HClass b) {
		return cn.clazNumber(a) - cn.clazNumber(b);
	    }
	});
	int n=0;
	for (Iterator<HClass> it=all.iterator(); it.hasNext(); n++) {
	    HClass hc = it.next();
	    assert cn.clazNumber(hc)==n;
	    stmlist.add(_DATUM(m_nm.label(hc)));
	}
	// done.
	return (HDataElement) Stm.toStm(stmlist);
    }
}
