// InstrFactory.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.Temp.TempFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;

/**
 * A <code>InstrFactory</code> is responsible for generating 
 * generic <code>Assem.Instr</code>s used in code generation.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: InstrFactory.java,v 1.1.2.1 1999-02-09 05:45:33 andyb Exp $
 */
public abstract class InstrFactory {

    public abstract TempFactory tempFactory();

    public abstract HCode getParent(); 

    public HMethod getMethod() { return getParent().getMethod(); }

    abstract int getUniqueID();

    public String toString() {
        return "InstrFactory["+getParent().toString()+"]";
    }
}
