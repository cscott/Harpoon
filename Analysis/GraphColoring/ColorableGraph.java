// ColorableGraph.java, created Wed Jan 13 14:13:21 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * <code>ColorableGraph</code> extends
 * <code>GraphColoring.Graph</code> with methods 
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
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: ColorableGraph.java,v 1.1.2.17 2001-06-17 22:29:38 cananian Exp $ */

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

    /** IllegalColor will be thrown on an attempt to color a
	node with a color that for some reason is not legal for that
	node in this graph.
    */
    public static class IllegalColor extends Throwable {
	/** Color intended for assignment. */
	public final Color color;
	/** Node intended to be assigned to <code>color</code>. */
	public final Object node;

	/** Constructs an IllegalColor with <code>node = n</code> and
	    <code>color = c</code>.
	*/
	public IllegalColor(Object n, Color c) {
	    super();
	    node = n;
	    color = c;
	}
    }

    /** Reverts the graph's color mapping to its initial state.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> undoes any modifications made to the
	     graph's Node -> Color mapping.  Note that if a colorable
	     graph is to support <i>node precoloring</i>, where
	     elements of the graph are assigned a color prior to an
	     attempt to color the remainder of the graph, then it
	     is not sufficient to implement this method by completely
	     clearing the Node -> Color mapping.
    */
    void resetColors();

    /** Sets the color of <code>n</code>.
	<BR> <B>requires:</B> <code>c</code> != null
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> If no exception is thrown, 
	     puts (n, c) in the Node -> Color mapping
        @throws IllegalArgumentException <code>n</code> is not
	     present in the node set for <code>this</code>.  No
	     modification to <code>this</code>.
	@throws AlreadyColoredException <code>n</code> is already
	     present in the Node -> Color mapping.  No modification to
	     <code>this</code>.
	@throws IllegalColor <code>c</code> is not an appropriate
	     <code>Color</code> for <code>n</code> in this graph.  No
	     modification to <code>this</code>.
    */
    void setColor(Object n, Color c) throws IllegalColor;
    
    /** Removes <code>n</code> from the Node -> Color mapping. 
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects</B> If the pair (n, c) is present in 
	     the Node -> Color mapping, removes the pair.  Else does
	     nothing. 
	@throws IllegalArgumentException <code>n</code> is not present
	     in the node set for <code>this</code>.  No modification
	     to <code>this</code>.
     */
    void unsetColor(Object n);

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

