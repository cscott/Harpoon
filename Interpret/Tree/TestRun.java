// Run.java, created Mon Dec 28 02:34:11 1998 by cananian
package harpoon.Interpret.Tree;

import harpoon.Analysis.QuadSSA.ClassHierarchy;
import harpoon.Backend.Analysis.DisplayInfo.HClassInfo;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.DefaultFrame;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HData;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Interpret.Quads.Method;
import harpoon.IR.Quads.QuadWithTry;
import harpoon.IR.Tree.Data;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

/**
 * <code>Run</code> invokes the interpreter.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TestRun.java,v 1.1.2.5 1999-08-03 22:20:03 duncan Exp $
 */
public abstract class TestRun extends HCLibrary {
    public static void main(String args[]) {
	java.io.PrintWriter out = new java.io.PrintWriter(System.err, true);
	
	HCodeFactory hcf = // default code factory.
	    harpoon.IR.Quads.QuadSSA.codeFactory();
	HCodeFactory hcfOpt;

	// Cache all conversions we make in the ClassHierarchy
	hcf = new CachingCodeFactory(hcf); 
	
	HClass cls = HClass.forName(args[0]);
	System.err.println("Collecting class hierarchy information...");
	ClassHierarchy ch = new ClassHierarchy
	    (cls.getMethod("main", new HClass[] { HCstringA }), hcf);
	System.err.println("done!");
	
	Frame frame = new DefaultFrame(new InterpreterOffsetMap(ch),
				       new InterpreterAllocationStrategy());
	
	hcf = harpoon.IR.LowQuad.LowQuadSSA.codeFactory(hcf);
	hcf = harpoon.IR.LowQuad.LowQuadNoSSA.codeFactory(hcf);
       	hcf = harpoon.IR.Tree.TreeCode.codeFactory(hcf, frame);
	hcf = harpoon.IR.Tree.CanonicalTreeCode.codeFactory(hcf, frame);
	hcfOpt = harpoon.IR.Tree.OptimizedTreeCode.codeFactory(hcf, frame);

	hcf = new InterpreterCachingCodeFactory(hcf, hcf);
	PrintWriter prof = null;	
	String[] params = new String[args.length-1];
	System.arraycopy(args, 1, params, 0, params.length);
	harpoon.Interpret.Tree.Method.run(prof, hcf, cls, params);
	
	if (prof!=null) prof.close();
    }
}












