// Uop.java, created Thu Feb  4 22:13:12 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

/**
 * <code>Uop</code> is an enumerated type for <code>UNOP</code>s.
 * Operations are typed: pointer (P), integer (I), long (L), float (F) or
 * double (D).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Uop.java,v 1.1.2.1 1999-02-05 10:40:46 cananian Exp $
 */
public class Uop  {
    public final static int
	// negation
	NEG=0,
	// binary NOT
	NOT=1,
	// int conversion
	_2B=2, _2C=3, _2S=4,
	// general conversion
	_2I=5, _2L=6, _2F=7, _2D=8;

    /** Determines if the given <code>Uop</code> value is valid. */
    public static boolean isValid(int op) {
	switch (op) {
	case NEG:
	case NOT:
	case _2B: case _2C: case _2S:
	case _2I: case _2L: case _2F: case _2D:
	    return true;
	default:
	    return false;
	}
    }
    
    /** Converts the enumerated <code>Uop</code> value to 
     *  a human-readable string. */
    public static String toString(int op) {
	switch (op) {
	case NEG:	return "neg";
	case NOT:	return "not";
	case _2B:	return "_2b";
	case _2C:	return "_2c";
	case _2S:	return "_2s";
	case _2I:	return "_2i";
	case _2L:	return "_2l";
	case _2F:	return "_2f";
	case _2D:	return "_2d";
	default:	throw new RuntimeException("Unknown Uop type: "+op);
	}
    }
}
