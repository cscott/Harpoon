// PAEdgeSet.java, created Mon Jun 27 14:58:25 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Collections;
import java.util.Collection;
import java.util.Set;
import java.util.List;

import java.io.PrintWriter;
import java.io.StringWriter;

import jpaul.Graphs.DiGraph;
import jpaul.Graphs.ForwardNavigator;

import jpaul.Misc.IntMCell;

import harpoon.ClassFile.HField;

/**
 * <code>PAEdgeSet</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: PAEdgeSet.java,v 1.1 2005-08-10 02:58:19 salcianu Exp $
 */
public abstract class PAEdgeSet extends DiGraph<PANode> implements Cloneable {

    protected PAEdgeSet() { 
	super(false); // digraph with no caching
    }

    /** Adds an <code>hf</code>-labelled edge between <code>n1</code>
        to <code>n2</code>.  Does not add any edge starting in an IMM
        / CONST / NULL node. */
    public boolean addEdge(PANode n1, HField hf, PANode n2) {
	return addEdge(n1, hf, n2, false);
    }

    /** Similar to addEdge, but allows the addition of edges from IMM
        / CONST nodes.  This method should beused only to construct
        ndoe structures that model const / immutable objects like
        Strings.  After such a structure is built, it is never
        mutated.  */
    abstract boolean addEdge(PANode n1, HField hf, PANode n2, boolean addToIMM);

 
    /** Adds an <code>hf</code>-labeled edge between <code>n1</code>
        and <code>n2</code> UNDER ALL CIRCUMSTANCES (regardless of the
        type filtering, etc.).  This method is used internally, while
        computing the reverse set of edges, in the inter-procedural
        analysis.  */
    protected abstract boolean uncheckedAddEdge(PANode n1, HField hf, PANode n2);


    /** Adds an <code>hf</code>-labelled edge between <code>n1</code>
        and each node from <code>n2s</code>.  

	@return true if this is new information. */
    public boolean addEdges(PANode n1, HField hf, Collection<PANode> n2s) {
	return addEdges(n1, hf, n2s, false);
    }

    abstract boolean addEdges(PANode n1, HField hf, Collection<PANode> n2s, boolean addToIMM);



    public boolean addEdges(Collection<PANode> n1s, HField hf, Collection<PANode> n2s) {
	return addEdges(n1s, hf, n2s, false);
    }

    boolean addEdges(Collection<PANode> n1s, HField hf, Collection<PANode> n2s, boolean addToIMM) {
	if(n1s.isEmpty() || n2s.isEmpty()) return false;
	boolean changed = false;
	for(PANode n1 : n1s) {
	    if(addEdges(n1, hf, n2s, addToIMM)) {
		changed = true;
	    }
	}
	return changed;
    }


    /** @return Collection of all nodes pointed to by <code>n</code>,
        regardless of the field. */
    public abstract Collection<PANode> pointedNodes(PANode n);

    /** @return The collection of the nodes pointed to from
        <code>n1</code>, along <code>hf</code>-labelled edges. */
    public abstract Collection<PANode> pointedNodes(PANode n, HField hf);


    /** @return All nodes from which an edge starts. */
    public abstract Collection<PANode> sources();

    /** @return All nodes from <code>this<code> set of edges. */
    public abstract Iterable<PANode> allNodes();
    
    /** @return All fields that label edges that leave
        <code>node</code>. */
    public abstract Collection<HField> fields(PANode node);


    /** Checks whether <code>this</code> set of edges is empty. */
    public boolean isEmpty() { return this.sources().isEmpty(); }


    /** Adds all the edges from <code>es2</code> to <code>this</code>
        set pf edges.  

	@return true if NEW information was added to <code>this</code>
	set of edges. */
    public abstract boolean join(PAEdgeSet es2);


    public Object clone() {
	PAEdgeSet newES = null;
	try {
	    newES = (PAEdgeSet) super.clone();
	}
	catch(CloneNotSupportedException e) {
	    // should not happen ...
	    throw new Error(e);
	}
	return newES;
    }

    public Collection<PANode> getRoots() {
	return this.sources();
    }


    interface EdgeAction {
	public void action(PANode n1, HField hf, PANode n2);
    }

    public void forAllEdges(PANode n, EdgeAction edgeAction) {
	for(HField hf : fields(n)) {
	    for(PANode n2 : pointedNodes(n, hf)) {
		edgeAction.action(n, hf, n2);
	    }
	}	
    }

    public void forAllEdges(EdgeAction edgeAction) {
	for(PANode n : sources()) {
	    forAllEdges(n, edgeAction);
	}
    }


    public String toString() {
	StringWriter sw = new StringWriter();
	print(new PrintWriter(sw, true), " ");
	return sw.toString();
    }


    public void print() { this.print(""); }

    public void print(String identStr) { this.print(new PrintWriter(System.out, true), identStr); }

    public void print(PrintWriter pw, String indentStr) {
	for(PANode node : sources()) {
	    pw.print("\n");
	    pw.print(indentStr);
	    pw.print(node);
	    pw.print(" --> ");
	    for(HField hf : fields(node)) {
		pw.print("\n");
		pw.print(indentStr);
		pw.print("  "); // extra indentation
		pw.print(hf.getName());
		pw.print(" --> ");
		pw.print(pointedNodes(node, hf));
	    }
	}
	pw.flush();
    }


    PAEdgeSet reverse() {
	final PAEdgeSet revEdges = DSFactories.edgeSetFactory.create();
	this.forAllEdges(new PAEdgeSet.EdgeAction() {
	    public void action(PANode n1, HField hf, PANode n2) {
		revEdges.uncheckedAddEdge(n2, hf, n1);
	    }
	});
	return revEdges;
    }

    // immutable empty set of edges
    static final PAEdgeSet IMM_EMPTY_EDGE_SET = new PAEdgeSet() {
	boolean addEdge(PANode n1, HField hf, PANode n2, boolean addToIMM) {
	    throw new UnsupportedOperationException(); 
	}
	protected boolean uncheckedAddEdge(PANode n1, HField hf, PANode n2) {
	    throw new UnsupportedOperationException(); 
	}
	boolean addEdges(PANode n1, HField hf, Collection<PANode> n2s, boolean addToIMM) {
	    throw new UnsupportedOperationException(); 
	}
	public Collection<PANode> pointedNodes(PANode n) { 
	    return Collections.<PANode>emptySet();
	}
	public Collection<PANode> pointedNodes(PANode n, HField hf) {
	    return Collections.<PANode>emptySet();
	}
	public Collection<PANode> sources() { 
	    return Collections.<PANode>emptySet();
	}
	public Iterable<PANode> allNodes() {
	    return Collections.<PANode>emptySet();
	}
	public Collection<HField> fields(PANode node) {
	    return Collections.<HField>emptySet();
	}
	public boolean join(PAEdgeSet es2) {
	    throw new UnsupportedOperationException(); 
	}
	public ForwardNavigator<PANode> getForwardNavigator() {
	    return new ForwardNavigator<PANode>() {
		public List<PANode> next(PANode node) {
		    return Collections.<PANode>emptyList();
		}
	    };
	}
    };
}
