// Hough.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

/**
 * A {@link Hough} node can recognize lines in an image.
 *
 * Warning: this is really too slow to use in an image recognition pipeline.
 * It can be used to prepare database images for matching, however...
 *
 * It's theta(width*height*precision).
 *
 * Use after {@link RobertsCross} edge detection.
 *
 * @see RobertsCross HoughThreshold HoughThin DeHough 
 * @see HoughPoly DeHoughPoly PolyMatch
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Hough extends Node {
    /** Construct a {@link Hough} node to recognize lines.
     * 
     *  The Hough transform maps (x,y) points to (r, theta) points where
     *  x*cos(theta) + y*sin(theta) = r is the equation of a line.
     *
     *  Thus, the accumulated value of each (r, theta) point represents the 
     *  degree to which a line is detected with the above equation.
     *
     *  Uses the default precision.
     *
     *  @param rvst The node to receive the graph of r vs. t.
     */
    public Hough(Node rvst) {
	this(300, rvst);
    }

    private final int precision;

    /** Construct a {@link Hough} node to recognize lines.
     *
     *  @param precision The width and height of the rvst graph.
     *  @param rvst The node to receive the graph of r vs. t.
     */
    public Hough(int precision, Node rvst) {
	super(rvst);
	this.precision = precision;
    }

    public void process(ImageData id) {
	//accumulator array represents the r. vs. theta graph
	//values stored by array will the the sum of the 
	long[] accumulator = new long[precision*precision+1];
	int width = id.width;
	int height = id.height;
	int size = accumulator.length;
	double scaleR = ((double)precision)/Math.sqrt(width*width+height*height);
	double offset = ((double)precision)/2.0;
	System.out.println("Must calculate "+id.gvals.length+" positions.");
	for (int pos=0; pos<id.gvals.length; pos++) {
	    //x ranges from (-width/2 to width/2)
	    //y ranges from (-height/2 to height/2), since id.gvals.length/width == height
	    //x sweeps from -width/2 to width/2, then y increments 1, etc, etc...
	    double x = (double)(pos%width-(width/2));
	    double y = (double)(pos/width-(height/2));

	    //val is the value of the green channel of the image
	    //the |256)&255 operations force the byte value
	    //to be interpreted as an unsigned int
	    //For example, a byte value = "0b10000000"
	    //would be interpreted as -128, however the operators
	    //set and then clear a 9th bit, thereby casting the variable
	    //to an int, and it is instead interpreted as 128, which was the
	    //original intent all-along.  We are just saving Java from itself.
	    int val = (id.gvals[pos]|256)&255;

	    if (pos%10000==0) {
		System.out.println("Calclating for gval["+pos+"]");
	    }

	    //populates the accumulator (r. vs theta) array
	    // r and theta values are normalized to lie between 0 and precision
	    //cos/sin operands range from 0 to 2*pi,
	    //with the total # of intermediate steps == precision
	    for (int t=0; t<precision; t++) {
		accumulator[((int)((x*Math.cos((2*((double)t)*Math.PI)/precision)+
				    y*Math.sin((2*((double)t)*Math.PI)/precision))
				   *scaleR+offset))
			   *precision+t]+=val;
		           //precision multiplier scales value between 0 and precision
		           //t addition 
	    }
	}
	//find maximum value in accumulator array, store in max
	long max = -1;
	for (int i=0; i<size; i++) {
	    long val = accumulator[i];
	    if (val>max) {
		max = val;
	    }
	}

	System.out.println(max);

	//scale all accumulated values so that they range from 0 to 255
	//store new values in gvals
	byte[] gvals = new byte[size];
	double scale = 255.0/((double)max);
	for (int i=0; i<size; i++) {
	    gvals[i] = (byte)(((double)accumulator[i])*scale);
	}
	super.process(ImageDataManip.create(id, gvals, gvals, gvals,
					    precision, precision));
    }
}
