// Options.java, created Tue Jul 20 17:31:26 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

/**
 * <code>Options</code> contains the values of the current runtime
 * environment.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Options.java,v 1.1.2.1 1999-07-23 06:50:55 cananian Exp $
 */
public class Options {
    /** Stream for writing statistics. */
    public static java.io.PrintWriter statWriter = null;
    /** Stream for writing profiling data. */
    public static java.io.PrintWriter profWriter = null;
}
