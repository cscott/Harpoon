// AddrMode2.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;

/**
 * <code>AddrMode2</code> is the abstract superclass for StrongARM
 * opcodes which use ARMv4 addressing mode two operands.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: AddrMode2.java,v 1.1.2.1 1999-02-08 05:35:14 andyb Exp $
 */
public abstract class AddrMode2 extends SAInsn {
    Temp dst, source1, source2;
    int shift, immed, condcode;
    boolean add, preindex, updbase;

    /** 
     * Constructor for opcodes of form:
     * opcode Rd, [Rn, #+/-<12_bit_offset>]{!}
     * opcode Rd, [Rn], #+/-<12_bit_offset> */
    AddrMode2(SAInsnFactory saif, HCodeElement source, Temp dst,
              Temp source1, int immed, boolean add, boolean preindex, 
              boolean updbase, int condcode) {
        super(saif, source);
    }

    /** 
     * Constructor for opcode of form:
     * opcode Rd, [Rn, +/-Rm]{!}
     * opcode Rd, [Rn], +/-Rm */
    AddrMode2(SAInsnFactory saif, HCodeElement source, Temp dst,
              Temp source1, Temp source2, boolean add, boolean preindex,
              boolean updbase, int condcode) {
        super(saif, source);
    }

    /**
     * Constructor for opcode of form:
     * opcode Rd, [Rn, +/-Rm, <shift> #<shift_imm>]{!}
     * opcode Rd, [Rn], +/-Rm, <shift> #<shift_imm */
    AddrMode2(SAInsnFactory saif, HCodeElement source, Temp dst,
              Temp source1, Temp source2, int shift, int immed, boolean add, 
              boolean preindex, boolean updbase, int condcode) {
        super(saif, source);
    }
}
