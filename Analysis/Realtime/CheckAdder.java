// CheckAdder.java, created Tue Jan 23 16:09:50 2001 by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import java.io.PrintWriter;

import harpoon.Analysis.Transformation.MethodMutator;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;

import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ARRAYINIT;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.SET;

import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadWithTry;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import harpoon.Util.HClassUtil;
import harpoon.Util.Util;

/**
 * <code>CheckAdder</code> attaches <code>javax.realtime.MemoryArea</code>s to
 * <code>NEW</code>s and <code>ANEW</code>s.  It also adds checks around 
 * <code>SET</code>s and <code>ASET</code>s only if the 
 * <code>CheckRemoval</code> indicates that the check cannot be removed.
 *
 * @author Wes Beebee <wbeebee@mit.edu>
 * @version $Id: CheckAdder.java,v 1.3 2002-02-26 22:41:57 cananian Exp $
 */

// Fix to be non-static...

abstract class CheckAdder extends MethodMutator {
    protected CheckRemoval checkRemoval;
    protected NoHeapCheckRemoval noHeapCheckRemoval;

    public boolean fastNew = true;
    public boolean smartMemAreaLoads = false;
    public boolean debugOutput = false;

    /** Creates a new <code>CheckAdder</code>, adding only the checks that
     *  can't be removed as specified by <code>CheckRemoval</code> and 
     *  <code>NoHeapCheckRemoval</code>.
     *  Use <code>hcf = (new CheckAdder(cr, nhcr, hcf)).codeFactory(); to link
     *  this <code>CheckAdder</code> into the <code>HCodeFactory</code> chain.
     */

    CheckAdder(CheckRemoval cr, NoHeapCheckRemoval nhcr, HCodeFactory parent) {
	super(parent);
	checkRemoval = cr;

	noHeapCheckRemoval = nhcr;
    }

    /** Adds the checks to the code <code>input</code>. */

    abstract protected HCode mutateHCode(HCodeAndMaps input);

    protected HCode mutateHCode(HCodeAndMaps input, QuadVisitor visitor) {
	Stats.realtimeBegin();
	HCode hc = input.hcode();
	if (hc == null) {
	    Stats.realtimeEnd();
	    return hc;
	}
	HClass hclass = hc.getMethod().getDeclaringClass();
	if (hclass.getName().startsWith("javax.realtime.")) {
	    Stats.realtimeEnd();
	    return hc;
	}
	final Linker linker = hclass.getLinker();
	
	if (debugOutput) {
	    System.out.println("Before:");
	    hc.print(new PrintWriter(System.out));
	}

	Quad[] ql = (Quad[]) hc.getElements();
	for (int i=0; i<ql.length; i++) 
	    ql[i].accept(visitor);

	if (debugOutput) {
	    System.out.println("After:");
	    hc.print(new PrintWriter(System.out));
	}

	Stats.realtimeEnd();
	return hc;
    }

    /** Indicates if the given instruction needs an access check wrapped 
     *  around it. 
     */
    
    protected boolean needsCheck(Quad inst) {
	Stats.analysisBegin();
	boolean removeCheck = checkRemoval.shouldRemoveCheck(inst);
	Stats.analysisEnd();
	if (removeCheck) {
	    Stats.addRemovedMemCheck();
	} else {
	    Stats.addActualMemCheck();
	}
	return !removeCheck;
    }

    /** Looks up the type of the parent <code>HCodeFactory</code> and constructs
     *  the appropriate <code>HCodeFactory</code> from the correct 
     *  <code>CheckAdder</code>.  A <code>QuadWithTry</code> is created for an input 
     *  <code>QuadWithTry</code>, a <code>QuadNoSSA</code> is created for an input
     *  <code>QuadNoSSA</code>.  Currently, no other quadforms are supported.
     */

    public static HCodeFactory codeFactory(CheckRemoval cr, 
					   NoHeapCheckRemoval nhcr,
					   HCodeFactory parent) {
	String codeName = parent.getCodeName();
	if (codeName.equals(QuadNoSSA.codename)) {
	    return (new CheckAdderNoSSA(cr, nhcr, parent)).codeFactory();
	} else if (codeName.equals(QuadWithTry.codename)) {
	    return (new CheckAdderWithTry(cr, nhcr, parent)).codeFactory();
	} else {
	    Util.ASSERT(false, "Quads type: " + codeName + " not a supported CheckAdder");
	    return null;
	}
    }
}

