// Spec.java, created Wed Feb 17 22:05:18 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Tools.PatMat;

import java.util.BitSet;
import java.util.List;

/**
 * <code>Spec</code> represents the parsed specification.  
 *
 * <BR> <B>NOTE:</B> Current documentation was written late at night
 * and therefore may be misleading; I'm worried that it isn't META
 * enough.  Must go over it and make sure that its clear that I'm
 * talking about the specification for a set of instruction patterns,
 * and <B>NOT</B> the actual code being analyzed and optimized by the
 * compiler.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: Spec.java,v 1.1.2.9 1999-06-28 09:16:54 pnkfelix Exp $
 */
public class Spec  {

    /** Java code statements that are going to be placed outside class
	body (package declaration, import statements).
    */
    public /*final*/ String global_stms;

    /** Java code statements that are going to be placed inside class
	body (helper methods, fields, inner classes).
    */
    public /*final*/ String class_stms;

    /** List of Instruction Patterns for this machine specification.
     */
    public /*final*/ RuleList rules;
    
    /** Creates a <code>Spec</code>. */
    public Spec(String global_stms, String class_stms, RuleList rules) {
        this.global_stms = global_stms;
	this.class_stms = class_stms;
	this.rules = rules;
    }
    public String toString() {
	return global_stms+"\n%%\n"+class_stms+"\n%%\n"+rules;
    }


    // *** inner classes ***

    /** Visitor class for traversing a set of <code>Spec.Rule</code>s
	and performing some action depending on the type of
	<code>Spec.Rule</code> visited.  Subclasses should implement a
	<code>visit</code> method for generic <code>Rule</code>s and
	also override the <code>visit</code> method for subclasses of
	<code>Rule</code> that the subclass cares about.
	@see <U>Design Patterns</U> pgs. 331-344
    */
    public static abstract class RuleVisitor {
	public abstract void visit(Rule r);
	public void visit(RuleExp r) { visit((Rule)r); }
	public void visit(RuleStm r) { visit((Rule)r); }
    }

    /** Abstract immutable representation of an Instruction Pattern.
	Contains a <code>Spec.DetailList</code> and a series of Java
	code statements.
     */
    public static abstract class Rule {

	/** List of the extra details associated with
	    <code>this</code> (speed-cost, size-cost, predicates, etc.).
	*/
	public final DetailList details;

	/** Java code to execute if <code>this</code> fires (ie. this
	    pattern is chosen as part of the optimal set).
	*/
	public final String action_str;
	
	/** Constructs a new <code>Spec.Rule</code>.
	    <BR> <B>requires:</B> 
	         <code>action_str</code> is a series of valid Java
		 code statements. 
	    <BR> <B>effects:</B> 
	         constructs a new <code>Spec.Rule</code> object with
		 associated <code>Spec.DetailList</code> and action to
		 perform if <code>this</code> fires.
	    @param details List of extra details associated with
	                   <code>this</code> (speed-cost, size-cost,
			   predicates, etc.).
	    @param action_str Series of Java code statements that
	                      represent the action to perform if
			      <code>this</code> fires.
	*/
	public Rule(DetailList details, String action_str) {
	    this.details = details; this.action_str = action_str;
	}

	/** Applies <code>v</code>'s <code>visit</code> method to
	    <code>this</code>.  This is effectively a gludge to
	    emulate <B>multiple dispatch</B>.  Must be reimplemented
	    by all subclasses of <code>Spec.Rule</code>.
	    <BR> <B>effects:</B> Calls <code>v.visit(this)</code>. 
	    @see <U>Design Patterns</U> pgs. 331-344
	*/
	public void accept(RuleVisitor v) { v.visit(this); }

	public String toString() {
	    String s = " %{" + action_str + "}%";
	    if (details==null) return s;
	    else return details.toString()+s;
	}
    }

    /** Extension of <code>Spec.Rule</code> that also contains a
	<code>Spec.Exp</code> to match <code>Tree</code> expressions
	and the identifier for the result that <code>this</code>
	produces.  
	
	<code>Spec.RuleExp</code>s match (sub)expressions in the code,
	which is why it is necessary to associate a
	<code>result_id</code>: expression matching is context
	sensitive, depending on what the outerlying expression or
	statement is expecting the nested expression to return.

    */
    public static class RuleExp extends Rule {

	/** Expression <code>this</code> matches. */
	public final Exp exp;

	/** Identifier for return value of expression that
	    <code>this</code> matches. */ 
	public final String result_id;
	
	/** Constructs a new <code>Spec.RuleExp</code>.
	    <BR> <B>requires:</B> 
	         <code>action_str</code> is a series of valid Java
		 code statements.
	    <BR> <B>effects:</B> 
	        constructs a new <code>Spec.RuleExp</code> object with
		associated <code>Spec.Exp</code>,
		<code>result_id</code>, <code>Spec.DetailList</code>,
		and action to perform if <code>this</code> fires.
	    @param exp <code>Spec.Exp</code> associated with
	               <code>this</code>.
	    @param result_id Tag identifying type of result that
	                     <code>this</code> produces.
	    @param details List of extra details associated with
	                   <code>this</code> (speed-cost, size-cost,
			   predicates, etc.). 
	    @param action_str Series of Java code statements that
	                      represent the action to perform if
			      <code>this</code> fires.
	*/
	public RuleExp(Exp exp, String result_id,
		       DetailList details, String action_str) {
	    super(details, action_str);
	    this.exp = exp; this.result_id = result_id;
	}
	public void accept(RuleVisitor v) { v.visit(this); }
	public String toString() {
	    return exp + "=" + result_id +" " + super.toString();
	}
    }
    
    /** Extension of <code>Spec.Rule</code> that also contains a
	<code>Spec.Stm</code> to match <code>Tree</code> statements. 
    */
    public static class RuleStm extends Rule {

	/** Statement associated with <code>this</code>. */
	public final Stm stm;

	/** Constructs a new <code>Spec.RuleStm</code>.
	    <BR> <B>requires:</B> 
	         <code>action_str</code> is a series of valid Java
		 code statements. 
	    <BR> <B>effects:</B> 
	         constructs a new <code>Spec.RuleStm</code> object
		 with associated <code>Spec.Stm</code>,
		 <code>Spec.DetailList</code>, and action to perform
		 if <code>this</code> fires.
	    @param stm <code>Spec.Stm</code> associated with
    	               <code>this</code>. 
	    @param details List of extra details associated with
	                   <code>this</code> (speed-cost, size-cost,
			   predicates, etc.). 
	    @param action_str Series of Java code statements that
	                      represent the action to perform if
			      <code>this</code> fires.
	*/
	public RuleStm(Stm stm, DetailList details, String action_str) {
	    super(details, action_str);
	    this.stm = stm;
	}
	public void accept(RuleVisitor v) { v.visit(this); }
	public String toString() {
	    return stm + " " + super.toString();
	}
    }

    /** Visitor class for traversing a set of <code>Spec.Exp</code>s
	and performing some action depending on the type of
	<code>Spec.Exp</code> visited.  Subclasses should implement a
	<code>visit</code> method for generic <code>Exp</code>s and
	also override the <code>visit</code> method for subclasses of
	<code>Exp</code> that the subclass cares about.
	@see <U>Design Patterns</U> pgs. 331-344
    */
    public static abstract class ExpVisitor {
	public abstract void visit(Exp e);
	public void visit(ExpBinop e) { visit((Exp)e); }
	public void visit(ExpConst e) { visit((Exp)e); }
	public void visit(ExpId e) { visit((Exp)e); }
	public void visit(ExpMem e) { visit((Exp)e); }
	public void visit(ExpName e) { visit((Exp)e); }
	public void visit(ExpTemp e) { visit((Exp)e); }
	public void visit(ExpUnop e) { visit((Exp)e); }
    }

    /** Abstract immutable representation of an Expression in an
	Instruction Pattern. 
	@see IR.Tree.Exp
    */
    public static abstract class Exp { 
	
	/** Applies <code>v</code>'s <code>visit</code> method to
	    <code>this</code>.  This is effectively a gludge to
	    emulate <B>multiple dispatch</B>.  Must be reimplemented
	    by all subclasses of <code>Spec.Exp</code>.
	    <BR> <B>effects:</B> Calls <code>v.visit(this)</code>. 
	    @see <U>Design Patterns</U> pgs. 331-344
	*/
	public void accept(ExpVisitor v) { v.visit(this); }
	
    }
    
    /** Extension of <code>Spec.Exp</code> that represents an
	Identifier in the code.  Essentially a wrapper around a
	<code>String</code>.
    */
    public static class ExpId extends Exp {
	/** Identifier that <code>this</code> represents. */
	public final String id;

	/** Constructs a <code>Spec.ExpId</code> around
	    <code>id</code>. 
	    @param id The Identifier that <code>this</code>
	              represents. 
	*/
	public ExpId(String id) { this.id = id; }
	public void accept(ExpVisitor v) { v.visit(this); }
	public String toString() { return id; }
    }

    /** Extension of <code>Spec.Exp</code> that represents a Binary
	Operation in the code.  An implicit restriction on our binary
	operations is that both operands must have equivalent domains
	(there is only one <code>Spec.TypeSet</code> for a given
	<code>Spec.ExpBinop</code>).
	@see IR.Tree.BINOP
    */
    public static class ExpBinop extends Exp {
	
	/** Types of values that <code>this</code> operates on. */
	public final TypeSet types;
	
	/** Opcode for <code>this</code>.
	    @see IR.Tree.Bop
	*/
	public final Leaf opcode;
	
	/** Expression on the left side of <code>this</code>. */
	public final Exp left;

	/** Expression on the right side of <code>this</code>. */
	public final Exp right;

	/** Constructs a <code>Spec.ExpBinop</code> that operates on
	    expressions of <code>types</code>.
	    @param types Types that <code>left</code> and
	                 <code>right</code> may be. 
	    @param opcode The binary operation that is being performed
	                  on <code>left</code> and <code>right</code>.
	    @param left The left operand.
	    @param right The right operand.
	*/
	public ExpBinop(TypeSet types, Leaf opcode, Exp left, Exp right) {
	    this.types = types; this.opcode = opcode;
	    this.left = left;   this.right = right;
	}
	public void accept(ExpVisitor v) { v.visit(this); }
	public String toString() {
	    return "BINOP"+types+"("+opcode.toBop().toUpperCase()+","
		+left+","+right+")";
	}
    }

    /** Extension of <code>Spec.Exp</code> that represents a Constant
	value in the code.
	@see IR.Tree.CONST
    */
    public static class ExpConst extends Exp {

	/** The set of types that <code>value</code> may take. */
	public final TypeSet types;

	/** The constant value associated with <code>this</code>. */
	public final Leaf value;

	/** Constructs a <code>Spec.ExpConst</code>. 
	    @param types Types that <code>value</code> may be.
	    @param value The constant value.
	 */
	public ExpConst(TypeSet types, Leaf value) {
	    this.types = types; this.value = value;
	}
	public void accept(ExpVisitor v) { v.visit(this); }
	public String toString() { return "CONST"+types+"("+value+")"; }
    }

    /** Extension of <code>Spec.Exp</code> that represents a Memory
	Access in the code (could be either Load or Store;
	Context-Sensitive).
	@see IR.Tree.MEM
    */
    public static class ExpMem extends Exp {

	/** The set of types that the values at <code>addr</code> may be. */ 
	public final TypeSet types;

	/** The expression that computes the address of memory
	    represented by <code>this</code>.
	*/
	public final Exp addr;

	/** Constructs a <code>Spec.ExpMem</code>.
	    @param types Types that the value at <code>addr</code> may be.
	    @param addr Address of memory for <code>this</code>.
	*/
	public ExpMem(TypeSet types, Exp addr) {
	    this.types = types; this.addr = addr;
	}
	public void accept(ExpVisitor v) { v.visit(this); }
	public String toString() { return "MEM"+types+"("+addr+")"; }
    }

    /** Extension of <code>Spec.Exp</code> that represents a symbolic
	constant.  Usually used to represent an Assembly Language
	Label in the data segment. 
	@see IR.Tree.NAME
    */
    public static class ExpName extends Exp {

	/** Name for <code>this</code>. */
	public final String name;

	/** Constructs a new <code>Spec.ExpName</code> representing
	    <code>name</code>. */
	public ExpName(String name) { this.name = name; }
	public String toString() { return "NAME("+name+")"; }
    }

    /** Extension of <code>Spec.Exp</code> that represents a Temporary
	value in the code.
	@see IR.Tree.TEMP
    */
    public static class ExpTemp extends Exp {

	/** The set of types that <code>this</code> may take. */
	public final TypeSet types;

	/** Identifier for <code>this</code>. */
	public final String name;

	/** Constructs a <code>Spec.ExpTemp</code>.
	    @param types Types that <code>this</code> may be.
	    @param name Identifier for <code>this</code>.
	*/
	public ExpTemp(TypeSet types, String name) {
	    this.types = types; this.name = name;
	}
	public void accept(ExpVisitor v) { v.visit(this); }
	public String toString() { return "TEMP"+types+"("+name+")"; }
    }

    /** Extension of <code>Spec.Exp</code> that represents a Unary
	operation. 
	@see IR.Tree.UNOP
    */
    public static class ExpUnop extends Exp {

	/** Set of Types that <code>this</code> operates on. (The
	    types returned are part of the <code>opcode</code>).
	*/
	public final TypeSet types;

	/** Opcode for <code>this</code>.
	    @see IR.Tree.Uop
	*/
	public final Leaf opcode;
	public final Exp exp;
	public ExpUnop(TypeSet types, Leaf opcode, Exp exp) {
	    this.types = types; this.opcode = opcode; this.exp = exp;
	}
	public void accept(ExpVisitor v) { v.visit(this); }
	public String toString() {
	    return "UNOP"+types+"("+opcode.toUop().toUpperCase()+","+exp+")";
	}
    }

    /** Visitor class for traversing a set of <code>Spec.Stm</code>s
	and performing some action depending on the type of
	<code>Spec.Stm</code> visited.  Subclasses should implement a
	<code>visit</code> method for generic <code>Stm</code>s and
	also override the <code>visit</code> method for subclasses of
	<code>Stm</code> that the subclass cares about.
	@see <U>Design Patterns</U> pgs. 331-344
    */
    public static abstract class StmVisitor {
	public abstract void visit(Stm s);
	public void visit(StmCall s) { visit((Stm)s); }
	public void visit(StmCjump s) { visit((Stm)s); }
	public void visit(StmExp s) { visit((Stm)s); }
	public void visit(StmJump s) { visit((Stm)s); }
	public void visit(StmLabel s) { visit((Stm)s); }
	public void visit(StmMove s) { visit((Stm)s); }
	public void visit(StmNativeCall s) { visit((Stm)s); }
	public void visit(StmReturn s) { visit((Stm)s); }
	public void visit(StmSeq s) { visit((Stm)s); }
	public void visit(StmThrow s) { visit((Stm)s); }
    }

    /** Abstract immutable representation of a Statement in an
	Instruction Pattern.
	@see IR.Tree.Stm
    */
    public static abstract class Stm { 

	/** Applies <code>v</code>'s <code>visit</code> method to
	    <code>this</code>.  This is effectively a gludge to
	    emulate <B>multiple dispatch</B>.  Must be reimplemented
	    by all subclasses of <code>Spec.Stm</code>.
	    <BR> <B>effects:</B> Calls <code>v.visit(this)</code>. 
	    @see <U>Design Patterns</U> pgs. 331-344
	*/
	public void accept(StmVisitor v) { v.visit(this); }
    }

    /** Extension of <code>Spec.Stm</code> that represents a call to a
	procedure. 
	@see IR.Tree.CALL
    */
    public static class StmCall extends Stm {
	/** Return value destination expression. */
	public final Exp retval;
	/** Exception value destination expression. */
	public final Exp retex;
	/** Function location expression. */
	public final Exp func;
	/** Arguments being passed to procedure. */
	public final String arglist;
	/** Constructs a new <code>Spec.StmCall</code>.
	    @param retval Return value destination expression.
	    @param retex Exception value destination expression. 
	    @param func Function location expression.
	    @param arglist Arguments.
	*/
	public StmCall(Exp retval, Exp retex, Exp func, String arglist) {
	    this.retval = retval; this.retex = retex; this.func = func;
	    this.arglist = arglist;
	}
	public void accept(StmVisitor v) { v.visit(this); }
	public String toString() {
	    return "CALL("+retval+","+retex+","+func+","+arglist+")";
	}
    }

    /** Extension of <code>Spec.Stm</code> representing a condition
	branch. 
	@see IR.Tree.CJUMP
    */
    public static class StmCjump extends Stm {
	/** Boolean expression that decides which direction we're
	    jumping. */
	public final Exp test;
	/** Label to branch to on a True value. */
	public final String t_label;
	/** Label to branch to on a False value. */
	public final String f_label;
	/** Constructs a new <code>Spec.StmCjump</code>.
	    @param test Text expression.
	    @param t_label True Label.
	    @param f_label False Label.
	*/
	public StmCjump(Exp test, String t_label, String f_label) {
	    this.test = test; this.t_label = t_label; this.f_label = f_label;
	}
	public void accept(StmVisitor v) { v.visit(this); }
	public String toString() {
	    return "CJUMP("+test+","+t_label+","+f_label+")";
	}
    }

    /** Extension of <code>Spec.Stm</code> representing an expression
	which is evaluated for its side effects (i.e. we throw away
	the return value).
	@see IR.Tree.EXP
    */
    public static class StmExp extends Stm {
	/** Expression for <code>this</code>. */
	public final Exp exp;
	/** Constructs a new <code>Spec.StmExp</code>.
	    @param exp Expression to be evaluated.
	*/
	public StmExp(Exp exp) { this.exp = exp; }
	public void accept(StmVisitor v) { v.visit(this); }
	public String toString() { return "EXP("+exp+")"; }
    }
    
    /** Extension of <code>Spec.Stm</code> representing an
	unconditional branch.
	@see IR.Tree.JUMP
    */
    public static class StmJump extends Stm {
	/** Expression which yields the target of this jump. */
	public final Exp exp;
	/** Constructs a new <code>Spec.StmJump</code>.
	    @param exp Jump target.
	*/
	public StmJump(Exp exp) { this.exp = exp; }
	public void accept(StmVisitor v) { v.visit(this); }
	public String toString() { return "JUMP("+exp+")"; }
    }
    /** Extension of <code>Spec.Stm</code> representing a label which
	is the target of a branch or call.
	@see IR.Tree.LABEL
    */
    public static class StmLabel extends Stm {
	/** Label. */
	public final String name;
	/** Constructs a new <code>Spec.StmLabel</code>.
	    @param name Label.
	*/
	public StmLabel(String name) { this.name = name; }
	public void accept(StmVisitor v) { v.visit(this); }
	public String toString() { return "LABEL("+name+")"; }
    }
    /** Extension of <code>Spec.Stm</code> representing an expression
	which moves a value from one place to another.
	@see IR.Tree.MOVE
    */
    public static class StmMove extends Stm {
	/** Expression yielding the destination of this move. */
	public final Exp dst;
	/** Expression yielding the source data of this move. */
	public final Exp src;
	/** Constructs a new <code>Spec.StmMove</code>.
	    @param dst Destination expression.
	    @param src Source expression.
	*/
	public StmMove(Exp dst, Exp src) { this.dst = dst; this.src = src; }
	public void accept(StmVisitor v) { v.visit(this); }
	public String toString() { return "MOVE("+dst+","+src+")"; }
    }
    /** Extension of <code>Spec.Stm</code> representing an expression
	which is evaluated for its side effects (i.e. we throw away
	the return value).
	@see IR.Tree.NATIVECALL
    */
    public static class StmNativeCall extends Stm {
	/** Return value destination expression. */
	public final Exp retval;
	/** Exception destination expression. */
	public final Exp retex;
	/** Function location expression. */
	public final Exp func;
	/** Arguments being passed to procedure. */
	public final String arglist;
	/** Constructs a new <code>Spec.StmNativeCall</code>.
	    @param retval Return value destination expression.
	    @param retex Exception value destination expression.
	    @param func Function location expression.
	    @param arglist Arguments.
	*/
	public StmNativeCall(Exp retval, Exp retex, Exp func, String arglist) {
	    this.retval = retval; this.retex = retex; this.func = func;
	    this.arglist = arglist;
	}
	public void accept(StmVisitor v) { v.visit(this); }
	public String toString() {
	    return "NATIVECALL("+retval+","+retex+","+func+","+arglist+")";
	}
    }
    /** Extension of <code>Spec.Stm</code> representing an expression
	which is evaluated for its side effects (i.e. we throw away
	the return value).
	@see IR.Tree.RETURN
    */
    public static class StmReturn extends Stm {
	/** The set of Types that <code>retval</code> may be. */
	public final TypeSet types;
	/** Return value expression. */
	public final Exp retval;
	/** Constructs a new <code>Spec.StmReturn</code>.
	    @param types Types that <code>retval</code> may be.
	    @param retval Return value. 
	*/
	public StmReturn(TypeSet types, Exp retval) {
	    this.types = types; this.retval = retval;
	}
	public void accept(StmVisitor v) { v.visit(this); }
	public String toString() { return "RETURN"+types+"("+retval+")"; }
    }
    /** Extension of <code>Spec.Stm</code> representing a sequence of
	statements to be executed in order.
	@see IR.Tree.SEQ
    */
    public static class StmSeq extends Stm {
	/** First statement to execute. */
	public final Stm s1;
	/** Second statement to execute. */
	public final Stm s2;
	/** Constructs a new <code>Spec.StmSeq</code>. 
	    @param s1 First statement
	    @param s2 Second statement
	*/
	public StmSeq(Stm s1, Stm s2) { this.s1 = s1; this.s2 = s2; }
	public void accept(StmVisitor v) { v.visit(this); }
	public String toString() { return "SEQ("+s1+","+s2+")"; }
    }

    /** Extension of <code>Spec.Stm</code> representing a operation to
	throw an exception.
	@see IR.Tree.THROW
    */
    public static class StmThrow extends Stm {
	/** The exceptional value. */
	public final Exp exp;
	/** Constructs a new <code>Spec.StmThrow</code>.
	    @param exp The exceptional value expression.
	*/
	public StmThrow(Exp exp) { this.exp = exp; }
	public void accept(StmVisitor v) { v.visit(this); }
	public String toString() { return "THROW("+exp+")"; }
    }

    /** Abstract representation of leaves in the instruction pattern. */
    public static abstract class Leaf {
	public String toBop() { return this.toString(); }
	public String toUop() { return this.toString(); }
    }
    /** Extension of <code>Spec.Leaf</code> which represents an
	Identifier. */
    public static class LeafId extends Leaf {
	/** Identifier string. */
	public final String id;
	/** Constructs a new <code>Spec.LeafId</code>.
	    @param id Identifier string.
	*/
	public LeafId(String id) {
	    this.id = id;
	}
	public String toString() { return id; }
    }
    /** Extension of <code>Spec.Leaf</code> representing an opcode
     *  for <code>Uop</code> and/or <code>Bop</code>.  */
    public static class LeafOp extends Leaf {
	/* Enumerated opcode. */
	public final int op;
	/** Constructs a new <code>Spec.LeafOp</code>.
	    @param op Enumerated opcode.
	*/
	public LeafOp(int op) {
	    this.op = op;
	}
	public String toString() { return Integer.toString(op); }
	public String toBop() { return harpoon.IR.Tree.Bop.toString(op); }
	public String toUop() { return harpoon.IR.Tree.Uop.toString(op); }
    }
    /** Extension of <code>Spec.Leaf</code> which represents a
	explicit number in the specification.
    */
    public static class LeafNumber extends Leaf {
	/** Number. */
	public final Number number;
	/** Constructs a new <code>Spec.LeafNumber</code>.
	    @param number Number.
	*/
	public LeafNumber(Number number) {
	    this.number = number;
	}
	public String toString() { return number.toString(); }
    }


    /** Visitor class for traversing a set of <code>Spec.Detail</code>s
	and performing some action depending on the type of
	<code>Spec.Detail</code> visited.  Subclasses should implement a
	<code>visit</code> method for generic <code>Detail</code>s and
	also override the <code>visit</code> method for subclasses of
	<code>Detail</code> that the subclass cares about.
	@see <U>Design Patterns</U> pgs. 331-344
    */
    public static abstract class DetailVisitor {
	public abstract void visit(Detail d);
	public void visit(DetailExtra d) { visit((Detail)d); }
	public void visit(DetailPredicate d) { visit((Detail)d); }
	public void visit(DetailWeight d) { visit((Detail)d); }
    }

    /** A detail is an abstract representation for a piece of data
	about the Instruction Pattern or <code>Rule</code>.  Details
	include predicates, speed-costs, size-costs...
    */
    public static abstract class Detail { 

	/** Applies <code>v</code>'s <code>visit</code> method to
	    <code>this</code>.  This is effectively a gludge to
	    emulate <B>multiple dispatch</B>.  Must be reimplemented
	    by all subclasses of <code>Spec.Detail</code>.
	    <BR> <B>effects:</B> Calls <code>v.visit(this)</code>. 
	    @see <U>Design Patterns</U> pgs. 331-344
	*/
	public void accept(DetailVisitor v) { v.visit(this); }
    }

    /** Extension of <code>Spec.Detail</code> that requests an extra
     *  temporary register for the use of the action clause.  For example,
     *  if multiplying two 32-bit registers generates a 64-bit result on
     *  the target architecture, you might need to request an extra
     *  <code>Temp</code> to have a place to put the high word of the
     *  result (before you throw it away). The <code>DetailExtra</code>
     *  element contains a list of identifiers naming the working temps
     *  that you are requesting. */
    public static class DetailExtra extends Detail {
	public final IdList extras;
	public DetailExtra(IdList extras) {
	    this.extras = extras;
	}
	public void accept(DetailVisitor v) { v.visit(this); }
	public String toString() {
	    return "%extra{"+((extras==null)?"":extras.toString())+"}";
	}
    }
    /** Extension of <code>Spec.Detail</code> that stores a
	<code>predicate_string</code> which is a piece of Java code
	that decides if a particular <code>Spec.Rule</code> can be
	applied. 
    */
    public static class DetailPredicate extends Detail {
	public final String predicate_string;
	/** Constructs a new <code>Spec.DetailPredicate</code>. 
	    <BR> <B>requires:</B> <code> predicate_string is a valid
	         Java expression that will evaluate to a boolean
		 value.  
	    @param predicate_string Predicate to check for
	           applicability of <code>Spec.Rule</code>
	*/
	public DetailPredicate(String predicate_string) {
	    this.predicate_string = predicate_string;
	}
	public void accept(DetailVisitor v) { v.visit(this); }
	public String toString() { return "%pred %("+predicate_string+")%"; }
    }

    /** Extension of <code>Spec.Detail</code> that stores a 
	(name,weight) pair.  This weight can be used by the
	Instruction Generator to choose one pattern over another if
	it is attempting to optimize for the property 
	(speed, size, etc) given in <code>name</code>.
    */
    public static class DetailWeight extends Detail {
	/** Describes what metric <code>value</code> is measuring. */
	public final String name;
	/** The weight associated with <code>name</code>. */
	public final double value;
	/** Constructs a new <code>Spec.DetailWeight</code>.
	    @param name Metric that <code>value</code> is measuring.
	    @param value Weight associated with <code>name</code>.
	*/
	public DetailWeight(String name, double value) {
	    this.name = name; this.value = value;
	}
	public void accept(DetailVisitor v) { v.visit(this); }
	public String toString() { return "%weight<"+name+","+value+">"; }
    }

    /** A representation for storing Types that values can be. 
	@see IR.Tree.Type
     */
    public static class TypeSet {
	final BitSet bs = new BitSet();
	/** Constructs a new <code>Spec.TypeSet</code>. */
	public TypeSet() { }
	
	/** Checks if <code>this</code> contains <code>type</code>.
	    <BR> <B>effects:</B> Returns true if <code>type</code> has
	         been turned on by a call to <code>set(type)</code> or
		 <code>setAll()</code>.  Else returns false.
	*/
	public boolean contains(int type) {
	    return bs.get(type);
	}
	
	/** Records that <code>this</code> contains
	    <code>type</code>.  
	*/
	public void set(int type) {
	    bs.set(type);
	}
	
	/** Records that <code>this</code> contains all five Types,
	    { INT, LONG, FLOAT, DOUBLE, POINTER }
	*/
	public void setAll() { 
	    set(harpoon.IR.Tree.Type.INT);
	    set(harpoon.IR.Tree.Type.LONG);
	    set(harpoon.IR.Tree.Type.FLOAT);
	    set(harpoon.IR.Tree.Type.DOUBLE);
	    set(harpoon.IR.Tree.Type.POINTER);
	}
	public String toString() {
	    StringBuffer sb = new StringBuffer();
	    if (contains(harpoon.IR.Tree.Type.INT))     sb.append(",i");
	    if (contains(harpoon.IR.Tree.Type.LONG))    sb.append(",l");
	    if (contains(harpoon.IR.Tree.Type.FLOAT))   sb.append(",f");
	    if (contains(harpoon.IR.Tree.Type.DOUBLE))  sb.append(",d");
	    if (contains(harpoon.IR.Tree.Type.POINTER)) sb.append(",a");
	    if (sb.length()>0) sb.setCharAt(0, '<'); else sb.append('<');
	    sb.append('>');
	    return sb.toString();
	}
    }

    /** Linked list representation for representing the series of
	<code>Spec.Rule</code>s in this <code>Spec</code>.  
    */
    public static class RuleList {
	public final Rule head;
	public final RuleList tail;
	public RuleList(Rule head, RuleList tail) {
	    this.head = head; this.tail = tail;
	}
	public String toString() {
	    if (tail==null) return head.toString();
	    else return head.toString() + "\n" + tail.toString();
	}
    }

    /** Linked list representation for representing the series of
	<code>Spec.Detail</code>s in this <code>Spec</code>.  
    */
    public static class DetailList {
	public final Detail head;
	public final DetailList tail;
	public DetailList(Detail head, DetailList tail) {
	    this.head = head; this.tail = tail;
	}
	public String toString() {
	    if (tail==null) return head.toString();
	    else return head.toString() + " " + tail.toString();
	}
    }

    /** Linked list representation for representing a series of
	Identifier <code>String</code>s in this <code>Spec</code>. 
    */
    public static class IdList {
	public final String head;
	public final IdList tail;
	public IdList(String head, IdList tail) {
	    this.head = head; this.tail = tail;
	}
	public String toString() {
	    if (tail==null) return head;
	    else return head + "," + tail.toString();
	}
    }
}
