// CodeGen.spec, created Mon Jun 28 23:00:48 1999 by cananian -*- Java -*-
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Sparc;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.Uop;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.Typed;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <code>Sparc.CodeGen</code> is a code-generator for the Sparc architecture.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: CodeGen.spec,v 1.1.2.8 1999-11-02 07:41:41 andyb Exp $
 */
%%

    Map origTempToNewTemp;
    Instr first, last;
    InstrFactory instrFactory;

    public CodeGen(Frame frame) {
        super(frame); 
    }

    public Instr procFixup(HMethod hm, Instr instr, int stackspace,
                           Set usedRegisters) {
        return null;
    }

    private Temp makeTemp( Temp orig ) {
        return null;
    }

%%

%start with %{
       // initialize state variables each time gen() is called
       first = null; last = null;
       origTempToNewTemp = new HashMap();
       this.instrFactory = inf;

}%
%end with %{
       // *** METHOD EPILOGUE *** 
       Util.assert(first != null, "Should always generate some instrs");
       return first;
}%

TEMP<p,i,f>(id) = i %{
   i = makeTemp( ROOT.temp );
}%
