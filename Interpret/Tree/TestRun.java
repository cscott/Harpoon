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
import harpoon.ClassFile.HField;
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
 * @version $Id: TestRun.java,v 1.1.2.3 1999-05-10 00:01:17 duncan Exp $
 */
public abstract class TestRun extends HCLibrary {
    public static void main(String args[]) {
	java.io.PrintWriter out = new java.io.PrintWriter(System.err, true);
	
	//HClass cls = HClass.forName(args[0]);
	HClass cls = HClass.forName(args[0]);
	//HMethod method = cls.getMethod("<clinit>", new HClass[] {  });

	HCodeFactory hcf = // default code factory.
	    harpoon.IR.Quads.QuadSSA.codeFactory();
	
	
	Frame frame = new DefaultFrame(new InterpreterOffsetMap(null),
				       new InterpreterAllocationStrategy());

	hcf = harpoon.IR.LowQuad.LowQuadSSA.codeFactory(hcf);
	hcf = harpoon.IR.LowQuad.LowQuadNoSSA.codeFactory(hcf);
       	hcf = harpoon.IR.Tree.TreeCode.codeFactory(hcf, frame);
	hcf = harpoon.IR.Tree.CanonicalTreeCode.codeFactory(hcf, frame);
	//hcf = harpoon.Analysis.Tree.TreeFolding.codeFactory(hcf);

	hcf = new InterpreterCachingCodeFactory(hcf);

	PrintWriter prof = null;

	String[] params = new String[args.length-1];
	System.arraycopy(args, 1, params, 0, params.length);
	harpoon.Interpret.Tree.Method.run(prof, hcf, cls, params);
	
	if (prof!=null) prof.close(); 
    }
}












