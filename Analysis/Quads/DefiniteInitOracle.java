// DefiniteInitOracle.java, created Mon Nov  5 16:14:29 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.DomTree;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SIGMA;
import harpoon.Util.Util;
import harpoon.Util.Collections.AggregateSetFactory;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A <code>DefiniteInitOracle</code> tells you whether a given
 * field is "definitely initialized" before control flow leaves
 * every constructor of its declaring class.  A field is not
 * considered to be "definitely initialized" if it "may" be read
 * before its definite initialization point.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DefiniteInitOracle.java,v 1.1.2.5 2001-11-07 19:08:09 cananian Exp $
 */
public class DefiniteInitOracle {
    private static final boolean DEBUG=true;
    final HCodeFactory hcf;
    final ClassHierarchy ch;
    final Set notDefinitelyInitialized = new HashSet();

    /** Queries the <code>DefiniteInitOracle</code>; returns <code>true</code>
     * iff every constructor for the declaring class of <code>hf</code>
     * *must* define the field before any reference to the field is made.
     */
    public boolean isDefinitelyInitialized(HField hf) {
	if (hf.isStatic()) return false; // XXX: would need to look at <clinit>
	if (!ch.instantiatedClasses().contains(hf.getDeclaringClass()))
	    return false; // never instantiated; thus not def. initialized.
	return !notDefinitelyInitialized.contains(hf);
    }
    /** Creates a <code>DefiniteInitOracle</code> which will use
     * the given code factory and hierarchy approximation.  For best
     * results, the code factory should produce SSI or SSA form. */
    public DefiniteInitOracle(HCodeFactory hcf, ClassHierarchy ch) {
	this.hcf = hcf;
	this.ch = ch;
	this.cg = new CallGraphImpl(ch, hcf);
	this.fso = new FieldSyncOracle(hcf, ch, cg);
	// for each constructor:
	for (Iterator it=ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    if (!isConstructor(hm)) continue;
	    Set di = getNotDefInit(hm);
	    notDefinitelyInitialized.addAll(di);
	}
	// free memory.
	fso=null;
	cg=null;
	ndiCache=null;
	// done!
	if (DEBUG) { // debugging:
	    System.out.println("DEFINITELY INITIALIZED FIELDS:");
	    for (Iterator it=ch.classes().iterator(); it.hasNext(); ) {
		HClass hc = (HClass) it.next();
		for (Iterator it2=Arrays.asList(hc.getDeclaredFields())
			 .iterator();
		     it2.hasNext(); ) {
		    HField hf = (HField) it2.next();
		    if (isDefinitelyInitialized(hf))
			System.out.println(" "+hf);
		}
	    }
	    System.out.println("------------------------------");
	}
    }

    // only used during analysis; clear afterwards
    FieldSyncOracle fso;
    CallGraph cg;
    Map ndiCache = new HashMap();

    Set getNotDefInit(HMethod hm) {
	if (!ndiCache.containsKey(hm)) {
	    Set notDefinitelyInitialized = new HashSet();
	    HCode hc = hcf.convert(hm);
	    Util.assert(hc!=null, hm);
	    // first, a quick pass to mark all variables which *must*
	    // contain 'this'.
	    Set thisvars = findThisVars(hc);
	    // compute dominator tree.
	    DomTree dt = new DomTree(hc, false);
	    // compute MayReadOracle
	    MayReadOracle mro = new MayReadOracle(hc, fso, cg,
						  hm.getDeclaringClass());
	    // make analysis results cache.
	    MultiMap cache = new GenericMultiMap(new AggregateSetFactory());
	    // for each exit point:
	    for (Iterator it2=findExitPoints(hc).iterator(); it2.hasNext(); ) {
		// recursively determine set of 'definitely initialized'
		// fields.
		Set definit = findDefInit((HCodeElement)it2.next(),
					  dt, thisvars, mro, cache);
		// add any fields which are *not* definitely initialized to
		// our set.
		Iterator it3=Arrays.asList
		    (hm.getDeclaringClass().getDeclaredFields()).iterator();
		while (it3.hasNext()) {
		    HField hf = (HField) it3.next();
		    if (hf.isStatic()) continue;
		    if (definit.contains(hf)) continue; // definitely init'd
		    // not definitely init'd!
		    notDefinitelyInitialized.add(hf);
		}
	    }
	    // okay, cache this!
	    ndiCache.put
		(hm, Collections.unmodifiableSet(notDefinitelyInitialized));
	}
	return (Set) ndiCache.get(hm);
    }
    /** return a conservative approximation to whether this is a constructor
     *  or not.  it's always safe to return true. */
    boolean isConstructor(HMethod hm) {
	// this is tricky, because we want split constructors to count, too,
	// even though renamed constructors (such as generated by initcheck,
	// for instance) won't always be instanceof HConstructor.  Look
	// for names starting with '<init>', as well.
	if (hm instanceof HConstructor) return true;
	if (hm.getName().startsWith("<init>")) return true;
	// XXX: what about methods generated by RuntimeMethod Cloner?
	// we could try methods ending with <init>, but then the
	// declaringclass information would be wrong.
	//if (hm.getName().endsWidth("<init>")) return true;//not safe yet.
	return false;
    }
    /** Is this a 'this' constructor?  Safe to return false if unsure. */
    boolean isThisConstructor(HMethod hm, HCodeElement me) {
	return isConstructor(hm) && // assumes this method is precise.
	    hm.getDeclaringClass().equals
	    (((Quad)me).getFactory().getMethod().getDeclaringClass());
    }
    /** Is this a super constructor?  Safe to return false if unsure. */
    boolean isSuperConstructor(HMethod hm, HCodeElement me) {
	return isConstructor(hm) && // assumes this method is precise.
	    hm.getDeclaringClass().equals
	    (((Quad)me).getFactory().getMethod().getDeclaringClass()
	     .getSuperclass());
    }

    // XXX: factor out into 'find vars which *must* equal a method param'
    // export sets, or query one, your choice.  this is used both to
    // find 'this' vars here as well as elsewhere to determine fields
    // which *must* be set to the same value as a method parameter.
    Set findThisVars(HCode hc) {
	final Set thisvars = new HashSet();
	final Set notthisvars = new HashSet();
	// create visitor.
	// lattice: don't know, this, not-this.  -->move-->
	// presence in 'not-this' overrides presence in 'this'.
	// sets can only grow.
	final Set relevant = new HashSet();
	QuadVisitor v = new QuadVisitor() {
		public void visit(Quad q) {
		    /* look for overwrites, which are always not 'this' */
		    notthisvars.addAll(q.defC());
		}
		public void visit(METHOD q) {
		    // param 0 is 'this'; all others are 'not-this'
		    thisvars.add(q.params(0));
		    for (int i=1; i<q.paramsLength(); i++)
			notthisvars.add(q.params(i));
		}
		public void visit(MOVE q) {
		    relevant.add(q);
		    if (thisvars.contains(q.src()) &&
			!notthisvars.contains(q.src()))
			thisvars.add(q.dst());
		    else
			notthisvars.add(q.dst());
		}
		public void visit(SIGMA q) {
		    relevant.add(q);
		    // get rid of overwrites.
		    Set s = new HashSet(q.defC());
		    for (int i=0; i<q.numSigmas(); i++)
			s.removeAll(Arrays.asList(q.dst(i)));
		    notthisvars.addAll(s);
		    // now look for src==this.
		    for (int i=0; i<q.numSigmas(); i++)
			if (notthisvars.contains(q.src(i)))
			    notthisvars.addAll(Arrays.asList(q.dst(i)));
			else if (thisvars.contains(q.src(i)))
			    // found! add all dst to thisvars.
			    thisvars.addAll(Arrays.asList(q.dst(i)));
		}
		public void visit(PHI q) {
		    relevant.add(q);
		    // phi(x,y) is 'this' iff x *and* y are 'this' and
		    // neither x nor y is *not* 'this'
		    for (int i=0; i<q.numPhis(); i++)
			for (int j=0; j<q.arity(); j++)
			    if (notthisvars.contains(q.src(i, j)))
				notthisvars.add(q.dst(i));
			    else if (thisvars.contains(q.src(i, j)))
				thisvars.add(q.dst(i));
		}
	    };
	// once through all elements.
	for (Iterator it=hc.getElementsI(); it.hasNext(); )
	    ((Quad)it.next()).accept(v);
	// iterate through relevant elements until the sets stop changing size.
	int oldsize = 0, size = thisvars.size() + notthisvars.size();
	while (size > oldsize) {
	    for (Iterator it=relevant.iterator(); it.hasNext(); )
		((Quad)it.next()).accept(v);
	    oldsize = size;
	    size = thisvars.size() + notthisvars.size();
	}
	// done!
	thisvars.removeAll(notthisvars);
	return Collections.unmodifiableSet(thisvars);
    }
    Set findExitPoints(HCode hc) {
	// exit points are returns.
	Set exits = new HashSet();
	for (Iterator it=hc.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    if (q instanceof RETURN)
		exits.add(q);
	}
	return Collections.unmodifiableSet(exits);
    }
    /** Returns true iff the given method hm contains no reads of fields
     *  in hc. */
    boolean isSafe(HMethod hm, HClass hc) {
	for (Iterator it=Arrays.asList(hc.getDeclaredFields()).iterator();
	     it.hasNext(); ) {
	    HField hf = (HField) it.next();
	    if (hf.isStatic()) continue;
	    if (fso.isRead(hm, hf))
		return false;
	}
	return true;
    }
    // convenience.
    boolean isSafe(HMethod hm, HCodeElement hce) {
	return isSafe
	    (hm, ((Quad)hce).getFactory().getMethod().getDeclaringClass());
    }
    Set findDefInit(HCodeElement exit,
		    DomTree dt, Set thisvars, MayReadOracle mro,
		    MultiMap cache) {
	return Collections.unmodifiableSet
	    (getDefBefore(exit, dt, thisvars, mro, cache));
    }
    private Set getDefBefore(HCodeElement hce,
			     DomTree dt, Set thisvars, MayReadOracle mro,
			     MultiMap cache) {
	if (!cache.containsKey(hce)) {
	    // get immediate dominator.
	    HCodeElement idom = dt.idom(hce);
	    if (idom!=null) { // if this *has* an immediate dominator...
		// get *its* definite initializations.
		Set definit = getDefBefore(idom, dt, thisvars, mro, cache);
		// all of these are also definitely initialized at this point.
		cache.addAll(hce, definit);
		// collect set of reads before this quad.
		Set readBefore = new HashSet();
		Edge[] prevEdges = ((Quad)hce).prevEdge();
		for (int i=0; i < prevEdges.length; i++)
		    readBefore.addAll(mro.mayReadAt(prevEdges[i]));
		// is the idom itself an initialization?
		if (idom instanceof SET) {
		    SET q = (SET) idom;
		    if (q.objectref()!=null &&
			thisvars.contains(q.objectref())) {
			// not a definite init if this field can be read before
			if (!readBefore.contains(q.field()))
			    // baby! add this to the definitely-init'd set!
			    cache.add(hce, q.field());
		    }
		} else if (idom instanceof CALL) {
		    CALL q = (CALL) idom;
		    // could this be a 'this' constructor?
		    if (isThisConstructor(q.method(), q)) {
			// add all the definitely-initialized vars which
			// have not been read-before.
			Set ndi = getNotDefInit(q.method());
			HClass hc = q.method().getDeclaringClass(); // 'this'
			Iterator it=Arrays.asList
			    (hc.getDeclaredFields()).iterator();
			while (it.hasNext()) {
			    HField hf = (HField) it.next();
			    if (hf.isStatic()) continue;
			    if (ndi.contains(hf)) continue; // not def init'd
			    if (!readBefore.contains(hf))
				cache.add(hce, hf); // def init'd!
			}
		    }
		}
	    }
	    // done.
	}
	return (Set) cache.getValues(hce);
    }
}
