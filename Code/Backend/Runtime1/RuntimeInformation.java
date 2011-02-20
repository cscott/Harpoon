// RuntimeInformation.java, created Mon Jan 17 08:06:29 2000 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.Util.ArraySet;

import java.util.Collections;
import java.util.Set;
/**
 * <code>RuntimeInformation</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: RuntimeInformation.java,v 1.2 2002-02-25 21:02:20 cananian Exp $
 */
public class RuntimeInformation
    extends harpoon.Backend.Generic.RuntimeInformation {
    harpoon.Backend.Generic.RuntimeInformation proxy;
    
    /** Creates a <code>RuntimeInformation</code>. */
    public RuntimeInformation(harpoon.Backend.Generic.RuntimeInformation proxy)
    {
        super(proxy.linker); this.proxy = proxy;
	HCcharA = linker.forDescriptor("[C");
	HCproperties = linker.forName("java.util.Properties");
	HCstring = linker.forName("java.lang.String");
	HCsystem = linker.forName("java.lang.System");
	HMsysInitProp = HCsystem
	    .getMethod("initProperties", new HClass[]{ HCproperties });
	HMpropSetProp = HCproperties
	    .getMethod("setProperty", new HClass[] { HCstring, HCstring });
    }
    private final HClass HCcharA, HCproperties, HCstring, HCsystem;
    private final HMethod HMsysInitProp, HMpropSetProp;

    public Set baseClasses() {
	return proxy.baseClasses(); 
    }
    public Set methodsCallableFrom(HMethod m) {
	Set s = proxy.methodsCallableFrom(m);
	if (m.equals(HMsysInitProp)) {
	    s = union(s, Collections.singleton(HMpropSetProp));
	}
	return s;
    }
    public Set initiallyCallableMethods() {
	Set s = union(proxy.initiallyCallableMethods(),
		      new ArraySet(new HMethod[] {
			  linker.forName("java.lang.NoClassDefFoundError")
			    .getConstructor(new HClass[] { HCstring }),
			  linker.forName("java.lang.NoSuchMethodError")
			    .getConstructor(new HClass[] { HCstring }),
			  linker.forName("java.lang.NoSuchFieldError")
			    .getConstructor(new HClass[] { HCstring }),
			  HCstring.getConstructor(new HClass[] { HCcharA }),
			  HCstring.getMethod("length", "()I"),
			  HCstring.getMethod("toCharArray","()[C"),
		      }));
	return s;
    }
}
