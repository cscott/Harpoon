// InterfaceMethodMap.java, created Tue Jan 19 17:10:17 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Analysis;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HMethod;
import harpoon.Backend.Maps.MethodMap;

import harpoon.Util.Util;
import harpoon.Util.UniqueVector;

import harpoon.Analysis.ClassHierarchy;

import harpoon.Analysis.GraphColoring.Color;
import harpoon.Analysis.GraphColoring.ColorableNode;
import harpoon.Analysis.GraphColoring.ColorFactory;
import harpoon.Analysis.GraphColoring.SimpleGraphColorer;
import harpoon.Analysis.GraphColoring.UnboundedGraphColorer;
import harpoon.Analysis.GraphColoring.SparseGraph;
import harpoon.Analysis.GraphColoring.SparseNode;
import harpoon.Analysis.GraphColoring.IllegalEdgeException;
/**
 * <code>InterfaceMethodMap</code> provides a mapping from interface
 * methods to the offset that the method-pointers should have on the
 * object layout.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: InterfaceMethodMap.java,v 1.1.4.6 2001-09-24 16:36:57 cananian Exp $
 */

public class InterfaceMethodMap extends MethodMap {

    private static boolean DEBUG = false;

    // maps HMethod to HmNode
    private Hashtable mtable;
    private HmNodeFactory factory;
	    
    /** Creates a <code>InterfaceMethodMap</code> for interfaces in
	<code>hclasses</code>. 
	<BR> <B>requires:</B> <code>hclasses</code> is an
	                      <code>Enumeration</code> of
			      <code>HClass</code> objects.
	<BR> <B>modifies:</B> <code>hclasses</code>
	<BR> <B>effects:</B> Iterates through <code>hclasses</code>,
	              accumulating all of the interface-methods and
		      returns a method->int mapping, where the integer
		      returned represents the placement of the method.
		      This method is not guaranteed to use any
		      information besides what is passed to it;
		      methods from interfaces not in
		      <code>hclasses</code> are not required to be
		      included in the method map returned, and
		      interferences from classes not in
		      <code>hclasses</code> are not required to be
		      accounted for.  
	@see HClass
	@see harpoon.Analysis.ClassHierarchy
	@see harpoon.Main.CallGraph
     */
    public InterfaceMethodMap( Enumeration hclasses ) {
	mtable = new Hashtable();
	factory = new HmNodeFactory();

        SparseGraph g = assembleGraph( hclasses );
	UnboundedGraphColorer colorer =
	    new UnboundedGraphColorer(new SimpleGraphColorer(),
				      new SlotColorFactory() );
	colorer.findColoring( g );
	
    }
    /** Creates a <code>InterfaceMethodMap</code> for interfaces in
	the given <code>ClassHierarchy</code> <code>ch</code>. 
	<BR> <B>effects:</B> Iterates through the class hierarchy
	     accumulating all of the interface-methods and returns a
	     method->int mapping, where the integer returned
	     represents the placement of the method.  This method is
	     not guaranteed to use any information besides what is
	     passed to it; methods from interfaces not in the class
	     hierarchy are not required to be included in the method
	     map returned, and interferences from classes not in 
	     the class hierarchy are not required to be accounted for.
	@see HClass
	@see harpoon.Analysis.ClassHierarchy
	@see harpoon.Main.CallGraph
     */
    public InterfaceMethodMap( ClassHierarchy ch ) {
	this(Collections.enumeration(ch.classes()));
    }
    
    /** Returns an ordering of the given method. 
	<BR> <B>requires:</B> <code>this</code> contains an ordering
	                      for <code>hm</code> 
	<BR> <B>effects:</B> returns the zero-indexed integer
	                     corresponding to <code>hm</code>'s
			     placement in the ordering.  
     */
    public int methodOrder( HMethod hm ) {
	// method must be an interface method and thus public,
	// not static, and not a constructor.
	Util.assert(hm.isInterfaceMethod() && !hm.isStatic());
	Util.assert(Modifier.isPublic(hm.getModifiers()));
	Util.assert(!(hm instanceof HConstructor));
	HmNode node = (HmNode) mtable.get( hm );
	Util.assert(node != null, 
		    "InterfaceMethodMap must contain "+
		    "a mapping for " + hm);
	SlotColor c = (SlotColor) node.getColor();
	return c.index;
    }

    
    // -- HELPER METHODS FOR CONSTUCTOR FOLLOW --

    /** Assembles a graph for use by the graph colorer.
	<BR> <B>requires:</B> <code>hclasses</code> is an enumeration of 
	                      <code>HClass</code> objects
	<BR> <B>modifies:</B> <code>hclasses</code>
	<BR> <B>effects:</B> Iterates through <code>hclasses</code>,
	                     building up a graph with nodes
			     corresponding to the methods of the
			     interfaces in <code>hclasses</code> and
			     the edges corresponding to interferences
			     between the methods (due to being
			     implemented by the same classes). 
    */
    private SparseGraph assembleGraph( Enumeration hclasses ) {
	UniqueVector classes = new UniqueVector();
	UniqueVector interfaces = new UniqueVector();
	SparseGraph graph = new SparseGraph();
	
	// MAKE NODES (methods of interfaces)
	while ( hclasses.hasMoreElements() ) {
	    HClass hc = (HClass) hclasses.nextElement();
	    if (hc.isInterface()) { // INTERFACE
		// make nodes for each method and add them to graph
		if (DEBUG) System.out.println("Adding methods of interface " + 
					      hc + " to graph");
		
		interfaces.addElement( hc );
		HMethod[] methods = hc.getMethods();
		for (int i=0; i<methods.length; i++) {
		    if (includeMethod( methods[i] )) {
			HmNode mnode = factory.getNode( methods[i] );
			if (DEBUG) System.out.println
				       ("Adding method/node " + 
					methods[i] + " / " + mnode);
			graph.addNode( mnode );
		    }
		}
	    } else {                // CLASS
		// add hclasses to classes for later edge searching
		if (DEBUG) System.out.println
			       ("Storing " + hc + " for later searching");
		classes.addElement( hc );
	    }
	}
	
	// MAKE EDGES (between nodes belonging to interfaces
	// implemented by same class) 
	for (int i=0; i<classes.size(); i++) {
	    HClass hc = (HClass) classes.elementAt(i);
	    UniqueVector cNodes = new UniqueVector();
	    
	    // backtrack through hierarchy, adding all methods of all
	    // interfaces found.
	    while( hc != null ) {
		HClass[] ifaces = hc.getInterfaces();
		for (int j=0; j<ifaces.length; j++) {
		    Vector nodes = findNodesFor( ifaces[j] );
		    for(int k=0; k<nodes.size(); k++) {
			cNodes.addElement( nodes.elementAt( k ));
		    }
		}
		hc = hc.getSuperclass();
	    }
	    
	    for (int j=0; j<cNodes.size(); j++) {
		HmNode nodeA = (HmNode) cNodes.elementAt(j);
		for (int k=j+1; k<cNodes.size(); k++) {
		    HmNode nodeB = (HmNode) cNodes.elementAt(k);
		    try {
			if (DEBUG) System.out.println
				       ("Making an edge between " + nodeA + 
					" and " + nodeB + " in graph.");
			graph.makeEdge( nodeA, nodeB );
		    } catch (IllegalEdgeException e) {
			// ignore (algorithm is dumb and doesn't know
			// better. 
		    }
		}
	    }
	}
	
	return graph;
    }

	
    /** Checks suitability of an HMethod for inclusion in a graph.
	<BR> <B>effects:</B> if <code>m</code> is a method of
	                     <code>java.lang.Object</code> then
			     returns false.  Else returns true. 
    */
    private static boolean includeMethod(HMethod m) {
	return !m.getDeclaringClass().getName().equals("java.lang.Object");
    }

    /** Generates a <code>Vector</code> of <code>HmNode</code>s for
	methods in an interface.  
	<BR> <B>requires:</B> <code>interfce</code> is an HClass
	                      representing an interface, and there are
			      no circular interface extensions 
			      (ie "A extends B" and "B extends A")
	<BR> <B>effects:</B> Iterates over the accessible methods of
	                     <code>interfce</code>, accumulating them
			     in a <code>Vector</code> of
			     <code>HmNode</code>s.  Methods of
			     superinterfaces are included, but the
			     methods of Object are not.  Returns the
			     newly built <code>Vector</code> of
			     <code>HmNode</code>s.
    */
    private Vector findNodesFor( HClass interfce ) {
	Util.assert( interfce.isInterface() );
	if (DEBUG) System.out.println("Finding nodes for " + interfce);
	Vector nodes = new Vector();
	HMethod[] methods = interfce.getMethods();
	for (int i=0; i<methods.length; i++) {
	    if (includeMethod( methods[i] )) {
		if (DEBUG) System.out.println
			       ("Adding method " + methods[ i ]);
		nodes.addElement( factory.getNode( methods[ i ] ) ); 
	    }
	}
	
	// this sequence of code is unnecessary; HClass.getMethods()
	// now performs according to spec.
	/*
	  HClass[] superinterfaces = interfce.getInterfaces(); 
	  for (int i=0; i<superinterfaces.length; i++) { 
	    Vector v = findNodesFor( superinterfaces[i] ); 
	    for (int j=0; j<v.size(); j++) { 
		nodes.addElement(v.elementAt(j)); 
		} 
	    }
	*/
	
	return nodes;
    }
        
    /** Produces HmNodes from HMethods, enforcing a mapping from a
	method's name and set of argument types to one unique node,
	regardless of which interface the method appears in.
	<p>
	Interface methods with the same name and descriptor always
	map to the same slot, either due to single or multiple inheritance.
	For example, A.foo() maps to the same slot as B.foo() if interface
	B extends interface A; if interface B also extends interface C
	and C has C.foo() then all three methods would always map to the
	same slot (it is impossible to give C.foo() a different
	implementation from A.foo() in any class which implements both A
	and C).
    */
    private class HmNodeFactory {
	// maps HmNode to HmNode
	Hashtable ntable;
	
	HmNodeFactory() {
	    ntable = new Hashtable();
	}
	
	HmNode getNode( HMethod hm ) {
	    HmNode temp = new HmNode( hm );
	    HmNode real = (HmNode) ntable.get( temp );
	    HmNode rtrn;
	    if (real == null) {
		ntable.put( temp, temp );
		rtrn = temp;
	    } else {
		rtrn = real;
	    }
	    mtable.put( hm, rtrn );
	    return rtrn;
	}
    }
    
    
    /** HmNode is an extension of a SparseNode suitable for
	representing a method of an interface.
    */
    private class HmNode extends SparseNode {
	final String name, desc;
	int hash;
	
	HmNode( HMethod hm ) {
	    name = hm.getName();
	    desc = hm.getDescriptor();
	    hash = name.hashCode() ^ desc.hashCode();
	}
	
	public int hashCode() { return hash; }

	public boolean equals(Object o) {
	    HmNode n;
	    if (o==null) return false;
	    if (this==o) return true;
	    try { n = (HmNode) o; }
	    catch (ClassCastException e) { return false; }
	    if (n.hash!=this.hash) return false;
	    return n.name.equals(this.name) && n.desc.equals(this.desc);
	}

	public String toString() {
	    return "HMethod Node [ " + name + desc + " " + hash +" ]";
	}
    }

    /** Simple implementation of a ColorFactory for interface slots. */
    private class SlotColorFactory extends ColorFactory {
	int counter;

	SlotColorFactory() {
	    counter = 0;
	}

	protected Color newColor() {
	    counter++;
	    return new SlotColor(counter);
	}
    }

    /** Simple implementation of a Color for interface slots. */
    private class SlotColor extends Color {
	int index;
	SlotColor(int i) { index = i; }
	public String toString() { return "SlotColor"+index; }
    }
}

