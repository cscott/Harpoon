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
    private int minwidth, maxwidth, minheight, maxheight, minsum, maxsum;
    //added by benji
    private double maxratio;

    private int numberOfImagesWithNoTank = 0;

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
	//this(outImage, outImages, 38, 78, 23, 69, 77, 129, 3.);
	this(outImage, outImages, 33, 300, 33, 200, 0, 1200, 3.);
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
     *  @param minsum The minimum sum of width and height.
     *  @param maxsum The maximum sum of width and height.
     *  @param maxratio The maximum value of width/height and height/width
     */
    public Label(Node outImage, Node outImages, int minwidth, 
		 int maxwidth, int minheight, int maxheight,
		 int minsum, int maxsum, double maxratio) {
	super(outImage, outImages);
	this.minwidth = minwidth;
	this.maxwidth = maxwidth;
	this.minheight = minheight;
	this.maxheight = maxheight;
	this.minsum = minsum;
	this.maxsum = maxsum;
	this.maxratio = maxratio;
    }

    protected boolean findOneObject = false;
    
    /**
       Select whether you want to only find one object in processed images.
       If this method is not called, the default behavior is to find multiple objects.
    */
    public void findOneObject(boolean b) {
	findOneObject = b;
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
		//x1, x2 range from 0 to width
		//y1, y2 range from 0 to height
		x1 = x2 = pos%id.width;
		y1 = y2 = pos/id.width;
		//label() call will change value of x1, x2, y1, y2
		label(id, num, pos);
		//width,height are the dimensions of the object discovered by label()
		int width = x2-x1;
		int height = y2-y1;
		//if object size exceeds given ranges for width and height
		//then do nothing
		if ((width<minwidth)||(width>maxwidth)||
		    (height<minheight)||(height>maxheight)||
		    ((width+height)<minsum)||((width+height)>maxsum)||
		    ((width/height>maxratio)||(height/width)>maxratio)) {
		    //System.out.println("Out of bounds!");

		    /*
		    for (int i=0; i<id.gvals.length; i++) {
			if (id.bvals[i]==num) {
			    // Erase the object, since it's out of bounds.
			    id.bvals[i]=id.bvals[i]=id.gvals[i]=0; 
  			}
  		    }
		    */
		}

		//else, if we found an object of a valid size, decrement num to keep track
		//of how many objects we've found,
		//crop around the object, and pass it on to the next node
		else {
		    //System.out.println("width: "+width);
		    Node right = getRight();
		    if (right!=null) {
			ImageData newImage = ImageDataManip.crop(id, x1, y1, x2-x1+1, y2-y1+1);
			newImage.labelID = num;
			right.process(newImage);
		    }
		    //To process only one object.
		    if (findOneObject)
			break;
		    if ((--num)==0) {
			throw new Error("Too many objects!");
		    }

		}
	    }
	}
	if (num == 127) {
	    numberOfImagesWithNoTank++;
	}
	if (id.lastImage) {
	    System.out.println("Label node: "+numberOfImagesWithNoTank+" images had no tank.");
	}
	//System.out.println("found "+(127-num)+" objects");
	Node left = getLeft();
	if (left!=null) {
	    left.process(id);
	}
    }

    /*
      From the starting (x,y) point specified by pos,
      recursively follow the surrounding high pixels
      in the green channel to find the boundaries of the object.

      If we decide a point belongs to this object,
      mark the corrosponding pixel in the blue channel
      with the object number (0 through 127)
    */
    private void label(ImageData id, byte num, int pos) {
	//if pos within boundaries, this pixel's blue channel has not been marked,
	//and this pixel's green channel is set high, then...
	if ((pos>=0)&&(pos<id.gvals.length)&&
	    (id.bvals[pos]==0)&&(id.gvals[pos]==127)) {
	    //find x and y corrosponding to pos 
	    int x = pos%id.width;
	    int y = pos/id.width;
	    //if this pixel is outside current boundaries,
	    //then set boundaries to include the pixel
	    x1 = (x<x1)?x:x1;
	    x2 = (x>x2)?x:x2;
	    y1 = (y<y1)?y:y1;
	    y2 = (y>y2)?y:y2;

	    id.bvals[pos]=num;
    
	    label(id, num, pos-id.width-1); //search one pixel up and to the left
	    label(id, num, pos-id.width); //search one pixel up
	    label(id, num, pos-id.width+1); //search one pixel up and to the right
	    label(id, num, pos-1); //search one pixel to the left
	    label(id, num, pos+1); //search one pixel to the right
	    label(id, num, pos+id.width-1); //search one pixel down and to the left
	    label(id, num, pos+id.width); //search one pixel down
	    label(id, num, pos+id.width+1); //search one pixel down and to the right
	    
	    //search two pixels away
	    //allow for small breaks in the outline
	    label(id, num, pos-id.width*2-2);
	    label(id, num, pos-id.width*2-1);
	    label(id, num, pos-id.width*2+0);
	    label(id, num, pos-id.width*2+1);	    
	    label(id, num, pos-id.width*2+2);
	    label(id, num, pos-id.width*1-2);
	    label(id, num, pos-id.width*1+2);
	    label(id, num, pos+id.width*0-2);
	    label(id, num, pos+id.width*0+2);
	    label(id, num, pos+id.width*1-2);
	    label(id, num, pos+id.width*1+2);
	    label(id, num, pos+id.width*2-2);
	    label(id, num, pos+id.width*2-1);
	    label(id, num, pos+id.width*2+0);
	    label(id, num, pos+id.width*2+1);
	    label(id, num, pos+id.width*2+2);
	}
    }
}
