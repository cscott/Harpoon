// InstrCALL.java, created Wed Jan 26 10:56:44 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import java.util.List;

/**
 * <code>InstrCALL</code>
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: InstrCALL.java,v 1.1.2.1 2000-01-26 21:03:22 pnkfelix Exp $
 */
public class InstrCALL extends Instr {
    
    /** Creates a <code>InstrCALL</code>. */
    public InstrCALL( InstrFactory inf, HCodeElement source,
		      String assem, Temp[] dst, Temp[] src,
		      boolean canFallThrough, List targets) {
        super(inf, source, assem, dst, src, canFallThrough, targets);
    }
    
}
