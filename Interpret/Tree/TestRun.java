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
 * @version $Id: TestRun.java,v 1.1.2.1 1999-03-27 22:05:10 duncan Exp $
 */
public abstract class TestRun {
    public static void main(String args[]) {
      
      // Test HClassInfo

      /*
	HClassInfo hci = new HClassInfo();
	HClass hvec = HClass.forName("java.util.Hashtable");
	HField hf[] = hvec.getDeclaredFields();
	HMethod hm[] = hvec.getDeclaredMethods();
	
	for (int i=0; i<hf.length; i++) {
	System.err.print(hf[i].isStatic()?"STATIC_":"NON_STATIC_");
	System.err.println("FIELD: " + hf[i] + ", " + hci.getFieldOffset(hf[i]));
	}
	for (int i=0; i<hm.length; i++) {
	System.err.print(hm[i].isStatic()?"STATIC_":"NON_STATIC_");
	System.err.println("METHOD: " + hm[i] + ", " + hci.getMethodOffset(hm[i]));
	}
      */
        java.io.PrintWriter out = new java.io.PrintWriter(System.err, true);

	HCodeFactory hcf = // default code factory.
	    harpoon.IR.Quads.QuadSSA.codeFactory();

	int i=0; // count # of args/flags processed.
	// check for "-prof" and "-code" flags in arg[i]

	HClass cls = HClass.forName(args[0]);
	
	/*	Frame frame =new DefaultFrame
	  (new InterpreterOffsetMap
	   (new ClassHierarchy(cls.getMethod
			       ("main", new HClass[] { 
				 HClass.forName("[Ljava/lang/String;") }),
			       hcf)),
	   new InterpreterAllocationStrategy());
	*/
	Frame frame = new DefaultFrame(new InterpreterOffsetMap(null),
				       new InterpreterAllocationStrategy());

	//	hcf.convert(HClass.forName("java.util.Properties").getMethod("getProperty", new HClass[] { HClass.forName("java.lang.String") })).print(out);

	hcf = harpoon.IR.LowQuad.LowQuadSSA.codeFactory(hcf);

	//	hcf.convert(HClass.forName("java.util.Properties").getMethod("getProperty", new HClass[] { HClass.forName("java.lang.String") })).print(out);

	hcf = harpoon.IR.LowQuad.LowQuadNoSSA.codeFactory(hcf);
	//	hcf.convert(HClass.forName("java.util.Properties").getMethod("getProperty", new HClass[] { HClass.forName("java.lang.String") })).print(out);

       	hcf = harpoon.IR.Tree.TreeCode.codeFactory(hcf, frame);

	//	hcf.convert(HClass.forName("java.util.Properties").getMethod("getProperty", new HClass[] { HClass.forName("java.lang.String") })).print(out);

	hcf = new CachingCodeFactory(hcf);

	PrintWriter prof = null;



	// construct caching code factory.
	//hcf = new CachingCodeFactory(hcf);
	//////////// now call interpreter with truncated args list.
	String[] params = new String[args.length-1];
	System.arraycopy(args, 1, params, 0, params.length);
	harpoon.Interpret.Tree.Method.run
	  (prof, hcf, cls, params);

	if (prof!=null) prof.close();
    }
}








