// Semaphore.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;

/**
 * <code>Branch</code> is the abstract superclass for StrongARM
 * opcodes from ARMv4 to perform semaphore instructions.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Semaphore.java,v 1.1.2.1 1999-02-08 05:35:15 andyb Exp $
 */
public abstract class Semaphore extends SAInsn {

    /** 
     * Constructor for instructions of form:
     * opcode Rd, Rs1, [Rs2] */
    Semaphore(SAInsnFactory saif, HCodeElement source, Temp dest,
              Temp source1, Temp source2, int condcode) {
        super(saif, source);
    }
}
