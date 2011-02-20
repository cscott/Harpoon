// JLSRuntimeInformation.java, created Mon Jan 17 03:19:30 2000 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Analysis;

import harpoon.Backend.Generic.RuntimeInformation;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.Util.ArraySet;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
/**
 * <code>JLSRuntimeInformation</code> contains the basic runtime information
 * required by the 
 * <A HREF="http://java.sun.com/docs/books/jls/html/index.html">Java
 * Language Specification</A>.  All possible runtime systems/JVMs must
 * display any behavior specified in this class.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: JLSRuntimeInformation.java,v 1.2 2002-02-25 21:00:47 cananian Exp $
 * @see RuntimeInformation
 */
public abstract class JLSRuntimeInformation extends RuntimeInformation {
    protected Linker linker;
    
    /** Creates a <code>JDKRuntimeInformation</code>. */
    public JLSRuntimeInformation(Linker linker) {
	super(linker);
	HCioE = linker.forName("java.io.IOException");
	HCstring = linker.forName("java.lang.String");
	HCsystem = linker.forName("java.lang.System");
	HCthread = linker.forName("java.lang.Thread");
	HMthreadStart = HCthread.getMethod("start", new HClass[0]);
	HMthreadRun   = HCthread.getMethod("run", new HClass[0]);
	HMsysArrayCopy = HCsystem
	    .getMethod("arraycopy",
		       "(Ljava/lang/Object;ILjava/lang/Object;II)V");
    }
    protected final HClass HCioE, HCstring, HCsystem, HCthread;
    protected final HMethod HMsysArrayCopy, HMthreadStart, HMthreadRun;

    public Set baseClasses() {
	return new ArraySet(new HClass[] {
	    HClass.Boolean, HClass.Byte, HClass.Short, HClass.Int,
	    HClass.Long, HClass.Float, HClass.Double, HClass.Char,
	    HCstring, // for string constants
	});
    }
    public Set methodsCallableFrom(HMethod m) {
	// Thread.start() implicitly causes a call to Thread.run()
	if (m.equals(HMthreadStart)) return Collections.singleton(HMthreadRun);
	// assume that any native method declared as throwing IOException
	// may in fact do so.
	if (Modifier.isNative(m.getModifiers()) &&
	    Arrays.asList(m.getExceptionTypes()).contains(HCioE))
	    return new ArraySet(new HMethod[] {
		HCioE.getConstructor(new HClass[0]),
		HCioE.getConstructor(new HClass[] { HCstring }),
	    });
	// System.arrayCopy can really throw the exceptions it says it can.
	if (m.equals(HMsysArrayCopy))
	    return new ArraySet(new HMethod[] {
		linker.forName("java.lang.ArrayIndexOutOfBoundsException")
		    .getConstructor(new HClass[] { HCstring }),
		linker.forName("java.lang.ArrayStoreException")
		    .getConstructor(new HClass[] { HCstring }),
		    });
	// okay, this method is boring.
	return Collections.EMPTY_SET;
    }
    public Set initiallyCallableMethods() {
	return Collections.EMPTY_SET;
    }
}

