// InstrLABEL.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Label;

/**
 * <code>InstrLABEL</code> is used to represents code labels in
 * assembly-level instruction representations.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: InstrLABEL.java,v 1.1.2.5 1999-06-29 05:21:24 cananian Exp $
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
    } 

    /** Return the code label specified in the constructor. */
    public Label getLabel() { return label; }
    // should clone label!!!!!!!

    /** Accept a visitor. */
    public void visit(InstrVisitor v) { v.visit(this); }
}
