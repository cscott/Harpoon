// ColorableGraph.java, created Wed Jan 13 14:13:21 1999 by pnkfelix
// Copyright (C) 1998 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * <code>ColorableGraph</code> extends <code>Graph</code> with methods
 * that are useful for a graph-coloring system.  Two main pieces of
 * state are added:  
 * <OL>
 * <LI> A Node -> Color mapping
 * <LI> A stack of "hidden" nodes.
 * </OL>
 * The first component is inherent in the nature of a colorable
 * graph.  
 * The second component of state is to allow optimization of a common 
 * routine required by graph-coloring algorithms: the ability to
 * temporarily remove a node and its associated edges from a graph but 
 * retaining the information for later replacement into the graph
 * again.  When a node is hidden, all methods on the graph will
 * operate as if it has been removed from the graph, except for
 * <code>addNode(Object)</code>.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: ColorableGraph.java,v 1.1.2.12 2000-07-25 03:01:02 pnkfelix Exp $ */

public interface ColorableGraph extends Graph {

    /** Ensures that this graph contains <code>node</code> (optional operation).
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B>  If this method returns normally,
	     <code>node</code> will be present in the node-set for
	     <code>this</code>.  Returns <tt>true</tt> if this graph
	     changed as a result of the call, <tt>false</tt>
	     otherwise.
	@throws UnsupportedOperationException addNode is not supported
	        by this graph.
	@throws ClassCastException class of specified element prevents
	        it from being added to this graph.
	@throws AlreadyHiddenException node is part of the set of
	        hidden nodes in <code>this</code>
	@throws IllegalArgumentException some aspect of
	        <code>node</code> prevents it from being added to the
		node-set for this graph.
    */
    boolean addNode( Object node );

    /** AlreadyHiddenException will be thrown on attempt to call
	<code>g.addNode(n)</code> while n is a member of the set of
	hidden nodes in <code>g</code>.
    */
    public static class AlreadyHiddenException 
	extends IllegalArgumentException {
	public AlreadyHiddenException() { super(); }
	public AlreadyHiddenException(String s) { super(s); }
    }
    
    /** AlreadyColoredException will be thrown on attempt to call
	<code>g.setColor(node,color)</code> when n is present in the
	node -> color mapping.
    */
    public static class AlreadyColoredException 
	extends IllegalArgumentException {
 	/** The node that was targetted for coloring. */
	public final Object node;

	public AlreadyColoredException(Object n) { 
	    super(); 
	    node = n; 
	}
	public AlreadyColoredException(String s, Object n) { 
	    super(s);
	    node = n;
	}
    }

    /** Returns all nodes in graph to their original state.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> the Node -> Color mapping 
	     is cleared and each hidden node is restored;
	     <code>this</code> is otherwise unchanged.
    */
    void resetGraph();

    /** Reverts the graph to an uncolored state.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> clears the Node -> Color mapping.
    */
    void resetColors();

    /** Sets the color of <code>n</code>.
	<BR> <B>effects:</B> If <code>c</code> is null, then
	     removes <code>n</code> from the Node -> Color mapping.
	     Else puts (n, c) in the Node -> Color mapping.
        @throws IllegalArgumentException <code>n</code> is not
	     present in the node set for <code>this</code>.
	@throws AlreadyColoredException <code>n</code> is already
	     present in the Node -> Color mapping.
    */
    void setColor(Object n, Color c);


    /** Returns the color of <code>node</code>.
	<BR> <B>effects:</B> Returns the color associated with
	     <code>node</code> in the Node -> Color mapping, or null
	     if there is no entry in the mapping for
	     <code>node</code>.  
        @throws IllegalArgumentException If <code>node</code> is not
	     present in the node set for <code>this</code>.
    */
    Color getColor(Object node);

    /** Temporarily removes <code>node</code> from graph.
	<BR> <B>modifies:</B> <code>this</code>, <code>node</code>
	<BR> <B>effects:</B> Removes <code>node</code> and
	     <code>node</code>'s associated edges from
	     <code>this</code>, pushing it onto hidden-nodes stack
	     for later replacement in the graph.  Also updates all
	     edges and nodes of <code>this</code> to reflect that 
	     <code>node</code> has been hidden.
        @throws IllegalArgumentException If <code>node</code> is not
	     present in the node set for <code>this</code>.
    */
    void hide( Object node );
    
    /** Replaces the last hidden node</code> into the graph.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> 
	     if hidden-nodes stack is empty,
	     then returns null
	     else let n = Pop(hidden-nodes stack).
	          puts `n' and its associated edges back in the
		  graph, updating all edges and nodes of
		  <code>this</code> to reflect that n has been 
		  replaced. 
		  Returns n. 
    */
    Object replace();

    /** Replaces all hidden nodes in graph.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> 
	     until hidden-nodes stack is empty,
	          let n = Pop(hidden-nodes stack).
	          puts `n' and its associated edges back in the
		  graph, updating all edges and nodes of
		  <code>this</code> to reflect that n has been 
		  replaced. 
    */
    void replaceAll();
    
}

