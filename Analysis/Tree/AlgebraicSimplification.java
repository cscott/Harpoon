package harpoon.Analysis.Tree; 

import harpoon.IR.Tree.BINOP; 
import harpoon.IR.Tree.Bop; 
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
 * @version $Id: AlgebraicSimplification.java,v 1.1.2.3 1999-12-20 09:28:53 duncan Exp $
 */
public abstract class AlgebraicSimplification { 
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
    // _CONST represents all constants _except_ 0 and null. 
    // Use _CONST0 and _CONSTNULL respectively.  
    private final static int _CONST      = (1<<4);
    private final static int _DATA       = (1<<5);
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
    private final static int _CONST0     = (1<<21); 
    private final static int _CONSTNULL  = (1<<22); 

    private static List DEFAULT_RULES = new ArrayList(); 

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
	// The tree must be in canonical form. 
	Code code = ((Code.TreeFactory)root.getFactory()).getParent(); 
	Util.assert(code.getName().equals("canonical-tree")); 

	// Perform the simpliciation. 
	SimplificationVisitor sv = new SimplificationVisitor(root, rules); 
    }

    /**
     * A class to applies simplification rules to elements of a canonical
     * tree form. 
     */ 
    private static class SimplificationVisitor extends TreeVisitor { 
	/*final*/ private Stack worklist = new Stack(); 
	/*final*/ private TreeStructure ts; 
	/*final*/ private List rules; 

	public SimplificationVisitor(Stm root, List rules) { 
	    this.ts = new TreeStructure(root); 
	    this.rules = rules; 
	    this.worklist.push(root); 
	    
	    while (!worklist.isEmpty()) { 
		((Tree)this.worklist.pop()).accept(this); 
	    }
	}

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
	    for (Iterator i = this.rules.iterator(); i.hasNext();) { 
		Rule rule = (Rule)i.next();
		if (rule.match(e)) {
		    Exp simpleE = rule.apply(e); 
		    this.ts.replace(e, simpleE); 
		    this.worklist.push(simpleE); 
		    return; 
		}
	    }
	    
	    // No matches.  Examine the children of this Exp. 
	    for (ExpList el = e.kids(); el != null; el = el.tail) { 
		this.worklist.push(el.head); 
	    }
	}

	public void visit(Stm s) { 
	    for (ExpList el = s.kids(); el != null; el = el.tail) { 
		this.worklist.push(el.head); 
	    }
	}

	public void visit(SEQ s) { 
	    this.worklist.push(s.left); 
	    this.worklist.push(s.right); 
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
	    public boolean match(Exp e) { 
		if (_KIND(e) != _BINOP) { return false; } 
		else { 
		    BINOP b = (BINOP)e; 
		    return 
		        ((_OP(b.op) & _ADD|_MUL|_SHL|_SHR|_USHR|_AND|_OR|_XOR) != 0) &&
		        ((_KIND(b.left) & (_CONST|_CONST0)) != 0) &&
		    
		        ((_KIND(b.right) & (_CONST|_CONST0)) != 0) &&
		        (!b.isFloatingPoint());
		}
	    }
	    
	    public Exp apply(Exp e) { 
		TreeFactory tf = e.getFactory(); 
		BINOP b  = (BINOP)e; 
		CONST k1 = (CONST)b.left;
		CONST k2 = (CONST)b.right; 

		Object k1pk2 = harpoon.IR.Tree.BINOP.evalValue
		    (tf, b.op, b.optype, k1.value, k2.value);


		switch (b.optype) { 
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
		        throw new Error("Invalid optype: " + b.optype);
		}
	    }
	};  

	// const + exp --> exp + const
	// const * exp --> exp * const
	// const & exp --> exp & const
	// const | exp --> exp | const
	// const ^ exp --> exp ^ const
	//
	Rule commute = new Rule() { 
	    public boolean match(Exp e) { 
		if (_KIND(e) != _BINOP) { return false; } 
		else { 
		    BINOP b = (BINOP)e; 
		    return 
		        ((_OP(b.op) & (_ADD|_MUL|_AND|_OR|_XOR)) != 0) &&
		        ((_KIND(b.left) & (_CONST|_CONST0)) != 0) &&
		        ((_KIND(b.right) & ~(_CONST|_CONST0|_CONSTNULL)) != 0) &&
		        (!b.isFloatingPoint());
		}
	    }
	    public Exp apply(Exp e) { 
		BINOP b = (BINOP)e; 
		TreeFactory tf = b.getFactory(); 
		return new BINOP(tf, b, b.optype, b.op, b.right, b.left); 
 	    }
	};


	// exp + 0   --> exp, 
	// exp << 0  --> exp, 
	// exp >> 0  --> exp,
	// exp >>> 0 --> exp
	// 
	Rule removeZero = new Rule() { 
	    public boolean match(Exp e) { 
		if (_KIND(e) != _BINOP) { return false; } 
		else { 
		    BINOP b = (BINOP)e; 
		    return 
		        ((_OP(b.op) & _ADD|_SHL|_SHR|_USHR) != 0) &&
		        ((_KIND(b.left) & _BINOP|_MEM|_NAME|_TEMP|_UNOP)!=0) &&
		        ((_KIND(b.right) & _CONST0) != 0);
		}
	    }
	    public Exp apply(Exp e) { 
		BINOP b = (BINOP)e; 
		return b.left; 
	    }
	};
	
	
	// -(-i) --> i 
	// 
	Rule doubleNegative = new Rule() { 
	    public boolean match(Exp e) { 
		if (_KIND(e) != _UNOP) { return false; } 
		else { 
		    UNOP u1 = (UNOP)e; 
		    if (_KIND(u1.operand) != _UNOP) { return false; } 
		    else { 
			UNOP u2 = (UNOP)u1.operand; 
			return u1.op == Uop.NEG && u2.op == Uop.NEG; 
		    }
		}
	    } 
	    public Exp apply(Exp e) {  
		UNOP u1 = (UNOP)e;  
		UNOP u2 = (UNOP)u1.operand;  
		Util.assert(u1.optype == Uop.NEG && u2.optype == Uop.NEG);  
		return u2.operand;  
	    } 
	}; 

	// -0 --> 0 
	//
	Rule negZero = new Rule() {  
	    public boolean match(Exp e) { 
		if (_KIND(e) != _UNOP) { return false; } 
		else { 
		    UNOP u = (UNOP)e; 
		    return _KIND(u.operand) == _CONST0; 
		}
	    }
	    public Exp apply(Exp e) { 
		UNOP u = (UNOP)e; 
		Util.assert(_KIND(u.operand) == _CONST0); 
		return u.operand; 
	    } 
	}; 

	Rule mulToShift = new Rule() { 
	    public boolean match (Exp e) { 
		if (_KIND(e) != _BINOP) { return false; } 
		else { 
		    BINOP b = (BINOP)e; 
		    if (_KIND(b.right) != _CONST) { return false; } 
		    else { 
			CONST c = (CONST)b.right; 
			return 
			    c.value.longValue() > 0                     &&
		            (_KIND(b.left) & (_BINOP|_UNOP|_TEMP)) != 0 &&
		            b.op == Bop.MUL                             &&
		            !b.isFloatingPoint(); 
		    }
		}
	    }
	    public Exp apply(Exp e) { 
		BINOP b = (BINOP)e; 
		return mul2shift(b.left, (CONST)b.right); 
	    }
	}; 


	Rule divToMul = new Rule() { 
	    public boolean match(Exp e) { 
		if (_KIND(e) != _BINOP) { return false; } 
		else { 
		    BINOP b = (BINOP)e; 
		    return b.op == Bop.DIV && _KIND(b.right) == _CONST;
		}
	    }
	    public Exp apply(Exp e) { 
		BINOP b = (BINOP)e; 
		Util.assert(b.op == Bop.DIV); 
		return div2mul(b.left, (CONST)b.right); 
	    }
	};  

	// Add rules to the rule set.  
	// 
	DEFAULT_RULES.add(combineConstants); 
	DEFAULT_RULES.add(removeZero); 
	DEFAULT_RULES.add(commute); 
	DEFAULT_RULES.add(doubleNegative); 
	DEFAULT_RULES.add(negZero); 
	DEFAULT_RULES.add(mulToShift); 
	DEFAULT_RULES.add(divToMul); 
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

    private static int _KIND(Tree t) { 
	switch (t.kind()) { 
	    case TreeKind.ALIGN:      return _ALIGN; 
	    case TreeKind.BINOP:      return _BINOP;
	    case TreeKind.CALL:       return _CALL;
	    case TreeKind.CJUMP:      return _CJUMP;
	    case TreeKind.CONST:      
		CONST c = (CONST)t; 
		return
		    c.value            == null ? _CONSTNULL : 
		    c.value.intValue() == 0    ? _CONST0 : 
		    _CONST; 
	    case TreeKind.DATA:       return _DATA;
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





