// AllCallers.java, created Wed Aug 10 10:02:54 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2.AllocSync;

import java.util.Collection;

import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;

/**
 * <code>AllCallers</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: AllCallers.java,v 1.1 2005-08-16 22:41:57 salcianu Exp $
 */
interface AllCallers {

    Collection<HMethod> getCallers(HMethod callee);
    
    Collection<CALL> getCALLs(HMethod caller, HMethod callee);

    boolean monoCALL(CALL cs);

}
