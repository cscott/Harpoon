// Runtime.java, created Wed Sep  8 14:24:18 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;

import java.util.List;
/**
 * A <code>Generic.Runtime</code> provides runtime-specific
 * information to the backend.  It should be largely-to-totally
 * independent of the particular architecture targetted; all
 * interfaces in Runtime interact with <code>IR.Tree</code> form,
 * not the architecture-specific <code>IR.Assem</code> form.<p>
 * Among other things, a <code>Generic.Runtime</code> provides
 * class data constructors to provide class information to the
 * runtime system.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Runtime.java,v 1.1.2.4 1999-09-11 20:06:29 cananian Exp $
 */
public abstract class Runtime {
    /** A <code>NameMap</code> valid for this
     *  <code>Generic.Runtime</code>. */
    public final NameMap nameMap;

    protected Runtime(NameMap nm) { this.nameMap=nm; }

    /** returns a list of <code>HData</code>s which are needed for the
     *  given class. */
    public abstract List classData(Frame f, HClass hc, ClassHierarchy ch);
}
