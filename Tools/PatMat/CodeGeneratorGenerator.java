// CodeGeneratorGenerator.java, created Thu Jun 24 20:08:33 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Tools.PatMat;

import java.io.Writer;
import java.io.IOException;

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
 * @version $Id: CodeGeneratorGenerator.java,v 1.1.2.2 1999-06-25 04:25:14 pnkfelix Exp $ */
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
	@see <A HREF="http://palmpilot.lcs.mit.edu/~pnkfelix/instr-selection-tool.html">Standard Specification Template</A>
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
	     Then writes the Java source code out to <code>out</code>.
	@param out Target output device for the Java source code.
	@exception IOException If there is an error writing to <code>out</code>
    */
    public void outputJavaFile(Writer out) throws IOException {
	out.write(spec.global_stms);

	out.write("public class " + className + " { ");

	out.write(spec.class_stms);

	outputSelectionMethod(out);

	out.write("}");
	
	out.flush();
	
    }

    /** Writes the Instruction Selection Method to <code>out</code>.
	<BR> <B>modifies:</B> <code>out</code>
	<BR> <B>effects:</B>
	     Generates Java source for the instruction selection
	     method, including method signature.  Outputs generated
	     source to <code>out</code>.
	@param out Target output device for the Java source code.
	@exception IOException If there is an error writing to <code>out</code>
    */
    public abstract void outputSelectionMethod(Writer out) throws IOException; 
}



