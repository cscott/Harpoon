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
 * tiles and constructs a java source file for a new
 * <code>HCodeFactory</code> which will convert a <code>TreeCode</code>
 * into an <code>HCode</code> in
 * <code>Instr</code> form.  In short, a CGG generates a Code
 * Generator.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: CodeGeneratorGenerator.java,v 1.1.2.3 1999-06-25 11:17:30 pnkfelix Exp $ */
public abstract class CodeGeneratorGenerator {

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
	@param out Target output device for the Java source code.
    */
    public void outputJavaFile(PrintWriter out) {
	out.println(spec.global_stms);
	out.println("public class " + className + " { ");
	out.println(spec.class_stms);
	
	out.println("\t/** Generates assembly code from a <code>TreeCode</code>.");
	out.println("\t    <BR> <B>modifies:</B> <code>this</code>");
	out.println("\t    <BR> <B>effects:</B>");
	out.println("\t         Scans <code>tree</code> to find a tiling of ");
	out.println("\t         Instruction Patterns, calling auxillary methods");
	out.println("\t         and data structures as defined in the .Spec file");
	out.println("\t    @param tree Set of abstract <code>Tree</code> instructions ");
	out.println("\t                that form the body of the procedure being compiled.");
	out.println("\t*/");
	out.println("\tpublic final void codegen(TreeCode tree) {"); // method start

	outputSelectionMethod(out);

	out.println("\t}"); // end method
	out.println("}"); // end class
	out.flush();
    }

    /** Writes the Instruction Selection Method to <code>out</code>.
	<BR> <B>modifies:</B> <code>out</code>
	<BR> <B>effects:</B>
	     Generates Java source for the instruction selection
	     method, not including method signature or surrounding
	     braces.  
	     
	     <BR>Generated method has one parameter available to be
	     referenced: <code>tree</code>, a <code>TreeCode</code>
	     that represents the input set of <code>Tree</code>
	     <code>HCodeElement</code>s.

	     <BR>Generated method finds a tiling for
	     <code>tree</code>, using the information in
	     <code>this.spec</code> as a <code>Spec.Rule</code> tile
	     source, and then runs <code>Spec.Rule.action_str</code>
	     for each matching tile.

	     <BR>Outputs generated source to <code>out</code>.

	     <BR>Awards bonus points to subclasses that implement this
	     method to output documentation for the generated method
	     body. 
	@param out Target output device for the Java source code.  */
    public abstract void outputSelectionMethod(PrintWriter out);
}



