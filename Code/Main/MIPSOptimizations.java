// MIPSOptimizations.java, created Sat Apr 12 15:39:22 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.Analysis.Quads.CallGraphImpl;

import harpoon.Backend.Backend;

/**
 * <code>MIPSOptimizations</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: MIPSOptimizations.java,v 1.1 2003-04-17 00:19:29 salcianu Exp $
 */
public abstract class MIPSOptimizations {
    
    private static boolean enabled() {
	return 
	    (SAMain.BACKEND == Backend.MIPSDA) ||
	    (SAMain.BACKEND == Backend.MIPSYP);
    }
	
    public static class QuadPass extends CompilerStageEZ {
	public QuadPass() { super("mips-optimizations.quad-pass"); }
	public boolean enabled() { return MIPSOptimizations.enabled(); }

	public void real_action() {
	    hcf = new harpoon.Analysis.Quads.ArrayUnroller(hcf).codeFactory();
	    /*
	      hcf = new harpoon.Analysis.Quads.DispatchTreeTransformation
	      (hcf, classHierarchy).codeFactory();
	    */
	    hcf = harpoon.IR.Quads.QuadSSA.codeFactory(hcf);
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    hcf = new harpoon.Analysis.Quads.SmallMethodInliner
		(hcf, classHierarchy);
	    hcf = harpoon.IR.Quads.QuadSSI.codeFactory(hcf);
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    hcf = new harpoon.Analysis.Quads.MemoryOptimization
		(hcf, classHierarchy, new CallGraphImpl(classHierarchy, hcf))
		.codeFactory();
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	}
    };
    

    public static class TreePass extends CompilerStageEZ {
	public TreePass() { super("mips-optimizations.tree-pass"); }
	public boolean enabled() { return MIPSOptimizations.enabled(); }

	public void real_action() {
	    // unknown if this is strictly needed
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    hcf = harpoon.Analysis.Tree.MemHoisting.codeFactory(hcf);
	    hcf = new harpoon.Analysis.Tree.DominatingMemoryAccess
		(hcf, frame, classHierarchy).codeFactory();
	}
    };
}
