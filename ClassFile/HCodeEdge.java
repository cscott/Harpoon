// HCodeEdge.java, created Sat Sep 12 17:17:55 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * An <code>HCodeEdge</code> connects two <code>HCodeElement</code>s
 * in a graph structure.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HCodeEdge.java,v 1.2 1998-10-11 02:37:08 cananian Exp $
 * @see HCodeElement
 */
public interface HCodeEdge  {
    /** Returns the source of this <code>HCodeEdge</code>. */
    public HCodeElement from();
    /** Returns the destination of the <code>HCodeEdge</code>. */
    public HCodeElement to();
}
