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
import harpoon.IR.Tree.ESEQ; 
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.SEQ; 
import harpoon.IR.Tree.Stm; 
import harpoon.IR.Tree.Tree; 
import harpoon.IR.Tree.TreeFactory; 
import harpoon.IR.Tree.TreeKind; 
import harpoon.IR.Tree.TreeVisitor; 
import harpoon.IR.Tree.Type; 
import harpoon.IR.Tree.UNOP; 
import harpoon.IR.Tree.Uop; 
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
 * @version $Id: AlgebraicSimplification.java,v 1.1.2.14 2000-02-15 01:01:46 cananian Exp $
 */
// XXX missing -K1 --> K2  and ~K1 --> K2 rules.
public abstract class AlgebraicSimplification { 
    private static final boolean debug = false;
    // hide constructor
    private AlgebraicSimplification() { }

    // Define new operator constants that can be masked together. 
    private final static int _CMPLT = (1<<0);
    private final static int _CMPLE = (1<<1);
    private final static int _CMPEQ = (1<<2);
    private final static int _CMPGE = (1<<3);
    private final static int _CMPGT = (1<<4);
    private final static int _ADD   = (1<<5);
    private final static int _MUL   = (1<<6);
    private final static int _DIV   = (1<<7);
    private final static int _REM   = (1<<8);
    private final static int _SHL   = (1<<9);
    private final static int _SHR   = (1<<10);
    private final static int _USHR  = (1<<11);
    private final static int _AND   = (1<<12);
    private final static int _OR    = (1<<13);
    private final static int _XOR   = (1<<14);

    // Define new TreeKind constants that can be masked together. 
    private final static int _ALIGN      = (1<<0);
    private final static int _BINOP      = (1<<1);
    private final static int _CALL       = (1<<2);
    private final static int _CJUMP      = (1<<3);
    // _CONST represents all constants including 0 and null. 
    // _CONST0 _CONST1 _CONSTm1 and _CONSTNULL represent specific constants.
    private final static int _CONST      = (1<<4);
    private final static int _DATUM      = (1<<5);
    private final static int _ESEQ       = (1<<6);
    private final static int _EXP        = (1<<7);
    private final static int _JUMP       = (1<<8);
    private final static int _LABEL      = (1<<9);
    private final static int _MEM        = (1<<10);
    private final static int _METHOD     = (1<<11);
    private final static int _MOVE       = (1<<12);
    private final static int _NAME       = (1<<13);
    private final static int _NATIVECALL = (1<<14);
    private final static int _RETURN     = (1<<15);
    private final static int _SEGMENT    = (1<<16);
    private final static int _SEQ        = (1<<17);
    private final static int _TEMP       = (1<<18);
    private final static int _THROW      = (1<<19);
    private final static int _UNOP       = (1<<20);

    // Define more specialized types used to match rules. 
    private final static int _CONSTNULL  = (1<<21); 
    private final static int _CONSTm1    = (1<<22); 
    private final static int _CONST0     = (1<<23); 
    private final static int _CONST1     = (1<<24); 

    private final static List _DEFAULT_RULES = new ArrayList(); 
    /** Default alegraic simplification rules. */
    public final static List DEFAULT_RULES = // protect the rules list.
	Collections.unmodifiableList(_DEFAULT_RULES);

    /**
     * Code factory for applying a set of simplification rules to
     * tree form.  Clones the tree before simplifying it in-place.
     */
    public static HCodeFactory codeFactory(final HCodeFactory parent,
					   final List rules) {
	Util.assert(parent.getCodeName().equals(CanonicalTreeCode.codename));
	return new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode hc = parent.convert(m);
		if (hc!=null) {
		    harpoon.IR.Tree.Code code = (harpoon.IR.Tree.Code) hc;
		    // um, i should really clone code here. FIXME
		    simplify((Stm)code.getRootElement(), rules);
		}
		return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	};
    }
    /** Code factory for applying the default set of simplifications to
     *  the given tree form.  Clones the tree before simplifying it
     *  in-place. */
    public static HCodeFactory codeFactory(final HCodeFactory parent) {
	return codeFactory(parent, DEFAULT_RULES);
    }

    /**
     * Performs algebraic simplification on <code>root</code>.
     * <b>Requires:</b>
     *   <code>root</code> is a tree in canonical form. 
     * 
     * @param root  the tree to simplify
     */ 
    public static void simplify(Stm root) { simplify(root, DEFAULT_RULES); }

    /**
     * Performs algebraic simplification on <code>root</code>, using the
     * specified set of simplification rules.  
     * <b>Requires:</b>
     * <OL>
     *   <LI><code>root</code> is a tree in canonical form.
     *   <LI>Each element of <code>rules</code> is an implementation of the 
     *       <code>AlgebraicSimplification.Rule</code> interface.  
     *   <LI>The supplied set of rules monotonically reduces all possible
     *       <code>Exp</code> objects to some fixed point. 
     * </OL>
     * 
     * @param root  the tree to simplify
     * @param rules the set of simplification rules to apply to the elements
     *              of <code>root</code>. 
     */ 
    public static void simplify(Stm root, List rules) { 
	// Shouldn't pass a null ptr. 
	Util.assert(root != null); 

	// Perform the simplification. 
	while (new SimplificationVisitor(root, rules).changed())
	    /* repeat */ ;
    }

    /**
     * A class to applies simplification rules to elements of a canonical
     * tree form. 
     */ 
    private static class SimplificationVisitor extends TreeVisitor { 
	private final List  rules; 
	private boolean changed = false;

	public SimplificationVisitor(Stm root, List rules) { 
	    this.rules = rules; 
	    // evaluate each subtree from leaves up to root.
	    postorder(root);
	}
	void postorder(Tree t) {
	    // post-order traversal: visit all children first.
	    for (Tree tp = t.getFirstChild(); tp!=null; ) {
		Tree one = tp;
		tp=tp.getSibling();// advance *before* we (possibly) unlink tp!
		postorder(one);
	    }
	    // now visit this.
	    t.accept(this);
	}
	public boolean changed() { return changed; }

	public void visit(Tree t) { 
	    throw new Error("No defaults here."); 
	}

	public void visit(ESEQ e) { 
	    // Although this tree form is canonical, the simplification visitor
	    // could reduce expressions to ESEQs.  For now, this feature is
	    // neither needed, nor supported. 
	    //
	    throw new Error("Not implemented."); 
	}

	public void visit(Exp e) { 
	RESTART: do {
	    for (Iterator i = this.rules.iterator(); i.hasNext();) { 
		Rule rule = (Rule)i.next();
		if (rule.match(e)) {
		    Exp simpleE = rule.apply(e); 
		    if (debug)
			System.out.println("Replacing: " + e + " with " +
					   simpleE + " by rule " + rule);
		    e.replace(simpleE);
		    e = simpleE;
		    changed = true;
		    // revisit this.
		    continue RESTART;
		}
	    }
	} while (false); // okay, so this is just a hacked-together 'goto'
	}

	public void visit(Stm s) { 
	    /* do nothing.  At the moment, at least. */
	}
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
	Rule combineConstants = new Rule() { 
	    public String toString() { return "combineConstants"; }
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
	    
	    public Exp apply(Exp e) { 
		TreeFactory tf = e.getFactory(); 
		BINOP b  = (BINOP)e; 
		CONST k1 = (CONST)b.getLeft();
		CONST k2 = (CONST)b.getRight(); 

		Object k1pk2 = harpoon.IR.Tree.BINOP.evalValue
		    (tf, b.op, b.optype, k1.value, k2.value);


		switch (b.type()) { 
		    case Type.INT: 
		        return new CONST(tf,b,((Integer)k1pk2).intValue());
		    case Type.LONG: 
		        return new CONST(tf,b,((Long)k1pk2).longValue());
		    case Type.POINTER: 
		        if (tf.getFrame().pointersAreLong()) { 
			    return new CONST(tf,b,((Long)k1pk2).longValue());
			}
		        else {
		            return new CONST(tf,b,((Integer)k1pk2).intValue());
		        }
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
	//
	Rule commute = new Rule() { 
	    public String toString() { return "commute"; }
	    public boolean match(Exp e) { 
		if (_KIND(e) != _BINOP) { return false; } 
		else { 
		    BINOP b = (BINOP)e; 
		    return 
		    contains(_OP(b.op), _ADD|_MUL|_AND|_OR|_XOR|_CMPEQ) && 
		    contains(_KIND(b.getLeft()), _CONST) &&
		    !contains(_KIND(b.getRight()), _CONST) &&
		    !b.isFloatingPoint();
		}
	    }
	    public Exp apply(Exp e) { 
		BINOP b = (BINOP)e; 
		TreeFactory tf = b.getFactory(); 
		return new BINOP(tf, b, b.optype, b.op, b.getRight(), b.getLeft()); 
 	    }
	};


	// (exp + const) + const --> exp + (const + const)
	// (exp * const) * const --> exp * (const * const)
	// (exp & const) & const --> exp & (const & const)
	// (exp | const) | const --> exp | (const | const)
	// (exp ^ const) ^ const --> exp ^ (const ^ const)
	//      note that == is not associative.
	Rule associate = new Rule() {
	    public String toString() { return "associate"; }
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
	    public Exp apply(Exp e) { 
		BINOP b1 = (BINOP) e;
		BINOP b2 = (BINOP) b1.getLeft();
		TreeFactory tf = b1.getFactory(); 
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
	Rule makeZero = new Rule() {
	    // NOTE: this rule is dangerous if tree is not canonical.
	    public String toString() { return "makeZero"; }
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
	    public Exp apply(Exp e) { 
		BINOP b = (BINOP)e; 
		TreeFactory tf = b.getFactory(); 
		if (b.type()==Type.INT)
		    return new CONST(tf, e, (int) 0);
		if (b.type()==Type.LONG)
		    return new CONST(tf, e, (long) 0);
		throw new Error("ack");
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
	Rule removeZero = new Rule() { 
	    public String toString() { return "removeZero"; }
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
	    public Exp apply(Exp e) { 
		BINOP b = (BINOP)e; 
		return b.getLeft(); 
	    }
	};
	
	
	// x ^ -1 --> ~ x
	//
	Rule createNot = new Rule() {
	    // note that since Qop doesn't have NOT, we have to recreate it.
	    public String toString() { return "createNot"; }
	    public boolean match(Exp e) { 
		if (_KIND(e) != _BINOP) return false;
		BINOP b = (BINOP) e;
		if (b.op != Bop.XOR ) return false;
		return contains(_KIND(b.getRight()), _CONSTm1);
	    } 
	    public Exp apply(Exp e) {  
		BINOP b = (BINOP) e;
		Util.assert(b.op == Bop.XOR);
		TreeFactory tf = b.getFactory(); 
		return new UNOP(tf, e, b.optype, Uop.NOT, b.getLeft());
	    } 
	};


	// -(-i) --> i 
	// ~(~i) --> i
	// 
	Rule doubleNegative = new Rule() { 
	    public String toString() { return "doubleNegative"; }
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
	    public Exp apply(Exp e) {  
		UNOP u1 = (UNOP)e;  
		UNOP u2 = (UNOP)u1.getOperand();  
		Util.assert(u1.op == u2.op);  
		return u2.getOperand();  
	    } 
	}; 

	// -0 --> 0 
	//
	Rule negZero = new Rule() {  
	    public String toString() { return "negZero"; }
	    public boolean match(Exp e) { 
		if (_KIND(e) != _UNOP) { return false; } 
		else { 
		    UNOP u = (UNOP)e; 
		    if (u.isFloatingPoint()) return false;
		    return contains(_KIND(u.getOperand()), _CONST0);
		}
	    }
	    public Exp apply(Exp e) { 
		UNOP u = (UNOP)e; 
		Util.assert(contains(_KIND(u.getOperand()), _CONST0));
		return u.getOperand(); 
	    } 
	}; 

	// exp * const --> (recoded as shifts)
	Rule mulToShift = new Rule() { 
	    // NOTE: this rule is dangerous if tree is not canonical.
	    public String toString() { return "mulToShift"; }
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
	    public Exp apply(Exp e) { 
		BINOP b = (BINOP)e; 
		return mul2shift(b.getLeft(), (CONST)b.getRight()); 
	    }
	}; 


	// exp / const --> (recoded as multiplication)
	Rule divToMul = new Rule() { 
	    // NOTE: this rule is dangerous if tree is not canonical.
	    public String toString() { return "divToMul"; }
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
	    public Exp apply(Exp e) { 
		BINOP b = (BINOP)e; 
		Util.assert(b.op == Bop.DIV); 
		return div2mul(b.getLeft(), (CONST)b.getRight()); 
	    }
	};  

	// Add rules to the rule set.  
	// 
	_DEFAULT_RULES.add(combineConstants); 
	//_DEFAULT_RULES.add(makeZero); //dangerous.
	_DEFAULT_RULES.add(removeZero); 
	_DEFAULT_RULES.add(commute); 
	_DEFAULT_RULES.add(associate); 
	_DEFAULT_RULES.add(createNot);
	_DEFAULT_RULES.add(doubleNegative); 
	_DEFAULT_RULES.add(negZero); 
	_DEFAULT_RULES.add(mulToShift); 
	_DEFAULT_RULES.add(divToMul); 
    }
		  
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    //                                                                 //
    //                       Auxiliary Classes                         //
    //                                                                 //
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

    /**
     * A simplification rule for use with the tree algebraic simplifier. 
     */ 
    public static interface Rule {
	/** Returns true if <code>exp</code> matches this rule. */ 
	public boolean match(Exp exp); 
	/** Applies this rule to <code>exp</code>, and returns the result. 
	 *  <code>apply()</code> should always succeed when
	 *  <code>match()</code> returns <code>true</code>.  Otherwise, 
	 *  the behavior of <code>apply</code> is undefined. 
	 */ 
	public Exp apply(Exp exp);  
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
     */ 
    public static Exp div2mul(Exp n, CONST d) { 
	Util.assert(d.type == Type.INT); 

	// Initialize parameters for the transformation
	int  dVal    = d.value.intValue(); 
	int  dAbs    = Math.abs(dVal); 
	int  l       = Math.max((int)Math.ceil(Math.log(dAbs)/Math.log(2)),1); 
	long m       = 1 + ((1L << (32 + l - 1)) / dAbs); 
	int  m_prime = (int)(m - 0x100000000L);
	int  d_sign  = (dVal < 0) ? -1 : 0; 
	int  sh_post = l - 1; 

	// Get a TreeFactory to use in creating new tree objects
	TreeFactory tf = n.getFactory(); 

	// If d is negative, flip the sign of n
	if (dVal < 0) { 
	    n = new UNOP(tf, n, Type.INT, Uop.NEG, n); 
	}

	if (dVal == 0) { // Don't process div-by-0
	    return new BINOP(tf,n,Type.INT,Bop.DIV,n,new CONST(tf, n, dVal));
	}
	else if (dAbs == 1) { // Dividing by 1
	    return n; 
	}
	else if (dAbs == (1 << l)) {  // Dividing by a power of 2 
	    return new BINOP(tf, n, Type.INT, Bop.SHL, n, new CONST(tf, n, l)); 
	}
	else { 
	    BINOP q0 = new BINOP
		(tf, n, Type.INT, Bop.ADD, 
		 n.build(n.kids()), // Clone n
		 new UNOP
		 (tf, n, Type.INT, Uop._2I, 
		  new BINOP
		  (tf, n, Type.LONG, Bop.USHR, 
		   new BINOP
		   (tf, n, Type.LONG, Bop.MUL,
		    new CONST(tf, n, m_prime), 
		    n.build(n.kids())),
		   new CONST(tf, n, 32)))); 
	    BINOP q1 = new BINOP
		(tf, n, Type.INT, Bop.ADD,  // Really a SUB.  
		 new BINOP
		 (tf, n, Type.INT, Bop.SHR, 
		  q0, 
		  new CONST(tf, n, sh_post)), 
		 new UNOP
		 (tf, n, Type.INT, Uop.NEG, 
		  new BINOP // XSIGN(n) 
		  (tf, n, Type.INT, Bop.SHL, n.build(n.kids()), new CONST(tf, n, 31))));
	    return new BINOP
		(tf, n, Type.INT, Bop.ADD, 
		 (new BINOP
		  (tf, n, Type.INT, Bop.XOR, q1, new CONST(tf, n, d_sign))),
		 new UNOP
		 (tf, n, Type.INT, Uop.NEG, new CONST(tf, n, d_sign))); 
	}
	
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

	// FIXME: 
	// 
	// 1) if n is a complex expression, this is inefficient.  May
	//    need to copy it to a TEMP first and return an ESEQ. 
	// 
	for (int i=0; i<numbits; i++) { 
	    int bitI = (int)((multiplier >> i) & 1); 
	    if (bitI == 0) { 
		if (ones < 3) { // Not enough ones to warrant a booth recoding
		    for (int bit = i-ones; bit<i; bit++) { 
			product = new BINOP
			    (tf, n, n.type(), Bop.ADD, 
			     product, 
			     new BINOP
			     (tf, n, n.type(), Bop.SHL,
			      n.build(n.kids()), 
			      new CONST(tf, n, bit))); 
		    }
		}
		else { // In this case we will see gains from a booth recoding
		    product = new BINOP
			(tf, n, n.type(), Bop.ADD, 
			 product, 
			 new UNOP
			 (tf, n, n.type(), Uop.NEG, 
			  new BINOP
			  (tf, n, n.type(), Bop.SHL, 
			   n.build(n.kids()), 
			   new CONST(tf, n, i-ones)))); 

		    product = new BINOP
			(tf, n, n.type(), Bop.ADD,
			 product, 
			 new BINOP
			 (tf, n, n.type(), Bop.SHL, 
			  n.build(n.kids()), 
			  new CONST(tf, n, i))); 
		}
		ones = 0; // Reset the count of ones. 

	    } // if (bitI == 0) { 
	    else { 
		// The current bit is a one.  Increase the ones count. 
		ones++;
	    }
	}
	
	return product; 
    }

    private static boolean contains(int val, int mask) {
	return (val & mask) != 0;
    }
    private static int _KIND(Tree t) { 
	switch (t.kind()) { 
	    case TreeKind.ALIGN:      return _ALIGN; 
	    case TreeKind.BINOP:      return _BINOP;
	    case TreeKind.CALL:       return _CALL;
	    case TreeKind.CJUMP:      return _CJUMP;
	    case TreeKind.CONST:      
		CONST c = (CONST)t; 
		return _CONST |
		    (c.value            == null ? _CONSTNULL : 
		     c.value.intValue() == 0    ? _CONST0 : 
		     c.value.intValue() == 1    ? _CONST1 :
		     c.value.intValue() ==-1    ? _CONSTm1:
		     0);
	    case TreeKind.DATUM:      return _DATUM;
	    case TreeKind.ESEQ:       return _ESEQ;
	    case TreeKind.EXP:        return _EXP;
	    case TreeKind.JUMP:       return _JUMP;
	    case TreeKind.LABEL:      return _LABEL;
	    case TreeKind.MEM:        return _MEM;
	    case TreeKind.METHOD:     return _METHOD;
	    case TreeKind.MOVE:       return _MOVE;
	    case TreeKind.NAME:       return _NAME;
	    case TreeKind.NATIVECALL: return _NATIVECALL;
	    case TreeKind.RETURN:     return _RETURN;
	    case TreeKind.SEGMENT:    return _SEGMENT;
	    case TreeKind.SEQ:        return _SEQ;
	    case TreeKind.TEMP:       return _TEMP;
	    case TreeKind.THROW:      return _THROW;
	    case TreeKind.UNOP:       return _UNOP; 
	default: throw new Error("Unrecognized type: " + t.kind()); 
	}
    }

    private static int _OP(int op) { 
	switch (op) { 
	case Bop.CMPLT: return _CMPLT; 
	case Bop.CMPLE: return _CMPLE; 
	case Bop.CMPEQ: return _CMPEQ; 
	case Bop.CMPGE: return _CMPGE;
	case Bop.CMPGT: return _CMPGT; 
	case Bop.ADD:   return _ADD; 
	case Bop.MUL:   return _MUL;
	case Bop.DIV:   return _DIV; 
	case Bop.REM:   return _REM;   
	case Bop.SHL:   return _SHL;   
	case Bop.SHR:   return _SHR;   
	case Bop.USHR:  return _USHR;  
	case Bop.AND:   return _AND;   
	case Bop.OR:    return _OR;    
	case Bop.XOR:   return _XOR;
	default: throw new Error("Unrecognized op: " + op); 
	}
    }
}





