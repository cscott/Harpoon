// SACode.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Backend.Generic.Code;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Tree.TreeCode;

import harpoon.Util.Util;

import java.util.List;
import java.util.Arrays;

/**
 * <code>SACode</code> is a code-view for StrongARM
 * assembly-like syntax (currently without register allocation).
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: SACode.java,v 1.1.2.4 1999-05-25 16:50:36 andyb Exp $
 */
public class SACode extends Code {
    /** The name of this code view. */
    public static final String codename = "strongarm";

    SACode(TreeCode tree) {
        super(tree.getMethod(), null, tree.getFrame());
        instrs = CodeGen.codegen(tree, this);
        Util.assert(instrs != null, "Code generation failed for SACode");
        instrs = frame.procLiveOnExit(instrs);
        instrs = frame.procAssemDirectives(instrs);
    }

    private SACode(HMethod parent, Instr instrs, Frame frame) {
        super(parent, instrs, frame);
    }

    /** Returns the name of this code view.
     *
     *  @return         The String naming this codeview, "strongarm".
     */
    public String getName() { return codename; }

    public HCode clone(HMethod newMethod) throws CloneNotSupportedException {
        throw new CloneNotSupportedException(this.toString());
    }

    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
        if (hcf.getCodeName().equals(TreeCode.codename)) {
            return new HCodeFactory() {
                public HCode convert(HMethod m) {
                    HCode c = hcf.convert(m);
                    return (c==null) ? null :
                        new SACode((TreeCode)c);
                }
                public void clear(HMethod m) { hcf.clear(m); }
                public String getCodeName() { return codename; }
            };
        } else {
            throw new Error("Cannot make " + codename + " from " +
                            hcf.getCodeName());
        }
    }

    public static HCodeFactory codeFactory() {
        return codeFactory(TreeCode.codeFactory(new SAFrame()));
    }
            
    /** Registers this codeview so that it will be recognized later. 
     */
    public static void register() { 
        HMethod.register(codeFactory());
    }

}
