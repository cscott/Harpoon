// InstrLABEL.java, created Wed Feb 17 16:32:40 1999 by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Label;
import harpoon.Util.CombineIterator;
import harpoon.Util.UnmodifiableIterator;
import harpoon.Util.Default;

import java.util.Iterator;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Set;

/**
 * <code>InstrLABEL</code> is used to represents code labels in
 * assembly-level instruction representations.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: InstrLABEL.java,v 1.1.2.10 1999-08-31 01:20:38 pnkfelix Exp $
 */
public class InstrLABEL extends Instr {
    private Label label;

    /** Create a code label <code>Instr</code>. The specified
	<code>String</code> <code>a</code> should be the
	assembly-language representation of the given
	<code>Label</code> <code>l</code>. */
    public InstrLABEL(InstrFactory inf, HCodeElement src, String a, Label l) {
        super(inf, src, a, null, null);
        label = l;
	inf.labelToInstrLABELmap.put(l, this);
    } 

    /** Return the code label specified in the constructor. */
    public Label getLabel() { return label; }
    // should clone label!!!!!!!

    /** Accept a visitor. */
    public void visit(InstrVisitor v) { v.visit(this); }

    /** Returns true.
	Labels are designed to have multiple predecessors.
    */
    protected boolean hasMultiplePredecessors() {
	return true;
    }

    public Collection predC() {
	return new AbstractCollection() {
	    public int size() {
		int total=0;
		if (prev!=null && prev.canFallThrough) {
		    total++;
		}

		Set s = (Set) getFactory().labelToBranchingInstrSetMap.get(label);
		total += s.size();

		return total;
	    }
	    public Iterator iterator() {
		return new CombineIterator
		    (new Iterator[] {
			
			// first iterator: prev falls to this?
			((prev!=null && prev.canFallThrough)?
			 Default.singletonIterator
			 (new InstrEdge(prev, InstrLABEL.this)):
			 Default.nullIterator),

			// second iterator: branches to this?
			new UnmodifiableIterator() {
			    Iterator instrs = 
				((Set) (getFactory().labelToBranchingInstrSetMap.get
					(label))).iterator();
			    public boolean hasNext() { return instrs.hasNext(); }
			    public Object next() { 
				return new InstrEdge
				    ((Instr)instrs.next(), InstrLABEL.this); 
			    }
			}});
	    }
	};
    }
}
