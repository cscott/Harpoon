// Type.java, created Fri Feb  5 05:16:21 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

/**
 * <code>Type</code> enumerates the possible Tree expression types.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Type.java,v 1.2 2002-02-25 21:05:42 cananian Exp $
 */
public abstract class Type {
    // enumerated constants.
    /** 32-bit integer type. */
    public final static int INT = 0;
    /** 64-bit integer type. */
    public final static int LONG = 1;
    /** 32-bit floating-point type. */
    public final static int FLOAT = 2;
    /** 64-bit floating-point type. */
    public final static int DOUBLE = 3;
    /** Pointer type.  Bitwidth is machine-dependent. */
    public final static int POINTER = 4;

    // Query functions.
    public final static boolean isDoubleWord(TreeFactory tf, int type) {
	if (type==POINTER) return tf.getFrame().pointersAreLong();
	else return type==LONG || type==DOUBLE;
    }
    public final static boolean isFloatingPoint(int type) {
	return type==FLOAT || type==DOUBLE;
    }
    public final static boolean isPointer(int type) {
	return type==POINTER;
    }

    public static boolean isValid(int type) {
	switch(type) {
	case INT: case LONG: case FLOAT: case DOUBLE: case POINTER:
	    return true;
	default:
	    return false;
	}
    }

    // human-readability.
    /** Return a string describing the given enumerated type. */
    public static String toString(int type) {
	switch(type) {
	case INT: return "INT";
	case LONG: return "LONG";
	case FLOAT: return "FLOAT";
	case DOUBLE: return "DOUBLE";
	case POINTER: return "POINTER";
	default: throw new RuntimeException("Unknown Type: "+type);
	}
    }
    /** Return a string describing the type of a <code>PreciselyTyped</code>
     *  expression. */
    public static String toString(PreciselyTyped pt) {
	String ty = toString(pt.type());
	if (pt.isSmall())
	    ty += "["+(pt.signed()?"s":"u")+":"+pt.bitwidth()+"]";
	return ty;
    }
}
