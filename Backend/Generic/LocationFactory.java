// LocationFactory.java, created Tue Oct 12 19:06:17 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.ClassFile.HData;
import harpoon.IR.Tree.Exp;

/**
 * <code>LocationFactory</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LocationFactory.java,v 1.1.2.1 1999-10-13 07:38:13 cananian Exp $
 */
public interface LocationFactory {
    public interface Location { Exp accessor(); }
    Location allocateLocation();
    HData makeLocationData();
}
