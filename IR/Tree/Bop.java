// Bop.java, created Thu Feb  4 21:40:35 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

/**
 * <code>Bop</code> is an enumerated type for <code>BINOP</code>s.
 * Operations are typed: pointer (A), integer (I), long (L), float (F) or
 * double (D).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Bop.java,v 1.1.2.1 1999-02-05 10:40:41 cananian Exp $
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
}
