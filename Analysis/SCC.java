// SCC.java, created Fri Sep 18 17:45:07 1998 by cananian
package harpoon.Analysis;

import harpoon.Analysis.Maps.ConstMap;
import harpoon.Analysis.Maps.ExecMap;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.Maps.UseDefMap;
import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;
import harpoon.Temp.Temp;
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
 * @version $Id: SCC.java,v 1.8 1998-09-19 06:20:16 cananian Exp $
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
	SCCVisitor visitor = new SCCVisitor(Wv, Wq);

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
	// local references to worklists.
	Worklist Wv, Wq;
	SCCVisitor(Worklist Wv, Worklist Wq) {
	    this.Wv = Wv;  this.Wq = Wq;
	}
	public void visit(Quad q) { /* do nothing. */ }
	/*
	public void visit(AGET q)		{ visit((Quad)q); }
	public void visit(ALENGTH q)	{ visit((Quad)q); }
	public void visit(ANEW q)		{ visit((Quad)q); }
	public void visit(ASET q)		{ visit((Quad)q); }
	public void visit(CALL q)		{ visit((Quad)q); }
	public void visit(CJMP q)		{ visit((SIGMA)q); }
	public void visit(COMPONENTOF q)	{ visit((Quad)q); }
	public void visit(CONST q)		{ visit((Quad)q); }
	public void visit(FOOTER q)		{ visit((Quad)q); }
	public void visit(GET q)		{ visit((Quad)q); }
	public void visit(HEADER q)		{ visit((Quad)q); }
	public void visit(INSTANCEOF q)	{ visit((Quad)q); }
	public void visit(SIGMA q)		{ visit((Quad)q); }
	public void visit(METHODHEADER q)	{ visit((Quad)q); }
	public void visit(MONITORENTER q)	{ visit((Quad)q); }
	public void visit(MONITOREXIT q)	{ visit((Quad)q); }
	public void visit(MOVE q)		{ visit((Quad)q); }
	public void visit(NEW q)		{ visit((Quad)q); }
	public void visit(NOP q)		{ visit((Quad)q); }
	public void visit(OPER q)		{ visit((Quad)q); }
	public void visit(PHI q)		{ visit((Quad)q); }
	public void visit(RETURN q)		{ visit((Quad)q); }
	public void visit(SET q)		{ visit((Quad)q); }
	public void visit(SWITCH q)		{ visit((SIGMA)q); }
	public void visit(THROW q)		{ visit((Quad)q); }
	*/
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
    public static class xNonIntConstant extends xClassNonNull 
	implements xConstant {
	protected Object value;
	public xNonIntConstant(HClass type, Object value) {
	    super(type); this.value = value;
	}
	public Object constValue() { return value; }
	public String toString() {
	    return "xNonIntConstant: " + type + " " + value.toString();
	}
	public boolean equals(Object o) {
	    return (o instanceof xNonIntConstant && super.equals(o) &&
		    ((xNonIntConstant)o).value.equals(value));
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
