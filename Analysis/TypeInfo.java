// TypeInfo.java, created Thu Sep 10 14:58:21 1998 by cananian
package harpoon.Analysis;

import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;
import harpoon.Temp.*;
import harpoon.Util.Util;
import harpoon.Util.UniqueStack;

import java.util.Vector;
import java.util.Hashtable;
/**
 * <code>TypeInfo</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TypeInfo.java,v 1.2 1998-09-10 23:19:53 cananian Exp $
 */

public class TypeInfo implements TypeMap {
    UseDef usedef = new UseDef();
    
    Hashtable map = new Hashtable();

    /** Creates a <code>TypeInfo</code>. */
    public TypeInfo(HMethod method) { analyze(method); }
    
    public HClass typeMap(Temp t) { return (HClass) map.get(t); }

    void analyze(HMethod method) {
	Quad ql[] = (Quad[])harpoon.IR.QuadSSA.Code.code(method).getElements();

	UniqueStack worklist = new UniqueStack();
	for (int i=ql.length-1; i>=0; i--)
	    worklist.push(ql[i]);
	
	while(!worklist.isEmpty()) {
	    Quad q = (Quad) worklist.pop();
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
	else if (q instanceof AGET) return analyze((AGET)q);
	else if (q instanceof ALENGTH) return analyze((ALENGTH)q);
	else if (q instanceof ANEW) return analyze((ANEW)q);
	//else if (q instanceof ASET) return analyze((ASET)q);
	else if (q instanceof CALL) return analyze((CALL)q);
	//else if (q instanceof CJMP) return analyze((CJMP)q);
	else if (q instanceof COMPONENTOF) return analyze((COMPONENTOF)q);
	else if (q instanceof CONST) return analyze((CONST)q);
	//else if (q instanceof FOOTER) return analyze((FOOTER)q);
	else if (q instanceof GET) return analyze((GET)q);
	//else if (q instanceof HEADER) return analyze((HEADER)q);
	else if (q instanceof INSTANCEOF) return analyze((INSTANCEOF)q);
	else if (q instanceof METHODHEADER) return analyze((METHODHEADER)q, m);
	else if (q instanceof MOVE) return analyze((MOVE)q);
	else if (q instanceof NEW) return analyze((NEW)q);
	//else if (q instanceof NOP) return analyze((NOP)q);
	else if (q instanceof OPER) return analyze((OPER)q);
	else if (q instanceof PHI) return analyze((PHI)q);
	//else if (q instanceof RETURN) return analyze((RETURN)q);
	//else if (q instanceof SET) return analyze((SET)q);
	//else if (q instanceof SWITCH) return analyze((SWITCH)q);
	//else if (q instanceof THROW) return analyze((THROW)q);
	return false;
    }

    boolean merge(Temp t, HClass newType) {
	HClass oldType = typeMap(t);
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

    boolean analyze(AGET q) {
	HClass ty = typeMap(q.objectref);
	if (ty==null) return false;
	Util.assert(ty.isArray());
	return merge(q.dst, ty.getComponentType());
    }
    boolean analyze(ALENGTH q) {
	return merge(q.dst, HClass.Int);
    }
    boolean analyze(ANEW q) {
	return merge(q.dst, q.hclass);
    }
    boolean analyze(CALL q) {
	boolean r1 = (q.retval==null) ? false:
	    merge(q.retval, q.method.getReturnType());
	boolean r2 = merge(q.retex, HClass.forClass(Throwable.class)); // XXX
	return r1 || r2;
    }
    boolean analyze(COMPONENTOF q) {
	return merge(q.dst, HClass.Boolean);
    }
    boolean analyze(CONST q) {
	return merge(q.dst, q.type);
    }
    boolean analyze(GET q) {
	return merge(q.dst, q.field.getType());
    }
    boolean analyze(INSTANCEOF q) {
	return merge(q.dst, HClass.Boolean);
    }
    boolean analyze(METHODHEADER q, HMethod m) {
	boolean r = false;
	HClass[] hc = m.getParameterTypes();
	int offset = m.isStatic()?0:1;
	for (int i=offset; i<q.params.length; i++)
	    if (merge(q.params[i], hc[i-offset])) 
		r = true;
	if (!m.isStatic())
	    r = merge(q.params[0], m.getDeclaringClass()) || r;
	return r;
    }
    boolean analyze(MOVE q) {
	HClass ty = typeMap(q.src);
	if (ty==null) return false;
	return merge(q.dst, ty);
    }
    boolean analyze(NEW q) {
	return merge(q.dst, q.hclass);
    }
    boolean analyze(OPER q) {
	return merge(q.dst, q.evalType());
    }
    boolean analyze(PHI q) {
	boolean r = false;
	for (int i=0; i<q.dst.length; i++)
	    for (int j=0; j<q.src[i].length; j++) {
		if (q.src[i][j]==null) continue;
		HClass ty = typeMap(q.src[i][j]);
		if (ty==null) continue;
		if (merge(q.dst[i], ty))
		    r = true;
	    }
	return r;
    }
}
