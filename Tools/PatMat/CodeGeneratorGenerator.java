// CodeGeneratorGenerator.java, created Thu Jun 24 20:08:33 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Tools.PatMat;

import java.io.PrintWriter;

/**
 * <code>CodeGeneratorGenerator</code> generates a java program to
 * convert <code>Tree</code>s into <code>Instr</code> sequences.  
 *
 * A Code Generator Generator (CGG) takes a set of instruction pattern
 * tiles and constructs a java source file for a utility class
 * containing a method <code>codegen(TreeCode)</code> which will
 * generate an <code>HCode</code> in <code>Instr</code> form from a
 * <code>TreeCode</code>.  In short, a CGG generates a Code Generator.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: CodeGeneratorGenerator.java,v 1.1.2.13 1999-07-30 18:45:17 pnkfelix Exp $ */
public abstract class CodeGeneratorGenerator {

    private static final String TREE_TreeCode = "harpoon.IR.Tree.TreeCode";
    private static final String TREE_Code = "harpoon.IR.Tree.Code";
    private static final String GENERIC_Code = "harpoon.Backend.Generic.Code";
    private static final String ASSEM_Instr = "harpoon.IR.Assem.Instr";

    /** The machine specification that the CodeGenerators outputted by
	<code>this</code> will target.  
    */
    protected Spec spec;
    
    /** Names the class that <code>this</code> outputs. */
    private String className;

    /** Creates a <code>CodeGeneratorGenerator</code>. 
	<BR> <B>requires:</B> <OL>
	     <LI> <code>s</code> follows the standard template for
	          defining a machine specification. 
	     <LI> if <code>s</code> contains Java statements that rely
	          on knowledge about the class to be produced (such as
		  a Constructor implementation) then the class named
		  must match the <code>className</code> parameter.
	     </OL>
	<BR> <B>effects:</B> Creates a new
             <code>CodeGeneratorGenerator</code> and associates the
	     machine specification <code>s</code> with the newly
	     constructed <code>CodeGeneratorGenerator</code>.
	@see <A HREF="doc-files/instr-selection-tool.html">Standard Specification Template</A>
	@param s <code>Spec</code> defining the machine specification
	         that the CodeGenerator output by <code>this</code>
		 will target.
	@param className Formal name of the class that
	                 <code>this</code> outputs. 
    */
    public CodeGeneratorGenerator(Spec s, String className) {
        this.spec = s;
	this.className = className;
    }
    
    /** Writes the Code Generator to <code>out</code>.
	<BR> <B>modifies:</B> <code>out</code>
	<BR> <B>effects:</B> 
	     Generates Java source for the machine specification
	     associated with <code>this</code>.  
	     Then writes the Java source code out to <code>out</code>,
	     and flushes <code>out</code> after it finishes outputting
	     the source code.
	     <BR>All of the output file is parameterized by
	     <code>this.spec</code>, with the exception of  
	     <OL> 
	     <LI>class name.
	         This is already defined by <code>this.className</code>.
	     <LI>class signature.
	         This is already hardcoded as 
		 <code>public class <u>this.className</u> extends
		 harpoon.Backend.Generic.CodeGen</code>. 
	     <LI>codegen method signature.  This is already hardcoded as 
		 <code>public final Instr gen(harpoon.IR.Tree.TreeCode tree)</code>. 
	     </OL>
	@param out Target output device for the Java source code.
    */
    public void outputJavaFile(PrintWriter out) {
	out.println(spec.global_stms);
	out.println("public class " + className + 
		    " extends harpoon.Backend.Generic.GenericCodeGen { ");
	out.println(spec.class_stms);
	
	out.println("\t/** Generates assembly code from a <code>" + TREE_TreeCode + "</code>.");
	out.println("\t    <BR> <B>modifies:</B> <code>this</code>");
	out.println("\t    <BR> <B>effects:</B>");
	out.println("\t         Scans <code>tree</code> to find a tiling of ");
	out.println("\t         Instruction Patterns, calling auxillary methods");
	out.println("\t         and data structures as defined in the .Spec file");
	out.println("\t    @param tree Set of abstract <code>Tree</code> instructions ");
	out.println("\t                that form the body of the procedure being compiled.");
	out.println("\t*/");
	out.println("\tpublic final "+ASSEM_Instr+" gen(" + TREE_Code +" code) {"); // method start

	out.println(spec.method_prologue_stms);

	outputSelectionMethod(out);

	out.println(spec.method_epilogue_stms);

	out.println("\t}"); // end method
	out.println("}"); // end class
	out.flush();
    }

    /** Writes the Instruction Selection Method to <code>out</code>.
	<BR> <B>modifies:</B> <code>out</code>
	<BR> <B>effects:</B>
	     <BR> Generates Java source for the instruction selection
	     method, not including method signature or surrounding
	     braces.
	     <BR>Outputs generated source to <code>out</code>.

	<BR> <B>Subclass Implementation Notes:</B>
	     <BR> Generated method has one parameter available to be
	     referenced: <code>tree</code>, a <code>TreeCode</code>
	     that represents the input set of <code>Tree</code>
	     <code>HCodeElement</code>s.
	     <BR>Generated method must, for each pattern in
	     <code>this.spec</code>, define variables for the action
	     statements in the pattern to refer to.  These variables
	     are:<OL>
	     <LI>The <code>Spec.ExpId</code> objects defined in the
  	         pattern.  Note that this does not include the
		 <code>Spec.RuleExp.result_id</code> of the whole
		 Pattern; it is the responsibility of the action code
		 of the Rule to properly initialize and assign a
		 <code>Temp</code> to the name given in
		 <code>Spec.RuleExp.result_id</code>. 
	     <LI>The <code>HCodeElement</code> <code>ROOT</code>.  
	         This should be defined as the
		 <code>IR.Tree.Tree</code> element being analyzed.  To
		 reference specific fields or methods of the
		 <code>IR.Tree.Tree</code> element being analyzed, one
		 should cast <code>ROOT</code> to the appropriate
		 subclass of <code>IR.Tree.Tree</code> (though this
		 requirement may be dropped in the future).
	     </OL>
	     <BR>Generated method finds a tiling for
	     <code>tree</code>, using the information in
	     <code>this.spec</code> as a <code>Spec.Rule</code> tile
	     source, and then runs <code>Spec.Rule.action_str</code>
	     for each matching tile.
	     <BR>Awards bonus points to subclasses that implement this
	     method to output documentation for the generated method
	     body. 
	@param out Target output device for the Java source code.  */
    public abstract void outputSelectionMethod(PrintWriter out);
}



