// Renameable.java, created Wed Sep 16 02:16:17 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Properties;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
/**
 * <code>Renameable</code> defines an interface for renaming temporaries
 * in an intermediate representation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Renameable.java,v 1.2.2.1 1998-12-11 22:21:03 cananian Exp $
 */

public interface Renameable  {
    /** Rename all used variables in this <code>HCodeElement</code> 
     *  according to mapping <code>tm</code>. */
    public HCodeElement rename(TempMap tm);
}
