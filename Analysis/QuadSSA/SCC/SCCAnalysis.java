// SCC.java, created Fri Sep 18 17:45:07 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.QuadSSA.SCC;

import harpoon.Analysis.Maps.ConstMap;
import harpoon.Analysis.Maps.ExecMap;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.Maps.UseDefMap;
import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import harpoon.Temp.Temp;
import harpoon.Util.HClassUtil;
import harpoon.Util.Set;
import harpoon.Util.HashSet;
import harpoon.Util.Util;
import harpoon.Util.Worklist;

import java.util.Hashtable;
import java.util.Enumeration;
/**
 * <code>SCCAnalysis</code> implements Sparse Conditional Constant Propagation,
 * with extensions to allow type and bitwidth analysis.  Fun, fun, fun.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SCCAnalysis.java,v 1.15.2.10 1999-02-05 08:26:19 cananian Exp $
 */

public class SCCAnalysis implements TypeMap, ConstMap, ExecMap {
    UseDefMap udm;

    /** Creates a <code>SCC</code>. */
    public SCCAnalysis(UseDefMap usedef) {
	this.udm = usedef;
    }

    /*-----------------------------*/
    // Class state.
    /** Set of all executable edges. */
    Set Ee = new HashSet();
    /** Set of all executable quads. */
    Set Eq = new HashSet();
    /** Mapping from <code>Temp</code>s to lattice values. */
    Hashtable V = new Hashtable();

    /*---------------------------*/
    // public information accessor methods.

    /** Determine whether <code>Quad</code> <code>q</code> in
     *  <code>HMethod</code> <code>m</code>
     *  is executable. */
    public boolean execMap(HCode hc, HCodeElement quad) {
	analyze(hc); return Eq.contains(quad);
    }
    /** Determine whether <code>Edge</code> <code>e</code> in 
     *  <code>HMethod</code> <code>m</code>
     *  is executable. */
    public boolean execMap(HCode hc, HCodeEdge edge) {
	analyze(hc); return Ee.contains(edge);
    }
    /** Determine the static type of <code>Temp</code> <code>t</code> in 
     *  <code>HMethod</code> <code>m</code>. */
    public HClass typeMap(HCode hc, Temp t) {
	analyze(hc);  LatticeVal v = (LatticeVal) V.get(t);
	if (v instanceof xClass) return ((xClass)v).type();
	return null;
    }
    /** Determine whether <code>Temp</code> <code>t</code> in 
     *  <code>HMethod</code> <code>m</code>
     *  has a constant value. */
    public boolean isConst(HCode hc, Temp t) {
	analyze(hc); return (V.get(t) instanceof xConstant);
    }
    /** Determine the constant value of <code>Temp</code> <code>t</code> in 
     *  <code>HMethod</code> <code>m</code>. 
     *  @exception Error if <code>Temp</code> <code>t</code> is not a constant.
     */
    public Object constMap(HCode hc, Temp t) {
	analyze(hc);  LatticeVal v = (LatticeVal) V.get(t);
	if (v instanceof xConstant) return ((xConstant)v).constValue();
	throw new Error(t.toString() + " not a constant");
    }

    /** Determine the positive bit width of <code>Temp</code> <code>t</code>
     *  in <code>HMethod</code> <code>m</code>.
     */
    public int plusWidthMap(HCode hc, Temp t) {
	analyze(hc); LatticeVal v = (LatticeVal) V.get(t);
	if (v==null) throw new Error("Unknown "+t);
	xBitWidth bw = extractWidth(v);
	return bw.plusWidth();
    }
    /** Determine the negative bit width of <code>Temp</code> <code>t</code>
     *  in <code>HMethod</code> <code>m</code>.
     */
    public int minusWidthMap(HCode hc, Temp t) {
	analyze(hc); LatticeVal v = (LatticeVal) V.get(t);
	if (v==null) throw new Error("Unknown "+t);
	xBitWidth bw = extractWidth(v);
	return bw.minusWidth();
    }

    /*---------------------------*/
    // Analysis code.

    /** Set of analyzed methods. */
    Set analyzed = new HashSet();
    HCode lastHCode = null;
    /** Main analysis method. */
    void analyze(HCode hc) {
	// caching.
	if (lastHCode==hc) return; // quick exit.
	if (analyzed.contains(hc)) return;
	analyzed.union(hc);
	lastHCode = hc;

	// Initialize worklists.
	Worklist Wv = new HashSet(); // variable worklist.
	Worklist Wq = new HashSet(); // block worklist.

	// Make instance of visitor class.
	SCCVisitor visitor = new SCCVisitor(hc, Wv, Wq);

	// put the root entry on the worklist and mark it executable.
	HCodeElement root = hc.getRootElement();
	if (! (root instanceof Quad) ) 
	    throw new Error("SCC analysis works only on QuadSSA form.");
	Wq.push(root);
	Eq.union(root);

	// Iterate.
	while (! (Wq.isEmpty() && Wv.isEmpty()) ) { // until both are empty.

	    if (!Wq.isEmpty()) { // grab statement from We if we can.
		Quad q = (Quad) Wq.pull();
		// Rule 2: for any executable block with
		// only one successor C, set edge leading to C executable.
		if (q.next().length==1) {
		    raiseE(Ee, Eq, Wq, q.nextEdge(0));
		}
		// check conditions 3-8 for q.
		q.visit(visitor);
	    } 

	    if (!Wv.isEmpty()) { // grab temp from Wv is possible.
		Temp t = (Temp) Wv.pull();
		// for every use of t...
		for (Enumeration e=udm.useMapE(hc, t); e.hasMoreElements(); )
		    // check conditions 3-8
		    ((Quad) e.nextElement()).visit(visitor);
	    }
	} // end while loop.
    } // end analysis.

    /*----------------------------------------*/
    // raising values in the lattice:

    /** Raise edge e in Ee/Eq, adding target q to Wq if necessary. */
    void raiseE(Set Ee, Set Eq, Worklist Wq, Edge e) {
	Quad q = (Quad) e.to();
	Ee.union(e);
	if (Eq.contains(q)) return;
	Eq.union(q);
	Wq.push(q);
    }
    /** Raise element t to a in V, adding t to Wv if necessary. */
    void raiseV(Hashtable V, Worklist Wv, Temp t, LatticeVal a) {
	LatticeVal old = (LatticeVal) V.get(t);
	// only allow raising value in lattice.
	if (old != null && old.equals(a)) return;
	if (old != null && !a.higherThan(old)) return;
	V.put(t, a);
	Wv.push(t);
    }
    /*------------------------------------------------------------*/
    // VISITOR CLASS (the real guts of the routine)
    class SCCVisitor extends QuadVisitor {
	// local reference to HCode.
	HCode hc;
	// local references to worklists.
	Worklist Wv, Wq;
	// give us an OperVisitor class to go along with this.
	OperVisitor opVisitor = new SCCOpVisitor();

	SCCVisitor(HCode hc, Worklist Wv, Worklist Wq) {
	    this.hc = hc;  this.Wv = Wv;  this.Wq = Wq;
	}

	// utility functions.
	LatticeVal get(Temp t) { return (LatticeVal) V.get(t); }

	void handleSigmas(CJMP q, INSTANCEOF def) {
	    // for every sigma source:
	    for (int i=0; i < q.numSigmas(); i++) {
		// check if this is the CJMP condition.
		if (q.test() == q.src(i)) { // known value after branch
		    raiseV(V, Wv, q.dst(i,0), 
			   new xIntConstant(HClass.Boolean, 0));
		    raiseV(V, Wv, q.dst(i,1),
			   new xIntConstant(HClass.Boolean, 1));
		    continue; // go on.
		}

		LatticeVal v = get( q.src(i) );
		if (v == null) continue; // skip: insufficient info.

		// check to see if this is the temp tested by INSTANCEOF
		if (q.src(i) == def.src()) {
		    // no new info on false branch.
		    raiseV(V, Wv, q.dst(i,0), v);
		    // we know q.dst[i][1] is INSTANCEOF def.hclass
		    // secret inside info: INSTANCEOF src is always non-null.
		    raiseV(V, Wv, q.dst(i,1), 
			   new xClassNonNull(def.hclass()));
		} else {
		    // fall back.
		    raiseV(V, Wv, q.dst(i,0), v);
		    raiseV(V, Wv, q.dst(i,1), v);
		}
	    }
	}
	
	void handleSigmas(CJMP q, OPER def) {
	    int opc = def.opcode();
	    LatticeVal left = def.operandsLength()<1?null:get(def.operands(0));
	    LatticeVal right= def.operandsLength()<2?null:get(def.operands(1));

	    // for every sigma source:
	    for (int i=0; i < q.numSigmas(); i++) {
		// check if this is the CJMP condition.
		if (q.test() == q.src(i)) {
		    raiseV(V, Wv, q.dst(i,0), 
			   new xIntConstant(HClass.Boolean, 0));
		    raiseV(V, Wv, q.dst(i,1),
			   new xIntConstant(HClass.Boolean, 1));
		    continue; // go on.
		}

		LatticeVal v = get( q.src(i) );
		if (v == null) continue; // skip: insufficient info.

		// check to see if it comes from the OPER defining the boolean.
		boolean handled = false;
		if (q.src(i) == def.operands(0)) { // left is source.
		    if (opc == Qop.ACMPEQ &&
			left  instanceof xClass && // not already xClassNonNull
			right instanceof xNullConstant) {
			raiseV(V, Wv, q.dst(i,0), // false branch: non-null
			       new xClassNonNull( ((xClass)left).type() ) );
			raiseV(V, Wv, q.dst(i,1), // true branch: null
			       new xNullConstant() );
			handled = true;
		    } else if ((opc == Qop.ICMPEQ || opc == Qop.LCMPEQ ||
				opc == Qop.FCMPEQ || opc == Qop.DCMPEQ) &&
			       right instanceof xConstant) {
			raiseV(V, Wv, q.dst(i,0), // false branch: no info
			       v);
			raiseV(V, Wv, q.dst(i,1), // true branch: constant!
			       right);
			handled = true;
		    } else if ((/*opc == Qop.ICMPGE || opc == Qop.LCMPGE ||*/
				opc == Qop.ICMPGT || opc == Qop.LCMPGT ) &&
			       right instanceof xBitWidth) {
			// XXX we can tighten bounds on gt, as opposed to ge.
			xBitWidth bw = (xBitWidth) right;
			xBitWidth sr = extractWidth(v);
			// false branch:
			raiseV(V, Wv, q.dst(i,0), new xBitWidth(sr.type(),
			       Math.max(sr.minusWidth(),bw.minusWidth()),
			       Math.min(sr.plusWidth(), bw.plusWidth()) ));
			// true branch.
			raiseV(V, Wv, q.dst(i,1), new xBitWidth(sr.type(),
			       Math.min(sr.minusWidth(),bw.minusWidth()),
                               Math.max(sr.plusWidth(), bw.plusWidth()) ));
			handled = true;
		    }
		} else if (q.src(i) == def.operands(1)) { // right is source.
		    if (opc == Qop.ACMPEQ &&
			right instanceof xClass && // not already xClassNonNull
			left  instanceof xNullConstant) {
			raiseV(V, Wv, q.dst(i,0), // false branch: non-null
			       new xClassNonNull( ((xClass)right).type() ) );
			raiseV(V, Wv, q.dst(i,1), // true branch: null
			       new xNullConstant() );
			handled = true;
		    } else if ((opc == Qop.ICMPEQ || opc == Qop.LCMPEQ ||
				opc == Qop.FCMPEQ || opc == Qop.DCMPEQ) &&
			       left instanceof xConstant) {
			raiseV(V, Wv, q.dst(i,0), // false branch: no info
			       v);
			raiseV(V, Wv, q.dst(i,1), // true branch: constant!
			       left);
			handled = true;
		    } else if ((/*opc == Qop.ICMPGE || opc == Qop.LCMPGE ||*/
				opc == Qop.ICMPGT || opc == Qop.LCMPGT ) &&
			       left instanceof xBitWidth) {
			// XXX we can tighten bounds on gt, as opposed to ge.
			xBitWidth bw = (xBitWidth) left;
			xBitWidth sr = extractWidth(v);
			// false branch:
			raiseV(V, Wv, q.dst(i,0), new xBitWidth(sr.type(),
			       Math.min(sr.minusWidth(),bw.minusWidth()),
			       Math.max(sr.plusWidth(), bw.plusWidth()) ));
			// true branch.
			raiseV(V, Wv, q.dst(i,1), new xBitWidth(sr.type(),
			       Math.max(sr.minusWidth(),bw.minusWidth()),
                               Math.min(sr.plusWidth(), bw.plusWidth()) ));
			handled = true;
		    }
		}
		// fall back.
		if (!handled) {
		    raiseV(V, Wv, q.dst(i,0), v);
		    raiseV(V, Wv, q.dst(i,1), v);
		}
	    }
	}

	// visitation.
	public void visit(Quad q) { /* do nothing. */ }
	public void visit(AGET q) {
	    LatticeVal v = get( q.objectref() );
	    if (v instanceof xClass)
		raiseV(V, Wv, q.dst(), 
		       new xClass( ((xClass)v).type().getComponentType() ) );
	}
	public void visit(ALENGTH q) {
	    LatticeVal v = get( q.objectref() );
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
	    raiseV(V, Wv, q.dst(), new xClassNonNull(q.hclass()) );
	}
	public void visit(ASET q) { /* do nothing. */ }
	public void visit(CALL q) {
	    if (q.retval() != null) {
		// in the bytecode world, everything's an int.
		HClass ty = q.method().getReturnType();
		LatticeVal v = new xClass(ty);
		if (ty==HClass.Byte)
		    v = new xBitWidth(HClass.Int,  8,  7);
		else if (ty==HClass.Short)
		    v = new xBitWidth(HClass.Int, 16, 15);
		else if (ty==HClass.Char)
		    v = new xBitWidth(HClass.Int,  0, 16);
		else if (ty.isPrimitive())
		    v = new xClassNonNull(ty);
		raiseV(V, Wv, q.retval(), v);
	    }
	    raiseV(V, Wv, q.retex(), 
		   new xClass( HClass.forClass(Throwable.class) ) );
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
		for (int i=0; i < q.numSigmas(); i++) {
		    LatticeVal v2 = get( q.src(i) );
		    if (v2 != null)
			raiseV(V, Wv, q.dst(i,test?1:0), v2);
		}
		return; // done.
	    } else if (v instanceof xClass) { // ie, not bottom.
		// both edges are potentially executable.
		raiseE(Ee, Eq, Wq, q.nextEdge(1) );
		raiseE(Ee, Eq, Wq, q.nextEdge(0) );

		// look at definition of boolean condition.
		Quad def= (Quad)udm.defMap(hc, q.test())[0];// SSA form, right?
		if (def instanceof OPER) // only case we care about
		    handleSigmas((CJMP) q, (OPER) def);
		else if (def instanceof INSTANCEOF) // ok, i lied.
		    handleSigmas((CJMP) q, (INSTANCEOF) def);
		else // fallback.
		    for (int i=0; i < q.numSigmas(); i++) {
			// is this the CJMP condition?
			if (q.src(i) == q.test()) {
			    raiseV(V, Wv, q.dst(i,0), 
				   new xIntConstant(HClass.Boolean, 0));
			    raiseV(V, Wv, q.dst(i,1),
				   new xIntConstant(HClass.Boolean, 1));
			} else {
			    LatticeVal v2 = get ( q.src(i) );
			    if (v2 != null) {
				raiseV(V, Wv, q.dst(i,0), v2);
				raiseV(V, Wv, q.dst(i,1), v2);
			    }
			}
		    }
	    }
	}
	public void visit(COMPONENTOF q) {
	    // we're guaranteed that q.arrayref is non-null here.
	    LatticeVal vA = get( q.arrayref() );
	    LatticeVal vO = get( q.objectref() );
	    if (vA instanceof xClass && vO instanceof xClass) {
		HClass hcA = ((xClass) vA).type().getComponentType() ;
		HClass hcO = ((xClass) vO).type();
		// special case when q.objectref is null
		if (hcO == HClass.Void) // always true.
		    raiseV(V, Wv, q.dst(), new xIntConstant(HClass.Boolean,1));
		else if (hcO.isInstanceOf(hcA)) // always true
		    raiseV(V, Wv, q.dst(), new xIntConstant(HClass.Boolean,1));
		else if (hcA.isInstanceOf(hcO)) // unknowable.
		    raiseV(V, Wv, q.dst(), new xBitWidth(HClass.Boolean,1,0));
		else // always false.
		    raiseV(V, Wv, q.dst(), new xIntConstant(HClass.Boolean,0));
	    }
	}
	public void visit(CONST q) {
	    if (q.type() == HClass.Void) // null constant
		raiseV(V,Wv, q.dst(), new xNullConstant() );
	    else if (q.type()==HClass.forClass(String.class))// string constant
		raiseV(V,Wv, q.dst(), new xStringConstant(q.type(),q.value()));
	    else if (q.type()==HClass.Float || q.type()==HClass.Double) // f-p
		raiseV(V,Wv, q.dst(), new xFloatConstant(q.type(),q.value()) );
	    else if (q.type()==HClass.Int || q.type() == HClass.Long)
		raiseV(V,Wv, q.dst(), 
		       new xIntConstant(q.type(),
					((Number)q.value()).longValue()));
	    else throw new Error("Unknown CONST type: "+q.type());
	}
	public void visit(FOOTER q) { /* do nothing. */ }
	public void visit(GET q) {
	    HClass type = q.field().getType();
	    if (q.field().isConstant()) {
		Object val = q.field().getConstant();
		if (type == HClass.forClass(String.class))
		    raiseV(V, Wv, q.dst(), new xStringConstant(type, val) );
		else if (type == HClass.Float || type == HClass.Double )
		    raiseV(V, Wv, q.dst(), new xFloatConstant(type, val) );
		else if (type == HClass.Int || type == HClass.Long)
		    raiseV(V, Wv, q.dst(), 
			   new xIntConstant(type,((Number)val).longValue() ) );
		else throw new Error("Unknown constant field type: "+type);
	    } else raiseV(V, Wv, q.dst(), new xClass( type ) );
	}
	public void visit(INSTANCEOF q) {
	    // no guarantee that src is not null.
	    LatticeVal v = get( q.src() );
	    if (v instanceof xNullConstant) // always true.
		raiseV(V, Wv, q.dst(), new xIntConstant(HClass.Boolean,1) );
	    else if (v instanceof xClassNonNull) { // analyzable
		HClass hcO = ((xClassNonNull)v).type();
		if (hcO.isInstanceOf(q.hclass())) // always true
		    raiseV(V,Wv, q.dst(), new xIntConstant(HClass.Boolean,1) );
		else if (q.hclass().isInstanceOf(hcO)) // unknowable.
		    raiseV(V,Wv, q.dst(), new xBitWidth(HClass.Boolean,1,0) );
		else // always false.
		    raiseV(V,Wv, q.dst(), new xIntConstant(HClass.Boolean,0) );
	    }
	    else if (v instanceof xClass) { // could be null.
		HClass hcO = ((xClass)v).type();
		if (q.hclass().isInstanceOf(hcO) || 
		    hcO.isInstanceOf(q.hclass()) ) // unknowable.
		    raiseV(V,Wv, q.dst(), new xBitWidth(HClass.Boolean,1,0) );
		else // always false (even if src==null)
		    raiseV(V,Wv, q.dst(), new xIntConstant(HClass.Boolean,0) );
	    }
	}
	public void visit(METHOD q) {
	    HMethod m = hc.getMethod();
	    HClass[] pt = m.getParameterTypes();
	    int j=0;
	    if (!m.isStatic() ) // raise 'this' variable (non-null!)
		raiseV(V, Wv, q.params(j++),
		       new xClassNonNull( m.getDeclaringClass() ) );
	    for (int k=0; k < pt.length; j++, k++)
		if (pt[k].isPrimitive())
		    raiseV(V, Wv, q.params(j), new xClassNonNull( pt[k] ) );
		else
		    raiseV(V, Wv, q.params(j), new xClass( pt[k] ) );
	}
	public void visit(MONITORENTER q) { /* do nothing. */ }
	public void visit(MONITOREXIT q) { /* do nothing. */ }
	public void visit(MOVE q) {
	    LatticeVal v = get ( q.src() );
	    if (v != null)
		raiseV(V, Wv, q.dst(), v);
	}
	public void visit(NEW q) {
	    raiseV(V, Wv, q.dst(), new xClassNonNull( q.hclass() ) );
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
			   new xIntConstant(ty, 
					    ((Boolean)o).booleanValue()?1:0));
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
		q.visit(opVisitor);
	    } else { // not all constant, not all known widths...
		// special-case ACMPEQ x, null
		if (opc == Qop.ACMPEQ &&
		    ((get( q.operands(0) ) instanceof xNullConstant &&
		      get( q.operands(1) ) instanceof xClassNonNull) ||
		     (get( q.operands(0) ) instanceof xClassNonNull &&
		      get( q.operands(1) ) instanceof xNullConstant) ) )
		    raiseV(V, Wv, q.dst(), // always false.
			   new xIntConstant(HClass.Boolean, 0));
		else {
		    // RULE 4:
		    HClass ty = q.evalType();
		    if (ty.isPrimitive())
			raiseV(V, Wv, q.dst(), new xClassNonNull( ty ) );
		    else
			raiseV(V, Wv, q.dst(), new xClass( ty ) );
		}
	    }
	}
	public void visit(PHI q) {
	    for (int i=0; i<q.numPhis(); i++) { // for each phi-function.
		boolean allConst = true;
		boolean allWidth = true;
		boolean allNonNull=true;
		boolean someValidValue=false;

		Object constValue = null;
		HClass mergedType = null;
		int mergedWidthPlus = 0;
		int mergedWidthMinus= 0;
		for (int j=0; j < q.arity(); j++) {
		    if (!Ee.contains( q.prevEdge(j) ))
			continue; // skip non-executable edges.
		    LatticeVal v = get ( q.src(i,j) );
		    if (v == null)
			continue; // skip this arg function.
		    else 
			someValidValue=true;
		    // constant merge.
		    if (v instanceof xConstant) {
			Object o = ((xConstant)v).constValue();
			// rule 5
			if (constValue==null) constValue = o;
			else if (!constValue.equals(o))
			    allConst = false;
		    } else  allConst = false;
		    // bitwidth merge.
		    if (v instanceof xBitWidth) {
			int plusWidth = ((xBitWidth)v).plusWidth();
			int minusWidth= ((xBitWidth)v).minusWidth();
			mergedWidthPlus =Math.max(mergedWidthPlus, plusWidth);
			mergedWidthMinus=Math.max(mergedWidthMinus,minusWidth);
		    } else allWidth = false;
		    // null status merge.
		    if (! (v instanceof xClassNonNull) )
			allNonNull = false;
		    // class/type merge.
		    if (v instanceof xClass) {
			HClass hc = ((xClass)v).type();
			// rule 6
			if (mergedType == null) mergedType = hc;
			else mergedType = merge(mergedType, hc);
		    } else throw new Error("non class merge.");
		}
		// assess results.
		if (!someValidValue)
		    continue; // nothing to go on.
		else if (allConst) {
		    LatticeVal v;
		    if (constValue == null)
			v = new xNullConstant();
		    else if (mergedType == HClass.forClass(String.class))
			v = new xStringConstant(mergedType, constValue);
		    else if (mergedType == HClass.Float || 
			     mergedType == HClass.Double)
			v = new xFloatConstant(mergedType, constValue);
		    else if (mergedType == HClass.Int ||
			     mergedType == HClass.Long ||
			     mergedType == HClass.Boolean)
			v = new xIntConstant(mergedType,
					     ((Number)constValue).longValue());
		    else throw new Error("Unknown constant type.");
		    raiseV(V, Wv, q.dst(i), v);
		} else if (allWidth) {
		    raiseV(V, Wv, q.dst(i), 
			   new xBitWidth(mergedType, 
					 mergedWidthMinus, mergedWidthPlus) );
		} else if (allNonNull) {
		    raiseV(V, Wv, q.dst(i), new xClassNonNull(mergedType) );
		} else {
		    raiseV(V, Wv, q.dst(i), new xClass(mergedType) );
		}
	    } // for each phi function.
	}
	public void visit(RETURN q) { /* do nothing. */ }
	public void visit(SET q) { /* do nothing. */ }
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
			raiseV(V, Wv, q.dst(j,i), v2);
		}
	    }
	    // XXX maybe stuff we can learn about v from bitwidth?
	    else if (v != null) {
		// mark all edges executable & propagate to all sigmas.
		for (int i=0; i < q.nextEdge().length; i++)
		    raiseE(Ee, Eq, Wq, q.nextEdge(i) );
		for (int i=0; i < q.numSigmas(); i++) {
		    LatticeVal v2 = get( q.src(i) );
		    if (v2 != null)
			for (int j=0; j < q.arity(); j++)
			    raiseV(V, Wv, q.dst(i,j), v2);
		}
	    }
	}
	public void visit(THROW q) { /* do nothing. */ }

	/*------------------------------------------------------------*/
	// VISITOR CLASS FOR OPER (ugh.  lots of cases)
	class SCCOpVisitor extends OperVisitor {

	    public void visit_default(OPER q) {
		HClass ty = q.evalType();
		if (ty.isPrimitive())
		    raiseV(V, Wv, q.dst(), new xClassNonNull( ty ) );
		else
		    raiseV(V, Wv, q.dst(), new xClass( ty ) );
	    }
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
				 left.plusWidth()  + left.minusWidth());
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
	    /*
    public void visit_ior(OPER q) { visit_default(q); }
    public void visit_irem(OPER q) { visit_default(q); }
    public void visit_ishl(OPER q) { visit_default(q); }
    public void visit_ishr(OPER q) { visit_default(q); }
    public void visit_iushr(OPER q) { visit_default(q); }
    public void visit_ixor(OPER q) { visit_default(q); }
    public void visit_lor(OPER q) { visit_default(q); }
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

    // Class merge functino.

    HClass merge(HClass a, HClass b) {
	Util.assert(a!=null && b!=null);
	if (a==b) return a; // take care of primitive types.

	// Special case 'Void' Hclass, used for null constants.
	if (a==HClass.Void)
	    return b;
	if (b==HClass.Void)
	    return a;

	// by this point better be array ref or object, not primitive type.
	Util.assert((!a.isPrimitive()) && (!b.isPrimitive()));
	int Adims = HClassUtil.dims(a);
	int Bdims = HClassUtil.dims(b);
	if (Adims==Bdims) {
	    a = HClassUtil.baseClass(a);
	    b = HClassUtil.baseClass(b);
	    // merge base component classes, then reform array.
	    return HClassUtil.arrayClass(HClassUtil.commonSuper(a, b), Adims);
	} else { // dimensions not equal.
	    int mindims = (Adims<Bdims)?Adims:Bdims;
	    // make an Object array of the smaller dimension.
	    return HClassUtil.arrayClass(HClass.forClass(Object.class), 
					 mindims);
	}
    }

    /*-------------------------------------------------------------*/
    // Lattice classes.

    /** No information obtainable about a temp. */
    static class LatticeVal {
	public String toString() { return "Top"; }
	public boolean equals(Object o) { return o instanceof LatticeVal; }
	public boolean higherThan(LatticeVal v) { return false; }
    }
    /** A typed temp. */
    static class xClass extends LatticeVal {
	protected HClass type;
	public xClass(HClass type) { this.type = type; }
	public HClass type() { return type; }
	public String toString() { 
	    return "xClass: " + type;
	}
	public boolean equals(Object o) {
	    return (o instanceof xClass &&
		    ((xClass)o).type.equals(type));
	}
	public boolean higherThan(LatticeVal v) {
	    if (!(v instanceof xClass)) return false;
	    if (v.equals(this)) return false;
	    return true;
	}
    }
    /** A single class type; guaranteed the value is not null. */
    static class xClassNonNull extends xClass {
	public xClassNonNull(HClass type) { 
	    super( type );
	}
	public String toString() { 
	    return "xClassNonNull: { " + type + " }";
	}
	public boolean equals(Object o) {
	    return (o instanceof xClassNonNull && super.equals(o));
	}
	public boolean higherThan(LatticeVal v) {
	    if (!(v instanceof xClassNonNull)) return false;
	    if (v.equals(this)) return false;
	    return true;
	}
    }
    /** An array with constant length.  The array is not null, of course. */
    static class xClassArray extends xClassNonNull {
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
	public boolean equals(Object o) {
	    return (o instanceof xClassArray && super.equals(o) &&
		    ((xClassArray)o).length == length);
	}
	public boolean higherThan(LatticeVal v) {
	    if (!(v instanceof xClassNonNull)) return false;
	    if (v.equals(this)) return false;
	    return true;
	}
    }
    /** An integer value of the specified bitwidth. */
    static class xBitWidth extends xClassNonNull {
	/** Highest significant bit for positive numbers. */
	protected int plusWidth;
	/** Highest significant bit for negative numbers. */
	protected int minusWidth;
	/** Constructor. */
	public xBitWidth(HClass type, int minusWidth, int plusWidth) {
	    super(type);
	    // limit.
	    if (type == HClass.Long) {
		this.minusWidth = Math.min(64, minusWidth);
		this.plusWidth  = Math.min(63, plusWidth);
	    } else if (type == HClass.Int) {
		this.minusWidth = Math.min(32, minusWidth);
		this.plusWidth  = Math.min(31, plusWidth);
	    } else if (type == HClass.Boolean) {
		this.minusWidth = Math.min( 0, minusWidth);
		this.plusWidth  = Math.min( 1, plusWidth);
	    } else // NON-CANONICAL TYPES: CAREFUL!
		if (type == HClass.Short) { this.type = HClass.Int;
		this.minusWidth = Math.min(16, minusWidth);
		this.plusWidth  = Math.min(15, plusWidth);
	    } else if (type == HClass.Byte) { this.type = HClass.Int;
		this.minusWidth = Math.min( 8, minusWidth);
		this.plusWidth  = Math.min( 7, plusWidth);
	    } else if (type == HClass.Char) { this.type = HClass.Int;
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
	public boolean equals(Object o) {
	    return (o instanceof xBitWidth && super.equals(o) &&
		    ((xBitWidth)o).minusWidth == minusWidth   &&
		    ((xBitWidth)o).plusWidth  == plusWidth    );
	}
	public boolean higherThan(LatticeVal v) {
	    if (!(v instanceof xClassNonNull)) return false;
	    if (v.equals(this)) return false;
	    return true;
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
	    if (type==HClass.Boolean) return new Integer(value!=0?1:0);
	    throw new Error("Unknown integer constant type.");
	}
	public String toString() {
	    return "xIntConstant: " + type + " " + value;
	}
	public boolean equals(Object o) {
	    return (o instanceof xIntConstant && super.equals(o) &&
		    ((xIntConstant)o).value == value);
	}
	public boolean higherThan(LatticeVal v) {
	    if (!(v instanceof xIntConstant)) return false;
	    if (v.equals(this)) return false;
	    return true;
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
	public boolean equals(Object o) {
	    return (o instanceof xNullConstant);
	}
	public boolean higherThan(LatticeVal v) {
	    if (!(v instanceof xClass) ) return false;
	    if (v.equals(this) ) return false;
	    return true;
	}
    }
    static class xFloatConstant extends xClassNonNull 
	implements xConstant {
	protected Object value;
	public xFloatConstant(HClass type, Object value) {
	    super(type); this.value = value;
	}
	public Object constValue() { return value; }
	public String toString() {
	    return "xFloatConstant: " + type + " " + value.toString();
	}
	public boolean equals(Object o) {
	    return (o instanceof xFloatConstant && super.equals(o) &&
		    ((xFloatConstant)o).value.equals(value));
	}
	public boolean higherThan(LatticeVal v) {
	    if (!(v instanceof xClassNonNull)) return false;
	    if (v.equals(this)) return false;
	    return true;
	}
    }
    static class xStringConstant extends xClassNonNull
	implements xConstant {
	protected Object value;
	public xStringConstant(HClass type, Object value) {
	    super(type); this.value = value;
	}
	public Object constValue() { return value; }
	public String toString() {
	    return "xStringConstant: " + 
		"\"" + Util.escape(value.toString()) + "\"";
	}
	public boolean equals(Object o) {
	    return (o instanceof xStringConstant && super.equals(o) &&
		    ((xStringConstant)o).value.equals(value));
	}
	public boolean higherThan(LatticeVal v) {
	    if (!(v instanceof xClassNonNull)) return false;
	    if (v.equals(this)) return false;
	    return true;
	}
    }
    static interface xConstant {
	public Object constValue();
    }
}
