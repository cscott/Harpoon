// Multiply.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;

/**
 * <code>Multiply</code> is the abstract superclass for StrongARM
 * opcodes from ARMv4 to perform integer multiplication.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Multiply.java,v 1.1.2.1 1999-02-08 05:35:15 andyb Exp $
 */
public abstract class Multiply extends SAInsn {

    Multiply(SAInsnFactory saif, HCodeElement source, Temp reg1,
             Temp reg2, Temp reg3, boolean updflags, int condcode) {
        super(saif, source);
    }

    Multiply(SAInsnFactory saif, HCodeElement source, Temp reg1,
             Temp reg2, Temp reg3, Temp reg4, boolean updflags,
             int condcode) {
        super(saif, source);
    }
}
