// DuplicateMemberException.java, created Mon Jan 10 22:11:57 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * <code>DuplicateMemberException</code> is thrown to indicate an
 * attempt to add a member to a class already containing a member
 * of the same name.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DuplicateMemberException.java,v 1.2 2002-02-25 21:03:01 cananian Exp $
 */
public class DuplicateMemberException extends RuntimeException {
    /** Creates a <code>DuplicateMemberException</code> with the
     *  supplied detail message. */
    public DuplicateMemberException(String message) { super(message); }
}
