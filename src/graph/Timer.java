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
    private long total = 0;
    private long squaresTotal = 0;
    private long frames = 0;
    private long max = 0;
    private long min = Long.MAX_VALUE;
    private long cache[] = new long[300];
    private int cacheCount = 0;
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
	    long diff = time-id.time;
	    total += id.time = diff;
	    squaresTotal += Math.pow(diff, 2);
	    if (diff > max)
		max = diff;
	    if (diff < min)
		min = diff;
	    cache[cacheCount] = diff;
	    cacheCount++;
	    if(cacheCount >= cache.length){
		if (announce) {
		    if(rawOutput){
			for(int i = 0; i< cache.length; i++){
			    if (header != null) {
				System.out.print(header+": ");
			    }
			    System.out.println(cache[i]);
			}
		    } else {
			if (header != null) {
			    System.out.print(header+": ");
			}
			for(int i = 0; i < cache.length; i++){
			    System.out.print("Time (ms): " + cache[i]);
			}
			
			//line below added by Benji 
			System.out.print(" ** Avg(ms):"+(int)(getLatency()*1000));
			System.out.print(" ** Min:"+min);
			System.out.print(" ** Max:"+max);
			System.out.println(" ** StdDev:"+getStdDev());
		    }
		}
		cacheCount = 0;
	    }
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
    //public synchronized float getLatency() {
    public float getLatency() {
	return ((float)total)/(1000*((float)frames));
    }

    /**
     *  Get the standard deviation of the latency (in seconds) of the frames
     *  that have passed through this point.
     */
    //public synchronized float getStdDev() {
    public float getStdDev() {
	/* PROOF: std. dev. = sqrt(E[(x-E[x])^2])
	 *                  = sqrt(sum((x-(sum(x)/n))^2)/n)
	 *                  = sqrt(sum((x^2 - 2*(sum(x)/n)*x + (sum(x)/n)^2))/n)
	 *                  = sqrt((sum(x^2) - sum(2*(sum(x)/n)*x) + (sum((sum(x)/n)^2)))/n)
	 *                  = sqrt((sum(x^2) - (2/n)*sum(sum(x)*x) + (sum(sum(x)^2)/(n^2)))/n)
	 *                  = sqrt((sum(x^2) - (2/n)*sum(x)*sum(x) + n*(sum(x)^2)/(n^2))/n)
	 *                  = sqrt((sum(x^2) - 2*(sum(x)/n)*sum(x) + n*(sum(x)/n)^2)/n)
	 *                  = sqrt((squaresTotal - 2*avg*total + frames*avg^2)/frames)
	 */

	float avg = getLatency()*1000;
	float avgSquared = (float)Math.pow(avg, 2);
	float totalDeviation =
	    squaresTotal
	    - 2*avg*total
	    + frames*avgSquared;
	float variance = totalDeviation / frames;
	float stdDev = (float)Math.sqrt(variance);
        return stdDev ;
    }
}
