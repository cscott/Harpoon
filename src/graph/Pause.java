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

    public static final int defaultPauseEvery = 1;
    public static final double defaultLength = -1.0;
    private double length;
    private int pauseEvery;
    private int count;

    /** Construct a {@link Pause} node that allows stepping through images.
     *
     *  @param out The node to send images to.
     */
    public Pause(Node out) {
	super(out);
	init(defaultLength, defaultPauseEvery);
    }

    public Pause(double length, int pauseEvery, Node out) {
	super(out);
	init(length, pauseEvery);
    }

    private void init(double length, int pauseEvery) {
	this.length = length;
	this.pauseEvery = pauseEvery;
	this.count = 0;
    }
    
    /** <code>process</code> an image by sending it to the next node, then
     *  asking for input from {@link System}<code>.in</code>.
     *
     *  @param id The image to process.
     */

    public synchronized void process(ImageData id) {
	this.count++;
	if (count == pauseEvery) {
	    count = 0;
	    if (length < 0) {
		try {
		    System.out.println("Hit [ENTER] to continue...");
		    System.in.read();
		} catch (Exception e) {
		    throw new Error(e);
		}
	    }
	    else {
		try {
		    Thread.currentThread().sleep((int)(length*1000));
		}
		catch (InterruptedException e) {
		}
	    }
	}
	super.process(id);
	
    }
}
