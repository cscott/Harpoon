// RoleInference.java, created Wed Apr 16 11:13:48 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.Analysis.Quads.QuadClassHierarchy;

import harpoon.Util.Options.Option;

import java.util.List;
import java.util.LinkedList;

/**
 * <code>RoleInference</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: RoleInference.java,v 1.1 2003-04-17 00:19:29 salcianu Exp $
 */
public class RoleInference extends CompilerStageEZ {
    
    public RoleInference() { super("role-inference"); }

    public List/*<Option>*/ getOptions() {
	List/*<Option>*/ opts = new LinkedList/*<Option>*/();
	opts.add(new Option("e", "Role Inference") {
	    public void action() { ROLE_INFER = true; }
	});
	return opts;
    }
    
    protected boolean enabled() { return ROLE_INFER; }
    private static boolean ROLE_INFER = false;
    
    protected void real_action() {
	hcf = harpoon.IR.Quads.QuadNoSSA.codeFactory(hcf);
	hcf = (new harpoon.Analysis.RoleInference.RoleInference
	       (hcf, linker)).codeFactory();
	classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
    }
    
}
