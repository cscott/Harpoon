// SAInsnFactory.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.ClassFile.HMethod;
import harpoon.Temp.TempFactory;

/**
 * A <code>SAInsnFactory</code> is responsible for generating unique
 * IDs for the <code>SAInsn</code>s in a method, and for
 * keeping some method-wide information.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: SAInsnFactory.java,v 1.1.2.1 1999-02-08 00:54:31 andyb Exp $
 */

public abstract class SAInsnFactory {

    public abstract TempFactory tempFactory();
    
    public abstract Code getParent();

    public HMethod getMethod() { return getParent().getMethod(); }

    abstract int getUniqueID();

    public String toString() {
        return "SAInsnFactory["+getParent().toString()+"]";
    }
}
