// TempBuilder.java, created Thu Oct 21 17:25:43 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.IR.Tree.Typed;
import harpoon.Util.Util;

/** <code>TempBuilder</code> defines an interface that general program
    transformations can call to generate data structures to represent
    <code>Temp</code>s.
    <p>
    This allows Backend writers to make their own extensions of
    <code>Temp</code> to make handling different value types easier. 
  
    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: TempBuilder.java,v 1.2 2002-02-25 21:01:28 cananian Exp $
 */
public abstract class TempBuilder {
    
    /** Creates a <code>TempBuilder</code>. */
    public TempBuilder() {
        
    }

    /** Makes a <code>Temp</code> for storing values with the same
	type as <code>t</code>.
    */
    public abstract Temp makeTemp(Typed t, TempFactory tf);

    
}
