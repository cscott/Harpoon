// Code.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.ArrayFactory;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrFactory;

import java.util.*;

/**
 * <code>StrongARM.Code</code> is a code-view for StrongARM
 * assembly-like syntax (currently without register allocation).
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Code.java,v 1.1.2.2 1999-02-09 05:45:32 andyb Exp $
 */

public class Code extends HCode {
    /** The name of this code view. */
    public static final String codename = "strongarm";
    /** The method that this code view represents. */
    HMethod parent;
    /** The StrongARM instructions composing this code view. */
    Instr instrs;
    /** Instruction factory. */
    final InstrFactory inf;

    Code(final HMethod parent, final Instr instrs) {
        this.parent = parent;
        this.instrs = instrs;
        final String scope = parent.getDeclaringClass().getName() + "." +
            parent.getName() + parent.getDescriptor() + "/" + getName();
        this.inf = new InstrFactory() {
            private final TempFactory tf = Temp.tempFactory(scope);
            private int id=0;
            public TempFactory tempFactory() { return tf; }
            public HCode getParent() { return Code.this; }
            synchronized int getUniqueID() { return id++; }
        };
        /*instrs = CodeGen.codegen(tree, this); */
    }

    /* XXX - not yet implemented */
    public static void register() {
    }

    public HMethod getMethod() { return parent; }

    public String getName() { return codename; }

    /* XXX - re-implement? */
    public HCodeElement[] getElements() {
        return null;
    }

    /* XXX - re-implement? */
    public Enumeration getElementsE() {
        return null;
    }
    
    public HCodeElement getRootElement() { return instrs; }

    public HCodeElement[] getLeafElements() { return null; }

    public ArrayFactory elementArrayFactory() { return Instr.arrayFactory; }

    /*
    public void print(java.io.PrintWriter pw) {
        Print.print(pw, this);
    }
    */
}
