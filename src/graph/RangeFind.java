// RangeFind.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 *  This node determines the distance (and direction) the target is from the camera.
 *
 *  @see Alert
 *
 *  @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class RangeFind extends Node {

    /** Construct a {@link RangeFind} node to send annotated images to <code>out</code>.
     *
     *  @param out The node to send annotated images to.
     */
    public RangeFind(Node out) {
	super(out);
    }

    /** <code>process</code> an image of a target by annotating it with a vector
     *  <c1, c2, c3> to the target.
     *
     *  @param id The image to find the range of and annotate.
     */
    public synchronized void process(ImageData id) {
	id.c1 = 1; /* Calculate the correct vector from the location in the original image, */
	id.c2 = 2; /* the size of the target and image, the angle of the camera, etc. */
	id.c3 = 3;
	super.process(id);
    }
}
