// InterfaceMethodMap.java, created Tue Jan 19 17:10:17 1999 by pnkfelix
package harpoon.Analysis;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.Backend.Maps.MethodMap;

import harpoon.Util.Util;
import harpoon.Util.UniqueVector;


import harpoon.Analysis.GraphColoring.Color;
import harpoon.Analysis.GraphColoring.ColorableNode;
import harpoon.Analysis.GraphColoring.ColorFactory;
import harpoon.Analysis.GraphColoring.SimpleGraphColorer;
import harpoon.Analysis.GraphColoring.SparseGraph;
import harpoon.Analysis.GraphColoring.SparseNode;
import harpoon.Analysis.GraphColoring.IllegalEdgeException;
/**
 * <code>InterfaceMethodMap</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: InterfaceMethodMap.java,v 1.1.2.3 1999-01-21 22:49:07 pnkfelix Exp $
 */

public class InterfaceMethodMap extends MethodMap {

    private static boolean DEBUG = false;

    // maps HMethod to HmNode
    private Hashtable mtable;
    private HmNodeFactory factory;
	    
    /** Creates a <code>InterfaceMethodMap</code> for interfaces in
	<code>hclasses</code>. 
	<BR> requires: <code>hclasses</code> is an
	               <code>Enumeration</code> of <code>HClass</code>
		       objects.   
	<BR> modifies: <code>hclasses</code>
	<BR> effects: Iterates through <code>hclasses</code>,
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
     */
    public InterfaceMethodMap( Enumeration hclasses ) {
	mtable = new Hashtable();
	factory = new HmNodeFactory();

        SparseGraph g = assembleGraph( hclasses );
	SimpleGraphColorer colorer =
	    new SimpleGraphColorer( new SlotColorFactory() );
	colorer.findColoring( g );
	
    }
    
    /** Returns an ordering of the given method. 
	requires: <code>this</code> contains an ordering for
	          <code>hm</code> 
	effects: returns the zero-indexed integer corresponding to
	         <code>hm</code>'s placement in the ordering. 
     */
    public int methodOrder( HMethod hm ) {
	HmNode node = (HmNode) mtable.get( hm );
	SlotColor c = (SlotColor) node.getColor();
	return c.index;
    }

    
    // -- HELPER METHODS FOR CONSTUCTOR FOLLOW --

    /** Assembles a graph for use by the graph colorer.
	<BR> requires: <code>hclasses</code> is an enumeration of 
	               <code>HClass</code> objects
	<BR> modifies: <code>hclasses</code>
	<BR> effects: Iterates through <code>hclasses</code>, building
	              up a graph with nodes corresponding to the
		      methods of the interfaces in
		      <code>hclasses</code> and the edges
		      corresponding to interferences between the
		      methods (due to being implemented by the same
		      classes). 
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

	
    private static HClass objHClass = HClass.forName("java.lang.Object");

    /** Helper method for graph assembly routines: checks suitability
	of an HMethod.
	<BR> effects: if <code>m</code> is a method of
	              <code>java.lang.Object</code> then returns
		      false.  Else returns true.
    */
    private static boolean includeMethod(HMethod m) {
	return m.getDeclaringClass() != objHClass;
    }

    /** Helper method for assembleGraph: generates a HmNode for
	methods in<code>interfce</code>. 
	<BR> requires: <code>interfce</code> is an HClass representing
	               an interface.
	<BR> effects: Iterates over the accessible methods of
	              <code>interfce</code>, accumulating them in a
		      <code>Vector</code> of <code>HmNode</code>s that
		      it returns at the end.  Methods of
		      superinterfaces are included, but the methods of
		      Object are not. 
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
	
	// this sequence of code apparently is unnecessary according
	// to the documentation for getMethods()...don't know how I
	// missed that before
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
	String name;
	HClass[] paramTypes;
	int hash;
	
	HmNode( HMethod hm ) {
	    name = hm.getName();
	    paramTypes = hm.getParameterTypes();
	    hash = name.hashCode() ^ hm.getDescriptor().hashCode();
	}
	
	public int hashCode() { return hash; }

	public boolean equals(Object o) {
	    if (o instanceof HmNode) {
		HmNode n = (HmNode) o;
		return n.hash == this.hash;
	    } else {
		return false;
	    }
	}

	public String toString() {
	    return "HMethod Node [ " + name + " " + hash +" ]";
	}
    }

    
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

    private class SlotColor extends Color {
	int index;
	SlotColor(int i) { index = i; }
    }

    
}

