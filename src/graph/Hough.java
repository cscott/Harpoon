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

    public synchronized void process(ImageData id) {
	long[] accumulator = new long[precision*precision+1];
	int width = id.width;
	int height = id.height;
	int size = accumulator.length;
	for (int pos=0; pos<id.gvals.length; pos++) {
	    double x = (double)(pos%width-(width/2));
	    double y = (double)(pos/width-(height/2));
	    int val = (id.gvals[pos]|256)&255;
	    double scaleR = ((double)precision)/Math.sqrt(width*width+height*height);
	    double offset = ((double)precision)/2.0;
	    if (pos%10000==0) {
		System.out.println(pos);
	    }
	    for (int t=0; t<precision; t++) {
		accumulator[((int)((x*Math.cos((2*((double)t)*Math.PI)/precision)+
				    y*Math.sin((2*((double)t)*Math.PI)/precision))
				   *scaleR+offset))
			   *precision+t]+=val;
	    }
	}
	long max = -1;
	for (int i=0; i<size; i++) {
	    long val = accumulator[i];
	    if (val>max) {
		max = val;
	    }
	}
	System.out.println(max);
	byte[] gvals = new byte[size];
	double scale = 255.0/((double)max);
	for (int i=0; i<size; i++) {
	    gvals[i] = (byte)(((double)accumulator[i])*scale);
	}
	super.process(ImageDataManip.create(id, gvals, gvals, gvals,
					    precision, precision));
    }
}
