// Node.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import java.io.Serializable;

/** The image recognition program is composed of a directed graph of nodes and edges.
 *  Each node can have any number of in-edges and can have zero, one, or two out edges. 
 *  {@link ImageData}s flow along the edges.
 *  Object references point in the direction of {@link ImageData} flow.
 *
 *  {@link Node}s are composable first-class entities.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Node implements Serializable, Runnable {
    private Node out1, out2;

    /** Construct a {@link Node} with zero out-edges. */
    public Node() {
	this(null);
    }

    /** Construct a {@link Node} with one out-edge. 
     *
     *  @param out1 The first out edge of the new {@link Node}.
     */
    public Node(Node out1) {
	this(out1, null);
    }

    /** Construct a {@link Node} with two out-edges. 
     *
     *  @param out1 The first out edge of the new {@link Node}.
     *  @param out2 The second out edge of the new {@link Node}.
     */
    public Node(Node out1, Node out2) {
	this.out1 = out1;
	this.out2 = out2;
    }

    /** Set the first out edge. 
     *
     *  @param out1 The new first out edge of <code>this</code>.
     */
    public void setLeft(Node out1) {
	this.out1 = out1;
    }

    /** Set the second out edge.
     *
     *  @param out2 The new second out edge of <code>this</code>. 
     */
    public void setRight(Node out2) {
	this.out2 = out2;
    }

    /**
       Equivalent to setLeft, but returns <code>this</code>
       so that setNode statements may be pieced together in
       a visually informative manner.
       <br><br>
       Example:
       <br>
       <code>
       Node n1 = new Node();<br>
       Node n2 = new Node();<br>
       Node n3 = new Node();<br>
       n1.linkL(n2.linkL(n3))
       </code>
       <br><br>
       
       @param out1 The new first out edge of <code>this</code>.
       @return <code>this</code>
    */
    public Node linkL(Node out1) {
	this.out1 = out1;
	return this;
    }

    /**
       Equivalent to setRight, but returns <code>this</code>
       so that setNode statements may be pieced together in
       a visually informative manner.
       <br><br>
       Example:
       <br>
       <code>
       Node n1 = new Node();<br>
       Node n2 = new Node();<br>
       Node n3 = new Node();<br>
       n1.linkR(n2.linkR(n3))
       </code>
       <br><br>
       
       @param out2 The new second out edge of <code>this</code>.
       @return <code>this</code>
    */
    public Node linkR(Node out2) {
	this.out2 = out2;
	return this;
    }

    /**
       Sets both out nodes and returns <code>this</code>
       so that setNodes statements may be pieced together in
       a visually informative manner.
       <br><br>
       Example:
       <br>
       <code>
       Node n1 = new Node();<br>
       Node n2 = new Node();<br>
       Node n3 = new Node();<br>
       Node n4 = new Node();<br>
       n1.link(n2.link(null,<br>
       &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp n3),<br>
       &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp n4)
       </code>
       <br><br>
       
       @param out1 The new first out edge of <code>this</code>.
       @param out2 The new second out edge of <code>this</code>.
       @return <code>this</code>
    */
    public Node link(Node out1, Node out2) {
	this.out1 = out1;
	this.out2 = out2;
	return this;
    }

    /** Get the first out node. 
     *
     *  @return The first out node.
     */
    public Node getLeft() {
	return out1;
    }

    /** Get the second out node. 
     *
     *  @return The second out node.
     */
    public Node getRight() {
	return out2;
    }

    /** Send an in image along any out edges. 
     * 
     *  @param id The {@link ImageData} to send.
     */
    public void process(ImageData id) {
	if (this.out1 != null) {
	    this.out1.process(id);
	}
	if (this.out2 != null) {
	    this.out2.process(id);
	}
    }

    /** Called to start image processing 
     *  Note this makes a {@link Node} a {@link java.lang.Runnable} which can
     *  be passed as logic into threads, or as logic run in <code>MemoryArea</code>s.
     */
    public void run() {
	process(null);
    }
}
