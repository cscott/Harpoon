package MCC;

import java.util.Vector;
import java.util.StringTokenizer;

/**
 * A generic command-line interface for 6.035 compilers.  This class
 * provides command-line parsing for student projects.  It recognizes
 * the required <tt>-target</tt>, <tt>-debug</tt>, <tt>-opt</tt>, and
 * <tt>-o</tt> switches, and generates a name for input and output
 * files.
 *
 * @author  le01, 6.035 Staff (<tt>6.035-staff@mit.edu</tt>)
 * @version <tt>$Id: CLI.java,v 1.1 2003-07-07 16:13:33 droy Exp $</tt>
 */
public class CLI {
    /**
     * Target value indicating that the compiler should produce its
     * default output.
     */
    public static final int DEFAULT = 0;

    /**
     * Target value indicating that the compiler should scan the input
     * and stop.
     */
    public static final int SCAN = 1;

    /**
     * Target value indicating that the compiler should scan and parse
     * its input, and stop.
     */
    public static final int PARSE = 2;

    /**
     * Target value indicating that the compiler should produce a
     * high-level intermediate representation from its input, and stop.
     * This is not one of the segment targets for Fall 2000, but you
     * may wish to use it for your own purposes.
     */
    public static final int INTER = 3;

    /**
     * Target value indicating that the compiler should produce a
     * low-level intermediate representation from its input, and stop.
     */
    public static final int LOWIR = 4;

    /**
     * Target value indicating that the compiler should produce
     * assembly from its input.
     */
    public static final int ASSEMBLY = 5;

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
     * The target stage.  This should be one of the integer constants
     * defined elsewhere in this package.
     */
    public int target;

    /**
     * The debug flag.  This is true if <tt>-debug</tt> was passed on
     * the command line, requesting debugging output.
     */
    public boolean debug;

    /**
     * Native MIPS architecture is specified by "-native".  The default
     * is SPIM.
     */
    public boolean fNative;

    /**
     * Runs IRVis on final node tree.
     */
    public boolean fVis;
    public String visClass;
    public String visMethod;

    /**
     * Dumps the before and after Node structure to two files that can be diffed.
     */
    public boolean fDiff;
    public String diffFile;

    /**
     * Maximum optimization iterations.
     */
    public int numIterations = 5;

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
        target = DEFAULT;
        extras = new Vector();
        extraopts = new Vector();
        fNative = false;
        fVis = false;
        verbose = 0;
        visClass = "";
        visMethod = "";
        fDiff = false;
        diffFile = "";
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

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-debug")) {
                context = 0;
                debug = true;
	    } else if (args[i].equals("-native")) {
                context = 0;
                fNative = true;
            } else if (args[i].equals("-vis")) {
                context = 4;
                fVis = true;
            } else if (args[i].equals("-diff")) {
                context = 5;
                fDiff = true;
            } else if (args[i].equals("-i")) {
                context = 6;
            } else if (args[i].equals("-verbose") || args[i].equals("-v")) {
                context = 0;
                verbose++;
            } else if (args[i].equals("-opt"))
                context = 1;
            else if (args[i].equals("-o"))
                context = 2;
            else if (args[i].equals("-target"))
                context = 3;
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
	    }
            else if (context == 2) {
                outfile = args[i];
                context = 0;
	    }
            else if (context == 3) {
                // Process case insensitive.
                String argSansCase = args[i].toLowerCase();
                // accept "scan" and "scanner" due to handout mistake
                if (argSansCase.equals("scan") || 
                    argSansCase.equals("scanner"))
                    target = SCAN;
                else if (argSansCase.equals("parse"))
                    target = PARSE;
                else if (argSansCase.equals("inter"))
                    target = INTER;
                else if (argSansCase.equals("lowir"))
                    target = LOWIR;
                else if (argSansCase.equals("assembly") ||
                         argSansCase.equals("codegen"))
                    target = ASSEMBLY;
                else
                    target = DEFAULT; // Anything else is just default
                context = 0;
	    } else if (context == 4) { // -vis
                StringTokenizer st = new StringTokenizer(args[i], ".");
                visClass = st.nextToken();
                visMethod = st.nextToken();
                context = 0;
	    } else if (context == 5) { // -diff
                diffFile = args[i]; // argument following is filename
                context = 0;
	    } else if (context == 6) { // -i <iterations>
                numIterations = Integer.parseInt(args[i]);
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

        // create outfile name
        switch (target) {
        case SCAN:
            ext = ".scan";
            break;
        case PARSE:
            ext = ".parse";
            break;
        case INTER:
            ext = ".ir";
            break;
        case LOWIR:
            ext = ".lowir";
            break;
        case ASSEMBLY:
            ext = ".s";
            break;
        case DEFAULT:
        default:
            ext = ".out";
            break;
        }

        if (outfile == null && infile != null) {
            int dot = infile.lastIndexOf('.');
            int slash = infile.lastIndexOf('/');
            // Last dot comes after last slash means that the file
            // has an extention.  Note that the base case where dot
            // or slash are -1 also work.
            if (dot <= slash)
                outfile = infile + ext;
            else
                outfile = infile.substring(0, dot) + ext;
	}
    }
}
