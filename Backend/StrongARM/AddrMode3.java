// AddrMode3.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;

/**
 * <code>AddrMode3</code> is the abstract superclass for StrongARM
 * opcodes which use ARMv4 addressing mode one operands.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: AddrMode3.java,v 1.1.2.1 1999-02-08 05:35:14 andyb Exp $
 */
public abstract class AddrMode3 extends SAInsn {
    
    AddrMode3(SAInsnFactory saif, HCodeElement source, Temp reg1, Temp reg2,
              int immed, boolean add, boolean preindex, boolean updbase,
              int condcode) {
        super(saif, source);
    }

    AddrMode3(SAInsnFactory saif, HCodeElement source, Temp reg1, Temp reg2,
              Temp reg3, boolean add, boolean preindex, boolean updbase,
              int condcode) {
        super(saif, source);
    }
}
