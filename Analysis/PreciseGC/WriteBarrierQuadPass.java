// WriteBarrierQuadPass.java, created Tue Aug 21 19:42:49 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Runtime1.Data;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.Code;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Tuple;
import harpoon.Util.Util;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.util.HashMap;
import harpoon.Util.Collections.WorkSet;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.NEW;


/**
 * <code>WriteBarrierQuadPass</code> takes code in Quad form and
 * inserts a fake call to a write barrier that is later replaced with
 * a real implementation in <code>WriteBarrierTreePass</code>.
 *
 * When used without <code>WriteBarrierTreePass</code> and in 
 * combination with the mark-and-sweep collector, reports statistics 
 * about the number of times the write-barrier is called.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: WriteBarrierQuadPass.java,v 1.7 2002-06-25 18:18:00 kkz Exp $
 */
public class WriteBarrierQuadPass extends 
    harpoon.Analysis.Transformation.MethodMutator {

    private final ClassHierarchy ch;
    private final boolean optimize;
    private final MRAFactory mraf;
    private final HMethod arraySC;
    private final HMethod fieldSC;
    private final HClass JLT;
    private final HClass JLRF;
    private WriteBarrierStats wbs;

    /** Creates a <code>WriteBarrierQuadPass</code>. 
     *  Write barrier removal performed if optimize is true.
     */
    public WriteBarrierQuadPass(ClassHierarchy ch, 
				HCodeFactory parent, 
				Linker linker,
				String resourceName,
				int optLevel) {
	super(parent);
	this.ch = ch;
	this.JLT = linker.forName("java.lang.Throwable");
	HClass WB = linker.forName("harpoon.Runtime.PreciseGC.WriteBarrier");
	HClass JLO = linker.forName("java.lang.Object");
	this.JLRF = linker.forName("java.lang.reflect.Field");
	this.arraySC = WB.getMethod("asc", new HClass[] 
				    { JLO, HClass.Int, JLO, HClass.Int });
	this.fieldSC = WB.getMethod("fsc", new HClass[] 
				    { JLO, JLRF, JLO, HClass.Int });
	this.optimize = (optLevel != 0);
	System.out.print("MRA analysis time = ");
	// for timing reasons, first force passes to run
	for(Iterator it = ch.callableMethods().iterator(); it.hasNext(); )
	    parent.convert((HMethod)it.next());
	long start_time = System.currentTimeMillis();
	this.mraf = optimize ?
	    new MRAFactory(ch, parent, linker, resourceName, optLevel): null;
	System.out.println(System.currentTimeMillis()-start_time);
    }
    
    protected HCode mutateHCode(HCodeAndMaps input) {
	Code hc = (Code)input.hcode();
	String cls_str = hc.getMethod().getDeclaringClass().getName();
	// we should not have to update derivation information
	assert hc.getDerivation() == null;
	// create a set of SETs and ASETs that can be ignored
	Set ignore = new HashSet();
	if (optimize) {
	    // first, run the MRA an the code
	    MRA mra = mraf.mra(hc);
	    // MRA mra = new MRA(hc, safeSet);
	    for (Iterator it = hc.getElementsI(); it.hasNext(); ) {
		Quad q = (Quad)it.next();
		Tuple mra_before = mra.mra_before(q);
		if (q.kind() == QuadKind.ASET) {
		    // ASETs only know whether the component type is
		    // an object, and not the specific type of the
		    // array, so we can only do an ignore if the
		    // exception set is empty, unless we want to do
		    // more analysis to determine the type of the array.
		    Map m = (Map) mra_before.proj(0);
		    Set s = (Set) mra_before.proj(1);
		    if (m.containsKey(((ASET)q).objectref()) && s.isEmpty()) {
			ignore.add(q);
		    }
		} else if (q.kind() == QuadKind.SET) {
		    Map m = (Map) mra_before.proj(0);
		    Set s = (Set) mra_before.proj(1);
		    if (m.containsKey(((SET)q).objectref())) {
			// add first, remove later if problems
			ignore.add(q);
			HClass type = ((SET)q).field().getType();
			for (Iterator cls = s.iterator(); cls.hasNext(); ) {
			    if (ch.parents((HClass)cls.next()).
				contains(type)) {
				ignore.remove(q);
				break;
			    }
			}
		    }
		}
	    }
	}
	// freeze our results
	ignore = Collections.unmodifiableSet(ignore);
	//hc.print(new java.io.PrintWriter(System.out), null);
	HEADER header = (HEADER) hc.getRootElement();
	FOOTER footer = (FOOTER) header.footer();
	// BIT EXPERIMENT
	// QuadVisitor qv = new WriteBarrierVisitor(footer, Collections.EMPTY_SET);
	QuadVisitor qv = new WriteBarrierVisitor(footer, ignore);
	// we put all elements in array to avoid screwing up the
	// iterator as we mutate the quad graph in-place.
	Quad[] allquads = (Quad[]) hc.getElements();
	for (int i=0; i<allquads.length; i++)
	    allquads[i].accept(qv);
	// yay, done!

	// BIT EXPERIMENT BEGIN
	// MartinVisitor mv = new MartinVisitor(ignore);
	// mv.doAnalysis((Quad)hc.getRootElement());
	// BIT EXPERIMENT END
	return hc;
    }

    /** Code factory for post pass. Emits data needed for gathering
     *  write barrier statistics. This pass needs to be run before
     *  the pass returned by <code>treeCodeFactory</code> to have
     *  any effect.
     */
    /*
    public HCodeFactory statsCodeFactory(Frame f, HCodeFactory hcf,
					 ClassHierarchy ch,
					 PrintStream out) {
	this.wbs = new WriteBarrierStats(f, hcf, ch, arraySC, fieldSC, out);
	return wbs.codeFactory();
    }
    */
    /** <code>Data</code> for gathering statistics on write barriers.
     *  Needs the results of the pass returned by
     *  <code>statsCodeFactory</code>.
     */
    /*
    public Data getData(HClass hc, Frame f) {
	// must have run statsCodeFactory first
	assert wbs != null;
	return new WriteBarrierData(hc, f, wbs.getCount());
    }
    */

    // BIT EXPERIMENT
    private class MartinVisitor {
	private final Set ignore;
	private HashMap map;

	MartinVisitor(Set ignore) {
	    this.ignore=ignore;
	    this.map=new HashMap();
	}
	
	public void doAnalysis(Quad header) {
	    WorkSet quadstolookat=new WorkSet();
	    WorkSet newstatements=new WorkSet();
	    quadstolookat.add(header);
	    while(!quadstolookat.isEmpty()) {
		Quad q=(Quad)quadstolookat.pull();
		if (isremovedSet(q)) {
		    if (!map.containsKey(q)) {
			WorkSet ws=new WorkSet();
			ws.add(q);
			map.put(q,ws);
			for(int i=0;i<q.nextLength();i++)
			    quadstolookat.add(q.next(i));
		    }
		} else if (mightCauseNewObject(q)) {
		    if (!map.containsKey(q)) {
			WorkSet ws=new WorkSet();
			map.put(q,ws);
			for(int i=0;i<q.nextLength();i++)
			    quadstolookat.add(q.next(i));
			newstatements.add(q);
		    }
		} else {
		    if (!map.containsKey(q)) {
			WorkSet ws=new WorkSet();
			for(int i=0;i<q.prevLength();i++)
			    if (map.containsKey(q.prev(i))) {
				WorkSet prevset=(WorkSet)map.get(q.prev(i));
				ws.addAll(prevset);
			    }
			map.put(q,ws);
			for(int i=0;i<q.nextLength();i++)
			    quadstolookat.add(q.next(i));
		    } else {
			WorkSet ws=new WorkSet();
			for(int i=0;i<q.prevLength();i++)
			    if (map.containsKey(q.prev(i))) {
				WorkSet prevset=(WorkSet)map.get(q.prev(i));
				ws.addAll(prevset);
			    }
			WorkSet oldset=(WorkSet) map.get(q);
			if (ws.size()>oldset.size()) {
			    map.put(q,ws);
			    for(int i=0;i<q.nextLength();i++)
				quadstolookat.add(q.next(i));
			}
		    }
		}
	    }
	    //cycle through each newstatements
	    WorkSet assigns=new WorkSet();
	    while(!newstatements.isEmpty()) {
		Quad q=(Quad)newstatements.pop();
		for(int i=0;i<q.prevLength();i++)
		    if (map.containsKey(q.prev(i))) {
			WorkSet prevset=(WorkSet)map.get(q.prev(i));
			assigns.addAll(prevset);
		    } else System.out.println("ERROR: "+q+" "+i+" "+q.prev(i)+ " in hacked analysis");
	    }
	    while(!assigns.isEmpty()) {
		Quad q=(Quad)assigns.pop();
		System.out.println("Adding overhead to: "+q);
		if (q instanceof SET) {
		    SET s=(SET)q;
		    Temp t=new Temp(q.getFactory().tempFactory());
		    GET g=new GET(q.getFactory(), q, t, s.field(), s.objectref());
		    SET ns=new SET(q.getFactory(), q, s.field(), s.objectref(),t);
		    Quad.addEdge(ns,0,s.next(0),s.nextEdge(0).which_pred());
		    Quad.addEdge(g,0,ns,0);
		    Quad.addEdge(s,0,g,0);
		} else if (q instanceof ASET) {
		    ASET s=(ASET)q;
		    Temp t=new Temp(q.getFactory().tempFactory());
		    AGET ag=new AGET(q.getFactory(), q, t, s.objectref(),s.index(),s.type());
		    ASET ans=new ASET(q.getFactory(), q, s.objectref(), s.index(), t, s.type());

		    Quad.addEdge(ans,0,s.next(0),s.nextEdge(0).which_pred());
		    Quad.addEdge(ag,0,ans,0);
		    Quad.addEdge(s,0,ag,0);
		} else {
		    System.out.println("ERROR: "+q+" in hacked analysis");
		}
	    }
	}

	public boolean isremovedSet(Quad q) {
	    if (q instanceof ASET) {
		ASET as = (ASET)q;
		return (!as.type().isPrimitive() && ignore.contains(as));
	    } else if (q instanceof SET) {
		SET s = (SET)q;
		return (!s.isStatic() && !s.field().getType().isPrimitive() &&
			ignore.contains(s));
	    } else return false;
	}
	public boolean mightCauseNewObject(Quad q) {
	    return ((q instanceof NEW)||
		(q instanceof THROW)||
		(q instanceof ANEW)||
		(q instanceof RETURN));
	}

    }


    private class WriteBarrierVisitor extends QuadVisitor {
	private FOOTER footer;
	private final Set ignore;

	/** Creates a <code>WriteBarrierVisitor</code>. */
	WriteBarrierVisitor(FOOTER footer, Set ignore) {
	    this.footer = footer;
	    this.ignore = ignore;
	}

	public void visit(Quad q) { /* do nothing */ }
	public void visit(ASET q) {
	    // we are interested only in arrays containing pointers
	    // we can ignore ASETs where the array is the mra object
	    if (!q.type().isPrimitive() && !ignore.contains(q)) {
		QuadFactory qf = (QuadFactory)q.getFactory();
		TempFactory tf = qf.tempFactory();
		// create Temps
		Temp retex = new Temp(tf, "wbex");
		Temp id = new Temp(tf, "wbid");
		// create needed Quads
		CONST idc = new CONST(qf, q, id, new Integer(0), HClass.Int);
		CALL call = new CALL(qf, q, arraySC,
				     new Temp[] { q.objectref(), q.index(), 
						  q.src(), id }, 
				     null, retex, false, false, new Temp[0]);
		THROW thr = new THROW(qf, q, retex);
		// add CONST and CALL before ASET
		splice(idc, q.prevEdge(0));
		splice(call, q.prevEdge(0));
		// add THROW after CALL
		Quad.addEdge(call, 1, thr, 0);
		footer = footer.attach(thr, 0);
	    }
	}
	public void visit(SET q) {
	    // we are interested only in non-static fields containing pointers
	    // we can ignore SETs where the object is the mra object
	    if (!q.isStatic() && !q.field().getType().isPrimitive() &&
		!ignore.contains(q)) {
		QuadFactory qf = (QuadFactory)q.getFactory();
		TempFactory tf = qf.tempFactory();
		// create needed Temps
		Temp field = new Temp(tf, "wbfield");
		Temp retex = new Temp(tf, "wbex");
		Temp id = new Temp(tf, "wbid");
		// create needed Quads
		CONST idc = new CONST(qf, q, id, new Integer(0), HClass.Int);
		CONST fieldc = new CONST(qf, q, field, q.field(), JLRF); 
		CALL call = new CALL(qf, q, fieldSC,
				     new Temp[] { q.objectref(), field, 
						  q.src(), id }, 
				     null, retex, false, false, new Temp[0]);
		THROW thr = new THROW(qf, q, retex);
		// add CONSTs and CALL before SET
		splice(idc, q.prevEdge(0));
		splice(fieldc, q.prevEdge(0));
		splice(call, q.prevEdge(0));
		// add THROW after CALL
		Quad.addEdge(call, 1, thr, 0);
		footer = footer.attach(thr, 0);
	    }
	}
	/** inserts the given Quad on the given Edge */
	private void splice(Quad q, Edge e) {
	    Quad.addEdge((Quad)e.from(), e.which_succ(), q, 0);
	    Quad.addEdge(q, 0, (Quad)e.to(), e.which_pred());
	}
    }
}
