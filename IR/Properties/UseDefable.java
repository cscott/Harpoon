// UseDef.java, created Sat Sep 12 17:55:44 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Properties;

import harpoon.Temp.Temp;

import java.util.Collection;
/**
 * <code>UseDefable</code> defines an interface for intermediate
 * representations that keep use/def information.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: UseDefable.java,v 1.2 2002-02-25 21:04:45 cananian Exp $
 */

public interface UseDefable extends harpoon.ClassFile.HCodeElement {
    /** Return all the <code>Temp</code>s used by this 
     *  <code>HCodeElement</code>. */
    public Temp[] use();
    /** Return all the <code>Temp</code>s defined by this 
     *  <code>HCodeElement</code>. */
    public Temp[] def();

    // JDK 1.2 collections API: [FSK, 3-Sep-1999]
    /** Returns a <code>Collection</code> of all the
	<code>Temp</code>s read in this <code>HCodeElement</code>.  
    */
    public Collection useC();

    /** Returns a <code>Collection</code> of all the
	<code>Temp</code>s defined in this <code>HCodeElement</code>.   
    */
    public Collection defC();
}
