// MaxMunchCG.java, created Fri Feb 11 01:26:47 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

/**
 * <code>MaxMunchCG</code> is a <code>MaximalMunchCGG</code> specific 
 * extension of <code>CodeGen</code>.  Its purpose is to incorporate
 * functionality common to all target architectures but specific to
 * the particular code generation strategy employed by the CGG.  Other
 * <code>CodeGeneratorGenerator</code> implementations should add
 * their own extensions of <code>CodeGen</code>.
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: MaxMunchCG.java,v 1.1.2.1 2000-02-11 06:35:02 pnkfelix Exp $ */
public abstract class MaxMunchCG extends CodeGen {
    
    /** Creates a <code>MaxMunchCG</code>. */
    public MaxMunchCG(Frame frame) {
        super(frame);
    }
    
}
