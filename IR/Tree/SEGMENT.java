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
 * @version $Id: SEGMENT.java,v 1.1.2.3 1999-08-03 21:12:58 duncan Exp $
 */
public class SEGMENT extends Stm {
    /** Storage for static class data (display, vmtable, etc) */
    public static final int CLASS             = 0; 
    /** Read-only instruction memory */
    public static final int CODE              = 1;
    /** Storage for GC tables */
    public static final int GC                = 2;
    /** r/w memory that must be initialized before use */
    public static final int INIT_DATA         = 3; 
    /** storage for static aggregate data */
    public static final int STATIC_OBJECTS    = 4;
    /** storage for static primitive data */
    public static final int STATIC_PRIMITIVES = 5;
    /** read-only memory (other than machine instructions) */
    public static final int TEXT              = 6; 
    /** r/w memory initialized at load time to be 0 */
    public static final int ZERO_DATA         = 7; 

    /** The type of segment this <code>SEGMENT</code> precedes. */
    public final int segtype; 

    /** Class constructor. 
     *  <code>segtype</code> must be one the segments types specified
     *  in this class. */
    public SEGMENT(TreeFactory tf, HCodeElement source, int segtype) { 
	super(tf, source); 
	this.segtype = segtype;
	Util.assert(segtype>=0 && segtype<8);
    }

    protected Set defSet() { return new HashSet(); }
    protected Set useSet() { return new HashSet(); }
    
    public ExpList kids()  { return null; } 
    public int     kind()  { return TreeKind.SEGMENT; }

    public Stm build(ExpList kids) { return build(tf, kids); } 
    public Stm build(TreeFactory tf, ExpList kids) { 
	return new SEGMENT(tf, this, segtype); 
    } 

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new SEGMENT(tf, this, segtype);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("SEGMENT<");
	sb.append(segtype==0 ? "CLASS"             :
		  segtype==1 ? "CODE"              :
		  segtype==2 ? "GC"                :
		  segtype==3 ? "INIT_DATA"         :
		  segtype==4 ? "STATIC_OBJECTS"    :
		  segtype==5 ? "STATIC_PRIMITIVES" :
		  segtype==6 ? "TEXT"              :
		  segtype==7 ? "ZERO_DATA"         :
		  "UNKNOWN SEGMENT TYPE"); 
	sb.append(">");
	return sb.toString();
    }
}

