// InstrList.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

/**
 * <code>InstrList</code> is the data representation for a
 * list of Instrs.  From the Appel book.
 *
 * @deprecated Felix is deprecating this, since `find . -iname "*.java" | xargs grep InstrList -n` told him that its not actually used anywhere else in the compiler.  Use an <code>Instr</code>[] instead.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: InstrList.java,v 1.1.2.3 1999-04-09 12:34:44 pnkfelix Exp $
 */
public class InstrList {
    public Instr head; 
    public InstrList tail;

    public InstrList(Instr head, InstrList tail) {
        this.head = head;
        this.tail = tail;
    }
}
