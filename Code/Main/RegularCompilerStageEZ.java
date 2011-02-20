// RegularCompilerStageEZ.java, created Mon Apr 21 12:19:59 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

/**
 * <code>RegularCompilerStageEZ</code> is a compiler stage that is
 * always enabled.
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: RegularCompilerStageEZ.java,v 1.1 2003-04-22 00:09:57 salcianu Exp $ */
public abstract class RegularCompilerStageEZ extends CompilerStageEZ {
    public RegularCompilerStageEZ(String name) { super(name); }
    
    /** @return <code>true</code>*/
    public final boolean enabled() { return true; }
}
