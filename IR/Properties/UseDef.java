// UseDef.java, created Sat Sep 12 17:55:44 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Properties;

import harpoon.Temp.Temp;
/**
 * <code>UseDef</code> defines an interface for intermediate representations
 * that keep use/def information.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: UseDef.java,v 1.2 1998-10-11 02:37:47 cananian Exp $
 */

public interface UseDef  {
    /** Return all the <code>Temp</code>s used by this 
     *  <code>HCodeElement</code>. */
    public Temp[] use();
    /** Return all the <code>Temp</code>s defined by this 
     *  <code>HCodeElement</code>. */
    public Temp[] def();
}
