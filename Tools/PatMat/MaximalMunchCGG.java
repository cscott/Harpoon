// MaximalMunchCGG.java, created Thu Jun 24 18:07:16 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Tools.PatMat;

import harpoon.Util.Util;
import harpoon.IR.Tree.Type;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;

/**
 * <code>MaximalMunchCGG</code> finds an optimal tiling of
 * instructions on a Tree-IR.
 *
 * This Code Generator Generator produces Code Generators that use the
 * Maximal Munch algorithm to find an optimal tiling for an input
 * tree.  See Appel "Modern Compiler Implementation in Java", Section
 * 9.1 for a description of Maximal Munch.
 * 
 * TODO: replace all Class references with full names (so there's no
 * chance of a name conflict in whatever import statements that the
 * user includes in their .spec file (just make static Strings in this
 * file to reference the full name
 *
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: MaximalMunchCGG.java,v 1.1.2.4 1999-06-28 09:16:54 pnkfelix Exp $ */
public class MaximalMunchCGG extends CodeGeneratorGenerator {
    
    /** Creates a <code>MaximalMunchCGG</code>. 
	<BR> <B>requires:</B> <OL>
	     <LI> <code>s</code> follows the standard template for
	          defining a machine specification.  
	     <LI> <code>className</code> is a legal Java identifier
	          string for a class.
	     <LI> For each node-type in the <code>Tree</code> IR,
	          there exists a single-node tile pattern (TODO: I
		  took this 'requirement' straight from Appel, but it
		  can't REALLY be that strict.  Find a tighter
		  requirement that we can actually satisfy)
	     <LI> if <code>s</code> contains Java statements that rely
	          on knowledge about the class to be produced (such as
		  a Constructor implementation) then the class named
		  must match the <code>className</code> parameter.
	     </OL>
	<BR> <B>effects:</B> Creates a new
             <code>MaximalMunchCGG</code> and associates the
	     machine specification <code>s</code> with the newly
	     constructed <code>MaximalMunchCGG</code>.
	@see <A HREF="doc-files/instr-selection-tool.html">Standard Specification Template</A>
    */
    public MaximalMunchCGG(Spec s, String className) {
        super(s, className);
    }

    /** Sets up a series of checks to ensure all of the values
	in the visited expression are of the appropriate
	type. 
    */
    static class TypeExpRecurse extends Spec.ExpVisitor {
	/** constantly updated boolean expression to match a tree's types. */
	StringBuffer exp;
	
	/** constantly updated (and reset) with current expression prefix throughout recursive calls. */
	String expPrefix;
		
	/** number of nodes "munched" by this so far. */
	int degree;
	
	/** Indentation. */
	String indentPrefix;

	private void append(String s) {
	    exp.append(indentPrefix + s +"\n");
	}

	TypeExpRecurse(String expPrefix, String indentPrefix) {
	    exp = new StringBuffer("true\n"); // hack to make everything else additive
	    degree = 0;
	    this.expPrefix = expPrefix;
	    this.indentPrefix = indentPrefix;
	}
	
	public void visit(Spec.Exp e) {
	    Util.assert(false, "ExpRecurse should never visit Exp");
	}
	public void visit(Spec.ExpBinop e) { 
	    degree++;

	    append("// check expression type");
	    append("&& " + expPrefix + " instanceof BINOP");

	    append("// check opcode");
	    append("&& ((BINOP)" + expPrefix + ").op == " + e.opcode.toBop());

	    append("// check operand types");
	    boolean allowInt, allowLong, allowFloat, allowDouble, allowPointer;
	    allowDouble = e.types.contains(Type.DOUBLE);
	    allowFloat = e.types.contains(Type.FLOAT);
	    allowInt = e.types.contains(Type.INT);
	    allowLong = e.types.contains(Type.LONG);
	    allowPointer = e.types.contains(Type.POINTER);

	    String checkPrefix = "\t(((BINOP)" + expPrefix + ").optype) ==";
	    append("&& ( ");
	    if(allowDouble) append(checkPrefix + " Type.DOUBLE ||");
	    if(allowFloat) append(checkPrefix + " Type.FLOAT ||");
	    if(allowInt) append(checkPrefix + " Type.INT ||");
	    if(allowLong) append(checkPrefix + " Type.LONG ||");
	    if(allowPointer) append(checkPrefix + " Type.POINTER ||");
	    append("\tfalse )");
	    append("// end check operand types");

	    // save state before outputing children-checking code
	    String oldPrefix = expPrefix;
	    String oldIndent = indentPrefix;

	    indentPrefix = oldIndent + "\t";
	    
	    append("// check left child");
	    expPrefix = "((BINOP)" + oldPrefix + ").left";
	    e.left.accept(this);
	    append("// check right child");  
	    expPrefix = "((BINOP)" + oldPrefix + ").right";
	    e.right.accept(this);
	    
	    // restore original state
	    indentPrefix = oldIndent;
	    expPrefix = oldPrefix;
	}
	
	public void visit(Spec.ExpConst e) {
	    degree++;

	    append("// check expression type");
	    append("&& " + expPrefix + " instanceof CONST");

	    append("// check operand types");
	    boolean allowInt, allowLong, allowFloat, allowDouble, allowPointer;
	    allowDouble = e.types.contains(Type.DOUBLE);
	    allowFloat = e.types.contains(Type.FLOAT);
	    allowInt = e.types.contains(Type.INT);
	    allowLong = e.types.contains(Type.LONG);
	    allowPointer = e.types.contains(Type.POINTER);

	    String checkPrefix = "\t(((CONST)" + expPrefix + ").type) ==";
	    append("&& ( ");
	    if(allowDouble) append(checkPrefix + " Type.DOUBLE ||");
	    if(allowFloat) append(checkPrefix + " Type.FLOAT ||");
	    if(allowInt) append(checkPrefix + " Type.INT ||");
	    if(allowLong) append(checkPrefix + " Type.LONG ||");
	    if(allowPointer) append(checkPrefix + " Type.POINTER ||");
	    append("\tfalse )");
	    append("// end check operand types");

	    try {
		Spec.LeafNumber val = (Spec.LeafNumber) e.value;
		append("// check that constant value matches");
		append("&& ( " + expPrefix + ".isFloatingPoint()?");
		append(expPrefix + ".doubleValue() == " + val.number.doubleValue() + ":");
		append(expPrefix + ".longValue() == " + val.number.longValue() + ")");
	    } catch (ClassCastException cce) {
		// whoops, not a LeafNumber.  No check needed.
	    }
			   
	    
	}
	public void visit(Spec.ExpId e) {
	    // do nothing; don't even increase the munch-factor (ie
	    // 'degree') (Spec.ExpId is strictly a way for a
	    // specification to refer back to items in the parsed
	    // tree, which is not the TypeExpRecurser's concern
	    append("// no check needed for ExpId children");
	    return;
	}
	public void visit(Spec.ExpMem e) { 
	    degree++;

	    append("// check expression type");
	    append("&& " + expPrefix + " instanceof MEM");

	    append("// check operand types");
	    boolean allowInt, allowLong, allowFloat, allowDouble, allowPointer;
	    allowDouble = e.types.contains(Type.DOUBLE);
	    allowFloat = e.types.contains(Type.FLOAT);
	    allowInt = e.types.contains(Type.INT);
	    allowLong = e.types.contains(Type.LONG);
	    allowPointer = e.types.contains(Type.POINTER);

	    String checkPrefix = "\t(((MEM)" + expPrefix + ").type) ==";
	    append("&& ( ");
	    if(allowDouble) append(checkPrefix + " Type.DOUBLE ||");
	    if(allowFloat) append(checkPrefix + " Type.FLOAT ||");
	    if(allowInt) append(checkPrefix + " Type.INT ||");
	    if(allowLong) append(checkPrefix + " Type.LONG ||");
	    if(allowPointer) append(checkPrefix + " Type.POINTER ||");
	    append("\tfalse )");
	    append("// end check operand types");

	    // save state before outputing child-checking code
	    String oldPrefix = expPrefix;
	    String oldIndent = indentPrefix;

	    indentPrefix = oldIndent + "\t";

	    append("// check child");
	    expPrefix = "((MEM)" + oldPrefix + ").exp";
	    e.addr.accept(this);

	    // restore original state
	    indentPrefix = oldIndent;
	    expPrefix = oldPrefix;

	}
	public void visit(Spec.ExpName e) { 
	    degree++;

	    append("// check expression type");
	    append("&& " + expPrefix + "instanceof NAME");
	    
	}
	public void visit(Spec.ExpTemp e) { 
	    degree++;

	    append("// check expression type");
	    append("&& " + expPrefix + "instanceof TEMP");
	    
	    append("// check operand type");
	    boolean allowInt, allowLong, allowFloat, allowDouble, allowPointer;
	    allowDouble = e.types.contains(Type.DOUBLE);
	    allowFloat = e.types.contains(Type.FLOAT);
	    allowInt = e.types.contains(Type.INT);
	    allowLong = e.types.contains(Type.LONG);
	    allowPointer = e.types.contains(Type.POINTER);

	    String checkPrefix = "\t(((TEMP)" + expPrefix + ").type) ==";
	    append("&& ( ");
	    if(allowDouble) append(checkPrefix + " Type.DOUBLE ||");
	    if(allowFloat) append(checkPrefix + " Type.FLOAT ||");
	    if(allowInt) append(checkPrefix + " Type.INT ||");
	    if(allowLong) append(checkPrefix + " Type.LONG ||");
	    if(allowPointer) append(checkPrefix + " Type.POINTER ||");
	    append("\tfalse )");
	    append("// end check operand types");
	}
	public void visit(Spec.ExpUnop e) { 
	    degree++;

	    append("// check expression type");
	    append("&& " + expPrefix + " instanceof UNOP");
	    append("// check opcode");
	    append("&& ((UNOP)" + expPrefix + ").op == " + e.opcode.toUop());

	    append("// check operand types");
	    boolean allowInt, allowLong, allowFloat, allowDouble, allowPointer;
	    allowDouble = e.types.contains(Type.DOUBLE);
	    allowFloat = e.types.contains(Type.FLOAT);
	    allowInt = e.types.contains(Type.INT);
	    allowLong = e.types.contains(Type.LONG);
	    allowPointer = e.types.contains(Type.POINTER);

	    String checkPrefix = "\t(((UNOP)" + expPrefix + ").optype) ==";
	    append("&& ( ");
	    if(allowDouble) append(checkPrefix + " Type.DOUBLE ||");
	    if(allowFloat) append(checkPrefix + " Type.FLOAT ||");
	    if(allowInt) append(checkPrefix + " Type.INT ||");
	    if(allowLong) append(checkPrefix + " Type.LONG ||");
	    if(allowPointer) append(checkPrefix + " Type.POINTER ||");
	    append("\tfalse )"); 
	    append("// end check operand types");

	    
	    // save state before outputting child-checking code
	    String oldPrefix = expPrefix;
	    String oldIndent = indentPrefix;

	    indentPrefix = oldIndent + "\t";

	    append("// check child");
	    expPrefix = "((UNOP)" + oldPrefix + ").operand";
	    e.exp.accept(this);

	    // restore state
	    expPrefix = oldPrefix;
	    indentPrefix = oldIndent;
	}
    }	    


    /** Writes the Instruction Selection Method to <code>out</code>.
	<BR> <B>modifies:</B> <code>out</code>
	<BR> <B>effects:</B>
	     Generates Java source for a MaximalMunch instruction
	     selection method, not including method signature or
	     surrounding braces.  Outputs generated source to
	     <code>out</code>. 
	@param out Target output device for the Java source code.
    */
    public void outputSelectionMethod(final PrintWriter out) { 
	// traverse 'this.spec' to acquire spec information
	final List expMatchActionPairs = new LinkedList(); // list of RuleTriplet
	final List stmMatchActionPairs = new LinkedList();

	final String expArg = "expArg";
	final String indent = "\t\t\t";

	Spec.RuleVisitor srv = new Spec.RuleVisitor() {
	    public void visit(Spec.Rule r) {
		Util.assert(false, "SpecRuleVisitor should never visit Rule");
	    }
	    public void visit(Spec.RuleStm r) { 

	    }
	    public void visit(Spec.RuleExp r) { 
		TypeExpRecurse recurse = 
		    new TypeExpRecurse(expArg, indent + "\t");
		r.exp.accept(recurse);

		String typeCheck = recurse.exp.toString();
		int munchFactor = recurse.degree;

		// TODO: add code to also check predicates and set _matched_ to true
		// for now faking it ...
		String matchStm = indent + "_matched_ =  (" + typeCheck + indent + ");";
		expMatchActionPairs.add( new RuleTriplet( matchStm,
							  r.action_str,
							  recurse.degree ) );
		
	    }

	};
	
	Spec.RuleList list = spec.rules;
	while(list != null) {
	    list.head.accept(srv);
	    list = list.tail;
	}
	
	Comparator compare = new RuleTripletComparator();
	Collections.sort(expMatchActionPairs, compare);
	Collections.sort(stmMatchActionPairs, compare);
	
	// Implement a recursive function by making a helper class to
	// visit the nodes
	out.println("\tstatic final class CggVisitor extends TreeVisitor {");

	// for each rule for an exp we need to implement a
	// clause in munchExp()
	out.println("\t\t void munchExp(Exp " + expArg + ") {");
	
	out.println("\t\t\tboolean _matched_ = false;");

	Iterator expPairsIter = expMatchActionPairs.iterator();
	while(expPairsIter.hasNext()) {
	    RuleTriplet triplet = (RuleTriplet) expPairsIter.next();
	    out.println(triplet.matchExp);
	    out.println("\t\t\tif (_matched_) { // action code!");
	    out.println(triplet.actionStms);
	    
	    out.println("\t\t\t}");
	}


	// for each rule for a statement we need to implement a
	// clause in munchStm()

	
	out.println("\t}"); // end CggVisitor
	

	
    }
    
    static class RuleTriplet {
	String matchExp, actionStms; int degree;
	    
	/** Constructs a new <code>RuleTriplet</code>.
	    @param matchExp A series of Java statements which will set _matched_ to TRUE if expression matches
	    @param actionStms A series of Java statements to execute if _matched_ == TRUE after 'matchExp' executes 
	    @param degree Number of nodes that this rule "eats"
	*/
	RuleTriplet(String matchExp, String actionStms, int degree) {
	    this.matchExp = matchExp;
	    this.actionStms = actionStms;
	    this.degree = degree;
	}
    }
    
    static class RuleTripletComparator implements Comparator {
	public int compare(Object o1, Object o2) {
	    RuleTriplet r1 = (RuleTriplet) o1;
	    RuleTriplet r2 = (RuleTriplet) o2;
	    return r1.degree - r2.degree;
	}
    }

}
