// SCC.java, created Fri Sep 18 17:45:07 1998 by cananian
package harpoon.Analysis;

import harpoon.Analysis.Maps.ConstMap;
import harpoon.Analysis.Maps.ExecMap;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.Maps.UseDefMap;
import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;
import harpoon.Temp.Temp;
import harpoon.Util.HClassUtil;
import harpoon.Util.Set;
import harpoon.Util.Util;
import harpoon.Util.Worklist;

import java.util.Hashtable;
import java.util.Enumeration;
/**
 * <code>SCC</code> implements Sparse Conditional Constant Propagation,
 * with extension to allow type and bitwidth analysis.  Fun, fun, fun.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SCC.java,v 1.10 1998-09-21 00:24:27 cananian Exp $
 */

public class SCC implements TypeMap, ConstMap, ExecMap {
    UseDefMap udm;

    /** Creates a <code>SCC</code>. */
    public SCC(UseDefMap usedef) {
	this.udm = usedef;
    }

    /*-----------------------------*/
    // Class state.
    /** Set of all executable edges. */
    Set Ee = new Set();
    /** Set of all executable quads. */
    Set Eq = new Set();
    /** Mapping from Temps to lattice values. */
    Hashtable V = new Hashtable();

    /*---------------------------*/
    // public information accessor methods.

    /** Determine whether <code>Quad q</code> in <code>HMethod m</code>
     *  is executable. */
    public boolean execMap(HCode hc, HCodeElement quad) {
	analyze(hc); return Eq.contains(quad);
    }
    /** Determine whether <code>Edge e</code> in <code>HMethod m</code>
     *  is executable. */
    public boolean execMap(HCode hc, HCodeEdge edge) {
	analyze(hc); return Ee.contains(edge);
    }
    /** Determine the static type of <code>Temp t</code> in 
     *  <code>HMethod m</code>. */
    public HClass typeMap(HCode hc, Temp t) {
	analyze(hc);  LatticeVal v = (LatticeVal) V.get(t);
	if (v instanceof xClass) return ((xClass)v).type();
	return null;
    }
    /** Determine whether <code>Temp t</code> in <code>HMethod m</code>
     *  has a constant value. */
    public boolean isConst(HCode hc, Temp t) {
	analyze(hc); return (V.get(t) instanceof xConstant);
    }
    /** Determine the constant value of <code>Temp t</code> in 
     *  <code>HMethod m</code>. 
     *  @exception Error if <code>Temp t</code> is not a constant.
     */
    public Object constMap(HCode hc, Temp t) {
	analyze(hc);  LatticeVal v = (LatticeVal) V.get(t);
	if (v instanceof xConstant) return ((xConstant)v).constValue();
	throw new Error(t.toString() + " not a constant");
    }
    

    /*---------------------------*/
    // Analysis code.

    /** Set of analyzed methods. */
    Set analyzed = new Set();
    HCode lastHCode = null;
    /** Main analysis method. */
    void analyze(HCode hc) {
	// caching.
	if (lastHCode==hc) return; // quick exit.
	if (analyzed.contains(hc)) return;
	analyzed.union(hc);
	lastHCode = hc;

	// Initialize worklists.
	Worklist Wv = new Set(); // variable worklist.
	Worklist Wq = new Set(); // block worklist.

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
	if (old != null && a.equals(old)) return;
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
	SCCVisitor(HCode hc, Worklist Wv, Worklist Wq) {
	    this.hc = hc;  this.Wv = Wv;  this.Wq = Wq;
	}

	// utility functions.
	LatticeVal get(Temp t) { return (LatticeVal) V.get(t); }
	int whichSigma(SIGMA s, Temp t) {
	    for (int i=0; i < s.src.length; i++)
		if (s.src[i] == t)
		    return i;
	    return -1;
	}

	// visitation.
	public void visit(Quad q) { /* do nothing. */ }
	public void visit(AGET q) {
	    LatticeVal v = get( q.objectref );
	    if (v instanceof xClass)
		raiseV(V, Wv, q.dst, 
		       new xClass( ((xClass)v).type().getComponentType() ) );
	}
	public void visit(ALENGTH q) {
	    LatticeVal v = get( q.objectref );
	    if (v instanceof xClassArray)
		raiseV(V, Wv, q.dst,
		       new xIntConstant(HClass.Int, 
					((xClassArray)v).length() ) );
	    else if (v instanceof xClass) // length is non-negative.
		raiseV(V, Wv, q.dst, new xBitWidth(HClass.Int, 0, 32) );
	}
	public void visit(ANEW q) { // dst of ANEW is non-null.
	    if (q.dims.length==1) {
		LatticeVal v = get( q.dims[0] );
		if (v instanceof xIntConstant) {
		    raiseV(V, Wv, q.dst, 
			   new xClassArray(q.hclass, 
					   (int) ((xIntConstant)v).value()) );
		    return;
		} else if (v == null) return; // bottom.
	    }
	    raiseV(V, Wv, q.dst, new xClassNonNull(q.hclass) );
	}
	public void visit(ASET q) { /* do nothing. */ }
	public void visit(CALL q) {
	    if (q.retval != null)
		raiseV(V, Wv, q.retval, new xClass(q.method.getReturnType() ));
	    raiseV(V, Wv, q.retex, 
		   new xClass( HClass.forClass(Throwable.class) ) );
	}
	public void visit(CJMP q) {
	    boolean takeTrue;
	    boolean takeFalse;
	    // is test constant?
	    LatticeVal v = get( q.test );
	    if (v instanceof xConstant) {
		boolean test =
		    ((Boolean) ((xConstant)v).constValue() ).booleanValue();
		if (test)
		    raiseE(Ee, Eq, Wq, q.nextEdge(1) ); // true edge.
		else
		    raiseE(Ee, Eq, Wq, q.nextEdge(0) ); // false edge.
		takeTrue = test;
		takeFalse= !test;
	    } else if (v instanceof xClass) { // ie, not bottom.
		// both edges are potentially executable.
		raiseE(Ee, Eq, Wq, q.nextEdge(1) );
		raiseE(Ee, Eq, Wq, q.nextEdge(0) );
		takeTrue = takeFalse = true;
	    } else return; // not enough info.
	    // look at definition of boolean condition.
	    /* XXX XXX XXX
	    Quad def = (Quad) udm.defmap(hc, q.test)[0]; // SSA form, right?
	    if (def instanceof OPER) {
		OPER o = (OPER) def;
		String opc = o.opcode.intern();
		LatticeVal left = o.operands.length<1?null: get(o.operands[0]);
		LatticeVal right= o.operands.length<2?null: get(o.operands[1]);
		int l = (left==null)?-1:whichSigma(q, o.operands[0] );
		int r = (right==null)?-1:whichSigma(q, o.operands[1] );
		// repeat twice; switching left and right in between.
		for (int i=0; i<2; i++) {
		if (opc == "acmpeq") { // check for test against null.
		    if (right instanceof xConstant && (l != -1)
			((xConstant)right).constValue() == null )
			raiseV();
		}
	    }
	    */ // XXX XXX XXX
	    // fallback.
	    for (int i=0; i < q.src.length; i++) {
		LatticeVal v2 = get ( q.src[i] );
		if (v2 != null) {
		    if (takeFalse)
			raiseV(V, Wv, q.dst[i][0], v2);
		    if (takeTrue)
			raiseV(V, Wv, q.dst[i][1], v2);
		}
	    }
	}
	public void visit(COMPONENTOF q) {
	    // we're guaranteed that q.arrayref is non-null here.
	    LatticeVal vA = get( q.arrayref );
	    LatticeVal vO = get( q.objectref );
	    if (vA instanceof xClass && vO instanceof xClass) {
		HClass hcA = ((xClass) vA).type().getComponentType() ;
		HClass hcO = ((xClass) vO).type();
		// special case when q.objectref is null
		if (hcO == HClass.Void) // always true.
		    raiseV(V, Wv, q.dst, new xIntConstant(HClass.Boolean,1));
		else if (hcA.isSuperclassOf(hcO)) // always true
		    raiseV(V, Wv, q.dst, new xIntConstant(HClass.Boolean,1));
		else if (hcO.isSuperclassOf(hcA)) // unknownable.
		    raiseV(V, Wv, q.dst, new xBitWidth(HClass.Boolean,1,0));
		else // always false.
		    raiseV(V, Wv, q.dst, new xIntConstant(HClass.Boolean,0));
	    }
	}
	public void visit(CONST q) {
	    if (q.type == HClass.Void) // null constant
		raiseV(V, Wv, q.dst, new xNullConstant() );
	    else if (q.type==HClass.forClass(String.class)) // string constant
		raiseV(V, Wv, q.dst, new xStringConstant(q.type,q.value) );
	    else if (q.type==HClass.Float || q.type==HClass.Double) // fp const
		raiseV(V, Wv, q.dst, new xFloatConstant(q.type,q.value) );
	    else if (q.type==HClass.Int || q.type == HClass.Long)
		raiseV(V, Wv, q.dst, 
		       new xIntConstant(q.type,((Number)q.value).longValue()));
	    else throw new Error("Unknown CONST type");
	}
	public void visit(FOOTER q) { /* do nothing. */ }
	public void visit(GET q) {
	    HClass type = q.field.getType();
	    if (q.field.isConstant()) {
		Object val = q.field.getConstant();
		if (type == HClass.forClass(String.class))
		    raiseV(V, Wv, q.dst, new xStringConstant(type, val) );
		else if (type == HClass.Float || type == HClass.Double )
		    raiseV(V, Wv, q.dst, new xFloatConstant(type, val) );
		else if (type == HClass.Int || type == HClass.Long)
		    raiseV(V, Wv, q.dst, 
			   new xIntConstant(type,((Number)val).longValue() ) );
		else throw new Error("Unknown constant field type.");
	    } else raiseV(V, Wv, q.dst, new xClass( type ) );
	}
	public void visit(INSTANCEOF q) {
	    // no guarantee that src is not null.
	    LatticeVal v = get( q.src );
	    if (v instanceof xNullConstant) // always true.
		raiseV(V, Wv, q.dst, new xIntConstant(HClass.Boolean,1) );
	    else if (v instanceof xClassNonNull) { // analyzable
		HClass hcO = ((xClassNonNull)v).type();
		if (q.hclass.isSuperclassOf(hcO)) // always true
		    raiseV(V, Wv, q.dst, new xIntConstant(HClass.Boolean,1) );
		else if (hcO.isSuperclassOf(q.hclass)) // unknowable.
		    raiseV(V, Wv, q.dst, new xBitWidth(HClass.Boolean,1,0) );
		else // always false.
		    raiseV(V, Wv, q.dst, new xIntConstant(HClass.Boolean,0) );
	    }
	    else if (v instanceof xClass) { // could be null.
		HClass hcO = ((xClass)v).type();
		if (q.hclass.isSuperclassOf(hcO) || 
		    hcO.isSuperclassOf(q.hclass) ) // unknowable.
		    raiseV(V, Wv, q.dst, new xBitWidth(HClass.Boolean,1,0) );
		else // always false (even if src==null)
		    raiseV(V, Wv, q.dst, new xIntConstant(HClass.Boolean,0) );
	    }
	}
	public void visit(METHODHEADER q) {
	    HMethod m = hc.getMethod();
	    HClass[] pt = m.getParameterTypes();
	    int j=0;
	    if (!m.isStatic() ) // raise 'this' variable (non-null!)
		raiseV(V, Wv, q.params[j++],
		       new xClassNonNull( m.getDeclaringClass() ) );
	    for (int k=0; k < pt.length; j++, k++)
		if (pt[k].isPrimitive())
		    raiseV(V, Wv, q.params[j], new xClassNonNull( pt[k] ) );
		else
		    raiseV(V, Wv, q.params[j], new xClass( pt[k] ) );
	}
	public void visit(MONITORENTER q) { /* do nothing. */ }
	public void visit(MONITOREXIT q) { /* do nothing. */ }
	public void visit(MOVE q) {
	    LatticeVal v = get ( q.src );
	    if (v != null)
		raiseV(V, Wv, q.dst, v);
	}
	public void visit(NEW q) {
	    raiseV(V, Wv, q.dst, new xClassNonNull( q.hclass ) );
	}
	public void visit(NOP q) { /* do nothing. */ }
	public void visit(OPER q) {
	    boolean allConst = true;
	    boolean allWidth = true;

	    Object[] op = new Object[q.operands.length];
	    for (int i=0; i < q.operands.length; i++) {
		LatticeVal v = get( q.operands[i] );
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
		if (ty == HClass.Int || ty == HClass.Long)
		    raiseV(V, Wv, q.dst, 
			   new xIntConstant(ty, ((Number)o).longValue() ) );
		else if (ty == HClass.Float || ty == HClass.Double)
		    raiseV(V, Wv, q.dst, new xFloatConstant(ty, o) );
		else throw new Error("Unknown OPER result type.");
	    } /* else if (allWidth) ... XXX XXX XXX */
	    else {
		// RULE 4:
		HClass ty = q.evalType();
		if (ty.isPrimitive())
		    raiseV(V, Wv, q.dst, new xClassNonNull( ty ) );
		else
		    raiseV(V, Wv, q.dst, new xClass( ty ) );
	    }
	}
	public void visit(PHI q) {
	    for (int i=0; i<q.src.length; i++) { // for each phi-function.
		boolean allConst = true;
		boolean allWidth = true;
		boolean allNonNull=true;
		boolean someValidValue=false;

		Object constValue = null;
		HClass mergedType = null;
		int mergedWidthPlus = 0;
		int mergedWidthMinus= 0;
		for (int j=0; j < q.src[i].length; j++) {
		    if (!Ee.contains( q.prevEdge(j) ))
			continue; // skip non-executable edges.
		    LatticeVal v = get ( q.src[i][j] );
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
			     mergedType == HClass.Double)
			v = new xIntConstant(mergedType,
					     ((Number)constValue).longValue());
		    else throw new Error("Unknown constant type.");
		    raiseV(V, Wv, q.dst[i], v);
		} else if (allWidth) {
		    raiseV(V, Wv, q.dst[i], 
			   new xBitWidth(mergedType, 
					 mergedWidthMinus, mergedWidthPlus) );
		} else if (allNonNull) {
		    raiseV(V, Wv, q.dst[i], new xClassNonNull(mergedType) );
		} else {
		    raiseV(V, Wv, q.dst[i], new xClass(mergedType) );
		}
	    } // for each phi function.
	}
	public void visit(RETURN q) { /* do nothing. */ }
	public void visit(SET q) { /* do nothing. */ }
	public void visit(SWITCH q) {
	    LatticeVal v = get( q.index );
	    if (v instanceof xIntConstant) {
		int index = (int) ((xIntConstant)v).value();
		int i;
		for (i=0; i<q.keys.length; i++)
		    if (q.keys[i] == index)
			break;
		// now i has the target index, even for the default case.
		raiseE(Ee, Eq, Wq, q.nextEdge(i) ); // executable edge.
		// handle sigmas.
		for (int j=0; j < q.src.length; j++) {
		    LatticeVal v2 = get( q.src[j] );
		    if (v2 != null)
			raiseV(V, Wv, q.dst[j][i], v2);
		}
	    }
	    // XXX maybe stuff we can learn about v from bitwidth?
	    else if (v != null) {
		// mark all edges executable & propagate to all sigmas.
		for (int i=0; i < q.nextEdge().length; i++)
		    raiseE(Ee, Eq, Wq, q.nextEdge(i) );
		for (int i=0; i < q.dst.length; i++) {
		    LatticeVal v2 = get( q.src[i] );
		    if (v2 != null)
			for (int j=0; j < q.dst[i].length; i++)
			    raiseV(V, Wv, q.dst[i][j], v2);
		}
	    }
	}
	public void visit(THROW q) { /* do nothing. */ }
    }
    /*-------------------------------------------------------------*/
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
    public static class LatticeVal {
	public String toString() { return "Top"; }
	public boolean equals(Object o) { return o instanceof LatticeVal; }
	public boolean higherThan(LatticeVal v) { return false; }
    }
    /** A typed temp. */
    public static class xClass extends LatticeVal {
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
    public static class xClassNonNull extends xClass {
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
    public static class xClassArray extends xClassNonNull {
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
    public static class xBitWidth extends xClassNonNull {
	/** Highest significant bit for positive numbers. */
	protected int plusWidth;
	/** Highest significant bit for negative numbers. */
	protected int minusWidth;
	/** Constructor. */
	public xBitWidth(HClass type, int minusWidth, int plusWidth) {
	    super(type);
	    this.minusWidth = minusWidth;
	    this.plusWidth  = plusWidth;
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
    public static class xIntConstant extends xBitWidth implements xConstant {
	protected long value;
	public xIntConstant(HClass type, long value) {
	    super(type, value<0?Util.fls(-value):0, value>0?Util.fls(value):0);
	    this.value = value;
	}
	public long value() { return value; }
	public Object constValue() { 
	    if (type==HClass.Int) return new Integer((int)value);
	    if (type==HClass.Long) return new Long((long)value);
	    if (type==HClass.Boolean) return new Boolean(value!=0);
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
    public static class xNullConstant extends xClass implements xConstant {
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
    public static class xFloatConstant extends xClassNonNull 
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
    public static class xStringConstant extends xClassNonNull
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
    public static interface xConstant {
	public Object constValue();
    }
}
