package harpoon.IR.Tree;

/**
 * <code>TreeKind</code> is an enumerated type for the various kinds of
 * <code>Tree</code>s.  Largely copied from Scott's <code>QuadKind</code>
 * class. 
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * $Id: TreeKind.java,v 1.1.2.1 1999-06-28 18:46:11 duncan Exp $
 */
public abstract class TreeKind  {
    private static int n = min();

    public final static int BINOP      = n++;
    public final static int CALL       = n++;
    public final static int CJUMP      = n++;
    public final static int CONST      = n++;
    public final static int ESEQ       = n++;
    public final static int EXP        = n++;
    public final static int JUMP       = n++;
    public final static int LABEL      = n++;
    public final static int MEM        = n++;
    public final static int MOVE       = n++;
    public final static int NAME       = n++;
    public final static int NATIVECALL = n++;
    public final static int RETURN     = n++;
    public final static int SEQ        = n++;
    public final static int TEMP       = n++;
    public final static int THROW      = n++;
    public final static int UNOP       = n++;

    public static int min() { return 0; }
    public static int max() { return n; }

    public static boolean isValid(int k) {
	return (min()<=k) && (k<max());
    }
}




