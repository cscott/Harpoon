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
 * @version $Id: InstrLABEL.java,v 1.1.2.1 1999-02-17 21:32:40 andyb Exp $
 */
public class InstrLABEL extends Instr {
    public Label label;

    InstrLABEL(InstrFactory inf, HCodeElement src, String a, Label l) {
        super(inf, src, a, null, null);
        label = l;
    } 
}
