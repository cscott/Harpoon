// Thresholding.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 *  Determines whether a pixel in an image that has been run through edge detection
 *  is definitely part of an edge or maybe part of an edge, based on intensity
 *  thresholds. 
 *
 *  @see RobertsCross
 *
 *  @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Thresholding extends Node {
    /* Is there a good heuristic for finding these???? */
    private int T1; /* Magic number - is an edge */
    private int T2; /* Another magic number - maybe an edge */  

    /** Create a new {@link Thresholding} node which will determine whether
     *  an edge should be considered "maybe" or "definite".
     *
     *  Uses default values for the thresholds.
     *
     *  @param out the node to send thresholded images to.
     */
    public Thresholding(Node out) {
	this(100, 100, out); /* Completely arbitrary defaults! */
	//this(50, 50, out);
    }

    /** Create a new {@link Thresholding} node which will determine whether
     *  an edge should be considered "maybe" or "definite".
     *  
     *  @param T1 this and above are definitely edges.
     *  @param T2 this and above are maybe edges.
     *  @param out the node to send thresholded images to.
     */
    public Thresholding(int T1, int T2, Node out) {
	super(out);
	this.T1 = T1;
	this.T2 = T2;
    }


    /** Threshold an image by determining whether edges are "maybe" or "definite".
     */
    public void process(ImageData id) {
	byte[] in = id.gvals;
	//first, run through image and find max/min average
	int length = in.length;
	int sum = 0;
	int max = -1; //smaller than smallest possible value of 0
	int min = 300; //larger than largest possible value of 255
	int val;
	int sampleCount = 0;
	for (int count = 0; count < length; count++) {
	    val = (in[count]|256)&255;
	    if (val > 25) {
		sum += val;
		if (val > max)
		    max = val;
		if (val < min)
		    min = val;
		sampleCount++;
	    }
	}
	
	int avg = sum/sampleCount;
	T1 = (max + 3*avg)/4;
	//System.out.println("\nThresholding: ");
	//System.out.println("Samps: "+sampleCount);
	//System.out.println("avg: "+avg);
	//System.out.println("max: "+max);
	//System.out.println("min: "+min);
	
	for (int i = 0; i<id.width*id.height; i++) {
	    in[i] = (((in[i]|256)&255)>=T1)?(byte)127:
		((((in[i]|256)&255)>=T2)?(byte)64:0);
	}
	super.process(id);
    }
}
