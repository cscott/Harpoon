// GraphColoringRegAlloc.java, created Mon Jul 17 16:39:13 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Analysis.Maps.Derivation;
import harpoon.Backend.Generic.Code;
import harpoon.IR.Assem.Instr;
import harpoon.Temp.Temp;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
/**
 * <code>GraphColoringRegAlloc</code>
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: GraphColoringRegAlloc.java,v 1.1.2.2 2000-07-20 16:44:52 pnkfelix Exp $
 */
public class GraphColoringRegAlloc extends RegAlloc {
    
    private static final int INITIAL_DISPLACEMENT = 0;
    
    /* Global Variables given in Muchnick, page 487, in ICAN notation,
       converted to java naming conventions for readability.  
       FSK: (ICAN notation is overly bloated for specifications 
       (why have an Array type when a Sequence will do for an abstract
       specification)) After I understand what these variables do, I
       will convert them to more Java-esque form.
    */      
    
    /* ** Types **
       Symbol     = Var U Register U Const         (Temp)
       UdDu       = Int x Int                      (Instr)
       UdDuChain  = (Symbol x Def) -> Set of Use
    */
    class WebRecord {
	Temp sym;
	Set defs, uses; // Set<Instr>
	boolean spill;
	int sreg;
	int disp;

	WebRecord(Temp symbol, Set defSet, Set useSet, 
		  boolean spll, int symReg, int displacement) {
	    sym = symbol; defs = defSet; uses = useSet;
	    spill = spll; sreg = symReg; disp = displacement;
	}
    }

    class ListRecord {
	int nints, color, disp;
	double spcost;
	List adjnds, rmvadj; // List<Integer>
    }

    class OpdRecord {
	boolean isVar() { return false; }
	boolean isRegno() { return false; }
	boolean isConst() { return false; }
	Temp val;
    }

    class AdjMtx {
	// implement here: a Lower Triangular Matrix backed by a
	// BitString.  Note that for Lower Triangular Matrix, order of
	// coordinate args is insignificant (from p.o.v. of user).

	boolean get(int x, int y) { return true; }
	void set(int x, int y, boolean b) {
	    
	}
	
    }
    
    /* ** Fields ** */
    
    double defWt, useWt, copyWt;
    int nregs, nwebs, baseReg;
    int disp = INITIAL_DISPLACEMENT, argReg;
    Temp retRegs;
    List symReg; // List<WebRecord>
    AdjMtx adjMtx;
    ListRecord[] adjLsts;
    List stack; // List<Integer>
    Map realReg; // Map<Integer, Integer>

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
		makeWebs(new HashMap()); // need to pass in DuChains here
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

    // Building the DuChain efficiently is going to be the tricky part
    // of this; do I convert from ReachingDefs results or come up with
    // alternative analysis?  It doesn't seem like anything in
    // harpoon.Analysis fits the bill yet, despite a few red
    // herrings...  and of course, there's always the option of
    // punting this and making a direct mapping from Temp to Web
    // (which shouldn't hurt us much since we're dealing with post-SSA
    // form here...)

    private void makeWebs(Map duchain) {
	Set webSet = new HashSet(), tmp1, tmp2; // Set<WebRecord>
	WebRecord web1, web2;
	List sd; // [Symbol, Def]
	int i, oldnwebs;
	
	nwebs = nregs;
	Iterator symDefPairIter = duchain.keySet().iterator();
	while(symDefPairIter.hasNext()) {
	    sd = (List) symDefPairIter.next();
	    nwebs++;
	    webSet.add(new WebRecord((Temp) sd.get(0), 
				     Collections.singleton(sd.get(1)), 
				     (Set)duchain.get(sd), 
				     false, -1, -1));
	}
	do {
	    // combine du-chains for the same symbol and that have a
	    // use in common to make webs  
	    oldnwebs = nwebs;
	    tmp1 = webSet;
	    while(!tmp1.isEmpty()) {
		web1 = (WebRecord) tmp1.iterator().next();
		tmp1.remove(web1);
		tmp2 = new HashSet(tmp1);
		while(!tmp2.isEmpty()) {
		    web2 = (WebRecord) tmp2.iterator().next();
		    tmp2.remove(web2);
		    if (web1.sym.equals(web2.sym)) {
			Set ns = new HashSet(web1.uses);
			ns.retainAll(web2.uses);
			if (!ns.isEmpty()) {
			    web1.defs.addAll(web2.defs);
			    web1.uses.addAll(web2.uses);
			    webSet.remove(web2);
			    nwebs--;
			}
		    }
		}
	    }
	} while ( oldnwebs != nwebs );
	
	// FSK: may need to switch the thinking here from "number of
	// regs" to "number of possible assignments" which is a
	// different beast altogether...

	for(i=1; i<=nregs; i++) {
	    symReg.add(new WebRecord(intToReg(i),
				     new HashSet(), new HashSet(),
				     false, -1, -1));
	}
	// assign symbolic register numbers to webs
	i = nregs;
	Iterator webs = webSet.iterator();
	while(webs.hasNext()) {
	    web1 = (WebRecord) webs.next();
	    i++;
	    symReg.add(web1);
	    web1.sreg = i;
	}

	// FSK: below is pointless; just including it for complete
	// mapping from Muchnick...
	// MIR_to_SymLIR();
    }

    Temp intToReg(int i) { return null; }

    // what rep to use for AdjMatrix?  Surely NOT a boolean[][]!!!
    // Seriously, a BitStringSet with IntPairs as the universe might
    // be a FAR better choice... look into it... in any case, if
    // AdjMtx is made global, can easily abstract these mutators and
    // fuss with rep later... (or just make a class for it here... do
    // SOMETHING)... am definitely leaning towards an AdjMtx ADT, with
    // operations like boolean maxmin(i, j) where it returns true iff
    // AdjMtx[max(i,j), min(i,j)] since that seems to be an operation
    // that's used VERY often.  Just need to be able to figure out how
    // to pass in the expected size so that it can construct an
    // appropriate universe (or is a BitStringSet not the appropriate
    // backing for this?  Need to look into alternatives... perhaps
    // just a normal BitString?  Still need a size there...)

    private void buildAdjMatrix() { 
	int i, j;
	for(i=2; i<=nwebs; i++) {
	    for(j=2; j<=i-1; j++) {
		adjMtx.set(i,j,false);
	    }
	}
	for(i=2; i<=nregs; i++) {
	    for(j=1; j<=i-1; j++) {
		adjMtx.set(i,j,true);
	    }
	}
	for(i=nregs+1; i<=nwebs; i++) {
	    for(j=1; j<=nregs; j++) {
		if (interfere((WebRecord)symReg.get(i), j)) {
		    adjMtx.set(i,j,true);
		}
	    }
	    for(j=nregs+1; j<=i-1; j++) {
		Iterator defs = ((WebRecord)symReg.get(i)).defs.iterator();
		while(defs.hasNext()) {
		    Instr def = (Instr) defs.next();
		    if (liveAt((WebRecord)symReg.get(j), 
			       ((WebRecord)symReg.get(i)).sym, def))
			adjMtx.set(i,j,true);
		}
	    }
	}
    }
    
    // it seems that Muchnick leaves a lot of the implementation of
    // auxilliary methods up to reader; perhaps because their
    // specifications are clean enough that demonstration with an
    // actual implementation was deemed unnecessary... 

    private boolean interfere(WebRecord s, int reg) {
	return true;
    }

    // returns true if there are any definitions in `web' that are
    // live at the definition `def' of symbol `sym' 
    // FSK: (why is `sym' a parameter here?  Doesn't seem needed)
    private boolean liveAt(WebRecord web, Temp sym, Instr def) {
	return true;
    }

    private boolean nonStore(Object lblock, 
			     int k, int l, int i, int j) {
	return false;
    }

    // This '.left' stuff is bullshit... just a complicated way of
    // indicating the definition type and doing the necessary
    // replacement... temp remapping should look cleaner...
    private boolean coalesceRegs() { 
	return false;
	/*
	int i, j, k, l, p, q;
	Instr inst, pqinst;
	for(i=1; i<=nblocks; i++) {
	    for(j=1; j<=ninsts[i]; j++) {
		inst = LBlock[i][j];
		if (inst.kind = regval) {
		    k = Reg_to_Int(inst.left);
		    l = Reg_to_Int(inst.opd.val);
		    if (! adjMtx.get(k,l) ||
			nonStore(LBlock,k,l,i,j)) {
			for(p=1; p<nblocks; p++) {
			    for(q=1; q<ninsts[p]; q++) {
				pqinst = LBlock[p][q];
				if (LIR_Has_Left(pqinst) &&
				    pqinst.left == inst.opt.val) {
				    pqinst.left = inst.left;
				}
			    }
			}
		    }
		    // remove the copy instruction 
		    inst.remove();
		    ((WebRecord)symReg.get(k)).defs
			.addAll(((WebRecord)symReg.get(l)).defs);
		    ((WebRecord)symReg.get(k)).uses
			.addAll(((WebRecord)symReg.get(l)).uses);
		    symReg.set(1, symReg.get(nwebs));
		    for(p=1; p<=nwebs; p++) {
			if (adjMtx.get(p,l)) {
			    adjMtx.set(p,l,true);
			}
			adjMtx.set(p,l, adjMtx.get(nwebs,p));
		    }
		    nwebs--;
		}
	    }
	}
	*/
    }

    private void buildAdjLists() { 
	int i, j;
	for(i=1; i<=nregs; i++) {
	    adjLsts[i].nints = 0;
	    adjLsts[i].color = Integer.MIN_VALUE;
	    adjLsts[i].disp = Integer.MIN_VALUE;
	    adjLsts[i].spcost =  Double.POSITIVE_INFINITY;
	    adjLsts[i].adjnds = new LinkedList();
	    adjLsts[i].rmvadj = new LinkedList();
	}
	for(i=nregs+1;i<=nwebs;i++) {
	    adjLsts[i].nints = 0;
	    adjLsts[i].color = Integer.MIN_VALUE;
	    adjLsts[i].disp = Integer.MIN_VALUE;
	    adjLsts[i].spcost = 0.0;
	    adjLsts[i].adjnds = new LinkedList();
	    adjLsts[i].rmvadj = new LinkedList();
	}
	for(i=2; i<=nwebs;i++) {
	    for(j=1;j<=nwebs-1;j++) {
		if (adjMtx.get(i,j)) {
		    adjLsts[i].adjnds.add(new Integer(j));
		    adjLsts[j].adjnds.add(new Integer(i));
		    adjLsts[i].nints++;
		    adjLsts[j].nints++;
		}
	    }
	}
    }

    private void computeSpillCosts() { 
	
    }

    private void pruneGraph() { 
    
    }

    private boolean assignRegs() { 
	return true; 
    }

    private void modifyCode() { 
    
    } 

    private void genSpillCode() { 

    }
       
    
}
