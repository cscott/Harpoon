// Timer.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * Time the latency of an image passing through the image recognition pipeline
 * by stamping it with a time and then later reading that time.
 * Can also keep track of average latency between two points in the pipeline.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Timer extends Node {
    private boolean start;
    private boolean announce;
    private long total = 0;
    private long frames = 0;
    private long max = 0;
    private long min = Long.MAX_VALUE;

    /** Create a new {@link Timer} node which can either stamp or read out 
     *  the difference between the current time and the time stamp.
     *
     *  @param start Whether to start or stop the timer at this {@link Node}.
     *  @param announce Whether to print to the screen the latency.
     *  @param out The node to send images to.
     */
    public Timer(boolean start, boolean announce, Node out) {
	super(out);
	this.start = start;
	this.announce = announce;
    }

    

    /** Either stamp, or read out the latency of the processing of 
     *  an image by the pipeline.
     *
     *  @param id The image to stamp or read the latency.
     */
    public synchronized void process(ImageData id) {
	long time = System.currentTimeMillis();
	frames++;
	if (start) {
	    id.time = time;
	} else {
	    long diff = time-id.time;
	    total += id.time = diff;
	    if (diff > max)
		max = diff;
	    if (diff < min)
		min = diff;
	    if (announce) {
		System.out.print("Time (ms): "+diff);
		//line below added by Benji 
		System.out.println(" ** Avg (ms) : "+(int)(getLatency()*1000));
	    }
	}
	if (id.lastImage) {
	    System.out.println("Timer: Max = "+max);
	    System.out.println("Timer: Min = "+min);
	    System.out.println("Timer: Avg = "+(int)(getLatency()*1000));
	}
	
	super.process(id);
    }

    /** Get the total amount of latency in milliseconds of all frames that
     *  passed through this point. 
     */
    public long getTotal() {
	return total;
    }

    /** Get the total number of frames that have passed through this point.
     */
    public long getFrames() {
	return frames;
    }

    /** Get the average latency of frames that have passed through this point
     *  in seconds.
     */
    public synchronized float getLatency() {
	return ((float)total)/(1000*((float)frames));
    }
}
