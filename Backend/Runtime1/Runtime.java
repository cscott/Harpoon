// Runtime.java, created Wed Sep  8 14:30:28 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;

import java.util.Arrays;
import java.util.List;
/**
 * <code>Runtime</code> is a no-frills implementation of the runtime
 * abstract class.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Runtime.java,v 1.1.2.1 1999-09-09 05:12:17 cananian Exp $
 */
public class Runtime extends harpoon.Backend.Generic.Runtime {
    
    /** Creates a <code>Runtime</code>. */
    public Runtime() { }

    public NameMap nameMap() { return _nm; }
    private final static NameMap _nm =
	new harpoon.Backend.Maps.DefaultNameMap();

    public List classData(Frame f, HClass hc, ClassHierarchy ch) {
	return Arrays.asList(new Data[] { new ClassData(f, hc, ch) });
    }
}
