// InterProc.java, created Fri Nov 20 19:21:40 1998 by marinov
// Copyright (C) 1998 Darko Marinov <marinov@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.TypeInference;

import harpoon.IR.Quads.CALL;
import harpoon.Temp.Temp;
import java.util.Hashtable;
import java.util.Enumeration;
import harpoon.Util.Worklist;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.QuadClassHierarchy;

import java.util.Collections;
/**
 * <code>InterProc</code>
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: InterProc.java,v 1.3 2004-02-08 01:54:33 cananian Exp $
 */

public class InterProc implements harpoon.Analysis.Maps.SetTypeMap {
    HCode main;
    boolean analyzed = false;
    Hashtable proc = new Hashtable();
    ClassHierarchy ch = null;
    HCodeFactory hcf;
    Linker linker;

    /** Creates an <code>InterProc</code> analyzer. */
    public InterProc(HCode main, HCodeFactory hcf) {
	this.main = main; this.hcf = hcf;
	this.linker = main.getMethod().getDeclaringClass().getLinker();
    }    
    public InterProc(HCode main, ClassHierarchy ch, HCodeFactory hcf) {
	this.main = main; this.ch = ch; this.hcf = hcf;
	this.linker = main.getMethod().getDeclaringClass().getLinker();
    }

    public SetHClass setTypeMap(HCode c, Temp t) { 
	analyze();
	IntraProc i = (IntraProc) proc.get(c.getMethod()); 
	if (i==null)
	    return i.getTempType(t);
	else
	    return new SetHClass();
    }

    public SetHClass getReturnType(HCode c) {
	analyze();
	IntraProc i = (IntraProc) proc.get(c.getMethod()); 
	if (i==null)
	    return i.getReturnType();
	else
	    return new SetHClass();
    }

    public HMethod[] calls(HMethod m) { 
	analyze();
	return ((IntraProc)proc.get(m)).calls();
    }
    public HMethod[] calls(HMethod m, CALL cs, boolean last) { 
	analyze();
	return ((IntraProc)proc.get(m)).calls(cs, last);
    }

    AuxUniqueFIFO wl;
    public void analyze() {
	if (analyzed) return; else analyzed = true;	
	/* main method, the one from which the analysis starts. */
	HMethod m = main.getMethod();
	/* build class hierarchy of classess reachable from main.
	   used for coning, i.e. finding all children of a given class. */
	if (ch==null) ch = new QuadClassHierarchy(linker, Collections.singleton(m), hcf);
	cc = new ClassCone(ch);
	/* worklist of methods that are to be processed. */
	wl = new AuxUniqueFIFO(16);
	/* put class initializers for reachable classes on the worklist. */
	SetHClass[] ep = new SetHClass[0];
	for (Enumeration e = classInitializers(ch); e.hasMoreElements(); ) {
	    HMethod ci = (HMethod)e.nextElement();
	    getIntra(null, ci, ep);
	}       
	/* ugly hack - add method(s) invoked automatically at jvm start-up??? */
	HMethod ci = linker.forName("java.lang.System").getMethod("initializeSystemClass", new HClass[0]);
	getIntra(null, ci, ep);
	/* put the main method on the worklist. */
	HClass[] c = m.getParameterTypes();
	SetHClass[] p = new SetHClass[c.length];
	for (int i=0; i<c.length; i++)
	    p[i] = cone(c[i]);
        getIntra(null, m, p);
	/* use straightforward worklist algorithm. */
	while (!wl.isEmpty()) {
	    IntraProc i = (IntraProc)wl.pull();
	    i.analyze();
	}
    }
 
    ClassCone cc;
    SetHClass cone(HClass c) { return cc.cone(c); }
    
    void reanalyze(IntraProc i) { wl.push(i, i.depth); }

    IntraProc getIntra(IntraProc c, HMethod m, SetHClass[] p) {
	IntraProc i = (IntraProc)proc.get(m);
	if (i==null) {
	    i = new IntraProc(this, m, hcf); 
	    proc.put(m, i);
	    i.addParameters(p);
	    if (c!=null) i.addCallee(c);
	    reanalyze(i);
	} else if (i.addParameters(p)) {
	    if (c!=null) i.addCallee(c);
	    reanalyze(i);
	}
	return i;
    }

    Hashtable instVar = new Hashtable();
    SetHClass getType(HField f, IntraProc i) {
	FieldType t = (FieldType)instVar.get(f);
	if (t==null) {
	    t = new FieldType();
	    instVar.put(f, t);
	} 
	t.addCallee(i);
	return t.getType();
    }
    void mergeType(HField f, SetHClass s) {
	FieldType t = (FieldType)instVar.get(f);
	if (t==null) {
	    t = new FieldType();
	    instVar.put(f, t);
	}
	if (t.union(s))
	    for (Enumeration e=t.getCallees(); e.hasMoreElements(); )
		reanalyze((IntraProc)e.nextElement());
    }

    Enumeration classInitializers(final ClassHierarchy ch) {
	return new Enumeration() {
	    Enumeration e =
		new net.cscott.jutil.IteratorEnumerator(ch.classes().iterator());
	    HMethod m = null;
	    private void advance() {
		while (e.hasMoreElements()&&(m==null)) {
		    HClass c = (HClass)e.nextElement();
		    m = c.getClassInitializer();
		}
	    }
	    public boolean hasMoreElements() { advance(); return (m!=null); }
	    public Object nextElement() {
		advance();
		if (m!=null) { HMethod n = m; m = null; return n; }
		else return null;
	    }
	};
    }

}
