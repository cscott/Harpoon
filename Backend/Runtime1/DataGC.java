// DataGC.java, created Thu Jan 13 20:55:26 2000 by kkz
// Copyright (C) 1999 Karen K. Zee <kkz@tesuji.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Maps.Derivation;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.GCInfo;
import harpoon.Backend.Generic.GCInfo.GCPoint;
import harpoon.Backend.Generic.RegFileInfo;
import harpoon.Backend.Generic.RegFileInfo.CommonLoc;
import harpoon.Backend.Generic.RegFileInfo.MachineRegLoc;
import harpoon.Backend.Generic.RegFileInfo.StackOffsetLoc;
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
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
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
 * @author  Karen K. Zee <kkz@tesuji.lcs.mit.edu>
 * @version $Id: DataGC.java,v 1.1.2.5 2000-02-07 23:46:40 pnkfelix Exp $
 */
public class DataGC extends Data {
    final GCInfo m_gc;
    final NameMap m_nm;
    final TreeBuilder m_tb;
    final private int numBR; // number of bytes needed for register data
    final private static boolean DEBUG = true;
    
    /** Creates a <code>DataGC</code>. */
    static final int BYTESIZE = 8;
    public DataGC(Frame f, HClass hc, ClassHierarchy ch) {
        super("gc-data", hc, f);
	Util.assert(f.getGCInfo() != null);
	this.m_gc = f.getGCInfo();
	this.m_nm = f.getRuntime().nameMap;
	this.m_tb = (TreeBuilder) f.getRuntime().treeBuilder;
	this.root = build(ch);
	double numR = (double)f.getRegFileInfo().maxRegIndex();
	this.numBR = (int)Math.ceil(numR/((double)BYTESIZE));
    }
    private HDataElement build(ClassHierarchy ch) {
	List stmlist = new ArrayList();
	stmlist.add(new SEGMENT(tf, null, SEGMENT.GC));
	stmlist.add(new ALIGN(tf, null, 4));
	Set callableMs = ch.callableMethods();
	HMethod[] hmArray = hc.getDeclaredMethods();
	Set hmset = new TreeSet(Arrays.asList(hmArray));
	hmset.retainAll(callableMs);
	for (Iterator it=hmset.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod)it.next();
	    if (!Modifier.isAbstract(hm.getModifiers()))
		stmlist.add(outputGCData(hm));
	}
	return (HDataElement) Stm.toStm(stmlist);
    }
    private static final int REGS_ARE_ZERO       = -128;
    private static final int REGS_SAME_AS_PREV   =   64;
    private static final int STACKLOCS_ARE_ZERO  =   32;
    private static final int STACK_SAME_AS_PREV  =   16;
    private static final int DERIVS_ARE_ZERO     =    8;
    private static final int DERIVS_SAME_AS_PREV =    4;
    private Stm outputGCData(HMethod hm) {
	List gcps = m_gc.gcPoints(hm);
	Util.assert(gcps != null);
	List stmlist = new ArrayList();
	// first output base table for stack locations
	Set baseTableSet = new HashSet();
	for(Iterator it = gcps.iterator(); it.hasNext(); ) {
	    Set stack = ((GCPoint)it.next()).liveStackOffsetLocs();
	    baseTableSet.addAll(stack);
	}
	if (!baseTableSet.isEmpty()) {
	    short size = (short)baseTableSet.size();
	    report("Number of entries in base table = " + size);
	    size *= 4; // each entry is a 32-bit integer
	    size += 2; // account for size field
	    size |= 1 << (2 * BYTESIZE - 1); // flag as base table
	    stmlist.add(_DATUM(new CONST(tf, null, size)));
	}
	// map each live StackOffsetLoc to an index
	Map baseTableMap = new HashMap();
	int counter = 0;
	for(Iterator it = baseTableSet.iterator(); it.hasNext(); ) {
	    StackOffsetLoc sol = (StackOffsetLoc)it.next();
	    report("Base table entry: " + sol.stackOffset());
	    stmlist.add(_DATUM(new CONST(tf, null, sol.stackOffset())));
	    baseTableMap.put(sol, new Integer(counter++));
	}
	Util.assert(baseTableSet.size() == counter &&
		    baseTableMap.size() == counter);
	int index = 0;
	GCPoint prev = null;
	for(Iterator it = gcps.iterator(); it.hasNext(); ) {
	    GCPoint gcp = (GCPoint)it.next();
	    byte desc = 0; // descriptor
	    boolean needRegs = true;
	    boolean needStack = true;
	    boolean needDerivs = true;
	    Set regs = gcp.liveMachineRegLocs();
	    Set stack = gcp.liveStackOffsetLocs();
	    Map derivs = gcp.liveDerivations();
	    if (regs.isEmpty()) {
		desc |= REGS_ARE_ZERO;
		needRegs = false;
	    } else if (prev != null && 
		       regs.equals(prev.liveMachineRegLocs())) {
		desc |= REGS_SAME_AS_PREV;
		needRegs = false;
	    }
	    if (stack.isEmpty()) {
		desc |= STACKLOCS_ARE_ZERO;
		needStack = false;
	    } else if (prev != null &&
		       stack.equals(prev.liveStackOffsetLocs())) {
		desc |= STACK_SAME_AS_PREV;
		needStack = false;
	    }   
	    if (derivs.isEmpty()) {
		desc |= DERIVS_ARE_ZERO;
		needDerivs = false;
	    } else if (prev != null && 
		       derivs.equals(prev.liveDerivations())) {
		desc |= DERIVS_SAME_AS_PREV;
		needDerivs = false;
	    }
	    List tmplist = new ArrayList();
	    // generate data
	    tmplist.add(_DATUM(gcp.label()));
	    tmplist.add(_DATUM(new CONST(tf, null, desc))); // descriptor
	    report("Descriptor: " + Integer.toBinaryString((int)desc));
 	    short size = (short)(m_tb.POINTER_SIZE + 3 * m_tb.WORD_SIZE);
	    if (needRegs) {
		tmplist.add(outputRegs(regs));
		size += numBR; 
	    }
	    if (needStack) {
		int numBSE = // number of bytes needed for stack entries
		    (int)Math.ceil(((double)counter)/((double)BYTESIZE));
		tmplist.add(outputStack(stack, baseTableMap, numBSE));
		size += numBSE;
	    }
	    if (needDerivs) {
		tmplist.add(outputDerivs(derivs));
		size += sizeD;
	    }
	    stmlist.add(_DATUM(new CONST(tf, null, size)));
	    stmlist.add(Stm.toStm(tmplist));
	    prev = gcp;
	} // for loop
	return Stm.toStm(stmlist);
    } // outputGCData
    private int sizeD;
    // dump register data in 8-bit chunks
    private Stm outputRegs(Set regs) {
	List stmlist = new ArrayList();
	byte[] data = new byte[numBR];
	for(Iterator it=regs.iterator(); it.hasNext(); ) {
	    int regIndex = ((MachineRegLoc)it.next()).regIndex();
	    int i = regIndex / BYTESIZE;
	    int j = regIndex % BYTESIZE;
	    data[i] |= 1 << (BYTESIZE - j - 1);
	}
	for(int k=0; k < numBR; k++) {
	    stmlist.add(_DATUM(new CONST(tf, null, data[k])));
	    report("Registers: " + Integer.toBinaryString((int)data[k]));
	}
	return Stm.toStm(stmlist);
    }
    // dump stack data in 8-bit chunks
    private Stm outputStack(Set stack, Map m, int size) {
	List stmlist = new ArrayList();
	byte[] data = new byte[size];
	for(Iterator it=stack.iterator(); it.hasNext(); ) {
	    int index = ((Integer)m.get(it.next())).intValue();
	    int i = index / BYTESIZE;
	    int j = index % BYTESIZE;
	    data[i] |= 1 << (BYTESIZE - j - 1);
	}
	for(int k=0; k < size; k++) {
	    stmlist.add(_DATUM(new CONST(tf, null, data[k])));
	    report("Stack Offsets: " + Integer.toBinaryString((int)data[k]));
	}
	return Stm.toStm(stmlist);
    }
    private Stm outputDerivs(Map derivs) {
	Util.assert(!derivs.isEmpty());
	List stmlist = new ArrayList();
	stmlist.add(_DATUM(new CONST(tf, null, (byte)derivs.size())));
	sizeD = 1;
	for(Iterator it=derivs.keySet().iterator(); it.hasNext(); ) {
	    CommonLoc cl = (CommonLoc)it.next();
	    int deriv = 0;
	    if (cl.kind() == StackOffsetLoc.KIND) {
		deriv = ((StackOffsetLoc)cl).stackOffset();
	    } else if (cl.kind() == MachineRegLoc.KIND) {
		deriv = ((MachineRegLoc)cl).regIndex();
	    } else {
		Util.assert(false, "neither Loc case came up");
	    }
	    stmlist.add(_DATUM(new CONST(tf, null, deriv)));
	    sizeD += 4; // 32-bits for the above Temp
	    List rEntries = new ArrayList();
	    List sEntries = new ArrayList();
	    Derivation.DList dl = (Derivation.DList)derivs.get(cl);
	    while(dl != null) {
		if (((CommonLoc)dl.base).kind() ==StackOffsetLoc.KIND) {
		    sEntries.add(dl);
		} else if (((CommonLoc)dl.base).kind() ==MachineRegLoc.KIND) {
		    rEntries.add(dl);
		} else {
		    Util.assert(false, "neither Loc case came up");
		}
		dl = dl.next;
	    }
	    stmlist.add(_DATUM(new CONST(tf, null, (byte)rEntries.size())));
	    stmlist.add(_DATUM(new CONST(tf, null, (byte)sEntries.size())));
	    sizeD += 2;
	    if (rEntries.size() != 0) {
		byte[] rData = new byte[numBR];
		byte[] rSign = new byte[numBR];
		for(Iterator rIt = rEntries.iterator(); rIt.hasNext(); ) {
		    Derivation.DList rDL = (Derivation.DList)rIt.next();
		    int regIndex = ((MachineRegLoc)rDL.base).regIndex();
		    int i = regIndex / BYTESIZE;
		    int j = regIndex % BYTESIZE;
		    rData[i] |= 1 << (BYTESIZE - j - 1);
		    rSign[i] |= (rDL.sign ? 0 : 1) << (BYTESIZE - j - 1);
		}
		for(int k=0; k < numBR; k++)
		    stmlist.add(_DATUM(new CONST(tf, null, rSign[k])));
		for(int k=0; k < numBR; k++)
		    stmlist.add(_DATUM(new CONST(tf, null, rData[k])));
		sizeD += 2 * numBR;
	    }
	    if (sEntries.size() != 0) {
		int numBSD = 
		    (int)Math.ceil((double)sEntries.size()/(double)BYTESIZE);
		byte[] sSign = new byte[numBSD];
		int i = 0, j = 0;
		List tmplist = new ArrayList();
		for(Iterator sIt = sEntries.iterator(); sIt.hasNext(); ) {
		    Derivation.DList sDL = (Derivation.DList)sIt.next();
		    int stackOffset = ((StackOffsetLoc)sDL.base).stackOffset();
		    tmplist.add(_DATUM(new CONST(tf, null, stackOffset)));
		    sSign[i] |= (sDL.sign ? 0 : 1) << (BYTESIZE - j - 1);
		    if (j == BYTESIZE - 1) {
			i++; j = 0;
		    } else j++;
		}
		for(int k=0; k < numBSD; k++)
		    stmlist.add(_DATUM(new CONST(tf, null, sSign[k])));
		stmlist.add(Stm.toStm(tmplist));
		sizeD += numBSD + (8 * i + j) * 4;
	    }
	}
	return Stm.toStm(stmlist);
    }
    private void report(String str) {
	if (DEBUG) System.out.println(str);
    }
}












