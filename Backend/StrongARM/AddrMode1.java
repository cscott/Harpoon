// AddrMode1.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;

/**
 * <code>AddrMode1</code> is the abstract superclass for StrongARM
 * opcodes which use ARMv4 addressing mode one operands.
 * XXX - this is incomplete, needs more constructors, or better,
 * a class to encapsulate the addressing mode 1 <shifter_operand>.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: AddrMode1.java,v 1.1.2.1 1999-02-08 05:35:14 andyb Exp $
 */
public abstract class AddrMode1 extends SAInsn {
    /* indicates whether this instruction should be conditionally
     * executed depending on the conditional code flags. */
    protected int condcode;
    /* indicates whether this instruction should update the 
     * conditional code flags. */
    protected boolean updflags;

    /** constructor for opcodes of form:
     *  opcode Rd, #immed */
    AddrMode1(SAInsnFactory saif, HCodeElement source, Temp dst, 
              int immed, boolean updflags, int condcode) {
        super(saif, source); this.updflags = updflags;
        this.condcode = condcode;
    }

    /** constructor for opcodes of form:
     *  opcode Rd, Rs, #immed */
    AddrMode1(SAInsnFactory saif, HCodeElement source, Temp reg1, Temp reg2,
              int immed, boolean updflags, int condcode) {
        super(saif, source); this.updflags = updflags;
        this.condcode = condcode;
    }

    /** constructor for opcodes of form:
     *  opcode Rd, Rs */
    AddrMode1(SAInsnFactory saif, HCodeElement source, Temp reg1, Temp reg2,
              boolean updflags, int condcode) {
        super(saif, source); this.updflags = updflags;
        this.condcode = condcode;
    }

    /** constructor for opcodes of form:
     *  opcode Rd, Rs1, Rs2 */
    AddrMode1(SAInsnFactory saif, HCodeElement source, Temp reg1, Temp reg2,
              Temp reg3, boolean updflags, int condcode) {
        super(saif, source); this.updflags = updflags;
        this.condcode = condcode;
    }
}
