// Run.java, created Mon Dec 28 02:34:11 1998 by cananian
package harpoon.Main;

import harpoon.ClassFile.*;
import harpoon.Interpret.Quads.Method;

/**
 * <code>Run</code> invokes the interpreter.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Run.java,v 1.1.2.1 1998-12-28 23:43:22 cananian Exp $
 */

public abstract class Run extends harpoon.IR.Registration {
    public static final String codename="quad-no-ssa";
    public static void main(String args[]) {
	// arg[0] is class name.  Load it's main method.
	if (args.length < 1) throw new Error("No class name.");
	HClass cls = HClass.forName(args[0]);
	HCodeFactory hf = new HCodeFactory() {
	    public HCode convert(HMethod m) { return m.getCode(codename); }
	    public String getCodeName() { return codename; }
	};
	String[] params = new String[args.length-1];
	System.arraycopy(args, 1, params, 0, params.length);
	harpoon.Interpret.Quads.Method.run(hf, cls, params);
    }
}
