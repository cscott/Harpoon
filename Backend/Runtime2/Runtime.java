// Runtime.java, created Wed Sep  8 14:30:28 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime2;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.CallGraph;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.ClassDepthMap;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.Util.Util;

import harpoon.Backend.Runtime1.AllocationStrategy;
import harpoon.Backend.Runtime1.ObjectBuilder.RootOracle;

import java.lang.reflect.Modifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * <code>Runtime2.Runtime</code> is a no-frills implementation of the runtime
 * abstract class.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Runtime.java,v 1.3 2002-04-10 03:03:34 cananian Exp $
 */
public class Runtime extends harpoon.Backend.Runtime1.Runtime {
    public Runtime(Frame frame, AllocationStrategy as,
		   HMethod main, boolean prependUnderscore) {
	this(frame, as, main, prependUnderscore, null);
    }

    public Runtime(Frame frame, AllocationStrategy as,
		   HMethod main, 
		   boolean prependUnderscore, RootOracle rootOracle) {
	super(frame,as,main,prependUnderscore,rootOracle);
    }
    protected harpoon.Backend.Runtime2.TreeBuilder initTreeBuilder() {
	int align = Integer.parseInt
	    (System.getProperty("harpoon.runtime1.pointer.alignment","0"));
	configurationSet.add("check_with_claz_shrink_not_needed");
	return new harpoon.Backend.Runtime2.TreeBuilder
	    (this, frame.getLinker(), as, frame.pointersAreLong(), align);
    }
}
