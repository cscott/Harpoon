// Edge.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeEdge;
import harpoon.Util.ArrayFactory;

/** 
 * <code>Edge</code> implements the representation of flow links
 * between SAInsn's.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Edge.java,v 1.1.2.1 1999-02-08 00:54:30 andyb Exp $
 */
public class Edge implements HCodeEdge {
    SAInsn from, to;
    int from_index, to_index;

    Edge(SAInsn from, int from_index, SAInsn to, int to_index) {
        this.from = from;
        this.to = to;
        this.from_index = from_index;
        this.to_index = to_index;
    }

    public HCodeElement from() { return from; }
    public HCodeElement to() { return to; }
    public int which_pred() { return to_index; }
    public int which_succ() { return from_index; }

    public String toString() {
        return "Edge from (" + from + ") to (" + to + ")";
    }

    public static final ArrayFactory arrayFactory =
        new ArrayFactory() {
            public Object[] newArray(int len) { return new Edge[len]; }
        };
}
