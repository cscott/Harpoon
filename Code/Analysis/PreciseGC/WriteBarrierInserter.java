// WriteBarrierInserter.java, created Wed Jun 19 16:47:07 2002 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

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
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.Code;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * <code>WriteBarrierInserter</code> takes code in Quad form and inserts 
 * write barriers for generational garbage collection. Write barriers are
 * inserted before <code>SET</code>s of non-static <code>Object</code> and
 * <code>ASET<code>s of <code>Object</code> arrays.
 * 
 * The write barrier used is a call to a native implementation that may be
 * replaced in later passes with a more efficient implementation.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: WriteBarrierInserter.java,v 1.1 2002-06-25 18:16:22 kkz Exp $
 */
public class WriteBarrierInserter extends 
    harpoon.Analysis.Transformation.MethodMutator {
    private final HClass JLRF;
    private final HMethod arrayWB;
    private final HMethod fieldWB;
    private final WriteBarrierAnalysis wba;
    
    /** Creates a <code>WriteBarrierInserter</code>. */
    public WriteBarrierInserter(HCodeFactory parent, Linker linker, 
				WriteBarrierAnalysis wba) {
	super(parent);
	this.wba = wba;
	HClass WBC = linker.forName("harpoon.Runtime.PreciseGC.WriteBarrier");
	HClass JLO = linker.forName("java.lang.Object");
	this.JLRF = linker.forName("java.lang.reflect.Field");
	this.arrayWB = WBC.getMethod("asc", new HClass[]
				     { JLO, HClass.Int, JLO, HClass.Int });
	this.fieldWB = WBC.getMethod("fsc", new HClass[]
				     { JLO, JLRF, JLO, HClass.Int });	
    }

    /** Creates a <code>WriteBarrierInserter</code> using a default
     *  no-analysis <code>WriteBarrierAnalysis</code>.
     */
    public WriteBarrierInserter(HCodeFactory parent, Linker linker) {
	this(parent, linker, DefaultWriteBarrierAnalysis.SINGLETON);
    }

    protected HCode mutateHCode(HCodeAndMaps input) {
	Code hc = (Code) input.hcode();
	// we should not have to update derivation information
	assert hc.getDerivation() == null: 
	    "WriteBarrierInserter does not handle derivation information";
	HEADER header = (HEADER) hc.getRootElement();
	FOOTER footer = (FOOTER) header.footer();
	Set ignore = wba.getIgnoreSet(hc);
	QuadVisitor qv = new WriteBarrierInserterVisitor
	    (footer, ignore, input.ancestorElementMap());
	// we put all elements in array to avoid screwing up 
	// the iterator as we mutate the quad graph in-place.
	Quad[] allquads = (Quad[]) hc.getElements();
	for(int i = 0; i < allquads.length; i++)
	    allquads[i].accept(qv);
	return hc;
    }
    
    private class WriteBarrierInserterVisitor extends QuadVisitor {
	private FOOTER footer;
	private final Set ignore;
	private final Map quadM;

	/** Creates a <code>WriteBarrierInserterVisitor</code>. */
	WriteBarrierInserterVisitor(FOOTER footer, Set ignore, Map quadM) {
	    this.footer = footer;
	    this.ignore = ignore;
	    this.quadM = quadM;
	}

	public void visit(Quad q) { /* do nothing */ }

	/* insert write barrier before ASET, if needed. */ 
	public void visit(ASET q) {
	    // we are interested only in ASETs of arrays 
	    // containing pointers not found in ignore
	    if (q.type().isPrimitive()) return;
	    if (ignore.contains(quadM.get(q))) return;
	    QuadFactory qf = (QuadFactory) q.getFactory();
	    TempFactory tf = qf.tempFactory();
	    // create needed Temps
	    Temp retex = new Temp(tf, "wbex");
	    Temp id = new Temp(tf, "wbid");
	    // create needed Quads
	    CONST idC = new CONST(qf, q, id, new Integer(0), HClass.Int);
	    CALL call = new CALL(qf, q, arrayWB, new Temp[]
				 { q.objectref(), q.index(), q.src(), id },
				 null, retex, false, false, new Temp[0]);
	    THROW thr = new THROW(qf, q, retex);
	    // add CONST and CALL before ASET
	    splice(idC, q.prevEdge(0));
	    splice(call, q.prevEdge(0));
	    // add THROW after CALL
	    Quad.addEdge(call, 1, thr, 0);
	    footer = footer.attach(thr, 0);
	}

	/* inserts write barrier before SET, if needed. */
	public void visit(SET q) {
	    // we are interested only in SETs of non-static
	    // Object fields not found in ignore
	    if (q.isStatic()) return;
	    if (q.field().getType().isPrimitive()) return;
	    if (ignore.contains(quadM.get(q))) return;
	    QuadFactory qf = (QuadFactory)q.getFactory();
	    TempFactory tf = qf.tempFactory();
	    // create needed Temps
	    Temp field = new Temp(tf, "wbfield");
	    Temp retex = new Temp(tf, "wbex");
	    Temp id = new Temp(tf, "wbid");
	    // create needed Quads
	    CONST idC = new CONST(qf, q, id, new Integer(0), HClass.Int);
	    CONST fieldC = new CONST(qf, q, field, q.field(), JLRF);
	    CALL call = new CALL(qf, q, fieldWB, new Temp[]
				 { q.objectref(), field, q.src(), id },
				 null, retex, false, false, new Temp[0]);
	    THROW thr = new THROW(qf, q, retex);
	    // add CONSTs and CALL before SET
	    splice(idC, q.prevEdge(0));
	    splice(fieldC, q.prevEdge(0));
	    splice(call, q.prevEdge(0));
	    // add THROW after CALL
	    Quad.addEdge(call, 1, thr, 0);
	    footer = footer.attach(thr, 0);
	}

	/* helper method inserts the given Quad on the given Edge. */
	private void splice(Quad q, Edge e) {
	    Quad.addEdge((Quad) e.from(), e.which_succ(), q, 0);
	    Quad.addEdge(q, 0, (Quad) e.to(), e.which_pred());
	}
    }

    /** A <code>WriteBarrierAnalysis</code> maps <code>Code</code>s
     *  to <code>Set</code>s of Quads for which write barriers have
     *  been deemed unnecessary.
     */
    public interface WriteBarrierAnalysis {

	/** returns a <code>Set</code> of <code>Quad</code>s for
	 *  the given <code>Code</code> for which write barriers
	 *  are not required.
	 */
	public Set getIgnoreSet(Code hc);
    }

    /** <code>DefaultWriteBarrierAnalysis</code> returns a no-
     *  analysis <code>WriteBarrierAnalysis</code> Object that
     *  assumes write barriers are needed for all SET and ASETs
     *  of object fields.
     */
    public static class DefaultWriteBarrierAnalysis implements
	WriteBarrierAnalysis {

	/** A static instance of the singleton 
	 *  <code>DefaultWriteBarrierAnalysis</code>.
	 */
	public static final WriteBarrierAnalysis SINGLETON =
	    new DefaultWriteBarrierAnalysis();

	private DefaultWriteBarrierAnalysis() { }

	/** returns the empty <code>Set</code> for all <code>Code<code>s. 
	 */
	public Set getIgnoreSet(Code hc) {
	    return Collections.EMPTY_SET;
	}
    }
}
    
