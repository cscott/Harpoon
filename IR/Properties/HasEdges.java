// HasEdges.java, created Sat Sep 12 18:01:09 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Properties;

import harpoon.ClassFile.HCodeEdge;
/**
 * <code>HasEdges</code> defines an interface for intermediate representations
 * that are interconnected as directed graphs.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HasEdges.java,v 1.1.2.1 1999-05-19 06:45:16 andyb Exp $
 */

public interface HasEdges  {
    
    /** Returns an array of all the edges to and from this 
     *  <code>HasEdges</code>. */
    public HCodeEdge[] edges();
    public HCodeEdge[] pred();
    public HCodeEdge[] succ();
}
