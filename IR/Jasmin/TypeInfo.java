// TypeInfo.java, created Thu Sep 10 14:58:21 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Jasmin;

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
 * <code>TypeInfo</code> is a simple type analysis tool for quad-ssi form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TypeInfo.java,v 1.1.2.5 1999-09-19 16:17:29 cananian Exp $
 */

public class TypeInfo implements harpoon.Analysis.Maps.TypeMap {
    UseDefMap usedef;
    
    Hashtable map = new Hashtable();

    protected TypeInfo() { } 
    
    /** Creates a <code>TypeInfo</code> analyzer. */
    public TypeInfo(HCode hc, UseDefMap usedef) { 
	this.usedef = usedef; 
	analyze((harpoon.IR.Quads.QuadWithTry)hc);
    }

    /** Creates a <code>TypeInfo</code> analyzer. */
    public TypeInfo(HCode hc) { this(hc, new UseDef()); }
    
    public HClass typeMap(HCodeElement hc, Temp t) { 
	return (HClass) map.get(t); 
    }

    void analyze(harpoon.IR.Quads.QuadWithTry hc) {
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
	    q.accept(tiv);
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
	harpoon.IR.Quads.QuadWithTry hc;
	boolean modified = false;
	Hashtable checkcast;
	TypeInfoVisitor(harpoon.IR.Quads.QuadWithTry hc, Hashtable checkcast) 
	{ this.hc = hc; this.checkcast = checkcast; }

	public void visit(Quad q) { modified = false; }

	public void visit(AGET q) {
	    HClass ty = typeMap(q, q.objectref());
	    if (ty==null) {modified=false; return; }
	    Util.assert(ty.isArray());
	    modified = merge(q, q.dst(), toInternal(ty.getComponentType()));
	    return;
	}
	public void visit(ALENGTH q) {
	    modified = merge(q, q.dst(), HClass.Int);
	}
	public void visit(ANEW q) {
	    modified = merge(q, q.dst(), q.hclass());
	}
	public void visit(CALL q) {
	    Util.assert(q.arity()==1, "Only accepts arity 1 call statements!");
	    modified = (q.retval()==null) ? false:
		merge(q, q.retval(), toInternal(q.method().getReturnType()));
	    // XXX specify class of exception better.
	    //boolean r2 = merge(q,q.retex(),HClass.forClass(Throwable.class));
	    //modified = r1 || r2;
	}
	public void visit(COMPONENTOF q) {
	    modified = merge(q, q.dst(), toInternal(HClass.Boolean));
	}
	public void visit(CONST q) {
	    modified = merge(q, q.dst(), toInternal(q.type()));
	}
	public void visit(GET q) {
	    modified = merge(q, q.dst(), toInternal(q.field().getType()));
	}

	public void visit(HANDLER q) {
	    if (q.caughtException()!=null)
		modified = merge(q, q.exceptionTemp(), q.caughtException());
	    else
		modified = merge(q, q.exceptionTemp(), HClass.forClass(Throwable.class));
	}

	public void visit(INSTANCEOF q) {
	    modified = merge(q, q.dst(), toInternal(HClass.Boolean));
	}
	public void visit(METHOD q) {
	    boolean r = false;
	    HMethod m = hc.getMethod();
	    HClass[] pt = m.getParameterTypes();
	    int offset = m.isStatic()?0:1;
	    for (int i=offset; i<q.paramsLength(); i++)
		if (merge(q, q.params(i), toInternal(pt[i-offset]))) 
		    r = true;
	    if (!m.isStatic())
		r = merge(q, q.params(0), m.getDeclaringClass()) || r;
	    modified = r;
	}
	public void visit(MOVE q) {
	    HClass ty = typeMap(q, q.src());
	    if (ty==null) { modified = false; return; }
	    modified = merge(q, q.dst(), ty);
	}
	public void visit(NEW q) {
	    modified = merge(q, q.dst(), q.hclass());
	}
	public void visit(OPER q) {
	    modified = merge(q, q.dst(), toInternal(q.evalType()));
	}
	public void visit(PHI q) {
	    boolean r = false;
	    for (int i=0; i<q.numPhis(); i++)
		for (int j=0; j<q.arity(); j++) {
		    if (q.src(i,j)==null) continue;
		    HClass ty = typeMap(q, q.src(i,j));
		    if (ty==null) continue;
		    if (merge(q, q.dst(i), ty))
			r = true;
		}
	    modified = r;
	}
	public void visit(SIGMA q) {
	}
	/* CJMP should actually somehow split and not merge types,
	   i.e. use the information at branch site to find narrower types? */
	public void visit(CJMP q) {
	}
    }

    HClass toInternal(HClass c) {
	if (c.equals(HClass.Byte) || c.equals(HClass.Short) ||
           c.equals(HClass.Char) || c.equals(HClass.Boolean))
           return HClass.Int;
       return c;
    }


    boolean merge(HCodeElement hce, Temp t, HClass newType) {
	HClass oldType = typeMap(hce, t);
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
