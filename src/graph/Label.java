// Label.java, created by wbeebee
//             modified by benster
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;
import imagerec.util.CommonMemory;
import imagerec.util.ObjectTracker;
import imagerec.util.ObjectInfo;
import imagerec.util.ObjectDataPoint;

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
	super(outImage, outImages);
	//defaults assuming 320x240 images
	init(33, 300, 33, 200, 0, 1200, 3.);
    }

    /**
     * Default setting for pipeline 1.
     */
    public final static int DEFAULT1 = 0;
    /**
     * Default setting for pipeline 2.
     */
    public final static int DEFAULT2 = 1;

    
    /** Construct a new {@link Label} node which will trace the outlines
     *  of objects and retain all objects with a bounding box that fits
     *  within a default range.
     *
     *  @param defaultNumber Specifies the particular set of internal defaults
     *                       to use for this {@link Label} node.
     *  @param outImage Node to send the labelled composite to.
     *                  If <code>null</code>, no composite is sent.
     *  @param outImages Node to send individual labelled objects to.
     *                   If <code>null</code>, no individual objects are sent.
     */
    public Label(int defaultNumber, Node outImage, Node outImages) {
	super(outImage, outImages);
	
	if (defaultNumber == DEFAULT1) {
	    init(38, 78, 23, 69, 77, 129, 3.);
	}
	else if (defaultNumber == DEFAULT2) {
	    init(33, 300, 33, 200, 0, 1200, 3.);
	}
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
	init(minwidth, maxwidth, minheight, maxheight, minsum, maxsum, maxratio);
    }

    /** 
     *  Method should be called by all constructors to initialize object fields.
     *
     *  @param minwidth The minimum width of the bounding box for a target object.
     *  @param maxwidth The maximum width of the bounding box for a target object.
     *  @param minheight The minimum height of the bounding box for a target object.
     *  @param maxheight The maximum height of the bounding box for a target object.
     *  @param minsum The minimum sum of width and height.
     *  @param maxsum The maximum sum of width and height.
     *  @param maxratio The maximum value of width/height and height/width
     */
    private void init(int minwidth, 
		 int maxwidth, int minheight, int maxheight,
		 int minsum, int maxsum, double maxratio) {
	this.minwidth = minwidth;
	this.maxwidth = maxwidth;
	this.minheight = minheight;
	this.maxheight = maxheight;
	this.minsum = minsum;
	this.maxsum = maxsum;
	this.maxratio = maxratio;
    }

    /**
     * Added for backwards compatibility with Wes' original pipeline.
     * If <code>true</code>, then <code>process()</code> only labels
     * a single object. If <code>false</code>, then <code>process()</code> labels
     *  multiple objects.
     */
    protected boolean findOneObject = false;
    
    /**
       Select whether you want to only find one object in processed images.
       If this method is not called, the default behavior is to find multiple objects.
    */
    public void findOneObject(boolean b) {
	findOneObject = b;
    }

    /**
     * The {@link ObjectTracker} variable in {@link CommonMemory}
     * that <code>process()</code> should update. If this field is <code>null</code>,
     * then <code>process()</code> will not update any {@link CommonMemory} variable.
     *
     * @see ObjectTracker
     * @see CommonMemory
     */
    protected ObjectTracker objectTracker = null;
    
    /**
     * Specify the {@link ObjectTracker} variable in {@link CommonMemory}
     * that <code>process()</code> should update with information about labeled objects.<br><br>
     *
     * If no {@link ObjectTracker} with the specified name has been instantianted
     * when this method is called, then one is created.
     *
     * You may disable object tracking by passing <code>null</code>.
     *
     * @param name The name of the {@link ObjectTracker} variable in {@link CommonMemory}.
     *
     * @throws RuntimeException Thrown if the specified name is already bound in
     * {@link CommonMemory} to an object that is NOT an {@link ObjectTracker}.
     *
     * @see ObjectTracker
     * @see CommonMemory
     */
    public void setObjectTracker(String name) {
	if (CommonMemory.valueExists(name)) {
	    Object o = CommonMemory.getValue(name);
	    if (!(o instanceof ObjectTracker)) {
		throw new RuntimeException("An object named '"+name+"' that is NOT an ObjectTracker already "+
					   "exists in CommonMemory.");
	    }
	    this.objectTracker = (ObjectTracker)o;
	}
	else {
	    this.objectTracker = new ObjectTracker();
	    CommonMemory.setValue(name, this.objectTracker);
	}
    }

    private int x1, x2, y1, y2;

    /** <code>process</code> an image by tracing the outlines of objects,
     *  checking whether they're in the bounding box ranges, and sending them
     *  on to the appropriate nodes.
     *
     *  @param id The image to label.
     */
    //public synchronized void process(ImageData id) {
    public void process(ImageData id) {

	int minWidthAdjust, maxWidthAdjust, minHeightAdjust, maxHeightAdjust, minSumAdjust, maxSumAdjust;
	double maxRatioAdjust;
	minWidthAdjust = (int)(minwidth * id.width/320.);
	maxWidthAdjust = (int)(maxwidth * id.width/320.);
	minHeightAdjust = (int)(minheight * id.width/320.);
	maxHeightAdjust = (int)(maxheight * id.width/320.);
	minSumAdjust = (int)(minsum * id.width/320.);
	maxSumAdjust = (int)(maxsum * id.width/320.);
	maxRatioAdjust = maxratio;


	byte num=127;
	//System.out.println("inside label");
	for (int pos=0; pos<id.gvals.length; pos++) {
	    if ((id.bvals[pos]==0)&&(id.gvals[pos]==127)) {
		//x1, x2 range from 0 to width
		//y1, y2 range from 0 to height
		x1 = x2 = pos%id.width;
		y1 = y2 = pos/id.width;
		//System.out.println("x1,x2 = "+x1+","+x2);
		//System.out.println("y1,y2 = "+y1+","+y2);
				
		//label() call will change value of x1, x2, y1, y2
		label(id, num, pos);
		//width,height are the dimensions of the object discovered by label()
		int width = x2-x1;
		int height = y2-y1;
		//System.out.println("Width: "+width);
		//System.out.println("Height:"+height);
		//System.out.println("");
		//if object size exceeds given ranges for width and height
		//then do nothing
		if ((width<minWidthAdjust)||(width>maxWidthAdjust)||
		    (height<minHeightAdjust)||(height>maxHeightAdjust)||
		    ((width+height)<minSumAdjust)||((width+height)>maxSumAdjust)||
		    ((width/height>maxRatioAdjust)||(height/width)>maxRatioAdjust)) {
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
		    //System.out.println("Label: found a valid object");
		    //System.out.println("  width: "+width);
		    Node right = getRight();
		    //System.out.println("Label: right: "+right);
		    if (right!=null) {
			ImageData newImage = ImageDataManip.crop(id, x1, y1, x2-x1+1, y2-y1+1);
			newImage.labelID = num;
			//System.out.println("About to handleTracking");
			if (this.objectTracker != null) {
			    handleTracking(newImage);
			}
			//System.out.println("label: calling right");
			//System.out.println("Label: ");
			//System.out.println("  width: "+newImage.width);
			//System.out.println("  height: "+newImage.height);
			//System.out.println("  x: "+newImage.x);
			//System.out.println("  y: "+newImage.y);
			right.process(newImage);
		    }
		    if ((--num)==0) {
			throw new Error("Too many objects!");
		    }
		    //To process only one object.
		    //This line must be below '--num'
		    if (findOneObject)
			break;
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
	    //System.out.println("label: calling left");
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

    private void handleTracking(ImageData croppedImageData) {
	//System.out.println("Label: Handling tracking");
	//System.out.println("       id.target# = "+croppedImageData.trackedObjectUniqueID);
	int trackingID = croppedImageData.trackedObjectUniqueID;
	//If trackingID == -1, then we know that Label was not
	//searching for a known object.
	if (trackingID == -1) {
	    ObjectInfo info =
		this.objectTracker.getClosestObject(croppedImageData.x,
						    croppedImageData.y,
						    croppedImageData.width,
						    croppedImageData.height);
	    if (info != null) {
		//System.out.println("     Closest object found.");
		ObjectDataPoint data = info.getLastDataPoint();
		int curXLoc = data.getX() + data.getWidth()/2;
		int curYLoc = data.getY() + data.getHeight()/2;
		int xLoc = croppedImageData.x + croppedImageData.width/2;
		int yLoc = croppedImageData.y + croppedImageData.height/2;
		if ((Math.abs(xLoc-curXLoc) < 30) &&
		    (Math.abs(yLoc-curYLoc) < 30)) {
		    croppedImageData.trackedObjectUniqueID = 
			info.getUniqueID();
		    info.addDataPoint(croppedImageData);
		}
		else {
		    //System.out.println("   Object not determined to be close enough");
		    //System.out.println("      ImageData #: "+croppedImageData.id);
		    //System.out.println("      xLoc-curXLoc: "+Math.abs(xLoc-curXLoc));
		    //System.out.println("      yLoc-curYLoc: "+Math.abs(yLoc-curYLoc));
		    ObjectInfo newInfo = new ObjectInfo();
		    newInfo.addDataPoint(croppedImageData);
		    croppedImageData.trackedObjectUniqueID =
			newInfo.getUniqueID();
		    this.objectTracker.addObjectInfo(newInfo);
		    
		}
	    }
	    else {
		//System.out.println("   No closest objet found");
		ObjectInfo newInfo = new ObjectInfo();
		newInfo.addDataPoint(croppedImageData);
		croppedImageData.trackedObjectUniqueID =
		    newInfo.getUniqueID();
		this.objectTracker.addObjectInfo(newInfo);
	    }
	    
	}
    }
}
