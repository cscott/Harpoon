// Compress.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.CODEC;

/**
 *  Compresses all images passing through the {@link Node} using the given {@link CODEC}.
 *
 *  @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Compress extends Node {

    private CODEC codec;

    /** Construct a {@link Compress} node that compresses images that flow through it.
     *
     *  @param codec A {@link CODEC} to compress images.
     *  @param out The {@link Node} to send compressed images to.
     */
    public Compress(CODEC codec, Node out) {
	super(out);
	this.codec = codec; 
    }

    /** Process an image by compressing it.
     *
     *  @param id The {@link ImageData} to compress.
     */
    public void process(ImageData id) {
	super.process(codec.compress(id));
    }
}
