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
 * @version $Id: Bop.java,v 1.1.2.3 1999-03-29 05:07:05 duncan Exp $
 */
public abstract class Bop  {
    public final static int
	// Comparison operations
	CMPLT=0, CMPLE=1, CMPEQ=2, CMPGE=3, CMPGT=4,
	// General arithmetic
	ADD=5, MUL=6, DIV=7, REM=8,
	// integer arithmetic
	SHL=9, SHR=10, USHR=11, AND=12, OR=13, XOR=14;

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

    // wrapper functions.
    private static Integer _i(int i)     { return new Integer(i); }
    private static Long    _l(long l)    { return new Long(l);    }
    private static Float   _f(float f)   { return new Float(f);   }
    private static Double  _d(double d)  { return new Double(d);  }
    private static Boolean _b(boolean b) { return new Boolean(b); }
    // unwrapper functions.
    private static int    _i(Object o) { return ((Integer)o).intValue(); }
    private static long   _l(Object o) { return ((Long)o)   .longValue(); }
    private static float  _f(Object o) { return ((Float)o)  .floatValue(); }
    private static double _d(Object o) { return ((Double)o) .doubleValue(); }

}
