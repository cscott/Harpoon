// CompilerStateEZ.java, created Sat Apr 12 12:56:27 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.Backend.Generic.Frame;
import harpoon.Analysis.ClassHierarchy;

import java.util.Set;
import java.util.Collections;
import java.util.List;

/**
 * <code>CompilerStageEZ</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: CompilerStageEZ.java,v 1.1 2003-04-17 00:16:55 salcianu Exp $
 */
public abstract class CompilerStageEZ extends CompilerStage {

    /** Creates a <code>CompilerStageEZ</code>. */
    public CompilerStageEZ(String name) { super(name); }
    
    public List/*<Option>*/ getOptions() {
	return Collections.EMPTY_LIST; // no options by default
    }


    protected abstract void real_action();
    protected abstract boolean enabled();

    public final CompilerState action(CompilerState cs) {
	if(enabled()) {
	    UNPACK(cs);
	    real_action();
	    return PACK(cs);
	}
	else return cs;
    }

    protected HMethod mainM;
    protected Set roots;
    protected Linker linker;
    protected HCodeFactory hcf;
    protected ClassHierarchy classHierarchy;
    protected Frame frame;

    private final void UNPACK(CompilerState cs) {
	this.mainM = cs.getMain();
	this.roots = cs.getRoots();
	this.linker = cs.getLinker();
	this.hcf = cs.getCodeFactory();
	this.classHierarchy = cs.getClassHierarchy();
	this.frame = cs.getFrame();
    }

    private final CompilerState PACK(CompilerState old_cs) {
	CompilerState new_cs = 
	    old_cs
	    .changeMain(mainM)
	    .changeRoots(roots)
	    .changeLinker(linker)
	    .changeCodeFactory(hcf)
	    .changeClassHierarchy(classHierarchy)
	    .changeFrame(frame);

	mainM = null;
	roots = null;
	linker = null;
	hcf = null;
	classHierarchy = null;
	frame = null;

	return new_cs;
    }
}
