// Run.java, created Mon Dec 28 02:34:11 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Backend.Analysis.DisplayInfo.HClassInfo;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.OffsetMap32;
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
 * @version $Id: TestRun.java,v 1.1.2.16 1999-10-15 18:25:39 cananian Exp $
 */
public abstract class TestRun extends HCLibrary {
    public static void main(String args[]) {
	java.io.PrintWriter out = new java.io.PrintWriter(System.err, true);
	
	HCodeFactory hcf = // default code factory.
	    new harpoon.ClassFile.CachingCodeFactory(
	    harpoon.IR.Quads.QuadWithTry.codeFactory()
	    );
	HCodeFactory hcfOpt;

	// Cache all conversions we make in the ClassHierarchy
	hcf = new CachingCodeFactory(hcf); 
	
	HClass cls = HClass.forName(args[0]);
	System.err.println("Collecting class hierarchy information...");
	HMethod main = cls.getMethod("main", new HClass[] { HCstringA });
	ClassHierarchy ch = new QuadClassHierarchy(main, hcf);
	System.err.println("done!");
	
	//	Frame frame = new DefaultFrame(new InterpreterOffsetMap(ch),
	//		       new InterpreterAllocationStrategy());
	
	Frame frame = new  DefaultFrame(main, ch, new OffsetMap32(ch),
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












