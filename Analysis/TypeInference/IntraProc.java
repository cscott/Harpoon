// TypeInfo.java, created Thu Sep 10 14:58:21 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.TypeInference;

import harpoon.Analysis.Maps.UseDefMap;
import harpoon.Analysis.UseDef;
import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;
import harpoon.Util.Set;
import harpoon.Util.Util;
import harpoon.Temp.*;
import harpoon.Util.UniqueFIFO;
import harpoon.Util.Worklist;
import java.util.Hashtable;
import java.util.Enumeration;
/**
 * <code>TypeInfo</code> is a simple type analysis tool for quad-ssa form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IntraProc.java,v 1.1.2.2 1998-12-03 04:26:37 marinov Exp $
 */

public class IntraProc {
    InterProc environment;
    HMethod method;
    Code code;
    UseDefMap usedef;
            
    SetHClass[] parameterTypes;
    SetHClass returnType;
    SetHClass exceptionType;
    Set callees;

    public IntraProc(InterProc e, HMethod m) { 
	environment = e;
	method = m;
	code = (Code)m.getCode("quad-ssa");
	usedef = new UseDef();
	parameterTypes = new SetHClass[m.getParameterTypes().length+(m.isStatic()?0:1)];
	for (int i=0; i<parameterTypes.length; i++)
	    parameterTypes[i] = new SetHClass();
	returnType = new SetHClass();
	exceptionType = new SetHClass();
	callees = new Set();
    }

    SetHClass getReturnType() { return returnType.copy(); }
    SetHClass getExceptionType() { return exceptionType.copy(); }

    void addCallee(IntraProc i) { callees.union(i); }
    boolean addParameters(SetHClass[] p) {
	boolean changed = false;
	for (int i=0; i<p.length; i++)
	    if (parameterTypes[i].union(p[i])) changed = true;
	return changed;
    }    

    HMethod[] possibleMethods(HMethod m, SetHClass[] p) {
	if (m.isStatic()) return new HMethod[]{ m };
	Set sm = new Set();
	for (Enumeration e = p[0].elements(); e.hasMoreElements(); ) {
	    HClass c = (HClass)e.nextElement();
	    boolean notDone = true;
	    HMethod nm = null;
	    do {
		try {
		    nm = c.getDeclaredMethod(m.getName(),
					     m.getDescriptor());
			notDone = false;
		} catch (NoSuchMethodError n) { 
		    c = c.getSuperclass();
		}
	    } while (notDone&&(c!=null));
	    if (c!=null) sm.union(nm);
	}
	HMethod[] am = new HMethod[sm.size()];
	sm.copyInto(am);
	return am;
    }

    boolean outputChanged;
    void analyze() {
	//DEBUG
	//if (method.getName().equals("t")) {
	//System.out.print("   analyzing " + method.getDeclaringClass() + " " + method.getName());
	//HClass[] II = method.getParameterTypes();
	//for (int ii=0; ii<II.length; ii++)
	//    System.out.print(" " + II[ii]);
	//System.out.println("");
	//    for (int ii=0; ii<parameterTypes.length; ii++)
	//    System.out.println("i" + ii + "= " + parameterTypes[ii]);
	//    System.out.println("r = " + returnType);
	//    System.out.println("e = " + exceptionType);
	//}
	//DEBUG
	outputChanged = false;
	if (code==null) { // native or unanalyzable method
	    returnType = environment.cone(method.getReturnType());
	    exceptionType = environment.cone(HClass.forClass(Throwable.class));
	    //DEBUG
	    //System.out.println("non-code=" + method.getDeclaringClass() + " " + method.getName());
	    //DEBUG
	} else {
	    Quad ql[] = (Quad[]) code.getElements();
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
	    
	    TypeInfoVisitor tiv = new TypeInfoVisitor(checkcast);
	    while(!worklist.isEmpty()) {
		Quad q = (Quad) worklist.pull();
		tiv.modified = false;
		q.visit(tiv);
		if (tiv.modified) {
		    Temp[] d = q.def();
		    for (int i=0; i<d.length; i++) {
			HCodeElement[] u = usedef.useMap(code, d[i]);
			for (int j=0; j<u.length; j++) {
			    worklist.push((Quad)u[j]); // only pushes unique quads.
			}
		    }
		}
	    }
	}
	//DEBUG
	//if (method.getName().equals("t")) {
	//System.out.println("   finished " + method.getDeclaringClass() + " " + method.getName());
	//    for (int ii=0; ii<parameterTypes.length; ii++)
	//	System.out.println("i" + ii + "=" + parameterTypes[ii]);
	//    System.out.println("r = " + returnType);
	//    System.out.println("e = " + exceptionType);
	//    System.out.println("changed = " + outputChanged);
	    /*
	    for (Enumeration ee=map.keys(); ee.hasMoreElements(); ) {
		Temp t = (Temp)ee.nextElement();
		System.out.println(t + "=" + map.get(t));
	    }
	    */
	//}
	//DEBUG
 
	if (outputChanged)
	    for (Enumeration e=callees.elements(); e.hasMoreElements(); )
		environment.reanalyze((IntraProc)e.nextElement());
    }

    HMethod[] calls() {
	Set r = new Set();
	for (Enumeration e = code.getElementsE(); e.hasMoreElements(); ) {
	    Quad qq = (Quad) e.nextElement();
	    if (!(qq instanceof CALL)) continue;
	    CALL q = (CALL) qq;
	    SetHClass[] paramTypes = new SetHClass[q.params.length];
	    for (int i=0; i<q.params.length; i++) {
		SetHClass s = (SetHClass)map.get(q.params[i]);
		Util.assert(s!=null);
		paramTypes[i] = s;
	    }
	    HMethod[] m;
	    if (q.isSpecial) m = new HMethod[]{ q.method };
	    else m = possibleMethods(q.method, paramTypes);
	    for (int i=0; i<m.length; i++)
		r.union(m[i]);
	}
	HMethod[] ret = new HMethod[r.size()];
	r.copyInto(ret);
	return ret;
    }

    SetHClass getTempType(Temp t) { return (SetHClass)map.get(t); }

    Hashtable map = new Hashtable();
    class TypeInfoVisitor extends QuadVisitor {
	boolean modified = false;
	Hashtable checkcast;
	TypeInfoVisitor(Hashtable checkcast) { this.checkcast = checkcast; }

	public void visit(Quad q) { modified = false; }

	public void visit(AGET q) {
	    SetHClass t = (SetHClass)map.get(q.objectref);
	    if (t==null) { modified=false; return; }
	    modified = merge(q.dst, t.getComponentType());
	    return;
	}
	public void visit(ALENGTH q) {
	    modified = merge(q.dst, HClass.Int);
	}
	public void visit(ANEW q) {
	    modified = merge(q.dst, environment.cone(q.hclass));
	}
	public void visit(ASET q) {
	    /* ??? additional precision could be gained
	     */
	    modified = false;
	}
	public void visit(CALL q) {
	    boolean r = false;
	    SetHClass[] paramTypes = new SetHClass[q.params.length];
	    for (int i=0; i<q.params.length; i++) {
		SetHClass s = (SetHClass)map.get(q.params[i]);
		if (s==null) { modified = false; return; }
		paramTypes[i] = s;
	    }
	    HMethod[] m;
	    if (q.isSpecial) m = new HMethod[]{ q.method };
	    else m = possibleMethods(q.method, paramTypes);
	    for (int i=0; i<m.length; i++) {
		IntraProc p = environment.getIntra(IntraProc.this, m[i], paramTypes);
		boolean r1 = (q.retval==null) ? false : merge(q.retval, p.getReturnType());
		boolean r2 = merge(q.retex, p.getExceptionType());
		r = r || r1 || r2;
	    }
	    modified = r;
	}
	public void visit(COMPONENTOF q) {
	    modified = merge(q.dst, HClass.Boolean);
	}
	public void visit(CONST q) {
	    modified = merge(q.dst, q.type);
	}
	public void visit(GET q) {
	    modified = merge(q.dst, environment.getType(q.field, IntraProc.this));
	}   
	public void visit(SET q) {
	    SetHClass t = (SetHClass)map.get(q.src);
	    if (t==null) { modified = false; return; }
	    environment.mergeType(q.field, t);
	    modified = false;
	}
	public void visit(INSTANCEOF q) {
	    modified = merge(q.dst, HClass.Boolean);
	}
	public void visit(METHODHEADER q) {
	    boolean r = false;
	    for (int i=0; i<q.params.length; i++)
		if (merge(q.params[i], parameterTypes[i])) 
		    r = true;
	    modified = r;
	}
	public void visit(MOVE q) {
	    SetHClass t = (SetHClass)map.get(q.src);
	    if (t==null) { modified = false; return; }
	    modified = merge(q.dst, t);
	}
	public void visit(NEW q) {
	    modified = merge(q.dst, q.hclass);
	}
	public void visit(OPER q) {
	    modified = merge(q.dst, q.evalType());
	}
	public void visit(PHI q) {
	    boolean r = false;
	    for (int i=0; i<q.dst.length; i++)
		for (int j=0; j<q.src[i].length; j++) {
		    if (q.src[i][j]==null) continue;
		    SetHClass t = (SetHClass)map.get(q.src[i][j]);
		    if (t==null) continue;
		    if (merge(q.dst[i], t))
			r = true;
		}
	    modified = r;
	}
	public void visit(SIGMA q) {
	    boolean r = false;
	    for (int i=0; i<q.src.length; i++) {
		if (q.src[i]==null) continue;
		SetHClass t = (SetHClass)map.get(q.src[i]);
		if (t==null) continue;
		for (int j=0; j<q.dst[i].length; j++)
		    if (merge(q.dst[i][j], t))
			r = true;
	    }
	    modified = r;
	}
	/* ??? additional precision could be gained
	   public void visit(CJMP q) {
	   modified = false;
	    }
	*/
	public void visit(RETURN q) {
	    if (q.retval!=null) {
		SetHClass s = (SetHClass)map.get(q.retval);
		if (s!=null)
		    outputChanged = returnType.union(s) || outputChanged;
	    }
	    modified = false;
	}
	public void visit(THROW q) {
	    SetHClass s = (SetHClass)map.get(q.throwable);
	    if (s!=null)
		outputChanged = exceptionType.union(s) || outputChanged;
	    modified = false;
	}
    }

    boolean merge(Temp t, SetHClass newType) {
	SetHClass oldType = (SetHClass)map.get(t);
	if (oldType==null) { 
	    map.put(t, newType.copy()); 
	    return true;
	}
	// if the merged value is different from the old value, update...
	if (oldType.union(newType)) {
	    map.put(t, oldType);
	    return true;
	} else return false;
    }
    
    boolean merge(Temp t, HClass c) { return merge(t, new SetHClass(c)); }

}
