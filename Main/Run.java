// Run.java, created Mon Dec 28 02:34:11 1998 by cananian
package harpoon.Main;

import harpoon.ClassFile.*;
import harpoon.Interpret.Quads.Method;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;
/**
 * <code>Run</code> invokes the interpreter.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Run.java,v 1.1.2.3 1999-01-03 03:01:44 cananian Exp $
 */

public abstract class Run extends harpoon.IR.Registration {
    public static final String codename="quad-with-try";
    public static void main(String args[]) {
	int i=0; // count # of args/flags processed.
	// check for "-prof" flag in arg[i]
	PrintWriter prof = null;
	if (args.length > i && args[i].startsWith("-prof")) {
	    String filename = "./java.prof";
	    if (args[i].startsWith("-prof:"))
		filename = args[i].substring(6);
	    try {
		FileOutputStream fos = new FileOutputStream(filename);
		prof = new PrintWriter(new GZIPOutputStream(fos));
	    } catch (IOException e) {
		throw new Error("Could not open "+filename+" for profiling: "+
				e.toString());
	    }
	    i++;
	}
	// arg[i] is class name.  Load its main method.
	if (args.length < i) throw new Error("No class name.");
	HClass cls = HClass.forName(args[i]);
	HCodeFactory hf = new HCodeFactory() {
	    public HCode convert(HMethod m) { return m.getCode(codename); }
	    public String getCodeName() { return codename; }
	};
	i++;
	//////////// now call interpreter with truncated args list.
	String[] params = new String[args.length-i];
	System.arraycopy(args, i, params, 0, params.length);
	harpoon.Interpret.Quads.Method.run(prof, hf, cls, params);
	if (prof!=null) prof.close();
    }
}
