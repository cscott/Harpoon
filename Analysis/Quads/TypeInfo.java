// TypeInfo.java, created Thu Sep 10 14:58:21 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.UseDef;
import harpoon.Analysis.Maps.UseDefMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ALENGTH;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.COMPONENTOF;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.SWITCH;
import harpoon.IR.Quads.THROW;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
import harpoon.Util.Worklist;
import harpoon.Util.WorkSet;
import harpoon.Util.HClassUtil;

import java.util.Vector;
import java.util.HashMap;
import java.util.Map;
/**
 * <code>TypeInfo</code> is a simple type analysis tool for quad-ssi form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TypeInfo.java,v 1.1.2.8 2000-01-17 11:10:13 cananian Exp $
 */

public class TypeInfo implements harpoon.Analysis.Maps.TypeMap {
    UseDefMap usedef;
    boolean verifierBehavior;

    Map map = new HashMap();

    /** Creates a <code>TypeInfo</code> analyzer for the specified
     *  <code>HCode</code>.
     */
    public TypeInfo(harpoon.IR.Quads.QuadSSI hc, UseDefMap usedef) { 
	this.usedef = usedef; 
	this.verifierBehavior=false;
	analyze(hc);
    }

    /** Creates a <code>TypeInfo</code> analyzer for the specified
     *  <code>HCode</code>.
     *  If <code>vBehavior</code> is true, the TypeInfo pass's IQ drops
     *  to the same level as a typical bytecode verifier; i.e. it gathers
     *  no information from instanceof's and such.  Because of the IQ
     *  drop, the verifier sometimes can't determine that the operand
     *  of an AGET is really an array; with <code>vBehaviour</code> true
     *  the <code>TypeInfo</code> handles this case gracefully, rather than
     *  failing an assertion.
     */
    public TypeInfo(harpoon.IR.Quads.QuadSSI hc, UseDefMap usedef, boolean vBehavior) { 
	this.usedef = usedef; 
	this.verifierBehavior=vBehavior;
	analyze(hc);
    }

    /** Creates a <code>TypeInfo</code> analyzer for the specified
     *  <code>HCode</code>. 
     */
    public TypeInfo(harpoon.IR.Quads.QuadSSI hc) { this(hc, new UseDef()); }
    
    public HClass typeMap(HCodeElement hce, Temp t) { 
	return (HClass) map.get(t); 
    }

    private void analyze(harpoon.IR.Quads.QuadSSI hc) {
	Quad ql[] = (Quad[]) hc.getElements();
	
	WorkSet worklist = new WorkSet(); // use as FIFO
	for (int i=0; i<ql.length; i++)
	    worklist.add(ql[i]);

	// hack to handle typecasting:
	//  keep track of booleans defined by instanceof's and acmpeq's.
	Map checkcast = new HashMap();
	if (!verifierBehavior)
	    for (int i=0; i<ql.length; i++)
		if (ql[i] instanceof INSTANCEOF ||
		    ql[i] instanceof OPER)
		    checkcast.put(ql[i].def()[0], ql[i]);
	
	TypeInfoVisitor tiv = new TypeInfoVisitor(hc, checkcast, verifierBehavior);
	while(!worklist.isEmpty()) {
	    Quad q = (Quad) worklist.removeFirst(); // use as FIFO
	    tiv.modified = false;
	    q.accept(tiv);
	    if (tiv.modified) {
		Temp[] d = q.def();
		for (int i=0; i<d.length; i++) {
		    HCodeElement[] u = usedef.useMap(hc, d[i]);
		    for (int j=0; j<u.length; j++) {
			worklist.add((Quad)u[j]); // only pushes unique quads.
		    }
		}
	    }
	}
    }

    class TypeInfoVisitor extends QuadVisitor {
	harpoon.IR.Quads.QuadSSI hc;
	boolean modified = false;
	Map checkcast;
	HClass hclassObj;
	boolean verifierBehavior;
	Linker linker;

	TypeInfoVisitor(harpoon.IR.Quads.QuadSSI hc, Map checkcast, boolean verifierBehavior) { 
	    this.hc = hc; this.checkcast = checkcast;
	    this.verifierBehavior=verifierBehavior;
	    this.linker = hc.getMethod().getDeclaringClass().getLinker();
	    if (verifierBehavior)
		this.hclassObj=linker.forName("java.lang.Object");
	}

	public void visit(Quad q) { modified = false; }

	public void visit(AGET q) {
	    HClass ty = typeMap(q, q.objectref());
	    if ((ty==null)||(ty==HClass.Void)) {modified=false; return; }
	    if (!verifierBehavior) {
		Util.assert(ty.isArray());
		modified = merge(q, q.dst(), toInternal(ty.getComponentType()));
	    }
	    else
		if (ty.isArray())
		    modified = merge(q, q.dst(), toInternal(ty.getComponentType()));
		else
		    modified = merge(q, q.dst(), toInternal(hclassObj));
	    return;
	}
	public void visit(ALENGTH q) {
	    modified = merge(q, q.dst(), HClass.Int);
	}
	public void visit(ANEW q) {
	    modified = merge(q, q.dst(), q.hclass());
	}
	public void visit(CALL q) {
	    boolean r1 = (q.retval()==null) ? false:
		merge(q, q.retval(), toInternal(q.method().getReturnType()));
	    // XXX specify class of exception better.
	    boolean r2 = merge(q,q.retex(),linker.forClass(Throwable.class));
	    modified = r1 || r2;

	    // deal with SIGMA functions in CALL
	    for (int i=0; i<q.numSigmas(); i++) {
		if (q.src(i)==null) continue;
		HClass ty = typeMap(q, q.src(i));
		if (ty==null) continue;
		for (int j=0; j<q.arity(); j++)
		    if (merge(q, q.dst(i,j), ty))
			modified = true;
	    }
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
	    boolean r = false;
	    for (int i=0; i<q.numSigmas(); i++) {
		if (q.src(i)==null) continue;
		HClass ty = typeMap(q, q.src(i));
		if (ty==null) continue;
		for (int j=0; j<q.arity(); j++)
		    if (merge(q, q.dst(i,j), ty))
			r = true;
	    }
	    modified = r;
	}
	/* CJMP should actually somehow split and not merge types,
	   i.e. use the information at branch site to find narrower types? */
	public void visit(CJMP q) {
	    // special case typecasting. (CHECKCAST in bytecode)
	    // special case comparisons against NULL.
	    INSTANCEOF idef = null; // CJMP test is from this INSTANCEOF
	    OPER       odef = null; // CJMP test is from this OPER
	    Quad def = (Quad) checkcast.get(q.test());
	    if (def instanceof INSTANCEOF) idef = (INSTANCEOF)def;
	    if (def instanceof OPER)       odef = (OPER)def;

	    boolean r = false;
	    for (int i=0; i<q.numSigmas(); i++) {
		if (q.src(i)==null) continue;
		HClass ty = typeMap(q, q.src(i));
		if (ty==null) continue;
		for (int j=0; j<q.arity(); j++) {
		    if (j==1) { // sometimes we gain info on true side of cjmp
			if (idef != null && idef.src() == q.src(i)) {
			    // test from INSTANCEOF.  we know class if true.
			    r = merge(q, q.dst(i,j), idef.hclass()) || r;
			    continue;
			}
			if (odef != null && odef.opcode()==Qop.ACMPEQ) {
			    // check to be sure we've got enough info:
			    HClass left = typeMap(odef, odef.operands(0));
			    HClass right= typeMap(odef, odef.operands(1));
			    if (left==null||right==null) continue;
			    // ACMPEQ.  Types are identical if true.
			    if (odef.operands(0) == q.src(i) &&
				right == HClass.Void) {
				r = merge(q, q.dst(i,j), right) || r;
				continue;
			    }
			    if (odef.operands(1) == q.src(i) &&
				left == HClass.Void) {
				r = merge(q, q.dst(i,j), left) || r;
				continue;
			    }
			}
		    }
		    // fall back
		    r = merge(q, q.dst(i,j), ty) || r;
		}
	    }
	    modified = r;
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
	
	// handle object types (possibly arrays)
	HClass merged = HClassUtil.commonParent(oldType, newType);
	// if the merged value is different from the old value, update...
	if (merged==oldType) return false;
	map.put(t, merged);
	return true;
    }
}


