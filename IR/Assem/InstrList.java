// InstrList.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

/**
 * <code>InstrList</code> is the data representation for a
 * list of Instrs.  From the Appel book.
 *
 * @deprecated Felix is deprecating this, since `find . -iname "*.java" | xargs grep InstrList -n` told him that its not actually used anywhere else in the compiler.  Use a <code>harpoon.Backend.Generic.Code</code> or a <code>java.util.List</code> of <code>Instr</code>s instead.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: InstrList.java,v 1.1.2.5 1999-04-20 19:06:41 pnkfelix Exp $
 */
public class InstrList {
    public Instr head; 
    public InstrList tail;

    public InstrList(Instr head, InstrList tail) {
        this.head = head;
        this.tail = tail;
    }
}
