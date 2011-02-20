// HoughThin.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * A {@link HoughThin} node can thin a <code>r vs. t</code> graph
 * (or any other graph, for that matter) using a hill-climbing
 * approach to find multiple peaks.
 *
 * @see Hough
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class HoughThin extends Node {
    private final int kernelSize;
    
    /** Construct a {@link HoughThin} which will thin a graph using hill-climbing
     *  using the default kernelSize.
     *
     *  @param out The node to send thinned images to.
     */
    public HoughThin(Node out) {
	this(3, out);
    }

    /** Construct a {@link HoughThin} which will thin a graph using hill-climbing.
     *
     *  @param kernelSize The distance to look for neighbors 
     *                    (this permits climbing out of local minima).
     *  @param out The node to send thinned images to.
     */
    public HoughThin(int kernelSize, Node out) {
	super(out);
	this.kernelSize = kernelSize;
    }
    
    /** <code>process</code> an image by using hill-climbing to thin it.
     *
     *  @param id The {@link ImageData} to thin.
     */
    public void process(ImageData id) {
	byte[] in = id.gvals;
	int size = in.length;
	int w = id.width;
	int[] surround = new int[(2*kernelSize+1)*(2*kernelSize+1)];
	int k = 0;
	for (int j = -kernelSize; j<kernelSize+1; j++) {
	    for (int i = -kernelSize; i<kernelSize+1; i++) {
		surround[k++] = w*j+i;
	    }
	}

	for (int pos = 0; pos<size; pos++) {
	    recurse(in, surround, pos);
	} 
	id.bvals = id.rvals = in;
	super.process(id);
    }

    private static void recurse(byte[] in, int[] surround, int pos) {
	if (in[pos]!=0) {
	    int idx;
	    int max = -1;
	    int maxIdx = 0;
	    for (int i=0; i<surround.length; i++) {
		if (((idx = pos+surround[i])>=0)&&(idx<in.length)) {
		    int newMax = ((in[idx])|256)&255;
		    if (newMax>max) {
			max = newMax;
			maxIdx = idx;
		    }
		}
	    }
	    if (maxIdx != pos) {
		recurse(in, surround, maxIdx);
	    }

	    for (int i=0; i<surround.length; i++) {
		if (((idx=pos+surround[i])>=0)&&(idx<in.length)&&(idx!=maxIdx)) {
		    in[idx] = 0;
		}
	    }
	}
    }
}
