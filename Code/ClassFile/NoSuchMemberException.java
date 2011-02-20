// NoSuchMemberException.java, created Mon Jan 10 22:11:57 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * <code>NoSuchMemberException</code> is thrown to indicate an
 * attempt to remove a member from a class which does not contain it.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NoSuchMemberException.java,v 1.2 2002-02-25 21:03:04 cananian Exp $
 */
public class NoSuchMemberException extends RuntimeException {
    /** Creates a <code>NoSuchMemberException</code> with the
     *  supplied detail message. */
    public NoSuchMemberException(String message) { super(message); }
}
