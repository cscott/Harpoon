// Bop.java, created Thu Feb  4 21:40:35 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

/**
 * <code>Bop</code> is an enumerated type for <code>BINOP</code>s.
 * Operations are typed: pointer (A), integer (I), long (L), float (F) or
 * double (D).
 * <p>
 * Basic rationale: Include full set of comparisons so we can optimize
 * jump direction for best cache locality, etc.  Include minimal
 * arithmetic, because we can pattern match <code>(x+(-y))</code> for SUB
 * pretty easily. Include full set of bitwise-operators to make it easier to
 * pattern-match on those architectures with insanely complete sets of
 * boolean ops (eg, SPARC has AND/OR/NAND/NOR/XOR/XNOR...).
 * Allow fully-flexible typing for easy pointer manipulation (ie pointer
 * shifts, pointer ANDs, pointer ORs, etc).
 * <p>
 * Note that SHL/SHR/USHR mask off all but the low 5 or 6 bits of
 * their right-hand operand, just as Qop.xSHL/xSHR/xUSHR do.
 * Also note that the NOT operation is included in <code>Uop</code>, but
 * not in <code>harpoon.IR.Quads.Qop</code>.  Thus you'll have to do
 * some pattern matching on <code>(x ^ (-1))</code> to properly generate
 * <code>Uop.NOT</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Bop.java,v 1.1.2.6 2000-02-16 07:11:56 cananian Exp $
 */
public abstract class Bop  {
    // Comparison operations
    /** If less-than, then 1 else 0. */
    public final static int CMPLT=0;
    /** If less-than-or-equal-to, then 1 else 0. */
    public final static int CMPLE=1;
    /** If equal-to, then 1 else 0. */
    public final static int CMPEQ=2;
    /** If greater-than-or-equal-to, then 1 else 0. */
    public final static int CMPGE=3;
    /** If greater-than, then 1 else 0. */
    public final static int CMPGT=4;
    // General arithmetic
    /** Addition. */
    public final static int ADD=5;
    /** Multiplication. */
    public final static int MUL=6;
    /** Division. */
    public final static int DIV=7;
    /** Remainder operation. Note that this is valid for floating-point
     *  as well as integer arithmetic; see the JVM definition of
     *  <code>frem</code>. Basically, this is remainder after a 
     *  truncating division for both integer and floating-point. */
    public final static int REM=8;
    // integer arithmetic
    /** Left bit-wise shift; long/integer only. */
    public final static int SHL =9;
    /** Right signed bit-wise shift; long/integer only. */
    public final static int SHR =10;
    /** Right unsigned bit-wise shift; long/integer only. */
    public final static int USHR=11;
    /** Bit-wise AND; long/integer only. */
    public final static int AND =12;
    /** Bit-wise OR; long/integer only. */
    public final static int OR  =13;
    /** Bit-wise XOR; long/integer only. */
    public final static int XOR =14;

    /** Determines if the given <code>Bop</code> value is valid. */
    public static boolean isValid(int op) {
	switch(op) {
	case CMPLT: case CMPLE: case CMPEQ: case CMPGE: case CMPGT:
	case ADD: case MUL: case DIV: case REM:
	case SHL: case SHR: case USHR: case AND: case OR: case XOR:
	    return true;
	default:
	    return false;
	}
    }

    /** Converts the enumerated <code>Bop</code> value to a human-readable
     *  string. */
    public static String toString(int op) {
	switch(op) {
	case CMPLT:	return "cmplt";
	case CMPLE:	return "cmple";
	case CMPEQ:	return "cmpeq";
	case CMPGE:	return "cmpge";
	case CMPGT:	return "cmpgt";
	case ADD:	return "add";
	case MUL:	return "mul";
	case DIV:	return "div";
	case REM:	return "rem";
	case SHL:	return "shl";
	case SHR:	return "shr";
	case USHR:	return "ushr";
	case AND:	return "and";
	case OR:	return "or";
	case XOR:	return "xor";
	default:	throw new RuntimeException("Unknown Bop type: "+op);
	}
    }

    /** Determines if the given <code>Bop</code> operation
     *  is commutative. */
    public static final boolean isCommutative(int op) {
	switch(op) {
	case CMPEQ: case ADD: case MUL: case AND: case OR: case XOR:
	    return true;
	default:
	    return false;
	}
    }
    /** Determines if the given <code>Bop</code> operation
     *  is associative. */
    public static final boolean isAssociative(int op) {
	switch(op) {
	case ADD: case MUL: case AND: case OR: case XOR:
	    return true;
	default:
	    return false;
	}
    }
}
