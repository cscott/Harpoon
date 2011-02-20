// Watermark.java, created by harveyj
// Copyright (C) 2003 Harvey Jones <harveyj@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;
import java.lang.Integer;

/** {@link Watermark} takes the upper left-hand corner's red value and marks it with the image's ID number 
 *  The input is 24-bit color, the output is 8-bit grey scale (stored in the green channel).
 *
 * @author Harvey Jones <<a href="mailto:harveyj@mit.edu">harveyj@mit.edu</a>>
 */

public class Watermark extends Node {
    /** Construct a new {@link Watermark} node that will send
     *  images to <code>out</code> and label images with their ID number.  
     */
    public Watermark(Node out) {
	super(out);
    }

    /** Set the top right pixel in {@link ImageData} to be the ImageData id.
     */
    public void process(ImageData id) {
	if(id != null && id.rvals!=null){
	    if(origWidth != -1 && origHeight != -1){
		if(id.width != origWidth || id.height != origHeight){
		    throw new Error("Image widths and heights are not constant.");
		}
	    } else {
		origWidth = id.width;
		origHeight = id.height;
	    }
	    id.rvals[0] = ((byte) (id.id % 255));    
	    super.process(id);
	}
    }
    protected int origWidth = -1;
    protected int origHeight = -1;
}
