package MCC;

import java.io.*;
import java.util.*;
import MCC.IR.*;

/**
 * The main compiler module, which does the following:
 * <ul>
 *  <li>
 *   nothing.
 *  </li> 
 * <ul>
 *
 * @author <b>Daniel Roy</b> droy (at) mit (dot) edu
 * @version %I, %G 
 */

public class Compiler {
    /* Set this flag to false to turn repairs off */
    public static boolean REPAIR=true;
    public static boolean AGGRESSIVESEARCH=false;
    public static boolean PRUNEQUANTIFIERS=false;
    public static boolean GENERATEDEBUGHOOKS=false;
    public static boolean GENERATEDEBUGPRINT=false;

    public static void main(String[] args) {
        State state = null;
        boolean success = true;
        CLI cli = new CLI();
        cli.parse(args);
        printArgInfo(cli); // prints debugging information and warning

        state = new State();
        State.currentState = state;
	State.debug = cli.debug;
	State.verbose = cli.verbose;
	State.infile = cli.infile;
	State.outfile = cli.outfile;

        /*
         * added: terminates with an error message if no input file
         * specified at command line
         */

        System.out.println("MCC v0.0.1 - MIT LCS (Author: Daniel Roy, Brian Demsky)\n");

	if (cli.infile == null) {
	    System.err.println("\nError: no input file specified");
	    System.exit(-1);
	}
        
	if (state.debug) {
	    System.out.println("Compiling " + cli.infile + ".");
	}
	
	success = scan(state) || error(state, "Scanning failed, not attempting to parse.");
	success = parse(state) || error(state, "Parsing failed, not attempting semantic analysis.");
	success = semantics(state) || error(state, "Semantic analysis failed, not attempting variable initialization.");
	
	
	Termination termination=null;
	/* Check partition constraints */
	(new ImplicitSchema(state)).update();
	termination=new Termination(state);
	
	state.printall();
	(new DependencyBuilder(state)).calculate();
	
	try {
	    Vector nodes = new Vector(state.constraintnodes.values());
	    nodes.addAll(state.rulenodes.values());
	    
	    FileOutputStream dotfile;
	    dotfile = new FileOutputStream(cli.infile + ".dependencies.edgelabels.dot");
	    GraphNode.useEdgeLabels = true;
	    GraphNode.DOTVisitor.visit(dotfile, nodes);
	    dotfile.close();
	    
	    dotfile = new FileOutputStream(cli.infile + ".dependencies.dot");
	    GraphNode.useEdgeLabels = false;
	    GraphNode.DOTVisitor.visit(dotfile, nodes);                
	    dotfile.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	
	try {
	    FileOutputStream gcode = new FileOutputStream(cli.infile + ".cc");
	    
	    
	    // do model optimizations
	    //(new Optimizer(state)).optimize();
	    
	    FileOutputStream gcode2 = new FileOutputStream(cli.infile + "_aux.cc");
	    FileOutputStream gcode3 = new FileOutputStream(cli.infile + "_aux.h");
	    RepairGenerator wg = new RepairGenerator(state,termination);
	    wg.generate(gcode,gcode2,gcode3, cli.infile + "_aux.h");
	    gcode2.close();
	    gcode3.close();
	    /*		} else {
			WorklistGenerator ng = new WorklistGenerator(state);
		    SetInclusion.worklist=true;
		    RelationInclusion.worklist=true;
		    ng.generate(gcode);
		    }*/
	    gcode.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	
	if (state.debug) {
	    System.out.println("Compilation of " + state.infile + " successful.");
	    System.out.println("#SUCCESS#");
	}
    }

    private static void printArgInfo(CLI cli) {
        if (cli.debug) {
            System.out.println("Printing debugging information...");
            System.out.println("Input filename: " + cli.infile);
            System.out.println("Output filename: " + cli.outfile);

            for (int i = 0; i < cli.opts.length; i++) {
                if (cli.opts[i]) {
                    System.out.println("Optimization");
                }
            }
        }

        for (int i = 0; i < cli.extraopts.size(); i++) {
            System.err.println("Warning: optimization \"" +
                               cli.extraopts.elementAt(i) +
                               "\" not recognized");
        }

        for (int i = 0; i < cli.extras.size(); i++) {
            System.err.println("Warning: option \"" +
                               cli.extras.elementAt(i) +
                               "\" not recognized");
        }
    }

    private static boolean error(State state, String error) {
	System.err.println(error);
	if (state.debug) {
	    System.out.println("#ERROR#");
	}
	System.exit(-1);
	return false;
    }

    public static boolean semantics(State state) {
        SimpleIRErrorReporter er = new SimpleIRErrorReporter();
        SemanticChecker checker = new SemanticChecker();
	boolean ok = true;

	try {
	    ok = checker.check(state, er);
	} catch (Exception e) {
            er.report(null, e.toString());
            e.printStackTrace();
	    er.error = true;
	}

        if (!ok) {
            er.report(null, "Semantic check failed.");
        }

	System.out.print(er.toString());

	return !er.error;
    }

    public static void debugMessage(int level, String s) {
        if (State.currentState.verbose >= level) {
            System.err.println(s);
        }
    }

    public static boolean parse(State state) {
        
        /* parse structure file */
        try {
            debugMessage(1, "Parsing structure file");
            LineCount.reset();
            FileInputStream infile = new FileInputStream(state.infile + ".struct");
            TDLParser parser = new TDLParser(new Lexer(infile));
	    parser.filename = state.infile + ".struct";
            CUP$TDLParser$actions.debug = state.verbose > 1 ;
            state.ptStructures = (ParseNode) parser.parse().value;
        } catch (FileNotFoundException fnfe) {
            System.err.println("Unable to open file: " + state.infile + ".struct");
            System.exit(-1);
	} catch (Exception e) {
	    //	    System.out.println(e);
	    //	    e.printStackTrace();
	    return false;
	}

        /* parse model file */
        try {
            debugMessage(1, "Parsing model file");
            LineCount.reset();
            FileInputStream infile = new FileInputStream(state.infile + ".model");
            MDLParser parser = new MDLParser(new Lexer(infile));
	    parser.filename = state.infile + ".model";
            CUP$MDLParser$actions.debug = state.verbose > 1 ;
            state.ptModel = (ParseNode) parser.parse().value;
        } catch (FileNotFoundException fnfe) {
            System.err.println("Unable to open file: " + state.infile + ".model");
            System.exit(-1);
	} catch (Exception e) {
	    //	    System.out.println(e);
	    //	    e.printStackTrace();
	    return false;
	}

        /* parse space file */
        try {
            debugMessage(1, "Parsing space file");
            LineCount.reset();
            FileInputStream infile = new FileInputStream(state.infile + ".space");
            SDLParser parser = new SDLParser(new Lexer(infile));
	    parser.filename = state.infile + ".space";
            CUP$SDLParser$actions.debug = state.verbose > 1 ;
            state.ptSpace = (ParseNode) parser.parse().value;
        } catch (FileNotFoundException fnfe) {
            System.err.println("Unable to open file: " + state.infile + ".space");
            System.exit(-1);
	} catch (Exception e) {
	    System.out.println(e);
	    e.printStackTrace();
	    return false;
	}

        /* parse constraints file */
        try {
            debugMessage(1, "Parsing constraints file");
            LineCount.reset();
            FileInputStream infile = new FileInputStream(state.infile + ".constraints");
            CDLParser parser = new CDLParser(new Lexer(infile));
	    parser.filename = state.infile + ".constraints";
            CUP$CDLParser$actions.debug = state.verbose > 1 ;
            state.ptConstraints = (ParseNode) parser.parse().value;
        } catch (FileNotFoundException fnfe) {
            System.err.println("Unable to open file: " + state.infile + ".constraints");
            System.exit(-1);
	} catch (Exception e) {
	    //	    System.out.println(e);
	    //	    e.printStackTrace();
	    return false;
	}

        boolean success = 
            !CUP$TDLParser$actions.errors && 
            !CUP$SDLParser$actions.errors && 
            !CUP$CDLParser$actions.errors && 
            !CUP$MDLParser$actions.errors;

                
        // if verbosity is on, then output parse trees as .dot files
        if (success && state.verbose > 0) {
            try {
                FileOutputStream dotfile;

                dotfile = new FileOutputStream(state.infile + ".struct.dot");
                ParseNodeDOTVisitor.visit(dotfile, state.ptStructures);                
                dotfile.close();

                dotfile = new FileOutputStream(state.infile + ".model.dot");
                ParseNodeDOTVisitor.visit(dotfile, state.ptModel);                
                dotfile.close();

                dotfile = new FileOutputStream(state.infile + ".space.dot");
                ParseNodeDOTVisitor.visit(dotfile, state.ptSpace);                
                dotfile.close();

                dotfile = new FileOutputStream(state.infile + ".constraints.dot");
                ParseNodeDOTVisitor.visit(dotfile, state.ptConstraints);                
                dotfile.close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
            
	return success;
    }


    public static boolean scan(State state) {
        FileInputStream infile = null;
        Lexer lexer;
        boolean errors = false;
        String files[] = { new String(state.infile + ".struct"),
                           new String(state.infile + ".model"),
                           new String(state.infile + ".constraints"),
                           new String(state.infile + ".space") };



        for (int i = 0; i < files.length; i++) {

            String filename = files[i];

            try {
                infile = new FileInputStream(filename);
            } catch (FileNotFoundException fnfe) {
                System.err.println("Unable to open file: " + filename);
                System.exit(-1);
            }
            
            lexer = new Lexer(infile);

            
            try {
                while (true) {
                    java_cup.runtime.Symbol symbol;
                    
                    symbol = lexer.next_token();
                    
                    if (symbol.sym == Sym.EOF) {
                        break;
                    } else if (symbol.sym == Sym.BAD) {
                        errors = true;
                    }
                    
                    if (State.verbose > 2) {
                        System.out.println("Got token: " + symbol.value);
                    }
                }
            } catch (Exception e) {
                return false;
            }
        }

	return !errors;
    }


}

