// PreallocatedMemory.java, created Tue Nov 26 16:02:05 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Runtime.PreallocOpt;

/**
 * <code>PreallocatedMemory</code> is a wrapper for the static fields
 * pointing to the pre-allocated chunks of memory.  These fields will
 * be added by FLEX itself, during code generation.
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: PreallocatedMemory.java,v 1.3 2003-03-03 23:38:29 salcianu Exp $ */
public abstract class PreallocatedMemory {
    // FLEX will fill in the code of this method such that it
    // pre-allocates memory and initializes the static fields of this
    // class; don't add any Java code to it
    public static void initFields() {}
}
