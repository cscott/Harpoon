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

    public Label(Node outImage) {
	this(outImage, null);
    }

    public Label(Node outImage, Node outImages) {
	this(outImage, outImages, 8, 100, 8, 100);
    }

    public Label(Node outImage, Node outImages, int minwidth, 
		 int maxwidth, int minheight, int maxheight) {
	super(outImage, outImages);
	this.minwidth = minwidth;
	this.maxwidth = maxwidth;
	this.minheight = minheight;
	this.maxheight = maxheight;
    }

    int x1, x2, y1, y2;

    public synchronized void process(ImageData id) {
	byte num=127;
	for (int pos=0; pos<id.gvals.length; pos++) {
	    if ((id.rvals[pos]==0)&&(id.gvals[pos]==127)) {
		x1 = x2 = pos%id.width;
		y1 = y2 = pos/id.width;
		label(id, num, pos);
// 		System.out.println("("+x1+","+y1+")-("+x2+","+y2+")");
		if (((x2-x1)<minwidth)||((x2-x1)>maxwidth)||
		    ((y2-y1)<minheight)||((y2-y1)>maxheight)) {
// 		    System.out.println("Out of bounds!");
		    for (int i=0; i<id.gvals.length; i++) {
			if (id.rvals[i]==num) {
			    /* Erase the object, since it's out of bounds. */
			    id.bvals[i]=id.rvals[i]=id.gvals[i]=0; 
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

    public void label(ImageData id, byte num, int pos) {
	if ((pos>=0)&&(pos<id.gvals.length)&&
	    (id.rvals[pos]==0)&&(id.gvals[pos]==127)) {
	    int x = pos%id.width;
	    int y = pos/id.width;
	    x1 = (x<x1)?x:x1;
	    x2 = (x>x2)?x:x2;
	    y1 = (y<y1)?y:y1;
	    y2 = (y>y2)?y:y2;

	    id.rvals[pos]=num;
    
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
