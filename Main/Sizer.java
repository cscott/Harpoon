// Sizer.java, created Sun Jul 16 21:25:17 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * <code>Sizer</code> computes the sizes of all the methods used in
 * a given program.  It is used for generating paper statistics on
 * our benchmarks.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Sizer.java,v 1.4 2002-11-27 18:34:24 salcianu Exp $
 */
public class Sizer extends harpoon.IR.Registration {

    public static void main(String[] args) {
	if (args.length!=1) {
	    System.err.println("USAGE: java harpoon.Main.Sizer <classname>");
	    System.exit(1);
	}
	Linker linker = Loader.systemLinker;
	HCodeFactory hcf = harpoon.IR.Bytecode.Code.codeFactory();
	HCodeFactory bytecode = new CachingCodeFactory(hcf) {
	    public void clear(HMethod m) {
		/* don't clear our cache, but go ahead and clear our parent */
		parent.clear(m);
	    }
	};
	hcf = new CachingCodeFactory
	    (harpoon.IR.Quads.QuadNoSSA.codeFactory(hcf));
	
	HClass root = linker.forName(args[0]);
	HMethod mainMethod = root.getMethod("main", "([Ljava/lang/String;)V");
	// any frame will do:
	Frame frame = Options.frameFromString("precisec", mainMethod);
	Set roots = new HashSet
	    (frame.getRuntime().runtimeCallableMethods());
	roots.add(mainMethod);
	ClassHierarchy ch = new QuadClassHierarchy(linker, roots, hcf);

	System.out.println("CLASSES:               " +
			   ch.classes().size());
	System.out.println("INSTANTIATED CLASSES:  " +
			   ch.instantiatedClasses().size());
	System.out.println("CALLABLE METHODS:      " +
			   ch.callableMethods().size());

	long app_bytecode_size = 0, app_quad_size = 0;
	long lib_bytecode_size = 0, lib_quad_size = 0;
	for (Iterator it=ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    HCode hcB = bytecode.convert(hm);
	    int bsize = (hcB==null) ? 0 : hcB.getElementsL().size();
	    HCode hcQ = hcf.convert(hm);
	    int qsize = (hcQ==null) ? 0 : hcQ.getElementsL().size();
	    if (hm.getDeclaringClass().getName().startsWith("java") ||
		hm.getDeclaringClass().getName().startsWith("sun")) {
		lib_bytecode_size += bsize; lib_quad_size += qsize;
	    } else {
		app_bytecode_size += bsize; app_quad_size += qsize;
	    }
	}
	System.out.println("TOTAL BYTECODE INSTRS: "+
			   (app_bytecode_size + lib_bytecode_size) +
			   "  ("+app_bytecode_size+" app / "
			        +lib_bytecode_size+" lib)");
	System.out.println("TOTAL QUAD INSTRS:     "+
			   (app_quad_size + lib_quad_size) +
			   "  ("+app_quad_size+" app / "
			        +lib_quad_size+" lib)");
    }    
}
