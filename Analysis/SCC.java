// SCC.java, created Fri Sep 11 00:57:39 1998 by cananian
package harpoon.Analysis;

import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;
import harpoon.Temp.Temp;
import harpoon.Util.*;

import java.util.Hashtable;
/**
 * <code>SCC</code> implements Sparse Conditional Constant Propagation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SCC.java,v 1.1 1998-09-11 07:00:14 cananian Exp $
 */

public class SCC  {
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
    

    /** Creates a <code>SCC</code>. */
    public SCC() {
        
    }

    public boolean isExec(HMethod method, Quad q) {
	analyze(method); return E.contains(q);
    }
    public HClass typeMap(HMethod method, Temp t) {
	analyze(method);
	Alphabet v = V.get(t);
	if (v instanceof Value) return ((Value)v).type;
	if (v instanceof xClass) return ((xClass)v).type;
	return null;
    }
    public boolean isConst(HMethod method, Temp t) {
	analyze(method); return (V.get(t) instanceof Value);
    }
    public Object constMap(HMethod method, Temp t) {
	analyze(method);
	Alphabet v = V.get(t);
	if (v instanceof Value) return ((Value)v).value;
	throw new Error(t.toString() + " not a constant");
    }
    
    QuadSet  E = new QuadSet();
    AlphaMap V = new AlphaMap();
    Hashtable analyzed = new Hashtable();

    void analyze(HMethod method) {
	// don't do things more than once.
	if (analyzed.containsKey(method)) return;
	analyzed.put(method, method);

	// need list of uses.
	UseDef usedef = new UseDef();

	// initialize worklists.
	UniqueFIFO Wv = new UniqueFIFO();
	UniqueFIFO Wb = new UniqueFIFO();

	// put the root entry on the worklist & mark it executable.
	Quad root= (Quad)harpoon.IR.QuadSSA.Code.code(method).getElements()[0];
	Wb.push(root);
	E.set(root);

	// Iterate
	while (! (Wb.empty() && Wv.empty()) ) { // until both are empty
	    Quad[] ql; // the statements to examine against conditions 3-8

	    if (!Wb.empty()) { // grab statement from Wb if we can.
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
	    } else if (!Wv.empty()) { // else grab temp from Wv
		Temp t = (Temp) Wv.pull();
		ql = usedef.useSites(method, t); // list of uses of t
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
		    raiseV(V, Wv, ((CALL)q).retex, // XXX
			   new xClass( HClass.forClass(Throwable.class) ) );
		} else if (q instanceof COMPONENTOF) {
		    raiseV(V, Wv, q.def()[0],
			   new xClass( HClass.Boolean ) );
		} else if (q instanceof CONST) {
		    raiseV(V, Wv, q.def()[0],
			   new Value( ((CONST)q).value, ((CONST)q).type ) );
		} else if (q instanceof GET) {
		    raiseV(V, Wv, q.def()[0],
			   new xClass( ((GET)q).field.getType() ) );
		} else if (q instanceof INSTANCEOF) {
		    raiseV(V, Wv, q.def()[0],
			   new xClass( HClass.Boolean ) );
		} else if (q instanceof METHODHEADER) {
		    METHODHEADER Q = (METHODHEADER) q;
		    HClass[] hc = method.getParameterTypes();
		    int j=0;
		    if (!method.isStatic())
			raiseV(V, Wv, Q.params[j++],
			       new xClass( method.getDeclaringClass() ) );
		    for (int k=0; k<hc.length; j++, k++)
			raiseV(V, Wv, Q.params[j], 
			       new xClass( hc[k] ) );
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
			int k;
			for (k=0; k<Q.src[j].length; k++) { // for each arg.
			    Alphabet v = V.get(Q.src[j][k]);
			    if (v instanceof Value) {
				Value val = (Value) v;
				// RULE 5:
				if (o!=null && !o.equals(val) ) {
				    raiseV(V, Wv, Q.dst[j],
					   new xClass( merge(V, Q.src[j]) ) );
				    break;
				}
				o = val;
			    } else if (v instanceof xClass ||
				       v instanceof Top) {
				// RULE 6:
				raiseV(V, Wv, Q.dst[j],
				       new xClass( merge(V, Q.src[j]) ) );
				break;
			    }
			}
			if (k==Q.src[j].length && o!=null) {
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


    void raiseE(QuadSet E, UniqueFIFO Wb, Quad q) {
	if (E.contains(q)) return;
	E.set(q); Wb.push(q);
    }
    void raiseV(AlphaMap V, UniqueFIFO Wv, Temp t, Alphabet a) {
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

    HClass merge(AlphaMap V, Temp[] src) {
	return null; // XXX FIXME
    }

    class QuadSet {
	Hashtable h = new Hashtable();
	void set(Quad q) { h.put(q, q); }
	void remove(Quad q) { h.remove(q); }
	boolean contains(Quad q) { return h.containsKey(q); }
    }
    class AlphaMap {
	Hashtable h = new Hashtable();
	void put(Temp t, Alphabet a) { h.put(t, a); }
	Alphabet get(Temp t) { return (Alphabet) h.get(t); }
	void remove(Temp t) { h.remove(t); }
	boolean contains(Temp t) { return h.containsKey(t); }
    }
}



