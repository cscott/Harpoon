// Instr.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.Temp.TempMap;
import harpoon.Temp.Temp;

/**
 * <code>Instr</code> is an abstract supperclass representation for
 * all of the assembly-level instruction representations used in
 * the Backend.* packages.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Instr.java,v 1.1.2.1 1999-02-08 05:33:19 andyb Exp $
 */
public abstract class Instr {

    /**
     * Returns all of the <code>Temp</code>s which are used by this
     * <code>Instr</code> */
    public abstract Temp[] use();
    
    /**
     * Returns all of the <code>Temp</code>s which are defined by this
     * <code>Instr</code> */
    public abstract Temp[] def();
    
    /**
     * Returns a list of <code>Instr</code>s which are possible targets of
     * jumps from the current instruction */
    public abstract Instr[] jumps();
    
    /**
     * Returns the assembly-level instruction as a String, with
     * <code>Temp</code>s represented either by their temp name or
     * (if the register allocator has been run), by their register. */
    public abstract String format(TempMap tm);
}
