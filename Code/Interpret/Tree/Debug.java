// Debug.java, created Tue Jan 11 19:04:03 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

/**
 * <code>Debug</code> contains the debug flags for the Tree
 * interpreter.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Debug.java,v 1.2 2002-02-25 21:05:50 cananian Exp $
 */
abstract class Debug {
    static void db(String str) { System.out.println(str); } 
    static boolean DEBUG = false;
}
