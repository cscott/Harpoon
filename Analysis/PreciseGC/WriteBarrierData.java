// WriteBarrierData.java, created Thu Aug 30 16:49:28 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HDataElement;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.Stm;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>WriteBarrierData</code> generates the static data needed
 * to gather dynamic statistics about write barriers. Should
 * be used in conjunction with <code>WriteBarrierStats</code>.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: WriteBarrierData.java,v 1.2 2002-02-25 20:58:54 cananian Exp $
 */
public class WriteBarrierData extends harpoon.Backend.Runtime1.Data {

    /** Creates a <code>WriteBarrierData</code> */
    WriteBarrierData(HClass hc, Frame f, int datum) {
	super("write-barrier", hc, f);
	// only emit this once
	Util.assert(hc == f.getLinker().forName("java.lang.Object"));
	this.root = build(datum);
    }

    private HDataElement build(int datum) {
	List stmlist = new ArrayList(4);
	stmlist.add(new SEGMENT(tf, null, SEGMENT.TEXT));
	stmlist.add(new ALIGN(tf, null, 4)); // word align
	stmlist.add(new LABEL(tf, null,new Label("num_write_barriers"), true));
	stmlist.add(new DATUM(tf, null, new CONST(tf, null, datum)));
	return (HDataElement) Stm.toStm(stmlist);
    }
}
