// TempInstrPair.java, created Mon May 31 13:52:05 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.IR.Assem.Instr;
import harpoon.Temp.Temp;
/**
  <code>TempInstrPair</code> is an immutable data structure that
  associates an <code>Instr</code> with a <code>Temp</code>. 
  
  @author  Felix S. Klock II <pnkfelix@mit.edu>
  @version $Id: TempInstrPair.java,v 1.1.2.4 2001-06-17 22:29:56 cananian Exp $
 */
public class TempInstrPair {
    private final Temp t;
    private final Instr i;
    
    public TempInstrPair(Instr i, Temp t) {
	this(t, i);
    }

    public TempInstrPair(Temp t, Instr i) {
	this.i = i;
	this.t = t;
    }

    public Temp getTemp() { return t; }
    public Instr getInstr() { return i; }

    public boolean equals(Object o) {
	TempInstrPair tip;
	if (this==o) return true;
	if (null==o) return false;
	try { tip = (TempInstrPair) o; }
	catch (ClassCastException ignore) { return false; }
	return tip.t.equals(this.t) &&
	       tip.i.equals(this.i);
    }

    public int hashCode() {
	return i.hashCode();
    }
    
}
