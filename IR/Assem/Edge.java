// Edge.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.Util.Util;

/**
 * <code>Edge</code> is a class for representing directed edges between
 * <code>Instr</code>s.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Edge.java,v 1.1.2.1 1999-05-25 16:45:13 andyb Exp $
 */
public class Edge implements HCodeEdge {
    private Instr from, to;

    /** Creates an <code>Edge</code>. */
    Edge(Instr from, Instr to) {
        Util.assert(from != null);
        Util.assert(to != null);
        this.from = from;
        this.to = to;
    }

    /** Returns the source <code>Instr</code> of this <code>Edge</code>. */
    public HCodeElement from() { return from; }

    /** Returns the destination <code>Instr</code> of this <code>Edge</code>. */
    public HCodeElement to() { return to; }

    // XXX to implement: equals, hashCode, toString
}
