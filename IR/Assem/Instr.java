// Instr.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.Util.ArrayFactory;
import harpoon.IR.Properties.UseDef;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Temp.Temp;

/**
 * <code>Instr</code> is an supperclass representation for
 * all of the assembly-level instruction representations used in
 * the Backend.* packages.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Instr.java,v 1.1.2.2 1999-02-09 05:45:33 andyb Exp $
 */
public abstract class Instr implements HCodeElement, UseDef {
    private String assem;

    public Temp[] use() { return null; }

    public Temp[] def() { return null; }

    /** Returns a list of <code>Instr</code>s which are possible targets of
     *  jumps from the current instruction */
    public abstract Instr[] jumps();

    public String getSourcefile() { return null; }

    public int getLineNumber() { return 0; }

    public int getID() { return 0; }
   
    /** Returns the assembly-level instruction as a String, with
     *  <code>Temp</code>s represented either by their temp name or
     *  (if the register allocator has been run), by their register. */
    public abstract String format(TempMap tm);

    public static final ArrayFactory arrayFactory =
        new ArrayFactory() {
            public Object[] newArray(int len) { return new Instr[len]; }
        };
}
