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
    public void process(ImageData id) {
	//System.out.println("Robert's Crossing Image #"+id.id);
	int width = id.width;
	int height = id.height;
	byte[] outs = new byte[width*height];
	
	//does not duplicate image data's arrays
	byte[][] vals = new byte[][] {id.rvals, id.gvals, id.bvals};

	for (int i=0; i<(width*(height-1)-1); i++) {
	    int out = 0;
	    //Cycle through image data colors (R, G, then B)
	    //Find the maximum gradient of each color, then store
	    //the result (scaled by a constant) in the outs[] array.
	    for (int j = 0; j<3; j++) {
		//val contains image data for single color
		byte[] val = vals[j];

		//javac does not inline Math.abs or Math.max
		//so this stuff was added for efficiency
		int val1;
		int val2;
		val1 = ((val[i]|256)&255)-((val[i+width+1]|256)&255);
		val1 = (val1 > 0)?val1:-val1;
		val2 = ((val[i+1]|256)&255)-((val[i+width]|256)&255);
		val2 = (val2 > 0)?val2:-val2;
		val1 = val1+val2;
		out = (out > val1)?out:val1;
		//the above code is equivalent to code below
		//out = Math.max(out,
		//	       Math.abs(((val[i]|256)&255)-
		//			((val[i+width+1]|256)&255))+
		//	       Math.abs(((val[i+1]|256)&255)-
		//			((val[i+width]|256)&255)));
	    }
	    //javac does not inline the Math.min function call
	    outs[i] = (byte)((out*2 < 255)?out*2:255);
	    //outs[i] = (byte)Math.min(out*2, 255);
	}
	id.gvals = outs;		     
	id.bvals = id.rvals = new byte[width*height];
	super.process(id);
    }
}
