// ToSSA.java, created Sat Jul  3 01:26:14 1999 by root
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.SSITOSSAMap;
import harpoon.Temp.TempMap;
import harpoon.Temp.Temp;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HANDLER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SWITCH;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.IR.LowQuad.LowQuadFactory;
import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.IR.LowQuad.PCALL;

/**
 * <code>ReHandlerToSSA</code>
 * Converts SSI to SSA.  Should work on LowQuads and Quads. 
 * <b>NOT FOR USE OUTSIDE REHANDLER</b>.  Use SSIToSSA instead.
 *
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: ReHandlerToSSA.java,v 1.1.2.1 2001-09-26 16:15:12 cananian Exp $
 */

final class ReHandlerToSSA {
    TempMap ssitossamap;

    /** <code>ToSSA</code> takes in a TempMap and returns a <code>ToSSA</code>
     *  object.*/
    public ReHandlerToSSA(TempMap ssitossamap) {
	this.ssitossamap=ssitossamap;
    }

    /** Creates a <code>toSSA</code> codeFactory. */
    public static HCodeFactory codeFactory(final HCodeFactory parent) {
	return new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode hc = parent.convert(m);
		try {
		hc = hc.clone(m).hcode();
		} catch (CloneNotSupportedException e) {
		    System.out.println("Error:  clone not supported on class handed to ToSSA");
		}
		if (hc!=null) {
		    (new ReHandlerToSSA(new SSITOSSAMap(hc))).optimize(hc);
		}
		return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	};
    }

    /** This method takes in a HCode and transforms it from SSI to SSA.*/
    public void optimize(final HCode hc) {
	SSAVisitor visitor=new SSAVisitor(ssitossamap);
	Quad[] ql = (Quad[]) hc.getElements();
	for (int i=0; i<ql.length; i++)
	    ql[i].accept(visitor);
    }

    class SSAVisitor extends LowQuadVisitor {
	SSAVisitor(TempMap ssitossamap) {
	    super(false/*non-strict*/);
	    this.ssitossamap=ssitossamap;
	}
	
	public void visit(Quad q) {
	    //Build a new quad and link it in
	    Quad newquad=q.rename(ssitossamap,ssitossamap);
	    Quad []prev=q.prev();
	    Quad []next=q.next();
	    for(int i=0;i<prev.length;i++) {
		Quad.addEdge(prev[i],q.prevEdge(i).which_succ(),newquad,i);
	    }
      	    for(int j=0;j<next.length;j++) {
		Quad.addEdge(newquad,j,next[j],q.nextEdge(j).which_pred());
	    }
	}
	
	public void visit(HEADER q) {
	    //Do nothing
	}

	public void visit(FOOTER q) {
	    //Do nothing
	}

	public void visit(CJMP q) {
	    int arity=q.arity();
	    Temp[] nothing=new Temp[0];
	    CJMP newsigma=new CJMP(q.getFactory(), q, ssitossamap.tempMap(q.test()), nothing);
	    Quad []prev=q.prev();
	    Quad []next=q.next();
	    for(int i=0;i<prev.length;i++) {
		Quad.addEdge(prev[i],q.prevEdge(i).which_succ(),newsigma,i);
	    }
      	    for(int j=0;j<next.length;j++) {
		Quad.addEdge(newsigma,j,next[j],q.nextEdge(j).which_pred());
	    }
	}

	public void visit(CALL q) {
	    int arity=q.arity();
	    Temp[] nparams=new Temp[q.paramsLength()];
	    for (int i=0; i<nparams.length; i++)
		nparams[i] = ssitossamap.tempMap(q.params(i));
	    CALL newcall=new CALL(q.getFactory(), q, q.method(),
				  nparams, ssitossamap.tempMap(q.retval()),
				  ssitossamap.tempMap(q.retex()),
				  q.isVirtual(), q.isTailCall(), new Temp[0]);
	    Quad []prev=q.prev();
	    Quad []next=q.next();
	    for(int i=0;i<prev.length;i++) {
		Quad.addEdge(prev[i],q.prevEdge(i).which_succ(),newcall,i);
	    }
      	    for(int j=0;j<next.length;j++) {
		Quad.addEdge(newcall,j,next[j],q.nextEdge(j).which_pred());
	    }
	}

	public void visit(PCALL q) {
	    int arity=q.arity();
	    Temp[] nparams=new Temp[q.paramsLength()];
	    for (int i=0; i<nparams.length; i++)
		nparams[i] = ssitossamap.tempMap(q.params(i));
	    PCALL newcall=new PCALL((LowQuadFactory)q.getFactory(), q, q.ptr(),
				  nparams, ssitossamap.tempMap(q.retval()),
				  ssitossamap.tempMap(q.retex()),
				  new Temp[0], q.isVirtual(), q.isTailCall());
	    Quad []prev=q.prev();
	    Quad []next=q.next();
	    for(int i=0;i<prev.length;i++) {
		Quad.addEdge(prev[i],q.prevEdge(i).which_succ(),newcall,i);
	    }
      	    for(int j=0;j<next.length;j++) {
		Quad.addEdge(newcall,j,next[j],q.nextEdge(j).which_pred());
	    }
	}

	public void visit(SWITCH q) {
	    int arity=q.arity();
	    Temp[] nothing=new Temp[0];
	    SWITCH newsigma=new SWITCH(q.getFactory(), q,ssitossamap.tempMap(q.index()), q.keys(),nothing);
	    Quad []prev=q.prev();
	    Quad []next=q.next();
	    for(int i=0;i<prev.length;i++) {
		Quad.addEdge(prev[i],q.prevEdge(i).which_succ(),newsigma,i);
	    }
      	    for(int j=0;j<next.length;j++) {
		Quad.addEdge(newsigma,j,next[j],q.nextEdge(j).which_pred());
	    }
	}

	public void visit(TYPESWITCH q) {
	    int arity=q.arity();
	    Temp[] nothing=new Temp[0];
	    TYPESWITCH newsigma=new TYPESWITCH(q.getFactory(), q,ssitossamap.tempMap(q.index()), q.keys(),nothing,q.hasDefault());
	    Quad []prev=q.prev();
	    Quad []next=q.next();
	    for(int i=0;i<prev.length;i++) {
		Quad.addEdge(prev[i],q.prevEdge(i).which_succ(),newsigma,i);
	    }
      	    for(int j=0;j<next.length;j++) {
		Quad.addEdge(newsigma,j,next[j],q.nextEdge(j).which_pred());
	    }
	}

	public void visit(PHI q) {
	    //create list of phi temps
	    int numberofphis=q.numPhis();
	    int numberofssa=0;
	    for (int i=0;i<numberofphis;i++) {
		Temp check=ssitossamap.tempMap(q.src(i,0));
		for (int j=1;j<q.arity();j++) {
		    if (ssitossamap.tempMap(q.src(i,j))!=check) {
			numberofssa++;
			break;
		    }
		}
	    }
	    Temp[] dst=new Temp[numberofssa];
	    Temp[][] src=new Temp[numberofssa][q.arity()];
	    numberofssa=0;
	    for (int i=0;i<numberofphis;i++) {
		Temp check=ssitossamap.tempMap(q.src(i,0));
		for (int j=1;j<q.arity();j++) {
		    if (ssitossamap.tempMap(q.src(i,j))!=check) {
			dst[numberofssa]=q.dst(i);
			for (int k=0;k<q.arity();k++) {
			    src[numberofssa][k]=ssitossamap.tempMap(q.src(i,k));
			}
			numberofssa++;
			break;
		    }
		}
	    }
	    PHI newphi=new PHI(q.getFactory(),q,dst,src,q.arity());
	    Quad []prev=q.prev();
	    Quad []next=q.next();
	    for(int i=0;i<prev.length;i++) {
		Quad.addEdge(prev[i],q.prevEdge(i).which_succ(),newphi,i);
	    }
      	    for(int j=0;j<next.length;j++) {
		Quad.addEdge(newphi,j,next[j],q.nextEdge(j).which_pred());
	    }
	}
	TempMap ssitossamap;
    }
}






