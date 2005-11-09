package MCC;

import java.util.*;
import MCC.IR.DebugItem;
import MCC.IR.RepairGenerator;

/**
 * A generic command-line interface for 6.035 compilers.  This class
 * provides command-line parsing for student projects.  It recognizes
 * the required <tt>-target</tt>, <tt>-debug</tt>, <tt>-opt</tt>, and
 * <tt>-o</tt> switches, and generates a name for input and output
 * files.
 *
 * @author  le01, 6.035 Staff (<tt>6.035-staff@mit.edu</tt>)
 * @version <tt>$Id: CLI.java,v 1.20 2005-11-09 16:47:42 bdemsky Exp $</tt>
 */
public class CLI {
    /**
     * Array indicating which optimizations should be performed.  If
     * a particular element is true, it indicates that the optimization
     * named in the optnames[] parameter to parse with the same index
     * should be performed.
     */
    public boolean opts[];

    /**
     * Vector of String containing the command-line arguments which could
     * not otherwise be parsed.
     */
    public Vector extras;

    /**
     * Vector of String containing the optimizations which could not be
     * parsed.  It is okay to complain about anything in this list, even
     * without the <tt>-debug</tt> flag.
     */
    public Vector extraopts;

    /**
     * Name of the file to put the output in.
     */
    public String outfile;

    /**
     * Name of the file to get input from.  This is null if the user didn't
     * provide a file name.
     */
    public String infile;

    /**
     * The debug flag.  This is true if <tt>-debug</tt> was passed on
     * the command line, requesting debugging output.
     */
    public boolean debug;

    /**
     * Verbose output
     */
    public int verbose;

    /**
     * Public constructor.  Sets up default values for all of the
     * result fields.  Specifically, sets the input and output files
     * to null, the target to DEFAULT, and the extras and extraopts
     * arrays to new empty Vectors.
     */
    public CLI() {
        outfile = null;
        infile = null;
        extras = new Vector();
        extraopts = new Vector();
        verbose = 0;
    }

    /**
     * Parse the command-line arguments.  Sets all of the result fields
     * accordingly. <BR>
     * <ul>
     * <li><TT>-target <I>target</I></TT> sets the CLI.target field based
     * on the <I>target</I> specified. </li>
     * <li><TT>scan</TT> or <TT>scanner</TT> specifies CLI.SCAN</li>
     * <li><TT>parse</TT> specifies CLI.PARSE</li>
     * <li><TT>inter</TT> specifies CLI.INTER</li>
     * <li><TT>lowir</TT> specifies CLI.LOWIR</li>
     * <TT>assembly</TT> or <TT>codegen</TT> specifies CLI.ASSEMBLY</li>
     * </ul>
     * The boolean array opts[] indicates which, if any, of the
     * optimizations in optnames[] should be performed; these arrays
     * are in the same order.
     *
     * @param args Array of arguments passed in to the program's Main
     *   function.
     * @param optnames Ordered array of recognized optimization names.  */
    public void parse(String args[]) {

        String optnames[] = {};
        int context = 0;
        String ext = ".out";

        opts = new boolean[optnames.length];

	if (args.length==0) {
	    System.out.println("-debugcompiler -- print out debug messages");
	    System.out.println("-depth depthnum constraintnum -- generate dependency graph from constraintnum with depth of depthnum");
	    System.out.println("-depthconj depthnum constraintnum conjunctionnum -- generate dependency graph from constraintnum with depth of depthnum");
	    System.out.println("-instrument -- generate instrumentation code");
	    System.out.println("-aggressivesearch -- search for one repair per constraint");
	    System.out.println("-prunequantifiernodes -- prune nodes that satisfy constraint by decreasing scope");
	    System.out.println("-cplusplus -- properly set up c++ classes");
	    System.out.println("-time -- generate timing code");
	    System.out.println("-omitcomp -- omit compensation updates");
	    System.out.println("-mergenodes -- omit nodes for simpler role dependence graphs");
	    System.out.println("-debuggraph -- add edge labels and support to debug graph");
	    System.out.println("-rejectlengthchanges -- reject all updates which change the length of an array");
	    System.out.println("-printrepairs -- print log of repair actions");
	    System.out.println("-exactallocation -- application calls malloc for each struct and");
	    System.out.println("                    allocates exactly the right amount of space.");
	    System.out.println("-name -- set name");
	    System.exit(-1);
	}

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-debugcompiler")) {
                context = 0;
                debug = true;
	    } else if (args[i].equals("-checkonly")) {
                Compiler.REPAIR=false;
	    } else if (args[i].equals("-exactallocation")) {
                Compiler.EXACTALLOCATION=true;
	    } else if (args[i].equals("-omitcomp")) {
                Compiler.OMITCOMP=true;
	    } else if (args[i].equals("-debuggraph")) {
                Compiler.DEBUGGRAPH=true;
	    } else if (args[i].equals("-mergenodes")) {
                Compiler.MERGENODES=true;
	    } else if (args[i].equals("-printrepairs")) {
                Compiler.PRINTREPAIRS=true;
	    } else if (args[i].equals("-depth")) {
		Compiler.debuggraphs.add(new DebugItem(Integer.parseInt(args[i+1]),Integer.parseInt(args[i+2])));
		i+=2;
	    } else if (args[i].equals("-depthconj")) {
		Compiler.debuggraphs.add(new DebugItem(Integer.parseInt(args[i+1]),Integer.parseInt(args[i+2]),Integer.parseInt(args[i+3])));
		i+=3;
            } else if (args[i].equals("-rejectlengthchanges")) {
                Compiler.REJECTLENGTH=true;
	    } else if (args[i].equals("-debug")) {
                Compiler.GENERATEDEBUGHOOKS=true;
	    } else if (args[i].equals("-time")) {
                Compiler.TIME=true;
	    } else if (args[i].equals("-instrument")) {
                Compiler.GENERATEINSTRUMENT=true;
	    } else if (args[i].equals("-aggressivesearch")) {
                Compiler.AGGRESSIVESEARCH=true;
	    } else if (args[i].equals("-prunequantifiernodes")) {
                Compiler.PRUNEQUANTIFIERS=true;
	    } else if (args[i].equals("-cplusplus")) {
                Compiler.ALLOCATECPLUSPLUS=true;
	    } else if (args[i].equals("-name")) {
		i++;
                RepairGenerator.name=args[i];
		RepairGenerator.postfix=args[i];
            } else if (args[i].equals("-verbose") || args[i].equals("-v")) {
                context = 0;
                verbose++;
            } else if (args[i].equals("-opt"))
                context = 1;
            else if (args[i].equals("-o"))
                context = 2;
            else if (context == 1) {
                boolean hit = false;
                for (int j = 0; j < optnames.length; j++) {
                    if (args[i].equals("all") ||
                        (args[i].equals(optnames[j]))) {
                        hit = true;
                        opts[j] = true;
		    }
                    if (args[i].equals("-" + optnames[j])) {
                        hit = true;
                        opts[j] = false;
		    }
		}
                if (!hit)
                    extraopts.addElement(args[i]);
	    } else if (context == 2) {
                outfile = args[i];
                context = 0;
            } else {
		boolean hit = false;
		for (int j = 0; j < optnames.length; j++) {
		    if (args[i].equals("-" + optnames[j])) {
			hit = true;
			opts[j] = true;
		    }
		}
		if (!hit) {
		    extras.addElement(args[i]);
		}
            }
	}

        // grab infile and lose extra args
        int i = 0;
        while (infile == null && i < extras.size()) {
            String fn = (String) extras.elementAt(i);

            if (fn.charAt(0) != '-')
	    {
                infile = fn;
                extras.removeElementAt(i);
	    }
            i++;
	}
    }
}
