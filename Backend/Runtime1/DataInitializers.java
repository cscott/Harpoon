// DataInitializers.java, created Thu Oct 14 19:52:32 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HDataElement;
import harpoon.ClassFile.HInitializer;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.SEGMENT;
import harpoon.Temp.Label;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * <code>DataInitializers</code> outputs a table listing the
 * static initializers needed for the program, in the proper
 * dependency order.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DataInitializers.java,v 1.1.2.2 1999-10-15 00:47:14 cananian Exp $
 */
public class DataInitializers extends Data {
    final NameMap m_nm;
    
    /** Creates a <code>DataInitializers</code>. */
    public DataInitializers(Frame f, HClass hc, List staticInitializers) {
        super("static-initializers", hc, f);
	this.m_nm = f.getRuntime().nameMap;
	// only build one of these; wait until hc is java.lang.Object.
	this.root = (hc==HClass.forName("java.lang.Object")) ?
	    build(staticInitializers) : null;
    }
    private HDataElement build(List initMethods) {
	List stmlist = new ArrayList(initMethods.size()+3);
	stmlist.add(new SEGMENT(tf, null, SEGMENT.TEXT));
	stmlist.add(new LABEL(tf, null, new Label("_static_inits"), true));
	for (Iterator it=initMethods.iterator(); it.hasNext(); )
	    stmlist.add(_DATA(m_nm.label((HInitializer)it.next())));
	stmlist.add(_DATA(new CONST(tf, null))); // null-terminate the list
	return (HDataElement) Stm.toStm(stmlist);
    }
}
