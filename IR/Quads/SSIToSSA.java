// SSIToSSA.java, created Wed May 31 16:35:30 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.Maps.Derivation;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.Code;
import harpoon.IR.LowQuad.DerivationMap;
import harpoon.IR.LowQuad.LowQuadFactory;
import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.IR.LowQuad.PCALL;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.TempMap;
import net.cscott.jutil.DisjointSet;
import harpoon.Util.Util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/**
 * <code>SSIToSSA</code> renames variables to eliminate sigma functions
 * in an SSI-form codeview, yielding an SSA codeview.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SSIToSSA.java,v 1.7 2004-02-08 01:55:25 cananian Exp $
 */
public class SSIToSSA {
    // Return values for the algorithm:

    /** New root element (of the SSA-form graph) */
    public final HEADER rootQuad;
    /** Map from old ssi temps to new ssa temps. */
    public final TempMap tempMap;
    /** Map from new ssa temps to old ssi temps. */
    public final TempMap revTempMap;
    /** Map from old ssi quads to new ssa quads. */
    public final Map<Quad,Quad> quadMap;
    /** Map from new ssa quads to old ssi quads. */
    public final Map<Quad,Quad> revQuadMap;
    /** <code>AllocationInformation</code> for the new quads, or
     *  <code>null</code> if no allocation information for the old
     *  quads was supplied. */
    public final AllocationInformation<Quad> allocInfo;
    /** <code>Derivation</code> for the new quads, or <code>null</code>
     *  if no <code>Derivation</code> for the old quads was supplied. */
    public final Derivation<Quad> derivation;

    /** Converts the given code (in SSI form) to a graph graph in
     *  SSA form created using the given code factory <code>nqf</code>. */
    public SSIToSSA(final Code c, final QuadFactory nqf) {
	HEADER oldhead = c.getRootElement();
	// first, zip through quads, renaming sigma dests to sources.
	Scanner scanner = new Scanner(c, oldhead.getFactory().tempFactory(),
				      nqf.tempFactory());
	// now clone quads, doing the renaming and clearing sigma funcs.
	Cloner cloner = new Cloner(c, nqf, scanner,
				   c.getDerivation(),
				   c.getAllocationInformation());
	// finally, link all the cloned quads together properly.
	Linker linker = new Linker(c, cloner.old2new);

	// set instance fields to return values.
	this.rootQuad = (HEADER) cloner.old2new.get(oldhead);
	this.tempMap = scanner.unmodifiable();
	this.quadMap = Collections.unmodifiableMap(cloner.old2new);
	this.revTempMap = scanner.reverseTempMap();
	this.revQuadMap = Collections.unmodifiableMap(cloner.new2old);
	this.allocInfo = cloner.naim;
	this.derivation = cloner.nderiv;
    }

    // create map of sigmas dsts to sources.
    // use this to implement cloningtempmap with appropriate merges.
    static class Scanner extends LowQuadVisitor implements TempMap {
	final DisjointSet<Temp> map = new DisjointSet<Temp>();
	final CloningTempMap ctm;
	final Map<Temp,Temp> reverseMap = new HashMap<Temp,Temp>();
	Scanner(Code c, TempFactory oldtf, TempFactory newtf) {
	    super(c instanceof harpoon.IR.LowQuad.Code);// strictness.
	    this.ctm = new CloningTempMap(oldtf, newtf);
	    // visit all elements.
	    for (Iterator<Quad> it=c.getElementsI(); it.hasNext(); )
		it.next().accept(this);
	}
	// implement TempMap via CloningTempMap.
	public Temp tempMap(Temp t) {
	    Temp r = ctm.tempMap(map.find(t));
	    reverseMap.put(r, t);
	    return r;
	}
	public TempMap unmodifiable() {
	    final TempMap uctm = ctm.unmodifiable();
	    return new TempMap() {
		public Temp tempMap(Temp t) {
		    return uctm.tempMap(map.find(t));
		}
	    };
	}
	public TempMap reverseTempMap() {
	    return new TempMap() {
		public Temp tempMap(Temp t) {
		    return reverseMap.get(t);
		}
	    };
	}

	// implement visitor.
	public void visit(Quad q) { /* do nothing. */ }
	public void visit(SIGMA q) {
	    final int numSigmas = q.numSigmas();
	    final int arity = q.arity();
	    for (int i=0; i<numSigmas; i++)
		for (int j=0; j<arity; j++)
		    map.union(q.dst(i, j), q.src(i));
	}
    }
    // create map of old quads to new quads.
    static class Cloner extends LowQuadVisitor {
	/** Maps SSI quads to SSA quads. */
	final Map<Quad,Quad> old2new = new HashMap<Quad,Quad>();
	/** Maps SSA quads to SSI quads. */
	final Map<Quad,Quad> new2old = new HashMap<Quad,Quad>();
	/** Maps old temps to new temps. */
	final TempMap tm;
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

	Cloner(Code c, QuadFactory nqf, TempMap tm,
	       Derivation<Quad> oderiv, AllocationInformation<Quad> oaim) {
	    super(c instanceof harpoon.IR.LowQuad.Code);// strictness.
	    // setup cloner fields.
	    this.nqf = nqf; this.tm = tm;
	    this.oaim  = oaim;
	    this.naim  = (oaim==null) ? null : new AllocationInformationMap();
	    this.oderiv= oderiv;
	    this.nderiv= (oderiv==null) ? null : new DerivationMap();
	    // visit all elements.
	    for (Iterator<Quad> it=c.getElementsI(); it.hasNext(); )
		it.next().accept(this);
	}
	private void record(Quad oldq, Quad newq, boolean transferDeriv) {
	    old2new.put(oldq, newq);
	    new2old.put(newq, oldq);
	    if (transferDeriv && nderiv!=null)
		nderiv.transfer(newq, oldq, oldq.def(), tm, oderiv);
	    if (newq instanceof ANEW || newq instanceof NEW)
		if (naim!=null) naim.transfer(newq, oldq, tm, oaim);
	}
	public void visit(Quad q) {
	    Quad nq = q.rename(nqf, tm, tm);
	    record(q, nq, true);
	}
	// instances of sigma functions.
	public void visit(SIGMA q) {
	    // should be caught by specific case.
	    assert false : ("unknown sigma function: "+q); 
	}
	public void visit(CALL q) {
	    // create CALL with no sigma functions.
	    CALL nq = new CALL(nqf, q, q.method(), map(q.params()),
			       map(q.retval()), map(q.retex()),
			       q.isVirtual(), q.isTailCall(), new Temp[0]);
	    record(q, nq, false);
	    if (nderiv!=null) {
		Temp[] odefs = (q.retval()!=null) ?
		    new Temp[] { q.retval(), q.retex() } :
		    new Temp[] { q.retex() };
		nderiv.transfer(nq, q, odefs, tm, oderiv);
	    }
	}
	public void visit(CJMP q) {
	    CJMP nq = new CJMP(nqf, q, map(q.test()), new Temp[0]);
	    record(q, nq, false);
	}
	public void visit(PCALL q) {
	    PCALL nq = new PCALL((LowQuadFactory)nqf, q,
				 map(q.ptr()), map(q.params()),
				 map(q.retval()), map(q.retex()),
				 new Temp[0], q.isVirtual(), q.isTailCall());
	    record(q, nq, false);
	    if (nderiv!=null) {
		Temp[] odefs = (q.retval()!=null) ?
		    new Temp[] { q.retval(), q.retex() } :
		    new Temp[] { q.retex() };
		nderiv.transfer(nq, q, odefs, tm, oderiv);
	    }
	}
	public void visit(SWITCH q) {
	    SWITCH nq = new SWITCH(nqf, q, map(q.index()), q.keys(),
				   new Temp[0]);
	    record(q, nq, false);
	}
	public void visit(TYPESWITCH q) {
	    TYPESWITCH nq = new TYPESWITCH(nqf, q, map(q.index()), q.keys(),
					   new Temp[0], q.hasDefault());
	    record(q, nq, false);
	}
	// convenience mapping functions.
	private Temp map(Temp t) { return t==null?null:tm.tempMap(t); }
	private Temp[] map(Temp[] ta) {
	    Temp[] result = new Temp[ta.length];
	    for (int i=0; i<ta.length; i++)
		result[i] = map(ta[i]);
	    return result;
	}
    }
    // link quads together.
    static class Linker {
	Linker(Code c, Map<Quad,Quad> old2new) {
	    // visit all elements, linking together.
	    for (Iterator<Quad> it=c.getElementsI(); it.hasNext(); ) {
		Quad q = it.next();
		int n = q.nextLength();
		for (int i=0; i<n; i++) {
		    Edge e = q.nextEdge(i);
		    Quad nfrm = old2new.get(e.from());
		    Quad nto  = old2new.get(e.to());
		    Quad.addEdge(nfrm, e.which_succ(), nto, e.which_pred());
		}
	    }
	}
    }
}
