// ConstructorClassifier.java, created Thu Nov  8 19:38:41 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.SizeOpt;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.MustParamOracle;
import harpoon.Analysis.Quads.SCC.SCCAnalysis;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.SET;
import harpoon.Util.Collections.AggregateMapFactory;
import harpoon.Util.Collections.MapFactory;
import harpoon.Util.Util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * The <code>ConstructorClassifier</code> class takes a look at
 * constructor invocations and determines whether we can do one
 * of several 'mostly-zero field' transformations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstructorClassifier.java,v 1.1.2.2 2001-11-09 22:13:24 cananian Exp $
 */
public class ConstructorClassifier {
    /* Definitions:
     * - a 'this-final' field is only written in constructors of its type.
     *   it is not written in subclass constructors.  every write within
     *   its constructor *must* be to the 'this' object.
     * - a 'subclass-final' field is only written in constructors of its type
     *   *and methods of subclasses*.  Every write in its own constructors
     *   must be to the 'this' object as before.
     *
     * classes with 'subclass' final fields can be split into a 'small'
     * version without the field, which is the superclass of a 'large' version
     * with the field (and a setter method).  Subclasses of the original
     * class extend the 'large' version and so can write the extra field.
     */
    private final HCodeFactory hcf;
    private final MapFactory mf = new AggregateMapFactory();
    private final Set badFields;
    
    /** Creates a <code>ConstructorClassifier</code>. */
    public ConstructorClassifier(HCodeFactory hcf, ClassHierarchy ch) {
	this.hcf = hcf;
        // first, find all the 'subclass-final' fields in the program.
	// actually, we find the dual of this set.
	this.badFields = findBadFields(hcf, ch);
	// we're done!  (the rest of the analysis is demand-driven)
	// debug:
	System.out.println("BAD FIELDS: "+badFields);
	System.out.println("CONSTRUCTOR RESULTS:");
	for (Iterator it=ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    if (!isConstructor(hm)) continue;
	    System.out.println(" "+hm+": "+classifyMethod(hm));
	}
    }
    private Set findBadFields(HCodeFactory hcf, ClassHierarchy ch) {
	Set badFields = new HashSet();
	// for each callable method...
	for (Iterator it=ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    HCode hc = hcf.convert(hm);
	    if (hc==null) continue; // xxx: native methods may write fields!
	    // construct a must-param oracle for constructors.
	    MustParamOracle mpo = 
		isConstructor(hm) ? new MustParamOracle(hc) : null;
	    // examine this method for writes
	    HClass thisClass = hc.getMethod().getDeclaringClass();
	    for (Iterator it2=hc.getElementsI(); it2.hasNext(); ) {
		Quad q = (Quad) it2.next();
		if (q instanceof SET) {
		    SET qq = (SET) q;
		    // ignore writes of static fields.
		    if (qq.isStatic()) continue;
		    // if this is a constructor, than it may write only
		    // to fields of 'this'
		    if (isConstructor(hm) &&
			mpo.isMustParam(qq.objectref()) &&
			mpo.whichMustParam(qq.objectref())==0)
			continue; // this is a permitted write.
		    // writes by subclass methods to superclass fields are
		    // okay. (but not writes by 'this' methods to 'this'
		    // fields, unless the method is a constructor)
		    if (qq.field().getDeclaringClass()
			.isInstanceOf(thisClass) &&
			(isConstructor(hm) ||
			 !thisClass.equals(qq.field().getDeclaringClass())))
			continue; // subclass writes are permitted.
		    // non-permitted write!
		    badFields.add(qq.field());
		}
	    }
	    // on to the next!
	}
	// done!  we have set of all bad (not subclass-final) fields.
	return Collections.unmodifiableSet(badFields);
    }

    // per-constructor analysis.....

    static class Classification {
	int param; // -1 means 'not a param'
	Object constant; // null means 'not a constant'
	Classification() { this.param=-1; this.constant=null; }
	Classification(int param) { this.param=param; this.constant=null; }
	Classification(Object constant){this.param=-1; this.constant=constant;}
	void merge(Classification c) {
	    if (this.param != c.param ||
		(this.constant==null) != (c.constant==null) ||
		(this.constant!=null && !this.constant.equals(c.constant))) {
		// goes to top.
		this.param=-1; this.constant=null;
	    }
	}
	public String toString() {
	    Util.assert(!(param>0 && constant!=null));
	    if (param>0) return "PARAM#"+param;
	    if (constant!=null) return "CONSTANT "+constant;
	    return "NO INFO";
	}
    }
    /** caching infrastructure around 'doOne' */
    Map classifyMethod(HMethod hm) {
	Util.assert(isConstructor(hm));
	if (!cache.containsKey(hm)) {
	    HCode hc = hcf.convert(hm);
	    Util.assert(hc!=null);
	    cache.put(hm, doOne(hc));
	}
	return (Map) cache.get(hm);
    }
    private final Map cache = new HashMap();

    /** returns a map from hfields to classification objects */
    private Map doOne(HCode hc) {
	Map map = mf.makeMap();
	HMethod hm = hc.getMethod();
	Util.assert(isConstructor(hm));
	HClass thisClass = hm.getDeclaringClass();
	// first, do a SCCAnalysis.
	SCCAnalysis scc = new SCCAnalysis(hc);
	// also create a MustParamOracle
	MustParamOracle mpo = new MustParamOracle(hc);
	// look at every SET and CALL.
	for (Iterator it=hc.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    // ignore non-executable quads.
	    if (!scc.execMap(q)) continue;
	    if (q instanceof SET) {
		SET qq = (SET) q;
		// field set.
		if (qq.field().getDeclaringClass().equals(thisClass)) {
		    Classification oc = (Classification) map.get(qq.field());
		    Classification nc = new Classification();
		    // okay, is the value we're setting a constant? or param?
		    if (scc.isConst(qq, qq.src()))
			// it's a constant.
			nc = new Classification(scc.constMap(qq, qq.src()));
		    else if (mpo.isMustParam(qq.src()))
			// it must be a parameter.
			nc = new Classification(mpo.whichMustParam(qq.src()));
		    // XXX: CAN MERGE const with param if the const is
		    // the 'right' value (the one we're optimizing for)
		    if (oc!=null) //null means we haven't seen this field yet.
			nc.merge(oc);
		    map.put(qq.field(), nc);
		}
	    }
	    if (q instanceof CALL) {
		// deal with call to 'this' constructor.
		CALL qq = (CALL) q;
		if (isThisConstructor(qq.method(), qq)) {
		    // recursively invoke!
		    Map m = classifyMethod(qq.method()); // this should cache.
		    for (Iterator it2=m.entrySet().iterator(); it2.hasNext();){
			Map.Entry me = (Map.Entry) it2.next();
			HField hf = (HField) me.getKey();
			Classification oc = (Classification) map.get(hf);
			Classification nc = (Classification) me.getValue();
			if (oc!=null)
			    nc.merge(oc);
			map.put(hf, nc);
		    }
		}
	    }
	}
	return Collections.unmodifiableMap(map);
    }
		

    ///////// copied from harpoon.Analysis.Quads.DefiniteInitOracle.
    /** return a conservative approximation to whether this is a constructor
     *  or not.  it's always safe to return true. */
    private boolean isConstructor(HMethod hm) {
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
    private boolean isThisConstructor(HMethod hm, HCodeElement me) {
	return isConstructor(hm) && // assumes this method is precise.
	    hm.getDeclaringClass().equals
	    (((Quad)me).getFactory().getMethod().getDeclaringClass());
    }
    /** Is this a super constructor?  Safe to return false if unsure. */
    private boolean isSuperConstructor(HMethod hm, HCodeElement me) {
	return isConstructor(hm) && // assumes this method is precise.
	    hm.getDeclaringClass().equals
	    (((Quad)me).getFactory().getMethod().getDeclaringClass()
	     .getSuperclass());
    }

}
