// SunJDKRuntimeInformation.java, created Mon Jan 17 07:59:55 2000 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Analysis;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;

import java.util.Collections;
import java.util.Set;
/**
 * <code>SunJDKRuntimeInformation</code> contains behavior
 * specific to Sun's implementation of the JVM runtime.  
 * This class contains only information common to <b>all</b>
 * Sun runtime implementations; JDK1.1- or JDK1.2-specific
 * information should go in the appropriate subclass of
 * <code>SunJDKRuntimeInformation</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SunJDKRuntimeInformation.java,v 1.2 2002-02-25 21:00:47 cananian Exp $
 */
abstract class SunJDKRuntimeInformation extends JLSRuntimeInformation {
    
    /** Creates a <code>SunJDKRuntimeInformation</code>. */
    public SunJDKRuntimeInformation(Linker linker) {
	super(linker);
	HCsystem = linker.forName("java.lang.System");
	HMsysInitSC = HCsystem.getMethod("initializeSystemClass", "()V");
    }
    protected final HClass HCsystem;
    protected final HMethod HMsysInitSC;

    public Set initiallyCallableMethods() {
	return union(super.initiallyCallableMethods(),
		     Collections.singleton(HMsysInitSC));
    }
}
