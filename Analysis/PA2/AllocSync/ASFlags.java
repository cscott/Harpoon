// ASFlags.java, created Wed Jul 27 09:23:19 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2.AllocSync;

/**
 * <code>ASFlags</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: ASFlags.java,v 1.1 2005-08-10 03:03:16 salcianu Exp $
 */
public abstract class ASFlags {

    public static int MAX_SA_INLINE_LEVEL = 3;

    public static boolean VERBOSE = false;

    public static boolean VERY_VERBOSE = false;

    public static int SA_MIN_LINE = -1;

    public static int SA_MAX_LINE = -1;

    public static int MAX_METHOD_SIZE = 1000;

    public static int MAX_INLINABLE_METHOD_SIZE = 100;

}
