// Copy.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

/** A {@link Copy} node explicitly copies the input to the output.
 *
 *  @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Copy extends Node {
    /** Construct a new {@link Copy} node that copies input to output. 
     * 
     *  @param out The node to send copied images to.
     */
    public Copy(Node out) {
	this(out, null);
    }

    /** Construct a new {@link Copy} node that copies input to first output.
     *
     *  @param out1 The node to send the copy to.
     *  @param out2 The node to send the original to (after copying).
     */
    public Copy(Node out1, Node out2) {
	super(out1, out2);
    }

    /** Process an image by copying it. 
     *
     *  @param id The image to copy.
     */
    public void process(ImageData id) {
	Node n;
	if ((n=getLeft()) != null) {
	    n.process(ImageDataManip.clone(id));
	}
	if ((n=getRight()) != null) {
	    n.process(id);
	}
    }
}
