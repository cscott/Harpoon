// RangeThreshold.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * {@link RangeThreshold} will enable you to search a range of possible thresholds
 * for an input image. 
 *
 * @see Thresholding
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class RangeThreshold extends Node {

    /** Create a new {@link RangeThreshold} node which will determine the 
     *  threshold to use for "maybe" or "definite" edges.
     */
    public RangeThreshold(Node out) {
	super(out);
    }

    public synchronized void process(ImageData id) {
	byte[] orig = (byte[])id.gvals.clone();
	for (int T2 = 0; T2 < 255; T2+=5) {
	    for (int T1 = T2; T1 < 255; T1+=5) { 
		byte[] in = id.gvals;
		for (int i = 0; i<id.width*id.height; i++) {
		    in[i] = (((orig[i]|256)&255)>=T1)?(byte)127:
			((((orig[i]|256)&255)>=T2)?(byte)64:0);
		}
		System.out.println("T1: "+T1+", T2: "+T2);
		try {
		    super.process(id);
		} catch (Error e) {
		    System.out.println("  error");
		}
	    }
	}
    }

}
