// InstrEdge.java, created Fri Aug 27 18:43:54 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;

/**
 * <code>InstrEdge</code> is an object representing an edge between
 * two <code>Instr</code>s. 
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: InstrEdge.java,v 1.1.2.1 1999-08-28 00:40:18 pnkfelix Exp $
 */
public class InstrEdge implements HCodeEdge {
    
    public final Instr from;
    public final Instr to;

    /** Creates a <code>InstrEdge</code> representing
	<nobr> &lt from, to &gt </nobr>.
     */
    public InstrEdge(Instr from, Instr to) {
        this.from = from;
	this.to = to;
    }
    
    public HCodeElement to() { return to; }
    public HCodeElement from() { return from; }
}
