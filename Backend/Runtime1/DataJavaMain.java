// DataJavaMain.java, created Fri Oct 15 14:13:17 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HDataElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.SEGMENT;
import harpoon.Temp.Label;

import java.util.ArrayList;
import java.util.List;
/**
 * <code>DataJavaMain</code> outputs a labeled string pointer ("FNI_javamain")
 * which tells the runtime with which method to begin execution of
 * this java program.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DataJavaMain.java,v 1.1.2.5 2000-01-10 05:08:35 cananian Exp $
 */
public class DataJavaMain extends Data {
    final NameMap m_nm;
    
    /** Creates a <code>DataInitializers</code>. */
    public DataJavaMain(Frame f, HClass hc, HMethod main) {
        super("java-main", hc, f);
	this.m_nm = f.getRuntime().nameMap;
	// only build one of these; wait until hc is java.lang.Object.
	this.root = (hc==HClass.forName("java.lang.Object")) ?
	    build(main) : null;
    }
    private HDataElement build(HMethod main) {
	List stmlist = new ArrayList(4);
	stmlist.add(new SEGMENT(tf, null, SEGMENT.TEXT));
	stmlist.add(new ALIGN(tf, null, 4)); // word align.
	stmlist.add(new LABEL(tf, null, new Label("_FNI_javamain"), true));
	stmlist.add(_DATUM(m_nm.label(main.getDeclaringClass(), "namestr")));
	return (HDataElement) Stm.toStm(stmlist);
    }
}
