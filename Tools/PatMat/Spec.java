// Spec.java, created Wed Feb 17 22:05:18 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Tools.PatMat;

import java.util.BitSet;
import java.util.List;

/**
 * <code>Spec</code> represents the parsed specification.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Spec.java,v 1.1.2.4 1999-06-25 04:10:56 pnkfelix Exp $
 */
public class Spec  {

    /** Java code statements that are going to be placed outside class
	body (package declaration, import statements).
    */
    public  String global_stms;

    /** Java code statements that are going to be placed inside class
	body (helper methods, fields, inner classes).
    */
    public String class_stms;

    /** List of Instruction Patterns for this machine specification.
     */
    public RuleList rules;
    
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

    /** Abstract immutable representation of an Instruction Pattern.
	Contains a <code>Spec.DetailList</code> and a series of Java
	code statements.
     */
    public static abstract class Rule {
	/** List of the extra details associated with
	    <code>this</code> (speed-cost, size-cost, etc.).
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
			   etc.).
	    @param action_str Series of Java code statements that
	                      represent the action to perform if
			      <code>this</code> fires.
	*/
	public Rule(DetailList details, String action_str) {
	    this.details = details; this.action_str = action_str;
	}

	public String toString() {
	    String s = " %{" + action_str + "}%";
	    if (details==null) return s;
	    else return details.toString()+s;
	}
    }

    /** Extension of <code>Spec.Rule</code> that also contains a
	<code>Spec.Exp</code> to match <code>Tree</code> expressions
	and a tag to identify the result that <code>this</code>
	?produces?.  (NOTE: Not sure if that's what result_id
	is...check with Scott).
	
	<code>Spec.RuleExp</code>s match (sub)expressions in the code,
	which is why it is necessary to associate a
	<code>result_id</code>: expression matching is context
	sensitive, depending on what the outerlying expression or
	statement is expecting the nested expression to return.

    */
    public static class RuleExp extends Rule {
	/** Expression <code>this</code> matches. */
	public final Exp exp;
	/** Result type that <code>this</code> matches. */
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
			   etc.). 
	    @param action_str Series of Java code statements that
	                      represent the action to perform if
			      <code>this</code> fires.
	*/
	public RuleExp(Exp exp, String result_id,
		       DetailList details, String action_str) {
	    super(details, action_str);
	    this.exp = exp; this.result_id = result_id;
	}
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
			   etc.). 
	    @param action_str Series of Java code statements that
	                      represent the action to perform if
			      <code>this</code> fires.
	*/
	public RuleStm(Stm stm, DetailList details, String action_str) {
	    super(details, action_str);
	    this.stm = stm;
	}
	public String toString() {
	    return stm + " " + super.toString();
	}
    }

    /** Abstract immutable representation of an Expression in an
	Instruction Pattern. 
    */
    public static abstract class Exp { }
    
    /** Extension of <code>Spec.Exp</code> that represents an
	Identifier in the code.  Essentially a wrapper around a
	<code>String</code>.
    */
    public static class ExpId extends Exp {
	/** Identifier that <code>this</code> represents. */
	public final String id;
	public ExpId(String id) { this.id = id; }
	public String toString() { return id; }
    }

    /** Extension of <code>Spec.Exp</code> that represents a Binary
	Operation in the code. 
    */
    public static class ExpBinop extends Exp {
	/** Types of values that <code>this</code> operates on. */
	public final TypeSet types;
	/** Opcode for <code>this</code>. */
	public final Leaf opcode;
	/** Expression on the left side of <code>this</code>. */
	public final Exp left;

	public final Exp right;
	public ExpBinop(TypeSet types, Leaf opcode, Exp left, Exp right) {
	    this.types = types; this.opcode = opcode;
	    this.left = left;   this.right = right;
	}
	public String toString() {
	    return "BINOP"+types+"("+opcode.toBop().toUpperCase()+","
		+left+","+right+")";
	}
    }
    public static class ExpConst extends Exp {
	public final TypeSet types;
	public final Leaf value;
	public ExpConst(TypeSet types, Leaf value) {
	    this.types = types; this.value = value;
	}
	public String toString() { return "CONST"+types+"("+value+")"; }
    }
    public static class ExpEseq extends Exp {
	public final Stm stm;
	public final Exp exp;
	public ExpEseq(Stm stm, Exp exp) { this.stm = stm; this.exp = exp; }
	public String toString() { return "ESEQ("+stm+","+exp+")"; }
    }
    public static class ExpMem extends Exp {
	public final TypeSet types;
	public final Exp addr;
	public ExpMem(TypeSet types, Exp addr) {
	    this.types = types; this.addr = addr;
	}
	public String toString() { return "MEM"+types+"("+addr+")"; }
    }
    public static class ExpName extends Exp {
	public final String name;
	public ExpName(String name) { this.name = name; }
	public String toString() { return "NAME("+name+")"; }
    }
    public static class ExpTemp extends Exp {
	public final TypeSet types;
	public final String name;
	public ExpTemp(TypeSet types, String name) {
	    this.types = types; this.name = name;
	}
	public String toString() { return "TEMP"+types+"("+name+")"; }
    }
    public static class ExpUnop extends Exp {
	public final TypeSet types;
	public final Leaf opcode;
	public final Exp exp;
	public ExpUnop(TypeSet types, Leaf opcode, Exp exp) {
	    this.types = types; this.opcode = opcode; this.exp = exp;
	}
	public String toString() {
	    return "UNOP"+types+"("+opcode.toUop().toUpperCase()+","+exp+")";
	}
    }

    public static abstract class Stm { }
    public static class StmCall extends Stm {
	public final Exp retval, retex, func;
	public final String arglist;
	public StmCall(Exp retval, Exp retex, Exp func, String arglist) {
	    this.retval = retval; this.retex = retex; this.func = func;
	    this.arglist = arglist;
	}
	public String toString() {
	    return "CALL("+retval+","+retex+","+func+","+arglist+")";
	}
    }
    public static class StmCjump extends Stm {
	public final Exp test;
	public final String t_label, f_label;
	public StmCjump(Exp test, String t_label, String f_label) {
	    this.test = test; this.t_label = t_label; this.f_label = f_label;
	}
	public String toString() {
	    return "CJUMP("+test+","+t_label+","+f_label+")";
	}
    }
    public static class StmExp extends Stm {
	public final Exp exp;
	public StmExp(Exp exp) { this.exp = exp; }
	public String toString() { return "EXP("+exp+")"; }
    }
    public static class StmJump extends Stm {
	public final Exp exp;
	public StmJump(Exp exp) { this.exp = exp; }
	public String toString() { return "JUMP("+exp+")"; }
    }
    public static class StmLabel extends Stm {
	public final String name;
	public StmLabel(String name) { this.name = name; }
	public String toString() { return "LABEL("+name+")"; }
    }
    public static class StmMove extends Stm {
	public final Exp dst, src;
	public StmMove(Exp dst, Exp src) { this.dst = dst; this.src = src; }
	public String toString() { return "MOVE("+dst+","+src+")"; }
    }
    public static class StmNativeCall extends Stm {
	public final Exp retval, retex, func;
	public final String arglist;
	public StmNativeCall(Exp retval, Exp retex, Exp func, String arglist) {
	    this.retval = retval; this.retex = retex; this.func = func;
	    this.arglist = arglist;
	}
	public String toString() {
	    return "NATIVECALL("+retval+","+retex+","+func+","+arglist+")";
	}
    }
    public static class StmReturn extends Stm {
	public final TypeSet types;
	public final Exp retval;
	public StmReturn(TypeSet types, Exp retval) {
	    this.types = types; this.retval = retval;
	}
	public String toString() { return "RETURN"+types+"("+retval+")"; }
    }
    public static class StmSeq extends Stm {
	public final Stm s1, s2;
	public StmSeq(Stm s1, Stm s2) { this.s1 = s1; this.s2 = s2; }
	public String toString() { return "SEQ("+s1+","+s2+")"; }
    }
    public static class StmThrow extends Stm {
	public final Exp exp;
	public StmThrow(Exp exp) { this.exp = exp; }
	public String toString() { return "THROW("+exp+")"; }
    }

    public static abstract class Leaf {
	public String toBop() { return this.toString(); }
	public String toUop() { return this.toString(); }
    }
    public static class LeafId extends Leaf {
	public final String id;
	public LeafId(String id) {
	    this.id = id;
	}
	public String toString() { return id; }
    }
    public static class LeafNumber extends Leaf {
	public final int number;
	public LeafNumber(int number) {
	    this.number = number;
	}
	public String toString() { return Integer.toString(number); }
	public String toBop() { return harpoon.IR.Tree.Bop.toString(number); }
	public String toUop() { return harpoon.IR.Tree.Uop.toString(number); }
    }

    public static abstract class Detail { }
    public static class DetailExtra extends Detail {
	public final IdList extras;
	public DetailExtra(IdList extras) {
	    this.extras = extras;
	}
	public String toString() {
	    return "%extra{"+((extras==null)?"":extras.toString())+"}";
	}
    }
    public static class DetailPredicate extends Detail {
	public final String predicate_string;
	public DetailPredicate(String predicate_string) {
	    this.predicate_string = predicate_string;
	}
	public String toString() { return "%pred %("+predicate_string+")%"; }
    }
    public static class DetailWeight extends Detail {
	public final String name;
	public final float value;
	public DetailWeight(String name, float value) {
	    this.name = name; this.value = value;
	}
	public String toString() { return "%weight<"+name+","+value+">"; }
    }

    public static class TypeSet {
	final BitSet bs = new BitSet();
	public TypeSet() { }
	public boolean contains(int type) {
	    return bs.get(type);
	}
	public void set(int type) {
	    bs.set(type);
	}
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
