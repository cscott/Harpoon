// RSSxToNoSSA.java, created Thu Feb 10 13:58:12 2000 by root
// Copyright (C) 2000 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.IR.LowQuad.LowQuadFactory;
import harpoon.IR.LowQuad.PCALL;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import net.cscott.jutil.WorkSet;

import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.Maps.AllocationInformation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;

/**
 * <code>RSSxToNoSSA</code> converts "relaxed-ssx" form into quads without
 * phi or sigma functions.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: RSSxToNoSSA.java,v 1.3 2004-02-08 01:55:25 cananian Exp $
 */
public class RSSxToNoSSA {
    QuadFactory newQF;
    Code code;
    private CloningTempMap ctm;
    private Quad header;
    AllocationInformationMap newai;
    AllocationInformation oldai;
    HashMap quadmap;
    HashMap newtempmap;

    
    /** Creates a <code>RSSxToNoSSA</code>. */
    public RSSxToNoSSA(QuadFactory newQF, Code code) {
        this.newQF=newQF;
	this.code=code;
	ctm=new CloningTempMap(code.qf.tempFactory(),newQF.tempFactory());
	this.oldai=code.getAllocationInformation();
	if (oldai!=null)
	    this.newai=new AllocationInformationMap();
	else
	    this.newai=null;
	this.newtempmap=new HashMap();
	this.header=translate();
    }
    public Quad getQuads() { return header; }

    public AllocationInformation getAllocationInfo() {return newai;}

    public TempMap tempMap() {
	return ctm;
    }

    public Map quadMap() {
	return quadmap;
    }

    public Map newTempMap() {
	return newtempmap;
    }

    private Quad translate() {
	quadmap=new HashMap();
	for (Iterator qiter=code.getElementsI();
	     qiter.hasNext();) {
	    Quad q=(Quad)qiter.next();
	    try {
		Quad qc=(Quad)q.clone(newQF,ctm);
		if ((newai!=null)&&((q instanceof harpoon.IR.Quads.NEW)||
		(q instanceof harpoon.IR.Quads.ANEW))) {
		    newai.transfer(qc,q,ctm, oldai);
		}
		quadmap.put(q, qc);
	    } catch (Exception e) {
		e.printStackTrace();
		System.out.println(((CALL)q).method());
		System.out.println(q);
		System.out.println(newQF);
		System.exit(1);
	    }
	}
	for (Iterator qiter=code.getElementsI();
	     qiter.hasNext();) {
	    Quad q=(Quad)qiter.next();
	    for (int i=0;i<q.nextLength();i++) {
		Quad.addEdge((Quad)quadmap.get(q),i,
			     (Quad)quadmap.get(q.next(i)),q.nextEdge(i).which_pred());
	    }
	}
	Quad newRoot=(Quad)quadmap.get(code.getRootElement());
	WorkSet todo=new WorkSet(),done=new WorkSet();
	todo.push(newRoot);
	Remover v=new Remover(done,newtempmap);
	while (!todo.isEmpty()) {
	    Quad q=(Quad)todo.pop();
	    done.add(q);
	    for(int i=0;i<q.nextLength();i++)
		if (!done.contains(q.next(i)))
		    todo.push(q.next(i));
	    q.accept(v);
	}
	return newRoot;
    }
    static class Remover extends LowQuadVisitor {
	Set done;
	Map newtempmap;
	public Remover(Set done, Map newtempmap) {
	    super(false/*non-strict*/);
	    this.done=done;
	    this.newtempmap=newtempmap;
	}


	private static Edge addAt(Edge e, Quad q) { return addAt(e, 0, q, 0); }
	private static Edge addAt(Edge e,
				  int which_pred, Quad q, int which_succ) {
	    Quad frm = (Quad) e.from(); int frm_succ = e.which_succ();
	    Quad to  = (Quad) e.to();   int to_pred = e.which_pred();
	    Quad.addEdge(frm, frm_succ, q, which_pred);
	    Quad.addEdge(q, which_succ, to, to_pred);
	    return to.prevEdge(to_pred);
	}
	private Edge addMoveAt(Edge e, Quad source, Temp dst, Temp src)
	{
	    MOVE m = new MOVE(source.getFactory(), source, dst, src);
	    done.add(m);
	    return addAt(e, m);
	}
	public void fixsigma(SIGMA q) {
	    for (int i=0;i<q.numSigmas();i++)
		for (int j=0;j<q.arity();j++)
		    // XXX: can there be conflicts in sigma functions?
		    addMoveAt(q.nextEdge(j), q, q.dst(i,j), q.src(i));
	}
	public void fixphi(PHI q) {
	    for (int i=0;i<q.numPhis();i++) {
		Temp Tt = new Temp(q.dst(i));
		newtempmap.put(Tt, q.dst(i));
		addMoveAt(q.nextEdge(0), q, q.dst(i), Tt);
		for (int j=0;j<q.arity();j++)
		    addMoveAt(q.prevEdge(j), q, Tt, q.src(i, j));
	    }
	}
	public void visit(Quad q) {}
	
	public void visit(CALL q) {
	    fixsigma(q);
	    CALL nc= new CALL(q.getFactory(), q, q.method(), q.params(),
			      q.retval(),
			      q.retex(), q.isVirtual(), q.isTailCall(),
			      new Temp[0]);
	    Quad.replace(q, nc);
	    done.add(nc);
	}

	public void visit(PCALL q) {
	    fixsigma(q);
	    PCALL nc= new PCALL((LowQuadFactory)q.getFactory(), q, q.ptr(), 
				q.params(),
				q.retval(), q.retex(), new Temp[0],
				q.isVirtual(), q.isTailCall());
	    Quad.replace(q, nc);
	    done.add(nc);
	}

	public void visit(CJMP q) {
	    fixsigma(q);
	    CJMP nc= new CJMP(q.getFactory(), q,q.test(),
			      new Temp[0]);
	    Quad.replace(q, nc);
	    done.add(nc);
	}

	public void visit(SWITCH q) {
	    fixsigma(q);
	    SWITCH ns= new SWITCH(q.getFactory(), q, q.index(), q.keys(),
				  new Temp[0]);
	    Quad.replace(q, ns);
	    done.add(ns);
	}

	public void visit(TYPESWITCH q) {
	    fixsigma(q);
	    TYPESWITCH nts = new TYPESWITCH(q.getFactory(), q, q.index(),
					    q.keys(), new Temp[0],
					    q.hasDefault());
	    Quad.replace(q, nts);
	    done.add(nts);
	}

	public void visit(LABEL q) {
	    fixphi(q);
	    LABEL np=new LABEL(q.getFactory(),q,q.label(),
			       new Temp[0], q.arity());
	    Quad.replace(q, np);
	    done.add(np);
	}
	public void visit(PHI q) {
	    fixphi(q);
	    PHI np=new PHI(q.getFactory(),q,new Temp[0], q.arity());
	    Quad.replace(q, np);
	    done.add(np);
	}
    }
}

