// Decompress.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.CODEC;

/**
 *  Decompress all images passing through the {@link Node} using the given {@link CODEC}.
 * 
 *  @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Decompress extends Node {

    private CODEC codec;

    /** Construct a {@link Decompress} node that decompresses images that flow through it.
     *
     *  @param codec A {@link CODEC} to decompress images.
     *  @param out The {@link Node} to send decompressed images to.
     */
    public Decompress(CODEC codec, Node out) {
	super(out);
	this.codec = codec;
    }

    /** Process an image by decompressing it.
     * 
     *  @param id The {@link ImageData} to decompress.
     */
    public synchronized void process(ImageData id) {
	super.process(codec.decompress(id));
    }
}
