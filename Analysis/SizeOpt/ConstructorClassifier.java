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
import harpoon.Temp.Temp;
import harpoon.Util.Collections.AggregateMapFactory;
import harpoon.Util.Collections.MapFactory;
import harpoon.Util.Util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * The <code>ConstructorClassifier</code> class takes a look at
 * constructor invocations and determines whether we can do one
 * of several 'mostly-zero field' transformations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstructorClassifier.java,v 1.1.2.4 2001-11-10 17:31:46 cananian Exp $
 */
public class ConstructorClassifier {
    private static final boolean DEBUG=false;
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
    private final ClassHierarchy ch;
    private final MapFactory mf = new AggregateMapFactory();
    private final Set badFields;
    
    /** Creates a <code>ConstructorClassifier</code>. */
    public ConstructorClassifier(HCodeFactory hcf, ClassHierarchy ch) {
	this.hcf = hcf;
	this.ch = ch;
        // first, find all the 'subclass-final' fields in the program.
	// actually, we find the dual of this set.
	this.badFields = findBadFields(hcf, ch);
	// we're done!  (the rest of the analysis is demand-driven)
	if (DEBUG) {
	    // debug:
	    System.out.println("BAD FIELDS: "+badFields);
	    System.out.println("CONSTRUCTOR RESULTS:");
	    for (Iterator it=ch.callableMethods().iterator(); it.hasNext(); ) {
		HMethod hm = (HMethod) it.next();
		if (!isConstructor(hm)) continue;
		System.out.println(" "+hm+": "+classifyMethod(hm));
	    }
	}
    }
    /** Returns <code>true</code> iff the given field is 'subclass-final'
     *  and there is at least one callable constructor where the field's
     *  content can be predicted.
     */
    public boolean isGood(HField hf) {
	if (hf.isStatic()) return false;
	if (badFields.contains(hf)) return false;
	// get declared methods of class (should include all constructors)
	List l = Arrays.asList(hf.getDeclaringClass().getDeclaredMethods());
	// for each callable constructor...
	for (Iterator it=l.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    if (!ch.callableMethods().contains(hm)) continue;
	    if (!isConstructor(hm)) continue;
	    // okay, hm is a callable constructor.
	    Map map = classifyMethod(hm);
	    if (!map.containsKey(hf) ||
		((Classification) map.get(hf)).isGood())
		return true; // this is a good field.
	}
	return false; // nothing good about this.
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
	boolean isGood() { return param>=0 || constant!=null; }
	void merge(Classification c) {
	    if (this.param != c.param ||
		(this.constant==null) != (c.constant==null) ||
		(this.constant!=null && !this.constant.equals(c.constant))) {
		// goes to top.
		this.param=-1; this.constant=null;
	    }
	}
	/** Take this classification and filter it through a mapping
	 *  of params to other classifications. */
	void map(Classification[] mapping) {
	    if (this.param==-1) return; // not a parameter! nothing to do!
	    int p = this.param; // entry in the mapping to utilize.
	    // make this a clone of mapping[p]
	    this.param = mapping[p].param;
	    this.constant = mapping[p].constant;
	    return; // done.
	}
	public String toString() {
	    Util.assert(!(param>0 && constant!=null));
	    if (param>0) return "PARAM#"+param;
	    if (constant!=null) return "CONSTANT "+constant;
	    return "NO INFO";
	}
    }
    Classification makeClassification(SCCAnalysis scc, MustParamOracle mpo,
				      HCodeElement hce, Temp t) {
	if (scc.isConst(hce, t)) // constant value?
	    return new Classification(scc.constMap(hce, t));
	if (mpo.isMustParam(t)) // must be a parameter?
	    return new Classification(mpo.whichMustParam(t));
	return new Classification(); // no info
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
		    // okay, is the value we're setting a constant? or param?
		    Classification nc =
			makeClassification(scc, mpo, qq, qq.src());
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
		    // construct parameter mapping.
		    Classification pc[]= new Classification[qq.paramsLength()];
		    for (int i=0; i<pc.length; i++)
			pc[i] = makeClassification(scc, mpo, qq, qq.params(i));
		    // filter method's classifications through the mapping and
		    // merge with our previous classifications.
		    for (Iterator it2=m.entrySet().iterator(); it2.hasNext();){
			Map.Entry me = (Map.Entry) it2.next();
			HField hf = (HField) me.getKey();
			Classification oc = (Classification) map.get(hf);
			Classification nc = (Classification) me.getValue();
			nc.map(pc); // map method's params to this' params.
			if (oc!=null)
			    nc.merge(oc); // merge if necessary.
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
