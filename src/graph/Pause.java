// Pause.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * Allows stepping of input images by pausing before sending each image.
 * You may specify whether a {@link Pause} node waits for user input before
 * proceeding or simply blocks for a fixed amount of time.
 * You may also specify that this {@link Pause} node blocks for every
 * kth image it receives.<br><br>
 *
 * The default behavior of this class is to pause for each image received
 * and wait for user input.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Pause extends Node {

    /**
       Default number of images to less pass before pausing.
    */
    private static final int defaultPauseEvery = 1;
    /**
       Default length of time, in seconds to block upon receiving an image.
       A default value less than 0 means that the {@link Pause} node will
       wait for user input.
    */
    private static final double defaultLength = -1.0;
    /**
       The length of time (in seconds) this {@link Pause} node should wait when
       blocking. A value less than 0 indicates that this {@link Pause} node
       should wait for user input before continuing. If <code>length</code> is
       <code>0</code>, then this {@link Pause} node will effectively not block at all.
    */
    private double length;
    /**
       Indicates the {@link Pause} node should block every specified # of images.
       For example, if <code>pauseEvery</code> is <code>5</code>, then
       this {@link Pause} node will allow image #'s 0, 1, 2, and 3 to pass
       while blocking on image #'s 4, 9, 13, etc..
       If <code>pauseEvery <= 0</code>, then this {@link Pause} node will not block.
    */
    private int pauseEvery;
    /**
       Internal variable that counts number of images passing through
       between pauses.
    */
    private int count;

    /** Construct a {@link Pause} node that allows stepping through images.
     *
     *  @param out The node to send images to.
     */
    public Pause(Node out) {
	super(out);
	init(defaultLength, defaultPauseEvery);
    }

    /**
       Construct a {@link Pause} node that allows stepping though images.
       @param length The length of time to wait during a pause.
       @param pauseEvery Indicates this {@link Pause} node should block every <pauseEvery> images.
    */
    public Pause(double length, int pauseEvery, Node out) {
	super(out);
	init(length, pauseEvery);
    }

    /**
       This method should be called by all constructors to initialize object fields.
    */
    private void init(double length, int pauseEvery) {
	this.length = length;
	this.pauseEvery = pauseEvery;
	this.count = 0;
    }
    
    /** <code>process</code> an image by pausing, then sending the image to the out {@link Node}.
     *
     *  @param id The image to process.
     */

    //public synchronized void process(ImageData id) {
    public void process(ImageData id) {
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
