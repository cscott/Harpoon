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
 * @version $Id: Runtime.java,v 1.1.2.4 2001-07-09 23:54:19 cananian Exp $
 */
public class Runtime extends harpoon.Backend.Runtime1.Runtime {
    public Runtime(Frame frame, AllocationStrategy as,
		   HMethod main, ClassHierarchy ch, CallGraph cg,
		   boolean prependUnderscore) {
	super(frame,as,main,ch,cg,prependUnderscore);
    }

    public Runtime(Frame frame, AllocationStrategy as,
		   HMethod main, ClassHierarchy ch, CallGraph cg,
		   boolean prependUnderscore, RootOracle rootOracle) {
	super(frame,as,main,ch,cg,prependUnderscore,rootOracle);
    }

    protected TreeBuilder initTreeBuilder(Object closure) {
	Frame f = (Frame) ((Object[])closure)[0];
	AllocationStrategy as = (AllocationStrategy) ((Object[])closure)[1];
	ClassHierarchy ch = (ClassHierarchy) ((Object[])closure)[2];
	return new harpoon.Backend.Runtime2.TreeBuilder(this, f.getLinker(),
							ch, as,
							f.pointersAreLong(),0);
    }
}
