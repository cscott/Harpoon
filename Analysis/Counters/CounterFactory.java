// CounterFactory.java, created Wed Feb 21 14:35:40 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Counters;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HFieldMutator;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.SET;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.util.Iterator;
/**
 * <code>CounterFactory</code> is a state-less instrumentation package,
 * with the goal of making it as easy as possible to add counters
 * and timers to executable code.  It comes in several parts: we have
 * a set of routines to splice in counter/time calls into Quad forms,
 * and we have a post-processing <code>HCodeFactory</code> available
 * from the <code>codeFactory()</code> method that will create the
 * appropriate epilog code to report the counter values at
 * program's end.
 * <p>
 * Counters are disabled by default.  All counters can be enabled by
 * setting the property <code>harpoon.counters.enabled.all</code> to
 * <code>"true"</code> and particular counters can be enabled by
 * setting the property <code>harpoon.counters.enabled.{counter-name}</code>
 * to <code>"true"</code>.  Particular counters can be disabled by
 * setting the property <code>harpoon.counters.disabled.{counter-name}</code>
 * to <code>"true"</code> and all counters can be disabled by setting
 * the property <code>harpoon.counters.disabled.all</code> to
 * <code>"true"</code>.
 * <p>
 * Counters can also be grouped into classes by naming them with
 * dot-separated strings.  For example, counters named 'foo.bar'
 * and 'foo.baz' are both enabled by setting 
 * <code>harpoon.counters.enabled.foo</code> to <code>"true"</code>;
 * the counters' actual name on output will be "foo_bar" and "foo_baz".
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CounterFactory.java,v 1.1.2.4 2001-03-02 04:04:07 cananian Exp $
 */
public final class CounterFactory {
    /** default status for all counters. */
    private static boolean ENABLED = false;
    // don't allow object creation: all methods here are static.
    private CounterFactory() { }
    /** allow enabling/disabling of all counters via properties */
    private static boolean isEnabled(String counter_name) {
	if (Boolean.getBoolean("harpoon.counters.enabled.all"))
	    return true;
	if (Boolean.getBoolean("harpoon.counters.disabled.all"))
	    return false;
	if (Boolean.getBoolean("harpoon.counters.enabled."+counter_name))
	    return true;
	if (Boolean.getBoolean("harpoon.counters.disabled."+counter_name))
	    return false;
	// allow 'package' enable/disable by recursing to check package status
	int idx = counter_name.lastIndexOf('.');
	if (idx>=0) return isEnabled(counter_name.substring(0, idx));
	// okay, can't find any guidance at all.  Use "default".
	return ENABLED;
    }
    
    /** <code>HCodeFactory</code> that will add calls to the counter-printing
     *  epilog at the end of the given main method and before calls to
     *  <code>Runtime.exit()</code>. */
    public static HCodeFactory codeFactory(HCodeFactory parent,
				    Linker linker, HMethod main) {
	// check whether *any* counters are enabled.
	boolean enabled = ENABLED;
	Iterator it=System.getProperties().keySet().iterator();
	while (it.hasNext())
	    if (((String)it.next()).startsWith("harpoon.counters.enabled."))
		enabled = true;
	if (Boolean.getBoolean("harpoon.counters.disabled.all"))
	    enabled = false;
	if (Boolean.getBoolean("harpoon.counters.enabled.all"))
	    enabled = true;
	// if nothing is enabled, don't bother splicing in our code factory.
	if (!enabled) return parent;
	// else do it!
	return new EpilogMutator(parent, linker, main).codeFactory();
    }

    /** Increment the named counter by 1 on the given edge. */
    public static Edge spliceIncrement(QuadFactory qf,
				       Edge e, String counter_name) {
	return spliceIncrement(qf, e, counter_name, 1);
    }
    /** Increment the named counter by <code>value</code> on the given edge. */
    public static Edge spliceIncrement(QuadFactory qf,
				       Edge e, String counter_name,
				       long value) {
	if (!isEnabled(counter_name)) return e;
	Temp t = new Temp(qf.tempFactory());
	return spliceIncrement(qf,
			       addAt(e, new CONST(qf, null, t, new Long(value),
						  HClass.Long)),
			       counter_name, t);
    }
    /** Increment the named counter by the amount in the <code>Temp</code>
     *  <code>Tvalue</code> (with type <code>long</code>) on the given edge. */
    public static Edge spliceIncrement(QuadFactory qf,
				       Edge e, String counter_name,
				       Temp Tvalue) {
	Quad CJMP, PHI;
	if (!isEnabled(counter_name)) return e;
	HField HFcounter = getCounterField(qf.getLinker(), counter_name);
	HField HFlockobj = getLockField(qf.getLinker(), counter_name);
	// first fetch the lock
	Temp Tlck = new Temp(qf.tempFactory());
	e = addAt(e, new GET(qf, null, Tlck, HFlockobj, null));
	// if package not initialized, than don't count yet.
	Temp Tnul = new Temp(qf.tempFactory());
	e = addAt(e, new CONST(qf, null, Tnul, null, HClass.Void));
	Temp Tcmp = new Temp(qf.tempFactory());
	e = addAt(e, new OPER(qf, null, Qop.ACMPEQ, Tcmp,
			      new Temp[] { Tlck, Tnul }));
	e = addAt(e, CJMP=new CJMP(qf, null, Tcmp, new Temp[0]));
	// and lock on it
	e = addAt(e, new MONITORENTER(qf, null, Tlck));
	// then fetch the old counter value
	Temp Tcnt = new Temp(qf.tempFactory());
	e = addAt(e, new GET(qf, null, Tcnt, HFcounter, null));
	// increment it, consistent with SSx forms.
	Temp Tcnt2 = new Temp(qf.tempFactory());
	e = addAt(e, new OPER(qf, null, Qop.LADD, Tcnt2,
			      new Temp[] { Tcnt, Tvalue}));
	// and store it back.
	e = addAt(e, new SET(qf, null, HFcounter, null, Tcnt2));
	// now release the lock.
	e = addAt(e, new MONITOREXIT(qf, null, Tlck));
	// merge from bypass.
	e = addAt(e, PHI=new PHI(qf, null, new Temp[0], 2));
	Quad.addEdge(CJMP, 1, PHI, 1);
	// done!
	return e;
    }

    // get counter and lock field, creating it if necessary.
    static HField getCounterField(Linker l, String counter_name) {
	return getField(l, "COUNTER_"+counter_name, "J");
    }
    static HField getLockField(Linker l, String counter_name) {
	return getField(l, "LOCK_"+counter_name, "Ljava/lang/Object;");
    }
    private static HField getField(Linker l, String name, String descriptor) {
	HClass hc = l.forName("harpoon.Runtime.Counters");
	name = name.replace('.','_'); // allow '.' in name, but strip.
	try {
	  HField hf = hc.getDeclaredField(name);
	  Util.assert(hf.getDescriptor().equals(descriptor));
	  Util.assert(hf.isStatic());
	  return hf;
	} catch (NoSuchFieldError e) {
	  // okay, have to create this field ourselves.
	  HField hf = hc.getMutator().addDeclaredField(name, descriptor);
	  // make public and static
	  hf.getMutator().setModifiers(Modifier.PUBLIC | Modifier.STATIC);
	  return hf;
	}
    }

    // private helper functions.
    private static Edge addAt(Edge e, Quad q) { return addAt(e, 0, q, 0); }
    private static Edge addAt(Edge e, int which_pred, Quad q, int which_succ) {
	    Quad frm = (Quad) e.from(); int frm_succ = e.which_succ();
	    Quad to  = (Quad) e.to();   int to_pred = e.which_pred();
	    Quad.addEdge(frm, frm_succ, q, which_pred);
	    Quad.addEdge(q, which_succ, to, to_pred);
	    return to.prevEdge(to_pred);
	}
}
