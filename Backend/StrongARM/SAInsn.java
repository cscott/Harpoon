// SAInsn.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeEdge;
import harpoon.IR.Properties.Edges;
import harpoon.Temp.Temp;
import harpoon.Util.ArrayFactory;
import harpoon.Util.Util;

/** 
 * <code>SAInsn</code> is the base class for the representation
 * of StrongARM opcodes.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: SAInsn.java,v 1.1.2.1 1999-02-08 00:54:30 andyb Exp $
 */
public abstract class SAInsn 
    implements HCodeElement, Edges, Cloneable
{
    SAInsnFactory saif;
    String source_file;
    int line_number;
    int id;
   
    protected SAInsn(SAInsnFactory saif, HCodeElement source,
                     int prev_arity, int next_arity) {
        this.source_file = (source!=null)?source.getSourceFile():"unknown";
        this.line_number = (source!=null)?source.getLineNumber():0;
        this.id = saif.getUniqueID();
        this.saif = saif;

        this.prev = new Edge[prev_arity];
        this.next = new Edge[next_arity];
    }

    protected SAInsn(SAInsnFactory saif, HCodeElement source) {
        this(saif, source, 1, 1);
    }

    /*--------------------------------------------------------*/
    
    public SAInsnFactory getFactory() { return saif; }

    public String getSourceFile() { return source_file; }

    public int getLineNumber() { return line_number; }

    public int getID() { return id; }

    /** XXX - not yet implmented */
    public final Object clone() { return null; }

    /*--------------------------------------------------------*/
    public abstract String toString();

    public abstract void visit(SAInsnVisitor v);

    public abstract int kind();

    /*--------------------------------------------------------*/
    Edge next[], prev[];

    public SAInsn next(int i) { return (SAInsn) next[i].to(); }

    public SAInsn prev(int i) { return (SAInsn) prev[i].from(); }

    public SAInsn[] next() { 
        SAInsn[] retval = new SAInsn[next.length];
        for (int i=0; i < retval.length; i++)
            retval[i] = (next[i]==null)?null:(SAInsn)next[i].to();
        return retval;
    }

    public SAInsn[] prev() { 
        SAInsn[] retval = new SAInsn[prev.length];
        for (int i=0; i < retval.length; i++)
            retval[i] = (prev[i]==null)?null:(SAInsn)prev[i].from();
        return retval;
    }

    public Edge nextEdge(int i) { return next[i]; }

    public Edge prevEdge(int i) { return prev[i]; }

    public Edge[] nextEdge() { 
        return (Edge[]) Util.safeCopy(Edge.arrayFactory, next); }

    public Edge[] prevEdge() { 
        return (Edge[]) Util.safeCopy(Edge.arrayFactory, prev); }
   
    public HCodeEdge[] edges() { 
        Edge[] e = new Edge[next.length + prev.length];
        System.arraycopy(next,0,e,0,next.length);
        System.arraycopy(prev,0,e,next.length,prev.length);
        return (HCodeEdge[]) e;
    }

    public HCodeEdge[] pred() { return prevEdge(); }

    public HCodeEdge[] succ() { return nextEdge(); }
    
    public static Edge addEdge(SAInsn from, int from_index,
                               SAInsn to, int to_index) {
        Edge e = new Edge(from, from_index, to, to_index);
        from.next[from_index] = e;
        to.prev[to_index] = e;
        return e;
    }

    /** Array factory to generate an array of <code>SAInsn</code>s */
    public static final ArrayFactory arrayFactory = 
        new ArrayFactory() {
            public Object[] newArray(int len) { return new SAInsn[len]; }
        };
}
