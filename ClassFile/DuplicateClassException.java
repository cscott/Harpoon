// DuplicateClassException.java, created Mon Jan 10 22:11:57 2000 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * <code>DuplicateClassException</code> is thrown to indicate an
 * attempt to create a class whose name conflicts with another
 * class already known to the linker.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DuplicateClassException.java,v 1.1.2.1 2000-01-11 12:35:04 cananian Exp $
 */
public class DuplicateClassException extends RuntimeException {
    /** Creates a <code>DuplicateClassException</code> with the
     *  supplied detail message. */
    public DuplicateClassException(String message) { super(message); }
}
