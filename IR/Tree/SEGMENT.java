// SEGMENT.java, created Tue Jul 27 12:43:42 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
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
 * @version $Id: SEGMENT.java,v 1.1.2.8 1999-08-11 13:26:09 cananian Exp $
 */
public class SEGMENT extends Stm {
    /** R/O storage for static class data (display, vmtable, etc) */
    public static final int CLASS                = 0; 
    /** Read-only instruction memory */
    public static final int CODE                 = 1;
    /** R/O storage for GC tables */
    public static final int GC                   = 2;
    /** R/W memory that must be initialized before use */
    public static final int INIT_DATA            = 3; 
    /** R/W storage for static aggregate data */
    public static final int STATIC_OBJECTS       = 4;
    /** R/W storage for static primitive data */
    public static final int STATIC_PRIMITIVES    = 5;
    /** R/O storage for string constant objects */
    public static final int STRING_CONSTANTS     = 6;
    /** R/O storage for component character arrays of statically allocated 
     *  string constant objects. */
    public static final int STRING_DATA  = 7;
    /** Read-only memory (other than machine instructions) */
    public static final int TEXT                 = 8; 
    /** R/W memory initialized at load time to be 0 */
    public static final int ZERO_DATA            = 9; 

    /** Converts a segtype into its string representation.
     */
    public static final String decode(int segtype) {
	switch(segtype) {
	case CLASS: return "CLASS";
	case CODE: return "CODE";
	case GC: return "GC";
	case INIT_DATA: return "INIT_DATA";
	case STATIC_OBJECTS: return "STATIC_OBJECTS";
	case STATIC_PRIMITIVES: return "STATIC_PRIMITIVES";
	case STRING_CONSTANTS: return "STRING_CONSTANTS";
	case STRING_DATA: return "STRING_DATA";
	case TEXT: return "TEXT";
	case ZERO_DATA: return "ZERO_DATA";
	default:
	    Util.assert(false, "Unknown segment type "+segtype);
	    return "UNKNOWN SEGMENT TYPE"; // if assertions disabled
	}
    }

    /** The type of segment this <code>SEGMENT</code> precedes. */
    public final int segtype; 

    /** Class constructor. 
     *  <code>segtype</code> must be one the segments types specified
     *  in this class. */
    public SEGMENT(TreeFactory tf, HCodeElement source, int segtype) { 
	super(tf, source); 
	this.segtype = segtype;
	Util.assert(segtype>=0 && segtype<10);
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
	sb.append(decode(segtype));
	sb.append(">");
	return sb.toString();
    }
}
