// CodeGen.java, created Wed Jul 28 18:19:29 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.IR.Tree.Code;

/**
 * <code>CodeGen</code> is a general class for specific Backends to
 * extend.  Typically a Specfile for a specific backend will be
 * designed as an extension of this class.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: GenericCodeGen.java,v 1.1.2.1 1999-07-29 00:39:56 pnkfelix Exp $ */
public abstract class GenericCodeGen {
    
    /** Creates a <code>CodeGen</code>. */
    public GenericCodeGen() {
        
    }
    
    public abstract harpoon.Backend.Generic.Code gen(harpoon.IR.Tree.Code tree);
}
