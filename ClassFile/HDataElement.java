// HDataElement.java, created Wed Sep  8 15:35:12 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * <code>HDataElement</code> is an interface that all views of
 * the data items in an <code>HData</code> must implement.
 * Items in an <code>HData</code> must be tracable to a particular source
 * file, and possess an unique numeric identifier.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HDataElement.java,v 1.2 2002-02-25 21:03:03 cananian Exp $
 */
public interface HDataElement {
    /** Get the original source file name that this element is derived from. */
    public String getSourceFile();
    /** Returns a unique numeric identifier for this element. */
    public int getID();
}
