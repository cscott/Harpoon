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
 *
 * {@link Label} labels objects in an image, given outlined edges.
 * It first recursively follows each edge, then determines the 
 * bounding box of each object. If the bounding box is within the
 * specified range, the object is sent to the second out node.<br><br>
 *
 * An overview of all of the objects (labeled) is sent
 * to the first out node.<br><br>
 *
 * This class has two behaviors, depending on whether object tracking
 * is enabled. Object tracking is enabled by calling
 * <code>myLabelObject.setObjectTracker(String)</code> and providing
 * a string by which an {@link ObjectTracker} may be accessed in
 * {@link CommonMemory}.<br><br>
 *
 * If object tracking is not enabled: <br>
 *    *  Each object that is found is cropped around and
 *    sent, one at a time, along the right (2nd) out node.< br>
 *    *  After all objects have been sent, the full labeled image,
 *    featuring all the labeled pixels, is sent to the
 *    left (1st) out node.<br>
 *<br>
 * If object tracking is enabled: <br>
 *    *  When an object is discovered, it's location is compared
 *    against the location of objects discovered in previous
 *    frames. If the locations are close enough, the object
 *    is assumed to be the same object as in the previous frame,
 *    and the new object is tagged with the same unique tracking ID
 *    as the previous object.<br>
 *    *  A counter associated with that tracked object's ID is incremented.
 *    If the counter reaches a certain threshold, and if
 *    that object's counter is greater than all other counters
 *    then the object is tagged with a <code>Command.GO_LEFT</code>
 *    command, indicating that it should be further analyzed (usually
 *    asynchronously).<br> The counter is reset.
 *    {@link Label} then sets an internal flag and will not
 *    tag any other objects with <code>Command.GO_LEFT</code> until the
 *    results of this asychronous analysis are returned.<br>
 *    *  The results returned should be reported by either calling
 *    <code>myLabelObject.confirm(int)</code> or
 *    <code>myLabelObject.deny(int)</code>. This response
 *    indicates whether the object should be considered a tank
 *    and should generate alerts. Currently, only one tracked
 *    object may be considered a tank at a time.<br>
 *    *  If an object is designated as a tank, then
 *    the object is tagged with a <code>Command.GO_RIGHT</code>
 *    command, indicating it should generate alerts.<br>
 *    *  An object is only tagged with <code>Command.GO_RIGHT</code>
 *    once every <code>timeBetweenSends</code> milliseconds,
 *    limiting the number of alerts generated. (Note, this
 *    class does not actually generate alerts, it only
 *    indicates that objects should generate alerts further
 *    down the pipeline.<br>
 *    *  If an object should be tagged with both commands, then
 *    it is tagged with <code>Command.GO_BOTH</code> instead.
 *    *  If an object satisfies neither of these conditions,
 *    then its tracking data is updated and the object is
 *    not passed on.<br>
 * <br>
 *    
 *
 * THIS CLASS HAS IMAGE SIZE DEPENDENCIES!!.
 * 
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Label extends Node {
    private int minwidth, maxwidth, minheight, maxheight, minsum, maxsum;

    private double maxratio;

    /**
     * The maximum pixel distance which an object
     * may move from image to image before it is
     * assumed to be another object.<br><br>
     * 
     * THIS VALUE SHOULD BE DEPENDENT ON IMAGE SIZE,
     * BUT IT CURRENTLY IS NOT!!
     */
    private static final int trackingTolerance = 45;

    /**
     * The running tally of images received that
     * did not contain any objects.
     */
    private int numberOfImagesWithNoTank = 0;

    /**
     * The minimum time between when objects 
     * are tagged to indicate that an alert should be
     * generated.
     */
    private long timeBetweenSends = 1000;

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
     *  on to the appropriate nodes.<br><br>
     *
     *  The behavior of this method depends greatly on whether
     *  tracking is enabled.
     *
     *  @param id The image to label.
     */
    public void process(ImageData id) {
	//System.out.println("Label.process()");
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
			boolean sendOn = true;
			if (this.objectTracker != null) {
			    sendOn = handleTracking(newImage);
			}
			//System.out.println("label: calling right");
			//System.out.println("Label: ");
			//System.out.println("  width: "+newImage.width);
			//System.out.println("  height: "+newImage.height);
			//System.out.println("  x: "+newImage.x);
			//System.out.println("  y: "+newImage.y);
			if (sendOn)
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

    /**
     * Handles all tracking processing. See the class description
     * for an explaination.
     *
     * @param croppedImageData The {@link ImageData}, cropped
     * around an object, that should be processed for tracking.
     * 
     * @return true If the specified {@link ImageData} should
     * be passed on to further {@link Node}s.
     * @return false If the specified {@link ImageData} should
     * not be passed on to further {@link Node}s.
     */
    private boolean handleTracking(ImageData croppedImageData) {
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
		if ((Math.abs(xLoc-curXLoc) < trackingTolerance) &&
		    (Math.abs(yLoc-curYLoc) < trackingTolerance)) {
		    //System.out.println("     Object satisfyed distance constraints");
		    croppedImageData.trackedObjectUniqueID = 
			info.getUniqueID();
		    //System.out.println("        Unique ID# = "+info.getUniqueID());
		    info.addDataPoint(croppedImageData);
		    Pair targetPair = getTarget(croppedImageData.trackedObjectUniqueID);
		    if (targetPair == null)
			System.out.println("Label: WHAT THE FUCK?????");
		    int maxCount = maxCount();
		    if ((!waitingForResponse) && (targetPair.getCount() == maxCount)) {
			//System.out.println("Target "+croppedImageData.trackedObjectUniqueID+" reconfirm");
			targetPair.resetCount();
			if (croppedImageData.trackedObjectUniqueID == currentSelectedTrackingID) {
			    long currentTime = System.currentTimeMillis();
			    if (currentTime - targetPair.getTime() > timeBetweenSends) {
				croppedImageData.command = Command.GO_BOTH;
				targetPair.setTime(currentTime);
			    }
			    else
				croppedImageData.command = Command.GO_LEFT;
			}
			else {
			    croppedImageData.command = Command.GO_LEFT;
			}
			waitingForResponse = true;
			return true;
		    }
		    targetPair.inc();
		    //System.out.println("Target "+croppedImageData.trackedObjectUniqueID+" auto-confirm");
		    if (croppedImageData.trackedObjectUniqueID == currentSelectedTrackingID) {
			long currentTime = System.currentTimeMillis();
			if (currentTime - targetPair.getTime() > timeBetweenSends) {
			    croppedImageData.command = Command.GO_RIGHT;
			    targetPair.setTime(currentTime);
			    return true;
			}
			else {
			    return false;
			}
		    }
		    else
			return false;
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
		    addTarget(croppedImageData.trackedObjectUniqueID);
		    //System.out.println("Label: new Target added #"+croppedImageData.trackedObjectUniqueID);
		    if (!waitingForResponse) {
			croppedImageData.command = Command.GO_LEFT;
			waitingForResponse = true;
			return true;
		    }
		    else
			return false;
		}
	    }
	    //[info == null]
	    else {
		//System.out.println("   No closest objet found");
		ObjectInfo newInfo = new ObjectInfo();
		newInfo.addDataPoint(croppedImageData);
		croppedImageData.trackedObjectUniqueID =
		    newInfo.getUniqueID();
		this.objectTracker.addObjectInfo(newInfo);
		addTarget(croppedImageData.trackedObjectUniqueID);
		//System.out.println("Label: new Target added #"+croppedImageData.trackedObjectUniqueID);
		if (!waitingForResponse) {
		    croppedImageData.command = Command.GO_LEFT;
		    waitingForResponse = true;
		    return true;
		}
		else
		    return false;
	    }
	}
	//[trackingID != -1]
	else {
	    if (trackingID == currentSelectedTrackingID) {
		System.out.print("Label: THIS SHOULD NEVER HAPPEN");
		return true;
	    }
	    else
		return false;
	}
    }

    /**
     * Informs this {@link Label} node that the
     * tracked object with the specified ID
     * is a tank.
     *
     * @param trackedObjectUniqueID The ID of the
     * object that should be considered a tank.
     */
    void confirm(int trackedObjectUniqueID) {
	currentSelectedTrackingID = trackedObjectUniqueID;
	waitingForResponse = false;
    }

    
    /**
     * Informs this {@link Label} node that the
     * tracked object with the specified ID
     * is not a tank.
     *
     * @param trackedObjectUniqueID The ID of the
     * object that should not be considered a tank.
     */
    void deny(int trackedObjectUniqueID) {
	if (currentSelectedTrackingID == trackedObjectUniqueID)
	    currentSelectedTrackingID = -1;
	waitingForResponse = false;
    }

    /**
     * The head of the linked-list structure
     * that contains this {@link Label} node's
     * trackingID/counter {@link Label.Pair}s.
     */
    private Pair head = null;
    /**
     * The tracking ID of the object that
     * is currently considered a tank, if any.<br>
     * <br>
     * A value of <code>-1</code> indicates that
     * no object is currently considered a tank.
     */
    private int currentSelectedTrackingID = -1;
    /**
     * Indicates whether this {@link Label} node
     * is currently waiting for a response as to
     * whether a particuar tracked object is a tank
     * or not.<br><br>
     *
     * While this is <code>true</code>, this {@link Label}
     * node may not send out any other objects for
     * further processing of this type.
     */
    private boolean waitingForResponse;

    /**
     * Creates a new objectID/counter {@link Label.Pair}
     * and adds it to the linked-list maintained by this class.
     *
     * @param trackedObjectUniqueID The object ID that the
     * new counter should be associated with.
     */
    private Pair addTarget(int trackedObjectUniqueID) {
	Pair newPair = new Pair(trackedObjectUniqueID, head);
	head = newPair;
	return newPair;
    }

    /**
     * Searches the linked-list maintained by this
     * class and returns the {@link Label.Pair} whose
     * object ID matches the one specified.
     *
     * @param targetID The object ID whose {@link Label.Pair}
     * should be returned.
     *
     * @return The {@link Label.Pair} containing the specified
     * object ID.
     * @return null If no {@link Label.Pair} containing the
     * specified ID exists.
     */
    private Pair getTarget(int targetID) {
	Pair currentPair = head;
	while (currentPair != null) {
	    //System.out.println("GetTarget while loop: targetID:"+currentPair.targetID);
	    if (currentPair.targetID == targetID) {
		break;
	    }
	    currentPair = currentPair.next;
	}
	return currentPair;
    }

    /**
     * Returns the largest value among
     * all the object counters stored
     * in the linked-list maintained by this class.
     */
    private int maxCount() {
	int maxCount = 0;
	Pair currentPair = head;
	while (currentPair != null) {
	    if (currentPair.getCount() > maxCount)
		maxCount = currentPair.getCount();
	    currentPair = currentPair.next;
	}
	return maxCount;
    }

    /**
     * A linked-list element that associates
     * a unique integer ID with a counter.<br>
     * <br>
     * The counters keep track of how many
     * times the object with the associated ID
     * has been seen in previous images, allowing
     * for a simple priority scheduler to be implemented
     * that governs when and how often tracked objects
     * are sent on for further analysis.
     */
    private class Pair {
	/**
	 * The ID of the object for which
	 * this information is being kept.
	 */
	int targetID;
	/**
	 * The number of times this object has been
	 * seen in previous images. This value
	 * is usually reset from time to time.
	 */
	int count;
	/**
	 * The system time at which this object was
	 * last sent out for further analysis.
	 */
	long lastSentTime;
	/**
	 * The next {@link Label.Pair} in the the
	 * linked-list structure.
	 */
	Pair next;
	/**
	 * Creates a new pair that will keep track
	 * of how often this object is seen.
	 *
	 * @param targetID The ID of the object
	 * whose information will be kept.
	 * @param next The {@link Label.Pair} that
	 * this object should point to
	 * in the linked-list structure maintained
	 * by {@link Label}.
	 */
	Pair(int targetID, Pair next) {
	    init(targetID, next);
	}
	/**
	 * This method should be called by all constructurs
	 * to properly initialize object fields.
	 *
	 * @param targetID The ID of the object
	 * whose information will be kept.
	 * @param next The {@link Label.Pair} that
	 * this object should point to
	 * in the linked-list structure maintained
	 * by {@link Label}.
	 */
	private void init(int targetID, Pair next) {
	    this.targetID = targetID;
	    this.next = next;
	    this.count = 0;
	    this.lastSentTime = 0;
	}
	
	/**
	 * Increment the counter for this {@link Label.Pair}.
	 */
	void inc() {
	    count++;
	}

	/**
	 * Reset the counter for this {@link Label.Pair}.
	 */
	void resetCount() {
	    count = 0;
	}
	
	/**
	 * Get the value of the counter for this {@link Label.Pair}.
	 */
	int getCount() {
	    return count;
	}
	
	/**
	 * Set the time value kept by this {@link Label.Pair}.
	 *
	 * @param t The new time value to be stored by this {@link Label.Pair}.
	 */
	void setTime(long t) {
	    lastSentTime = t;
	}

	/**
	 * Get the time value kept by this {@link Label.Pair}.
	 */
	long getTime() {
	    return lastSentTime;
	}
    }

    /**
     * Sets the minimum time between when objects 
     * are tagged to indicate that an alert should be
     * generated.
     */
    public void setTimeBetweenSends(long t) {
	timeBetweenSends = t;
    }
    
    /**
     * Returns the minimum time between when objects 
     * are tagged to indicate that an alert should be
     * generated.
     */
    public long getTimeBetweenSends() {
	return timeBetweenSends;
    }
}
