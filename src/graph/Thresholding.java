// Thresholding.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Thresholding extends Node {
    /* Is there a good heuristic for finding these???? */
    public int T1; /* Magic number */
    public int T2; /* Another magic number */  

    public Thresholding(int T1, int T2, Node out) {
	super(out);
	this.T1 = T1;
	this.T2 = T2;
    }

    public synchronized void process(ImageData id) {
	byte[] in = id.gvals;
	for (int i = 0; i<id.width*id.height; i++) {
	    in[i] = (((in[i]|256)&255)>=T1)?(byte)127:
		((((in[i]|256)&255)>=T2)?(byte)64:0);
	}
	super.process(id);
    }
}
