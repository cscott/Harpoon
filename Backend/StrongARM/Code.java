// Code.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.ArrayFactory;
import harpoon.Util.Set;
import harpoon.Util.HashSet;
import harpoon.IR.Tree.Tree;

import java.util.*;

/**
 * <code>StrongARM.Code</code> is a code-view for StrongARM
 * assembly-like syntax (currently without register allocation).
 * The plan is turn to Code into an abstract class with two
 * subclass codeviews - one without register allocation, one with.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Code.java,v 1.1.2.1 1999-02-08 00:54:30 andyb Exp $
 */

public class Code extends HCode {
    /** The name of this code view. */
    public static final String codename = "strongarm";
    /** The method that this code view represents. */
    HMethod parent;
    /** The StrongARM instructions composing this code view. */
    SAInsn insns;
    /** SAInsn factory. */
    final SAInsnFactory saif;

    /** Creates a <code>StrogARM.Code</code> object from a
     *  <code>Tree</code> object. */
    Code(Tree tree) {
        this.parent = tree.getFactory().getMethod();
        final String scope = parent.getDeclaringClass().getName() + "." +
            parent.getName() + parent.getDescriptor() + "/" + getName();
        this.saif = new SAInsnFactory() {
            private final TempFactory tf = Temp.tempFactory(scope);
            private int id=0;
            public TempFactory tempFactory() { return tf; }
            public Code getParent() { return Code.this; }
            synchronized int getUniqueID() { return id++; }
        };
        insns = CodeGen.codegen(tree, this);
    }

    /* XXX - not yet implemented */
    public static void register() {
    }

    public HMethod getMethod() { return parent; }

    public String getName() { return codename; }

    public HCodeElement[] getElements() {
        Vector v = new Vector();
        for (Enumeration e = getElementsE(); e.hasMoreElements(); )
            v.addElement(e.nextElement());
        HCodeElement[] elements = new SAInsn[v.size()];
        v.copyInto(elements);
        return elements;
    }

    /* XXX - update to use Collections API */
    public Enumeration getElementsE() {
        return new Enumeration() {
            private Set visited = new HashSet();
            private Stack s = new Stack();
            {
                if (insns != null) {
                    s.push(insns); visited.union(insns);
                }
            }
            public boolean hasMoreElements() { return !s.isEmpty(); }
            public Object nextElement() {
                if (s.empty()) throw new NoSuchElementException();
                SAInsn sai = (SAInsn) s.pop();
                SAInsn[] next = sai.next();
                for (int i = 0; i < next.length; i++) 
                    if (!visited.contains(next[i])) {
                        s.push(next[i]);
                        visited.union(next[i]);
                    }
                return sai;
            }
        };
    }
    
    public HCodeElement getRootElement() { return insns; }

    /** XXX not yet implemented */
    public HCodeElement[] getLeafElements() { return null; }

    public ArrayFactory elementArrayFactory() { return SAInsn.arrayFactory; }

    /*
    public void print(java.io.PrintWriter pw) {
        Print.print(pw, this);
    }
    */
}
