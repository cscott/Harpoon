// GraphColoringRegAlloc.java, created Mon Jul 17 16:39:13 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Analysis.Maps.Derivation;
import harpoon.Backend.Generic.Code;

/**
 * <code>GraphColoringRegAlloc</code>
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: GraphColoringRegAlloc.java,v 1.1.2.1 2000-07-18 23:03:41 pnkfelix Exp $
 */
public class GraphColoringRegAlloc extends RegAlloc {
    
    /* Global Variables given in Muchnick, page 487, in ICAN notation,
       converted to java naming conventions for readability.  
       FSK: (ICAN notation is overly bloated for specifications 
       (why have an Array type when a Sequence will do for an abstract
       specification)) After I understand what these variables do, I
       will convert them to more Java-esque form.
      
      Symbol     = Var U Register U Const
      UdDu       = Integer x Integer
      UdDuChain  = (Symbol x UdDu) -> Set of UdDu
      WebRecord  = record { sym        : Symbol, 
                            defs, uses : Set of UdDu,
			    spill      : Boolean,
			    sreg       : Register,
			    disp       : Integer }
      ListRecord = record { nints, color, disp : Integer,
                            spcost : Real,
			    adjnds, rmvadj : List of Integer }
      OpdRecord  = record { kind : enum { var, regno, const },
                            val  : Symbol }

      defWt, useWt, copyWt : Real
      nregs, nwebs, baseReg : Integer
      disp := INITIAL_DISPLACEMENT, argReg : Integer
      retReg  : Register
      symReg  : Array [**] of WebRecord
      adjMtx  : Array [**,**] of Boolean
      adjLsts : Array [**] of ListRecord
      stack   : Sequence of Integer
      realReg : Integer -> Integer */


    /** Creates a <code>GraphColoringRegAlloc</code>. */
    public GraphColoringRegAlloc(Code code) {
        super(code);
    }

    public Derivation getDerivation() {
	return null;
    }

    protected void generateRegAssignment() {
	boolean success, coalesced;
	do {
	    do {
		makeWebs();
		buildAdjMatrix();
		coalesced = coalesceRegs();
	    } while (coalesced);
	    buildAdjLists();
	    computeSpillCosts();
	    pruneGraph();
	    success = assignRegs();
	    if (success) {
		modifyCode();
	    } else {
		genSpillCode();
	    }
	} while (!success);
    }
    
    private void makeWebs() { }
    private void buildAdjMatrix() { }
    private boolean coalesceRegs() { return false; }
    private void buildAdjLists() { }
    private void computeSpillCosts() { }
    private void pruneGraph() { }
    private boolean assignRegs() { return true; }
    private void modifyCode() { } 
    private void genSpillCode() { }
       
    
}
