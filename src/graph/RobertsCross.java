// RobertsCross.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/** {@link RobertsCross} implements line recognition using Robert's Cross operator.
 *  The input is 24-bit color, the output is 8-bit grey scale (stored in the green channel).
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class RobertsCross extends Node {
    /** Construct a new {@link RobertsCross} node that will send
     *  images to <code>out</code>.  
     */
    public RobertsCross(Node out) {
	super(out);
    }

    /** Apply line recognition to an {@link ImageData} and pass the
     *  result onto <code>out</code>.  
     */
    public synchronized void process(ImageData id) {
	int width = id.width;
	int height = id.height;
	byte[] outs = new byte[width*height];
	byte[][] vals = new byte[][] {id.rvals, id.gvals, id.bvals}; 
	for (int i=0; i<(width*(height-1)-1); i++) {
	    int out = 0;
	    for (int j = 0; j<3; j++) {
		byte[] val = vals[j];
		out = Math.max(out, 
			       Math.abs(((val[i]|256)&255)-
					((val[i+width+1]|256)&255))+
			       Math.abs(((val[i+1]|256)&255)-
					((val[i+width]|256)&255)));
	    }
	    outs[i] = (byte)Math.min(out*2, 255);
	}
	id.gvals = outs;		     
	id.bvals = id.rvals = new byte[width*height];
	super.process(id);
    }
}
