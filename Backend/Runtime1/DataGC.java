// DataGC.java, created Thu Jan 13 20:55:26 2000 by kkz
// Copyright (C) 1999 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.Maps.Derivation;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.GCInfo;
import harpoon.Backend.Generic.GCInfo.DLoc;
import harpoon.Backend.Generic.GCInfo.GCPoint;
import harpoon.Backend.Generic.GCInfo.WrappedMachineRegLoc;
import harpoon.Backend.Generic.GCInfo.WrappedStackOffsetLoc;
import harpoon.Backend.Generic.RegFileInfo.CommonLoc;
import harpoon.Backend.Generic.RegFileInfo.MachineRegLoc;
import harpoon.Backend.Generic.RegFileInfo.StackOffsetLoc;
import harpoon.Backend.Maps.BackendDerivation;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HDataElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.Stm;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * <code>DataGC</code> outputs the tables needed by the garbage collector.
 * 
 * @author  Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: DataGC.java,v 1.1.2.16 2001-07-10 22:49:48 cananian Exp $
 */
public class DataGC extends Data {
    final GCInfo m_gc;
    final NameMap m_nm;
    final TreeBuilder m_tb;
    final int numRegs;
    
    // number of bits in a 32-bit int
    final int INT_BITS = 32;
    // size of descriptor in bits
    final int DESC_BITS = 8;

    /** Creates a <code>DataGC</code>. */
    public DataGC(Frame f, HClass hc) {
        super("gc-data", hc, f);
	Util.assert(f.getGCInfo() != null);
	this.m_gc = f.getGCInfo();
	this.m_nm = f.getRuntime().getNameMap();
	this.m_tb = (TreeBuilder) f.getRuntime().getTreeBuilder();
	this.numRegs = f.getRegFileInfo().maxRegIndex();
	this.root = build();
    }

    private HDataElement build() {
	final List stmlist = new ArrayList();
	// handle only methods that we've had to generate
	// GCInfo for and do so in the order saved by GCInfo
	final List methods = m_gc.getOrderedMethods(hc);
	if (methods == null) return null;
	report(methods.toString());
	// switch to the GC segment
	stmlist.add(new SEGMENT(tf, null, SEGMENT.GC));
	// align on word-boundary
	stmlist.add(new ALIGN(tf, null, 4));
	for (Iterator it=methods.iterator(); it.hasNext(); ) {
	    Stm output = outputGCData((HMethod)it.next());
	    if (output != null)
		stmlist.add(output);
	}
	return (HDataElement) Stm.toStm(stmlist);
    }

    // requires: method for which GC points have been calculated
    // modifies: basetableMap
    // effects:  returns statements for outputting base table and
    //           puts numeric mapping of stackOffsetLocs into the
    //           basetableMap
    private Stm outputBaseTable(HMethod hm) {
	// get ordered list of GC points for this method
	List gcps = m_gc.gcPoints(hm);
	// make sure the list is non-null
	Util.assert(gcps != null);
	// first create base table for stack locations
	Set basetableSet = new HashSet();
	// take union of live WrappedStackOffsetLocs for all GC points
	for(Iterator it = gcps.iterator(); it.hasNext(); )
	    basetableSet.addAll(((GCPoint)it.next()).liveStackOffsetLocs());
	// make statements
	List stmlist = new ArrayList(basetableSet.size()+2);
	// label base table location
	stmlist.add(new LABEL(tf, null, m_nm.label(hm, "gc_bt"), true));
	// number of entries in base table (int)
	stmlist.add(_DATUM(new CONST(tf, null, basetableSet.size())));
	report("Size of base table = " + basetableSet.size());
	// clear basetableMap
	basetableMap = new HashMap(); int i = 0;
	for(Iterator btIt = basetableSet.iterator();
	    btIt.hasNext(); ) {
	    // put new entries into basetableMap
	    WrappedStackOffsetLoc wsol = (WrappedStackOffsetLoc)btIt.next();
	    // output stackOffset (int)
	    stmlist.add(_DATUM(new CONST(tf, null, wsol.stackOffset())));
	    report("Base table entry: " + wsol.stackOffset());
	    basetableMap.put(wsol, new Integer(i++));
	}
	return Stm.toStm(stmlist);
    }
    private Map basetableMap;

    private Stm outputGCData(HMethod hm) {
	// get ordered list of GC points for this method
	final List gcps = m_gc.gcPoints(hm);
	if (gcps.isEmpty()) return null;
	final List stmlist = new ArrayList();
	// make base table for method
	stmlist.add(outputBaseTable(hm));
	GCPoint prev = null; int i = 0;
	// make entry for each GC point
	for(Iterator it = gcps.iterator(); it.hasNext(); ) {
	    GCPoint gcp = (GCPoint)it.next();
	    // add index entry to GC_INDEX segment
	    stmlist.add(new SEGMENT(tf, null, SEGMENT.GC_INDEX));
	    // location of GC point
	    stmlist.add(_DATUM(gcp.label()));
	    // location of GC data
	    stmlist.add(_DATUM(m_nm.label(hm, "gcp_index_"+i)));
	    // location of GC base table
	    // stmlist.add(_DATUM(m_nm.label(hm, "gc_bt")));
	    // add data entry to GC segment
	    stmlist.add(new SEGMENT(tf, null, SEGMENT.GC));
	    // align on word boundary
	    stmlist.add(new ALIGN(tf, null, 4));
	    // label GC data
	    stmlist.add(new LABEL(tf, null, m_nm.label(hm, "gcp_index_"+i), 
				  true));
	    // output actual data
	    stmlist.add(outputGCPoint(gcp, prev));
	    // setup for next
	    prev = gcp; i++;
	}
	//System.out.println(i+" GC points in "+hm);
	return Stm.toStm(stmlist);
    }

    // octal
    final int NO_LIVE_REGISTERS              =   01;
    final int NO_CHANGE_IN_REGISTERS         =   02;
    final int NO_LIVE_STACK_LOCATIONS        =   04;
    final int NO_CHANGE_IN_STACK_LOCATIONS   =  010;
    final int NO_LIVE_DERIVED_POINTERS       =  020;
    final int NO_CHANGE_IN_DERIVED_POINTERS  =  040;
    final int NO_LIVE_CALLEE_SAVED_REGISTERS = 0100;
    final int NO_CHANGE_IN_CALLEE_SAVED_REGISTERS = 0200;
    final int DESCRIPTOR_SIZE                =    8; // in bits
    // requires: current and previous GC point
    // modifies: nil
    // effects:  returns statements for outputting data relevant
    //           to current GC point
    private Stm outputGCPoint(GCPoint curr, GCPoint prev) {
	// output format: bits 31-26 -- descriptor
	int output = 0;
	boolean outputRegisters = true;
	final Set registers = curr.liveMachineRegLocs();
	if (registers.isEmpty()) {
	    output |= NO_LIVE_REGISTERS; outputRegisters = false;
	} else if (prev != null && 
		   registers.equals(prev.liveMachineRegLocs())) {
	    output |= NO_CHANGE_IN_REGISTERS; outputRegisters = false;
	}
	boolean needStack = true;
	Set stack = curr.liveStackOffsetLocs();
	if (stack.isEmpty()) {
	    output |= NO_LIVE_STACK_LOCATIONS;
	    needStack = false;
	} else if (prev != null &&
		   stack.equals(prev.liveStackOffsetLocs())) {
	    output |= NO_CHANGE_IN_STACK_LOCATIONS;
		needStack = false;
	    }   
	boolean needDerivs = true;
	Map regDerivs = curr.regDerivations();
	Map stackDerivs = curr.stackDerivations();
	if (regDerivs.isEmpty() && stackDerivs.isEmpty()) {
	    output |= NO_LIVE_DERIVED_POINTERS;
	    needDerivs = false;
	} else if (prev != null && 
		   regDerivs.equals(prev.regDerivations()) &&
		   stackDerivs.equals(prev.stackDerivations())) {
	    output |= NO_CHANGE_IN_DERIVED_POINTERS;
	    needDerivs = false;
	}
	// handle callee-saved registers
	boolean needCSaved = true;
	Map cSaved = curr.calleeSaved();
	if (cSaved.isEmpty()) {
	    output |= NO_LIVE_CALLEE_SAVED_REGISTERS;
	    needCSaved = false;
	} else if (prev != null && cSaved.equals(prev.calleeSaved())) {
	    output |= NO_CHANGE_IN_CALLEE_SAVED_REGISTERS;
	    needCSaved = false;
	}
	List stmlist = new ArrayList();
	if (!outputRegisters && !needStack)
	    // output descriptor (int)
	    stmlist.add(_DATUM(new CONST(tf, null, output)));
	else
	    // handle both registers and stack together to save bits
	    stmlist.add(outputRS(outputRegisters, needStack, 
				 registers, stack, output));
	if (needDerivs)
	    stmlist.add(outputDerivs(regDerivs, stackDerivs));
	if (needCSaved)
	    stmlist.add(outputCSaved(cSaved));
	return Stm.toStm(stmlist);
    } // outputGCPoint

    // requires: descriptor set in output
    // modifies: nil
    // effects: returns statements that output descriptor
    //          plus register data (if needed) and stack data
    //          (if needed)
    private Stm outputRS(boolean needRegs, boolean needStack,
			 Set regs, Set stack, int output)
    {
	// number of bits needed to store the descriptor,
	// the register bitmap and the stack bitmap
	final int bits = DESCRIPTOR_SIZE + 
	    (needRegs?numRegs:0) + (needStack?basetableMap.size():0);
	// number of 32-bit integers needed to encode the data 
	final int numInts = (bits + INT_BITS - 1) / INT_BITS;
	int[] data = new int[numInts];
	// remember the descriptor
	int offset = DESCRIPTOR_SIZE;
	// get descriptor from output
	data[0] = output;
	// do registers first
	if (needRegs) {
	    for(Iterator it=regs.iterator(); it.hasNext(); ) {
		int regIndex = ((WrappedMachineRegLoc)it.next()).regIndex();
		Util.assert(regIndex < numRegs);
		int i = (regIndex+offset) / INT_BITS;
		int j = (regIndex+offset) % INT_BITS;
		Util.assert(i < numInts);
		data[i] |= 1 << (INT_BITS - j - 1);
	    }
	    offset += numRegs;
	}
	if (needStack) {
	    for(Iterator it=stack.iterator(); it.hasNext(); ) {
		int index = ((Integer)basetableMap.get(it.next())).intValue();
		int i = (index+offset) / INT_BITS;
		int j = (index+offset) % INT_BITS;
		Util.assert(i < numInts);
		data[i] |= 1 << (INT_BITS - j - 1);
	    }
	}
	// output accumulated data
	List stmlist = new ArrayList();
	for(int k=0; k < numInts; k++)
	    stmlist.add(_DATUM(new CONST(tf, null, data[k])));
	return Stm.toStm(stmlist);
    }

    // effects: returns statements that output the derivation data
    private Stm outputDerivs(Map regDerivs, Map stackDerivs) {
	List stmlist = new ArrayList();
	// number of derived pointers in registers
	stmlist.add(_DATUM(new CONST(tf, null, regDerivs.size())));
	// number of derived pointers in stack
	stmlist.add(_DATUM(new CONST(tf, null, stackDerivs.size())));
	// handle derived pointers in registers
	for(Iterator keys=regDerivs.keySet().iterator(); keys.hasNext(); ) {
	    WrappedMachineRegLoc key = (WrappedMachineRegLoc)keys.next();
	    // location of derived pointer (int)
	    stmlist.add(_DATUM(new CONST(tf, null, key.regIndex()))); 
	    // derivation information
	    stmlist.add(outputDLoc((GCInfo.DLoc)regDerivs.get(key)));
	}
	// handle derived pointers in stack
	for (Iterator keys=stackDerivs.keySet().iterator(); keys.hasNext(); ) {
	    WrappedStackOffsetLoc key = (WrappedStackOffsetLoc)keys.next();
	    // location of derived pointer (int)
	    stmlist.add(_DATUM(new CONST(tf, null, key.stackOffset())));
	    // derivation information
	    stmlist.add(outputDLoc((GCInfo.DLoc)stackDerivs.get(key)));
	}
	return Stm.toStm(stmlist);
    }
    private Stm outputDLoc(GCInfo.DLoc dl) {
	List stmlist = new ArrayList();
	// number of base pointers total
	stmlist.add(_DATUM(new CONST(tf, null, (dl.stackLocs.length + 
						dl.regLocs.length))));
	// register bits are laid out in (data, sign) pairs
	{
	    final int numInts = (2 * numRegs + INT_BITS - 1) / INT_BITS;
	    int[] data = new int[numInts];
	    for(int index=0; index < dl.regLocs.length; index++) {
		int regIndex = dl.regLocs[index].regIndex();
		int i = (2 * regIndex) / INT_BITS;
		int j = (2 * regIndex) % INT_BITS;
		data[i] |= 1 << (INT_BITS - j - 1);
		data[i] |= 
		    (dl.regSigns[index] ? 0 : 1) << (INT_BITS - j - 2);
	    }
	    for(int k=0; k < numInts; k++)
		stmlist.add(_DATUM(new CONST(tf, null, data[k])));
	}
	// only output if we have non-zero entries
	if (dl.stackLocs.length != 0) {
	    final int numInts = (dl.stackLocs.length+INT_BITS-1)/INT_BITS;
	    int[] sign = new int[numInts];
	    for(int index=0; index < dl.stackLocs.length; index++) {
		int stackOffset = dl.stackLocs[index].stackOffset();
		stmlist.add(_DATUM(new CONST(tf, null, stackOffset)));
		int i = index / INT_BITS;
		int j = index % INT_BITS;
		sign[i] |= 
		    (dl.stackSigns[index] ? 0 : 1) << (INT_BITS - j - 1);
	    }
	    for(int k=0; k < numInts; k++)
		stmlist.add(_DATUM(new CONST(tf, null, sign[k])));
	}
	return Stm.toStm(stmlist);
    }
    // output information about callee-saved registers
    private Stm outputCSaved(Map cSaved) {
	// calculate how many 32-bit ints we need to encode bit field
	final int numInts = (2 * numRegs + INT_BITS - 1) / INT_BITS;
	int[] bitfield = new int[numInts];
	int[] locations = new int[cSaved.size()];
	// output a bit field indicating which entries have data
	//   00, 10 - no data
	//       01 - register
	//       11 - stack
	for(Iterator keys=cSaved.keySet().iterator(); keys.hasNext(); ) {
	    BackendDerivation.Register key = 
		(BackendDerivation.Register)keys.next();
	    CommonLoc location = (CommonLoc)cSaved.get(key);
	    // make sure we have a valid register index
	    Util.assert(key.regIndex() < numRegs);
	    int i = (2 * key.regIndex()) / INT_BITS;
	    int j = (2 * key.regIndex()) % INT_BITS;
	    // set the bit for data/no data
	    bitfield[i] |= 1 << (INT_BITS - j - 2);
	    switch(location.kind()) {
	    case StackOffsetLoc.KIND:
		// set the bit for stack/register
		bitfield[i] |= 1 << (INT_BITS - j - 1);
	        locations[key.regIndex()] = 
		    ((StackOffsetLoc)location).stackOffset(); break;
	    case MachineRegLoc.KIND:
		locations[key.regIndex()] =
		    ((MachineRegLoc)location).regIndex(); break;
	    default:
		Util.assert(false);
	    }
	}
	List stmlist = new ArrayList();
	// dump out bitfield
	for(int k = 0; k < bitfield.length; k++)
	    stmlist.add(_DATUM(new CONST(tf, null, bitfield[k])));
	// dump out register and stack locations
	for(int k = 0; k < locations.length; k++)
	    stmlist.add(_DATUM(new CONST(tf, null, locations[k])));
	return Stm.toStm(stmlist);
    }
    final private boolean DEBUG = false;
    // convenient debugging utility
    private void report(String str) {
	if (DEBUG) System.out.println(str);
    }
}












