// InstrList.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

/**
 * <code>InstrList</code> is the data representation for a
 * list of Instrs.  From the Appel book.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: InstrList.java,v 1.1.2.2 1999-02-26 17:58:50 andyb Exp $
 */
public class InstrList {
    public Instr head; 
    public InstrList tail;

    public InstrList(Instr head, InstrList tail) {
        this.head = head;
        this.tail = tail;
    }
}
