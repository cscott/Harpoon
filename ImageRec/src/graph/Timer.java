// Timer.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * Time the latency of an image passing through the image recognition pipeline
 * by stamping it with a time and then later reading that time.
 * Can also keep track of minimum, maximum, and average, and
 * standard deviation of latencies between two points in the pipeline.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Timer extends Node {
    private boolean start;
    private boolean announce;
    private boolean rawOutput;
    private long lastTimeCalled = 0;
    private StatArray latencyStats;
    private StatArray rateStats;
    private int frames;

    private String header;

    /** Create a new {@link Timer} node which can either stamp or read out 
     *  the difference between the current time and the time stamp and
     *  print useful statistics.
     *
     *  @param start Whether to start or stop the timer at this {@link Node}.
     *  @param announce Whether to print to the screen the latency.
     *  @param out The node to send images to.
     */
    public Timer(boolean start, boolean announce, Node out) {
	super(out);
	init(start, announce, null, true);
    }

    /** Create a new {@link Timer} node which can either stamp or read out
     *  the difference between the current time and the time stamp and
     *  print useful statistics.
     *
     *  @param start Whether to start or stop the timer at this {@link Node}.
     *  @param announce Whether to print to the screen the latency.
     *  @param header The string that will precede each line of statistics printed. Useful if you have more than one set of timers
     *  printing to the same output stream.
     *  @param out The node to send images to.
     */
    public Timer(boolean start, boolean announce, String header, Node out) {
	super(out);
	init(start, announce, header, true);
    }

    public Timer(boolean start, boolean announce, String header, boolean rawOutput, Node out) {
        super(out);
	init(start, announce, header, rawOutput);
    }
   
    /**
     * Method that should be called by all constructors to ensure that object fields get
     * initialized correctly.
     */
    private void init(boolean start, boolean announce, String header, boolean rawOutput) {
	this.start = start;
	this.announce = announce;
	this.header = header;
	this.rawOutput = rawOutput;
	if(!start){
	    this.latencyStats = new StatArray(header + ":Latency");
	    this.rateStats = new StatArray(header+ ":Rate");
	}
    }

    /** Either stamp, or read out the latency of the processing of 
     *  an image by the pipeline.
     *  
     *  If we're announcing, store the data in the array. If we're at 100,
     *  dump the array.
     *  @param id The image to stamp or read the latency.
     */
    //public synchronized void process(ImageData id) {
    public void process(ImageData id) {
	long time = System.currentTimeMillis();
	frames++;
	if (start) {
	    id.time = time;
	} else {
	    if(latencyStats.isFull() || rateStats.isFull()){
		if(announce){
		    latencyStats.printAll();
		    rateStats.printAll();
		}
		latencyStats.clear();
		rateStats.clear();
	    }

	    // The time since this image passed through the start node
	    latencyStats.add(time - id.time);

	    // The time since the last image passed through this node
	    if(lastTimeCalled != 0){
		rateStats.add(time - lastTimeCalled);
	    }
	}
	lastTimeCalled = time;
	super.process(id);
    }
}
