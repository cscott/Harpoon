// UseDefer.java, created Thu Jan 27 15:33:54 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Properties;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import java.util.Collection;

/**
 * <code>UseDefer</code>
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: UseDefer.java,v 1.1.2.1 2000-01-27 20:43:08 pnkfelix Exp $
 */
public abstract class UseDefer {
    
    public Temp[] use(HCodeElement hce) {
	Collection c = useC(hce);
	return (Temp[]) c.toArray(new Temp[c.size()]);
    }
    public Temp[] def(HCodeElement hce) {
	Collection c = defC(hce);
	return (Temp[]) c.toArray(new Temp[c.size()]);
    }

    public abstract Collection useC(HCodeElement hce);
    public abstract Collection defC(HCodeElement hce);
    


}
