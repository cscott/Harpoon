// TreeDataFilter.java, created Fri Oct 15 14:13:17 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.Analysis.Transactions.BitFieldNumbering.BitFieldTuple;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Runtime.TreeBuilder;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HData;
import harpoon.ClassFile.HDataElement;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.TreeKind;
import harpoon.Temp.Label;
import net.cscott.jutil.FilterIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
/**
 * <code>TreeDataFilter</code> hacks through the field information tables
 * emitted by <code>Runtime1.DataReflection2</code> to add in additional
 * information about the bitfield-numbering of fields.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TreeDataFilter.java,v 1.4.2.1 2004-06-28 04:17:13 cananian Exp $
 */
public class TreeDataFilter extends FilterIterator.Filter<HData,HData> {
    final Frame f;
    final NameMap nm;
    final TreeBuilder tb;
    final BitFieldNumbering bfn;
    final Set<HField> transFields;
    final Map<Label,HField> label2field = new HashMap<Label,HField>();
    final boolean pointersAreLong;
    /** Creates a <code>DataInitializers</code>. */
    public TreeDataFilter(Frame f,
			  BitFieldNumbering bfn, Set<HField> transFields) {
	this.f = f; this.bfn = bfn;  this.transFields = transFields;
	this.nm = f.getRuntime().getNameMap();
	this.tb = f.getRuntime().getTreeBuilder();
	this.pointersAreLong = f.pointersAreLong();
	// map fields to field info labels.
	for (HField hf : transFields) {
	    Label l = nm.label(hf, "info");
	    label2field.put(l, hf);
	}
    }
    public HData map(HData d) {
	if (d instanceof harpoon.Backend.Runtime1.DataReflection2) {
	    // iterate through the Data, looking for the field info
	    // table.
	    int countdown=-1; HField which=null;
	    for (Stm s : linearize((Stm)d.getRootElement())
		) {
		if (s instanceof LABEL &&
		    label2field.containsKey(((LABEL)s).label)) {
		    // hey, we're getting close!  it's the fifth datum
		    // that we want to change, now.
		    which = label2field.get(((LABEL)s).label);
		    countdown=5;
		} else if (which!=null && s instanceof DATUM) {
		    countdown--;
		    if (countdown==0) { // this is the one to change.
			TreeFactory tf = s.getFactory();
			BitFieldTuple bft = bfn.bfLoc(which);
			Exp fldoffE = // get bitfield offset
			    tb.fieldOffset(tf, null, null, bft.field).unEx(tf);
			int data = 1 /* indicate data is valid */ +
			    2 * bft.bit /* bit # in bitfield */ +
			    128 * ((CONST)fldoffE).value.intValue();
			CONST c;
			if (pointersAreLong)
			    c = new CONST(tf, null, (long) data);
			else
			    c = new CONST(tf, null, (int) data);
			s.replace(new DATUM(tf, null, c));
			// done!
			which=null;
		    }
		}
	    }
	}
	return d;
    }
    // from Stm.java
    public static List<Stm> linearize(Stm stm) {
	List<Stm>  l = new ArrayList<Stm>();
	Stack<Stm> s = new Stack<Stm>();
	s.push(stm);

	while (!s.isEmpty()) {
	    Stm next = s.pop();
	    if (next.kind() == TreeKind.SEQ) {
		SEQ seq = (SEQ)next;
		s.push(seq.getRight());
		s.push(seq.getLeft());
	    }
	    else {
		l.add(next);
	    }
	}
	return l;
    }
}
