// SCC.java, created Fri Sep 18 17:45:07 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.SizeOpt;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Maps.ConstMap;
import harpoon.Analysis.Maps.ExactTypeMap;
import harpoon.Analysis.Maps.ExecMap;
import harpoon.Analysis.Maps.UseDefMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ALENGTH;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.COMPONENTOF;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.NOP;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.OperVisitor;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.SWITCH;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.HClassUtil;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;
import harpoon.Util.Worklist;
import harpoon.Util.Collections.AggregateSetFactory;
import harpoon.Util.Collections.Factories;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>BitWidthAnalysis</code> implements Sparse Conditional Constant
 * Propagation, with extensions to allow type and bitwidth analysis.
 * It combines the intraprocedural SCC analysis with an interprocedural
 * driver to infer the widths of object fields.
 * <p>Only works with quads in SSI form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: BitWidthAnalysis.java,v 1.1.2.5 2001-07-19 22:20:12 cananian Exp $
 */

public class BitWidthAnalysis implements ExactTypeMap, ConstMap, ExecMap {
    final static boolean DEBUG = false;
    final Linker linker;
    final HCodeFactory hcf;
    final ClassHierarchy ch;

    /** Creates a <code>BitWidthAnalysis</code>. */
    public BitWidthAnalysis(Linker linker, HCodeFactory hcf,
			    ClassHierarchy ch, Set roots) {
	Util.assert(hcf.getCodeName().equals(QuadSSI.codename));
	this.linker = linker;
	this.hcf = hcf;
	this.ch = ch;
	analyze(roots);
	/* accounting: */
	long before=0, before8=0, after=0, after8=0;
	for (Iterator it=Vf.keySet().iterator(); it.hasNext(); ) {
	    HField hf = (HField) it.next();
	    HClass ty = hf.getType();
	    if (ty==HClass.Byte) { before+=8+7; before8+=1; }
	    else if (ty==HClass.Short) { before+=16+15; before8+=2; }
	    else if (ty==HClass.Char) { before+=0+16; before8+=2; }
	    else if (ty==HClass.Boolean) { before+=0+1; before8+=1; }
	    else if (ty==HClass.Int) { before+=32+31; before8+=4; }
	    else if (ty==HClass.Long) { before+=64+63; before8+=8; }
	    else continue; // not an integer type.
	    xBitWidth small= extractWidth((LatticeVal) Vf.get(hf));
	    after+=small.plusWidth()+small.minusWidth();
	    // round to byte allocations.
	    int bytes = (Math.max(small.minusWidth(),small.plusWidth())+7)/8;
	    after8 += bytes==3 ? 4 : bytes;
	}
	System.out.println("BITWIDTH RESULTS: "+before+"/"+after+" bits; "+
			   before8+"/"+after8+" bytes");
    }

    /*-----------------------------*/
    // Class state.
    /** Set of all executable edges. */
    final Set Ee = new HashSet();
    /** Set of all executable quads. */
    final Set Eq = new HashSet();
    /** Mapping from <code>Temp</code>s to lattice values. */
    final Map V = new HashMap();
    /** Mapping from <code>HField</code>s to lattice values. */
    final Map Vf = new HashMap();
    /** Mapping from <code>Temp</code>s to <code>Quad</code>s which use them.*/
    final MultiMap useMap = new GenericMultiMap(Factories.arrayListFactory);
    /** Mapping from <code>HField</code>s to <code>Quad</code>s which read
	them. */
    final MultiMap fieldMap = new GenericMultiMap(new AggregateSetFactory());
    /** Mapping from <code>HMethod</code>s to <code>METHOD</code>s. */
    final Map methodMap = new HashMap();
    /** Mapping from <code>HMethod</code>s to <code>CALL</code> quads
     *  which may invoke them. */
    final MultiMap callMap = new GenericMultiMap(new AggregateSetFactory());

    /*---------------------------*/
    // public information accessor methods.

    /** Determine whether <code>Quad</code> <code>q</code>
     *  is executable. */
    public boolean execMap(HCodeElement quad) {
	// ignore hc
	return Eq.contains(quad);
    }
    /** Determine whether <code>Edge</code> <code>e</code>
     *  is executable. */
    public boolean execMap(HCodeEdge edge) {
	// ignore hc
	return Ee.contains(edge);
    }
    /** Determine the static type of <code>Temp</code> <code>t</code> in 
     *  <code>HMethod</code> <code>m</code>. */
    public HClass typeMap(HCodeElement hce, Temp t) {
	// ignore hce
	LatticeVal v = (LatticeVal) V.get(t);
	if (v instanceof xClass) return ((xClass)v).type();
	return null;
    }
    /** Determine whether the static type of <code>Temp</code> <code>t</code>
     *  defined at <code>hce</code> is exact (or whether the runtime type
     *  could be a subclass of the static type). */
    public boolean isExactType(HCodeElement hce, Temp t) {
	// ignore hce
	return V.get(t) instanceof xClassExact;
    }
    /** Determine whether the given <code>Temp</code> can possibly be
     *  <code>null</code>. */
    public boolean isPossiblyNull(HCodeElement hce, Temp t) {
	return !(V.get(t) instanceof xClassNonNull);
    }
    /** Determine whether <code>Temp</code> <code>t</code>
     *  has a constant value. */
    public boolean isConst(HCodeElement hce, Temp t) {
	// ignore hce -- this is SSA form
	return (V.get(t) instanceof xConstant);
    }
    /** Determine the constant value of <code>Temp</code> <code>t</code>.
     *  @exception Error if <code>Temp</code> <code>t</code> is not a constant.
     */
    public Object constMap(HCodeElement hce, Temp t) {
	// ignore hce -- this is SSA form.
	LatticeVal v = (LatticeVal) V.get(t);
	if (v instanceof xConstant) return ((xConstant)v).constValue();
	throw new Error(t.toString() + " not a constant");
    }

    /** Determine the positive bit width of <code>Temp</code> <code>t</code>.
     */
    public int plusWidthMap(HCodeElement hce, Temp t) {
	// ignore hce -- this is SSA form
	LatticeVal v = (LatticeVal) V.get(t);
	if (v==null) throw new Error("Unknown "+t);
	xBitWidth bw = extractWidth(v);
	return bw.plusWidth();
    }
    /** Determine the negative bit width of <code>Temp</code> <code>t</code>.
     */
    public int minusWidthMap(HCodeElement hce, Temp t) {
	// ignore hce -- this is SSA form.
	LatticeVal v = (LatticeVal) V.get(t);
	if (v==null) throw new Error("Unknown "+t);
	xBitWidth bw = extractWidth(v);
	return bw.minusWidth();
    }

    /** Create methodMap, useMap */
    private void scan_one(HMethod hm) {
	HCode hc = hcf.convert(hm);
	if (hc==null) return; // abstract method.
	if (DEBUG) System.out.println("SCAN_ONE: "+hm);
	for (Iterator it=hc.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    // add entries to useMap.
	    Temp[] used = q.use();
	    for (int i=0; i<used.length; i++)
		useMap.add(used[i], q);
	    // add entry to methodMap
	    if (q instanceof METHOD)
		methodMap.put(hm, q);
	}
    }

    /*---------------------------*/
    // Analysis code.

    /** Main analysis method. */
    private void analyze(Set roots) {
	// Initialize worklists.
	Worklist Wv = new WorkSet(); // variable worklist.
	Worklist Wq = new WorkSet(); // block worklist.
	Worklist Wf = new WorkSet(); // field worklist.

	// Make instance of visitor class.
	SCCVisitor visitor = new SCCVisitor(Wv, Wq, Wf);

	// make root methods set (ignore classes)
	List root_methods = new ArrayList(roots);
	for (Iterator it=root_methods.iterator(); it.hasNext(); )
	    if (!(it.next() instanceof HMethod))
		it.remove();
	// all static initializers to root methods set
	for (Iterator it=ch.classes().iterator(); it.hasNext(); ) {
	    HMethod hm = ((HClass)it.next()).getClassInitializer();
	    if (hm!=null) root_methods.add(hm);
	}
	// put all root methods on the worklist & mark as executable.
	for (Iterator it=root_methods.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    scan_one(hm);
	    METHOD method = (METHOD) methodMap.get(hm);
	    if (method==null) continue; // native method in root set.
	    Wq.push(method);
	    Eq.add(method);
	    // set up parameters.
	    int j=0;
	    if ( !hm.isStatic() ) // raise 'this' variable (non-null!)
		raiseV(V, Wv, method.params(j++),
		       new xClassNonNull( hm.getDeclaringClass() ) );
	    HClass[] pt = hm.getParameterTypes();
	    for (int k=0; k < pt.length; j++, k++)
		raiseV(V, Wv, method.params(j),
		       pt[k].isPrimitive() ?
		       new xClassNonNull( toInternal(pt[k]) ) :
		       new xClass( pt[k] ) );
	}
	// XXX: main method *could* use xClassExact on String[] arg.

	// Iterate until worklists are empty.
	while (! (Wq.isEmpty() && Wv.isEmpty() && Wf.isEmpty()) ) {

	    if (!Wq.isEmpty()) { // grab statement from We if we can.
		Quad q = (Quad) Wq.pull();
		// Rule 2: for any executable block with
		// only one successor C, set edge leading to C executable.
		if (q.next().length==1) {
		    raiseE(Ee, Eq, Wq, q.nextEdge(0));
		}
		// check conditions 3-8 for q.
		q.accept(visitor);
	    } 

	    if (!Wv.isEmpty()) { // grab temp from Wv if possible.
		Temp t = (Temp) Wv.pull();
		// for every use of t...
		for (Iterator it=useMap.getValues(t).iterator(); it.hasNext();)
		    // check conditions 3-8
		    ((Quad) it.next()).accept(visitor);
	    }

	    if (!Wf.isEmpty()) { // grab field from Wf if possible.
		HField hf = (HField) Wf.pull();
		// for every read of hf...
		for (Iterator it=fieldMap.getValues(hf).iterator();
		     it.hasNext(); )
		    // check conditions 3-8
		    ((Quad) it.next()).accept(visitor);
	    }
	} // end while loop.
    } // end analysis.

    /*----------------------------------------*/
    // raising values in the lattice:

    /** Raise edge e in Ee/Eq, adding target q to Wq if necessary. */
    void raiseE(Set Ee, Set Eq, Worklist Wq, Edge e) {
	Quad q = (Quad) e.to();
	Ee.add(e);
	if (Eq.contains(q)) return;
	Eq.add(q);
	Wq.push(q);
    }
    /** Raise element t to a in V, adding t to Wv if necessary. */
    void raiseV(Map V, Worklist Wv, Temp t, LatticeVal a) {
	Util.assert(a!=null);
	LatticeVal old = get( t );
	if (corruptor!=null) a=corruptor.corrupt(a); // support incrementalism
	// only allow raising value in lattice.
	if (old != null) {
	    a = a.merge(old);
	    if (old.equals(a) && a.equals(old)) return; // same old same old
	}
	V.put(t, a);
	Wv.push(t);
    }
    /** Merge a with type of element t in V, adding t to Wv if necessary. */
    void mergeV(Map V, Worklist Wv, Temp t, LatticeVal a) {
	raiseV(V, Wv, t, a);
    }
    /** Merge a with type of field hf in Vf, adding reads of hf to Wf if
	necessary. */
    void mergeV(Map V, Worklist Wf, HField hf, LatticeVal a) {
	Util.assert(a!=null);
	LatticeVal old = get( hf );
	if (corruptor!=null) a=corruptor.corrupt(a); // support incrementalism
	if (old != null) {
	    a = a.merge(old);
	    if (old.equals(a) && a.equals(old)) return; // no change.
	}
	Vf.put(hf, a);
	Wf.push(hf);
    }

    // utility functions.
    LatticeVal get(Temp t) { return (LatticeVal) V.get(t); }
    LatticeVal get(HField hf) {
	// if value in cache, use it.
	if (Vf.containsKey(hf)) return (LatticeVal) Vf.get(hf);
	HClass type = toInternal(hf.getType());
	// deal with constant fields.
	if (hf.isConstant()) {
	    Object val = hf.getConstant();
	    if (type == linker.forName("java.lang.String"))
		return new xStringConstant(type, val);
	    else if (type == HClass.Float || type == HClass.Double )
		return new xFloatConstant(type, val);
	    else if (type == HClass.Int || type == HClass.Long)
		return new xIntConstant(type,((Number)val).longValue() );
	    else throw new Error("Unknown constant field type: "+type);
	    }
	// final fields will be explicitly initialized.
	if (Modifier.isFinal(hf.getModifiers()))
	    return null; // bottom
	// else assume that field is set to zero upon object creation.
	if (!type.isPrimitive()) return new xNullConstant();
	if (type==HClass.Float)
	    return new xFloatConstant(type, new Float(0.0));
	if (type == HClass.Double)
	    return new xFloatConstant(type, new Double(0.0));
	if (type == HClass.Int || type==HClass.Long)
	    return new xIntConstant(type, 0 );
	else throw new Error("Unknown field type: "+type);
    }

    /*------------------------------------------------------------*/
    // VISITOR CLASS (the real guts of the routine)
    class SCCVisitor extends QuadVisitor {
	// local references to worklists.
	final Worklist Wv, Wq, Wf;
	// give us an OperVisitor class to go along with this.
	final OperVisitor opVisitor = new SCCOpVisitor();

	SCCVisitor(Worklist Wv, Worklist Wq, Worklist Wf) {
	    this.Wv = Wv;  this.Wq = Wq; this.Wf = Wf;
	}

	void handleSigmas(CJMP q, boolean falseTaken, boolean trueTaken) {
	    // for every sigma source:
	    for (int i=0; i < q.numSigmas(); i++) {
		LatticeVal v = get( q.src(i) );
		if (v == null) continue; // skip: insufficient info.
		// check if this is the CJMP condition.
		if (useSigmas && q.test() == q.src(i)) {
		    // we know that the conditional is zero on the false leg
		    if (falseTaken)
			raiseV(V, Wv, q.dst(i,0),
			       new xIntConstant(toInternal(HClass.Boolean),0));
		    // CJMP test is possibly non-boolean, so we don't in fact
		    // know the value of the true side (except that it is
		    // non-zero)
		    if (trueTaken) raiseV(V, Wv, q.dst(i,1), v.rename(q, 1));
		} else {
		    // fall back.
		    if (falseTaken) raiseV(V, Wv, q.dst(i,0), v.rename(q, 0));
		    if (trueTaken) raiseV(V, Wv, q.dst(i,1), v.rename(q, 1));
		}
	    }
	}
	void handleSigmas(CJMP q, xInstanceofResult io,
			  boolean falseTaken, boolean trueTaken) {
	    // for every sigma source:
	    for (int i=0; i < q.numSigmas(); i++) {
		// check if this is the CJMP condition.
		if (q.test() == q.src(i)) { // known value after branch
		    if (falseTaken) raiseV(V, Wv, q.dst(i,0),
					   io.makeKnown(false).rename(q,0));
		    if (trueTaken) raiseV(V, Wv, q.dst(i,1),
					  io.makeKnown(true).rename(q,1));
		    continue; // go on.
		}

		LatticeVal v = get( q.src(i) );
		if (v == null) continue; // skip: insufficient info.

		// check to see if this is the temp tested by INSTANCEOF
		if (q.src(i) == io.tested()) {
		    // no new info on false branch.
		    if (falseTaken)
			raiseV(V, Wv, q.dst(i,0), v.rename(q, 0));
		    // we know q.dst[i][1] is INSTANCEOF def.hclass
		    // secret inside info: INSTANCEOF src is always non-null.
		    HClass hcI = io.def().hclass();
		    HClass hcV = ((xClass)v).type();
		    // use more specific type
		    if (!hcI.isInterface() && !hcV.isInterface() &&
			hcI.isSuperclassOf(hcV))
			hcI = hcV;
		    LatticeVal nv = new xClassNonNull(hcI);
		    // use more specific of original class, instanceof.
		    if (v.isLowerThan(nv)) nv = v;
		    if (trueTaken)
			raiseV(V, Wv, q.dst(i,1), nv);
		} else {
		    // fall back.
		    if (falseTaken) raiseV(V, Wv, q.dst(i,0), v.rename(q, 0));
		    if (trueTaken) raiseV(V, Wv, q.dst(i,1), v.rename(q, 1));
		}
	    }
	}
	void handleSigmas(CJMP q, xOperBooleanResult or,
			  boolean falseTaken, boolean trueTaken) {
	    int opc = or.def().opcode();
	    int opa = or.operands().length;
	    LatticeVal left = opa<1?null:get(or.operands()[0]);
	    LatticeVal right= opa<2?null:get(or.operands()[1]);

	    // for every sigma source:
	    for (int i=0; i < q.numSigmas(); i++) {
		// check if this is the CJMP condition.
		if (q.test() == q.src(i)) {
		    if (falseTaken) raiseV(V, Wv, q.dst(i,0),
					   or.makeKnown(false).rename(q,0));
		    if (trueTaken) raiseV(V, Wv, q.dst(i,1),
					  or.makeKnown(true).rename(q,1));
		    continue; // go on.
		}

		LatticeVal v = get( q.src(i) );
		if (v == null) continue; // skip: insufficient info.

		// check to see if it comes from the OPER defining the boolean.
		boolean handled = false;
		boolean leftIsSource = false, swapped = false;
		if (q.src(i) == or.operands()[0]) { // left is source.
		    leftIsSource = true;
		} else if (q.src(i) == or.operands()[1]) { // right is source.
		    LatticeVal t = left; left = right; right = t;
		    leftIsSource = true; swapped = true;
		}
		if (leftIsSource) {
		    if (opc == Qop.ACMPEQ &&
			left  instanceof xClass && // not already xClassNonNull
			(!(left instanceof xNullConstant)) &&
			right instanceof xNullConstant) {
			if (falseTaken)
			    raiseV(V, Wv, q.dst(i,0), // false branch: non-null
				   new xClassNonNull( ((xClass)left).type() ));
			if (trueTaken)
			    raiseV(V, Wv, q.dst(i,1), // true branch: null
				   new xNullConstant() );
			handled = true;
		    } else if ((opc == Qop.ICMPEQ || opc == Qop.LCMPEQ ||
				opc == Qop.FCMPEQ || opc == Qop.DCMPEQ) &&
			       right instanceof xConstant) {
			if (falseTaken)
			    raiseV(V, Wv, q.dst(i,0), // false branch: no info
				   left.rename(q, 0));
			if (trueTaken)
			    raiseV(V, Wv, q.dst(i,1), // true branch: constant!
				   right.rename(q, 1));
			handled = true;
		    } else if ((opc == Qop.ICMPGT || opc == Qop.LCMPGT ) &&
			       right instanceof xBitWidth) {
			xBitWidth bw = (xBitWidth) right;
			xBitWidth sr = extractWidth(left);
			xBitWidth lessThan = new xBitWidth
			    (sr.type(),
			     sr.minusWidth(),
			     Math.min(sr.plusWidth(), bw.plusWidth()) );
			xBitWidth greaterThan = new xBitWidth
			    (sr.type(),
			     Math.min(sr.minusWidth(),bw.minusWidth()),
			     sr.plusWidth() );
			// use more specific of original class, xBitWidth.
			if (left.isLowerThan(lessThan))
			    lessThan = (xBitWidth) left;
			if (left.isLowerThan(greaterThan))
			    greaterThan = (xBitWidth) left;
			// false branch:
			if (falseTaken)
			    raiseV(V, Wv, q.dst(i,0),
				   swapped ? greaterThan : lessThan);
			// true branch.
			if (trueTaken)
			    raiseV(V, Wv, q.dst(i,1),
				   swapped ? lessThan : greaterThan);
			handled = true;
		    }
		}
		// fall back.
		if (!handled) {
		    if (falseTaken) raiseV(V, Wv, q.dst(i,0), v.rename(q, 0));
		    if (trueTaken) raiseV(V, Wv, q.dst(i,1), v.rename(q, 1));
		}
	    }
	}

	// visitation.
	public void visit(Quad q) { /* do nothing. */ }
	public void visit(AGET q) {
	    LatticeVal v = get( q.objectref() );
	    if (corruptor==null)
		Util.assert(v==null || v instanceof xClassNonNull);
	    if (v instanceof xClass)
		raiseV(V, Wv, q.dst(), 
		       new xClass( toInternal( ((xClass)v).type().getComponentType() ) ) );
	}
	public void visit(ALENGTH q) {
	    LatticeVal v = get( q.objectref() );
	    if (corruptor==null)
		Util.assert(v==null || v instanceof xClassNonNull);
	    if (v instanceof xClassArray)
		raiseV(V, Wv, q.dst(),
		       new xIntConstant(HClass.Int, 
					((xClassArray)v).length() ) );
	    else if (v instanceof xClass) // length is non-negative.
		raiseV(V, Wv, q.dst(), new xBitWidth(HClass.Int, 0, 32) );
	}
	public void visit(ANEW q) { // dst of ANEW is non-null.
	    if (q.dimsLength()==1) {
		LatticeVal v = get( q.dims(0) );
		if (v instanceof xIntConstant) {
		    raiseV(V, Wv, q.dst(), 
			   new xClassArray(q.hclass(), 
					   (int) ((xIntConstant)v).value()) );
		    return;
		} else if (v == null) return; // bottom.
	    }
	    raiseV(V, Wv, q.dst(), new xClassExact(q.hclass()) );
	}
	public void visit(ASET q) {
	    LatticeVal v = get( q.objectref() );
	    if (corruptor==null)
		Util.assert(v==null || v instanceof xClassNonNull);
	    /* do nothing. */
	}
	public void visit(CALL q) {
	    if (corruptor==null)
		Util.assert(q.isVirtual() ?
			    get( q.params(0) )==null ||
			    get( q.params(0) ) instanceof xClassNonNull : true,
			    q);
	    // don't analyze this quad until all params are known.
	    for (int i=0; i<q.paramsLength(); i++)
		if (get(q.params(i))==null)
		    return;
	    // find methods callable from this site.
	    HMethod hm = q.method();
	    List callable = new ArrayList(4);
	    if (q.isVirtual()) { // need type info for virtual methods.
		Util.assert(!hm.isStatic());
		LatticeVal v = get( q.params(0) );
		HClass ty = ((xClass) v).type();
		Util.assert(!ty.isPrimitive(), v);
		// when hm.getDeclaringClass() is an interface, the
		// implementations may not share a common superclass which
		// implements that interface.
		if (!ty.isInstanceOf(hm.getDeclaringClass()))
		    ty = hm.getDeclaringClass(); // always safe to fall back.
		hm = ty.getMethod(hm.getName(), hm.getDescriptor());
		if (!(v instanceof xClassExact))
		    callable.addAll(ch.overrides(ty,hm,true));
	    }
	    callable.add(hm);
	    // for every callable method, raise its Vparam.
	    // flag if any callable methods are native.
	    boolean anyNative = true;//false;
	    Temp[] myparams = q.params();
	    for (Iterator it=callable.iterator(); it.hasNext(); ) {
		HMethod hmm = (HMethod) it.next();
		callMap.add(hmm, q); // keep callMap updated.
		if (Modifier.isNative(hmm.getModifiers())) anyNative = true;
		if (!methodMap.containsKey(hmm)) scan_one(hmm);
		METHOD method = (METHOD) methodMap.get(hmm);
		if (method==null) continue; // abstract or native method.
		for (int i=0; i<myparams.length; i++)
		    mergeV(V, Wv, method.params(i), get( myparams[i] ));
		// also mark "method" executable.
		Wq.push(method); Eq.add(method);
		// analysis of "method" (in particular, the RETURN/THROW
		// quads) will raiseE on appropriate outgoing edge and
		// raiseV on retval/retex.
	    }
	    if (!anyNative) return; // done.
	    // if *native* method in callable methods set, then use
	    // conservative retval/retex/edge assumptions.
	    if (q.retval() != null) {
		// in the bytecode world, everything's an int.
		HClass ty = q.method().getReturnType();
		LatticeVal v = new xClass(toInternal(ty));
		if (ty==HClass.Byte)
		    v = new xBitWidth(toInternal(HClass.Byte),  8,  7);
		else if (ty==HClass.Short)
		    v = new xBitWidth(toInternal(HClass.Short), 16, 15);
		else if (ty==HClass.Char)
		    v = new xBitWidth(toInternal(HClass.Char),  0, 16);
		else if (ty==HClass.Boolean)
		    v = new xBitWidth(toInternal(HClass.Boolean),0, 1);
		else if (ty.isPrimitive())
		    v = new xClassNonNull(toInternal(ty));
		mergeV(V, Wv, q.retval(), v);
	    }
	    mergeV(V, Wv, q.retex(), 
		   new xClassNonNull( linker.forName("java.lang.Throwable") ));
	    // both outgoing edges are potentially executable.
	    raiseE(Ee, Eq, Wq, q.nextEdge(1) );
	    raiseE(Ee, Eq, Wq, q.nextEdge(0) );
	    // handle SIGMAs
	    for (int i=0; i < q.numSigmas(); i++) {
		// no q.src(x) should equal retval or retex...
		// not that it would particularly break anything if it
		// did.
		LatticeVal v2 = get ( q.src(i) );
		if (v2 != null) {
		    raiseV(V, Wv, q.dst(i, 0), v2.rename(q, 0));
		    raiseV(V, Wv, q.dst(i, 1), v2.rename(q, 1));
		}
	    }
	}
	public void visit(CJMP q) {
	    // is test constant?
	    LatticeVal v = get( q.test() );
	    if (v instanceof xIntConstant) {
		boolean test = ((xIntConstant)v).value()!=0;

		if (test)
		    raiseE(Ee, Eq, Wq, q.nextEdge(1) ); // true edge.
		else
		    raiseE(Ee, Eq, Wq, q.nextEdge(0) ); // false edge.
		// handle sigmas.
		if (useSigmas && v instanceof xOperBooleanResult)
		    handleSigmas((CJMP) q, (xOperBooleanResult)v,
				 !test, test);
		else if (useSigmas && v instanceof xInstanceofResult)
		    handleSigmas((CJMP) q, (xInstanceofResult) v,
				 !test, test);
		else // fallback.
		    handleSigmas((CJMP) q, !test, test);
		return; // done.
	    } else if (v instanceof xClass) { // ie, not bottom.
		// both edges are potentially executable.
		raiseE(Ee, Eq, Wq, q.nextEdge(1) );
		raiseE(Ee, Eq, Wq, q.nextEdge(0) );

		// look at definition of boolean condition.
		if (useSigmas && v instanceof xOperBooleanResult)
		    handleSigmas((CJMP) q, (xOperBooleanResult)v, true, true);
		else if (useSigmas && v instanceof xInstanceofResult)
		    handleSigmas((CJMP) q, (xInstanceofResult) v, true, true);
		else // fallback.
		    handleSigmas((CJMP) q, true, true);
	    }
	}
	public void visit(COMPONENTOF q) {
	    // we're guaranteed that q.arrayref is non-null here.
	    LatticeVal vA = get( q.arrayref() );
	    LatticeVal vO = get( q.objectref() );
	    if (vA instanceof xClass && vO instanceof xClass) {
		HClass hcA = ((xClass) vA).type().getComponentType() ;
		HClass hcO = ((xClass) vO).type();
		if (hcA==null) { // can't prove type is array; usually this
		                 // means we've turned useSigmas off.
		    raiseV(V, Wv, q.dst(), new xBitWidth(toInternal(HClass.Boolean),0,1));
		    return;
		}
		hcA = toInternal(hcA); // normalize external types.
		// special case when q.objectref is null
		if (hcO == HClass.Void) // always true.
		    raiseV(V, Wv, q.dst(), new xIntConstant(toInternal(HClass.Boolean),1));
		else if (vA instanceof xClassExact &&
			 hcO.isInstanceOf(hcA)) // always true
		    raiseV(V, Wv, q.dst(), new xIntConstant(toInternal(HClass.Boolean),1));
		else if (vO instanceof xClassExact &&
			 !hcO.isInstanceOf(hcA)) // always false
		    raiseV(V, Wv, q.dst(), new xIntConstant(toInternal(HClass.Boolean),0));
		else if (hcO.isInstanceOf(hcA) ||
			 hcA.isInstanceOf(hcO)) // unknowable.
		    raiseV(V, Wv, q.dst(), new xBitWidth(toInternal(HClass.Boolean),0,1));
		else // always false.
		    raiseV(V, Wv, q.dst(), new xIntConstant(toInternal(HClass.Boolean),0));
	    }
	}
	public void visit(CONST q) {
	    if (q.type() == HClass.Void) // null constant
		raiseV(V,Wv, q.dst(), new xNullConstant() );
	    else if (q.type()==linker.forName("java.lang.String"))// string constant
		raiseV(V,Wv, q.dst(), new xStringConstant(q.type(),q.value()));
	    else if (q.type()==HClass.Float || q.type()==HClass.Double) // f-p
		raiseV(V,Wv, q.dst(), new xFloatConstant(q.type(),q.value()) );
	    else if (q.type()==HClass.Int || q.type() == HClass.Long)
		raiseV(V,Wv, q.dst(), 
		       new xIntConstant(q.type(),
					((Number)q.value()).longValue()));
	    else if (q.type()==linker.forName("java.lang.Class") ||
		     q.type()==linker.forName("java.lang.reflect.Field") ||
		     q.type()==linker.forName("java.lang.reflect.Method"))
		raiseV(V,Wv, q.dst(), new xClassNonNull( q.type() ) );
	    else throw new Error("Unknown CONST type: "+q.type());
	}
	public void visit(FOOTER q) { /* do nothing. */ }
	public void visit(GET q) {
	    if (corruptor==null)
		Util.assert(q.objectref()!=null ?
			    get(q.objectref())==null ||
			    get(q.objectref()) instanceof xClassNonNull : true,
			    q);
	    // variable gets current lattice val of field.
	    LatticeVal v = get( q.field() );
	    if (v==null) return; // wait for field initialization.
	    raiseV(V, Wv, q.dst(), v);
	    // add to list of reading quads.
	    fieldMap.add(q.field(), q);
	    if (DEBUG) System.out.println("READ OF "+q.field()+" GETS "+get( q.field() ));
	}
	public void visit(HEADER q) {
	    Util.assert(false); /* we should "skip to the METHOD" */
	}
	public void visit(INSTANCEOF q) {
	    // no guarantee that src is not null.
	    LatticeVal v = get( q.src() );
	    if (v instanceof xNullConstant) // always false.
		raiseV(V, Wv, q.dst(), new xInstanceofResultKnown(q,false));
	    else if (v instanceof xClassNonNull) { // analyzable
		HClass hcO = ((xClassNonNull)v).type();
		if (hcO.isInstanceOf(q.hclass())) // always true
		    raiseV(V,Wv, q.dst(), new xInstanceofResultKnown(q,true));
		else if (q.hclass().isInstanceOf(hcO)) // unknowable.
		    raiseV(V,Wv, q.dst(), new xInstanceofResultUnknown(q));
		else // always false.
		    raiseV(V,Wv, q.dst(), new xInstanceofResultKnown(q,false));
	    }
	    else if (v instanceof xClass) { // could be null.
		HClass hcO = ((xClass)v).type();
		if (q.hclass().isInstanceOf(hcO) || 
		    hcO.isInstanceOf(q.hclass()) ) // unknowable.
		    raiseV(V,Wv, q.dst(), new xInstanceofResultUnknown(q));
		else // always false (even if src==null)
		    raiseV(V,Wv, q.dst(), new xInstanceofResultKnown(q,false));
	    }
	}
	public void visit(METHOD q) {
	    /* do very little */
	    Util.assert(methodMap.get(q.getFactory().getMethod())==q);
	    if (DEBUG) System.out.println("METHOD: "+q.getFactory().getMethod());
	}
	public void visit(MONITORENTER q) {
	    LatticeVal v = get( q.lock() );
	    Util.assert(v==null || v instanceof xClassNonNull);
	    /* do nothing. */
	}
	public void visit(MONITOREXIT q) {
	    LatticeVal v = get( q.lock() );
	    Util.assert(v==null || v instanceof xClassNonNull);
	    /* do nothing. */
	}
	public void visit(MOVE q) {
	    LatticeVal v = get ( q.src() );
	    if (v != null)
		raiseV(V, Wv, q.dst(), v);
	}
	public void visit(NEW q) {
	    raiseV(V, Wv, q.dst(), new xClassExact( q.hclass() ) );
	}
	public void visit(NOP q) { /* do nothing. */ }
	public void visit(OPER q) {
	    int opc = q.opcode();
	    boolean allConst = true;
	    boolean allWidth = true;

	    Object[] op = new Object[q.operandsLength()];
	    for (int i=0; i < q.operandsLength(); i++) {
		LatticeVal v = get( q.operands(i) );
		if (v==null) return; // can't eval yet.
		if (v instanceof xConstant)
		    op[i] = ((xConstant)v).constValue();
		else if (v instanceof xBitWidth)
		    allConst = false;
		else
		    allConst = allWidth = false;
	    }
	    if (allConst) {
		// RULE 3:
		HClass ty = q.evalType();
		Object o = q.evalValue(op);
		if (ty == HClass.Boolean)
		    raiseV(V, Wv, q.dst(),
			   new xOperBooleanResultKnown
			   (q,((Boolean)o).booleanValue()));
		else if (ty == HClass.Int || ty == HClass.Long)
		    raiseV(V, Wv, q.dst(), 
			   new xIntConstant(ty, ((Number)o).longValue() ) );
		else if (ty == HClass.Float || ty == HClass.Double)
		    raiseV(V, Wv, q.dst(), new xFloatConstant(ty, o) );
		else throw new Error("Unknown OPER result type: "+ty);
	    } else if ((allWidth) || 
		       opc == Qop.I2B || opc == Qop.I2C || opc == Qop.I2L || 
		       opc == Qop.I2S || opc == Qop.L2I) {
		// do something intelligent with the bitwidths.
		q.accept(opVisitor);
	    } else { // not all constant, not all known widths...
		// special-case ACMPEQ x, null
		if (opc == Qop.ACMPEQ &&
		    ((get( q.operands(0) ) instanceof xNullConstant &&
		      get( q.operands(1) ) instanceof xClassNonNull) ||
		     (get( q.operands(0) ) instanceof xClassNonNull &&
		      get( q.operands(1) ) instanceof xNullConstant) ) )
		    raiseV(V, Wv, q.dst(), // always false.
			   new xOperBooleanResultKnown(q, false));
		// special case boolean operations.
		else if (opc == Qop.ACMPEQ ||
			 opc == Qop.DCMPEQ || opc == Qop.DCMPGE ||
			 opc == Qop.DCMPGT ||
			 opc == Qop.FCMPEQ || opc == Qop.FCMPGE ||
			 opc == Qop.FCMPGT ||
			 opc == Qop.ICMPEQ || opc == Qop.ICMPGT ||
			 opc == Qop.LCMPEQ || opc == Qop.LCMPGT)
		    raiseV(V, Wv, q.dst(), new xOperBooleanResultUnknown(q));
		else {
		    // RULE 4:
		    HClass ty = q.evalType();
		    if (ty.isPrimitive())
			raiseV(V, Wv, q.dst(), new xClassNonNull( toInternal(ty) ) );
		    else
			raiseV(V, Wv, q.dst(), new xClass( ty ) );
		}
	    }
	}
	public void visit(PHI q) {
	    for (int i=0; i<q.numPhis(); i++) { // for each phi-function.
		LatticeVal merged = null;
		for (int j=0; j < q.arity(); j++) {
		    if (!Ee.contains( q.prevEdge(j) ))
			continue; // skip non-executable edges.
		    LatticeVal v = get ( q.src(i,j) );
		    if (v == null)
			continue; // skip this arg function.
		    v = v.rename(q, j);
		    if (merged == null)
			merged = v; // first valid value
		    else merged = merged.merge(v);
		}
		// assess results.
		if (merged == null)
		    continue; // nothing to go on.
		else raiseV(V, Wv, q.dst(i), merged);
	    } // for each phi function.
	}
	public void visit(RETURN q) {
	    if (get( q.retval() )==null) return; // wait for definition!
	    // for all CALLs which may invoke this method...
	    for (Iterator it=callMap.getValues(q.getFactory().getMethod())
		     .iterator(); it.hasNext(); ) {
		CALL call = (CALL) it.next();
		// mergeV on retval.
		if (q.retval()!=null)
		    mergeV(V, Wv, call.retval(), get( q.retval() ));
		// raiseE on appropriate outgoing edge.
		raiseE(Ee, Eq, Wq, call.nextEdge(0));
		// (don't forget sigmas)
		for (int i=0; i < call.numSigmas(); i++) {
		    LatticeVal v2 = get ( call.src(i) );
		    if (v2 != null)
			raiseV(V, Wv, call.dst(i, 0), v2.rename(call, 0));
		}
	    }
	}
	public void visit(SET q) {
	    if (corruptor==null)
		Util.assert(q.objectref()!=null ?
			    get(q.objectref())==null ||
			    get(q.objectref()) instanceof xClassNonNull : true,
			    q);
	    /* widen type of field */
	    LatticeVal v = get( q.src() );
	    if (v != null)
		mergeV(V, Wf, q.field(), v);
	    if (DEBUG) System.out.println("WRITE TO "+q.field()+" OF "+get( q.field() ));
	}
	public void visit(SWITCH q) {
	    LatticeVal v = get( q.index() );
	    if (v instanceof xIntConstant) {
		int index = (int) ((xIntConstant)v).value();
		int i;
		for (i=0; i<q.keysLength(); i++)
		    if (q.keys(i) == index)
			break;
		// now i has the target index, even for the default case.
		raiseE(Ee, Eq, Wq, q.nextEdge(i) ); // executable edge.
		// handle sigmas.
		for (int j=0; j < q.numSigmas(); j++) {
		    LatticeVal v2 = get( q.src(j) );
		    if (v2 != null)
			raiseV(V, Wv, q.dst(j,i), v2.rename(q,i));
		}
	    }
	    else if (v != null) {
		xBitWidth bw = extractWidth(v);
		// mark some edges executable & propagate to all sigmas.
		for (int j=0; j < q.nextEdge().length; j++) {
		    if (j<q.keysLength()) { // default edge always executable
			// learn stuff about cases from bitwidth of v.
			int k = q.keys(j);
			if (k>0 && Util.fls(k) > bw.plusWidth())
			    continue; // key too large to be selected.
			if (k<0 && Util.fls(-k) > bw.minusWidth())
			    continue; // key too small to be selected.
		    }
		    raiseE(Ee, Eq, Wq, q.nextEdge(j) );
		    for (int i=0; i < q.numSigmas(); i++) {
			LatticeVal v2 = get( q.src(i) );
			if (v2 != null)
			    raiseV(V, Wv, q.dst(i,j), v2.rename(q,j));
		    }
		}
	    }
	}
	public void visit(THROW q) {
	    if (corruptor==null)
		Util.assert(get(q.throwable())==null ||
			    get(q.throwable()) instanceof xClassNonNull);
	    if (get( q.throwable() )==null) return; // wait for definition!
	    // for all CALLs which may invoke this method...
	    for (Iterator it=callMap.getValues(q.getFactory().getMethod())
		     .iterator(); it.hasNext(); ) {
		CALL call = (CALL) it.next();
		// mergeV on retex.
		mergeV(V, Wv, call.retex(), get( q.throwable() ));
		// raiseE on appropriate outgoing edge.
		raiseE(Ee, Eq, Wq, call.nextEdge(1));
		// (don't forget sigmas)
		for (int i=0; i < call.numSigmas(); i++) {
		    LatticeVal v2 = get ( call.src(i) );
		    if (v2 != null)
			raiseV(V, Wv, call.dst(i, 1), v2.rename(call, 1));
		}
	    }
	}
	public void visit(TYPESWITCH q) {
	    LatticeVal v = get( q.index() );
	    if (v instanceof xClass) {
		HClass type = ((xClass)v).type();
		boolean catchAll = false;
		for (int i=0; i<q.keysLength(); i++) {
		    if (q.keys(i).isInstanceOf(type)) // executable
			raiseE(Ee, Eq, Wq, q.nextEdge(i) );
		    if (type.isInstanceOf(q.keys(i))) {// catches all remaining
			raiseE(Ee, Eq, Wq, q.nextEdge(i) );
			catchAll = true;
			break;
		    }
		}
		if ((!q.hasDefault()) ||
		    (catchAll && v instanceof xClassNonNull))
		    /* default edge never taken */;
		else // make the default case executable.
		    raiseE(Ee, Eq, Wq, q.nextEdge(q.keysLength()));

		// handle sigmas.
		for (int i=0; i < q.arity(); i++) {
		    if (!Ee.contains(q.nextEdge(i))) continue;//only raise exec
		    for (int j=0; j < q.numSigmas(); j++) {
			if (q.src(j)==q.index() && i<q.keysLength())
			    raiseV(V, Wv, q.dst(j,i),
				   new xClassNonNull(q.keys(i)));
			else {
			    LatticeVal v2 = get( q.src(j) );
			    if (v2 != null)
				raiseV(V, Wv, q.dst(j,i), v2.rename(q,i));
			}
		    }
		}
	    }
	    else if (v != null) {
		// mark all edges executable & propagate to all sigmas.
		for (int i=0; i < q.nextLength(); i++)
		    raiseE(Ee, Eq, Wq, q.nextEdge(i) );
		for (int i=0; i < q.numSigmas(); i++) {
		    LatticeVal v2 = get( q.src(i) );
		    if (v2 != null)
			for (int j=0; j < q.arity(); j++)
			    raiseV(V, Wv, q.dst(i,j), v2.rename(q,j));
		}
	    }
	}

	/*------------------------------------------------------------*/
	// VISITOR CLASS FOR OPER (ugh.  lots of cases)
	class SCCOpVisitor extends OperVisitor {

	    public void visit_default(OPER q) {
		HClass ty = q.evalType();
		if (ty.isPrimitive())
		    raiseV(V, Wv, q.dst(), new xClassNonNull( toInternal(ty) ) );
		else
		    raiseV(V, Wv, q.dst(), new xClass( ty ) );
	    }
	    // comparisons
	    void visit_cmpeq(OPER q) {
		xBitWidth left = extractWidth( get( q.operands(0) ) );
		xBitWidth right= extractWidth( get( q.operands(1) ) );
		// comparisons against a constant.
		if ((left instanceof xIntConstant &&// left a constant and
		     ((left.minusWidth()==0 &&      // right smaller than left.
		       right.plusWidth() < left.plusWidth()) ||
		      (left.plusWidth()==0 &&
		       right.minusWidth() < left.minusWidth()))) || // or...
		    (right instanceof xIntConstant &&// right a constant and
		     ((right.minusWidth()==0 &&     // left smaller than right.
		       left.plusWidth() < right.plusWidth()) ||
		      (right.plusWidth()==0 &&
		       left.minusWidth() < right.minusWidth()))) )
		    // okay, comparison can never be true.
		    raiseV(V, Wv, q.dst(),
			   new xOperBooleanResultKnown(q,false));
		else // okay, nothing known.
		    raiseV(V, Wv, q.dst(), new xOperBooleanResultUnknown(q));
	    }
	    public void visit_icmpeq(OPER q) { visit_cmpeq(q); }
	    public void visit_lcmpeq(OPER q) { visit_cmpeq(q); }
	    void visit_cmpgt(OPER q) {
		xBitWidth left = extractWidth( get( q.operands(0) ) );
		xBitWidth right= extractWidth( get( q.operands(1) ) );
		// comparisons against a non-zero constant.
		if ((left instanceof xIntConstant &&
		     ((xIntConstant)left).value()!=0 &&
		     left.plusWidth() > right.plusWidth()) ||
		    (right instanceof xIntConstant &&
		     ((xIntConstant)right).value()!=0 &&
		     right.minusWidth() > left.minusWidth()))
		    // comparison is always true
		    raiseV(V,Wv, q.dst(), new xOperBooleanResultKnown(q,true));
		else if ((left instanceof xIntConstant &&
			  ((xIntConstant)left).value()!=0 &&
			  left.minusWidth() > right.minusWidth()) ||
			 (right instanceof xIntConstant &&
			  ((xIntConstant)right).value()!=0 &&
			  right.plusWidth() > left.plusWidth()))
		    // comparison is always false
		    raiseV(V,Wv, q.dst(),new xOperBooleanResultKnown(q,false));
		// comparisons against zero.
		else if ((left instanceof xIntConstant &&
			  ((xIntConstant)left).value()==0 &&
			  right.minusWidth()==0) ||
			 (right instanceof xIntConstant &&
			  ((xIntConstant)right).value()==0 &&
			  left.plusWidth()==0))
		    // comparison is always false. 0 > 0+ or 0- > 0
		    raiseV(V,Wv, q.dst(),new xOperBooleanResultKnown(q,false));
		else // okay, nothing known.
		    raiseV(V, Wv, q.dst(), new xOperBooleanResultUnknown(q));
	    }
	    public void visit_icmpgt(OPER q) { visit_cmpgt(q); }
	    public void visit_lcmpgt(OPER q) { visit_cmpgt(q); }
	    // conversions
	    public void visit_i2b(OPER q) {
		xBitWidth bw = extractWidth( get( q.operands(0) ) );
		raiseV(V, Wv, q.dst(), 
		       new xBitWidth(HClass.Int, 
				     Math.min(8, bw.minusWidth()),
				     Math.min(7, bw.plusWidth()) ));
	    }
	    public void visit_i2c(OPER q) {
		xBitWidth bw = extractWidth( get( q.operands(0) ) );
		raiseV(V, Wv, q.dst(), 
		       new xBitWidth(HClass.Int, 0, 
				     Math.min(16, bw.plusWidth()) ));
	    }
	    public void visit_i2l(OPER q) {
		xBitWidth bw = extractWidth( get( q.operands(0) ) );
		raiseV(V, Wv, q.dst(), 
		       new xBitWidth(HClass.Long,
				     Math.min(32, bw.minusWidth()),
				     Math.min(31, bw.plusWidth()) ));
	    }
	    public void visit_i2s(OPER q) {
		xBitWidth bw = extractWidth( get( q.operands(0) ) );
		raiseV(V, Wv, q.dst(), 
		       new xBitWidth(HClass.Int,
				     Math.min(16, bw.minusWidth()),
				     Math.min(15, bw.plusWidth()) ));
	    }
	    public void visit_l2i(OPER q) {
		xBitWidth bw = extractWidth( get( q.operands(0) ) );
		raiseV(V, Wv, q.dst(), 
		       new xBitWidth(HClass.Int,
				     Math.min(32, bw.minusWidth()),
				     Math.min(31, bw.plusWidth()) ));
	    }
	    // binops
	    void visit_add(OPER q) {
		xBitWidth left = extractWidth( get( q.operands(0) ) );
		xBitWidth right= extractWidth( get( q.operands(1) ) );
		int m = Math.max( left.minusWidth(), right.minusWidth() );
		int p = Math.max( left.plusWidth(),  right.plusWidth() );
		// zero plus zero is always zero, but other numbers grow.
		if (m > 0) m++;
		if (p > 0) p++;
		// XXX special case 0+x: x doesn't grow.
		raiseV(V, Wv, q.dst(), new xBitWidth(q.evalType(), m, p) );
	    }
	    public void visit_iadd(OPER q) { visit_add(q); }
	    public void visit_ladd(OPER q) { visit_add(q); }

	    void visit_and(OPER q) {
		xBitWidth left = extractWidth( get( q.operands(0) ) );
		xBitWidth right= extractWidth( get( q.operands(1) ) );
		// if there are zero crossings, we have worst-case performance.
		int m = Math.max( left.minusWidth(), right.minusWidth() );
		int p = Math.max( left.plusWidth(),  right.plusWidth() );
		// check for special positive-number cases.
		if (left.minusWidth()==0 && right.minusWidth()==0)
		    p = Math.min( left.plusWidth(), right.plusWidth() );
		raiseV(V, Wv, q.dst(), new xBitWidth(q.evalType(), m, p) );
	    }
	    public void visit_iand(OPER q) { visit_and(q); }
	    public void visit_land(OPER q) { visit_and(q); }

	    void visit_div(OPER q) {
		// we can ignore divide-by-zero.
		xBitWidth left = extractWidth( get( q.operands(0) ) );
		xBitWidth right= extractWidth( get( q.operands(1) ) );
		// worst case: either number both pos and neg
		int m = Math.max(left.minusWidth(), left.plusWidth());
		int p = Math.max(left.minusWidth(), left.plusWidth());
		// check for special one-quadrant cases.
		if (left.minusWidth()==0) {
		    if (right.minusWidth()==0)  m=0; // result positive
		    if (right.plusWidth()==0)   p=0; // result negative
		}
		if (left.plusWidth()==0) {
		    if (right.minusWidth()==0)  m=0; // result negative
		    if (right.plusWidth()==0)   p=0; // result positive
		}
		// special case if divisor is a constant.
		if (right instanceof xIntConstant) {
		    if (right.minusWidth()==0) { // a positive constant
			m = Math.max(0, left.minusWidth() - right.plusWidth());
			p = Math.max(0, left.plusWidth()  - right.plusWidth());
		    }
		    if (right.plusWidth()==0) { // a negative constant
			m = Math.max(0, left.minusWidth()-right.minusWidth());
			p = Math.max(0, left.plusWidth() -right.minusWidth());
		    }
		}
		// done.
		raiseV(V, Wv, q.dst(), new xBitWidth(q.evalType(), m, p) );
	    }
	    public void visit_idiv(OPER q) { visit_div(q); }
	    public void visit_ldiv(OPER q) { visit_div(q); }

	    void visit_mul(OPER q) {
		xBitWidth left = extractWidth( get( q.operands(0) ) );
		xBitWidth right= extractWidth( get( q.operands(1) ) );
		// worst case: either number both pos and neg
		int m = Math.max(left.minusWidth() + right.plusWidth(),
				 left.plusWidth()  + right.minusWidth());
		int p = Math.max(left.minusWidth() + right.minusWidth(),
				 left.plusWidth()  + right.plusWidth());
		// special case multiplication by zero, one, and two.
		if (left instanceof xIntConstant) {
		    long val = ((xIntConstant)left).value();
		    if (val==0) {
			raiseV(V, Wv, q.dst(), left); return;
		    }
		    if (val==1) {
			raiseV(V, Wv, q.dst(), right); return;
		    }
		    if (val==2) {
			m = right.minusWidth()+1;
			p = right.plusWidth() +1;
		    }
		}
		if (right instanceof xIntConstant) {
		    long val = ((xIntConstant)right).value();
		    if (val==0) {
			raiseV(V, Wv, q.dst(), right); return;
		    }
		    if (val==1) {
			raiseV(V, Wv, q.dst(), left); return;
		    }
		    if (val==2) {
			m = left.minusWidth()+1;
			p = left.plusWidth() +1;
		    }
		}
		// XXX special case multiplication by one-bit quantities?
		// done.
		raiseV(V, Wv, q.dst(), new xBitWidth(q.evalType(), m, p) );
	    }
	    public void visit_imul(OPER q) { visit_mul(q); }
	    public void visit_lmul(OPER q) { visit_mul(q); }

	    void visit_neg(OPER q) {
		xBitWidth bw = extractWidth( get( q.operands(0) ) );
		int m = bw.plusWidth();
		int p = bw.minusWidth();
		raiseV(V, Wv, q.dst(), new xBitWidth(q.evalType(), m, p) );
	    }
	    public void visit_ineg(OPER q) { visit_neg(q); }
	    public void visit_lneg(OPER q) { visit_neg(q); }

	    void visit_or(OPER q) {
		xBitWidth left = extractWidth( get( q.operands(0) ) );
		xBitWidth right= extractWidth( get( q.operands(1) ) );
		// if there are zero crossings, we have worst-case performance.
		// XXX: check this "worst-case" computation; might be overly
		// conservative.  If definitely correct for the 
		// positive-number-only case.
		int m = Math.max( left.minusWidth(), right.minusWidth() );
		int p = Math.max( left.plusWidth(),  right.plusWidth() );
		raiseV(V, Wv, q.dst(), new xBitWidth(q.evalType(), m, p) );
	    }
	    public void visit_ior(OPER q) { visit_or(q); }
	    public void visit_lor(OPER q) { visit_or(q); }
	    /*
    public void visit_irem(OPER q) { visit_default(q); }
    public void visit_ishl(OPER q) { visit_default(q); }
    public void visit_ishr(OPER q) { visit_default(q); }
    public void visit_iushr(OPER q) { visit_default(q); }
    public void visit_ixor(OPER q) { visit_default(q); }
    public void visit_lrem(OPER q) { visit_default(q); }
    public void visit_lshl(OPER q) { visit_default(q); }
    public void visit_lshr(OPER q) { visit_default(q); }
    public void visit_lushr(OPER q) { visit_default(q); }
    public void visit_lxor(OPER q) { visit_default(q); }
	    */
	}
    }
    /*-------------------------------------------------------------*/
    // Extract bitwidth information from unwilling victims.
    xBitWidth extractWidth(LatticeVal v) {
	if (v instanceof xBitWidth)
	    return (xBitWidth) v;
	if (! (v instanceof xClass) )
	    throw new Error("Something's seriously screwed up.");
	xClass xc = (xClass) v;
	// trust xBitWidth to properly limit.
	return new xBitWidth(xc.type(), 1000, 1000);
    }

    // Deal with the fact that external Byte/Short/Char/Boolean classes
    // are represented internally as ints.

    static HClass toInternal(HClass c) {
	if (c.equals(HClass.Byte) || c.equals(HClass.Short) ||
	    c.equals(HClass.Char) || c.equals(HClass.Boolean))
	    return HClass.Int;
	return c;
    }

    /*-------------------------------------------------------------*/
    // Lattice classes.

    /** No information obtainable about a temp. */
    static abstract class LatticeVal {
	public String toString() { return "Top"; }
	public boolean equals(Object o) { return o instanceof LatticeVal; }
	// merge.
	public abstract LatticeVal merge(LatticeVal v);
	// narrow.
	public abstract boolean isLowerThan(LatticeVal v);
	// by default, the renaming does nothing.
	public LatticeVal rename(PHI p, int i) { return this; }
	public LatticeVal rename(SIGMA s, int i) { return this; }
    }
    /** A typed temp. */
    static class xClass extends LatticeVal {
	protected HClass type;
	public xClass(HClass type) {
	    Util.assert(type!=HClass.Boolean && type!=HClass.Byte &&
			type!=HClass.Short && type!=HClass.Char,
			"Not an internal type ("+type+")");
	    this.type = type;
	}
	public HClass type() { return type; }
	public String toString() { 
	    return "xClass: " + type;
	}
	public boolean equals(Object o) { return _equals(o); }
	private boolean _equals(Object o) {
	    xClass xc;
	    try { xc=(xClass) o; }
	    catch (ClassCastException e) { return false;}
	    return xc!=null && xc.type.equals(type);
	}
	public LatticeVal merge(LatticeVal v) {
	    xClass vv = (xClass) v;
	    return new xClass(mergeTypes(this.type, vv.type));
	}
	public boolean isLowerThan(LatticeVal v) {
	    return !(v instanceof xClass);
	}
	// Class merge function.
	static HClass mergeTypes(HClass a, HClass b) {
	    Util.assert(a!=null && b!=null);
	    if (a==b) return a; // take care of primitive types.
	    
	    // Special case 'Void' Hclass, used for null constants.
	    if (a==HClass.Void)
		return b;
	    if (b==HClass.Void)
		return a;
	    
	    // by this point better be array ref or object, not primitive type.
	    Util.assert((!a.isPrimitive()) && (!b.isPrimitive()));
	    return HClassUtil.commonParent(a,b);
	}
    }
    /** A single class type; guaranteed the value is not null. */
    static class xClassNonNull extends xClass {
	public xClassNonNull(HClass type) { 
	    super( type );
	    Util.assert(type!=HClass.Void);
	}
	public String toString() { 
	    return "xClassNonNull: { " + type + " }";
	}
	public boolean equals(Object o) { return _equals(o); }
	private boolean _equals(Object o) {
	    return (o instanceof xClassNonNull && super.equals(o));
	}
	public LatticeVal merge(LatticeVal v) {
	    if (!(v instanceof xClassNonNull)) return super.merge(v);
	    xClassNonNull vv = (xClassNonNull) v;
	    return new xClassNonNull(mergeTypes(this.type, vv.type));
	}
	public boolean isLowerThan(LatticeVal v) {
	    return !(v instanceof xClassNonNull);
	}
    }
    /** An object of the specified *exact* type (not a subtype). */
    static class xClassExact extends xClassNonNull {
	public xClassExact(HClass type) {
	    super(type);
	}
	public String toString() { 
	    return "xClassExact: { " + type + " }";
	}
	public boolean equals(Object o) { return _equals(o); }
	private boolean _equals(Object o) {
	    return (o instanceof xClassExact && super.equals(o));
	}
	public LatticeVal merge(LatticeVal v) {
	    if (this._equals(v)) return new xClassExact(type);
	    return super.merge(v);
	}
	public boolean isLowerThan(LatticeVal v) {
	    return !(v instanceof xClassExact);
	}
    }
    /** An array with constant length.  The array is not null, of course. */
    static class xClassArray extends xClassExact {
	protected int length;
	public xClassArray(HClass type, int length) {
	    super(type);
	    this.length = length;
	}
	public int length() { return length; }
	public String toString() {
	    return "xClassArray: " + 
		type.getComponentType() + "["+length+"]";
	}
	public boolean equals(Object o) { return _equals(o); }
	private boolean _equals(Object o) {
	    xClassArray xca;
	    try { xca = (xClassArray) o; }
	    catch (ClassCastException e) { return false; }
	    return xca!=null && super.equals(xca) && xca.length == length;
	}
	public LatticeVal merge(LatticeVal v) {
	    if (this._equals(v)) return new xClassArray(type,length);
	    return super.merge(v);
	}
	public boolean isLowerThan(LatticeVal v) {
	    return !(v instanceof xClassArray);
	}
    }
    /** An integer value of the specified bitwidth. */
    static class xBitWidth extends xClassExact {
	/** Highest significant bit for positive numbers. */
	protected int plusWidth;
	/** Highest significant bit for negative numbers. */
	protected int minusWidth;
	/** Constructor. */
	public xBitWidth(HClass type, int minusWidth, int plusWidth) {
	    super(toInternal(type));
	    // limit.
	    if (type == HClass.Long) {
		this.minusWidth = Math.min(64, minusWidth);
		this.plusWidth  = Math.min(63, plusWidth);
	    } else if (type == HClass.Int) {
		this.minusWidth = Math.min(32, minusWidth);
		this.plusWidth  = Math.min(31, plusWidth);
	    } else // NON-CANONICAL TYPES: CAREFUL! (this.type fixed by above)
		if (type == HClass.Boolean) {
		this.minusWidth = Math.min( 0, minusWidth);
		this.plusWidth  = Math.min( 1, plusWidth);
	    } else if (type == HClass.Short) {
		this.minusWidth = Math.min(16, minusWidth);
		this.plusWidth  = Math.min(15, plusWidth);
	    } else if (type == HClass.Byte) {
		this.minusWidth = Math.min( 8, minusWidth);
		this.plusWidth  = Math.min( 7, plusWidth);
	    } else if (type == HClass.Char) {
		this.minusWidth = Math.min( 0, minusWidth);
		this.plusWidth  = Math.min(16, plusWidth);
	    } else throw new Error("Unknown type for xBitWidth: "+type);
	}
	public int minusWidth() { return minusWidth; }
	public int plusWidth () { return plusWidth;  }
	public String toString() {
	    return "xBitWidth: " + type + " " +
		"-"+minusWidth+"+"+plusWidth+" bits";
	}
	public boolean equals(Object o) { return _equals(o); }
	private boolean _equals(Object o) {
	    xBitWidth xbw;
	    try { xbw = (xBitWidth) o; }
	    catch (ClassCastException e) { return false; }
	    return xbw!=null && super.equals(xbw) &&
		xbw.minusWidth == minusWidth &&
		xbw.plusWidth  == plusWidth;
	}
	public LatticeVal merge(LatticeVal v) {
	    if (!(v instanceof xBitWidth)) return super.merge(v);
	    // bitwidth merge
	    xBitWidth vv = (xBitWidth) v;
	    if (!this.type.equals(vv.type)) return super.merge(vv);
	    return new xBitWidth
		(this.type, 
		 Math.max(this.minusWidth, vv.minusWidth),
		 Math.max(this.plusWidth, vv.plusWidth));
	}
	public boolean isLowerThan(LatticeVal v) {
	    return !(v instanceof xBitWidth);
	}
    }
    /** An integer value which is the result of an INSTANCEOF. */
    static class xInstanceofResultUnknown extends xBitWidth
	implements xInstanceofResult {
	Temp tested;
	INSTANCEOF q;
	public xInstanceofResultUnknown(INSTANCEOF q) { this(q, q.src()); }
	xInstanceofResultUnknown(INSTANCEOF q, Temp tested) {
	    super(toInternal(HClass.Boolean),0,1);
	    this.q = q;
	    this.tested = tested;
	}
	public Temp tested() { return tested; }
	public INSTANCEOF def() { return q; }
	public String toString() {
	    return "xInstanceofResultUnknown: " + type + " " +q;
	}
	public boolean equals(Object o) { return _equals(o); }
	private boolean _equals(Object o) {
	    return (o instanceof xInstanceofResultUnknown && super.equals(o) &&
		    ((xInstanceofResultUnknown)o).q == q &&
		    ((xInstanceofResultUnknown)o).tested == tested);
	}
	public LatticeVal merge(LatticeVal v) {
	    if (v instanceof xInstanceofResult)
		// xInstanceofResultKnown merged with
		// xInstanceofResultKnown or xInstanceofResultUnknown
		return makeUnknown();
	    // all others.
	    return super.merge(v);
	}
	public boolean isLowerThan(LatticeVal v) {
	    return !(v instanceof xBitWidth);
	}
	public xInstanceofResultKnown makeKnown(boolean nvalue) {
	    return new xInstanceofResultKnown(q,tested,nvalue?1:0);
	}
	public xInstanceofResultUnknown makeUnknown() {
	    return new xInstanceofResultUnknown(q,tested);
	}
	// override renaming functions.
	public LatticeVal rename(PHI q, int j) {
	    for (int i=0; i<q.numPhis(); i++)
		if (q.src(i, j)==this.tested)
		    return new xInstanceofResultUnknown(def(), q.dst(i));
	    return this;
	}
	public LatticeVal rename(SIGMA q, int j) {
	    for (int i=0; i<q.numSigmas(); i++)
		if (q.src(i)==this.tested)
		    return new xInstanceofResultUnknown(def(), q.dst(i, j));
	    return this;
	}
    }
    /** An unknown boolean value which is the result of an OPER. */
    static class xOperBooleanResultUnknown extends xBitWidth
	implements xOperBooleanResult {
	OPER q;
	Temp[] operands;
	public xOperBooleanResultUnknown(OPER q) { this(q, q.operands()); }
	xOperBooleanResultUnknown(OPER q, Temp[] operands) {
	    super(toInternal(HClass.Boolean),0,1);
	    this.q = q;
	    this.operands = operands;
	}
	public Temp[] operands() { return operands; }
	public OPER def() { return q; }
	public String toString() {
	    return "xOperBooleanResultUnknown: " + type + " " +q;
	}
	public boolean equals(Object o) { return _equals(o); }
	private boolean _equals(Object o) {
	    if (o==this) return true; // common case.
	    if (!(o instanceof xOperBooleanResultUnknown)) return false;
	    if (!super.equals(o)) return false;
	    xOperBooleanResultUnknown oo = (xOperBooleanResultUnknown)o;
	    if (oo.q != q) return false;
	    if (oo.operands.length != operands.length) return false;
	    for (int i=0; i<operands.length; i++)
		if (oo.operands[i] != operands[i]) return false;
	    return true;
	}
	public LatticeVal merge(LatticeVal v) {
	    if (v instanceof xOperBooleanResult)
		// xOperBooleanResultKnown merged with
		// xOperBooleanResultKnown or xOperBooleanResultUnknown
		return makeUnknown();
	    // all others.
	    return super.merge(v);
	}
	public boolean isLowerThan(LatticeVal v) {
	    return !(v instanceof xBitWidth);
	}
	public xOperBooleanResultKnown makeKnown(boolean nvalue) {
	    return new xOperBooleanResultKnown(q,operands,nvalue?1:0);
	}
	public xOperBooleanResultUnknown makeUnknown() {
	    return new xOperBooleanResultUnknown(q,operands);
	}
	// override renaming functions.
	public LatticeVal rename(PHI q, int j) {
	    MyTempMap mtm = new MyTempMap();
	    for (int i=0; i<q.numPhis(); i++)
		mtm.put(q.src(i,j), q.dst(i));
	    return new xOperBooleanResultUnknown(def(), mtm.tempMap(operands()));
	}
	public LatticeVal rename(SIGMA q, int j) {
	    MyTempMap mtm = new MyTempMap();
	    for (int i=0; i<q.numSigmas(); i++)
		mtm.put(q.src(i), q.dst(i, j));
	    return new xOperBooleanResultUnknown(def(), mtm.tempMap(operands()));
	}
	private static class MyTempMap extends HashMap implements TempMap {
	    public Temp tempMap(Temp t) {
		return containsKey(t) ? (Temp) get(t) : t;
	    }
	    public Temp[] tempMap(Temp[] t) {
		Temp[] r = new Temp[t.length];
		for (int i=0; i<r.length; i++)
		    r[i] = tempMap(t[i]);
		return r;
	    }
	}
    }
    /** An integer or boolean constant. */
    static class xIntConstant extends xBitWidth implements xConstant {
	protected long value;
	public xIntConstant(HClass type, long value) {
	    super(type, value<0?Util.fls(-value):0, value>0?Util.fls(value):0);
	    this.value = value;
	}
	public long value() { return value; }
	public Object constValue() { 
	    if (type==HClass.Int) return new Integer((int)value);
	    if (type==HClass.Long) return new Long((long)value);
	    //if (type==HClass.Boolean) return new Integer(value!=0?1:0);
	    throw new Error("Unknown integer constant type.");
	}
	public String toString() {
	    return "xIntConstant: " + type + " " + value;
	}
	public boolean equals(Object o) { return _equals(o); }
	private boolean _equals(Object o) {
	    return (o instanceof xIntConstant && super.equals(o) &&
		    ((xIntConstant)o).value == value);
	}
	public LatticeVal merge(LatticeVal v) {
	    if (this._equals(v)) return new xIntConstant(type,value);
	    return super.merge(v);
	}
	public boolean isLowerThan(LatticeVal v) {
	    return !(v instanceof xIntConstant);
	}
    }
    /** An integer value which is the result of an INSTANCEOF. */
    static class xInstanceofResultKnown extends xIntConstant
	implements xInstanceofResult {
	Temp tested;
	INSTANCEOF q;
	public xInstanceofResultKnown(INSTANCEOF q, boolean value) {
	    this(q, q.src(), value?1:0);
	}
	private xInstanceofResultKnown(INSTANCEOF q, Temp tested, long value) {
	    super(toInternal(HClass.Boolean),value);
	    this.q = q;
	    this.tested = tested;
	    Util.assert(value==0 || value==1);
	}
	public Temp tested() { return tested; }
	public INSTANCEOF def() { return q; }
	public String toString() {
	    return "xInstanceofResultKnown: " + value + " " +q;
	}
	public boolean equals(Object o) { return _equals(o); }
	private boolean _equals(Object o) {
	    return (o instanceof xInstanceofResultKnown && super.equals(o) &&
		    ((xInstanceofResultKnown)o).q == q &&
		    ((xInstanceofResultKnown)o).tested == tested);
	}
	public LatticeVal merge(LatticeVal v) {
	    if (v instanceof xInstanceofResult)
		// xInstanceofResultKnown merged with
		// xInstanceofResultKnown or xInstanceofResultUnknown
		return this._equals(v) ? (LatticeVal)
		    makeKnown(value!=0) : makeUnknown();
	    // all others.
	    return super.merge(v);
	}
	public boolean isLowerThan(LatticeVal v) {
	    return !(v instanceof xIntConstant);
	}
	public xInstanceofResultKnown makeKnown(boolean nvalue) {
	    return new xInstanceofResultKnown(q,tested,nvalue?1:0);
	}
	public xInstanceofResultUnknown makeUnknown() {
	    return new xInstanceofResultUnknown(q,tested);
	}
	// override renaming functions.
	public LatticeVal rename(PHI q, int j) {
	    for (int i=0; i<q.numPhis(); i++)
		if (q.src(i, j)==this.tested)
		    return new xInstanceofResultKnown(def(), q.dst(i), value);
	    return this;
	}
	public LatticeVal rename(SIGMA q, int j) {
	    for (int i=0; i<q.numSigmas(); i++)
		if (q.src(i)==this.tested)
		    return new xInstanceofResultKnown(def(),q.dst(i, j),value);
	    return this;
	}
    }
    /** A known boolean value which is the result of an OPER. */
    static class xOperBooleanResultKnown extends xIntConstant
	implements xOperBooleanResult {
	OPER q;
	Temp[] operands;
	public xOperBooleanResultKnown(OPER q, boolean value) {
	    this(q, q.operands(), value?1:0);
	}
	xOperBooleanResultKnown(OPER q, Temp[] operands, long value)
	{
	    super(toInternal(HClass.Boolean),value);
	    this.q = q;
	    this.operands = operands;
	    Util.assert(value==0 || value==1);
	}
	public Temp[] operands() { return operands; }
	public OPER def() { return q; }
	public String toString() {
	    return "xOperBooleanResultKnown: " + value + " " +q;
	}
	public boolean equals(Object o) { return _equals(o); }
	private boolean _equals(Object o) {
	    if (o==this) return true; // common case.
	    if (!(o instanceof xOperBooleanResultKnown)) return false;
	    if (!super.equals(o)) return false;
	    xOperBooleanResultKnown oo = (xOperBooleanResultKnown)o;
	    if (oo.q != q) return false;
	    if (oo.operands.length != operands.length) return false;
	    for (int i=0; i<operands.length; i++)
		if (oo.operands[i] != operands[i]) return false;
	    return true;
	}
	public LatticeVal merge(LatticeVal v) {
	    if (v instanceof xOperBooleanResult)
		// xOperBooleanResultKnown merged with
		// xOperBooleanResultKnown or xOperBooleanResultUnknown
		return this._equals(v) ? (LatticeVal)
		    makeKnown(value!=0) : makeUnknown();
	    // all others.
	    return super.merge(v);
	}
	public boolean isLowerThan(LatticeVal v) {
	    return !(v instanceof xIntConstant);
	}
	public xOperBooleanResultKnown makeKnown(boolean nvalue) {
	    return new xOperBooleanResultKnown(q,operands,nvalue?1:0);
	}
	public xOperBooleanResultUnknown makeUnknown() {
	    return new xOperBooleanResultUnknown(q,operands);
	}
	// override renaming functions.
	public LatticeVal rename(PHI q, int j) {
	    MyTempMap mtm = new MyTempMap();
	    for (int i=0; i<q.numPhis(); i++)
		mtm.put(q.src(i,j), q.dst(i));
	    return new xOperBooleanResultKnown(def(), mtm.tempMap(operands()),
					       value);
	}
	public LatticeVal rename(SIGMA q, int j) {
	    MyTempMap mtm = new MyTempMap();
	    for (int i=0; i<q.numSigmas(); i++)
		mtm.put(q.src(i), q.dst(i, j));
	    return new xOperBooleanResultKnown(def(), mtm.tempMap(operands()),
					       value);
	}
	private static class MyTempMap extends HashMap implements TempMap {
	    public Temp tempMap(Temp t) {
		return containsKey(t) ? (Temp) get(t) : t;
	    }
	    public Temp[] tempMap(Temp[] t) {
		Temp[] r = new Temp[t.length];
		for (int i=0; i<r.length; i++)
		    r[i] = tempMap(t[i]);
		return r;
	    }
	}
    }
    static class xNullConstant extends xClass implements xConstant {
	public xNullConstant() {
	    super(HClass.Void);
	}
	public Object constValue() { return null; }
	public String toString() {
	    return "xNullConstant: null";
	}
	public boolean equals(Object o) { return _equals(o); }
	private boolean _equals(Object o) {
	    return (o instanceof xNullConstant);
	}
	public LatticeVal merge(LatticeVal v) {
	    if (this._equals(v)) return new xNullConstant();
	    return super.merge(v);
	}
	public boolean isLowerThan(LatticeVal v) {
	    return !(v instanceof xNullConstant);
	}
    }
    static class xFloatConstant extends xClassExact
	implements xConstant {
	protected Object value;
	public xFloatConstant(HClass type, Object value) {
	    super(type); this.value = value;
	}
	public Object constValue() { return value; }
	public String toString() {
	    return "xFloatConstant: " + type + " " + value.toString();
	}
	public boolean equals(Object o) { return _equals(o); }
	private boolean _equals(Object o) {
	    return (o instanceof xFloatConstant && super.equals(o) &&
		    ((xFloatConstant)o).value.equals(value));
	}
	public LatticeVal merge(LatticeVal v) {
	    if (this._equals(v)) return new xFloatConstant(type, value);
	    return super.merge(v);
	}
	public boolean isLowerThan(LatticeVal v) {
	    return !(v instanceof xFloatConstant);
	}
    }
    static class xStringConstant extends xClassExact
	implements xConstant {
	protected Object value;
	public xStringConstant(HClass type, Object value) {
	    super(type);
	    // note that the string constant objects are intern()ed.
	    // doing this here ensures that evaluating ACMPEQ with constant
	    // args works correctly.
	    this.value = ((String)value).intern();
	}
	public Object constValue() { return value; }
	public String toString() {
	    return "xStringConstant: " + 
		"\"" + Util.escape(value.toString()) + "\"";
	}
	public boolean equals(Object o) { return _equals(o); }
	private boolean _equals(Object o) {
	    return (o instanceof xStringConstant && super.equals(o) &&
		    ((xStringConstant)o).value.equals(value));
	}
	public LatticeVal merge(LatticeVal v) {
	    if (this._equals(v)) return new xStringConstant(type, value);
	    return super.merge(v);
	}
	public boolean isLowerThan(LatticeVal v) {
	    return !(v instanceof xStringConstant);
	}
    }
    static interface xConstant {
	public Object constValue();
    }
    static interface xInstanceofResult {
	public Temp tested();
	public INSTANCEOF def();
	public xInstanceofResultKnown makeKnown(boolean value);
	public xInstanceofResultUnknown makeUnknown();
    }
    static interface xOperBooleanResult {
	public Temp[] operands();
	public OPER def();
	public xOperBooleanResultKnown makeKnown(boolean value);
	public xOperBooleanResultUnknown makeUnknown();
    }
    /////////////////////////////////////////////////////////
    // ways to degrade the analysis to collect statistics.
    private final Corruptor corruptor = null; // no corruption.
    private final boolean useSigmas = true;
    /** A <code>Corruptor</code> lets you 'dumb-down' the analysis 
     *  incrementally, so that we can generate numbers showing that
     *  every step makes it better and better. */
    static abstract class Corruptor {
	/** make this lattice value worse than we know it to be. */
	abstract LatticeVal corrupt(LatticeVal v);
    }
    static final Corruptor nobitwidth = new Corruptor() {
	public LatticeVal corrupt(LatticeVal v) {
	    if (v!=null && v.toString().startsWith("xBitWidth:"))
	      return new xClassExact(((xClassExact)v).type);
	    return v;
	}
    };
    static final Corruptor nofixedarray = new Corruptor() {
	public LatticeVal corrupt(LatticeVal v) {
	    v = nobitwidth.corrupt(v);
	    if (v instanceof xClassArray)
	      return new xClassNonNull(((xClassNonNull)v).type);
	    return v;
	}
    };
    static final Corruptor nonullpointer = new Corruptor() {
	public LatticeVal corrupt(LatticeVal v) {
	    if (v instanceof xClassNonNull)
	      return new xClass(((xClassNonNull)v).type);
	    return v;
	}
    };
    static final Corruptor nononint = new Corruptor() {
	public LatticeVal corrupt(LatticeVal v) {
	    v = nonullpointer.corrupt(v);
	    if (v instanceof xFloatConstant ||
		v instanceof xStringConstant ||
		(v instanceof xIntConstant && ((xClass)v).type!=HClass.Int))
	      return new xClass(((xClass)v).type);
	    return v;
	}
    };
}
