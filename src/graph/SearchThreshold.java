// SearchThreshold.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 *
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class SearchThreshold extends Node {
    private static final double T1 = 0.9; /* Top percentile */
    private static final double T2 = 0.85; /* Bottom percentile */

    public SearchThreshold(Node out) {
	super(out);
    }

    public synchronized void process(ImageData id) {
	int t1 = 0;
	int t2 = 0;
	byte[] in = id.gvals;
	int[] scale = new int[256];
	int numPix = id.width*id.height;
	for (int i=0; i<numPix; i++) {
	    scale[(in[i]|256)&255]++;
	}
	int pixThresh1 = (int)(T1*((double)numPix));
	int pixThresh2 = (int)(T2*((double)numPix));
	int num = 0;
	for (int i=0; i<256; i++) {
	    num+=scale[i]; 
	    if (num<pixThresh1) t1=i;
	    if (num<pixThresh2) t2=i;
	}
	for (int i=0; i<numPix; i++) {
	    in[i] = (((in[i]|256)&255)>=t1)?(byte)127:((in[i]>=t2)?(byte)64:0);
	}
	super.process(id);
    }
}
