// InstrDIRECTIVE.java, created Mon May 17 16:08:00 1999 by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Label;
import harpoon.Temp.TempMap;

/**
 * <code>InstrDIRECTIVE</code> is used to represents assembler
 * directives.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: InstrDIRECTIVE.java,v 1.1.2.4 1999-11-05 01:10:30 cananian Exp $
 */
public class InstrDIRECTIVE extends Instr {

    public InstrDIRECTIVE(InstrFactory inf, HCodeElement src, String a) {
        super(inf, src, a, null, null);
    } 

    public Instr rename(InstrFactory inf, TempMap defMap, TempMap useMap) {
	return new InstrDIRECTIVE(inf, this, getAssem());
    }

    public void accept(InstrVisitor v) { v.visit(this); }
}
