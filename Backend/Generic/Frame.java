// Frame.java, created Fri Feb  5 05:48:12 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.Temp.Temp;

/**
 * A <code>Frame</code> encapsulates the machine-dependent information
 * needed for compilation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Frame.java,v 1.1.2.2 1999-02-09 05:45:32 andyb Exp $
 */
public abstract class Frame  {
    /** Returns <code>false</code> if pointers can be represented in
     *  32 bits, or <code>true</code> otherwise. */
    public abstract boolean pointersAreLong();

    /** Returns a <code>Temp</code> to represent where return values
     *  for procedures will be stored. */
    public abstract Temp RV();

    /** Returns a <code>Temp</code> to represent where the frame
     *  pointer will be stored. */
    public abstract Temp FP();

    /** Returns an array of <code>Temp</code>s which represent all
     *  the available registers on the machine. */
    public abstract Temp[] registers();
}
