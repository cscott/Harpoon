// Thinning.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/** {@link Thinning} is a {@link Node} which reduces lines which are
 *  more than one pixel thick to lines that are one pixel thick.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>> 
 */
public class Thinning extends Node {

    /** The number of iterations of thinning to go through. */
    private static final int iterations = 40;

    public static final int RED = 0;
    public static final int GREEN = 1;
    public static final int BLUE = 2;
    public static int defaultColorChannel = GREEN;
    private int colorChannel;

    /** Construct a {@link Thinning} node.
     *
     *  @param out The node that receives thinned images.
     */
    public Thinning(Node out) {
	super(out);
	init(defaultColorChannel);
    }
    
    public Thinning(int colorChannel, Node out) {
	super(out);
	init(colorChannel);
    }

    private void init(int colorChannel) {
	this.colorChannel = colorChannel;
    }

    /** Thin the lines in an image. 
     *
     *  @param id The image to thin.
     */
    public void process(ImageData id) {
	//System.out.println("Thinning image #"+id.id);
	int changed=1;
	byte[] in;
	//System.out.println("inside thinning");
	if (colorChannel == RED) {in = id.rvals;}
	else if (colorChannel == GREEN) {in = id.gvals;}
	else {in = id.bvals;}
	int w = id.width;
	int h = id.height;
	byte[] out = null;
	for (int j=0;(j<iterations)&&(changed==1);j++) {
	    changed=0;
	    //commented by benji
	    //System.arraycopy(in,0,out=new byte[w*h],0,w*h);
	    for (int i=w+1;i<w*(h-1)-2;i++) {
		if (in[i]!=0) {
		    /* 0 1 2
                       3 * 4
                       5 6 7 */
		    int[] n = new int[] {in[i-w-1],in[i-w],in[i-w+1],
					 in[i-1],          in[i+1],
					 in[i+w-1],in[i+w],in[i+w+1]};
		    /*
		    if (((n[0]|n[1]|n[2])<65 && (n[5]&n[6]&n[7])==127) ||
			((n[2]|n[4]|n[7])<65 && (n[0]&n[3]&n[5])==127) ||
			((n[5]|n[6]|n[7])<65 && (n[0]&n[1]&n[2])==127) ||
			((n[0]|n[3]|n[5])<65 && (n[2]&n[4]&n[7])==127) ||

			((n[1]|n[2]|n[4])<65 && (n[3]&n[6])==127) ||
			((n[4]|n[6]|n[7])<65 && (n[1]&n[3])==127) ||
			((n[3]|n[5]|n[6])<65 && (n[1]&n[4])==127) ||
			((n[0]|n[1]|n[3])<65 && (n[4]&n[6])==127)) {
		    */
		    if (((n[0]|n[1]|n[2])==0 && (n[5]!=0) && (n[6]!=0) && (n[7]!=0)) ||
			((n[2]|n[4]|n[7])==0 && (n[0]!=0) && (n[3]!=0) && (n[5]!=0)) ||
			((n[5]|n[6]|n[7])==0 && (n[0]!=0) && (n[1]!=0) && (n[2]!=0)) ||
			((n[0]|n[3]|n[5])==0 && (n[2]!=0) && (n[4]!=0) && (n[7]!=0)) ||

			((n[1]|n[2]|n[4])==0 && (n[3]!=0) && (n[6]!=0)) ||
			((n[4]|n[6]|n[7])==0 && (n[1]!=0) && (n[3]!=0)) ||
			((n[3]|n[5]|n[6])==0 && (n[1]!=0) && (n[4]!=0)) ||
			((n[0]|n[1]|n[3])==0 && (n[4]!=0) && (n[6]!=0))) {
			changed=1;
			//commented by benji
			//out[i]=0;
			in[i]=0;
		    }
		}
	    }
	    //commented by benji
	    //in=out;
	}
	if (colorChannel == RED) {id.rvals=in;}
	else if (colorChannel == GREEN) {id.gvals=in;}
	else {id.bvals=in;}
	super.process(id);
    }
}
