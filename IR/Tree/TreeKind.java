package harpoon.IR.Tree;

/**
 * <code>TreeKind</code> is an enumerated type for the various kinds of
 * <code>Tree</code>s.  Largely copied from Scott's <code>QuadKind</code>
 * class. 
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * $Id: TreeKind.java,v 1.1.2.2 1999-07-07 09:47:24 duncan Exp $
 */
public abstract class TreeKind  {
    public final static int BINOP      = 0;
    public final static int CALL       = 1;
    public final static int CJUMP      = 2;
    public final static int CONST      = 3;
    public final static int ESEQ       = 4;
    public final static int EXP        = 5;
    public final static int JUMP       = 6;
    public final static int LABEL      = 7;
    public final static int MEM        = 8;
    public final static int MOVE       = 9;
    public final static int NAME       = 0;
    public final static int NATIVECALL = 11;
    public final static int RETURN     = 12;
    public final static int SEQ        = 13;
    public final static int TEMP       = 14;
    public final static int THROW      = 15;
    public final static int UNOP       = 16;

    public static int min() { return 0; }
    public static int max() { return 17; }

    public static boolean isValid(int k) {
	return (min()<=k) && (k<max());
    }
}




