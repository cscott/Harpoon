// BINOP.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>BINOP</code> objects are expressions which stand for result of
 * applying some binary operator <i>o</i> to a pair of subexpressions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: BINOP.java,v 1.3 2002-02-26 22:46:10 cananian Exp $
 * @see Bop
 */
public class BINOP extends OPER {
    /** Constructor.
     * @param binop Enumerated operation type, from <code>Bop</code>.
     */
    public BINOP(TreeFactory tf, HCodeElement source,
		 int optype, int binop, Exp left, Exp right) {
	super(tf, source, optype, binop, 2);
	Util.ASSERT(left != null && right != null);
	this.setLeft(left); this.setRight(right); 
	Util.ASSERT(Bop.isValid(binop));
	Util.ASSERT(tf==right.tf,"Left and Right must have same tree factory");
	Util.ASSERT(tf==left.tf,"This and Left must have same tree factory");

	// FSK: debugging hack
	// this.accept(TreeVerifyingVisitor.norepeats());
    }

    // binops defined in harpoon.IR.Tree.Bop.
    public int type() {
	switch(op) {
	case Bop.CMPLT: case Bop.CMPLE:
	case Bop.CMPEQ: case Bop.CMPNE:
	case Bop.CMPGE: case Bop.CMPGT:
	    return Type.INT; // boolean comparison result
	default:
	    return optype;
	}
    }
    
    /** Returns the subexpression of the left-hand side of the operator. */
    public Exp getLeft() { return (Exp) getChild(0); } 
    /** Returns the subexpression of the right-hand side of the operator. */
    public Exp getRight() { return (Exp) getChild(1); } 

    /** Sets the subexpression of the left-hand side of the operator. */
    public void setLeft(Exp left) {  setChild(0, left); }
    /** Sets the subexpression of the right-hand side of the operator. */
    public void setRight(Exp right) { setChild(1, right); }
    
    public int kind() { return TreeKind.BINOP; }
    
    public Exp build(TreeFactory tf, ExpList kids) {
	Util.ASSERT(kids!=null && kids.tail!=null && kids.tail.tail==null);
	Util.ASSERT(tf == kids.head.tf && tf == kids.tail.head.tf);
	return new BINOP(tf, this, optype, op, kids.head, kids.tail.head);
    }
    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }
  
    public Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb) {
        return cb.callback(this, new BINOP(tf, this, optype, op, 
					   (Exp)getLeft().rename(tf, tm, cb), 
					   (Exp)getRight().rename(tf, tm, cb)),
			   tm);
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
	case Bop.CMPNE: 
	    switch (optype) {
	    case Type.INT:      return _i((_i(left)!=_i(right))?1:0);
	    case Type.LONG:     return _i((_l(left)!=_l(right))?1:0);
	    case Type.FLOAT:    return _i((_f(left)!=_f(right))?1:0);
	    case Type.DOUBLE:   return _i((_d(left)!=_d(right))?1:0);
	    case Type.POINTER:  
		return Type.isDoubleWord(tf, optype) ?
		    _i(_l(left)!=_l(right)?1:0) :
			_i(_i(left)!=_i(right)?1:0);
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
               ", #" + getLeft().getID() + ", #" + getRight().getID() + ")";
    }
}

