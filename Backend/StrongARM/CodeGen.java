// CodeGen.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.IR.Assem.Instr;
import harpoon.IR.Tree.Code;

/**
 * <code>CodeGen</code> is a utility class which implements instruction
 * selection of <code>SAInsn</code>s from an input <code>Tree</code>.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: CodeGen.java,v 1.1.2.3 1999-02-16 21:14:39 andyb Exp $
 */
final class CodeGen {

    /** Generates StrongARM assembly from the internal Tree representation.
     *  <BR> XXX - NOT YET IMPLEMENTED.
     *
     *  @param  tree    The Tree codeview to generate code from.
     *  @param  code    The StrongARM codeview to generate code for.
     *  @return         The newly generated StrongARM instructions.
     */
    public static final Instr codegen(harpoon.IR.Tree.Code tree,
                                      harpoon.Backend.StrongARM.Code code) {
        return null;
    }
}
