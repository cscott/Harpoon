// AO.java, created Sat Oct 17 03:07:33 1998 by marinov
package harpoon.Analysis.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import harpoon.Util.*;
import harpoon.Temp.Temp;
import java.util.Hashtable;
import java.util.Enumeration;
import harpoon.Analysis.QuadSSA.TypeInfo;
import harpoon.Analysis.UseDef;
import harpoon.Analysis.Maps.UseDefMap;
/**
 * <code>AO</code> deteremines which objects are accessed in the given method.
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: AO.java,v 1.1.2.1 1998-12-03 07:52:38 marinov Exp $
 */

public class AO  {
    
    Hashtable ptr2sore = new Hashtable(); // maps pointers to simple object regexps
    TypeInfo ti = new TypeInfo(); // types used just to find out pointers
    UseDefMap usedef; // use def chains foolowed for pointers

    /** Creates an <code>AO</code>. */
    public AO(UseDefMap usedef) { this.usedef = usedef; }
    /** Creates an <code>AO</code>. */
    public AO() { this(new UseDef()); }

    public void analyze(QuadSSA hc) {
	Worklist wl = new UniqueFIFO();
	Quad[] ql = (Quad[]) hc.getElements();
	for (int i=0; i<ql.length; i++)
	    wl.push(ql[i]);
	HeapPointersVisitor hpv = new HeapPointersVisitor(hc);
	while (!wl.isEmpty()) {
	    //DEBUG
	    //for (int ii=0; ii<ql.length; ii++)
		//if (wl.contains(ql[ii])) System.out.print(ql[ii].getID()+" ");
	    //System.out.println();
	    //DEBUG
	    Quad q = (Quad) wl.pull();
	    hpv.modified = false;
	    q.visit(hpv);
	    if (hpv.modified) {
		//DEBUG
		//System.out.println("modified "+q.getID());
		//DEBUG
		Quad[] qn = (Quad[]) q.next();
		for (int j=0; j<qn.length; j++)
		    wl.push(qn[j]);
		Temp[] d = q.def();
		for (int i=0; i<d.length; i++) {
		    HCodeElement[] u = usedef.useMap(hc, d[i]);
		    for (int j=0; j<u.length; j++) {
			wl.push((Quad)u[j]);
		    }
		}
	    }
	}
	//DEBUG
	System.out.println(toString());
	//DEBUG
    }

    class HeapPointersVisitor extends QuadVisitor {
	harpoon.IR.Quads.QuadSSA c;
	boolean modified = false;
	HeapPointersVisitor(QuadSSA c) { this.c = c; }
	
	public void visit(Quad q) { modified = false; }
	public void visit(AGET q) { 
	    if (ptr(c, q.dst)) modified = move(c, q.dst, q.objectref);
	    else modified = false;
	}
	public void visit(ANEW q) { 
	    if ((q.dims.length>1)||objectClass(q.hclass)) modified = (ptr2sore.put(q.dst, SORE.singletonNew)!=SORE.singletonNew);
	    else modified = false;
	}
	public void visit(ASET q) { 
	    if (ptr(c, q.src)) modified = move(c, q.objectref, q.src);
	    else modified = false;
	}
	public void visit(CALL q) { /**/ }
	public void visit(CONST q) { 
	    if (ptr(c, q.dst)) modified = (ptr2sore.put(q.dst, SORE.emptySet)!=SORE.emptySet);
	    else modified = false;
	}
	public void visit(GET q) { 
	    if (ptr(c, q.dst)) modified = get(c, q.dst, q.field, q.objectref);
	    else modified = false;
	}
	public void visit(METHODHEADER q) { 
	    // this quad is visited only once, at the beginning, in intraprocedural analysis
	    for (int i=0; i<q.params.length; i++)
		if (ptr(c, q.params[i]))
		    ptr2sore.put(q.params[i], new SORE(q.params[i]));
	    modified = true;
	}
	public void visit(MOVE q) { 
	    if (ptr(c, q.dst)) modified = move(c, q.dst, q.src);
	    else modified = false;
	}
	public void visit(NEW q) { 
	    modified = (ptr2sore.put(q.dst, SORE.singletonNew)!=SORE.singletonNew);
	}
	public void visit(PHI q) { 
	    boolean r = false;
	    for (int i=0; i<q.dst.length; i++)
		if (ptr(c, q.dst[i]))
		    if (merge(c, q.dst[i], q.src[i])) r = true;
	    modified = r;
	}
	public void visit(SET q) { /**/ }
	public void visit(SIGMA q) { 
	    boolean r = false;
	    for (int i=0; i<q.src.length; i++) 
		if (ptr(c, q.src[i]))
		    for (int j=0; j<q.dst[i].length; j++)
			if (move(c, q.dst[i][j], q.src[i])) r = true;
	    modified = r;
	}
	// cannot assign to a pointer: public void visit(ALENGTH q) { }
	// cannot assign to a pointer: public void visit(CJMP q) { }
	// cannot assign to a pointer: public void visit(COMPONENTOF q) { }
	// cannot assign to a pointer: public void visit(FOOTER q) { }
	// cannot assign to a pointer: public void visit(HEADER q) { }
	// cannot assign to a pointer: public void visit(INSTANCEOF q) { }
	// cannot assign to a pointer: public void visit(MONITORENTER q) { }
	// cannot assign to a pointer: public void visit(MONITOREXIT q) { }
	// cannot assign to a pointer: public void visit(NOP q) { }
	// cannot assign to a pointer: public void visit(OPER q) { }
	// cannot assign to a pointer: public void visit(RETURN q) { }
	// cannot assign to a pointer: public void visit(SWITCH q) { }
	// cannot assign to a pointer: public void visit(THROW q) { }
    } // class QuadVisitor

    // auxiliary functions for calculating expresionns over simple field regular expressions
    // sore's pointed to by p are obtained by concatenating field f to sore's pointed to by q 
    boolean get(QuadSSA c, Temp p, HField f, Temp q) { 
	SORE qs = (SORE)ptr2sore.get(q);
	if (qs==null) return false; // this should not happen
	SORE ns = qs.field(f);
	if (ns.equals(ptr2sore.get(p))) return false;
        ptr2sore.put(p, ns);
	return true;
    }
    // sore's pointed to by p are set to sore's pointed to by q
    boolean move(QuadSSA c, Temp p, Temp q) { 
	SORE qs = (SORE)ptr2sore.get(q);
	if (qs==null) return false; // this should not happen
	if (qs.equals(ptr2sore.get(p))) return false;
	ptr2sore.put(p, qs.copy());
	return true;
    }
    // sore's pointed to by p are union of sore's pointed to by q's
    boolean merge(QuadSSA c, Temp p, Temp[] q) {
	SORE ns = new SORE();
	for (int i=0; i<q.length; i++) {
	    SORE qs = (SORE)ptr2sore.get(q[i]);
	    if (qs==null) continue; 
	    ns.union(qs);
	}
	if (ns.equals(ptr2sore.get(p))) return false;
        ptr2sore.put(p, ns);
	return true;
    }
    // returns true if Temp t is of object type
    boolean ptr(QuadSSA c, Temp t) {
	return objectClass(ti.typeMap(c, t));
    }
    boolean objectClass(HClass c) {
	return (!((c==HClass.Byte)||(c==HClass.Char)||(c==HClass.Double)||
		  (c==HClass.Float)||(c==HClass.Int)||(c==HClass.Long)||
		  (c==HClass.Short)||(c==HClass.Boolean)));
    }
    // returns a string representation of this ao
    public String toString() {
	StringBuffer sb = new StringBuffer();
	for (Enumeration e=ptr2sore.keys(); e.hasMoreElements(); ) {
	    Temp p = (Temp)e.nextElement();
	    sb.append(p.name());
	    sb.append("=");
	    sb.append(((SORE)ptr2sore.get(p)).toString());
	    sb.append("\n");
	}   
	return sb.toString();
    }
   
} // class AO
