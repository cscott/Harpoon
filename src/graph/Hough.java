// Hough.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * A {@link Hough} node can recognize lines in an image.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Hough extends Node {
    /** Contruct a {@link Hough} node to recognize lines and send the lines to <code>out</code>. */
    public Hough(Node out) {
	super(out);
    }

    public synchronized void process(ImageData id) {
	super.process(id);
    }
}
