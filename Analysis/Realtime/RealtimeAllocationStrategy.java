// RealtimeAllocationStrategy.java, created Fri Mar 23 10:31:56 2001 by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.Analysis.Realtime.Realtime;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Runtime1.MallocAllocationStrategy;

import harpoon.ClassFile.HCodeElement;

import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.JUMP;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;

import harpoon.Temp.Label;
import harpoon.Temp.Temp;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * <code>RealtimeAllocationStrategy</code> makes all memory allocations to go through
 * <code>RTJ_malloc</code>.  If DEBUG_REF is turned on, then it calls 
 * <code>RTJ_malloc_ref(size, fileNumber, "fileName")</code>.  The const char* "fileName"
 * must be emitted in a separate area, so emitStrings must be called later in the 
 * file to emit that data.
 * @author Wes Beebee <wbeebee@mit.edu>
 */

public class RealtimeAllocationStrategy extends MallocAllocationStrategy {

    private static Hashtable string2label = new Hashtable();

    /** Creates a <code>RealtimeAllocationStrategy</code>.
     */
    public RealtimeAllocationStrategy(Frame f) { super (f, "RTJ_malloc"); }

    /** Produces the Tree code corresponding to a memory allocation. 
     */
    public Exp memAlloc(TreeFactory tf, HCodeElement src,
			DerivationGenerator dg,
			AllocationProperties ap,
			Exp length) {
	if (Realtime.DEBUG_REF) {
	    return buildAllocCall(tf, src, dg, ap, "RTJ_malloc_ref", length, 
				  new ExpList(new CONST(tf, src, src.getLineNumber()),
					      new ExpList(new NAME(tf, src, 
								   fileLabel(src)),
							  null)));
	} 
	return buildAllocCall(tf, src, dg, ap, "RTJ_malloc", length, null);
    }

    /** Return a label corresponding to the string which represents the file that 
     *  src came from.  Available only if DEBUG_REF is turned on.
     */
    public static Label fileLabel(HCodeElement src) {
	Label file = null; 
	if (Realtime.DEBUG_REF) {
	    file = (Label)string2label.get(src.getSourceFile());
	    if (file == null) {
		string2label.put(src.getSourceFile(), (file=new Label()));
	    }
	}
	return file;
    }


    /** Emit all of the const char*'s used so far in the file and flush the list. 
     */
    public static Stm emitStrings(TreeFactory tf, HCodeElement src) {
	List stmList = new ArrayList();
	stmList.add(new SEGMENT(tf, src, SEGMENT.STRING_CONSTANTS));

	Enumeration keys = string2label.keys();
	while (keys.hasMoreElements()) {
	    String s = (String)keys.nextElement();
	    stmList.add(new LABEL(tf, src, (Label)string2label.get(s), true));
	    for (int i=0; i<s.length(); i++) 
		stmList.add(new DATUM(tf, src, new CONST(tf, src, 8, false, 
							 (int) s.charAt(i))));
	    // null terminate
	    stmList.add(new DATUM(tf, src, new CONST(tf, src, 8, false, 0)));
	    // align to proper word boundary
	    stmList.add(new ALIGN(tf, src, 8));
	}
	string2label.clear();
	return Stm.toStm(stmList);
    }
}
