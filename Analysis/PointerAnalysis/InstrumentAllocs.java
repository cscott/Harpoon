// InstrumentAllocs.java, created Tue Nov  7 14:29:16 2000 by root
// Copyright (C) 2000 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import harpoon.Analysis.Transformation.MethodMutator;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.Temp.TempFactory;
import harpoon.Temp.Temp;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ALENGTH;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import harpoon.Util.Collections.WorkSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <code>InstrumentAllocs</code> adds calls to instrumenting code to
 * each allocation site and, if explicitly requested, to each
 * synchronization instruction.  If call chain sensitivity is
 * requested, instrumentation is added around method calls to.  The
 * produced code prints the instrumentation statistics at the end of
 * the program.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: InstrumentAllocs.java,v 1.4 2002-10-04 19:53:51 salcianu Exp $ */
public class InstrumentAllocs extends MethodMutator
    implements java.io.Serializable {

    private final HMethod main;
    private final Linker linker;
    private final HCodeFactory parenthcf;
    private final AllocationNumbering an;

    private final boolean syncs;
    private final boolean callchains;

    /** Creates a <code>InstrumentAllocs</code>.

	@param parent <code>HCodeFactory</code> that provides the code
	to instrument

	@param main main method of the instrumented program

	@param linker linker for the code to instrument

	@param an <code>AllocationNumbering</code> for the
	allocation/call sites

	@param syncs if true, then the synchronization operations are
	instrumented too.

	@param callchains if true, the instrumented code will record
	the call chains for ecah execution of the instrumented
	instructions. */
    public InstrumentAllocs(HCodeFactory parent, HMethod main,
			    Linker linker, AllocationNumbering an,
			    boolean syncs, boolean callchains) {
        super(parent);
	
	assert
	    parent.getCodeName().equals(QuadNoSSA.codename) :
	    "InstrumentAllocs works only with QuadNoSSA";

	parenthcf = parent;

	this.main   = main;
	this.linker = linker;
	this.an     = an;
	this.syncs      = syncs;
	this.callchains = callchains;

	init_methods();
    }

    private void init_methods() {
	hc_obj = linker.forName("java.lang.Object");

	hm_count_alloc =
	    get_method("harpoon.Runtime.CounterSupport", "count",
		       new HClass[]{HClass.Int});    
	hm_count_sync =
	    get_method("harpoon.Runtime.CounterSupport", "countm",
		       new HClass[]{hc_obj});

	method3 =
	    get_method("harpoon.Runtime.CounterSupport", "label",
		       new HClass[]{hc_obj, HClass.Int});
    
	hm_call_enter = 
	    get_method("harpoon.Runtime.CounterSupport", "callenter",
		       new HClass[]{HClass.Int});
    
	hm_call_exit =
	    get_method("harpoon.Runtime.CounterSupport", "callexit",
		       new HClass[0]);
	
	hm_instr_exit = 
	    get_method("harpoon.Runtime.CounterSupport",
		       "exit", new HClass[0]);
	
	hm_orig_exit = get_method("java.lang.System", "exit", "(I)V");	       
    }

    public HCodeFactory parent() {
	return parenthcf;
    }

    // returns that method of class clsn that is called mn and has
    // arguments of types a_types
    private HMethod get_method(String clsn, String mn, HClass[] atypes) {
	return linker.forName(clsn).getMethod(mn, atypes);
    }

    // like previous method except that the arg types are given as a string
    private HMethod get_method(String clsn, String mn, String atypes) {
	return linker.forName(clsn).getMethod(mn, atypes);
    }


    // handles for important methods
    private HClass hc_obj;

    private HMethod hm_count_alloc;
    private HMethod hm_count_sync;
    // what's this? TODO: find more appropriate name
    private HMethod method3;
    private HMethod hm_call_enter;
    private HMethod hm_call_exit;
    private HMethod hm_instr_exit;
    private HMethod hm_orig_exit;


    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();

	// we avoid instrumenting the instrumentation itself !
	if (hc.getMethod().getDeclaringClass().getName().
	    equals("harpoon.Runtime.CounterSupport"))
	    return hc;

	instr_visitor.ancestor = input.ancestorElementMap();

	WorkSet newset = new WorkSet();
	for(Iterator it = hc.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad)it.next();
	    if ((q instanceof NEW) || (q instanceof ANEW) ||
		(syncs && (q instanceof MONITORENTER)) ||
		(q instanceof CALL))
		newset.add(q);
	}
	
	for(Iterator setit = newset.iterator(); setit.hasNext(); ) {
	    Quad q = (Quad) setit.next();
	    instr_visitor.qf = q.getFactory();
	    instr_visitor.tf = instr_visitor.qf.tempFactory();
	    q.accept(instr_visitor);
	}

	instr_visitor.ancestor = null;
	    
	if (hc.getMethod().equals(main))
	    treat_main_method(hc);

	// hc.print(new java.io.PrintWriter(System.out, true));
	return hc;
    }

    private InstrQuadVisitor instr_visitor = new InstrQuadVisitor();

    private class InstrQuadVisitor extends QuadVisitor {
	QuadFactory qf;
	TempFactory tf;
	Map ancestor;

	public void visit(MONITORENTER q) {
	    CALL qcall = 
		new CALL(qf, q, hm_count_sync, new Temp[] {q.lock()}, null,
			 new Temp(tf), false, false,
			 new Temp[0][2], new Temp[0]);
	    PHI qphi = new PHI(qf, q, new Temp[0], new Temp[0][2], 2);

	    make_links(q, qcall, qphi);
	}
	
	public void visit(CALL q) {
	    if (q.method().equals(hm_orig_exit)) {
		CALL qcall =
		    new CALL(qf, q, hm_instr_exit, new Temp[0], null,
			     new Temp(tf), false, false, new Temp[0][2],
			     new Temp[0]);
		PHI qphi = new PHI(qf, q, new Temp[0], new Temp[0][2], 2);

		make_links(q, qcall, qphi);
	    }
	    
	    if (callchains) {
		try {
		    treat_callchains(q);
		}
		catch(Error e) {
		}
	    }
	}

	private void treat_callchains(Quad q) {
	    Temp tconst = new Temp(tf);
	    Temp texcept = new Temp(tf);

	    // index inside AllocationNumbering
	    int an_idx = an.callID((Quad) ancestor.get(q));
	    CONST qconst =
		new CONST(qf, q, tconst, new Integer(an_idx), HClass.Int);
	    CALL qcall =
		new CALL(qf, q, hm_call_enter, new Temp[]{tconst}, null,
			 texcept, false, false, new Temp[0][2], new Temp[0]);
	    PHI qphi = new PHI(qf, q, new Temp[0], new Temp[0][2], 2);
	    
	    Quad.addEdge(qconst, 0, qcall, 0);
	    link_call_2_phi(qcall, qphi);
	    Quad.addEdge(q.prev(0), q.prevEdge(0).which_succ(), qconst, 0);
	    Quad.addEdge(qphi, 0, q, 0);
	    
	    CALL qcall2 = 
		new CALL(qf, q, hm_call_exit, new Temp[]{}, null, texcept,
			 false, false, new Temp[0][2], new Temp[0]);
	    PHI qphi2 = new PHI(qf, q, new Temp[0], new Temp[0][2], 2);
	    
	    link_call_2_phi(qcall2, qphi2);
	    Quad.addEdge(qphi2, 0, q.next(0), q.nextEdge(0).which_pred());
	    Quad.addEdge(q, 0, qcall2, 0);
	    
	    CALL qcall3 =
		new CALL(qf, q, hm_call_exit, new Temp[]{}, null, texcept,
			 false, false, new Temp[0][2], new Temp[0]);
	    PHI qphi3 = new PHI(qf, q, new Temp[0], new Temp[0][2], 2);
	    
	    link_call_2_phi(qcall3, qphi3);
	    Quad.addEdge(qphi3, 0, q.next(1), q.nextEdge(1).which_pred());
	    Quad.addEdge(q, 1, qcall3, 0);
	}


	public void visit(NEW q) {
	    treat_allocs(q);
	}
	
	public void visit(ANEW q) {
	    treat_allocs(q);
	}
	
	
	private void treat_allocs(Quad q) {
	    try {
		treat_allocs_real(q);
	    } catch (Error e) {
		// Ignore, means that its code called only
		// by our instrumenting code
	    }
	}
	
	private void treat_allocs_real(Quad q) {
	    assert ((q instanceof NEW) || (q instanceof ANEW)) :
		"unexpected quad type " + q;
	    
	    Temp tconst = new Temp(tf);
	    Temp texcept = new Temp(tf);

	    // index inside AllocationNumbering
	    int an_idx = an.allocID((Quad) ancestor.get(q));
	    CONST qconst =
		new CONST(qf, q, tconst, new Integer(an_idx), HClass.Int);
	    CALL qcall =
		new CALL(qf, q, hm_count_alloc, new Temp[]{tconst}, null,
			 texcept, false, false, new Temp[0][2],
			 new Temp[0]);
	    PHI qphi = new PHI(qf, q, new Temp[0], new Temp[0][2], 2);
	    
	    Quad.addEdge(qconst, 0, qcall, 0);
	    link_call_2_phi(qcall, qphi);
	    Quad.addEdge(q.prev(0), q.prevEdge(0).which_succ(), qconst, 0);
	    Quad.addEdge(qphi, 0, q, 0);
	
	    if (syncs) {
		Temp dst = (q instanceof NEW) ?
		    ((NEW)q).dst() : ((ANEW)q).dst();
		
		qcall =
		    new CALL(qf, q, method3, new Temp[]{dst, tconst}, null,
			     texcept, false, false, new Temp[0][2],
			     new Temp[0]);
		qphi = new PHI(qf, q, new Temp[0], new Temp[0][2], 2);

		link_call_2_phi(qcall, qphi);
		Quad.addEdge(qphi, 0, q.next(0), q.nextEdge(0).which_pred());
		Quad.addEdge(q, 0, qcall, 0);
	    }
	}

	// take care of all the other quad types
	public void visit(Quad q) {
	    assert false : "unexpected quad type " + q;
	}
    };


    // merge both exits from the CALL qcall into PHI: qcall => qphi
    private static void link_call_2_phi(CALL qcall, PHI qphi) {
	Quad.addEdge(qcall, 0, qphi, 0);
	Quad.addEdge(qcall, 1, qphi, 1);
    }
    
    // connect q.prev(0) -> qcall => qphi -> q
    private static void make_links(Quad q, CALL qcall, PHI qphi) {
	link_call_2_phi(qcall, qphi);
	Quad.addEdge(q.prev(0), q.prevEdge(0).which_succ(), qcall, 0);
	Quad.addEdge(qphi, 0, q, 0);
    }
    

    // make sure that any normal / exceptional return from main (which
    // is equivalent to the program termination) outputs the computed
    // allocated map by calling harpoon.Runtime.CounterSupport.exit();
    private void treat_main_method(HCode hc) {
	WorkSet exitset = new WorkSet();
	
	for(Iterator it = hc.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad)it.next();
	    if ((q instanceof RETURN) || (q instanceof THROW))
		exitset.add(q);
	}
	
	QuadFactory qf = ((Quad) hc.getRootElement()).getFactory();
	TempFactory tf = qf.tempFactory();

	for(Iterator setit = exitset.iterator(); setit.hasNext(); ) {
	    Quad q = (Quad) setit.next();
	    CALL qcall =
		new CALL(qf, q, hm_instr_exit, new Temp[0], null, new Temp(tf),
			 false, false, new Temp[0][2], new Temp[0]);
	    PHI qphi = new PHI(qf, q, new Temp[0], new Temp[0][2], 2);
	    make_links(q, qcall, qphi);
	}
    }

}
