// InstrDIRECTIVE.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Label;

/**
 * <code>InstrDIRECTIVE</code> is used to represents assembler
 * directives.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: InstrDIRECTIVE.java,v 1.1.2.1 1999-05-17 20:08:00 andyb Exp $
 */
public class InstrDIRECTIVE extends Instr {

    public InstrDIRECTIVE(InstrFactory inf, HCodeElement src, String a) {
        super(inf, src, a, null, null);
    } 

    public void visit(InstrVisitor v) { v.visit(this); }
}
