// MaximalMunchCGG.java, created Thu Jun 24 18:07:16 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Tools.PatMat;

import java.io.PrintWriter;

/**
 * <code>MaximalMunchCGG</code> finds an optimal tiling of
 * instructions on a Tree-IR.
 *
 * This Code Generator Generator produces Code Generators that use the
 * Maximal Munch algorithm to find an optimal tiling for an input
 * tree.  See Appel "Modern Compiler Implementation in Java", Section
 * 9.1 for a description of Maximal Munch.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: MaximalMunchCGG.java,v 1.1.2.3 1999-06-25 12:13:57 pnkfelix Exp $ */
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


    /** Writes the Instruction Selection Method to <code>out</code>.
	<BR> <B>modifies:</B> <code>out</code>
	<BR> <B>effects:</B>
	     Generates Java source for a MaximalMunch instruction
	     selection method, not including method signature or
	     surrounding braces.  Outputs generated source to
	     <code>out</code>. 
	@param out Target output device for the Java source code.
    */
    public void outputSelectionMethod(PrintWriter out) { 
	// Implement a recursive function by making a helper class to
	// visit the nodes
	out.println("\tstatic final class CggVisitor extends TreeVisitor {");

	// for each rule for a statement we need to implement a
	// clause in munchStm()
	// for each rule for an exp we need to implement a
	// clause in munchExp()
	// for each rule for a move we need to implement a
	// clause in munchMove()
	
	out.println("\t}"); // end CggVisitor

	
    }
    
}
