// ClassDataException.java, created Mon Jan 18 22:44:36 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/** 
 * Specialized IOException class to handle malformed class files.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClassDataException.java,v 1.2 2002-02-25 21:05:26 cananian Exp $
 */
public class ClassDataException extends java.io.IOException {
  public ClassDataException() { super(); }
  public ClassDataException(String s) { super(s); }
}
