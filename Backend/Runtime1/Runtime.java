// Runtime.java, created Wed Sep  8 14:30:28 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.ClassDepthMap;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * <code>Runtime1.Runtime</code> is a no-frills implementation of the runtime
 * abstract class.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Runtime.java,v 1.1.2.6 1999-10-13 17:16:50 cananian Exp $
 */
public class Runtime extends harpoon.Backend.Generic.Runtime {
    final Frame frame;
    final ClassHierarchy ch;
    final ObjectBuilder ob;
    
    /** Creates a new <code>Runtime1.Runtime</code>. */
    public Runtime(Frame frame, AllocationStrategy as, ClassHierarchy ch) {
	super(new Object[] { frame, as, ch });
	this.frame = frame;
	this.ch = ch;
	this.ob = new harpoon.Backend.Runtime1.ObjectBuilder(this);
    }
    // protected initialization methods.
    protected NameMap initNameMap(Object closure) {
	return new harpoon.Backend.Maps.DefaultNameMap();
    }
    protected TreeBuilder initTreeBuilder(Object closure) {
	Frame f = (Frame) ((Object[])closure)[0];
	AllocationStrategy as = (AllocationStrategy) ((Object[])closure)[1];
	ClassHierarchy ch = (ClassHierarchy) ((Object[])closure)[2];
	return new harpoon.Backend.Runtime1.TreeBuilder(this, ch, as,
							f.pointersAreLong());
    }

    public List classData(HClass hc) {
	// i don't particularly like this solution to generating
	// the needed string constants, but it works.
	harpoon.Backend.Runtime1.TreeBuilder tb =
	    (harpoon.Backend.Runtime1.TreeBuilder) treeBuilder;
	tb.stringSet.removeAll(stringsSeen);
	stringsSeen.addAll(tb.stringSet);
	Set newStrings = new HashSet(tb.stringSet);
	tb.stringSet.clear();

	return Arrays.asList(new Data[] {
	    new DataClaz(frame, hc, ch),
	    new DataInterfaceList(frame, hc, ch),
	    new DataStaticFields(frame, hc),
	    new DataStrings(frame, hc, newStrings),
	});
    }
    final Set stringsSeen = new HashSet();
}
