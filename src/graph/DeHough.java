// DeHough.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

/**
 * A {@link DeHough} node inverts a <code>r vs. t</code> graph
 * back into a (color and size-normalized) lined graph.
 *
 * @see Hough
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class DeHough extends Node {
    private final int width, height;

    /** Construct a {@link DeHough} which will invert a <code>r vs. t</code> graph.
     *  
     *  Reconstructed image is of a default size.
     *
     *  @param out The node to send DeHoughed images to.
     */
    public DeHough(Node out) {
	this(100,100,out);
    }

    /** Construct a {@link DeHough} which will invert a <code>r vs. t</code> graph.
     *
     *  @param width The width of the new images.
     *  @param height The height of the new images.
     *  @param out The node to send DeHoughed images to.
     */
    public DeHough(int width, int height, Node out) {
	super(out);
	this.width = width;
	this.height = height;
    }

    /** <code>process</code> an image by inverting the input <code>r vs. t</code> graph
     *  into a normalized graph.
     *
     *  @param id The {@link ImageData} to invert.
     */
    public void process(ImageData id) {
	int newSize = width*height;
	byte[] in = id.gvals;
	byte[] out = new byte[newSize];
	int size = in.length;
	int precision = id.width;
	int numPoly = 0;
	for (int pos = 0; pos<size; pos++) {
	    if (in[pos]!=0) numPoly++;
	}

	double[] r = new double[numPoly];
	double[] t = new double[numPoly];
	int poly = 0;
	for (int pos = 0; pos<size; pos++) {
	    if (in[pos]!=0) {
		r[poly] = (((double)(pos/precision))/((double)(precision)))*2.0-1.0;
		t[poly++] = (((double)(pos%precision))/((double)precision))*2.0*Math.PI;
	    }
	}

	System.out.println("Poly: "+poly);
	for (int i = 0; i<poly; i++) {
	    System.out.println("("+r[i]+","+t[i]+")");
	}
	double tolerance = 1.0/((double)Math.min(width, height));
	for (int pos = 0; pos<newSize; pos++) {
	    double x = (((double)(pos%width))/((double)width))*2.0-1.0;
	    double y = (((double)(pos/width))/((double)height))*2.0-1.0;
	    for (int i = 0; i<numPoly; i++) {
		double theta = t[i];
		if (Math.abs(x*Math.cos(theta)+y*Math.sin(theta)-r[i])<tolerance) {
		    out[pos]=127;
		    break;
		}
	    }
	}
	
	super.process(ImageDataManip.create(id, new byte[newSize], out, new byte[newSize], 
					    width, height));
    }
}
