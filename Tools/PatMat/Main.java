// Main.java, created Wed Feb 17 22:05:18 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Tools.PatMat;

import harpoon.Util.Util;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;

/* Main entry point for the instruction selection tool.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Main.java,v 1.2 2002-02-25 21:08:30 cananian Exp $
 */
public class Main {
    private static boolean DEBUG_parser = false;
    private static boolean DEBUG_production = false;
    private static boolean DEBUG_maxMunch = true;


    public static void main(String args[]) throws Exception {
	/* check command-line options */
	if (args.length<1) {
	    System.err.println("Usage: java "+Main.class.getName()+" "+
                               "[spec file] {class name}");
	    System.err.println("If class name is not specified, it defaults "+
                               "to 'CodeGen'.");
	    System.exit(2);
	}
	Reader r = new BufferedReader(new FileReader(args[0]));
	ErrorMsg e = new ErrorMsg(args[0]);
	Lexer l = new Lexer(r, e);
	Parser p = new Parser(l, e);
	Spec s = (Spec) p.parse().value;
	s = CommutativityExpander.expand(s);

	if (DEBUG_parser) {
	    System.out.println(s);
	} else if (DEBUG_production) {
	    PrintWriter pw = new PrintWriter(System.out);
	    TestCGG t = new TestCGG(s);
	    t.outputJavaFile(pw);
	} else if (DEBUG_maxMunch) {
	    String classname = (args.length>1)?args[1]:"CodeGen";
	    PrintWriter pw = new PrintWriter(System.out);
	    MaximalMunchCGG t = new MaximalMunchCGG(s, classname);
	    t.outputJavaFile(pw); 
	}
	// exit with appropriate status code.
	if (e.anyErrors) System.err.println("******* ERRORS FOUND *******");
	System.exit(e.anyErrors?1:0);
    }

    static class TestCGG extends CodeGeneratorGenerator {
	TestCGG(Spec s) {
	    super(s, "TestCggClassDontUse");
	}

	public String producedClassType() {
	    return "harpoon.Backend.Generic.CodeGen";
	}

	public void outputSelectionMethod(PrintWriter out, boolean isData) {
	    out.println();
	    out.println(spec);
	    out.println();
	}
    }
 
}

