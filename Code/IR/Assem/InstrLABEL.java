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

import java.util.Iterator;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Set;

/**
 * <code>InstrLABEL</code> is used to represents code labels in
 * assembly-level instruction representations.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: InstrLABEL.java,v 1.4 2002-08-30 22:39:11 cananian Exp $
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

    public Collection<InstrEdge> predC() {
	return new AbstractCollection<InstrEdge>() {
	    public int size() {
		int total=0;
		if (prev!=null && prev.canFallThrough) {
		    total++;
		}

		Set s = getFactory().labelToBranches.get(label);
		total += s.size();

		return total;
	    }
	    public Iterator<InstrEdge> iterator() {
		return new CombineIterator<InstrEdge>
		    (new Iterator<InstrEdge>[] {
			
			// first iterator: prev falls to this?
			((prev!=null && prev.canFallThrough)?
			 Default.singletonIterator
			 (new InstrEdge(prev, InstrLABEL.this)):
			 Default.nullIterator),

			// second iterator: branches to this?
			new UnmodifiableIterator<InstrEdge>() {
			    Iterator<Instr> instrs = 
				getFactory().labelToBranches.get
					(label).iterator();
			    public boolean hasNext() { return instrs.hasNext(); }
			    public InstrEdge next() { 
				return new InstrEdge
				    ((Instr)instrs.next(), InstrLABEL.this); 
			    }
			}});
	    }
	};
    }

    public boolean isLabel() { return true; }
}
