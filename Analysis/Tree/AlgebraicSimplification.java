// AlgebraicSimplification.java, created Sat Dec 18 17:42:19 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree; 

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.BINOP; 
import harpoon.IR.Tree.Bop; 
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.Code; 
import harpoon.IR.Tree.CONST; 
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.ESEQ; 
import harpoon.IR.Tree.EXPR;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.SEQ; 
import harpoon.IR.Tree.Stm; 
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Tree; 
import harpoon.IR.Tree.TreeFactory; 
import harpoon.IR.Tree.TreeKind; 
import harpoon.IR.Tree.TreeVisitor; 
import harpoon.IR.Tree.Type; 
import harpoon.IR.Tree.UNOP; 
import harpoon.IR.Tree.Uop; 
import harpoon.Temp.Temp;
import harpoon.Util.Util; 

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator; 
import java.util.List; 
import java.util.Stack; 

/**
 * <code>AlgebraicSimplification</code> performs algebraic simplification
 * on canonical trees. 
 * 
 * <B>Warning:</B> this performs modifications on the tree form in place.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: AlgebraicSimplification.java,v 1.1.2.24 2001-01-23 23:45:15 cananian Exp $
 */
// XXX missing -K1 --> K2  and ~K1 --> K2 rules.
public abstract class AlgebraicSimplification extends Simplification { 
    // hide constructor
    private AlgebraicSimplification() { }

    private final static List _DEFAULT_RULES = new ArrayList(); 
    /** Default alegraic simplification rules. */
    public final static List DEFAULT_RULES = // protect the rules list.
	Collections.unmodifiableList(_DEFAULT_RULES);

    /** Code factory for applying the default set of simplifications to
     *  the given tree form.  Clones the tree before simplifying it
     *  in-place. */
    public static HCodeFactory codeFactory(final HCodeFactory parent) {
	return codeFactory(parent, DEFAULT_RULES);
    }

    // Static initialization: add all available rules to the rule set. 
    // 
    static { 
	// K1 + K2 --> K3
	// K1 * K2 --> K3
	// K1 << K2 --> K3
	// K1 >> K2 --> K3
	// K1 >>> K2 --> K3
	// K1 & K2 --> K3
	// K1 | K2 --> K3
	// K1 ^ K2 --> K3 
	// 
	Rule combineConstants = new Rule("combineConstants") { 
	    public boolean match(Exp e) { 
		if (_KIND(e) != _BINOP) { return false; } 
		else { 
		    BINOP b = (BINOP)e; 
		    return 
		    contains(_OP(b.op),
			     _ADD|_MUL|_SHL|_SHR|_USHR|_AND|_OR|_XOR) &&
		    contains(_KIND(b.getLeft()), _CONST) &&
		    contains(_KIND(b.getRight()), _CONST) &&
		    !b.isFloatingPoint();
		}
	    }
	    
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) { 
		BINOP b  = (BINOP)e; 
		CONST k1 = (CONST)b.getLeft();
		CONST k2 = (CONST)b.getRight(); 

		Object k1pk2 = harpoon.IR.Tree.BINOP.evalValue
		    (tf, b.op, b.optype, k1.value, k2.value);


		switch (b.type()) { 
		    case Type.POINTER:
		    Util.assert(k1.type()==Type.INT && k2.type()==Type.INT);
		    case Type.INT: 
		        return new CONST(tf,b,((Integer)k1pk2).intValue());
		    case Type.LONG: 
		        return new CONST(tf,b,((Long)k1pk2).longValue());
		    default: 
		        throw new Error("Invalid type: " + b.type());
		}
	    }
	};  

	// const + exp --> exp + const
	// const * exp --> exp * const
	// const & exp --> exp & const
	// const | exp --> exp | const
	// const ^ exp --> exp ^ const
	// const ==exp --> exp ==const
	// const !=exp --> exp !=const
	//
	Rule commute = new Rule("commute") { 
	    public boolean match(Exp e) { 
		if (_KIND(e) != _BINOP) { return false; } 
		else { 
		    BINOP b = (BINOP)e; 
		    return 
		    Bop.isCommutative(b.op) &&
		    contains(_KIND(b.getLeft()), _CONST) &&
		    !contains(_KIND(b.getRight()), _CONST) &&
		    !b.isFloatingPoint();
		}
	    }
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) { 
		BINOP b = (BINOP)e; 
		return new BINOP(tf, b, b.optype, b.op, b.getRight(), b.getLeft()); 
 	    }
	};


	// const < exp --> exp > const
	// const <=exp --> exp >=const
	// const > exp --> exp < const
	// const >=exp --> exp <=const
	//
	Rule commute2 = new Rule("commute2") { 
	    public boolean match(Exp e) { 
		if (_KIND(e) != _BINOP) { return false; } 
		else { 
		    BINOP b = (BINOP)e; 
		    return 
		    (b.op==Bop.CMPLT || b.op==Bop.CMPLE ||
		     b.op==Bop.CMPGT || b.op==Bop.CMPGE) &&
		    contains(_KIND(b.getLeft()), _CONST) &&
		    !contains(_KIND(b.getRight()), _CONST);
		}
	    }
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) { 
		BINOP b = (BINOP)e; 
		int op;
		switch(b.op) {
		case Bop.CMPLT: op=Bop.CMPGT; break;
		case Bop.CMPLE: op=Bop.CMPGE; break;
		case Bop.CMPGE: op=Bop.CMPLE; break;
		case Bop.CMPGT: op=Bop.CMPLT; break;
		default: throw new Error("impossible");
		}
		return new BINOP(tf, b, b.optype, op, b.getRight(), b.getLeft()); 
 	    }
	};


	// (exp + const) + const --> exp + (const + const)
	// (exp * const) * const --> exp * (const * const)
	// (exp & const) & const --> exp & (const & const)
	// (exp | const) | const --> exp | (const | const)
	// (exp ^ const) ^ const --> exp ^ (const ^ const)
	//      note that == is not associative.
	Rule associate = new Rule("associate") {
	    public boolean match(Exp e) {
		if (_KIND(e) != _BINOP) return false;
		BINOP b1 = (BINOP) e; 
		if (_KIND(b1.getLeft()) != _BINOP) return false;
		BINOP b2 = (BINOP) b1.getLeft();
		if (b1.op != b2.op) return false;
		return
		contains(_OP(b1.op), _ADD|_MUL|_AND|_OR|_XOR) &&
		contains(_KIND(b1.getRight()), _CONST) &&
		contains(_KIND(b2.getRight()), _CONST) &&
		(b1.operandType() == b2.operandType()) &&
		!b1.isFloatingPoint();
	    }
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) { 
		BINOP b1 = (BINOP) e;
		BINOP b2 = (BINOP) b1.getLeft();
		int bop = b1.op, optype = b1.optype;
		// be careful not to screw with commutativity.
		return new BINOP(tf, e, optype, bop, b2.getLeft(),
				 new BINOP(tf, e, optype, bop,
					   b2.getRight(), b1.getRight()));
 	    }
	};


	// exp & 0 --> 0
	// exp * 0 --> 0
	// exp % 1 --> 0
	//
	Rule makeZero = new Rule("makeZero") {
	    // NOTE: this rule creates non-canonical tree form.
	    public boolean match(Exp e) { 
		if (_KIND(e) != _BINOP) { return false; } 
		else { 
		    BINOP b = (BINOP)e; 
		    if (b.type()!=Type.INT && b.type()!=Type.LONG)
		        return false;
		    // first the weird rem case
		    if (b.op == Bop.REM &&
			contains(_KIND(b.getRight()), _CONST1)) return true;
		    // now 'operation with zero' cases
		    return contains(_OP(b.op), _AND|_MUL) &&
		           contains(_KIND(b.getRight()), _CONST0);
		}
	    }
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) { 
		BINOP b = (BINOP)e; 
		CONST c;
		if (b.type()==Type.INT)
		    c = new CONST(tf, e, (int) 0);
		else if (b.type()==Type.LONG)
		    c = new CONST(tf, e, (long) 0);
		else throw new Error("ack");
		return new ESEQ(tf, b, new EXPR(tf, b, b.getLeft()), c);
	    }
	};


	// exp + 0   --> exp, 
	// exp | 0   --> exp,
	// exp ^ 0   --> exp,
	// exp << 0  --> exp, 
	// exp >> 0  --> exp,
	// exp >>> 0 --> exp
	// exp * 1   --> exp
	// exp / 1   --> exp
	// 
	Rule removeZero = new Rule("removeZero") { 
	    public boolean match(Exp e) { 
		if (_KIND(e) != _BINOP) { return false; } 
		else { 
		    BINOP b = (BINOP)e; 
		    if (b.isFloatingPoint()) return false;
		    // first handle mul/div cases.
		    if (contains(_OP(b.op), _MUL|_DIV) &&
			contains(_KIND(b.getRight()), _CONST1)) return true;
		    // now the 'operation-with-zero' cases.
		    return 
		    contains(_OP(b.op), _ADD|_OR|_XOR|_SHL|_SHR|_USHR) &&
		    contains(_KIND(b.getRight()), _CONST0);
		}
	    }
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) { 
		BINOP b = (BINOP)e; 
		return b.getLeft(); 
	    }
	};
	
	
	// x ^ -1 --> ~ x
	//
	Rule createNot = new Rule("createNot") {
	    // note that since Qop doesn't have NOT, we have to recreate it.
	    public boolean match(Exp e) { 
		if (_KIND(e) != _BINOP) return false;
		BINOP b = (BINOP) e;
		if (b.op != Bop.XOR ) return false;
		return contains(_KIND(b.getRight()), _CONSTm1);
	    } 
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) {  
		BINOP b = (BINOP) e;
		Util.assert(b.op == Bop.XOR);
		return new UNOP(tf, e, b.optype, Uop.NOT, b.getLeft());
	    } 
	};


	// -(-i) --> i 
	// ~(~i) --> i
	// 
	Rule doubleNegative = new Rule("doubleNegative") { 
	    public boolean match(Exp e) { 
		if (_KIND(e) != _UNOP) { return false; } 
		else { 
		    UNOP u1 = (UNOP)e; 
		    if (_KIND(u1.getOperand()) != _UNOP) { return false; } 
		    else { 
			UNOP u2 = (UNOP)u1.getOperand(); 
			return (u1.op == Uop.NEG && u2.op == Uop.NEG) ||
			       (u1.op == Uop.NOT && u2.op == Uop.NOT); 
		    }
		}
	    } 
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) {  
		UNOP u1 = (UNOP)e;  
		UNOP u2 = (UNOP)u1.getOperand();  
		Util.assert(u1.op == u2.op);  
		return u2.getOperand();  
	    } 
	}; 

	// -0 --> 0 
	//
	Rule negZero = new Rule("negZero") {  
	    public boolean match(Exp e) { 
		if (_KIND(e) != _UNOP) { return false; } 
		else { 
		    UNOP u = (UNOP)e; 
		    if (u.isFloatingPoint()) return false;
		    if (u.op != Uop.NEG) return false;
		    return contains(_KIND(u.getOperand()), _CONST0);
		}
	    }
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) { 
		UNOP u = (UNOP)e; 
		Util.assert(contains(_KIND(u.getOperand()), _CONST0));
		return u.getOperand(); 
	    } 
	}; 

	// exp * const --> (recoded as shifts)
	Rule mulToShift = new Rule("mulToShift") { 
	    // NOTE: this rule may create non-canonical form.
	    public boolean match (Exp e) { 
		if (_KIND(e) != _BINOP) { return false; } 
		else { 
		    BINOP b = (BINOP)e; 
		    if (b.op != Bop.MUL) return false;
		    if (!contains(_KIND(b.getRight()), _CONST)) return false;
		    if (contains(_KIND(b.getLeft()), _CONST)) return false;
		    else { 
			CONST c = (CONST)b.getRight(); 
			return 
			    c.value.longValue() > 0                     &&
		            !b.isFloatingPoint(); 
		    }
		}
	    }
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) { 
		BINOP b = (BINOP)e; 
		return mul2shift(b.getLeft(), (CONST)b.getRight()); 
	    }
	}; 


	// exp / const --> (recoded as multiplication)
	Rule divToMul = new Rule("divToMul") { 
	    // NOTE: this rule may create non-canonical form.
	    public boolean match(Exp e) { 
		if (e.type() != Type.INT ) return false;
		if (_KIND(e) != _BINOP) { return false; } 
		else { 
		    BINOP b = (BINOP)e; 
		    return b.op == Bop.DIV &&
                           contains(_KIND(b.getRight()), _CONST) &&
			   !contains(_KIND(b.getLeft()), _CONST);
		}
	    }
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) { 
		BINOP b = (BINOP)e; 
		Util.assert(b.op == Bop.DIV); 
		return div2mul(b.getLeft(),
			       ((CONST)b.getRight()).value.intValue()); 
	    }
	};  

	// Add rules to the rule set.  
	// 
	_DEFAULT_RULES.add(combineConstants); 
	_DEFAULT_RULES.add(makeZero); // non-canonical
	_DEFAULT_RULES.add(removeZero); 
	_DEFAULT_RULES.add(commute); 
	_DEFAULT_RULES.add(commute2); 
	_DEFAULT_RULES.add(associate); 
	_DEFAULT_RULES.add(createNot);
	_DEFAULT_RULES.add(doubleNegative); 
	_DEFAULT_RULES.add(negZero); 
	_DEFAULT_RULES.add(mulToShift); // non-canonical
	_DEFAULT_RULES.add(divToMul); // non-canonical

	// and re-canonicalize
	_DEFAULT_RULES.addAll(Canonicalize.RULES);
    }
		  
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    //                                                                 //
    //                    Simplification Routines                      //
    //                                                                 //
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//


    /**
     * Converts an arbitrary division by a constant into a series of 
     * multiplications, shifts, and bitwise operations.  Based on the 
     * paper <i>Division by Invariant Integers using Multiplication</i>, 
     * by Granlund and Montgomery.  
     * 
     * This method is used internally by the 
     * <code>AlgebraicSimplification</code> class.  However, this method is
     * public because it could conceivably be of use in other transformations.
     *
     * <b>Requires:</b>  d is a 32-bit integer constant
     *
     * @return  an Exp which contains no divisions, yet 
     *          represents the same value as (n/d). 
     *          not guaranteed to be in canonical form.
     */ 
    public static Exp div2mul(Exp n, int dVal) {
	TreeFactory tf = n.getFactory(); 
	// if d<0, n/d == -(n/|d|)
	// note that we do NOT negate *n*, as that fails for n=-2^(N-1)
	if (dVal < 0)
	    return new UNOP(tf, n, Type.INT, Uop.NEG,
			    div2mul_positive(n, -dVal));
	else return div2mul_positive(n, dVal);
    }
    // input is positive now.
    private static Exp div2mul_positive(Exp n, int dAbs) { 
	final int N=32;
	Util.assert(dAbs >= 0);

	// Get a TreeFactory to use in creating new tree objects
	TreeFactory tf = n.getFactory(); 

	if (dAbs == 0) { // Don't process div-by-0
	    return new BINOP(tf,n,Type.INT,Bop.DIV,n,new CONST(tf, n, dAbs));
	}
	else if (dAbs == 1) { // Dividing by 1
	    return n; 
	}
	MultiplierTuple tuple = CHOOSE_MULTIPLIER(dAbs, N-1);
	long m = tuple.m_high; 
	int sh_post = tuple.sh_post;
	int l = tuple.l;
	
	if (dAbs == (1 << l)) {  // Dividing by a power of 2 
	    return new BINOP(tf, n, Type.INT, Bop.SHR, n, new CONST(tf, n, l));
	}
	else { 
	    Util.assert(m < (1L<<N));
	    // we need to reuse n.
	    Temp t = new Temp(tf.tempFactory(), "dm");
	    MOVE move = new MOVE(tf, n, new TEMP(tf, n, Type.INT, t), n);
	    // q0 = MULUH(m, EOR(n_sign,n))
	    Exp q0 = new UNOP
		(tf, n, Type.INT, Uop._2I,
		 new BINOP
		 (tf, n, Type.LONG, Bop.USHR,
		  new BINOP
		  (tf, n, Type.LONG, Bop.MUL,
		   new CONST(tf, n, m),
		   new UNOP
		   (tf, n, Type.LONG, Uop._2L,
		    new BINOP
		    (tf, n, Type.INT,  Bop.XOR,
		     new BINOP // n_sign = n >> N-1
		     (tf, n, Type.INT, Bop.SHR,
		      new TEMP(tf, n, Type.INT, t),
		      new CONST(tf, n, N-1)),
		     new TEMP(tf, n, Type.INT, t)))),
		  new CONST(tf, n, N)));
	    // q1 = EOR(n_sign, SRL(q0, sh_post))
	    Exp q1 = new BINOP
		(tf, n, Type.INT, Bop.XOR,
		 new BINOP // n_sign = n >> N-1
		 (tf, n, Type.INT, Bop.SHR,
		  new TEMP(tf, n, Type.INT, t),
		  new CONST(tf, n, N-1)),
		 new BINOP // SRL(q0, sh_post)
		 (tf, n, Type.INT, Bop.USHR,
		  q0, new CONST(tf, n, sh_post)
		  ));
	    // done.
	    return new ESEQ(tf, n, move/* move n into t*/, q1);
	}
    }
    private static class MultiplierTuple {
	final long m_high; // range is 0 to 2^32
	final int sh_post, l;
	MultiplierTuple(long m_high, int sh_post, int l) {
	    this.m_high=m_high; this.sh_post=sh_post; this.l=l;
	}
    }
    private static MultiplierTuple CHOOSE_MULTIPLIER(int d, int prec) {
	final int N=32; // size of int.
	Util.assert(1<=d);
	Util.assert(1<=prec && prec<=N);
	// Finds m, sh_post, l such that:
	//     2^(l-1) < d <= 2^l
	//     0 <= sh_post <= l.  If sh_post>0, then N+sh_post <= l+prec
	//     2^(N+sh_post) < m*d <= 2^(N+sh_post)*(1-2^(-prec))
	// Corollary: If d<=2^prec, then
	//     m < 2^(N+sh_post)*(1+2^(1-l))/d <= 2^(N+sh_post-l+1).
	//   Hence m fits in max(prec, N-l)+1 bits (unsigned)
	int l = Util.log2c(d), sh_post = l;
	// m_low = floor((2^(N+l))/d)
	// we compute it as m_low = 2^N + (m_low-2^N) to avoid overflow
	// in the numerator.
	long m_low = (1L<<N) + ((((1L<<l)-d)<<N)/d);
	// m_high = floor((2^(N+l)+2^(N+l-prec))/d)
	// as above, m_high = 2^N + (m_high-2^N) to avoid overflow.
	long m_high= (1L<<N) + 
	    ((((1L<<(l+prec))+(1L<<l)-(((long)d)<<prec))<<(N-prec))/d); 
	Util.assert(m_low < m_high);
	while ((m_low/2) < (m_high/2) && sh_post > 0) {
	    m_low/=2; m_high/=2; sh_post--;
	} /* reduce to lowest terms */
	return new MultiplierTuple(m_high, sh_post, l); // 3 outputs
    }
    
    /**
     * Converts an arbitrary multiplication by a positive constant into a 
     * series of shifts, additions, and multiplies. Based on the m4 macros
     * found in the text <i>Sparc Architecture, Assembly Language, 
     * Programming, & C</i>, by Richard P. Paul.  
     * 
     * This method is used internally by the 
     * <code>AlgebraicSimplification</code> class.  However, this method is
     * public because it could conceivably be of use in other transformations.
     *
     * <b>Requires:</b>  m is a <i>positive</i> 32-bit or 64-bit 
     *                   integer constant
     *
     * @return  an Exp which contains no multiplications, yet 
     *          represents the same value as (n*m). 
     */ 
    public static Exp mul2shift(Exp n, CONST m) { 
	TreeFactory tf    = n.getFactory();

	int  numbits    = n.isDoubleWord() ? 64 : 32;
	int  ones       = 0; 
	long multiplier = m.value.longValue(); 
	Exp  product    = new CONST(tf, n, 0); 

	// copy to temp in case we need to reuse n.
	Temp t = new Temp(tf.tempFactory(), "dm");
	MOVE move = new MOVE(tf, n, new TEMP(tf, n, n.type(), t), n);
	TEMP Tlast = null; // keep the last TEMP generated around.
	int numuses = 0; // count how many times we've referenced TEMP t

	for (int i=0; i<numbits; i++) { 
	    int bitI = (int)((multiplier >> i) & 1); 
	    if (bitI == 0) { 
		if (ones < 3) { // Not enough ones to warrant a booth recoding
		    for (int bit = i-ones; bit<i; bit++) { 
			Tlast = new TEMP(tf, n, n.type(), t); numuses++;
			product = new BINOP
			    (tf, n, n.type(), Bop.ADD, 
			     product, 
			     new BINOP
			     (tf, n, n.type(), Bop.SHL,
			      Tlast,
			      new CONST(tf, n, bit))); 
		    }
		}
		else { // In this case we will see gains from a booth recoding
		    Tlast = new TEMP(tf, n, n.type(), t); numuses++;
		    product = new BINOP
			(tf, n, n.type(), Bop.ADD, 
			 product, 
			 new UNOP
			 (tf, n, n.type(), Uop.NEG, 
			  new BINOP
			  (tf, n, n.type(), Bop.SHL, 
			   Tlast,
			   new CONST(tf, n, i-ones)))); 

		    Tlast = new TEMP(tf, n, n.type(), t); numuses++;
		    product = new BINOP
			(tf, n, n.type(), Bop.ADD,
			 product, 
			 new BINOP
			 (tf, n, n.type(), Bop.SHL, 
			  Tlast,
			  new CONST(tf, n, i))); 
		}
		ones = 0; // Reset the count of ones. 

	    } // if (bitI == 0) { 
	    else { 
		// The current bit is a one.  Increase the ones count. 
		ones++;
	    }
	}
	
	// either chain the MOVE together w/ the product, or replace
	// the only TEMP with n.
	if (numuses==0) return new ESEQ(tf, n, new EXPR(tf, n, n), product);
	if (numuses==1) {
	    Tlast.replace(n); // replace TEMP(t) with n itself.
	    return product; // no need to separate variable.
	}
	return new ESEQ(tf, n, move, product); // move n into t, then compute.
    }
}
