// Edges.java, created Sat Sep 12 18:01:09 1998 by cananian
package harpoon.IR.Properties;

import harpoon.ClassFile.HCodeEdge;
/**
 * <code>Edges</code> defines an interface for intermediate representations
 * that are interconnected as directed graphs.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Edges.java,v 1.2 1998-09-15 01:25:41 cananian Exp $
 */

public interface Edges  {
    /** Returns an array of all the edges to and from this 
     *  <code>HCodeElement</code>. */
    public HCodeEdge[] edges();
    public HCodeEdge[] pred();
    public HCodeEdge[] succ();
}
