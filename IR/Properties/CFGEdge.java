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
 * @version $Id: CFGEdge.java,v 1.5 2003-05-09 16:35:30 cananian Exp $
 * @see CFGraphable
 */
public abstract class CFGEdge<CFG extends CFGraphable<CFG,E>,
			      E extends CFGEdge<CFG,E>>
    implements harpoon.ClassFile.HCodeEdge<CFG> {
    /** Returns the source of this <code>CFGEdge</code>. 
     *  The return value is guaranteed to implement to 
     *  <code>CFGraphable</code> interface. */
    public abstract CFG from();

    /** Returns the destination of this <code>HCodeEdge</code>.
     *  The return value is guaranteed to implement to  
     *  <code>CFGraphable</code> interface. */ 
    public abstract CFG to();
}
