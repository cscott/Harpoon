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
 * @version $Id: CompilerStageEZ.java,v 1.2 2003-04-18 16:27:14 salcianu Exp $
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
	    _UNPACK_CS(cs);
	    real_action();
	    return _PACK_CS();
	}
	else return cs;
    }

    protected CompilerState old_cs;
    protected HMethod mainM;
    protected Set roots;
    protected Linker linker;
    protected HCodeFactory hcf;
    protected ClassHierarchy classHierarchy;
    protected Frame frame;

    protected final void _UNPACK_CS(CompilerState cs) {
	this.old_cs = cs;
	this.mainM = cs.getMain();
	this.roots = cs.getRoots();
	this.linker = cs.getLinker();
	this.hcf = cs.getCodeFactory();
	this.classHierarchy = cs.getClassHierarchy();
	this.frame = cs.getFrame();
    }

    protected final CompilerState _PACK_CS() {
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
