// InstrumentSyncOps.java, created Thu Jul 13  2:18:26 2000 by jwhaley
// Copyright (C) 2000 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package harpoon.Analysis.PointerAnalysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.Edge;
import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.Temp.Temp;
import harpoon.Util.ArrayFactory;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * <code>InstrumentSyncOps</code> instruments synchronization operations for
 * statistics-gathering purposes.
 * 
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: InstrumentSyncOps.java,v 1.4 2002-04-10 03:00:41 cananian Exp $
 */

public class InstrumentSyncOps implements java.io.Serializable {

    static final HMethod new_method;
    static final HMethod monitorenter_method;
    static final HMethod monitorexit_method;

    static {
	Linker linker = Loader.systemLinker;
	HClass instr_class = linker.forName("harpoon.Analysis.Quads.CollectSyncStats");
	new_method = instr_class.getDeclaredMethod("onNew", "(ILjava/lang/Object;I)V");
	monitorenter_method = instr_class.getDeclaredMethod("onMonitorEnter", "(ILjava/lang/Object;I)V");
	monitorexit_method = instr_class.getDeclaredMethod("onMonitorExit", "(ILjava/lang/Object;I)V");
    }

    private PointerAnalysis pa;
    private QuadMap newmap;	    // Map from new quads to id numbers.
    private QuadMap monitorentermap;	// Map from monitorenter quads to id numbers.
    private QuadMap monitorexitmap;	// Map from monitorexit quads to id numbers.
    private HashMap locks[];
    private HashMap ocs[];

    public InstrumentSyncOps(PointerAnalysis pa) {
	this.pa = pa;
	this.newmap = new QuadMap();
	this.monitorentermap = new QuadMap();
	this.monitorexitmap = new QuadMap();
	this.ocs = new HashMap[2];
	this.ocs[0] = new HashMap();
	this.ocs[1] = new HashMap();
	this.locks = new HashMap[2];
	this.locks[0] = new HashMap();
	this.locks[1] = new HashMap();
    }
    
    public void addRoot(MetaMethod mm) {

	System.out.println("Adding analysis info from root method "+mm);

	final ParIntGraph pig = pa.threadInteraction(mm);
	final NodeRepository nr = pa.getNodeRepository();
	final ActionRepository ar = pig.ar;

	// add to the set of inside ocs nodes.
	PANodeVisitor node_visitor = new PANodeVisitor() {
		public void visit(PANode n) {
		    if (!n.inside()) return;
		    HCodeElement hce = nr.node2Code(n);
		    if (hce == null) return;
		    //ProgramLocation l = new ProgramLocation(hce);
		    //ocs.put(l, n);
		    ocs[0].put(hce, n); // don't use ProgramLocation for now.
		}
	    };
	pig.G.I.forAllNodes(node_visitor);
	Iterator it;
	it = pig.G.r.iterator();
	while (it.hasNext()) {
	    PANode n = (PANode)it.next();
	    node_visitor.visit(n);
	}
	it = pig.G.excp.iterator();
	while (it.hasNext()) {
	    PANode n = (PANode)it.next();
	    node_visitor.visit(n);
	}

	ActionVisitor act_visitor = new ActionVisitor() {
		public void visit_ld(PALoad load){
		}
		public void visit_sync(PASync sync){
		    // this sync exists in the action repository, so it operates
		    // on an escaped node, therefore it is necessary.
		    HCodeElement hce = sync.hce;
		    if (hce != null)
			locks[1].put(hce, sync);
		}
	    };
	ar.forAllActions(act_visitor);
	
	// add to the set of necessary sync ops.
	// a sync op is necessary if it is executed in parallel with another
	// thread that has a sync op on the same node.
	ParActionVisitor par_act_visitor = new ParActionVisitor(){
		public void visit_par_ld(PALoad load, PANode nt2){
		}
		public void visit_par_sync(PASync sync, PANode nt2){
		    PANode n = sync.n;
		    PANode nt = sync.nt;
		    // Sync on node n is performed by nt in || with nt2.
		    // If nt2 has syncs on node n, this sync is necessary.
		    Iterator i = ar.syncsOn(n, nt2);
		    if (!i.hasNext()) return;
		    HCodeElement hce = sync.hce;
		    if (hce != null)
			locks[0].put(hce, sync);
		    // Also add all syncs by nt2 on node n as necessary.
		    do {
			PASync sync2 = (PASync)i.next();
			HCodeElement hce2 = sync.hce;
			if (hce2 != null)
			    locks[0].put(hce2, sync2);
		    } while (i.hasNext());
		}
	    };
	ar.forAllParActions(par_act_visitor);

    }
    
    public void calculate() {
    }
    
    public void dumpMaps() {
	System.out.println("OBJECT CREATION SITES:");
	newmap.dump(ocs[0]);
	System.out.println("MONITORENTER:");
	monitorentermap.dump(locks[1]);
	System.out.println("MONITOREXIT:");
	monitorexitmap.dump(locks[1]);
    }
    
    public HCode instrument(HCode hc) {
        // Visit all quads.
	List ql = hc.getElementsL();
	for (int i=0; i<ql.size(); ++i) {
            Quad q = (Quad)ql.get(i);
            int kind = q.kind();
	    Temp dest; Integer id;
	    if ((kind == QuadKind.NEW) || (kind == QuadKind.ANEW)) {
		if (kind == QuadKind.NEW)
		    dest = ((NEW)q).dst();
		else
		    dest = ((ANEW)q).dst();
		// get new id number
		id = newmap.getNewID(q);
		// add instrumentation after instruction
		int val = 0;
		if (ocs[0].get(q) != null) val += 1;
		addInstrumentation(q, dest, new_method, id, new Integer(val));
		// update list position
		ql = hc.getElementsL();
		i += 3;
	    } else if (kind == QuadKind.MONITORENTER) {
		dest = ((MONITORENTER)q).lock();
		// get new id number
		id = monitorentermap.getNewID(q);
		// add instrumentation after instruction
		int val = 0;
		if (locks[0].get(q) != null) val += 1;
		if (locks[1].get(q) != null) val += 2;
		addInstrumentation(q, dest, monitorenter_method, id,
		    new Integer(val));
		// update list position
		ql = hc.getElementsL();
		i += 3;
	    } else if (kind == QuadKind.MONITOREXIT) {
		dest = ((MONITOREXIT)q).lock();
		// get new id number
		id = monitorexitmap.getNewID(q);
		// add instrumentation before instruction
		int val = 0;
		if (locks[0].get(q) != null) val += 1;
		if (locks[1].get(q) != null) val += 2;
		addInstrumentation(q, dest, monitorexit_method, id,
		    new Integer(val));
		// update list position
		ql = hc.getElementsL();
		i += 3;
	    }
	}
	return hc;
    }

    private static void addInstrumentation(Quad q, Temp dest, HMethod method, Integer id, Integer id2) {
        // use same quad factory as existing quad.
        QuadFactory qf = q.getFactory();
	// generate a new temp to hold the id number.
	Temp idTemp = new Temp(dest);
	Temp idTemp2 = new Temp(idTemp);
	// generate const quad.
	CONST co = new CONST(qf, null, idTemp2, id2, HClass.Int);
	// generate const quad.
	CONST co2 = new CONST(qf, null, idTemp, id, HClass.Int);
	// create param array for call.
	Temp[] params = new Temp[3];
	params[0] = idTemp;
	params[1] = dest;
	params[2] = idTemp2;
        // create a new call quad.
        CALL ca = new CALL(qf, null, method, params, null, null, false, false, new Temp[0]);
        // add the quads AFTER the NEW quad.
	assert q.nextLength() == 1;
	Edge ne = q.nextEdge(0);
	Quad.addEdge(q, 0, co, 0);
	Quad.addEdge(co, 0, co2, 0);
	Quad.addEdge(co2, 0, ca, 0);
	Quad.addEdge(ca, 0, (Quad)ne.to(), ne.which_pred());
    }
    
    static class QuadMap {
	
	ArrayList quads;
	
	QuadMap() { quads = new ArrayList(); }
	
	Integer getNewID(Quad q) {
	    quads.add(q);
	    return new Integer(quads.size());
	}
	
	Quad getQuad(Integer i) {
	    return (Quad)quads.get(i.intValue());
	}
	
	void dump(Map m) {
	    int num1 = 0;
	    for (int i=0; i<quads.size(); ++i) {
		Quad q = (Quad)quads.get(i);
		//ProgramLocation l = new ProgramLocation(q);
		if (m != null) {
		    Object o = m.get(q);
		    if (o != null) {
			System.out.print("necessary for: "+o);
			++num1;
		    }
		}
		System.out.println(" id "+(i+1)+": "+new ProgramLocation(q));
	    }
	    System.out.println("Total number necessary: "+num1);
	}
    }
    
    static class ProgramLocation {
	String sourcefile;
	int linenum;
	
	ProgramLocation(HCodeElement hce) {
	    sourcefile = hce.getSourceFile();
	    linenum = hce.getLineNumber();
	}
	
	public boolean equals(ProgramLocation that) {
	    if (!this.sourcefile.equals(that.sourcefile)) return false;
	    if (this.linenum != that.linenum) return false;
	    return true;
	}
	
	public boolean equals(Object o) {
	    if (o instanceof ProgramLocation) return equals((ProgramLocation)o);
	    return false;
	}
	
	public int hashCode() {
	    return sourcefile.hashCode() ^ linenum;
	}
	
	public String toString() {
	    return sourcefile+":"+linenum;
	}
    }
    
    
    /** Returns a <code>HCodeFactory</code> that uses <code>InstrumentSyncOps</code>. */
    public static HCodeFactory codeFactory(final HCodeFactory parent, final InstrumentSyncOps se) {
	return new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode hc = parent.convert(m);
		if (hc!=null) {
		    System.out.println("Instrumenting method "+m);
		    return se.instrument(hc);
		} else
		    return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	};
    }

}
