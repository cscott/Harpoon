// Label.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

/**
 * {@link Label} labels objects in an image, given outlined edges.
 * It first recursively follows each edge, then determines the 
 * bounding box of each object.  If the bounding box is within the
 * specified range, the object is sent to the second out node.
 *
 * An overview of all of the objects (labelled) is sent to the first out node.
 * 
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Label extends Node {
    private int minwidth, maxwidth, minheight, maxheight;

    /** Construct a new {@link Label} node which will trace the outlines
     *  of objects and retain all objects with a bounding box that fits
     *  within a default range.
     *
     *  @param outImage Node to send the labelled composite to.
     *                  If <code>null</code>, no composite is sent.
     */
    public Label(Node outImage) {
	this(outImage, null);
    }

    /** Construct a new {@link Label} node which will trace the outlines
     *  of objects and retain all objects with a bounding box that fits
     *  within a default range.
     *
     *  @param outImage Node to send the labelled composite to.
     *                  If <code>null</code>, no composite is sent.
     *  @param outImages Node to send individual labelled objects to.
     *                   If <code>null</code>, no individual objects are sent.
     */
    public Label(Node outImage, Node outImages) {
	this(outImage, outImages, 8, 100, 8, 100);
    }

    /** Construct a new {@link Label} node which will trace the outlines
     *  of objects and retain all objects with a bounding box that fits
     *  within the specified range.
     *
     *  @param outImage Node to send the labelled composite to.
     *                  If <code>null</code>, no composite is sent.
     *  @param outImages Node to send individual labelled objects to.
     *                   If <code>null</code>, no individual objects are sent.
     *  @param minwidth The minimum width of the bounding box for a target object.
     *  @param maxwidth The maximum width of the bounding box for a target object.
     *  @param minheight The minimum height of the bounding box for a target object.
     *  @param maxheight The maximum height of the bounding box for a target object.
     */
    public Label(Node outImage, Node outImages, int minwidth, 
		 int maxwidth, int minheight, int maxheight) {
	super(outImage, outImages);
	this.minwidth = minwidth;
	this.maxwidth = maxwidth;
	this.minheight = minheight;
	this.maxheight = maxheight;
    }

    private int x1, x2, y1, y2;

    /** <code>process</code> an image by tracing the outlines of objects,
     *  checking whether they're in the bounding box ranges, and sending them
     *  on to the appropriate nodes.
     *
     *  @param id The image to label.
     */
    public synchronized void process(ImageData id) {
	byte num=127;
	for (int pos=0; pos<id.gvals.length; pos++) {
	    if ((id.bvals[pos]==0)&&(id.gvals[pos]==127)) {
		x1 = x2 = pos%id.width;
		y1 = y2 = pos/id.width;
		label(id, num, pos);
		if (((x2-x1)<minwidth)||((x2-x1)>maxwidth)||
		    ((y2-y1)<minheight)||((y2-y1)>maxheight)) {
// 		    System.out.println("Out of bounds!");
		    for (int i=0; i<id.gvals.length; i++) {
			if (id.bvals[i]==num) {
			    /* Erase the object, since it's out of bounds. */
			    id.bvals[i]=id.bvals[i]=id.gvals[i]=0; 
			}
		    }
		} else {
		    if ((--num)==0) {
			throw new Error("Too many objects!");
		    }
		    Node right = getRight();
		    if (right!=null) {
			right.process(ImageDataManip.crop(id, x1, y1, x2-x1+1, y2-y1+1));
		    }
		}
	    }
	}
	Node left = getLeft();
	if (left!=null) {
	    left.process(id);
	}
    }

    private void label(ImageData id, byte num, int pos) {
	if ((pos>=0)&&(pos<id.gvals.length)&&
	    (id.bvals[pos]==0)&&(id.gvals[pos]==127)) {
	    int x = pos%id.width;
	    int y = pos/id.width;
	    x1 = (x<x1)?x:x1;
	    x2 = (x>x2)?x:x2;
	    y1 = (y<y1)?y:y1;
	    y2 = (y>y2)?y:y2;

	    id.bvals[pos]=num;
    
	    label(id, num, pos-id.width-1);
	    label(id, num, pos-id.width);
	    label(id, num, pos-id.width+1);
	    label(id, num, pos-1);
	    label(id, num, pos+1);
	    label(id, num, pos+id.width-1);
	    label(id, num, pos+id.width);
	    label(id, num, pos+id.width+1);
	}
    }
}
