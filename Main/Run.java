// Run.java, created Mon Dec 28 02:34:11 1998 by cananian
package harpoon.Main;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Interpret.Quads.Method;
import harpoon.IR.Quads.QuadWithTry;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

/**
 * <code>Run</code> invokes the interpreter.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Run.java,v 1.1.2.6 1999-02-09 03:59:30 cananian Exp $
 */
public abstract class Run extends harpoon.IR.Registration {
    public static void main(String args[]) {
	HCodeFactory hf = // default code factory.
	    harpoon.Analysis.QuadSSA.SCC.SCCOptimize.codeFactory
	    (harpoon.IR.Quads.QuadSSA.codeFactory());
	int i=0; // count # of args/flags processed.
	// check for "-prof" and "-code" flags in arg[i]
	PrintWriter prof = null;
	for (; i < args.length ; i++) {
	    if (args[i].startsWith("-code")) {
		if (++i < args.length)
		    hf = HMethod.getCodeFactory(args[i]);
		else throw new Error("-code option needs codename");
	    } else if (args[i].startsWith("-prof")) {
		String filename = "./java.prof";
		if (args[i].startsWith("-prof:"))
		    filename = args[i].substring(6);
		try {
		    FileOutputStream fos = new FileOutputStream(filename);
		    prof = new PrintWriter(new GZIPOutputStream(fos));
		} catch (IOException e) {
		    throw new Error("Could not open " + filename +
				    " for profiling: "+ e.toString());
		}
	    } else break; // no more command-line options.
	}
	// arg[i] is class name.  Load its main method.
	if (args.length < i) throw new Error("No class name.");
	HClass cls = HClass.forName(args[i]);
	i++;
	// construct caching code factory.
	hf = new CachingCodeFactory(hf);
	//////////// now call interpreter with truncated args list.
	String[] params = new String[args.length-i];
	System.arraycopy(args, i, params, 0, params.length);
	harpoon.Interpret.Quads.Method.run(prof, hf, cls, params);
	if (prof!=null) prof.close();
    }
}
