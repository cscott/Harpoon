// QuadKind.java, created Fri Dec 11 06:48:53 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

/**
 * <code>QuadKind</code> is an enumerated type for the various kinds of
 * <code>Quad</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadKind.java,v 1.2 2002-02-25 21:05:12 cananian Exp $
 */
public abstract class QuadKind  {
    private static int n = min();

    public final static int AGET = n++;
    public final static int ALENGTH = n++;
    public final static int ANEW = n++;
    public final static int ARRAYINIT = n++;
    public final static int ASET = n++;
    public final static int CALL = n++;
    public final static int CJMP = n++;
    public final static int COMPONENTOF = n++;
    public final static int CONST = n++;
    public final static int DEBUG = n++;
    public final static int FOOTER = n++;
    public final static int GET = n++;
    public final static int HEADER = n++;
    public final static int INSTANCEOF = n++;
    public final static int LABEL = n++;
    public final static int HANDLER = n++;
    public final static int METHOD = n++;
    public final static int MONITORENTER = n++;
    public final static int MONITOREXIT = n++;
    public final static int MOVE = n++;
    public final static int NEW = n++;
    public final static int NOP = n++;
    public final static int OPER = n++;
    public final static int PHI = n++;
    public final static int RETURN = n++;
    public final static int SET = n++;
    public final static int SIGMA = n++;
    public final static int SWITCH = n++;
    public final static int THROW = n++;
    public final static int TYPECAST = n++;
    public final static int TYPESWITCH = n++;
    public final static int XI = n++;

    public static int min() { return 0; }
    public static int max() { return n; }

    public static boolean isValid(int k) {
	return (min()<=k) && (k<max());
    }
}




