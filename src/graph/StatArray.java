// StatArray.java, created by harveyj
// Copyright (C) 2003 Harvey Jones <harveyj@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * StatArray is a wrapper around a preallocated array. It keeps running totals
 * in order to facilitate statistics fuctions, and it will give the minimum value
 * passed, the maximum value passed, and the standard deviation. It will also dump its
 * values to stdout.
 *
 * This is an abstraction of code taken from Timer.java
 *
 * @author Harvey Jones <<a href="mailto:harveyj@mit.edu">harveyj@mit.edu</a>>
 */

public class StatArray {
    private static final int CACHE_SIZE = 300;

    private long total = 0;
    private long squaresTotal = 0;
    private long numAdds = 0;
    private long max = 0;
    private long min = Long.MAX_VALUE;
    private long cache[] = new long[CACHE_SIZE];
    private int cacheCount = 0;

    private String header;
    /** Create a new {@link StatArray} which can print useful statistics.
     * 
     */
    public StatArray(String header) {
	this.header = header;
    }

    public void add(long data){
	if(cacheCount>=CACHE_SIZE){
	    throw new ArrayIndexOutOfBoundsException("Tried to add too many items to the cache.");
	}

	if(max < data)  max = data;
	if(min > data)  min = data;
	total += data;
	squaresTotal += data*data;
	cache[cacheCount] = data;
	cacheCount++;
	numAdds++;
    }

    /** Prints all of the cached numbers to stdout */
    public synchronized void printAll(){
	for(int i = 0; i< cacheCount; i++){
	    //	    System.out.print(i+"th number:");
	    if (header != null) {
		System.out.print(header+":num: ");
		System.out.flush();
	    }
	    System.out.println(cache[i]);
	    System.out.flush();
	}
	System.out.println(header + ":Avg: "+(int)(getAverage()*1000));
	System.out.flush();
       	System.out.println(header +":Min: "+min);
	System.out.flush();
	System.out.println(header + ":Max: "+max);
	System.out.flush();
	System.out.println(header + ":StdDev: "+getStdDev());
	System.out.flush();
    }

    /** Clears the array */
    public void clear(){
	cacheCount = 0;
    }

    /** Get the total amount of latency in milliseconds of all numbers that
     *  passed through this point. 
     */
    public long getTotal() {
	return total;
    }

    /** Get the number of times that something has been added to the array.
     */
    public long getNumAdds() {
	return numAdds;
    }

    /** Get the average latency of frames that have passed through this point
     *  in seconds.
     */
    public float getAverage() {
	return ((float)total)/(1000*((float)numAdds));
    }

    /**
     *  Get the standard deviation of the latency (in seconds) of the frames
     *  that have passed through this point.
     */
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

	float avg = getAverage()*1000;
	float avgSquared = (float)Math.pow(avg, 2);
	float totalDeviation =
	    squaresTotal
	    - 2*avg*total
	    + numAdds*avgSquared;
	float variance = totalDeviation / numAdds;
	float stdDev = (float)Math.sqrt(variance);
        return stdDev;
    }

    public boolean isFull(){
	return cacheCount == CACHE_SIZE; 
    }
}
