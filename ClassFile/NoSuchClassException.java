// NoSuchClassException.java, created Mon Jan 10 22:11:57 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * <code>NoSuchClassException</code> is thrown to indicate an
 * attempt to remove a class from a list which does not contain it,
 * or a failed attempt to look up a class by name or descriptor.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NoSuchClassException.java,v 1.2 2002-02-25 21:03:04 cananian Exp $
 */
public class NoSuchClassException extends RuntimeException {
    /** Creates a <code>NoSuchClassException</code> with the
     *  supplied detail message. */
    public NoSuchClassException(String message) { super(message); }
}
