// InstrBuilder.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Sparc;

import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.Temp.Temp;

import java.util.List;

/**
 * <code>Sparc.InstrBuilder</code> is another implementation of
 * <code>Generic.InstrBuilder</code> - for the Sparc architecture.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: InstrBuilder.java,v 1.1.2.1 1999-11-02 07:07:04 andyb Exp $
 */
public class InstrBuilder extends harpoon.Backend.Generic.InstrBuilder {
    RegFileInfo regFileInfo;
    TempBuilder tempBuilder;

    InstrBuilder(RegFileInfo regFileInfo, TempBuilder tempBuilder) {
        super();
        this.regFileInfo = regFileInfo;
        this.tempBuilder = tempBuilder;
    }

    public List makeLoad(Temp r, int offset, Instr template) {
        return null;
    }

    public List makeStore(Temp r, int offset, Instr template) {
        return null;
    }    

    public InstrLABEL makeLabel(Instr template) {
        return null;
    }
}
