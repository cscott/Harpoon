// Grab.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * {@link Grab} is a node which grabs the result of a string of nodes
 * and allows the user to invoke <code>grab</code> to examine the {@link ImageData}
 * that was passed to <code>process</code> by the in-node.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Grab extends Node {
    private ImageData id = null;

    /** Create a new {@link Grab} node. */
    public Grab() {
	super();
    }

    /** Called by in-node to '<code>process</code>' <code>id</code> by storing it
     *  in a place that can be later examined using <code>grab</code>.
     */
    public synchronized void process(ImageData id) {
	this.id = id;
    }
    
    /** Examine the <code>id</code> stored by the last <code>process</code> invocation. 
     */
    public ImageData grab() {
	return id;
    }
}
