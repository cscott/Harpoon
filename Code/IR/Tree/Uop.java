// Uop.java, created Thu Feb  4 22:13:12 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

/**
 * <code>Uop</code> is an enumerated type for <code>UNOP</code>s.
 * Operations are typed: pointer (P), integer (I), long (L), float (F) or
 * double (D).
 * <p>
 * See <code>Bop</code> for basic rationale.  We provide full set of
 * conversion operations and both negation and bitwise-not.  Some of
 * these <code>Uop</code>s are not in <code>harpoon.IR.Quads.Qop</code>
 * and thus will require pattern-matching during translation for
 * proper generation.  This could also be done in a peephole optimization.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Uop.java,v 1.2 2002-02-25 21:05:42 cananian Exp $
 */
public class Uop  {

    /** Negation (<code>-</code>). */
    public final static int NEG=0;
    /** Bit-wise NOT (<code>~</code>). */
    public final static int NOT=1;

	// int conversion

    /** Converts an int to a byte.  Result is still int type, but it
	is truncated to 8 bits, then sign extended. 
    */ 
    public final static int I2B=2; 

    /** Converts an int to a character.  Result is still int type, but
	is truncated to 16 bits.  No sign extension.
    */
    public final static int I2C=3; 

    /** Converts an int to a short.  Result is still int type, but is
	truncated to 16 bits, then sign extended.
    */
    public final static int I2S=4;

	// general conversion
    /** Converts to int. */
    public final static int _2I=5; 
    /** Converts to long. */
    public final static int _2L=6; 
    /** Converts to float. */
    public final static int _2F=7; 
    /** Converts to double. */
    public final static int _2D=8;

    /** Determines if the given <code>Uop</code> value is valid. */
    public static boolean isValid(int op) {
	switch (op) {
	case NEG:
	case NOT:
	case I2B: case I2C: case I2S:
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
	case I2B:	return "i2b";
	case I2C:	return "i2c";
	case I2S:	return "i2s";
	case _2I:	return "_2i";
	case _2L:	return "_2l";
	case _2F:	return "_2f";
	case _2D:	return "_2d";
	default:	throw new RuntimeException("Unknown Uop type: "+op);
	}
    }
}
