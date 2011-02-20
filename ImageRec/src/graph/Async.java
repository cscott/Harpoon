// Async.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * {@link Async} is a {@link Node} which allows the "out"
 * {@link Node} to execute asynchronously with the "in" node.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Async extends Node {
    private Node out;

    /** Construct an {@link Async} to run <code>out</code> asynchronously. */
    public Async(Node out) {
	super(out);
    }

    /** This calls the <code>out</code> node asynchronously. */
    public void process(final ImageData id) {
	(new Thread() {
	    public void run() {
		Async.super.process(id);
	    }
	}).start();
    }
}
