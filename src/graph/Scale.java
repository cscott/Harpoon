// Scale.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

/**
 * Scales incoming images to the specified width and height.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Scale extends Node {
    private int width;
    private int height;

    /** Construct a {@link Scale} node that will scale the incoming
     *  images to the specified width and height.
     *
     *  @param width The new width of outgoing images.
     *  @param height The new height of outgoing images.
     *  @param out The node to send scaled images to.
     */
    public Scale(int width, int height, Node out) {
	super(out);
	this.width = width;
	this.height = height;
    }

    /** <code>process an image by scaling it to the specified width
     *  and height. 
     * 
     *  @param id The image to scale.
     */
    public void process(ImageData id) {
	super.process(ImageDataManip.scale(id, width, height));
    }

}
