// TreeKind.java, created Mon Jun 28 14:46:11 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

/**
 * <code>TreeKind</code> is an enumerated type for the various kinds of
 * <code>Tree</code>s.  Largely copied from Scott's <code>QuadKind</code>
 * class. 
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: TreeKind.java,v 1.3 2003-04-11 00:30:08 cananian Exp $
 */
public abstract class TreeKind  {
    public final static int ALIGN      = 0;
    public final static int BINOP      = 1;
    public final static int CALL       = 2;
    public final static int CJUMP      = 3;
    public final static int CONST      = 4;
    public final static int DATUM      = 5;
    public final static int ESEQ       = 6;
    public final static int EXPR       = 7;
    public final static int JUMP       = 8;
    public final static int LABEL      = 9;
    public final static int MEM        = 10;
    public final static int METHOD     = 11;
    public final static int MOVE       = 12;
    public final static int NAME       = 13;
    public final static int NATIVECALL = 14;
    public final static int RETURN     = 15;
    public final static int SEGMENT    = 16;
    public final static int SEQ        = 17;
    public final static int TEMP       = 18;
    public final static int THROW      = 19;
    public final static int UNOP       = 20;

    public static int min() { return 0; }
    public static int max() { return 21; }

    public static boolean isValid(int k) {
	return (min()<=k) && (k<max());
    }
}




