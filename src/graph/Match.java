// Match.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 *
 *
 *
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Match extends Node {

    /** Construct a {@link Match} node to send matches to <code>out</code>. */
    public Match(Node out) {
	super(out);
    }

    /** <code>process</code> an image of an object and see if it matches the template. */
    public synchronized void process(ImageData id) {
    }
}
