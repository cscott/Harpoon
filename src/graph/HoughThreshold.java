// HoughTheshold.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * A {@link HoughThreshold} node thresholds a {@link Hough} <code>r vs. t</code>
 * graph.
 *
 * It can also be used to apply an arbitrary threshold to an image
 * (keeping all pixels above the threshold).
 *
 * @see Hough
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class HoughThreshold extends Node {
    private final int[] thresh, set;

    /** Construct a {@link HoughThreshold} node to apply the default threshold to a 
     *  {@link Hough} <code>r vs. t</code> graph.
     *
     *  @param rvst This is a node to receive the thresholded graph.
     */
    public HoughThreshold(Node rvst) {
	this(190, rvst);
    }

    /** Construct a {@link HoughThreshold} node to apply a threshold to a graph.
     *
     *  @param threshold This is the threshold to apply to the graph.
     *                   May be <code>-1</code> to turn off thresholding.
     *  @param rvst This is a node to receive the thresholded graph.
     */
    public HoughThreshold(int threshold, Node rvst) {
	this(threshold, threshold, threshold, rvst);
    } 

    /** Construct a {@link HoughThreshold} node to apply a threshold to a graph.
     *
     *  @param rthresh The threshold for red values of pixels.
     *                 May be <code>-1</code> to turn off thresholding.
     *  @param gthresh The threshold for green values of pixels.
     *                 May be <code>-1</code> to turn off thresholding.
     *  @param bthresh The threshold for blue values of pixels.
     *                 May be <code>-1</code> to turn off thresholding.
     *  @param rvst This is a node to receive the thresholded graph.
     */
    public HoughThreshold(int rthresh, int gthresh, int bthresh, Node rvst) {
	this(rthresh, gthresh, bthresh, 0, 0, 0, rvst);
    }

    /** Construct a {@link HoughThreshold} node to apply a threshold to a graph.
     *
     *  @param rthresh The threshold for red values of pixels.
     *                 May be <code>-1</code> to turn off thresholding.
     *  @param gthresh The threshold for green values of pixels.
     *                 May be <code>-1</code> to turn off thresholding.
     *  @param bthresh The threshold for blue values of pixels.
     *                 May be <code>-1</code> to turn off thresholding.
     *  @param rset The red value to set all pixels below the red threshold.
     *  @param gset The green value to set all pixels below the green threshold.
     *  @param bset The blue value to set all pixels below the blue threshold.
     *  @param rvst This is a node to receive the thresholded graph.
     */
    public HoughThreshold(int rthresh, int gthresh, int bthresh, 
			  int rset, int gset, int bset, Node rvst) {
	super(rvst);
	thresh = new int[] {rthresh, gthresh, bthresh};
	set = new int[] {rset, gset, bset};
    }

    /** <code>process</code> an {@link ImageData} by applying a threshold to it.
     *
     *  @param id The {@link ImageData} to process.
     */
    public synchronized void process(ImageData id) {
	int size = id.width*id.height;
	byte[][] vals = new byte[][] { id.rvals, id.gvals, id.bvals };

	for (int j=0; j<3; j++) {
	    int t = thresh[j];
	    if (t>=0) {
		int val;
		byte[] in = vals[j];
		int s = set[j];
		for (int i=0; i<size; i++) {
		    in[i] = ((val=((in[i]|256)&255))>=t)?(byte)val:(byte)s;
		}
	    }
	}
	super.process(id);
    }
}
