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
 * @version $Id: InstrLABEL.java,v 1.1.2.4 1999-05-25 16:45:13 andyb Exp $
 */
public class InstrLABEL extends Instr {
    private Label label;

    public InstrLABEL(InstrFactory inf, HCodeElement src, String a, Label l) {
        super(inf, src, a, null, null);
        label = l;
    } 

    /** Return the code label used in this. */
    public Label getLabel() { return label; }
    // should clone label!!!!!!!

    /** Accept a visitor. */
    public void visit(InstrVisitor v) { v.visit(this); }
}
