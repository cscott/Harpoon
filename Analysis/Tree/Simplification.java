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
 * <code>Simplification</code> is a general-purpose simplification engine
 * for trees.
 * 
 * <B>Warning:</B> this performs modifications on the tree form in place.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: Simplification.java,v 1.3.2.1 2002-02-27 08:33:37 cananian Exp $
 */
public abstract class Simplification { 
    private static final boolean debug = false;
    // hide constructor
    protected Simplification() { }

    /**
     * Code factory for applying a set of simplification rules to
     * tree form.  Clones the tree before simplifying it in-place.
     */
    public static HCodeFactory codeFactory(final HCodeFactory parent,
					   final List rules) {
	assert parent.getCodeName().equals(CanonicalTreeCode.codename);
	return new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode hc = parent.convert(m);
		if (hc!=null) {
		    harpoon.IR.Tree.Code code = (harpoon.IR.Tree.Code) hc;
		    // clone code...
		    code = (harpoon.IR.Tree.Code) code.clone(m).hcode();
		    DerivationGenerator dg = null;
		    try {
			dg = (DerivationGenerator) code.getTreeDerivation();
		    } catch (ClassCastException ex) { /* i guess not */ }
		    // ...and modify cloned code in-place.
		    simplify((Stm)code.getRootElement(), dg, rules);
		    hc = code;
		}
		return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	};
    }

    // Define new operator constants that can be masked together. 
    protected final static int _CMPLT = (1<<0);
    protected final static int _CMPLE = (1<<1);
    protected final static int _CMPEQ = (1<<2);
    protected final static int _CMPNE = (1<<15);
    protected final static int _CMPGE = (1<<3);
    protected final static int _CMPGT = (1<<4);
    protected final static int _ADD   = (1<<5);
    protected final static int _MUL   = (1<<6);
    protected final static int _DIV   = (1<<7);
    protected final static int _REM   = (1<<8);
    protected final static int _SHL   = (1<<9);
    protected final static int _SHR   = (1<<10);
    protected final static int _USHR  = (1<<11);
    protected final static int _AND   = (1<<12);
    protected final static int _OR    = (1<<13);
    protected final static int _XOR   = (1<<14);

    // Define new TreeKind constants that can be masked together. 
    protected final static int _ALIGN      = (1<<0);
    protected final static int _BINOP      = (1<<1);
    protected final static int _CALL       = (1<<2);
    protected final static int _CJUMP      = (1<<3);
    // _CONST represents all constants including 0 and null. 
    // _CONST0 _CONST1 _CONSTm1 and _CONSTNULL represent specific constants.
    protected final static int _CONST      = (1<<4);
    protected final static int _DATUM      = (1<<5);
    protected final static int _ESEQ       = (1<<6);
    protected final static int _EXPR       = (1<<7);
    protected final static int _JUMP       = (1<<8);
    protected final static int _LABEL      = (1<<9);
    protected final static int _MEM        = (1<<10);
    protected final static int _METHOD     = (1<<11);
    protected final static int _MOVE       = (1<<12);
    protected final static int _NAME       = (1<<13);
    protected final static int _NATIVECALL = (1<<14);
    protected final static int _RETURN     = (1<<15);
    protected final static int _SEGMENT    = (1<<16);
    protected final static int _SEQ        = (1<<17);
    protected final static int _TEMP       = (1<<18);
    protected final static int _THROW      = (1<<19);
    protected final static int _UNOP       = (1<<20);

    // Define more specialized types used to match rules. 
    protected final static int _CONSTNULL  = (1<<21); 
    protected final static int _CONSTm1    = (1<<22); 
    protected final static int _CONST0     = (1<<23); 
    protected final static int _CONST1     = (1<<24); 

    /**
     * Performs simplification on <code>root</code>, using the
     * specified set of simplification rules.  
     * <b>Requires:</b>
     * <OL>
     *   <LI><code>root</code> is a tree.
     *   <LI>Each element of <code>rules</code> is an implementation of the 
     *       <code>Simplification.Rule</code> interface.  
     *   <LI>The supplied set of rules monotonically reduces all possible
     *       <code>Tree</code> objects to some fixed point. 
     * </OL>
     * 
     * @param root  the tree to simplify
     * @param dg    a <code>DerivationGenerator</code> for the tree,
     *              or <code>null</code> to generate no type information.
     * @param rules the set of simplification rules to apply to the elements
     *              of <code>root</code>. 
     */ 
    public static void simplify(Tree root, DerivationGenerator dg, List rules)
    {
	// Shouldn't pass a null ptr. 
	assert root != null && rules != null; 

	// Perform the simplification. 
	while (new SimplificationVisitor(root, dg, rules).changed())
	    /* repeat */ ;
    }

    /**
     * A class to applies simplification rules to elements of a canonical
     * tree form. 
     */ 
    private static class SimplificationVisitor extends TreeVisitor { 
	private final TreeFactory tf;
	private final DerivationGenerator dg;
	private final Rule[] rules; 
	private boolean changed = false;

	public SimplificationVisitor(Tree root, DerivationGenerator dg,
				     List rules) { 
	    this.tf = root.getFactory();
	    this.dg = dg;
	    this.rules = (Rule[]) rules.toArray(new Rule[rules.size()]); 
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

	public void visit(Exp e) { 
	    boolean done = false;
	RESTART: while (!done) {
	    for (int i=0; i<rules.length; i++) {
		Rule rule = rules[i];
		if (rule.match(e)) {
		    Exp simpleE = rule.apply(tf, e, dg); 
		    if (debug)
			System.out.println("Replacing: " + e + " with " +
					   simpleE + " by rule " + rule);
		    e.replace(simpleE);// XXX: simpleE cannot contain e
		    e = simpleE;
		    changed = true;
		    // revisit this.
		    continue RESTART;
		}
	    }
	    done = true; // okay, so this is just a hacked-together 'goto'
	}
	}

	public void visit(Stm s) { 
	    boolean done = false;
	RESTART: while (!done) {
	    for (int i=0; i<rules.length; i++) {
		Rule rule = rules[i];
		if (rule.match(s)) {
		    Stm simpleS = rule.apply(tf, s, dg); 
		    if (debug)
			System.out.println("Replacing: " + s + " with " +
					   simpleS + " by rule " + rule);
		    s.replace(simpleS);// XXX: simpleS cannot contain e
		    s = simpleS;
		    changed = true;
		    // revisit this.
		    continue RESTART;
		}
	    }
	    done = true; // okay, so this is just a hacked-together 'goto'
	}
	}
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    //                                                                 //
    //                       Auxiliary Classes                         //
    //                                                                 //
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

    /**
     * A simplification rule for use with the tree simplifier. 
     */ 
    public static abstract class Rule {
	private final String _name;
	public Rule(String name) { this._name = name; }
	/** Returns a human-readable name for this rule. */
	public String toString() { return _name; }
	/** Returns true if <code>exp</code> matches this rule. */ 
	public boolean match(Exp exp) { return false; }
	/** Applies this rule to <code>exp</code>, and returns the result. 
	 *  <code>apply()</code> should always succeed when
	 *  <code>match()</code> returns <code>true</code>.  Otherwise, 
	 *  the behavior of <code>apply</code> is undefined. 
	 *  The returned <code>Exp</code> cannot contain the parameter
	 *  <code>exp</code>, although it can contain its children.
	 *  (use exp.build(tf, exp.kids()) to workaround this limitation.)
	 */ 
	public Exp apply(TreeFactory tf, Exp exp, DerivationGenerator dg) {
	    throw new Error("unimplemented");
	}

	/** Returns true if <code>stm</code> matches this rule. */ 
	public boolean match(Stm stm) { return false; }
	/** Applies this rule to <code>stm</code>, and returns the result. 
	 *  <code>apply()</code> should always succeed when
	 *  <code>match()</code> returns <code>true</code>.  Otherwise, 
	 *  the behavior of <code>apply</code> is undefined. 
	 *  The returned <code>Stm</code> cannot contain the parameter
	 *  <code>stm</code>, although it can contain its children.
	 *  (use stm.build(tf, stm.kids()) to workaround this limitation.)
	 */ 
	public Stm apply(TreeFactory tf, Stm stm, DerivationGenerator dg) {
	    throw new Error("unimplemented");
	}
    }

    /** Convenience function to test whether any of the bits in
     *  <code>mask</code> are set in <code>val</code>. */
    protected static boolean contains(int val, int mask) {
	return (val & mask) != 0;
    }
    protected static int _KIND(Tree t) { 
	switch (t.kind()) { 
	    case TreeKind.ALIGN:      return _ALIGN; 
	    case TreeKind.BINOP:      return _BINOP;
	    case TreeKind.CALL:       return _CALL;
	    case TreeKind.CJUMP:      return _CJUMP;
	    case TreeKind.CONST:      
		CONST c = (CONST)t; 
		return _CONST |
		    (c.isFloatingPoint() ? 0 :
		     // note that CONSTNULL is not necessarily same as CONST0
		     c.value            == null ? _CONSTNULL : 
		     c.value.longValue() == 0    ? _CONST0 : 
		     c.value.longValue() == 1    ? _CONST1 :
		     c.value.longValue() ==-1    ? _CONSTm1:
		     0);
	    case TreeKind.DATUM:      return _DATUM;
	    case TreeKind.ESEQ:       return _ESEQ;
	    case TreeKind.EXPR:       return _EXPR;
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

    protected static int _OP(int op) { 
	switch (op) { 
	case Bop.CMPLT: return _CMPLT; 
	case Bop.CMPLE: return _CMPLE; 
	case Bop.CMPEQ: return _CMPEQ; 
	case Bop.CMPNE: return _CMPNE; 
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
