// UseDefChecker.java, created Wed Nov 15 13:44:59 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Properties.UseDefable;
import harpoon.Temp.Temp;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
/**
 * <code>UseDefChecker</code> verifies that all variables are defined
 * before they are used.  It aids in debugging code transformations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: UseDefChecker.java,v 1.3 2004-02-08 03:19:12 cananian Exp $
 */
public class UseDefChecker implements HCodeFactory {
    HCodeFactory parent;
    ReachingDefsFactory rdf;
    /** Creates a <code>UseDefChecker</code> from a 
     *  <code>ReachingDefsFactory</code>. */
    public UseDefChecker(HCodeFactory parent, ReachingDefsFactory rdf) {
	this.parent = parent; this.rdf = rdf;
    }
    /** Creates a <code>UseDefChecker</code> using the named class
     *  as the <code>ReachingDefs</code> implementation. */
    public UseDefChecker(HCodeFactory parent, String reachingdefsClassName) {
	this(parent, new GenericRDFactory(reachingdefsClassName));
    }
    public String getCodeName() { return parent.getCodeName(); }
    public void clear(HMethod m) { parent.clear(m); }
    public HCode convert(HMethod m) {
	HCode hc = parent.convert(m);
	if (hc!=null) {
	    ReachingDefs rd = rdf.makeReachingDefs(hc);
	    for (Iterator it=hc.getElementsI(); it.hasNext(); ) {
		UseDefable ud = (UseDefable) it.next();
		for (Object tO : ud.useC()) {
		    Temp t = (Temp) tO;
		    if (rd.reachingDefs(ud, t).size()==0) {
			hc.print(new java.io.PrintWriter(System.err));
			throw new Error("Use of "+t+" in "+ud+" before def.");
		    }
		}
	    }
	}
	return hc;
    }

    // inner classes.
    /** The <code>UseDefChecker</code> constructor takes a
     *  <code>ReachingDefsFactory</code> argument to specify which
     *  <code>ReachingDefs</code> implementation it should use. */
    public static abstract class ReachingDefsFactory {
	public abstract ReachingDefs makeReachingDefs(HCode hc);
    }
    private static final class GenericRDFactory extends ReachingDefsFactory {
	final java.lang.reflect.Constructor constructor;
	public GenericRDFactory(String classname) {
	    try {
	    this.constructor = Class.forName(classname)
		.getDeclaredConstructor(new Class[] { HCode.class });
	    } catch (Throwable t) {
		throw new Error("GENERICRDFACTORY FAILED: "+t);
	    }
	}
	public ReachingDefs makeReachingDefs(HCode hc) {
	    try {
	    return (ReachingDefs) constructor.newInstance(new Object[] { hc });
	    } catch (java.lang.reflect.InvocationTargetException e) {
		e.printStackTrace();
		throw new Error(e.getTargetException().toString());
	    } catch (Throwable t) {
		throw new Error("REACHINGDEFS INSTANTIATION FAILED: "+t);
	    }
	}
    }
}
