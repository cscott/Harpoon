// HCodeEdge.java, created Sat Sep 12 17:17:55 1998 by cananian
package harpoon.ClassFile;

/**
 * An <code>HCodeEdge</code> connects two <code>HCodeElement</code>s
 * in a graph structure.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HCodeEdge.java,v 1.1 1998-09-13 23:57:15 cananian Exp $
 * @see HCodeElement
 */
public interface HCodeEdge  {
    /** Returns the source of this <code>HCodeEdge</code>. */
    public HCodeElement from();
    /** Returns the destination of the <code>HCodeEdge</code>. */
    public HCodeElement to();
}
