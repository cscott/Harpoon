// Transactions.java, created Sat Apr 12 15:17:41 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;


import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Analysis.Transactions.SyncTransformer;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HData;
import harpoon.Util.Options.Option;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;


/**
 * <code>Transactions</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: Transactions.java,v 1.3 2003-07-21 21:21:55 cananian Exp $
 */
public abstract class Transactions {
    
    static boolean DO_TRANSACTIONS = false;
    private static SyncTransformer syncTransformer = null;

    private static boolean enabled() { return DO_TRANSACTIONS; }

    public static class QuadPass extends CompilerStageEZ {
	public QuadPass() { super("transactions.quad-pass"); }

	public List<Option> getOptions() {
	    return Arrays.asList(new Option[] {
		new Option("T", "Transactions support (CSA)") {
		    public void action() { DO_TRANSACTIONS = true; }
		}
	    });
	}

	public boolean enabled() { return Transactions.enabled(); }
	
	public void real_action() {
	    if(!DO_TRANSACTIONS) return;
	    String resource = frame.getRuntime().resourcePath
		("transact-root.properties");
	    hcf = harpoon.IR.Quads.QuadSSI.codeFactory(hcf);
	    hcf = new harpoon.Analysis.Transactions.ArrayCopyImplementer
		(hcf, linker);
	    hcf = new harpoon.Analysis.Transactions.CloneImplementer
		(hcf, linker, classHierarchy.classes());
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	    
	    hcf = new harpoon.Analysis.Quads.ArrayInitRemover(hcf)
		.codeFactory();
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    
	    syncTransformer = new SyncTransformer
		(hcf, classHierarchy, linker, mainM, roots, resource);
	    roots.addAll(syncTransformer.transRoots());
	    hcf = syncTransformer.codeFactory();
	    hcf = harpoon.Analysis.Counters.CounterFactory
		.codeFactory(hcf, linker, mainM);
	    hcf = new harpoon.ClassFile.CachingCodeFactory(hcf);
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	    // config checking
	    frame.getRuntime().configurationSet.add
		("check_with_transactions_needed");   
	}
    }


    public static class TreePass extends CompilerStageEZ {
	public TreePass() { super("transactions.tree-pass"); }
	public boolean enabled() { return Transactions.enabled(); }
	
	public void real_action() {
	    // just to be safe, we'll do the algebraic simplication before
	    // the pattern-matching in SyncTransformer occurs.
	    hcf = 
		harpoon.Analysis.Tree.AlgebraicSimplification.codeFactory(hcf);
	    hcf = syncTransformer.treeCodeFactory(frame, hcf);
	}
    }

    public static Iterator<HData> filterData(Frame f, Iterator<HData> it) {
	return syncTransformer.filterData(f, it);
    }
}
