// DynamicSyncRemoval.java, created Sat Apr 12 15:17:41 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Util.Options.Option;

import java.util.Arrays;
import java.util.List;

/**
 * <code>DynamicSyncRemoval</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DynamicSyncRemoval.java,v 1.1 2003-07-09 22:28:51 cananian Exp $
 */
public abstract class DynamicSyncRemoval {
    
    private static boolean DO_DYN_SYNC_REMOVE = false;

    private static boolean enabled() { return DO_DYN_SYNC_REMOVE; }

    public static class QuadPass extends CompilerStageEZ {
	public QuadPass() { super("dynamic-sync-removal.quad-pass"); }

	public List<Option> getOptions() {
	    List<Option> opts = Arrays.asList
		(new Option[] {
		    new Option("dyn-sync",
			       "Dynamic sync removal support (CSA)") {
			public void action() { DO_DYN_SYNC_REMOVE = true; }
		    },
		});
	    return opts;
	}

	public boolean enabled() { return DynamicSyncRemoval.enabled(); }
	
	public void real_action() {
	    hcf = new harpoon.Analysis.DynamicSyncRemoval.SyncRemover
		(hcf, linker).codeFactory();
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	    // config checking
	    frame.getRuntime().configurationSet.add
		("check_with_dynamic_sync_removal_needed");   
	}
    }


    public static class TreePass extends CompilerStageEZ {
	public TreePass() { super("dynamic-sync-removal.tree-pass"); }
	public boolean enabled() { return DynamicSyncRemoval.enabled(); }
	
	public void real_action() {
	    hcf = harpoon.Analysis.DynamicSyncRemoval.SyncRemover
		.treeCodeFactory(frame, hcf);
	}
    }
}
