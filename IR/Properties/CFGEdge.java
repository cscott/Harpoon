// CFGEdge.java, created Tue Jan  4 22:52:37 2000 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Properties;

import harpoon.ClassFile.HCodeElement;

/**
 * An <code>CFGEdge</code> connects two <code>CFGraphable</code>s
 * in a graph structure.
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: CFGEdge.java,v 1.3 2002-04-10 03:05:05 cananian Exp $
 * @see CFGraphable
 */
public abstract class CFGEdge<CFG extends CFGraphable>
    implements harpoon.ClassFile.HCodeEdge<CFG> {
    /** Returns the source of this <code>CFGEdge</code>. 
     *  The return value is guaranteed to implement to 
     *  <code>CFGraphable</code> interface. */
    public final CFG from() { return this.fromCFG(); }
    /** Returns the source of this <code>CFGEdge</code>. */
    public abstract CFG fromCFG(); 

    /** Returns the destination of this <code>HCodeEdge</code>.
     *  The return value is guaranteed to implement to  
     *  <code>CFGraphable</code> interface. */ 
    public final CFG to() { return this.toCFG(); } 
    /** Returns the destination of this <code>HCodeEdge</code>. */
    public abstract CFG toCFG(); 
}
