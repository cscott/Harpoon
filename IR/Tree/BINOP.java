// BINOP.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

/**
 * <code>BINOP</code> objects are expressions which stand for result of
 * applying some binary operator <i>o</i> to a pair of subexpressions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: BINOP.java,v 1.1.2.21 2000-01-09 01:04:41 duncan Exp $
 * @see Bop
 */
public class BINOP extends OPER {
    /** The subexpression of the left-hand side of the operator. */
    private Exp left;
    /** The subexpression of the right-hand side of the operator. */
    private Exp right;
    /** Constructor.
     * @param binop Enumerated operation type, from <code>Bop</code>.
     */
    public BINOP(TreeFactory tf, HCodeElement source,
		 int optype, int binop, Exp left, Exp right) {
	super(tf, source, optype, binop);
	this.left = left; this.right = right; 
	this.setLeft(left); this.setRight(right); 
	Util.assert(Bop.isValid(binop));
	Util.assert(tf==right.tf,"Left and Right must have same tree factory");
	Util.assert(tf==left.tf,"This and Left must have same tree factory");
    }

    // binops defined in harpoon.IR.Tree.Bop.
    public int type() {
	switch(op) {
	case Bop.CMPLT: case Bop.CMPLE: case Bop.CMPEQ:
	case Bop.CMPGE: case Bop.CMPGT:
	    return Type.INT; // boolean comparison result
	default:
	    return optype;
	}
    }
    
    public Tree getFirstChild() { return left; } 
    
    public Exp getLeft() { return this.left; } 
    public Exp getRight() { return this.right; } 

    public void setLeft(Exp left) { 
	this.left         = left; 
	this.left.parent  = this; 
	this.left.sibling = right; 
    }

    public void setRight(Exp right) { 
	this.right         = right;
	this.right.parent  = this;
	this.right.sibling = null;
	this.left.sibling  = this.right; 
    }
    
    public int kind() { return TreeKind.BINOP; }
    
    public Exp build(ExpList kids) { return build(tf, kids); }

    public Exp build(TreeFactory tf, ExpList kids) {
	Util.assert(tf == kids.head.tf && tf == kids.tail.head.tf);
	return new BINOP(tf, this, optype, op, kids.head, kids.tail.head);
    }
    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }
  
    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new BINOP(tf, this, optype, op, 
			 (Exp)left.rename(tf, ctm), 
			 (Exp)right.rename(tf, ctm));
    }

    /** Evaluates a constant value for the result of a <code>BINOP</code>, 
     *  given constant values for the operands. */
    public static Object evalValue(TreeFactory tf, 
				   int op, int optype, 
				   Object left, Object right) {
        switch(op) {
	case Bop.CMPLT:	
	    switch(optype) {
	    case Type.INT:      return _i((_i(left)<_i(right))?1:0);
	    case Type.LONG:     return _i((_l(left)<_l(right))?1:0);
	    case Type.FLOAT:    return _i((_f(left)<_f(right))?1:0);
	    case Type.DOUBLE:   return _i((_d(left)<_d(right))?1:0);
	    case Type.POINTER:  
		return Type.isDoubleWord(tf, optype) ?
		    _i(_l(left)<_l(right)?1:0) :
			_i(_i(left)<_i(right)?1:0);
	    }
	case Bop.CMPLE:	
	    switch(optype) {
	    case Type.INT:      return _i((_i(left)<=_i(right))?1:0);
	    case Type.LONG:     return _i((_l(left)<=_l(right))?1:0);
	    case Type.FLOAT:    return _i((_f(left)<=_f(right))?1:0);
	    case Type.DOUBLE:   return _i((_d(left)<=_d(right))?1:0);
	    case Type.POINTER:  
		return Type.isDoubleWord(tf, optype) ?
		    _i(_l(left)<=_l(right)?1:0) :
			_i(_i(left)<=_i(right)?1:0);
	    }
	case Bop.CMPEQ: 
	    switch (optype) {
	    case Type.INT:      return _i((_i(left)==_i(right))?1:0);
	    case Type.LONG:     return _i((_l(left)==_l(right))?1:0);
	    case Type.FLOAT:    return _i((_f(left)==_f(right))?1:0);
	    case Type.DOUBLE:   return _i((_d(left)==_d(right))?1:0);
	    case Type.POINTER:  
		return Type.isDoubleWord(tf, optype) ?
		    _i(_l(left)==_l(right)?1:0) :
			_i(_i(left)==_i(right)?1:0);
	    }
	case Bop.CMPGE:
	    switch (optype) {
	    case Type.INT:      return _i((_i(left)>=_i(right))?1:0);
	    case Type.LONG:     return _i((_l(left)>=_l(right))?1:0);
	    case Type.FLOAT:    return _i((_f(left)>=_f(right))?1:0);
	    case Type.DOUBLE:   return _i((_d(left)>=_d(right))?1:0);
	    case Type.POINTER:  
		return Type.isDoubleWord(tf, optype) ?
		    _i(_l(left)>=_l(right)?1:0) :
			_i(_i(left)>=_i(right)?1:0);
	    }
	case Bop.CMPGT:	
	    switch (optype) {
	    case Type.INT:      return _i((_i(left)>_i(right))?1:0);
	    case Type.LONG:     return _i((_l(left)>_l(right))?1:0);
	    case Type.FLOAT:    return _i((_f(left)>_f(right))?1:0);
	    case Type.DOUBLE:   return _i((_d(left)>_d(right))?1:0);
	    case Type.POINTER:  
		return Type.isDoubleWord(tf, optype) ?
		    _i(_l(left)>_l(right)?1:0) :
			_i(_i(left)>_i(right)?1:0);
	    }
	case Bop.ADD:	
	    switch (optype) {
	    case Type.INT:      return _i(_i(left)+_i(right));
	    case Type.LONG:     return _l(_l(left)+_l(right));
	    case Type.FLOAT:    return _f(_f(left)+_f(right));
	    case Type.DOUBLE:   return _d(_d(left)+_d(right));
	    case Type.POINTER:  
	      return Type.isDoubleWord(tf, optype) ?
		    (Object)_l(_l(left)+_l(right)) :
			(Object)_i(_i(left)+_i(right));
	    }
	case Bop.MUL:	
	    switch (optype) {
	    case Type.INT:      return _i(_i(left)*_i(right));
	    case Type.LONG:     return _l(_l(left)*_l(right));
	    case Type.FLOAT:    return _f(_f(left)*_f(right));
	    case Type.DOUBLE:   return _d(_d(left)*_d(right));
	    case Type.POINTER:  
   	      return Type.isDoubleWord(tf, optype) ?
		    (Object)_l(_l(left)*_l(right)) :
			(Object)_i(_i(left)*_i(right));
	    }
	case Bop.DIV:
	    switch (optype) {
	    case Type.INT:      return _i(_i(left)/_i(right));
	    case Type.LONG:     return _l(_l(left)/_l(right));
	    case Type.FLOAT:    return _f(_f(left)/_f(right));
	    case Type.DOUBLE:   return _d(_d(left)/_d(right));
	    case Type.POINTER:  
		throw new Error("Operation not supported");
	    }
	case Bop.REM: 
	    switch (optype) {
	    case Type.INT:      return _i(_i(left)%_i(right));
	    case Type.LONG:     return _l(_l(left)%_l(right));
	    case Type.FLOAT:    return _f(_f(left)%_f(right));
	    case Type.DOUBLE:   return _d(_d(left)%_d(right));
	    case Type.POINTER:  
		throw new Error("Operation not supported");
	    }
	case Bop.SHL:
	    switch (optype) {
	    case Type.INT:      return _i(_i(left)<<_i(right));
	    case Type.LONG:     return _l(_l(left)<<_i(right));
	    case Type.FLOAT:
	    case Type.DOUBLE:
	    case Type.POINTER:
		throw new Error("Operation not supported");
	    }
	case Bop.SHR:
	    switch (optype) {
	    case Type.INT:      return _i(_i(left)>>_i(right));
	    case Type.LONG:     return _l(_l(left)>>_i(right));
	    case Type.FLOAT:
	    case Type.DOUBLE:
	    case Type.POINTER:
		throw new Error("Operation not supported");
	    }
	case Bop.USHR:
	    switch (optype) {
	    case Type.INT:      return _i(_i(left)>>>_i(right));
	    case Type.LONG:     return _l(_l(left)>>>_i(right));
	    case Type.FLOAT:
	    case Type.DOUBLE:
	    case Type.POINTER:
		throw new Error("Operation not supported");
	    }
	case Bop.AND:
	    switch (optype) {
	    case Type.INT:      return _i(_i(left)&_i(right));
	    case Type.LONG:     return _l(_l(left)&_l(right));
	    case Type.FLOAT:
	    case Type.DOUBLE:
	    case Type.POINTER:
		throw new Error("Operation not supported");
	    }
	case Bop.OR:
	    switch (optype) {
	    case Type.INT:      return _i(_i(left)|_i(right));
	    case Type.LONG:     return _l(_l(left)|_l(right));
	    case Type.FLOAT:
	    case Type.DOUBLE:
	    case Type.POINTER:
		throw new Error("Operation not supported");
	    }
	case Bop.XOR:
	    switch (optype) {
	    case Type.INT:      return _i(_i(left)^_i(right));
	    case Type.LONG:     return _l(_l(left)^_l(right));
	    case Type.FLOAT:
	    case Type.DOUBLE:
	    case Type.POINTER:
		throw new Error("Operation not supported");
	    }
	default:	
	    throw new RuntimeException("Unknown Bop type: "+op);
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

    public String toString() {
        return "BINOP<"+Type.toString(optype)+">("+Bop.toString(op)+
               ", #" + left.getID() + ", #" + right.getID() + ")";
    }
}

