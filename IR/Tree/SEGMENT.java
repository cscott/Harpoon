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
 * @version $Id: SEGMENT.java,v 1.1.2.7 1999-08-11 10:48:39 duncan Exp $
 */
public class SEGMENT extends Stm {
    /** Storage for static class data (display, vmtable, etc) */
    public static final int CLASS                = 0; 
    /** Read-only instruction memory */
    public static final int CODE                 = 1;
    /** Storage for GC tables */
    public static final int GC                   = 2;
    /** R/W memory that must be initialized before use */
    public static final int INIT_DATA            = 3; 
    /** Storage for static aggregate data */
    public static final int STATIC_OBJECTS       = 4;
    /** Storage for static primitive data */
    public static final int STATIC_PRIMITIVES    = 5;
    /** Storage for string constants */
    public static final int STRING_CONSTANTS     = 6;
    /** Storage for character arrays of statically allocated 
     *  string constants. */
    public static final int STRING_CONSTANTS_CA  = 7;
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
	case TEXT: return "TEXT";
	case ZERO_DATA: return "ZERO_DATA";
	default: Util.assert(false, "Unknown segment type "+segtype); return null;
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
	sb.append(segtype==CLASS               ? "CLASS"               :
		  segtype==CODE                ? "CODE"                :
		  segtype==GC                  ? "GC"                  :
		  segtype==INIT_DATA           ? "INIT_DATA"           :
		  segtype==STATIC_OBJECTS      ? "STATIC_OBJECTS"      :
		  segtype==STATIC_PRIMITIVES   ? "STATIC_PRIMITIVES"   :
		  segtype==STRING_CONSTANTS    ? "STRING_CONSTANTS"    :
		  segtype==STRING_CONSTANTS_CA ? "STRING_CONSTANTS_CA" :
		  segtype==TEXT                ? "TEXT"                :
		  segtype==ZERO_DATA           ? "ZERO_DATA"           :
		  "UNKNOWN SEGMENT TYPE"); 
	sb.append(">");
	return sb.toString();
    }
}

