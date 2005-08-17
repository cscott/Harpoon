// SSIRename.java, created Fri Aug 27 17:58:00 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.Liveness;
import harpoon.Analysis.Place;
import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.Maps.Derivation;
import harpoon.Analysis.Quads.QuadLiveness;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HClass;
import harpoon.IR.Properties.CFGraphable;
import harpoon.IR.LowQuad.DerivationMap;
import harpoon.IR.LowQuad.PCALL;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.TempMap;
import harpoon.Temp.WritableTempMap;
import net.cscott.jutil.Environment;
import net.cscott.jutil.HashEnvironment;
import harpoon.Util.HClassUtil;
import harpoon.Util.Util;
import net.cscott.jutil.WorkSet;
import jpaul.DataStructs.MapSetRelation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * <code>SSIRename</code> is a new, improved, fast SSI-renaming
 * algorithm.  Detailed in the author's thesis.  This Java version
 * is hairy because of the big "efficiency-vs-immutable quads" fight.
 * Also, we have to keep <code>Derivation</code>s and
 * <code>AllocationInformation</code> up-to-date.
 * XXX: DERIVATION INFORMATION FOR PHI/SIGMAS IS CURRENTLY LOST. [CSA]
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SSIRename.java,v 1.7 2005-08-17 23:39:36 salcianu Exp $
 */
public class SSIRename {
    private static final boolean DEBUG = false;
    private static final boolean sort_phisig = false;
    // RETURN TUPLE FOR THE ALGORITHM
    /** New root element (of the SSI-form graph) */
    public final Quad rootQuad;
    /** Map from old no-ssa temps to new ssi temps (incomplete). */
    public final TempMap tempMap;
    /** Map from old no-ssa quads to new ssi quads. */
    public final Map<Quad,Quad> quadMap;
    /** <code>AllocationInformation</code> for the new quads, or
     *  <code>null</code> if no allocation information for the old
     *  quads was supplied. */
    public final AllocationInformation<Quad> allocInfo;
    /** <code>Derivation</code> for the new quads, or <code>null</code>
     *  if no <code>Derivation</code> for the old quads was supplied. */
    public final Derivation<Quad> derivation;

    /** Return a copy of the given quad graph properly converted to
     *  SSI form. */
    public SSIRename(final Code c, final QuadFactory nqf) {
	final SearchState S = new SearchState(c, nqf,
					      c.getDerivation(),
					      c.getAllocationInformation());
	this.rootQuad = S.old2new.get(c.getRootElement());
	this.tempMap = S.varmap;
	this.quadMap = S.old2new;
	this.allocInfo = S.naim;
	this.derivation = S.nderiv;
    }
    
    static class VarMap implements TempMap {
	final TempFactory tf;
	final Environment<Temp,Temp> vm = new HashEnvironment<Temp,Temp>();
	Temp get(Temp t) {
	    if (!vm.containsKey(t)) { vm.put(t, t.clone(tf)); }
	    return vm.get(t);
	}
	Temp inc(Temp t) {
	    if (!vm.containsKey(t)) { vm.put(t, t.clone(tf)); }
	    else { vm.put(t, get(t).clone()); }
	    return get(t);
	}
	// environment interface.
	Stack<Environment.Mark> s = new Stack<Environment.Mark>();
	void beginScope() { s.push(vm.getMark()); }
	void endScope() { vm.undoToMark(s.pop()); }

	public Temp tempMap(Temp t) { return get(t); }
	VarMap(TempFactory tf) { this.tf = tf; }
    }

    static class SearchState {
	/** maps no-ssi quads to ssi quads */
	final Map<Quad,Quad> old2new = new HashMap<Quad,Quad>();
	/** shows where to place phi/sigs */
	final Place place;
	/** QuadFactory to use for new Quads. */
	final QuadFactory nqf;
	/** AllocationInformation for old Quads */
	final AllocationInformation<Quad> oaim;
	/** AllocationInformationMap for new Quads */
	final AllocationInformationMap<Quad> naim;
	/** Derivation for old Quads */
	final Derivation<Quad> oderiv;
	/** Derivation map for new Quads */
	final DerivationMap<Quad> nderiv;
	/** Derivation map for Sigma/Phis */
	final Map<Temp,DerivType> derivmap;
	// algorithm state
	/** maps old variables to new variables */
	final VarMap varmap;
	/** edge stack to unroll dfs recursion. */
	final Stack<Edge> We = new Stack<Edge>();
	/** mark edges as we visit them */
	final Set<Edge> marked = new HashSet<Edge>();

	SearchState(HCode<Quad> c, QuadFactory nqf,
		    Derivation<Quad> oderiv, AllocationInformation<Quad> oaim){
	    this.place = new Place(c, (Liveness) new QuadLiveness(c));
	    this.varmap= new VarMap(nqf.tempFactory());
	    this.nqf   = nqf;
	    this.oaim  = oaim;
	    this.naim  = (oaim==null) ? null : new AllocationInformationMap<Quad>();
	    this.oderiv= oderiv;
	    this.nderiv= (oderiv==null) ? null : new DerivationMap<Quad>();
	    this.derivmap= (oderiv==null) ? null : new HashMap<Temp,DerivType>();
	    setup(c);

	    for (Edge e : c.getRootElement().edgeC()) {
		We.push(e);
	    }
	    
	    while (!We.isEmpty()) {
		Edge e = We.pop();
		if (e==null) { varmap.endScope(); continue; }
		We.push(null); varmap.beginScope();
		search(e);
	    }

	    makePhiSig(c);
	    if (nderiv!=null)
		fixPhiSigDeriv(c);

	    // now link edges
	    for (Iterator<Quad> it=c.getElementsI(); it.hasNext(); ) {
		Quad q = it.next();
		Quad fr = old2new.get(q);
		for (int i = 0; i < q.nextLength(); i++) {
		    Edge e = q.nextEdge(i);
		    Quad to = old2new.get(e.to());
		    Quad.addEdge(fr, e.which_succ(), to, e.which_pred());
		}
		// check for unreachable code.
		for (int i = 0; i < q.prevLength(); i++)
		    assert old2new.get(q.prev(i))!=null : (!DEBUG ? "unreachable predecessor" :
				i+"th predecessor of "+q+" is unreachable");
	    }
	}

	final Map lhs = new HashMap();//lhs of phi/sigma
	final Map rhs = new HashMap();//rhs of phi/sigma
	final Map arg = new HashMap();//uses in a sigma
	final Map sdf = new HashMap();//defs in a sigma
	void setup(HCode c) {
	    for (Iterator it=c.getElementsI(); it.hasNext(); ) {
		Quad q = (Quad) it.next();
		if (q instanceof PHI) {
		    Temp[] l = place.phiNeeded(q);
		    if (sort_phisig) Collections.sort(Arrays.asList(l));
		    Temp[][] r = new Temp[l.length][((PHI)q).arity()];
		    for (int i = 0; i < r.length; i++)
			for (int j = 0; j < r[i].length; j++)
			    r[i][j] = l[i];
		    lhs.put(q, l);
		    rhs.put(q, r);
		} else if (q instanceof SIGMA) {
		    Temp[] r = place.sigNeeded(q);
		    if (sort_phisig) Collections.sort(Arrays.asList(r));
		    Temp[][] l = new Temp[r.length][((SIGMA)q).arity()];
		    for (int i = 0; i < l.length; i++)
			for (int j = 0; j < l[i].length; j++)
			    l[i][j] = r[i];
		    lhs.put(q, l);
		    rhs.put(q, r);
		}
	    }
	}

	final WritableTempMap wtm = new WritableTempMap() {
	    final Map<Temp,Temp> backing = new HashMap();
	    public Temp tempMap(Temp t) { return backing.get(t); }
	    public void associate(Temp o, Temp n) { backing.put(o,n); }
	};
	void search(Edge e) {
	    assert e.from() instanceof PHI ||
			e.from() instanceof SIGMA ||
			e.from() instanceof HEADER;
	    // handle dfs bookkeeping.
	    assert !marked.contains(e);
	    marked.add(e);
	    // setup 'from' state.
	    Quad from = e.from();
	    if (from instanceof HEADER) {
		old2new.put(from, from.rename(nqf, null, null));
	    } else if (from instanceof PHI) {
		Temp[] l = (Temp[]) lhs.get(from);
		for (int i=0; i<l.length; i++)
		    l[i] = varmap.inc(l[i]);
	    } else if (from instanceof SIGMA) {
		Temp[][] l = (Temp[][]) lhs.get(from);
		int j = e.which_succ();
		for (int i=0; i<l.length; i++)
		    l[i][j] = varmap.inc(l[i][j]);
	    }
	    // fixup defs in CALL/PCALL sigma appropriately
	    if (from instanceof CALL || from instanceof PCALL) {
		Temp retval, retex;
		if (from instanceof CALL) {
		    retval = ((CALL) from).retval();
		    retex  = ((CALL) from).retex();
		} else {
		    retval = ((PCALL) from).retval();
		    retex  = ((PCALL) from).retex();
		}
		Temp[] defs = (Temp[]) sdf.get(from);
		if (defs==null) { defs=new Temp[2]; sdf.put(from, defs); }
		// use of inc() below serves to kill any aliases on this path
		if (e.which_succ()==0 && retval!=null)
		    defs[0]=varmap.inc(retval);
		if (e.which_succ()==1)
		    defs[1]=varmap.inc(retex);
	    }
	    // now go on renaming inside basic block until we get to a phi
	    // or sigma.
	    for (Quad q; ; e=q.nextEdge(0)) {
		q = e.to();
		if (q instanceof PHI) { /* update src */
		    Temp[][] r = (Temp[][]) rhs.get(q);
		    int j = e.which_pred();
		    for (int i=0; i < r.length; i++)
			r[i][j] = varmap.get(r[i][j]);
		    break;
		}
		if (q instanceof SIGMA) { /* update src */
		    // rhs of sigma comes before any defs in the sigma.
		    Temp[] r = (Temp[]) rhs.get(q);
		    for (int i=0; i < r.length; i++)
			r[i] = varmap.get(r[i]);
		    if (q instanceof CJMP)
			arg.put(q, varmap.get(((CJMP)q).test()));
		    else if (q instanceof SWITCH)
			arg.put(q, varmap.get(((SWITCH)q).index()));
		    else if (q instanceof TYPESWITCH)
			arg.put(q, varmap.get(((TYPESWITCH)q).index()));
		    else if (q instanceof CALL) {
			CALL Q = (CALL) q;
			Temp[] args = new Temp[Q.paramsLength()];
			for (int i=0; i<Q.paramsLength(); i++)
			    args[i] = varmap.get(Q.params(i));
			arg.put(q, args);
		    } else if (q instanceof PCALL) {
			PCALL Q = (PCALL) q;
			Temp[] args = new Temp[1+Q.paramsLength()];
			for (int i=0; i<Q.paramsLength(); i++)
			    args[i] = varmap.get(Q.params(i));
			args[Q.paramsLength()] = varmap.get(Q.ptr());
			arg.put(q, args);
		    } else throw new Error("Ack!");
		    break;
		}
		/* else, rename src, then rename dst */
		Temp u[] = q.use(), d[] = q.def();
		for (int i=0; i<u.length; i++)
		    wtm.associate(u[i], varmap.get(u[i]));
		for (int i=0; i<d.length; i++)
		    varmap.inc(d[i]);
		Quad nq = q.rename(nqf, varmap, wtm);
		old2new.put(q, nq);
		if (nderiv!=null)
		    transferderivation(nq, q, q.def(), varmap, oderiv);
		if (nq instanceof ANEW || nq instanceof NEW)
		    if (naim!=null) naim.transfer(nq, q, varmap, oaim);
		if (q instanceof FOOTER) break;
	    }
	    for (Iterator<Edge> it=e.to().succC().iterator();it.hasNext();) {
		e = it.next();
		if (!marked.contains(e))
		    We.push(e);
	    }
	}

	void transferderivation(Quad nq, Quad q, Temp[] defs, TempMap varmap, Derivation oderiv) {
	    nderiv.transfer(nq,q,defs,varmap,oderiv);
	    for(int i=0;i<defs.length;i++)
		derivmap.put(varmap.tempMap(defs[i]),new DerivType(nderiv.derivation(nq,varmap.tempMap(defs[i])),nderiv.typeMap(nq,varmap.tempMap(defs[i]))));
	}

	void addType(Quad nq, Temp t,HClass type) {
	    nderiv.putType(nq,t,type);
	    derivmap.put(t, new DerivType(null,type));
	}

	static class DerivType {
	    Derivation.DList deriv;
	    HClass type;
	    DerivType(Derivation.DList deriv,HClass type) {
		this.deriv=deriv;
		this.type=type;
	    }

	    DerivType() {
		this.deriv=null;
		this.type=null;
	    }

	    void merge(DerivType dt) {
		if (dt!=null) {
		    //Merge types
		    if ((type!=null)&&(dt.type!=null))
			type=HClassUtil.commonParent(type,dt.type);
		    else if (type==null)
			type=dt.type;
		    
		    //Merge derivations
		    if (deriv==null)
			this.deriv=dt.deriv;
		}
	    }

	    HClass type() {
		return type;
	    }

	    Derivation.DList deriv() {
		return deriv;
	    }
	}

	static class PhiSigFunction {
	    Temp[] left,right;
	    PhiSigFunction(Temp[] left, Temp[] right) {
		this.left=left;
		this.right=right;
	    }
	    Temp[] left() {
		return left;
	    }
	    Temp[] right() {
		return right;
	    }
	}

	void fixPhiSigDeriv(HCode<Quad> c) {
	    WorkSet functionSet=new WorkSet();
	    MapSetRelation rel=new MapSetRelation();
	    HashSet phisig=new HashSet();
	    //Add Phi's and Sigmas to function set

	    for (Iterator<Quad> it=c.getElementsI(); it.hasNext(); ) {
		Quad q = old2new.get(it.next());
		if (q instanceof PHI) {
		    for(int i=0;i<((PHI)q).numPhis();i++) {
			PhiSigFunction psf=new PhiSigFunction(new Temp[]{((PHI)q).dst(i)},((PHI)q).src(i));
			functionSet.add(psf);
			for(int j=0;j<((PHI)q).arity();j++) {
			    rel.add(((PHI)q).src(i,j),psf);
			}
		    }
		} else if (q instanceof SIGMA) {
		    for(int i=0;i<((SIGMA)q).numSigmas();i++) {
			PhiSigFunction psf=new PhiSigFunction(((SIGMA)q).dst(i),new Temp[]{((SIGMA)q).src(i)});
			functionSet.add(psf);
			rel.add(((SIGMA)q).src(i),psf);
		    }
		}
	    }
	    while(!functionSet.isEmpty()) {
		PhiSigFunction psf=(PhiSigFunction)functionSet.pop();
		Temp[] right=psf.right();
		Temp[] left=psf.left();
		DerivType best=new DerivType();
		for(int i=0;i<right.length;i++) {
		    best.merge((DerivType)derivmap.get(right[i]));
		}
		for(int i=0;i<left.length;i++) {
		    DerivType old=(DerivType)derivmap.get(left[i]);
		    if (old==null) {
			derivmap.put(left[i],best);
			Set values=rel.getValues(left[i]);
			Iterator it=values.iterator();
			while(it.hasNext())
			    functionSet.add(it.next());
		    } else if ((old.type!=best.type)||((best.deriv!=null)&&(old.deriv==null))) {
			derivmap.put(left[i],best);
			Set values=rel.getValues(left[i]);
			Iterator it=values.iterator();
			while(it.hasNext())
			    functionSet.add(it.next());
		    }
		}
	    }
	    
	    for (Iterator<Quad> it=c.getElementsI(); it.hasNext(); ) {
		Quad q = old2new.get(it.next());
		if (q instanceof PHI) {
		    for(int i=0;i<((PHI)q).numPhis();i++) {
			Temp t=((PHI)q).dst(i);
			DerivType dt=(DerivType)derivmap.get(t);
			if (dt.type()!=null)
			    nderiv.putType(q,t,dt.type());
			if (dt.deriv()!=null)
			    nderiv.putDerivation(q,t,dt.deriv());
		    }
		} else if (q instanceof SIGMA) {
		    for(int i=0;i<((SIGMA)q).numSigmas();i++) {
			for(int j=0;j<((SIGMA)q).arity();j++) {
			    Temp t=((SIGMA)q).dst(i,j);
			    DerivType dt=(DerivType)derivmap.get(t);
			    if (dt.type()!=null)
				nderiv.putType(q,t,dt.type());
			    if (dt.deriv()!=null)
				nderiv.putDerivation(q,t,dt.deriv());
			}
		    }
		}
	    }
	}

	void makePhiSig(HCode<Quad> c) {
	    for (Iterator<Quad> it=c.getElementsI(); it.hasNext(); ) {
		Quad q = it.next();
		if (q instanceof PHI) {
		    Temp[] l = (Temp[]) lhs.get(q);
		    Temp[][] r = (Temp[][]) rhs.get(q);
		    Quad nq = new PHI(nqf, q, l, r, ((PHI)q).arity());
		    old2new.put(q, nq);
		} else if (q instanceof SIGMA) {
		    Temp[][] l = (Temp[][]) lhs.get(q);
		    Temp[] r = (Temp[]) rhs.get(q);
		    Quad nq;
		    if (q instanceof CJMP)
			nq = new CJMP(nqf, q, (Temp) arg.get(q), l, r);
		    else if (q instanceof SWITCH)
			nq = new SWITCH(nqf, q, (Temp) arg.get(q),
					((SWITCH)q).keys(), l, r);
		    else if (q instanceof TYPESWITCH)
			nq = new TYPESWITCH(nqf, q, (Temp) arg.get(q),
					    ((TYPESWITCH)q).keys(), l, r,
					    ((TYPESWITCH)q).hasDefault());
		    else if (q instanceof CALL) {
			CALL Q = (CALL) q;
			Temp[] defs = (Temp[]) sdf.get(q);
			nq = new CALL(nqf, Q, Q.method(), (Temp[]) arg.get(q),
				      defs[0], defs[1],
				      Q.isVirtual(), Q.isTailCall(),
				      l, r);
			if (nderiv!=null) {
			    addType(nq,defs[0],oderiv.typeMap(q,((CALL)q).retval()));
			    addType(nq,defs[1],oderiv.typeMap(q,((CALL)q).retex()));
			}
		    } else if (q instanceof PCALL) {
			harpoon.IR.LowQuad.LowQuadFactory lqf =
			    (harpoon.IR.LowQuad.LowQuadFactory) nqf;
			PCALL Q = (PCALL) q;
			Temp[] defs = (Temp[]) sdf.get(q);
			Temp[] argsandptr = (Temp[]) arg.get(q);
			Temp[] args = new Temp[argsandptr.length - 1];
			System.arraycopy(argsandptr,0,args,0,args.length);
			nq = new PCALL(lqf, Q, argsandptr[args.length],
				       args, defs[0], defs[1], l, r,
				       Q.isVirtual(), Q.isTailCall());
			if (nderiv!=null) {
			    if (defs[0]!=null)
				addType(nq,defs[0],oderiv.typeMap(q,((PCALL)q).retval()));
			    if (defs[1]!=null)
				addType(nq,defs[1],oderiv.typeMap(q,((PCALL)q).retex()));
			}
		    } else throw new Error("Ack!");
		    old2new.put(q, nq);
		}
	    }
	}
    }
}
