// SCC.java, created Fri Sep 11 00:57:39 1998 by cananian
package harpoon.Analysis;

import harpoon.Analysis.Maps.UseDefMap;
import harpoon.Analysis.Maps.ConstMap;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.IR.QuadSSA.*;
import harpoon.Temp.Temp;
import harpoon.Util.UniqueFIFO;
import harpoon.Util.Worklist;
import harpoon.Util.HClassUtil;
import harpoon.Util.Util;

import java.util.Hashtable;
/**
 * <code>SCC</code> implements a Sparse Conditional Constant Propagation
 * analysis.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SCC.java,v 1.6 1998-09-14 05:21:45 cananian Exp $
 */

public class SCC implements TypeMap, ConstMap {
    // Various object types in the lattice (null is Bottom)
    // lattice is ordered: bottom->value->xClass->top.
    // generally even if we don't know the value, we know the class...
    static class Alphabet {
	static Top top = new Top();
    }
    static class Top extends Alphabet { 
	public String toString() { return "Top"; } 
	public boolean equals(Object o) { return o instanceof Top; }
    }
    static class Value extends Alphabet {
	Object value;
	HClass type;
	Value(Object value, HClass type) {
	    this.value=value; this.type=type;
	}
	public String toString() { return "Value ("+type+") "+value; }
	public boolean equals(Object o) {
	    if (o instanceof Value) {
		Value v = (Value) o;
		return (value.equals(v.value) && type.equals(v.type));
	    }
	    return false;
	}
    }
    static class xClass extends Alphabet {
	HClass type;
	xClass(HClass type) {
	    this.type=type;
	}
	public String toString() { return "xClass "+type; }
	public boolean equals(Object o) {
	    if (o instanceof xClass) {
		xClass xc = (xClass) o;
		return (type==xc.type);
	    }
	    return false;
	}
    }
    

    /** Creates an <code>SCC</code> analyzer. */
    public SCC(UseDefMap usedef) { this.usedef = usedef; }
    /** Creates an <code>SCC</code> analyzer. */
    public SCC() { this(new UseDef()); }
    UseDefMap usedef;

    //----------------------------------------------
    // public information accessor methods.

    /** Determine whether <code>Quad q</code> in <code>HMethod m</code>
     *  is executable. */
    public boolean isExec(HCode hc, Quad q) {
	analyze(hc); return E.contains(q);
    }
    /** Determine the static type of <code>Temp t</code> in 
     *  <code>HMethod m</code>. */
    public HClass typeMap(HCode hc, Temp t) {
	analyze(hc);
	Alphabet v = V.get(t);
	if (v instanceof Value) return ((Value)v).type;
	if (v instanceof xClass) return ((xClass)v).type;
	return null;
    }
    /** Determine whether <code>Temp t</code> in <code>HMethod m</code>
     *  has a constant value. */
    public boolean isConst(HCode hc, Temp t) {
	analyze(hc); return (V.get(t) instanceof Value);
    }
    /** Determine the constant value of <code>Temp t</code> in 
     *  <code>HMethod m</code>. 
     *  @exception Error if <code>Temp t</code> is not a constant.
     */
    public Object constMap(HCode hc, Temp t) {
	analyze(hc);
	Alphabet v = V.get(t);
	if (v instanceof Value) return ((Value)v).value;
	throw new Error(t.toString() + " not a constant");
    }
    
    //--------------------------------------
    // Class state.
    /** Set of all executable quads. */
    QuadSet  E = new QuadSet();
    /** Mapping from Temps to Alphabet lattices */
    AlphaMap V = new AlphaMap();
    /** Mapping from HMethods to Return/Throw types. */
    Hashtable analyzed = new Hashtable();

    //-------------------------------------
    // Analysis code.
    /** Main analysis method.  */
    void analyze(HCode hc) {
	// don't do things more than once.
	if (analyzed.containsKey(hc)) return;
	analyzed.put(hc, hc);

	HMethod method = hc.getMethod();
	Util.assert(hc instanceof harpoon.IR.QuadSSA.Code);

	// initialize worklists.
	Worklist Wv = new UniqueFIFO();
	Worklist Wb = new UniqueFIFO();

	// put the root entry on the worklist & mark it executable.
	Quad root= (Quad) hc.getRootElement();
	Wb.push(root);
	E.set(root);

	// Iterate
	while (! (Wb.isEmpty() && Wv.isEmpty()) ) { // until both are empty
	    Quad[] ql; // the statements to examine against conditions 3-8

	    if (!Wb.isEmpty()) { // grab statement from Wb if we can.
		Quad q = (Quad) Wb.pull();
		// Rule 2: for any executable block with only one successor
		// C, set E[C] to true.
		if (q.next().length==1) {
		    Quad nq = q.next()[0];
		    if (!E.contains(nq)) {
			raiseE(E, Wb, nq);
		    }
		}
		ql = new Quad[] { q }; // examine this statement.
	    } else if (!Wv.isEmpty()) { // else grab temp from Wv
		Temp t = (Temp) Wv.pull();
		HCodeElement[] ul = usedef.useMap(hc, t); // list of uses of t 
		ql = new Quad[ul.length];
		System.arraycopy(ul, 0, ql, 0, ql.length);
	    } else ql = new Quad[0]; // should never execute.
	    // consider conditions 3-8 for all statements in ql.
	    for (int i=0; i<ql.length; i++) {
		Quad q = ql[i]; // consider this.

		if (!E.contains(q)) continue; // not executable.

		// consider the statement.
		if (q instanceof AGET) { // only know class.
		    Alphabet v = V.get( ((AGET)q).objectref );
		    if (v instanceof xClass)
			raiseV(V, Wv, ((AGET)q).dst, 
			       new xClass( ((xClass)v).type
					   .getComponentType()) );
		    if (v instanceof Top)
			raiseV(V, Wv, ((AGET)q).dst, Alphabet.top);
		} else if (q instanceof ALENGTH) {
		    raiseV(V, Wv, q.def()[0],
			   new xClass( HClass.Int ) );
		} else if (q instanceof ANEW) {
		    raiseV(V, Wv, q.def()[0],
			   new xClass( ((ANEW)q).hclass ) );
		} else if (q instanceof CALL) {
		    if (((CALL)q).retval!=null)
			raiseV(V, Wv, ((CALL)q).retval,
			       new xClass( ((CALL)q).method.getReturnType()) );
		    raiseV(V, Wv, ((CALL)q).retex, // XXX can do better.
			   new xClass( HClass.forClass(Throwable.class) ) );
		} else if (q instanceof COMPONENTOF) { // XXX fixme
		    raiseV(V, Wv, q.def()[0],
			   new xClass( HClass.Boolean ) );
		} else if (q instanceof CONST) {
		    raiseV(V, Wv, q.def()[0],
			   new Value( ((CONST)q).value, ((CONST)q).type ) );
		} else if (q instanceof GET) {
		    raiseV(V, Wv, q.def()[0],
			   new xClass( ((GET)q).field.getType() ) );
		} else if (q instanceof INSTANCEOF) {
		    Alphabet result = new xClass(HClass.Boolean); // default.

		    INSTANCEOF Q = (INSTANCEOF) q;
		    Alphabet a = V.get( Q.src );
		    if (Q.hclass == HClass.forClass(Object.class))
			result = new Value(new Boolean(true), HClass.Boolean);
		    else if (a instanceof Value) {
			Value v = (Value)a;
			if (v.type.isSuperclassOf(Q.hclass)) // unknowable.
			    ;
			else if (Q.hclass.isSuperclassOf(v.type)) //always true
			    result = new Value(new Boolean(true), 
					       HClass.Boolean);
			else // always false
			    result = new Value(new Boolean(false),
					       HClass.Boolean);
		    }
		    // XXX can't do xClass analysis unless we know whether
		    // xClass value can be null.
		    raiseV(V, Wv, q.def()[0], result);
		} else if (q instanceof METHODHEADER) {
		    METHODHEADER Q = (METHODHEADER) q;
		    HClass[] pt = method.getParameterTypes();
		    int j=0;
		    if (!method.isStatic())
			raiseV(V, Wv, Q.params[j++],
			       new xClass( method.getDeclaringClass() ) );
		    for (int k=0; k<pt.length; j++, k++)
			raiseV(V, Wv, Q.params[j], 
			       new xClass( pt[k] ) );
		} else if (q instanceof MOVE) {
		    Alphabet v = V.get(q.use()[0]);
		    if (v!=null)
			raiseV(V, Wv, q.def()[0], v );
		} else if (q instanceof NEW) {
		    raiseV(V, Wv, q.def()[0],
			   new xClass( ((NEW)q).hclass ) );
		} else if (q instanceof OPER) {
		    OPER Q = (OPER) q;
		    boolean anyTops = false;
		    Object[] op = new Object[Q.operands.length];
		    int j;
		    for (j=0; j<Q.operands.length; j++) {
			Alphabet v = V.get(Q.operands[j]);
			if (v == null) break; // still undefined
			if (v instanceof Value) {
			    op[j] = ((Value)v).value;
			} else { anyTops = true; break; }
		    }
		    if (j==Q.operands.length) { // made it to end of loop.
			// RULE 3:
			Object o = Q.evalValue(op);
			raiseV(V, Wv, Q.dst,
			       new Value(o, Q.evalType() ) );
		    } else if (anyTops) { // result is unknowable.
			// RULE 4:
			raiseV(V, Wv, Q.dst,
			       new xClass( Q.evalType() ) );
		    }
		} else if (q instanceof PHI) {
		    PHI Q = (PHI) q;
		    for (int j=0; j<Q.dst.length; j++) {//for each phi-function
			Value o = null;
			Temp s[] = (Temp[]) Util.copy(Q.src[j]);
			// skip unexecutable edges by scribbling null.
			Quad p[] = Q.prev();
			Util.assert(p.length==s.length);
			for (int k=0; k<p.length; k++)
			    if (!E.contains(p[k]))
				s[k]=null; // scribble out this entry.

			int k;
			for (k=0; k<s.length; k++) { // for each arg.
			    if (s[k]==null) continue;
			    Alphabet v = V.get(s[k]);
			    if (v instanceof Value) {
				Value val = (Value) v;
				// RULE 5:
				if (o!=null && !o.equals(val) ) {
				    raiseV(V, Wv, Q.dst[j],
					   new xClass( merge(V, s) ) );
				    break;
				}
				o = val;
			    } else if (v instanceof xClass ||
				       v instanceof Top) {
				// RULE 6:
				raiseV(V, Wv, Q.dst[j],
				       new xClass( merge(V, s) ) );
				break;
			    }
			}
			if (k==s.length && o!=null) {
			    // RULE 7:
			    raiseV(V, Wv, Q.dst[j], o);
			}
		    }
		} else if (q instanceof CJMP) {
		    Alphabet v = V.get(q.use()[0]);
		    if (v instanceof xClass ||
			v instanceof Top) {
			// RULE 8:
			raiseE(E, Wb, q.next()[0]);
			raiseE(E, Wb, q.next()[1]);
		    } else if (v instanceof Value) {
			// RULE 9:
			boolean test = 
			    ((Boolean) ((Value)v).value).booleanValue();
			if (test)
			    raiseE(E, Wb, q.next()[1]);
			else
			    raiseE(E, Wb, q.next()[0]);
		    }
		} else if (q instanceof SWITCH) {
		    SWITCH Q = (SWITCH) q;
		    Alphabet v = V.get(Q.index);
		    if (v instanceof xClass ||
			v instanceof Top) {
			// RULE 8:
			Quad[] nxt = q.next();
			for (int j=0; j<nxt.length; j++)
			    raiseE(E, Wb, nxt[j]);
		    } else if (v instanceof Value) {
			// RULE 9:
			int index =
			    ((Integer) ((Value)v).value).intValue();
			int j;
			for (j=0; j<Q.keys.length; j++)
			    if (Q.keys[j] == index) {
				raiseE(E, Wb, q.next()[j]);
				break;
			    }
			if (j==Q.keys.length) // no match found.
			    raiseE(E, Wb, q.next()[j]); // default target.
		    }
		}
	    } // END for every statement in ql.
	} // END while worklists not empty.
    } // END analyze method.

    //------------------------------------------------------
    // Utility functions to raise an element in E or V:

    /** Raise element q in E, adding q to Wb if necessary. */
    void raiseE(QuadSet E, Worklist Wb, Quad q) {
	if (E.contains(q)) return;
	E.set(q); Wb.push(q);
    }
    /** Raise element t to a in V, adding t to Wv if necessary. */
    void raiseV(AlphaMap V, Worklist Wv, Temp t, Alphabet a) {
	Alphabet old = V.get(t);
	// only allow raising value.  null->value->xClass->Top
	if (old instanceof Value)
	    if (a == null || a instanceof Value) return;
	if (old instanceof xClass) {
	    if (a == null || a instanceof Value) return;
	    if (old.equals(a)) return;
	}
	if (old instanceof Top)
	    return;
	// okay, it's valid to raise V.
	V.put(t, a);
	Wv.push(t);
    }

    //---------------------------------------------------------
    // Functions to merge two xClass values in a Phi.

    HClass merge(AlphaMap V, Temp[] src) {
	HClass r = null;
	for (int i=0; i<src.length; i++) {
	    if (src[i]==null) continue; // skip null entries.
	    Alphabet a = V.get(src[i]);
	    HClass hc;
	    if (a instanceof Value) {
		hc = ((Value)a).type;
	    } else if (a instanceof xClass) {
		hc = ((xClass)a).type;
	    } else continue; // skip bottoms and tops.
	    if (r==null) r=hc;
	    else r = merge(r, hc);
	}
	return r;
    }
    HClass merge(HClass a, HClass b) {
	Util.assert(a!=null && b!=null);
	if (a==b) return a; // take care of primitive types.

	// Special case 'Void' Hclass, used for null constants.
	if (a==HClass.Void && b != HClass.Void)
	    return b;
	if (b==HClass.Void) // covers case where both a and b are Void.
	    return a;

	// by this point better be array ref, not primitive type.
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

    //---------------------------------------------------------
    // Auxilliary mapping classes.
    
    /** Maintains a set of Quads (each element unique). */
    class QuadSet {
	Hashtable h = new Hashtable();
	void set(Quad q) { h.put(q, q); }
	void remove(Quad q) { h.remove(q); }
	boolean contains(Quad q) { return h.containsKey(q); }
    }
    /** Maintains a mapping from Temp to Alphabet. */
    class AlphaMap {
	Hashtable h = new Hashtable();
	void put(Temp t, Alphabet a) { h.put(t, a); }
	Alphabet get(Temp t) { return (Alphabet) h.get(t); }
	void remove(Temp t) { h.remove(t); }
	boolean contains(Temp t) { return h.containsKey(t); }
    }
}



