// Pause.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * Allows stepping of input images by pausing after sending each image.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Pause extends Node {
    
    /** Construct a {@link Pause} node that allows stepping through images.
     *
     *  @param out The node to send images to.
     */
    public Pause(Node out) {
	super(out);
    }
    
    /** <code>process</code> an image by sending it to the next node, then
     *  asking for input from {@link System}<code>.in</code>.
     *
     *  @param id The image to process.
     */
    public synchronized void process(ImageData id) {
	//Why pause afterwards? Not intuitive.
	//I changed code so the pause comes
	//before the image is sent on to other nodes
	// -- Benji
	//super.process(id);
	try {
	    System.in.read();
	} catch (Exception e) {
	    throw new Error(e);
	}
	super.process(id);
	
    }
}
