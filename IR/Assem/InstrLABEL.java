// InstrLABEL.java, created Wed Feb 17 16:32:40 1999 by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Label;
import harpoon.Temp.TempMap;
import harpoon.Util.CombineIterator;
import harpoon.Util.Default;
import harpoon.Util.Collections.UnmodifiableIterator;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * <code>InstrLABEL</code> is used to represents code labels in
 * assembly-level instruction representations.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: InstrLABEL.java,v 1.5 2004-02-07 21:28:47 cananian Exp $
 */
public class InstrLABEL extends Instr {
    private Label label;

    /** Create a code label <code>Instr</code>. The specified
	<code>String</code> <code>a</code> should be the
	assembly-language representation of the given
	<code>Label</code> <code>l</code>. */
    public InstrLABEL(InstrFactory inf, HCodeElement src, String a, Label l) {
        this(inf, src, a, l, true);
    } 

    private InstrLABEL(InstrFactory inf, HCodeElement src, 
		       String a, Label l, boolean falls) {
	super(inf, src, a, null, null, falls, null);
        label = l;
	inf.labelToInstrLABELmap.put(l, this);	
    }

    public static InstrLABEL makeNoFall
	(InstrFactory inf, HCodeElement src, String a, Label l) {
	return new InstrLABEL(inf, src, a, l, false);
    }

    /** Return the code label specified in the constructor. */
    public Label getLabel() { return label; }
    // should clone label!!!!!!!

    public Instr rename(InstrFactory inf, TempMap defMap, TempMap useMap) {
	// should clone label or something.
	return new InstrLABEL(inf, this, getAssem(), label);
    }
    public Instr cloneMutateAssem(InstrFactory inf, String newAssem) {
	return new InstrLABEL(inf, this, newAssem, label);
    }

    /** Accept a visitor. */
    public void accept(InstrVisitor v) { v.visit(this); }

    /** Returns true.
	Labels are designed to have multiple predecessors.
    */
    protected boolean hasMultiplePredecessors() {
	return true;
    }

    public List<InstrEdge> predC() {
	// XXX not entirely happy w/ this.  We're taking an O(s.size())
	// performance hit on each call to predC().  But this beats taking
	// the same hit on each call to precC().get().
	Set<Instr> s = getFactory().labelToBranches.get(label);
	final List<Instr> branchesHere =
	    Arrays.asList(s.toArray(new Instr[s.size()]));
	return new AbstractList<InstrEdge>() {
	    public int size() {
		int total=0;
		if (prev!=null && prev.canFallThrough) {
		    total++;
		}

		total += branchesHere.size();

		return total;
	    }
	    public InstrEdge get(int i) {
		// first iterator: prev falls to this?
		if ((prev != null) && prev.canFallThrough) {
		    if (i--==0) return new InstrEdge(prev, InstrLABEL.this);
		}
		// second iterator: branches to this?
		// hm. need to index these; using the static branchesHere
		// list may be non-optimal.
		return new InstrEdge(branchesHere.get(i), InstrLABEL.this);
	    }
	};
    }

    public boolean isLabel() { return true; }
}
