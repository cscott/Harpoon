// InstrEdge.java, created Fri Aug 27 18:43:54 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.IR.Properties.CFGEdge;
import harpoon.IR.Properties.CFGraphable;

/**
 * <code>InstrEdge</code> is an object representing an edge between
 * two <code>Instr</code>s. 
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: InstrEdge.java,v 1.2.2.1 2002-04-07 20:51:36 cananian Exp $
 */
public class InstrEdge extends CFGEdge<Instr> {
    
    public final Instr from;
    public final Instr to;

    /** Creates a <code>InstrEdge</code> representing
	<nobr> &lt from, to &gt </nobr>.
     */
    public InstrEdge(Instr from, Instr to) {
        this.from = from;
	this.to = to;
    }
    
    public Instr toCFG() { return to; }
    public Instr fromCFG() { return from; }

    public boolean equals(Object o) {
	try {
	    InstrEdge ie = (InstrEdge) o;
	    return ie.to.equals(this.to) &&
		ie.from.equals(this.from);
	} catch (ClassCastException e) {
	    return false;
	}
    }
    public int hashCode() {
	return to.hashCode() ^ from.hashCode();
    }
    public String toString() {
	return "InstrEdge:<\""+from+"\", \""+to+"\">"+
	    " (<"+from.hashCode()+", "+to.hashCode()+">)";
    }
}
