// AddrMode4.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;

import java.util.Vector;

/**
 * <code>AddrMode4</code> is the abstract superclass for StrongARM
 * opcodes which use ARMv4 addressing mode four operands.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: AddrMode4.java,v 1.1.2.1 1999-02-08 05:35:14 andyb Exp $
 */
public abstract class AddrMode4 extends SAInsn {

    /** 
     * Constructor for instructions of form:
     * opcode<stackmode> Rd{!}, <registers>{^} */
    AddrMode4(SAInsnFactory saif, HCodeElement source, Temp dst, 
              Vector srclist, int stackmode, boolean updbase,
              boolean loadstate, int condcode) {
        super(saif, source);
    }
}
