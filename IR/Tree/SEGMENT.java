package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.Set;

/**
 *  The <code>SEGMENT</code> class is used to mark the beginning of a new
 *  section of memory.  All subsequent <code>Tree</code> objects will be 
 *  stored in the specified section.  
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: SEGMENT.java,v 1.1.2.1 1999-07-27 16:43:42 duncan Exp $
 */
public class SEGMENT extends Stm {
    /** Storage for global variables with an initial value */
    public static final int DATA_I  = 0; 
    /** Storage for read-only constant data (i.e. machine instructions) */
    public static final int TEXT    = 1; 
    /** Storage for global variables initialized to 0 */
    public static final int DATA_U  = 2; 

    /** The type of segment this <code>SEGMENT</code> precedes. */
    public final int segtype; 

    /** Class constructor. 
     *  <code>segtype</code> must be one the segments types specified
     *  in this class. */
    public SEGMENT(TreeFactory tf, HCodeElement source, int segtype) { 
	super(tf, source); 
	this.segtype = segtype;
	Util.assert(segtype>=0 && segtype<3);
    }

    protected Set defSet() { return new HashSet(); }
    protected Set useSet() { return new HashSet(); }
    
    public ExpList kids()  { return null; } 
    public int     kind()  { return TreeKind.SEGMENT; }

    public Stm build(ExpList kids) { return this; } 

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new SEGMENT(tf, this, segtype);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("SEGMENT<");
	sb.append(segtype==0 ? "INITIALIZED_DATA"   : 
		  segtype==1 ? "TEXT"               :
		  segtype==2 ? "UNINITIALIZED_DATA" : 
		  "UNKNOWN SEGMENT TYPE"); 
	sb.append(">");
	return sb.toString();
    }
}

