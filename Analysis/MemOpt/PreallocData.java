// PreallocData.java, created Sat Feb 22 12:12:38 2003 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MemOpt;

import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HDataElement;

import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.Stm;
import harpoon.Temp.Label;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;


/**
 * <code>PreallocData</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: PreallocData.java,v 1.1 2003-03-03 23:26:47 salcianu Exp $
 */
public class PreallocData extends harpoon.Backend.Runtime1.Data {
    
    /** Creates a <code>PreallocData</code>. */
    public PreallocData(HClass hc, Frame f, Set/*<Label>*/ labels,
			Label beginLabel, Label endLabel) {
	super("prealloc-data", hc, f);
	// only emit this once
	assert hc == f.getLinker().forName("java.lang.Object") :
	    "Pointers to preallocated memory should be stored only " + 
	    "in the data segment for java.lang.Object";
	this.root = build(labels, beginLabel, endLabel);
    }


    private HDataElement build(Set/*<Label>*/ labels,
			       Label beginLabel, Label endLabel) {
	boolean first = true;

	List stmList = new ArrayList();
	stmList.add(new SEGMENT(tf, null, SEGMENT.INIT_DATA));

	for(Iterator it = labels.iterator(); it.hasNext(); ) {
	    Label label = (Label) it.next();
	    stmList.add(new ALIGN(tf, null, 4)); // word align
	    // mark the beginning of the prealloc data segment
	    if(first)
		stmList.add(new LABEL(tf, null, beginLabel, true));
	    // reserve space for one pointer; referred by "label"
	    stmList.add(new LABEL(tf, null, label, true));
	    stmList.add(new DATUM(tf, null, new CONST(tf, null)));
	    first = false;
	}

	// mark the end of the prealloc data segment
	stmList.add(new LABEL(tf, null, endLabel, true));

	return (HDataElement) Stm.toStm(stmList);
    }
}
