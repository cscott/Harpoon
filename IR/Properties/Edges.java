// Edges.java, created Sat Sep 12 18:01:09 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Properties;

import harpoon.ClassFile.HCodeEdge;
/**
 * <code>Edges</code> defines an interface for intermediate representations
 * that are interconnected as directed graphs.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Edges.java,v 1.3 1998-10-11 02:37:46 cananian Exp $
 */

public interface Edges  {
    /** Returns an array of all the edges to and from this 
     *  <code>HCodeElement</code>. */
    public HCodeEdge[] edges();
    public HCodeEdge[] pred();
    public HCodeEdge[] succ();
}
