// LabelBlue.java, created by benster
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;
import imagerec.util.CommonMemory;

import java.util.Arrays;

/**
   A {@link LabelBlue} node takes a full color image, and based on calibrated thresholds, determines what
   parts of the image are the most blue. Pixels that are determined to be blue are colored, and
   all other pixels are erased. The a copy of the image is then cropped such that it contains
   all discovered blue areas from the original, and the cropped
   image is finally passed to the next {@link Node}. The 'blueness' of a pixel is determined
   using all three color channels.<br><br>

   In the event that no acceptible blue is found, then the <code>process()</code> method simply
   returns and does not 

   The default behavior of the <code>process()</code> method is to use each image it receives
   to calibrate its thresholds, perform the search for blue, then pass the
   resulting image onto the next {@link Node}. However, you can also instruct
   the method to calibrate using a single image, then use that calibration for
   all subsequent images.<br><br>

   This is done by calling <code>myLabelBlue.calibrateAndProcessSeparately(true)</code>,
   where <code>myLabelBlue</code> is the name of a {@link LabelBlue} node. In this mode,
   processed {@link ImageData}s that are tagged with <code>Command.CALIBRATION_IMAGE</code> will
   be used for calibration and those with any other tag will be processed. Calibration images
   will not be passed on to the next {@link Node}.<br><br>

   A {@link LabelBlue} node is also capable of "returning" a value (after the <code>process()</code> method)
   by setting <code>true</code> or <code>false</code> to a value in {@link CommonMemory}.
   If the value is set to <code>true</code>, then this {@link LabelBlue} node found blue in the image.
   If the value is set to <code>false</code>, then the node found no blue.<br><br>

   By default, {@link LabelBlue} does not set any variable in {@link CommonMemory},
   but you may tell it to do so by calling the <code>setCommonMemory()</code> method
   and specifiying the name of any {@link CommonMemory} variable.<br><br>

   Remember that the boolean value of the variable in {@link CommonMemory}
   is actually stored in a {@link Boolean} object wrapper.


   @see Command
   @see CommonMemory
   @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
*/
public class LabelBlue extends Node {
    /**
       Specifies the color channel on which all labeling should be stored.
    */
    protected int finalChannel;
    /**
       Indicates the red color channel.
    */
    public static final int RED = 10;
    /**
       Indicates the blue color channel.
    */
    public static final int BLUE = 11;
    /**
       Indicates the green color channel.
    */
    public static final int GREEN = 12;
    /**
       The default color channel on which labeling will be stored.
    */
    protected static final int defaultFinalChannel = BLUE;


    /**
       Determines what calibration mode the object is in.
       If <code>false</code>, then this {@link LabelBlue} object
       calibrates on each provided image. If <code>true</code>, then this
       {@link LabelBlue} object calibrates only on images tagged with
       <code>Command.CALIBRATION_IMAGE</code>.

       @see Command
    */
    protected boolean calibrateAndProcessSeparately = false;

    /**
       An individual patch of blue in the image will not be considered significant
       if its width is smaller than <code>minWidth</code>.
    */
    protected static final int minWidth = 10;
    /**
       An individual patch of blue in the image will not be considered significant
       if its width is larger than <code>maxWidth</code>.
    */
    protected static final int maxWidth = 480;
    /**
       An individual patch of blue in the image will not be considered significant
       if its height is smaller than <code>minHeight</code>.
    */
    protected static final int minHeight = 10;
    /**
       An individual patch of blue in the image will not be considered significant
       if its height is larger than <code>maxHeight</code>.
    */
    protected static final int maxHeight = 480;

    /**
       The Node to which an uncropped version of the processed {@link ImageData} will be passed.
     */
    protected Node fullImage;

    /**
       Create a new {@link LabelBlue} node with the specified children.
       @param fullImage The {@link Node} to which an uncropped version of the processed {@link ImageData} will be passed.
       @param out The {@link Node} to which the cropped version of the processed {@link ImageData} will be passed.
    */
    public LabelBlue(Node fullImage, Node out) {
	super(out);
	init(defaultFinalChannel, fullImage);
    }

    /**
       Create a new {@link LabelBlue} node with the specified children, and with the labeling data stored
       in the specified color channel.
       @param colorChannel The color channel to store the labeling data.
       @param fullImage The {@link Node} to which an uncropped version of the processed {@link ImageData} will be passed.
       @param out The {@link Node} to which the cropped version of the processed {@link ImageData} will be passed.
    */
    public LabelBlue(int colorChannel, Node fullImage, Node out) {
	super(out);
	init(colorChannel, fullImage);
    }


    /**
       Sets this node to either calibrate on each image, or calibrate once
       and use it over subsequent images. Passing <code>false</code> causes
       this object to calibrate on each image. Passing <code>true</code> causes
       this object to calibrate on images tagged with the <code>Command.CALIBRATION_IMAGE</code>
       command and process those that are not.
    */
    public void calibrateAndProcessSeparately(boolean b) {
	calibrateAndProcessSeparately = b;
    }
    
    /**
       This method should be called by all constructors to intialize object fields.
    */
    protected void init(int colorChannel, Node fullImage) {
	this.finalChannel = colorChannel;
	this.fullImage = fullImage;
    }


    /**
       The name of the variable in {@link CommonMemory} that this
       {@link LabelBlue} node will set with true or false
       before it returns. If <code>memName</code> is null,
       then this {@link LabelBlue} node will not set any
       value in {@link CommonMemory}.<br><br>
       If this variable is equal to <code>true</code>, this means that
       the <code>process()</code> method found blue in the
       image. If the variable is set to <code>false</code>, then
       no blue was found.<br><br>
       Remember that the boolean value of the variable in {@link CommonMemory}
       is actually stored in a {@link Boolean} object wrapper.
       @see CommonMemory
     */
    private String memName;

    /**
     * This method either tells the {@link LabelBlue} node to begin or stop
     * setting a "return value" in {@link CommonMemory}.
     * Specifying a name will ause the {@link LabelBlue} node to store either
     * <code>true</code> or <code>false</code> in the variable who's name you specify
     * depending on whether the <code>process()</code> method detects blue in an image.<br><br>
     *
     * By specifying a <code>null</code> name, you turn this feature off.
     * @param name The name of the variable in {@link CommonMemory} where this {@link LabelBlue}
     * node will store its "return value."
     *
     * @see CommonMemory
     */
    public void setCommonMemory(String name) {
	this.memName = name;
    }

    /**
       Calculates the level of 'blueness' based on the given values of red, green and blue.
       @param r The red value [0-255]
       @param g The green value [0-255]
       @param b The blue value [0-255]
       @return Returns the measure of 'blueness'.
    */
    protected int calcDiff(int r, int g, int b) {
	//Manually inlining this function does not change execution time
	//I'm not sure why it doesn't, because calibration routine executes this
	//function for every pixel.
	//return (int)(1.*b-r-g);
	return b-r-g;
    }

    /**
       Calibrates the upper and lower threshold values based on the specified {@link ImageData}.
       @param imageData The {@link ImageData} with which to calibrate.
    */
    protected void calibrate(ImageData imageData) {
	//first find maximum blue diff and average blue diff
	//	System.out.println("Calibrating LabelBlue node");
	byte[] rvals = imageData.rvals;
	byte[] gvals = imageData.gvals;
	byte[] bvals = imageData.bvals;

	int sumDiff = 0;
	int maxDiff = -1000;
	int minDiff = 1000;
	int length = bvals.length;
	//used to be 10000
	int numberOfSamples = length/10;
	int point;
	int rval, bval, gval, diff;

	//this sampling method steps through the image at regular intervals and
	//takes a jittered random sample centered around each step
	int sampleStep = 1;
	int halfStep = sampleStep/2;
	int jitter;
	for (int count = halfStep; count < length - halfStep; count += sampleStep) {
	    //jitter = (int)(Math.random()*sampleStep - halfStep);
	    jitter = 0; //currently no jitter
	    point = count+jitter;
	    rval = (rvals[point]|256)&255;
	    gval = (gvals[point]|256)&255;
	    bval = (bvals[point]|256)&255;
	    diff = calcDiff(rval, gval, bval);
	    //diff = bval-rval-gval;
	    sumDiff += diff;
	    if (diff > maxDiff)
		maxDiff = diff;
	    if (diff < minDiff)
		minDiff = diff;
	}
	//need a much better way of defining thresholds
	//need to know standard deviation, maybe
	int avgDiff = sumDiff/(length/sampleStep);
	imageData.blueThreshold1 = (5*maxDiff + avgDiff)/6;
	imageData.blueThreshold2 = (2*maxDiff + avgDiff)/3;
	/*
	System.out.println("maxDiff: "+ maxDiff);
	System.out.println("minDiff: "+minDiff);
	System.out.println("avgDiff: "+ avgDiff);
	System.out.println("Threshold1: "+threshold1);
	System.out.println("Threshold2: "+threshold2);
	*/
    }

    /**
       Processes the spcecified {@link ImageData} as described in the class description.<br><br>

       In the event that this method does not find any suitable blue, then it simply returns
       and does not pass the {@link ImageData} onto the next node.
       @param imageData The {@link ImageData} to process.
    */
    //public synchronized void process(ImageData imageData) {
    public void process(ImageData imageData) {
	//If this image is the calibration image,
	//then calibrate this node, and return.
	//Do not perform labeling or pass image onto subsequent nodes.
	int command = Command.read(imageData);
	if (calibrateAndProcessSeparately) {
	    if (command == Command.CALIBRATION_IMAGE) {
		calibrate(imageData);
		return;
	    }
	}
	else {
	    calibrate(imageData);
	}

	int labelNumber = 255;
	int width = imageData.width;
	int height = imageData.height;
	int length = imageData.bvals.length;
	//maxMins[0] is xMin
	//maxMins[1] is xMax
	//maxMins[2] is yMin
	//maxMins[3] is yMax
	int[] maxMins = new int[4];

	/**
	   Stores the label data. Each element corrosponds to a pixel in the image.
	   Elements may have one of several values:
	   0 - Not yet seen, 'blueness' of corrosponding pixel not yet calculated.
	   1 - Seen, 'blueness' satisfied lower threshold, but algorithm wasn't looking for low threshold at that point.
	   2 - Seen, 'blueness' did not satisfy lower threshold.
	   3+, Seen and identified as part of an object.
	*/
	byte[] labels = new byte[length];
	
	int lCount = 0;
	boolean done = false;
	int dw = 1;
	int mainXMin=width+1;
	int mainXMax=-1;
	int mainYMin=height+1;
	int mainYMax=-1;
	int hCount;
	int wCount;

	///*
	for (hCount = 0; hCount < height; hCount++) {
	    for (wCount = 0; wCount < width; wCount+=dw, lCount+=dw) {
		//xMin = xMax = wCount;
		maxMins[0] = wCount;
		maxMins[1] = wCount;
		maxMins[2] = hCount;
		maxMins[3] = hCount;

		//this function modifies labels and maxMins
		label(wCount, hCount, lCount, false, 0, imageData, labelNumber, labels, maxMins);
		

		int curW = maxMins[1] - maxMins[0];
		int curH = maxMins[3] - maxMins[2];
		//if object is a good size
		if ((curW >= minWidth) && (curW <= maxWidth) && (curH >= minHeight) && (curH <= maxHeight)) {
		    labelNumber--;
		    //if (xMin < mainXMin) mainXMin = xMin;
		    //if (xMax > mainXMax) mainXMax = xMax;
		    //if (yMin < mainYMin) mainYMin = yMin;
		    //if (yMax > mainYMax) mainYMax = yMax;
		    if (maxMins[0] < mainXMin) mainXMin = maxMins[0];
		    if (maxMins[1] > mainXMax) mainXMax = maxMins[1];
		    if (maxMins[2] < mainYMin) mainYMin = maxMins[2];
		    if (maxMins[3] > mainYMax) mainYMax = maxMins[3];
		    
		    //System.out.println("Found object: w = "+curW+"  h = "+curH);
		    if (labelNumber == 2) {
			done = true;
			break;
		    }
			
		
		}
		if (done)
		    break;
	    }
	    if (wCount > width)
		lCount -= wCount-width;
	}
	//*/

	/*
	for (hCount = 0; hCount < height; hCount++) {
	    for (wCount = 0; wCount < width; wCount+=dw, lCount+=dw) {
		//new way, no hysteresis, just one threshold
		int bval = (imageData.bvals[lCount]|256)&255;
		int gval = (imageData.gvals[lCount]|256)&255;
		int rval = (imageData.rvals[lCount]|256)&255;
		int diff = calcDiff(rval, gval, bval);
		if (diff > imageData.blueThreshold2) {
		    labels[lCount] = (byte)labelNumber;
		    if (wCount < mainXMin) mainXMin = wCount;
		    if (wCount > mainXMax) mainXMax = wCount;
		    if (hCount < mainYMin) mainYMin = hCount;
		    if (hCount > mainYMax) mainYMax = hCount;
		    labelNumber = 254;
		}
		
	    }
	    if (wCount > width)
		lCount -= wCount-width;
	}
	*/


	//if no objects are found
	if (labelNumber == 255) {
	    if (this.memName != null) {
		//System.out.println("LabelBlue found no blue.");
		CommonMemory.setValue(this.memName, new Boolean(false));
	    }
	    return;
	    //mainXMin = 0;
	    //mainXMax = 0;
	    //mainYMin = 0;
	    //mainYMax = 0;
	    //System.out.println("******** NO OBJECTS FOUND *************");
	}
	
	    if (this.memName != null) {
		//System.out.println("LabelBlue found blue.");
		CommonMemory.setValue(this.memName, new Boolean(true));
	    }
	

	//now that pixels in 'labels' have been labeled, copy that to the requested channel
	byte[] channel1;
	byte[] channel2;
	if (finalChannel == RED) {
	    imageData.rvals = labels;
	    channel1 = imageData.bvals;
	    channel2 = imageData.gvals;
	}
	else if (finalChannel == GREEN) {
	    imageData.gvals = labels;
	    channel1 = imageData.bvals;
	    channel2 = imageData.rvals;
	}
	else {
	    imageData.bvals = labels;
	    channel1 = imageData.gvals;
	    channel2 = imageData.rvals;
	}


	channel1 = new byte[length];
	channel2 = new byte[length];
	Arrays.fill(channel1, 0, length, (byte)0);
	Arrays.fill(channel2, 0, length, (byte)0);
	/*
	for (int count = 0; count < length; count++) {
	    channel1[count] = (byte)0;
	    channel2[count] = (byte)0;
	}
	*/

	//super.process(ImageDataManip.crop(imageData, 0, 0, 100, 100));
	//System.out.println("xMin: "+mainXMin+"  xMax: "+mainXMax+"  yMin: "+mainYMin+"  yMax: "+mainYMax);
	if (this.fullImage != null)
	    this.fullImage.process(imageData);
	ImageData croppedImage =
	    ImageDataManip.crop(imageData, mainXMin, mainYMin, mainXMax-mainXMin+1, mainYMax-mainYMin+1);
	croppedImage.x += imageData.x;
	croppedImage.y += imageData.y;
	super.process(croppedImage);

    }



    protected void label2(int curX, int curY, int curL, ImageData imageData, int labelNumber, byte[] labels, int[] maxMins) {
	int[] xStack = new int[100];
	int[] yStack = new int[100];
	int stackPtr = 0;
	
	
    }



    /**
       This method is called by <code>process()</code> to track and label blue pixels in the image.
       The algorithm recursivly tracks through the image, labeling as it goes and sees fit.
       @param curX The X position of the current pixel in the image we are labeling.
       @param curY The Y position of the current pixel in the image we are labeling.
       @param curL The index position of the current pixel in the image we are labeling.
       The following invariant holds true:<br>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<code>curY*<width of the image>+curX == curL</code>
       @param tracking If <code>true</code>, then if the current pixel satisfies either 'blueness' threshold, the
       algorithm will continue tracking. If <code>false</code>, then only if the current pixel satisfies the
       high 'blueness' threshold will the algorithm continue to track.
       @param depth The recursive depth we have reached. This is necessary because we need to limit the recursive
       depth of the algorithm due to {@link StackOverflowError}s.
    */
    protected void label(int curX, int curY, int curL, boolean tracking, int depth,
			 ImageData imageData, int labelNumber, byte[] labels, int[] maxMins) {	
	//invariant: curY*width+curX == curL
	/*
	if (curY*width.getInt(imageData.id)+curX != curL) {
	    System.out.println("INVARIANT FAILED!!!");
	    System.out.println("curY: "+curY);
	    System.out.println("width: "+width);
	    System.out.println("curX: "+curX);
	    System.out.println("curL: "+curL);
	    System.out.println("depth: "+depth);
	    System.exit(0);
	}
	*/

	int width = imageData.width;
	int height = imageData.height;
	int wPlusH = width+height;
	int length = imageData.bvals.length;
	byte[] rvals = imageData.rvals;
	byte[] gvals = imageData.gvals;
	byte[] bvals = imageData.bvals;

	//no need for a recursive search with depth more than the width plus the height of the image
	//this limit is necessary because of stack overflow exceptions
	if (depth > wPlusH)
	    return;
	//return if x or y is outside the boundaries of the image
	if (curX < 0 || curX >= width || curY < 0 || curY >= height)
	    return;
	int labelValue = (labels[curL]|256)&255;
	//return if this point has already been labeled or determined to not satisfy either threshold
	if (labelValue > 1)
	    return;
	boolean good = false;
	
	//All this stuff inside the if-else is written so that the diff of a pixel
	//does not get calculated and compared more than once if it doesn't need to be.
	
	//if this pixel has previously been determined to satisfy lower threshold
	//and we are tracking, then great
	if (labelValue == 1 && tracking)
	    good = true;
	else {
	    //at this point, we know diff has not been calculated
	    int bval = (bvals[curL]|256)&255;
	    int gval = (gvals[curL]|256)&255;
	    int rval = (rvals[curL]|256)&255;
	    int diff = calcDiff(rval, gval, bval);
	    //int diff = bval-rval-gval;
	    //if below lower threshold, then mark as seen
	    if (diff < imageData.blueThreshold2)
		labels[curL] = (byte)2;
	    else {
		//now we know pixel is above lower threshold
		//if we are tracking, then good enough
		if (tracking)
		    good = true;
		//if not tracking and pixel is above high threshold, then good
		else if (diff >= imageData.blueThreshold1)
		    good = true;
		//pixel must be between low and high thresholds, but we are not tracking,so mark for later use
		else
		    labels[curL] = (byte)1;
	    }
	}
	//if diff is above appropriate threshold, mark as blue and continue
	if (good) {
	    //System.out.println("bval-rval-gval  = "+diff+" depth: "+depth);
	    //if (curX < xMin) xMin = curX;
	    //if (curX > xMax) xMax = curX;
	    //if (curY < yMin) yMin = curY;
	    //if (curY > yMax) yMax = curY;
	    if (curX < maxMins[0]) maxMins[0] = curX;
	    if (curX > maxMins[1]) maxMins[1] = curX;
	    if (curY < maxMins[2]) maxMins[2] = curY;
	    if (curY > maxMins[3]) maxMins[3] = curY;
	    labels[curL] = (byte)labelNumber; //assign this pixel the current label

	    label(curX-1, curY-1, curL-1-width, true, depth+1,  imageData, labelNumber, labels, maxMins);
	    label(curX, curY-1, curL-width, true, depth+1,  imageData, labelNumber, labels, maxMins);
	    label(curX+1, curY-1, curL+1-width, true, depth+1,  imageData, labelNumber, labels, maxMins);
	    label(curX-1, curY, curL-1, true, depth+1,  imageData, labelNumber, labels, maxMins);
	    label(curX+1, curY, curL+1, true, depth+1,  imageData, labelNumber, labels, maxMins);
	    label(curX-1, curY+1, curL-1+width, true, depth+1,  imageData, labelNumber, labels, maxMins);
	    label(curX, curY+1, curL+width, true, depth+1,  imageData, labelNumber, labels, maxMins);
	    label(curX+1, curY+1, curL+1+width, true, depth+1,  imageData, labelNumber, labels, maxMins);


	    label(curX-2, curY-2, curL-2-width*2, true, depth+2, imageData, labelNumber, labels, maxMins);
	    label(curX-1, curY-2, curL-1-width*2, true, depth+2, imageData, labelNumber, labels, maxMins);
	    label(curX+0, curY-2, curL+0-width*2, true, depth+2, imageData, labelNumber, labels, maxMins);
	    label(curX+1, curY-2, curL+1-width*2, true, depth+2, imageData, labelNumber, labels, maxMins);
	    label(curX+2, curY-2, curL+2-width*2, true, depth+2, imageData, labelNumber, labels, maxMins);
	    label(curX-2, curY-1, curL-2-width*1, true, depth+2, imageData, labelNumber, labels, maxMins);
	    label(curX+2, curY-1, curL+2-width*1, true, depth+2, imageData, labelNumber, labels, maxMins);
	    label(curX-2, curY+0, curL-2+width*0, true, depth+2, imageData, labelNumber, labels, maxMins);
	    label(curX+2, curY+0, curL+2+width*0, true, depth+2, imageData, labelNumber, labels, maxMins);
	    label(curX-2, curY+1, curL-2+width*1, true, depth+2, imageData, labelNumber, labels, maxMins);
	    label(curX+2, curY+1, curL+2+width*1, true, depth+2, imageData, labelNumber, labels, maxMins);
	    label(curX-2, curY+2, curL-2+width*2, true, depth+2, imageData, labelNumber, labels, maxMins);
	    label(curX-1, curY+2, curL-1+width*2, true, depth+2, imageData, labelNumber, labels, maxMins);
	    label(curX+0, curY+2, curL+0+width*2, true, depth+2, imageData, labelNumber, labels, maxMins);
	    label(curX+1, curY+2, curL+1+width*2, true, depth+2, imageData, labelNumber, labels, maxMins);
	    label(curX+2, curY+2, curL+2+width*2, true, depth+2, imageData, labelNumber, labels, maxMins);

	}
    }
}
