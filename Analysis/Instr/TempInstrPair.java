// TempInstrPair.java, created Mon May 31 13:52:05 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.IR.Assem.Instr;
import harpoon.Temp.Temp;
/**
  <code>TempInstrPair</code> is an immutable data structure that
  associates an <code>Instr</code> with a <code>Temp</code>. 
  
  @author  Felix S Klock <pnkfelix@mit.edu>
  @version $Id: TempInstrPair.java,v 1.1.2.1 1999-06-03 01:46:45 pnkfelix Exp $
 */
class TempInstrPair {
    final Temp t;
    final Instr i;
    
    TempInstrPair(Instr i, Temp t) {
	this.i = i;
	this.t = t;
    }

    public TempInstrPair(Temp t, Instr i) {
	this.i = i;
	this.t = t;
    }

    public boolean equals(Object o) {
	return (o instanceof TempInstrPair && 
		((TempInstrPair)o).t.equals(this.t) &&
		((TempInstrPair)o).i.equals(this.i));
    }

    public int hashCode() {
	return i.hashCode();
    }
    
}
