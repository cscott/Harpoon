// CompilerStage.java, created Sat Apr 12 12:52:09 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import java.util.List;
import harpoon.Util.Options.Option;

/**
 * <code>CompilerStage</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: CompilerStage.java,v 1.1 2003-04-17 00:10:10 salcianu Exp $
 */
public abstract class CompilerStage {
    public CompilerStage(String name) { this.name = name; }
    public String name() { return name; }
    private final String name;

    public abstract List/*<Option>*/ getOptions();
    public abstract CompilerState action(CompilerState cs);
}
