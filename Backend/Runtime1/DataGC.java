// DataGC.java, created Thu Jan 13 20:55:26 2000 by kkz
// Copyright (C) 1999 Karen K. Zee <kkz@tesuji.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.GCInfo;
import harpoon.Backend.Generic.GCInfo.GCPoint;
import harpoon.Backend.Generic.RegFileInfo;
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
 * @version $Id: DataGC.java,v 1.1.2.2 2000-02-01 20:42:18 kkz Exp $
 */
public class DataGC extends Data {
    final protected GCInfo m_gc;
    final protected NameMap m_nm;
    final protected int numInts;
    final protected static boolean DEBUG = true;
    
    /** Creates a <code>DataGC</code>. */
    static final int BITS_PER_INT = 32;
    public DataGC(Frame f, HClass hc, ClassHierarchy ch) {
        super("gc-data", hc, f);
	Util.assert(f.getGCInfo() != null);
	m_gc = f.getGCInfo();
	m_nm = f.getRuntime().nameMap;
	root = build(ch);
	double maxRI = (double)f.getRegFileInfo().maxRegIndex();
	numInts = (int)Math.ceil(maxRI/((double)BITS_PER_INT));
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
	    if (!Modifier.isAbstract(hm.getModifiers())) {
		Util.assert(m_gc.gcPoints(hm) != null);
		stmlist.add(outputGCData(hm));
	    }
	}

	stmlist.add(new LABEL(tf, null, m_nm.label(hc, "gc"), true));
	// name of class
	stmlist.add(_DATUM(m_nm.label(hc, "namestr")));
	// number of methods
	int size = hc.getMethods().length;
	stmlist.add(_DATUM(new CONST(tf, null, size)));
	// address of the class gc label
	stmlist.add(_DATUM(m_nm.label(hc, "gc")));
	return (HDataElement) Stm.toStm(stmlist);
    }
    private static final int REGS_ARE_ZERO       = -128;
    private static final int REGS_SAME_AS_PREV   =   64;
    private static final int STACKLOCS_ARE_ZERO  =   32;
    private static final int STACK_SAME_AS_PREV  =   16;
    private static final int DERIVS_ARE_ZERO     =    8;
    private static final int DERIVS_SAME_AS_PREV =    4;
    private static final int HAS_LABEL_TO_PREV   =    2;
    private Stm outputGCData(HMethod hm) {
	List stmlist = new ArrayList();
	// first output base table for stack locations
	Set baseTableSet = new HashSet();
	for(Iterator it=m_gc.gcPoints(hm).iterator(); it.hasNext(); ) {
	    Set stack = ((GCPoint)it.next()).liveStackOffsetLocs();
	    baseTableSet.addAll(stack);
	}
	if (!baseTableSet.isEmpty()) {
	    stmlist.add(_DATUM(m_nm.label(hm, "gcbt")));
	    stmlist.add(_DATUM(new CONST(tf, null, baseTableSet.size())));
	    if (DEBUG) System.out.println("Size of base table = " +
					  baseTableSet.size());
	}
	// map each live StackOffsetLoc to a numbered bit
	Map baseTableMap = new HashMap();
	int counter = 0;
	for(Iterator it = baseTableSet.iterator(); it.hasNext(); ) {
	    StackOffsetLoc sol = (StackOffsetLoc)it.next();
	    stmlist.add(_DATUM(new CONST(tf, null, sol.stackOffset())));
	    baseTableMap.put(sol, new Integer(counter++));
	}
	int index = 0;
	GCPoint prev = null;
	Label prevL = null;
	for(Iterator it=m_gc.gcPoints(hm).iterator(); it.hasNext(); ) {
	    GCPoint gcp = (GCPoint)it.next();
	    byte desc = 0; // descriptor
	    boolean needRegs = true;
	    boolean needStack = true;
	    boolean needDerivs = true;
	    boolean needPrev = false;
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
		needPrev = true;
	    }
	    if (stack.isEmpty()) {
		desc |= STACKLOCS_ARE_ZERO;
		needStack = false;
	    } else if (prev != null &&
		       stack.equals(prev.liveStackOffsetLocs())) {
		desc |= STACK_SAME_AS_PREV;
		needStack = false;
		needPrev = true;
	    }   
	    if (derivs.isEmpty()) {
		desc |= DERIVS_ARE_ZERO;
		needDerivs = false;
	    } else if (prev != null && 
		       derivs.equals(prev.liveDerivations())) {
		desc |= DERIVS_SAME_AS_PREV;
		needDerivs = false;
		needPrev = true;
	    }
	    if (needPrev) desc |= HAS_LABEL_TO_PREV;
	    // dump data
	    Label l = new Label(m_nm.mangle(hm, "gcdata_"+index++));
	    stmlist.add(new LABEL(tf, null, l, true));
	    stmlist.add(_DATUM(gcp.label()));
	    stmlist.add(_DATUM(new CONST(tf, null, desc))); // descriptor
	    if (DEBUG) System.out.println("Descriptor: " + 
					  Integer.toBinaryString((int)desc));
	    if (needPrev) stmlist.add(_DATUM(prevL));
	    if (needRegs) stmlist.add(outputRegs(regs));
	    int numSE = // number of stack entries
		(int)Math.ceil(((double)counter)/((double)BITS_PER_INT));
	    if (needStack) 
		stmlist.add(outputStack(stack, baseTableMap, numSE));
	    //if (needDerivs) stmlist.add(outputDerivs(derivs));
	    prev = gcp;
	    prevL = l;
	} // for loop
	return Stm.toStm(stmlist);
    } // outputGCData
    // dump register information in 32-bit chunks
    private Stm outputRegs(Set regs) {
	List stmlist = new ArrayList();
	int[] data = new int[numInts];
	for(Iterator it=regs.iterator(); it.hasNext(); ) {
	    int regIndex = ((MachineRegLoc)it.next()).regIndex();
	    int i = regIndex/BITS_PER_INT;
	    int j = regIndex%BITS_PER_INT;
	    data[i] |= 1 << (BITS_PER_INT - j - 1);
	}
	for(int k=0; k < numInts; k++) {
	    stmlist.add(_DATUM(new CONST(tf, null, data[k])));
	    if (DEBUG) System.out.println("Registers: " +
					  Integer.toBinaryString(data[k]));
	}
	return Stm.toStm(stmlist);
    }
    private Stm outputStack(Set stack, Map m, int size) {
	List stmlist = new ArrayList();
	int[] data = new int[size];
	for(Iterator it=stack.iterator(); it.hasNext(); ) {
	    int index = ((Integer)m.get(it.next())).intValue();
	    int i = index/BITS_PER_INT;
	    int j = index%BITS_PER_INT;
	    data[i] |= 1 << (BITS_PER_INT - j - 1);
	}
	for(int k=0; k < size; k++) {
	    stmlist.add(_DATUM(new CONST(tf, null, data[k])));
	    if (DEBUG) System.out.println("Stack Offsets: " +
					  Integer.toBinaryString(data[k]));
	}
	return Stm.toStm(stmlist);
    }
    private void outputDerivs(Map derivs) {
    }
}












