// TypeInfo.java, created Thu Sep 10 14:58:21 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.QuadSSA;

import harpoon.Analysis.UseDef;
import harpoon.Analysis.Maps.UseDefMap;
import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import harpoon.Temp.*;
import harpoon.Util.Util;
import harpoon.Util.UniqueFIFO;
import harpoon.Util.Worklist;
import harpoon.Util.HClassUtil;

import java.util.Vector;
import java.util.Hashtable;
/**
 * <code>TypeInfo</code> is a simple type analysis tool for quad-ssa form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TypeInfo.java,v 1.1.2.3 1998-12-01 12:36:27 cananian Exp $
 */

public class TypeInfo implements harpoon.Analysis.Maps.TypeMap {
    UseDefMap usedef;
    
    Hashtable map = new Hashtable();
    Hashtable analyzed = new Hashtable();

    /** Creates a <code>TypeInfo</code> analyzer. */
    public TypeInfo(UseDefMap usedef) { this.usedef = usedef; }
    /** Creates a <code>TypeInfo</code> analyzer. */
    public TypeInfo() { this(new UseDef()); }
    
    public HClass typeMap(HCode hc, Temp t) { 
	analyze((harpoon.IR.Quads.QuadSSA)hc);
	return (HClass) map.get(t); 
    }

    void analyze(harpoon.IR.Quads.QuadSSA hc) {
	// don't do the same method more than once.
	if (analyzed.containsKey(hc)) return;
	analyzed.put(hc, hc);

	Quad ql[] = (Quad[]) hc.getElements();
	
	Worklist worklist = new UniqueFIFO();
	for (int i=0; i<ql.length; i++)
	    worklist.push(ql[i]);

	// hack to handle typecasting:
	//  keep track of booleans defined by instanceof's and acmpeq's.
	Hashtable checkcast = new Hashtable();
	for (int i=0; i<ql.length; i++)
	    if (ql[i] instanceof INSTANCEOF ||
		ql[i] instanceof OPER)
		checkcast.put(ql[i].def()[0], ql[i]);
	
	TypeInfoVisitor tiv = new TypeInfoVisitor(hc, checkcast);
	while(!worklist.isEmpty()) {
	    Quad q = (Quad) worklist.pull();
	    tiv.modified = false;
	    q.visit(tiv);
	    if (tiv.modified) {
		Temp[] d = q.def();
		for (int i=0; i<d.length; i++) {
		    HCodeElement[] u = usedef.useMap(hc, d[i]);
		    for (int j=0; j<u.length; j++) {
			worklist.push((Quad)u[j]); // only pushes unique quads.
		    }
		}
	    }
	}
    }

    class TypeInfoVisitor extends QuadVisitor {
	harpoon.IR.Quads.QuadSSA hc;
	boolean modified = false;
	Hashtable checkcast;
	TypeInfoVisitor(harpoon.IR.Quads.QuadSSA hc, Hashtable checkcast) 
	{ this.hc = hc; this.checkcast = checkcast; }

	public void visit(Quad q) { modified = false; }

	public void visit(AGET q) {
	    HClass ty = typeMap(hc, q.objectref);
	    if (ty==null) {modified=false; return; }
	    Util.assert(ty.isArray());
	    modified = merge(hc, q.dst, ty.getComponentType());
	    return;
	}
	public void visit(ALENGTH q) {
	    modified = merge(hc, q.dst, HClass.Int);
	}
	public void visit(ANEW q) {
	    modified = merge(hc, q.dst, q.hclass);
	}
	public void visit(CALL q) {
	    boolean r1 = (q.retval==null) ? false:
		merge(hc, q.retval, q.method.getReturnType());
	    // XXX specify class of exception better.
	    boolean r2 = merge(hc, q.retex, HClass.forClass(Throwable.class));
	    modified = r1 || r2;
	}
	public void visit(COMPONENTOF q) {
	    modified = merge(hc, q.dst, HClass.Boolean);
	}
	public void visit(CONST q) {
	    modified = merge(hc, q.dst, q.type);
	}
	public void visit(GET q) {
	    modified = merge(hc, q.dst, q.field.getType());
	}
	public void visit(INSTANCEOF q) {
	    modified = merge(hc, q.dst, HClass.Boolean);
	}
	public void visit(METHODHEADER q) {
	    boolean r = false;
	    HMethod m = hc.getMethod();
	    HClass[] pt = m.getParameterTypes();
	    int offset = m.isStatic()?0:1;
	    for (int i=offset; i<q.params.length; i++)
		if (merge(hc, q.params[i], pt[i-offset])) 
		    r = true;
	    if (!m.isStatic())
		r = merge(hc, q.params[0], m.getDeclaringClass()) || r;
	    modified = r;
	}
	public void visit(MOVE q) {
	    HClass ty = typeMap(hc, q.src);
	    if (ty==null) { modified = false; return; }
	    modified = merge(hc, q.dst, ty);
	}
	public void visit(NEW q) {
	    modified = merge(hc, q.dst, q.hclass);
	}
	public void visit(OPER q) {
	    modified = merge(hc, q.dst, q.evalType());
	}
	public void visit(PHI q) {
	    boolean r = false;
	    for (int i=0; i<q.dst.length; i++)
		for (int j=0; j<q.src[i].length; j++) {
		    if (q.src[i][j]==null) continue;
		    HClass ty = typeMap(hc, q.src[i][j]);
		    if (ty==null) continue;
		    if (merge(hc, q.dst[i], ty))
			r = true;
		}
	    modified = r;
	}
	public void visit(SIGMA q) {
	    boolean r = false;
	    for (int i=0; i<q.src.length; i++) {
		if (q.src[i]==null) continue;
		HClass ty = typeMap(hc, q.src[i]);
		if (ty==null) continue;
		for (int j=0; j<q.dst[i].length; j++)
		    if (merge(hc, q.dst[i][j], ty))
			r = true;
	    }
	    modified = r;
	}
	public void visit(CJMP q) {
	    // special case typecasting. (CHECKCAST in bytecode)
	    // special case comparisons against NULL.
	    INSTANCEOF idef = null; // CJMP test is from this INSTANCEOF
	    OPER       odef = null; // CJMP test is from this OPER
	    Quad def = (Quad) checkcast.get(q.test);
	    if (def instanceof INSTANCEOF) idef = (INSTANCEOF)def;
	    if (def instanceof OPER)       odef = (OPER)def;

	    boolean r = false;
	    for (int i=0; i<q.src.length; i++) {
		if (q.src[i]==null) continue;
		HClass ty = typeMap(hc, q.src[i]);
		if (ty==null) continue;
		for (int j=0; j<q.dst[i].length; j++) {
		    if (j==1) { // sometimes we gain info on true side of cjmp
			if (idef != null && idef.src == q.src[i]) {
			    // test from INSTANCEOF.  we know class if true.
			    r = merge(hc, q.dst[i][j], idef.hclass) || r;
			    continue;
			}
			if (odef != null && odef.opcode==Qop.ACMPEQ) {
			    // check to be sure we've got enough info:
			    HClass left = typeMap(hc, odef.operands[0]);
			    HClass right= typeMap(hc, odef.operands[1]);
			    if (left==null||right==null) continue;
			    // ACMPEQ.  Types are identical if true.
			    if (odef.operands[0] == q.src[i] &&
				right == HClass.Void) {
				r = merge(hc, q.dst[i][j], right) || r;
				continue;
			    }
			    if (odef.operands[1] == q.src[i] &&
				left == HClass.Void) {
				r = merge(hc, q.dst[i][j], left) || r;
				continue;
			    }
			}
		    }
		    // fall back
		    r = merge(hc, q.dst[i][j], ty) || r;
		}
	    }
	    modified = r;
	}
    }

    boolean merge(HCode hc, Temp t, HClass newType) {
	HClass oldType = typeMap(hc, t);
	if (oldType==null) { map.put(t, newType); return true; }
	if (oldType==newType) return false;
	// special case 'Void' HClass, which is used for null constants.
	if (oldType==HClass.Void && newType != HClass.Void) {
	    map.put(t, newType); return true;
	} else if (newType == HClass.Void)
	    return false;
	
	// handle object types (possible arrays)
	int olddims = HClassUtil.dims(oldType);
	int newdims = HClassUtil.dims(newType);
	HClass merged;
	if (olddims == newdims) { // if the dimensions are equal...
	    // find the first common super class of the types.
	    merged = HClassUtil.commonSuper(HClassUtil.baseClass(oldType),
					    HClassUtil.baseClass(newType));
	    // match the array dimensions.
	    merged = HClassUtil.arrayClass(merged, olddims);
	} else { // dimensions not equal.
	    int dims = (olddims<newdims)?olddims:newdims;
	    // make an object array of the smaller dimension.
	    merged = HClassUtil.arrayClass(HClass.forClass(Object.class), 
					   dims);
	}
	// if the merged value is different from the old value, update...
	if (merged==oldType) return false;
	map.put(t, merged);
	return true;
    }
}
