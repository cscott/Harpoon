// QuadFlowGraph.java, created Fri May  9 18:34:48 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Companions;

import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.SWITCH;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.AbstractGraph;
import net.cscott.jutil.WorkSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * <code>QuadFlowGraph</code> is an expanded flow graphs for Quads, where
 * additional nodes are added before PHI and after SIGMA nodes to make the
 * dataflow corresponding to the phi and sigma functions easier to express.
 * Move operations corresponding to a PHI (SIGMA) can now take place
 * conceptually before (after) the node, on the incoming (outgoing) edge.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadFlowGraph.java,v 1.3 2004-02-08 01:50:55 cananian Exp $
 */
public class QuadFlowGraph extends AbstractGraph<QNode,QEdge> {
    
    /** Creates a <code>QuadFlowGraph</code>. */
    public QuadFlowGraph(harpoon.IR.Quads.Code c) {
	Map<Quad,QNode> map = new HashMap<Quad,QNode>();
	WorkSet<harpoon.IR.Quads.Edge> ws=new WorkSet<harpoon.IR.Quads.Edge>();
	// make nodes for all Quads.
	for (Iterator<Quad> it=c.getElementsI(); it.hasNext(); ) {
	    Quad q = it.next();
	    QNode qn = new NormalNode(this, q);
	    map.put(q, qn);
	    addNode(qn);
	    ws.addAll(q.edgeC());
	}
	// make QEdges for all Edges
	while (!ws.isEmpty()) {
	    harpoon.IR.Quads.Edge e = ws.removeFirst();
	    QNode fr = map.get(e.from());
	    if (fr.baseQuad() instanceof SIGMA) { // split Sigma edge
		QNode qn = new SigmaExitNode(this, (SIGMA) fr.baseQuad(),
					     e.which_succ());
		addNode(qn);
		addEdge(fr, qn);
		fr = qn;
	    }
	    QNode to = map.get(e.to());
	    if (to.baseQuad() instanceof PHI) { // split phi edge
		QNode qn = new PhiEntranceNode(this, (PHI) to.baseQuad(),
					       e.which_pred());
		addNode(qn);
		addEdge(qn, to);
		to = qn;
	    }
	    addEdge(fr, to);
	}
	// done!
    }
    public QEdge addEdge(QNode from, QNode to) {
	QEdge qe = new QEdge(from, to);
	this.addEdge(qe);
	return qe;
    }
}
// Bug in JSR-14 compiler prevents these from being inner classes, as they
// ought be.
/** A <code>QNode</code> is an element of the <code>QuadFlowGraph</code>.
 *  Most <code>QNode</code> correspond to <code>Quad</code>s in the underlying
 *  flowgraph (these are instances of the <code>QNode</code> subclass
 *  <code>NormalNode</code>, but there are also <code>QNode</code>s
 *  representing artificial nodes before <code>PHI</code>s
 *  (<code>PhiEntranceNode</code>) and after <code>SIGMA</code>s
 *  (<code>SigmaExitNode</code>).  Because of this, the <code>QNode</code>
 *  may have different edges than the underlying <code>Quad</code>
 *  returned by the <code>baseQuad()</code> method.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadFlowGraph.java,v 1.3 2004-02-08 01:50:55 cananian Exp $
 */
abstract class QNode extends AbstractGraph.Node<QNode,QEdge> {
    QNode(QuadFlowGraph parent) { super(parent); }
    abstract Quad baseQuad();
    abstract boolean isPhiEntrance();
    abstract boolean isSigmaExit();
    abstract int whichEdge();
    abstract Collection<Temp> useC();
    abstract Collection<Temp> defC();
}
/** A <code>NormalNode</code> wraps an arbitrary <code>Quad</code> and
 *  corresponds directly to a node in the underlying flow graph,
 *  although the edges may differ if the underlying quad is a
 *  <code>PHI</code> or <code>SIGMA</code>.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadFlowGraph.java,v 1.3 2004-02-08 01:50:55 cananian Exp $
 */
final class NormalNode extends QNode {
    final Quad baseQuad;
    NormalNode(QuadFlowGraph parent, Quad baseQuad) {
	super(parent);
	this.baseQuad = baseQuad;
    }
    Quad baseQuad() { return baseQuad; }
    boolean isPhiEntrance() { return false; }
    boolean isSigmaExit() { return false; }
    int whichEdge() { throw new RuntimeException("a normal quad"); }
    Collection<Temp> useC() {
	if (baseQuad instanceof PHI)
	    return Collections.EMPTY_LIST;
	if (baseQuad instanceof CALL)
	    return Arrays.asList(((CALL)baseQuad).params());
	if (baseQuad instanceof CJMP)
	    return Collections.singletonList(((CJMP)baseQuad).test());
	if (baseQuad instanceof SWITCH)
	    return Collections.singletonList(((SWITCH)baseQuad).index());
	if (baseQuad instanceof TYPESWITCH)
	    return Collections.singletonList(((TYPESWITCH)baseQuad).index());
	return baseQuad.useC();
    }
    Collection<Temp> defC() {
	if (baseQuad instanceof CALL) {
	    CALL call = (CALL) baseQuad;
	    List<Temp> defs = new ArrayList<Temp>(2);
	    if (call.retval()!=null) defs.add(call.retval());
	    if (call.retex()!=null) defs.add(call.retex());
	    return defs;
	}
	if (baseQuad instanceof PHI ||
	    baseQuad instanceof SIGMA)
	    return Collections.EMPTY_LIST;
	return baseQuad.defC();
    }
}
/** A <code>PhiEntranceNode</code> is an artificial node in the
 * <code>QuadFlowGraph</code> which is inserted on the incoming
 * edge of a <code>PHI</code>.  It reports its <code>baseQuad()</code>
 * as the affiliated <code>PHI</code>.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadFlowGraph.java,v 1.3 2004-02-08 01:50:55 cananian Exp $
 */
final class PhiEntranceNode extends QNode {
    final PHI baseQuad;  final int whichEdge;
    PhiEntranceNode(QuadFlowGraph parent, PHI baseQuad, int whichEdge) {
	super(parent);
	this.baseQuad = baseQuad; this.whichEdge = whichEdge;
    }
    PHI baseQuad() { return baseQuad; }
    boolean isPhiEntrance() { return true; }
    boolean isSigmaExit() { return false; }
    int whichEdge() { return whichEdge; }
    List<Temp> defC() {
	List<Temp> defs = new ArrayList<Temp>(baseQuad.numPhis());
	for (int i=0; i<baseQuad.numPhis(); i++)
	    defs.add(baseQuad.dst(i));
	return defs;
    }
    List<Temp> useC() {
	List<Temp> uses = new ArrayList<Temp>(baseQuad.numPhis());
	for (int i=0; i<baseQuad.numPhis(); i++)
	    uses.add(baseQuad.src(i, whichEdge));
	return uses;
    }
}
/** A <code>SigmaExitNode</code> is an artificial node in the
 * <code>QuadFlowGraph</code> which is inserted on the outgoing
 * edge of a <code>SIGMA</code>.  It reports its <code>baseQuad()</code>
 * as the affiliated <code>SIGMA</code>.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadFlowGraph.java,v 1.3 2004-02-08 01:50:55 cananian Exp $
 */
final class SigmaExitNode extends QNode {
    final SIGMA baseQuad;  final int whichEdge;
    SigmaExitNode(QuadFlowGraph parent, SIGMA baseQuad, int whichEdge) {
	super(parent);
	this.baseQuad = baseQuad; this.whichEdge = whichEdge;
    }
    SIGMA baseQuad() { return baseQuad; }
    boolean isPhiEntrance() { return false; }
    boolean isSigmaExit() { return true; }
    int whichEdge() { return whichEdge; }
    List<Temp> defC() {
	List<Temp> defs = new ArrayList<Temp>(baseQuad.numSigmas());
	for (int i=0; i<baseQuad.numSigmas(); i++)
	    defs.add(baseQuad.dst(i, whichEdge));
	return defs;
    }
    List<Temp> useC() {
	List<Temp> uses = new ArrayList<Temp>(baseQuad.numSigmas());
	for (int i=0; i<baseQuad.numSigmas(); i++)
	    uses.add(baseQuad.src(i));
	return uses;
    }
}
/**
 * <code>QEdge</code> represents edges between <code>QNode</code>
 * objects.  They usually correspond to <code>Quad.Edge</code> object,
 * but not in the case of inserted <code>PhiEntranceNode</code>s and
 * <code>SigmaExitNode</code>s.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadFlowGraph.java,v 1.3 2004-02-08 01:50:55 cananian Exp $
 */
final class QEdge extends AbstractGraph.Edge<QNode,QEdge> {
    QEdge(QNode from, QNode to) { super(from, to); }
}
