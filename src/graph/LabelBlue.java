// LableBlue.java, created by benster
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

public class LabelBlue extends Node {
    protected int finalChannel;
    public static final int RED = 10;
    public static final int BLUE = 11;
    public static final int GREEN = 12;
    protected static final int defaultFinalChannel = BLUE;

    protected int threshold1;
    protected int threshold2;
    protected static final int defaultThreshold1 = 20;
    protected static final int defaultThreshold2 = 10;

    protected static final int minWidth = 10;
    protected static final int maxWidth = 480;
    protected static final int minHeight = 10;
    protected static final int maxHeight = 480;

    protected Node fullImage;

    public LabelBlue(Node fullImage, Node out) {
	super(out);
	init(defaultFinalChannel, defaultThreshold1, defaultThreshold2, fullImage);
    }

    public LabelBlue(int colorChannel, Node fullImage, Node out) {
	super(out);
	init(colorChannel, defaultThreshold1, defaultThreshold2, fullImage);
    }

    public LabelBlue(int threshold1, int threshold2, Node fullImage, Node out) {
	super(out);
	init(defaultFinalChannel, threshold1, threshold2, fullImage);
    }

    public LabelBlue(int colorChannel, int threshold1, int threshold2, Node fullImage, Node out) {
	super(out);
	init(colorChannel, threshold1, threshold2, fullImage);
    }
    
    protected void init(int colorChannel, int threshold1, int threshold2, Node fullImage) {
	this.finalChannel = colorChannel;
	this.threshold1 = threshold1;
	this.threshold2 = threshold2;
	this.fullImage = fullImage;
    }

    private int xMin, xMax, yMin, yMax;
    private int labelNumber;
    
    private ImageData imageData;
    private int width, height, length;
    private int wPlusH;

    //pixels may have one of several values:
    //0 - not yet seen, treshold not yet calculated
    //1 - seen, did satisfy lower threshold, but algorithm wasn't tracking at that point
    //2 - seen, did not satisfy lower threshold
    //3+, seen, and identified as part of an object
    private byte[] labels;
    private byte[] rvals, gvals, bvals;

    protected int calcDiff(int r, int g, int b) {
	return (int)(1.*b-r-g);
	//return b-r-g;
    }

    public void process(ImageData imageData) {
	labelNumber = 255;
	this.imageData = imageData;
	width = imageData.width;
	height = imageData.height;
	wPlusH = width+height;
	length = imageData.bvals.length;
	if (labels == null || labels.length != length)
	    labels = new byte[length];
	else
	    for(int count = 0; count < length; count++)
		labels[count] = (byte)0;
	rvals = imageData.rvals;
	gvals = imageData.gvals;
	bvals = imageData.bvals;
	int lCount = 0;
	//first find maximum blue diff and average blue diff
	int sumDiff = 0;
	int maxDiff = -1000;
	int minDiff = 1000;
	//used to be 10000
	int numberOfSamples = length/10;
	int point;
	int rval, bval, gval, diff;
	//one method of random sampling, doesn't guarentee samples from the whole image
	/*
	for (int count = 0; count < numberOfSamples; count++) {
	    point = (int)(Math.random()*length);
	    rval = (rvals[point]|256)&255;
	    gval = (gvals[point]|256)&255;
	    bval = (bvals[point]|256)&255;
	    diff = calcDiff(rval, gval, bval);
	    sumDiff += diff;
	    if (diff > maxDiff)
		maxDiff = diff;
	    if (diff < minDiff)
		minDiff = diff;
	}
	int avgDiff = sumDiff/numberOfSamples;
	*/
	//this sampling method steps through the image at regular intervals and
	//takes a jittered random sample centered around each step
	int sampleStep = 4;
	int halfStep = sampleStep/2;
	int jitter;
	for (int count = halfStep; count < length - halfStep; count += sampleStep) {
	    jitter = (int)(Math.random()*sampleStep - halfStep);
	    point = count+jitter;
	    rval = (rvals[point]|256)&255;
	    gval = (gvals[point]|256)&255;
	    bval = (bvals[point]|256)&255;
	    diff = calcDiff(rval, gval, bval);
	    sumDiff += diff;
	    if (diff > maxDiff)
		maxDiff = diff;
	    if (diff < minDiff)
		minDiff = diff;
	}
	int avgDiff = sumDiff/(length/sampleStep);
	threshold1 = (5*maxDiff + avgDiff)/6;
	threshold2 = (2*maxDiff + avgDiff)/3;
	/*
	System.out.println("maxDiff: "+ maxDiff);
	System.out.println("minDiff: "+minDiff);
	System.out.println("avgDiff: "+ avgDiff);
	System.out.println("Threshold1: "+threshold1);
	System.out.println("Threshold2: "+threshold2);
	*/
	boolean done = false;
	int dw = 2;
	int mainXMin=width+1, mainXMax=-1, mainYMin=height+1, mainYMax=-1;
	for (int hCount = 0; hCount < height; hCount++) {
	    for (int wCount = 0; wCount < width; wCount+=dw, lCount+=dw) {
		xMin = xMax = wCount;
		yMin = yMax = hCount;
		//this function modifies imageData, xMin, xMax, yMin, yMax
		label(wCount, hCount, lCount, false, 0);
		int curW = xMax - xMin;
		int curH = yMax - yMin;
		//if object is a good size
		if ((curW >= minWidth) && (curW <= maxWidth) && (curH >= minHeight) && (curH <= maxHeight)) {
		    labelNumber--;
		    if (xMin < mainXMin) mainXMin = xMin;
		    if (xMax > mainXMax) mainXMax = xMax;
		    if (yMin < mainYMin) mainYMin = yMin;
		    if (yMax > mainYMax) mainYMax = yMax;
		    //System.out.println("Found object: w = "+curW+"  h = "+curH);
		    if (labelNumber == 2) {
			done = true;
			break;
		    }
			
		}
		if (done)
		    break;
	    }
	}
	//if no objects are found
	if (labelNumber == 255) {
	    mainXMin = 0;
	    mainXMax = 0;
	    mainYMin = 0;
	    mainYMax = 0;
	    System.out.println("******** NO OBJECTS FOUND *************");
	}
	//now that pixels in 'labels' have been labeled, copy that to the requested channel
	byte[] channel1;
	byte[] channel2;
	if (finalChannel == RED) {
	    imageData.rvals = labels;
	    channel1 = imageData.bvals;
	    channel2 = imageData.gvals;
	    //imageData.bvals = new byte[length];
	    //imageData.gvals = new byte[length];
	}
	else if (finalChannel == GREEN) {
	    imageData.gvals = labels;
	    channel1 = imageData.bvals;
	    channel2 = imageData.rvals;
	    //imageData.bvals = new byte[length];
	    //imageData.rvals = new byte[length];
	}
	else {
	    imageData.bvals = labels;
	    channel1 = imageData.gvals;
	    channel2 = imageData.rvals;
	    //imageData.gvals = new byte[length];
	    //imageData.rvals = new byte[length];
	}

	for (int count = 0; count < length; count++) {
	    channel1[count] = (byte)0;
	    channel2[count] = (byte)0;
	}

	//super.process(ImageDataManip.crop(imageData, 0, 0, 100, 100));
	//System.out.println("xMin: "+mainXMin+"  xMax: "+mainXMax+"  yMin: "+mainYMin+"  yMax: "+mainYMax);
	if (this.fullImage != null)
	    this.fullImage.process(imageData);
	super.process(ImageDataManip.crop(imageData, mainXMin, mainYMin, mainXMax-mainXMin+1, mainYMax-mainYMin+1));
    }

    protected void label(int curX, int curY, int curL, boolean tracking, int depth) {	
	//invariant: curY*width+curX == curL
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
	    //if below lower threshold, then mark as seen
	    if (diff < threshold2)
		labels[curL] = (byte)2;
	    else {
		//now we know pixel is above lower threshold
		//if we are tracking, then good enough
		if (tracking)
		    good = true;
		//if not tracking and pixel is above high threshold, then good
		else if (diff >= threshold1)
		    good = true;
		//pixel must be between low and high thresholds, but we are not tracking,so mark for later use
		else
		    labels[curL] = (byte)1;
	    }
	}
	//if diff is above appropriate threshold, mark as blue and continue
	if (good) {
	    //System.out.println("bval-rval-gval  = "+diff+" depth: "+depth);
	    if (curX < xMin) xMin = curX;
	    if (curX > xMax) xMax = curX;
	    if (curY < yMin) yMin = curY;
	    if (curY > yMax) yMax = curY;
	    labels[curL] = (byte)labelNumber; //assign this pixel the current label
	    /*
	    label(curX-1, curY-1, curL-1-width, true, depth+1);
	    label(curX, curY-1, curL-width, true, depth+1);
	    label(curX+1, curY-1, curL+1-width, true, depth+1);
	    label(curX-1, curY, curL-1, true, depth+1);
	    label(curX+1, curY, curL+1, true, depth+1);
	    label(curX-1, curY+1, curL-1+width, true, depth+1);
	    label(curX, curY+1, curL+width, true, depth+1);
	    label(curX+1, curY+1, curL+1+width, true, depth+1);
	    */
	    label(curX-2, curY-2, curL-2-width*2, true, depth+1);
	    label(curX, curY-2, curL-width*2, true, depth+1);
	    label(curX+2, curY-2, curL+2-width*2, true, depth+1);
	    label(curX-2, curY, curL-2, true, depth+1);
	    label(curX+2, curY, curL+2, true, depth+1);
	    label(curX-2, curY+2, curL-2+width*2, true, depth+1);
	    label(curX, curY+2, curL+width*2, true, depth+1);
	    label(curX+2, curY+2, curL+2+width*2, true, depth+1);
	}
    }
}
