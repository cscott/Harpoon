// TypeInfo.java, created Thu Sep 10 14:58:21 1998 by cananian
package harpoon.Analysis;

import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;
import harpoon.Temp.*;
import harpoon.Util.Util;
import harpoon.Util.UniqueFIFO;

import java.util.Vector;
import java.util.Hashtable;
/**
 * <code>TypeInfo</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TypeInfo.java,v 1.4 1998-09-11 13:12:50 cananian Exp $
 */

public class TypeInfo implements TypeMap {
    UseDef usedef;
    
    Hashtable map = new Hashtable();
    Hashtable analyzed = new Hashtable();

    /** Creates a <code>TypeInfo</code> analyzer. */
    public TypeInfo(UseDef usedef) { this.usedef = usedef; }
    /** Creates a <code>TypeInfo</code> analyzer. */
    public TypeInfo() { this(new UseDef()); }
    
    public HClass typeMap(HMethod m, Temp t) { 
	analyze(m);  return (HClass) map.get(t); 
    }

    void analyze(HMethod method) {
	// don't do the same method more than once.
	if (analyzed.containsKey(method)) return;
	analyzed.put(method, method);

	Quad ql[] = (Quad[])harpoon.IR.QuadSSA.Code.code(method).getElements();

	UniqueFIFO worklist = new UniqueFIFO();
	for (int i=0; i<ql.length; i++)
	    worklist.push(ql[i]);
	
	while(!worklist.empty()) {
	    Quad q = (Quad) worklist.pull();
	    boolean isChanged = analyze(method, q);
	    if (isChanged) {
		Temp[] d = q.def();
		for (int i=0; i<d.length; i++) {
		    Quad[] u = usedef.useSites(method, d[i]);
		    for (int j=0; j<u.length; j++) {
			worklist.push(u[j]); // only pushes unique elements.
		    }
		}
	    }
	}
    }

    boolean analyze(HMethod m, Quad q) {
	if (false) ;
	else if (q instanceof AGET) return analyze((AGET)q, m);
	else if (q instanceof ALENGTH) return analyze((ALENGTH)q, m);
	else if (q instanceof ANEW) return analyze((ANEW)q, m);
	//else if (q instanceof ASET) return analyze((ASET)q, m);
	else if (q instanceof CALL) return analyze((CALL)q, m);
	//else if (q instanceof CJMP) return analyze((CJMP)q, m);
	else if (q instanceof COMPONENTOF) return analyze((COMPONENTOF)q, m);
	else if (q instanceof CONST) return analyze((CONST)q, m);
	//else if (q instanceof FOOTER) return analyze((FOOTER)q, m);
	else if (q instanceof GET) return analyze((GET)q, m);
	//else if (q instanceof HEADER) return analyze((HEADER)q, m);
	else if (q instanceof INSTANCEOF) return analyze((INSTANCEOF)q, m);
	else if (q instanceof METHODHEADER) return analyze((METHODHEADER)q, m);
	else if (q instanceof MOVE) return analyze((MOVE)q, m);
	else if (q instanceof NEW) return analyze((NEW)q, m);
	//else if (q instanceof NOP) return analyze((NOP)q, m);
	else if (q instanceof OPER) return analyze((OPER)q, m);
	else if (q instanceof PHI) return analyze((PHI)q, m);
	//else if (q instanceof RETURN) return analyze((RETURN)q, m);
	//else if (q instanceof SET) return analyze((SET)q, m);
	//else if (q instanceof SWITCH) return analyze((SWITCH)q, m);
	//else if (q instanceof THROW) return analyze((THROW)q, m);
	return false;
    }

    boolean merge(HMethod m, Temp t, HClass newType) {
	HClass oldType = typeMap(m, t);
	if (oldType==null) { map.put(t, newType); return true; }
	if (oldType==newType) return false;
	// special case 'Void' HClass, which is used for null constants.
	if (oldType==HClass.Void && newType != HClass.Void) {
	    map.put(t, newType); return true;
	} else if (newType == HClass.Void)
	    return false;
	
	// handle object types (possible arrays)
	int olddims = dims(oldType);
	int newdims = dims(newType);
	HClass merged;
	if (olddims == newdims) { // if the dimensions are equal...
	    // find the first common super class of the types.
	    merged = commonSuper(baseClass(oldType),baseClass(newType));
	    // match the array dimensions.
	    merged = arrayClass(merged, olddims);
	} else { // dimensions not equal.
	    int dims = (olddims<newdims)?olddims:newdims;
	    // make an object array of the smaller dimension.
	    merged = arrayClass(HClass.forClass(Object.class), dims);
	}
	// if the merged value is different from the old value, update...
	if (merged==oldType) return false;
	map.put(t, merged);
	return true;
    }

    int dims(HClass hc) {
	int i=0;
	while (hc.isArray()) {
	    hc = hc.getComponentType();
	    i++;
	}
	return i;
    }
    HClass baseClass(HClass hc) {
	while (hc.isArray())
	    hc = hc.getComponentType();
	return hc;
    }
    HClass arrayClass(HClass hc, int dims) {
	StringBuffer sb = new StringBuffer();
	for (int i=0; i<dims; i++)
	    sb.append('[');
	sb.append(hc.getDescriptor());
	return HClass.forDescriptor(sb.toString());
    }

    HClass commonSuper(HClass a, HClass b) {
	HClass[] aI = inheritance(a);
	HClass[] bI = inheritance(b);
	int i=1;
	while (i<aI.length && i<bI.length && aI[i]==bI[i])
	    i++;
	return aI[i-1];
    }

    HClass[] inheritance(HClass hc) {
	Vector v = new Vector();
	while (hc != null) {
	    v.addElement(hc);
	    hc = hc.getSuperclass();
	}
	HClass[] r = new HClass[v.size()];
	for (int i=0; i<r.length; i++) // reverse
	    r[i] = (HClass) v.elementAt(r.length-i-1);
	return r;
    }

    boolean analyze(AGET q, HMethod m) {
	HClass ty = typeMap(m, q.objectref);
	if (ty==null) return false;
	Util.assert(ty.isArray());
	return merge(m, q.dst, ty.getComponentType());
    }
    boolean analyze(ALENGTH q, HMethod m) {
	return merge(m, q.dst, HClass.Int);
    }
    boolean analyze(ANEW q, HMethod m) {
	return merge(m, q.dst, q.hclass);
    }
    boolean analyze(CALL q, HMethod m) {
	boolean r1 = (q.retval==null) ? false:
	    merge(m, q.retval, q.method.getReturnType());
	boolean r2 = merge(m, q.retex, HClass.forClass(Throwable.class)); //XXX
	return r1 || r2;
    }
    boolean analyze(COMPONENTOF q, HMethod m) {
	return merge(m, q.dst, HClass.Boolean);
    }
    boolean analyze(CONST q, HMethod m) {
	return merge(m, q.dst, q.type);
    }
    boolean analyze(GET q, HMethod m) {
	return merge(m, q.dst, q.field.getType());
    }
    boolean analyze(INSTANCEOF q, HMethod m) {
	return merge(m, q.dst, HClass.Boolean);
    }
    boolean analyze(METHODHEADER q, HMethod m) {
	boolean r = false;
	HClass[] hc = m.getParameterTypes();
	int offset = m.isStatic()?0:1;
	for (int i=offset; i<q.params.length; i++)
	    if (merge(m, q.params[i], hc[i-offset])) 
		r = true;
	if (!m.isStatic())
	    r = merge(m, q.params[0], m.getDeclaringClass()) || r;
	return r;
    }
    boolean analyze(MOVE q, HMethod m) {
	HClass ty = typeMap(m, q.src);
	if (ty==null) return false;
	return merge(m, q.dst, ty);
    }
    boolean analyze(NEW q, HMethod m) {
	return merge(m, q.dst, q.hclass);
    }
    boolean analyze(OPER q, HMethod m) {
	return merge(m, q.dst, q.evalType());
    }
    boolean analyze(PHI q, HMethod m) {
	boolean r = false;
	for (int i=0; i<q.dst.length; i++)
	    for (int j=0; j<q.src[i].length; j++) {
		if (q.src[i][j]==null) continue;
		HClass ty = typeMap(m, q.src[i][j]);
		if (ty==null) continue;
		if (merge(m, q.dst[i], ty))
		    r = true;
	    }
	return r;
    }
}
