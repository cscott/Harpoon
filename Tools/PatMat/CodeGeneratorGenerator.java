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
 * @see harpoon.Backend.Generic.CodeGen
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: CodeGeneratorGenerator.java,v 1.3 2002-02-26 22:47:11 cananian Exp $ */
public abstract class CodeGeneratorGenerator {

    private static final String TREE_TreeCode = "harpoon.IR.Tree.TreeCode";
    private static final String TREE_Code = "harpoon.IR.Tree.Code";
    private static final String TREE_Data = "harpoon.IR.Tree.Data";
    private static final String GENERIC_Code = "harpoon.Backend.Generic.Code";
    private static final String ASSEM_Instr = "harpoon.IR.Assem.Instr";
    private static final String ASSEM_InstrFactory = 
	"harpoon.IR.Assem.InstrFactory";
    private static final String UTIL_List = "java.util.List";

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
    
    /** Returns the fully qualified name of the superclass of the
	generated <code>CodeGen</code>.
	The superclass should be a subclass of
	<code>Generic.CodeGen</code> (or <code>Generic.CodeGen</code>
	itself.  
    */
    protected abstract String producedClassType();

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
	     <LI>Class name.
	         This is already defined by <code>this.className</code>.
	     <LI>Class signature.
	         This is already hardcoded as 
		 <code>public class <u>this.className</u> extends
		 harpoon.Backend.Generic.CodeGen</code>. 
	     <LI>Code-gen method signature.  This is already hardcoded as 
		 <code>public final Instr gen(harpoon.IR.Tree.Code tree,
		       harpoon.IR.Assem.InstrFactory inf)</code>. 
	     <LI>Data-gen method signature.  This is already hardcoded as 
		 <code>public final Instr gen(harpoon.IR.Tree.Data tree,
		       harpoon.IR.Assem.InstrFactory inf)</code>. 
	     </OL>
	@param out Target output device for the Java source code.
    */
    public void outputJavaFile(PrintWriter out) {
	out.println(spec.global_stms);
	out.println("public class " + className + 
		    " extends "+producedClassType()+" { ");
	out.println(spec.class_stms);
	
	out.println("\t/** Generates assembly code from a <code>" + TREE_Code + "</code>.");
	out.println("\t    <BR> <B>modifies:</B> <code>this</code>");
	out.println("\t    <BR> <B>effects:</B>");
	out.println("\t         Scans <code>tree</code> to find a tiling of ");
	out.println("\t         Instruction Patterns, calling auxillary methods");
	out.println("\t         and data structures as defined in the .spec file.");
	out.println("\t         Generates an associated <code>Derivation</code>");
	out.println("\t         object as the second element of the returned");
	out.println("\t         <code>List</code>.");
	out.println("\t    @param tree Set of abstract <code>Tree</code> instructions ");
	out.println("\t                that form the body of the procedure being compiled.");
	out.println("\t*/");
	out.println("\tpublic final "+UTIL_List+" cgg_genCode(final " + 
		    TREE_Code +" code, final " + ASSEM_InstrFactory +
		    " inf) {"); // method start

	out.println("\t_methodPrologue_(inf);");
	out.println(spec.method_prologue_stms);

	outputSelectionMethod(out, false);

	out.println(spec.method_epilogue_stms);

	out.println(returnCodeStatements());

	out.println("\t}"); // end Code-gen method

	out.println("\t/** Generates assembly code from a <code>" + TREE_Data + "</code>.");
	out.println("\t    <BR> <B>modifies:</B> <code>this</code>");
	out.println("\t    <BR> <B>effects:</B>");
	out.println("\t         Scans <code>tree</code> to define a layout of ");
	out.println("\t         Instructions, calling auxillary methods");
	out.println("\t         and data structures as defined in the .Spec file");
	out.println("\t    @param tree Set of abstract <code>Tree</code> instructions ");
	out.println("\t                that form the body of the data structure being compiled.");
	out.println("\t*/");
	out.println("\tpublic final "+ASSEM_Instr+" cgg_genData(" + 
 		    TREE_Data +" code, final " + ASSEM_InstrFactory + 
		    " inf) {"); // method start

	out.println("\t_methodPrologue_(inf);");
	out.println(spec.method_prologue_stms);

	outputSelectionMethod(out, true);

	out.println(spec.method_epilogue_stms);

	out.println(returnDataStatements());

	out.println("\t}"); // end method

	out.println("}"); // end class
	out.flush();
    }

    protected String returnCodeStatements() {
	return 
	    "\tUtil.ASSERT(first != null, \""+
	    "Should always generate some instrs\");\n"+
	    "\treturn harpoon.Util.Default.pair(first, getDerivation());";
    }

    protected String returnDataStatements() {
	return 
	    "\tUtil.ASSERT(first != null, \""+
	    "Should always generate some instrs\");\n"+
	    "\treturn first;";
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
	     referenced: <code>code</code>, a <code>Tree.Code</code>
	     that represents the input set of <code>Tree</code>
	     <code>HCodeElement</code>s.  From this the code may
	     access the <code>Frame</code>,
	     <code>TreeDerivation</code>, and
	     <code>IR.Tree.Tree</code> objects necessary to create
	     assembly code <code>Instr</code>s using the actions
	     provided by a spec file.

	     <BR>Generated method must, for each pattern in
	     <code>this.spec</code>, define variables for the action
	     statements in the pattern to refer to.  These variables
	     are:<OL>
	     <LI>The <code>Spec.ExpId</code> objects defined in the
  	         pattern, including the result_id and %extra Temp
		 objects. 
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
	@param out Target output device for the Java source code.  
	@param isData indicates if we're pattern matching code or data tables
    */
    public abstract void outputSelectionMethod(PrintWriter out, boolean isData);
}



