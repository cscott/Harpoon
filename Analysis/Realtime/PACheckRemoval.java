// PACheckRemoval.java, created Mon Jan 22 19:51:51 2001 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import harpoon.IR.Quads.Quad;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HCodeFactory;
import harpoon.Analysis.ClassHierarchy;

import java.util.Set;

/**
 * <code>PACheckRemoval</code> is a complex, pointer analysis based
 * implementation of the <codE>CheckRemoval</code> interface.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PACheckRemoval.java,v 1.1.2.2 2001-01-23 21:09:50 wbeebee Exp $ */
public class PACheckRemoval implements CheckRemoval {
    
    /** Creates a <code>PACheckRemoval</code>. */
    public PACheckRemoval(Linker linker, ClassHierarchy ch, HCodeFactory hc,
			  Set roots) {
        
    }

    /** TODO */
    public boolean shouldRemoveCheck(Quad inst) { 
	return false;
    }

}
