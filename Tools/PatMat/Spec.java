// Spec.java, created Wed Feb 17 22:05:18 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Tools.PatMat;

import harpoon.Util.BitString;
import harpoon.Util.Util;
import harpoon.IR.Tree.Type;

import java.util.List;

/**
 * <code>Spec</code> represents the parsed specification of a set of
 * Instruction Patterns for a target backend .  
 *
 * <BR> <B>NOTE:</B> Current documentation was written late at night
 * and therefore may be misleading; I'm worried that it isn't META
 * enough.  Must go over it and make sure that its clear that I'm
 * talking about the specification for a set of instruction patterns,
 * and <B>NOT</B> the actual IR for the code being analyzed and optimized by the
 * compiler.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: Spec.java,v 1.3.2.1 2002-02-27 08:37:38 cananian Exp $
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

    /** Java code statements to be inserted in the prologue of the
     *  code generator method body.
     */
    public /*final*/ String method_prologue_stms;

    /** Java code statements to be inserted in the epilogue of the
     *  code generator method body.
     */
    public /*final*/ String method_epilogue_stms;

    /** List of Instruction Patterns for this machine specification.
	<code>null</code> is a legal value.
     */
    public /*final*/ RuleList rules;
    
    /** Creates a <code>Spec</code>. */
    public Spec(String global_stms, String class_stms,
		String method_prologue_stms, String method_epilogue_stms,
		RuleList rules) {
        this.global_stms = global_stms;
	this.class_stms = class_stms;
	this.method_prologue_stms = method_prologue_stms;
	this.method_epilogue_stms = method_epilogue_stms;
	this.rules = rules;
    }
    public String toString() {
	return global_stms+"\n%%\n"+class_stms+"\n%%\n"+
	    ((method_prologue_stms==null)?"":
	     ("%start with %{"+method_prologue_stms+"}%\n"))+
	    ((method_epilogue_stms==null)?"":
	     ("%end with %{"+method_epilogue_stms+"}%\n"))+
	    rules;
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
	/** Visits elements of <code>l</code>.
	    If (l!=null) visits l.head then l.tail.  Else does nothing.
	*/
	public void visit(RuleList l) {
	    if(l!=null){l.head.accept(this); visit(l.tail);}
	}
    }

    /** Abstract immutable representation of an Instruction Pattern.
	Contains a <code>Spec.DetailList</code> and a series of Java
	code statements.
     */
    public static abstract class Rule {

	/** List of the extra details associated with
	    <code>this</code> (speed-cost, size-cost, predicates, etc.).
	    <code>null</code> is a legal value.
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
	public abstract void accept(RuleVisitor v);

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
	<code>visit</code> method for generic <code>Spec.Exp</code>s and
	also override the <code>visit</code> method for subclasses of
	<code>Spec.Exp</code> that the subclass cares about.
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
	@see harpoon.IR.Tree.Exp
    */
    public static abstract class Exp { 
	
	/** Applies <code>v</code>'s <code>visit</code> method to
	    <code>this</code>.  This is effectively a gludge to
	    emulate <B>multiple dispatch</B>.  Must be reimplemented
	    by all subclasses of <code>Spec.Exp</code>.
	    <BR> <B>effects:</B> Calls <code>v.visit(this)</code>. 
	    @see <U>Design Patterns</U> pgs. 331-344
	*/
	public abstract void accept(ExpVisitor v);
	
	/** Creates a new <code>Spec.Exp</code> similar to this one,
	 *  using the provided <code>Spec.ExpList</code> of children. */
	public abstract Exp build(ExpList kids);
	/** Creates an <code>Spec.ExpList</code> of children of this
	 *  <code>Spec.Exp</code>. */
	public abstract ExpList kids();
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
	public Exp build(ExpList kids) {
	    assert kids==null;
	    return new ExpId(this.id);
	}
	public ExpList kids() { return null; }
	public String toString() { return id; }
    }

    /** Extension of <code>Spec.Exp</code> that represents a Binary
	Operation in the code.  An implicit restriction on our binary
	operations is that both operands must have equivalent domains
	(there is only one <code>Spec.TypeSet</code> for a given
	<code>Spec.ExpBinop</code>).
	@see harpoon.IR.Tree.BINOP
    */
    public static class ExpBinop extends Exp {
	
	/** Types of values that <code>this</code> operates on. */
	public final TypeSet types;
	
	/** Opcode for <code>this</code>.
	    @see harpoon.IR.Tree.Bop
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
	    assert !types.containsSmall() : ("BINOP cannot be precisely typed: "+this);
	}
	public void accept(ExpVisitor v) { v.visit(this); }
	public Exp build(ExpList kids) {
	    assert kids!=null && kids.tail!=null && kids.tail.tail==null;
	    return new ExpBinop((TypeSet)this.types.clone(),
				opcode/*immutable*/,
				kids.head, kids.tail.head);
	}
	public ExpList kids() {
	    return new ExpList(left, new ExpList(right, null));
	}
	public String toString() {
	    return "BINOP"+types+"("+opcode.toBop()+","
		+left+","+right+")";
	}
    }

    /** Extension of <code>Spec.Exp</code> that represents a Constant
	value in the code.
	@see harpoon.IR.Tree.CONST
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
	public Exp build(ExpList kids) {
	    assert kids==null;
	    return new ExpConst((TypeSet)types.clone(),value/*immutable*/);
	}
	public ExpList kids() { return null; }
	public String toString() { return "CONST"+types+"("+value+")"; }
    }

    /** Extension of <code>Spec.Exp</code> that represents a Memory
	Access in the code (could be either Load or Store;
	Context-Sensitive).
	@see harpoon.IR.Tree.MEM
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
	public Exp build(ExpList kids) {
	    assert kids!=null && kids.tail==null;
	    return new ExpMem((TypeSet)types.clone(), kids.head);
	}
	public ExpList kids() { return new ExpList(addr, null); }
	public String toString() { return "MEM"+types+"("+addr+")"; }
    }

    /** Extension of <code>Spec.Exp</code> that represents a symbolic
	constant.  Usually used to represent an Assembly Language
	Label in the data segment. 
	@see harpoon.IR.Tree.NAME
    */
    public static class ExpName extends Exp {

	/** Name for <code>this</code>. 
	    @see harpoon.Temp.Label;
	 */
	public final String name;

	/** Constructs a new <code>Spec.ExpName</code> representing
	    <code>name</code>. */
	public ExpName(String name) { this.name = name; }
	public void accept(ExpVisitor v) { v.visit(this); }
	public Exp build(ExpList kids) {
	    assert kids==null;
	    return new ExpName(name);
	}
	public ExpList kids() { return null; }
	public String toString() { return "NAME("+name+")"; }
    }

    /** Extension of <code>Spec.Exp</code> that represents a Temporary
	value in the code.
	@see harpoon.IR.Tree.TEMP
    */
    public static class ExpTemp extends Exp {

	/** The set of types that <code>this</code> may take. */
	public final TypeSet types;

	/** Identifier for <code>this</code>. 
	    @see harpoon.Temp.Temp
	 */
	public final String name;

	/** Constructs a <code>Spec.ExpTemp</code>.
	    @param types Types that <code>this</code> may be.
	    @param name Identifier for <code>this</code>.
	*/
	public ExpTemp(TypeSet types, String name) {
	    this.types = types; this.name = name;
	    assert !types.containsSmall() : ("TEMP cannot be precisely typed: "+this);
	}
	public void accept(ExpVisitor v) { v.visit(this); }
	public Exp build(ExpList kids) {
	    assert kids==null;
	    return new ExpTemp((TypeSet)types.clone(), name);
	}
	public ExpList kids() { return null; }
	public String toString() { return "TEMP"+types+"("+name+")"; }
    }

    /** Extension of <code>Spec.Exp</code> that represents a Unary
	operation. 
	@see harpoon.IR.Tree.UNOP
    */
    public static class ExpUnop extends Exp {

	/** Set of Types that <code>this</code> operates on. (The
	    types returned are part of the <code>opcode</code>).
	*/
	public final TypeSet types;

	/** Opcode for <code>this</code>.
	    @see harpoon.IR.Tree.Uop
	*/
	public final Leaf opcode;
	public final Exp exp;
	public ExpUnop(TypeSet types, Leaf opcode, Exp exp) {
	    this.types = types; this.opcode = opcode; this.exp = exp;
	    assert !types.containsSmall() : ("UNOP cannot be precisely typed: "+this);
	}
	public void accept(ExpVisitor v) { v.visit(this); }
	public Exp build(ExpList kids) {
	    assert kids!=null && kids.tail==null;
	    return new ExpUnop((TypeSet)types.clone(), opcode/*immutable*/,
			       kids.head);
	}
	public ExpList kids() { return new ExpList(exp, null); }
	public String toString() {
	    return "UNOP"+types+"("+opcode.toUop()+","+exp+")";
	}
    }

    /** Visitor class for traversing a set of <code>Spec.Stm</code>s
	and performing some action depending on the type of
	<code>Spec.Stm</code> visited.  Subclasses should implement a
	<code>visit</code> method for generic <code>Spec.Stm</code>s and
	also override the <code>visit</code> method for subclasses of
	<code>Spec.Stm</code> that the subclass cares about.
	@see <U>Design Patterns</U> pgs. 331-344
    */
    public static abstract class StmVisitor {
	public abstract void visit(Stm s);
	public void visit(StmAlign s) { visit((Stm)s); }
	public void visit(StmCall s) { visit((Stm)s); }
	public void visit(StmCjump s) { visit((Stm)s); }
	public void visit(StmData s) { visit((Stm)s); }
	public void visit(StmExp s) { visit((Stm)s); }
	public void visit(StmJump s) { visit((Stm)s); }
	public void visit(StmLabel s) { visit((Stm)s); }
	public void visit(StmMethod s) { visit((Stm)s); }
	public void visit(StmMove s) { visit((Stm)s); }
	public void visit(StmNativeCall s) { visit((Stm)s); }
	public void visit(StmReturn s) { visit((Stm)s); }
	public void visit(StmSegment s) { visit((Stm)s); }
	public void visit(StmSeq s) { visit((Stm)s); }
	public void visit(StmThrow s) { visit((Stm)s); }
    }

    /** Abstract immutable representation of a Statement in an
	Instruction Pattern.
	@see harpoon.IR.Tree.Stm
    */
    public static abstract class Stm { 

	/** Applies <code>v</code>'s <code>visit</code> method to
	    <code>this</code>.  This is effectively a gludge to
	    emulate <B>multiple dispatch</B>.  Must be reimplemented
	    by all subclasses of <code>Spec.Stm</code>.
	    <BR> <B>effects:</B> Calls <code>v.visit(this)</code>. 
	    @see <U>Design Patterns</U> pgs. 331-344
	*/
	public abstract void accept(StmVisitor v);

	/** Creates a new <code>Spec.Stm</code> similar to this one,
	 *  using the provided <code>Spec.ExpList</code> of children. */
	public abstract Stm build(ExpList kids);
	/** Creates an <code>Spec.ExpList</code> of children of this
	 *  <code>Spec.Stm</code>. */
	public abstract ExpList kids();

	/** Checks if this <code>Stm</code> object is valid for Data
	    patterns. 
	    Most patterns are for code generation, not data tables.
	    Specific subclasses of <code>Stm</code> that wish to be
	    matched when generating data tables should override this
	    method to return true.
	*/
	public boolean canBeRootOfData() { return false; }

    }

    /** Extension of <code>Spec.Stm</code> representing an alignment
     *  request.
     *  @see harpoon.IR.Tree.ALIGN
     */
    public static class StmAlign extends Stm {
	/** Type of segment. */
	public final Leaf alignment;
	/** Constructs a new <code>Spec.StmSegment</code>.
	 *  @param segtype Segment type.
	 */
	public StmAlign(Leaf alignment) {
	    assert alignment instanceof LeafId ||
			(alignment instanceof LeafNumber &&
			 ((LeafNumber)alignment).number instanceof Integer) : "Only integer alignments make sense for ALIGN";
	    this.alignment = alignment;
	}
	public void accept(StmVisitor v) { v.visit(this); }
	public Stm build(ExpList kids) {
	    assert kids==null;
	    return new StmAlign(alignment/*immutable*/);
	}
	public ExpList kids() { return null; }
	public String toString() { return "ALIGN("+alignment+")"; }
	public boolean canBeRootOfData() { return true; }
    }

    /** Extension of <code>Spec.Stm</code> that represents a call to a
	procedure. 
	@see harpoon.IR.Tree.CALL
    */
    public static class StmCall extends Stm {
	/** Return value destination expression. 
	    @see harpoon.IR.Tree.TEMP
	 */
	public final String retval;
	/** Exception value destination expression. 
	    @see harpoon.IR.Tree.TEMP
	 */
	public final String retex;
	/** Function location expression. 
	    @see harpoon.IR.Tree.Exp
	 */
	public final Exp func;
	/** Arguments being passed to procedure. 
	    @see harpoon.IR.Tree.TempList
	 */
	public final String arglist;
	/** Exception handler label.
	 *  @see harpoon.IR.Tree.NAME
	 */
	public final String handler;
	/** Constructs a new <code>Spec.StmCall</code>.
	    @param retval Return value destination expression.
	    @param retex Exception value destination expression. 
	    @param func Function location expression.
	    @param arglist Arguments.
	    @param handler Exception handler location.
	*/
	public StmCall(String retval, String retex, Exp func, String arglist,
		       String handler) {
	    this.retval = retval; this.retex = retex; this.func = func;
	    this.arglist = arglist; this.handler = handler;
	}
	public void accept(StmVisitor v) { v.visit(this); }
	public Stm build(ExpList kids) {
	    assert kids!=null && kids.tail==null;
	    return new StmCall(retval, retex, kids.head, arglist, handler);
	}
	public ExpList kids() { return new ExpList(func, null); }
	public String toString() {
	    return "CALL("+retval+","+retex+","+func+","+arglist+","+
		           handler+")";
	}
    }

    /** Extension of <code>Spec.Stm</code> representing a conditional
	branch. 
	@see harpoon.IR.Tree.CJUMP
    */
    public static class StmCjump extends Stm {
	/** Boolean expression that decides which direction we're
	    jumping. */
	public final Exp test;
	/** Label to branch to on a True value. 
	    @see harpoon.Temp.Label
	 */
	public final String t_label;
	/** Label to branch to on a False value. 
	    @see harpoon.Temp.Label
	 */
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
	public Stm build(ExpList kids) {
	    assert kids!=null && kids.tail==null;
	    return new StmCjump(kids.head, t_label, f_label);
	}
	public ExpList kids() { return new ExpList(test, null); }
	public String toString() {
	    return "CJUMP("+test+","+t_label+","+f_label+")";
	}
    }

    /** Extension of <code>Spec.Stm</code> representing a raw datum in
	memory.
	@see harpoon.IR.Tree.DATUM
    */
    public static class StmData extends Stm {
	/** A <code>IR.Tree.CONST</code> or <code>IR.Tree.NAME</code>
	    specifying the value with which to initialize this location.
	*/
	public final Exp data;
	/** Constructs a new <code>Spec.StmData</code>.
	    @param data Value expression.
	*/
	public StmData(Exp data) {
	    this.data = data;
	}
	public void accept(StmVisitor v) { v.visit(this); }
	public Stm build(ExpList kids) {
	    assert kids!=null && kids.tail==null;
	    return new StmData(kids.head);
	}
	public ExpList kids() { return new ExpList(data, null); }
	public String toString() {
	    return "DATUM("+data+")";
	}
	public boolean canBeRootOfData() { return true; }
    }

    /** Extension of <code>Spec.Stm</code> representing an expression
	which is evaluated for its side effects (i.e. we throw away
	the return value).
	@see harpoon.IR.Tree.EXPR
    */
    public static class StmExp extends Stm {
	/** Expression for <code>this</code>. */
	public final Exp exp;
	/** Constructs a new <code>Spec.StmExp</code>.
	    @param exp Expression to be evaluated.
	*/
	public StmExp(Exp exp) { this.exp = exp; }
	public void accept(StmVisitor v) { v.visit(this); }
	public Stm build(ExpList kids) {
	    assert kids!=null && kids.tail==null;
	    return new StmExp(kids.head);
	}
	public ExpList kids() { return new ExpList(exp, null); }
	public String toString() { return "EXPR("+exp+")"; }
    }
    
    /** Extension of <code>Spec.Stm</code> representing an
	unconditional branch.
	@see harpoon.IR.Tree.JUMP
    */
    public static class StmJump extends Stm {
	/** Expression which yields the target of this jump. */
	public final Exp exp;
	/** Constructs a new <code>Spec.StmJump</code>.
	    @param exp Jump target.
	*/
	public StmJump(Exp exp) { this.exp = exp; }
	public void accept(StmVisitor v) { v.visit(this); }
	public Stm build(ExpList kids) {
	    assert kids!=null && kids.tail==null;
	    return new StmJump(kids.head);
	}
	public ExpList kids() { return new ExpList(exp, null); }
	public String toString() { return "JUMP("+exp+")"; }
    }
    /** Extension of <code>Spec.Stm</code> representing a label which
	is the target of a branch or call.
	@see harpoon.IR.Tree.LABEL
    */
    public static class StmLabel extends Stm {
	/** Label. 
	    @see harpoon.Temp.Label
	 */
	public final String name;
	/** Constructs a new <code>Spec.StmLabel</code>.
	    @param name Label.
	*/
	public StmLabel(String name) { this.name = name; }
	public void accept(StmVisitor v) { v.visit(this); }
	public Stm build(ExpList kids) {
	    assert kids==null;
	    return new StmLabel(name);
	}
	public ExpList kids() { return null; }
	public String toString() { return "LABEL("+name+")"; }
	public boolean canBeRootOfData() { return true; }
    }
    /** Extension of <code>Spec.Stm</code> representing a method header.
     *  @see harpoon.IR.Tree.METHOD
     */
    public static class StmMethod extends Stm {
	/** Identifier name to get params field of <code>Tree.METHOD</code>. */
	public final String params;
	/** Constructs a new <code>Spec.StmMethod</code>.
	 *  @param params Identifier to get params field of 
	 *                <code>Tree.METHOD</code>.
	 */
	public StmMethod(String params) { this.params = params; }
	public void accept(StmVisitor v) { v.visit(this); }
	public Stm build(ExpList kids) {
	    assert kids==null;
	    return new StmMethod(params);
	}
	public ExpList kids() { return null; }
	public String toString() { return "METHOD("+params+")"; }
    }
    /** Extension of <code>Spec.Stm</code> representing an expression
	which moves a value from one place to another.
	@see harpoon.IR.Tree.MOVE
    */
    public static class StmMove extends Stm {
	/** The set of Types that <code>src</code> and <code>dst</code>
	 *  may be (they will always be the same type). */
	public final TypeSet types;
	/** Expression yielding the destination of this move. */
	public final Exp dst;
	/** Expression yielding the source data of this move. */
	public final Exp src;
	/** Constructs a new <code>Spec.StmMove</code>.
	    @param dst Destination expression.
	    @param src Source expression.
	*/
	public StmMove(TypeSet types, Exp dst, Exp src) {
	    this.types = types; this.dst = dst; this.src = src;
	    assert !types.containsSmall() : ("MOVE cannot be precisely typed: "+this);
	}
	public void accept(StmVisitor v) { v.visit(this); }
	public Stm build(ExpList kids) {
	    assert kids!=null && kids.tail!=null && kids.tail.tail==null;
	    return new StmMove((TypeSet)types.clone(),
			       kids.head, kids.tail.head);
	}
	public ExpList kids() {
	    return new ExpList(dst, new ExpList(src, null));
	}
	public String toString() { return "MOVE"+types+"("+dst+","+src+")"; }
    }
    /** Extension of <code>Spec.Stm</code> representing an expression
	which is evaluated for its side effects (i.e. we throw away
	the return value).
	@see harpoon.IR.Tree.NATIVECALL
    */
    public static class StmNativeCall extends Stm {
	/** Return value destination expression. 
	    @see harpoon.IR.Tree.Exp
	 */
	public final String retval;
	/** Function location expression. 
	    @see harpoon.IR.Tree.Exp
	 */
	public final Exp func;
	/** Arguments being passed to procedure. 
	    @see harpoon.IR.Tree.TempList
	*/
	public final String arglist;
	/** Constructs a new <code>Spec.StmNativeCall</code>.
	    @param retval Return value destination expression.
	    @param func Function location expression.
	    @param arglist Arguments.
	*/
	public StmNativeCall(String retval, Exp func, String arglist) {
	    this.retval = retval; this.func = func;
	    this.arglist = arglist;
	}
	public void accept(StmVisitor v) { v.visit(this); }
	public Stm build(ExpList kids) {
	    assert kids!=null && kids.tail==null;
	    return new StmNativeCall(retval, kids.head, arglist);
	}
	public ExpList kids() { return new ExpList(func, null); }
	public String toString() {
	    return "NATIVECALL("+retval+","+func+","+arglist+")";
	}
    }
    /** Extension of <code>Spec.Stm</code> representing an expression
	which is evaluated for its side effects (i.e. we throw away
	the return value).
	@see harpoon.IR.Tree.RETURN
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
	    assert !types.containsSmall() : ("RETURN cannot be precisely typed: "+this);
	}
	public void accept(StmVisitor v) { v.visit(this); }
	public Stm build(ExpList kids) {
	    assert kids!=null && kids.tail==null;
	    return new StmReturn((TypeSet)types.clone(), kids.head);
	}
	public ExpList kids() { return new ExpList(retval, null); }
	public String toString() { return "RETURN"+types+"("+retval+")"; }
    }

    /** Extension of <code>Spec.Stm</code> representing a change of
     *  output segment.
     *  @see harpoon.IR.Tree.SEGMENT
     */
    public static class StmSegment extends Stm {
	/** Type of segment. */
	public final Leaf segtype;
	/** Constructs a new <code>Spec.StmSegment</code>.
	 *  @param segtype Segment type.
	 */
	public StmSegment(Leaf segtype) { this.segtype = segtype; }
	public void accept(StmVisitor v) { v.visit(this); }
	public Stm build(ExpList kids) {
	    assert kids==null;
	    return new StmSegment(segtype/*immutable*/);
	}
	public ExpList kids() { return null; }
	public String toString() { return "SEGMENT("+segtype+")"; }
	public boolean canBeRootOfData() { return true; }
    }

    /** Extension of <code>Spec.Stm</code> representing a sequence of
	statements to be executed in order.
	@see harpoon.IR.Tree.SEQ
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
	public Stm build(ExpList kids) {
	    throw new Error("build not valid for Seq");
	}
	public ExpList kids() {
	    throw new Error("build not valid for Seq");
	}
	public String toString() { return "SEQ("+s1+","+s2+")"; }
    }

    /** Extension of <code>Spec.Stm</code> representing a operation to
	throw an exception.
	@see harpoon.IR.Tree.THROW
    */
    public static class StmThrow extends Stm {
	/** The exceptional value. */
	public final Exp retex;
	/** The location of the exception-handling code to return to. */
	public final Exp handler;
	/** Constructs a new <code>Spec.StmThrow</code>.
	    @param retex The exceptional value expression.
	    @param handler The location of the exception-handling code.
	*/
	public StmThrow(Exp retex, Exp handler)
	{ this.retex = retex; this.handler = handler; }
	/** Provisional: REMOVE this. */
	public StmThrow(Exp exp) { this.retex = exp; this.handler=null;}
	public void accept(StmVisitor v) { v.visit(this); }
	public Stm build(ExpList kids) {
	    assert kids!=null && kids.tail!=null && kids.tail.tail==null;
	    return new StmThrow(kids.head, kids.tail.head);
	}
	public ExpList kids() {
	    return new ExpList(retex, new ExpList(handler, null));
	}
	public String toString() { return "THROW("+retex+","+handler+")"; }
    }

    /** Visitor class for traversing a set of <code>Spec.Leaf</code> objects 
	and performing some action depending on the type of
	<code>Spec.Leaf</code> visited.  Subclasses should implement a
	<code>visit</code> method for generic <code>Spec.Leaf</code>s (ed. note: Leaves?) and
	also override the <code>visit</code> method for subclasses of
	<code>Spec.Leaf</code> that the subclass cares about.
	@see <U>Design Patterns</U> pgs. 331-344
    */
    public static abstract class LeafVisitor {
	public abstract void visit(Leaf l);
	public void visit(LeafId l) { visit((Leaf)l); }
	public void visit(LeafOp l) { visit((Leaf)l); }
	public void visit(LeafNull l) { visit((Leaf)l); }
	public void visit(LeafNumber l) { visit((Leaf)l); }
	public void visit(LeafSegType l) { visit((Leaf)l); }
    }

    /** Abstract representation of leaves in the instruction pattern. */
    public static abstract class Leaf {
	public String toBop() { return this.toString(); }
	public String toUop() { return this.toString(); }

	/** Applies <code>v</code>'s <code>visit</code> method to
	    <code>this</code>.
	    This is effectively a gludge to emulate <B>multiple
	    dispatch</B>.  Must be reimplemented by all subclasses of
	    <code>Spec.Leaf</code>.
	    <BR> <B>effects:</B> Calls <code>v.visit(this)</code>. 
	    @see <U>Design Patterns</U> pgs. 331-344
	*/
	public abstract void accept(Spec.LeafVisitor v);
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
	/** Applies <code>v</code>'s <code>visit</code> method to
	    <code>this</code>.
	    This is effectively a gludge to emulate <B>multiple
	    dispatch</B>.  Must be reimplemented by all subclasses of
	    <code>Spec.Leaf</code>.
	    <BR> <B>effects:</B> Calls <code>v.visit(this)</code>. 
	    @see <U>Design Patterns</U> pgs. 331-344
	*/
	public void accept(Spec.LeafVisitor v) { v.visit(this); }
	public String toString() { return id; }
    }
    /** Extension of <code>Spec.Leaf</code> representing a null constant. */
    public static class LeafNull extends Leaf {
	/** Constructs a new <code>Spec.LeafNull</code>. */
	public LeafNull() { }
	public String toString() { return "null"; }
	public void accept(Spec.LeafVisitor v) { v.visit(this); }
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
	public String toBop() { return harpoon.IR.Tree.Bop.toString(op).toUpperCase(); }
	public String toUop() { return harpoon.IR.Tree.Uop.toString(op).toUpperCase(); }
	/** Applies <code>v</code>'s <code>visit</code> method to
	    <code>this</code>.
	    This is effectively a gludge to emulate <B>multiple
	    dispatch</B>.  Must be reimplemented by all subclasses of
	    <code>Spec.Leaf</code>.
	    <BR> <B>effects:</B> Calls <code>v.visit(this)</code>. 
	    @see <U>Design Patterns</U> pgs. 331-344
	*/
	public void accept(Spec.LeafVisitor v) { v.visit(this); }
    }
    /** Extension of <code>Spec.Leaf</code> which represents a
	segment type in the specification.
    */
    public static class LeafSegType extends Leaf {
	/* Enumerated segment type. */
	public final int segtype;
	/** Constructs a new <code>Spec.LeafSegType</code>.
	    @param segtype Segment type.
	*/
	public LeafSegType(int segtype) {
	    this.segtype = segtype;
	}
	public String toString() {
	    return harpoon.IR.Tree.SEGMENT.decode(segtype);
	}
	/** Applies <code>v</code>'s <code>visit</code> method to
	    <code>this</code>.
	    This is effectively a gludge to emulate <B>multiple
	    dispatch</B>.  Must be reimplemented by all subclasses of
	    <code>Spec.Leaf</code>.
	    <BR> <B>effects:</B> Calls <code>v.visit(this)</code>. 
	    @see <U>Design Patterns</U> pgs. 331-344
	*/
	public void accept(Spec.LeafVisitor v) { v.visit(this); }
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
	/** Applies <code>v</code>'s <code>visit</code> method to
	    <code>this</code>.
	    This is effectively a gludge to emulate <B>multiple
	    dispatch</B>.  Must be reimplemented by all subclasses of
	    <code>Spec.Leaf</code>.
	    <BR> <B>effects:</B> Calls <code>v.visit(this)</code>. 
	    @see <U>Design Patterns</U> pgs. 331-344
	*/
	public void accept(Spec.LeafVisitor v) { v.visit(this); }
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
	/** Visits elements of <code>l</code>.
	    If (l!=null) visits l.head then l.tail.  Else does nothing.
	*/
	public void visit(DetailList l) { 
	    if(l!=null){l.head.accept(this); visit(l.tail);}
	}
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
	public abstract void accept(DetailVisitor v);
    }

    /** Extension of <code>Spec.Detail</code> that requests an extra
     *  temporary register for the use of the action clause.  For example,
     *  if multiplying two 32-bit registers generates a 64-bit result on
     *  the target architecture, you might need to request an extra
     *  <code>Temp</code> to have a place to put the high word of the
     *  result (before you throw it away). The <code>DetailExtra</code>
     *  element contains a list of identifiers naming the working temps
     *  that you are requesting. 
     * 
     *  <P> <B>syntax:</B> <code> %extra { </code> ID-LIST <code> } </code>
     */
    public static class DetailExtra extends Detail {
	public final int type;
	public final IdList extras;
	public DetailExtra(int type, IdList extras) {
	    this.type = type; this.extras = extras;
	}
	/** Applies <code>v</code>'s <code>visit</code> method to <code>this</code>. */
	public void accept(DetailVisitor v) { v.visit(this); }
	public String toString() {
	    return "%extra"+new TypeSet(type)+"{"+((extras==null)?"":extras.toString())+"}";
	}
    }
    /** Extension of <code>Spec.Detail</code> that stores a
	<code>predicate_string</code> which is a piece of Java code
	that decides if a particular <code>Spec.Rule</code> can be
	applied. 

	<P> <B>syntax:</B> <code> %pred %( </code> BOOLEAN-EXPRESSION <code> )% </code>
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

	<P> <B>syntax:</B> <code> %weight &lt; </code> ID <code> , </code> WEIGHT <code> &gt; </code>
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
	
	@see harpoon.IR.Tree.Type
	@see harpoon.IR.Tree.PreciselyTyped
     */
    public static class TypeSet {
	// maps Type->boolean
	final BitString bs;
	
	// maps ( bitLength - 1 )->boolean
	// (should only be on if this.contains(PreciseType.SMALL))
	final BitString unsignedPrecises;

	// maps ( bitLength - 1 )->boolean
	// (should only be on if this.contains(PreciseType.SMALL))
	final BitString signedPrecises;

	/** Constructs a new <code>Spec.TypeSet</code>. */
	public TypeSet() {
	    bs = new BitString(5);//ACK: too tightly coupled to Tree.Type
	    unsignedPrecises = new BitString(32);
	    signedPrecises = new BitString(32);
	}

	/** Constructs a new <code>Spec.TypeSet</code> with type <code>t</code>. */
	public TypeSet(int t) { this(); set(t); }

	/** Constructs a new <code>Spec.TypeSet</code> with the same
	 *  contents as the supplied <code>Spec.TypeSet</code>. */
	public TypeSet(TypeSet ts) {
	    this.bs = (BitString) ts.bs.clone();
	    this.unsignedPrecises = (BitString) ts.unsignedPrecises.clone();
	    this.signedPrecises = (BitString) ts.signedPrecises.clone();
	}

	/** Clones a <code>Spec.TypeSet</code>. */
	public Object clone() { return new TypeSet(this); }
	
	/** Checks if <code>this</code> contains <code>type</code>.
	    <BR> <B>effects:</B> Returns true if <code>type</code> has
	         been turned on by a call to <code>set(type)</code> or
		 <code>setAll()</code>.  Else returns false.
	*/
	public boolean contains(int type) {
	    return bs.get(type);
	}
	/** Returns true if <code>this</code> contains any small types. */
	public boolean containsSmall() {
	    return !(unsignedPrecises.isZero() && signedPrecises.isZero());
	}
	/** Returns true if <code>this</code> contains the specified
	 *  signed precise type. */
	public boolean containsSigned(int bitwidth) {
	    return signedPrecises.get(bitwidth-1);
	}
	/** Returns true if <code>this</code> contains the specified
	 *  unsigned precise type. */
	public boolean containsUnsigned(int bitwidth) {
	    return unsignedPrecises.get(bitwidth-1);
	}

	/** Records that <code>this</code> contains
	    <code>type</code>.  
	*/
	public void set(int type) {
	    bs.set(type);
	}

	/** Recordes that <code>this</code> contains
	    a signed, specific precise type value
	    <BR> <B>requires:</B>  1 <= numBits <= 32
	    @param numBits The bit length of the type. 
	*/
	public void setSignedPrecise(int numBits) {
	    assert (numBits <= 32) && (numBits >= 1) : ("invalid bit length:"+numBits);
	    signedPrecises.set(numBits-1);
	}

	/** Recordes that <code>this</code> contains
	    an unsigned, specific precise type value
	    <BR> <B>requires:</B>  1 <= numBits <= 32
	    @param numBits The bit length of the type. 
	*/
	public void setUnsignedPrecise(int numBits) {
	    assert (numBits <= 32) && (numBits >= 1) : ("invalid bit length:"+numBits);
	    unsignedPrecises.set(numBits-1);
	}
	
	/** Adds all the types contained in <code>TypeSet</code>
	 *  <code>ts</code> to <code>this</code>.
	 */
	public void addAll(TypeSet ts) {
	    bs.or(ts.bs);
	    signedPrecises.or(ts.signedPrecises);
	    unsignedPrecises.or(ts.unsignedPrecises);
	}
	
	/** Records that <code>this</code> contains all five basic
	    Types,  { INT, LONG, FLOAT, DOUBLE, POINTER }.
	    Note that it does not set any bits for the PreciseTypes.
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
	    if (contains(Type.INT))     sb.append(",i");
	    if (contains(Type.LONG))    sb.append(",l");
	    if (contains(Type.FLOAT))   sb.append(",f");
	    if (contains(Type.DOUBLE))  sb.append(",d");
	    if (contains(Type.POINTER)) sb.append(",p");
	    for(int i=0; i<32; i++)
		if(unsignedPrecises.get(i))
		    sb.append(",u:"+(i+1));
	    for(int i=0; i<32; i++)
		if(signedPrecises.get(i))
		    sb.append(",s:"+(i+1));
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
	/** Applies <code>v</code>'s <code>visit</code> method to <code>this</code>. */
	public void accept(RuleVisitor v) { v.visit(this); }
    }

    /** Linked list representation for representing the series of
     *  <code>Spec.Exp</code>s in a given <code>Spec.Exp</code> or
     *  <code>Spec.Stm</code>.
     */
    public static class ExpList {
	public final Exp head;
	public final ExpList tail;
	public ExpList(Exp head, ExpList tail) {
	    this.head = head; this.tail = tail;
	}
	public String toString() {
	    if (tail==null) return head.toString();
	    else return head.toString() + " " + tail.toString();
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
	public void accept(DetailVisitor v) { v.visit(this); }
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
