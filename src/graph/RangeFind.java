// RangeFind.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import java.util.Arrays;


/**
 This node determines the distance to a target and the direction
 that it is facing by analyzing a single bitmap outline of the target.
 The default behavior of this class is to analyze the BLUE color channel.<br><br>
 
 The algorithm assumes that thinning has been performed on the outline so that
 it is a single pixel wide.

 @see Alert
 
 @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class RangeFind extends Node {

    /**
       Indicates the red color channel.
    */
    public static final int RED = 0;
    /**
       Indicates the green color channel.
    */
    public static final int GREEN = 1;
    /**
       Indicates the blue color channel.
    */
    public static final int BLUE = 2;
    /**
       The default color channel that the range finding algorithm operates on.
    */
    private static final int defaultColorChannel = BLUE;
    /**
       The actual color channel that the range finding algorithm will operate on.
    */
    private int colorChannel;

 
    /** Construct a {@link RangeFind} node to send annotated images to <code>out</code>.
     *
     *  @param out The node to send annotated images to.
     */
    public RangeFind(Node out) {
	super(out);
	init(defaultColorChannel);
    }
    
    /** Construct a {@link RangeFind} node to send annotated images to <code>out</code>.
     *
     *  @param colorChannel The color channel that the range finding algorithm should operate on.
     *  @param out The node to send annotated images to.
     */
    public RangeFind(int colorChannel, Node out) {
	super(out);
	init(colorChannel);
    }

    
    /**
       This method should be called by all constructors to intialize object fields.
    */
    private void init(int colorChannel) {
	this.colorChannel = colorChannel;
	int count = 0;
	double angle = Math.PI/2;
	for (count = 0; count < angleDataCapacity; count++) {
	    angleData[count] = new AngleStat(angle);
	    angle -= angleEpsilon;
	}
    }

    private byte[] vals; //specified color channel values
    private byte[] tracked; //same size as vals, marks whether pixels have been visisted
    private int shortRangeSamples; 
    private int aryCount;
    private static final double refAngleInit = 3.0; //bigger than any value that Math.atan would produce
    private int labelID;
    private AngleStat[] angleData = new AngleStat[angleDataCapacity];
    private static final double angleEpsilon = Math.PI/15;
    private static final int angleDataCapacity = (int)(Math.PI/angleEpsilon)+1;


    private void addAngleStat(double angle) {
	int size = angleData.length;
	AngleStat stat = null;
	boolean found = false;
	for (int count = 0; count < size; count++) {
	    stat = angleData[count];
	    if (Math.abs(stat.angle - angle) < angleEpsilon) {
		found = true;
		break;
	    }
	}
	if (found) {
	    stat.increment();
	}
    }

    private void resetAngleData() {
	//System.out.println("angleDataCap: "+angleDataCapacity);
	for (int count = 0; count < angleData.length; count++) {
	    angleData[count].count = 0;
	}
    }

    private class AngleStat {
	double angle;
	int count=0;
	AngleStat(double angle) {
	    this.angle = angle;
	}
	void increment() {
	    count++;
	}
    }

    /** <code>process</code> an image of a target by annotating it with a vector
     *  <c1, c2, c3> to the target.
     *
     *  @param id The image to find the range of and annotate.
     */
    public void process(ImageData id) {
	/* Calculate the correct vector from the location in the original image, */
	/* the size of the target and image, the angle of the camera, etc. */

	//System.out.println("");
/*
	//This code assumes that thinning has been run on a labeled image.
	//This should ensure line thickness of 1.
	int width = id.width;
	int height = id.height;
	int length = id.rvals.length;
	labelID = id.labelID;

	if (colorChannel == RED) {vals = id.rvals;}
	else if (colorChannel == GREEN) {vals = id.gvals;}
	else {vals = id.bvals;}

	tracked = new byte[length];

	shortRangeSamples = Math.max(width, height)/7;
	//System.out.println("RangeFind: #ofshortrangesamples: "+shortRangeSamples);
	double[] shortRangeAngles = new double[shortRangeSamples];
	int[] shortRangeDXs = new int[shortRangeSamples];
	int[] shortRangeDYs = new int [shortRangeSamples];
	aryCount = 0;
	double resultAngle = 3.0;
	resetAngleData();
	for (int index = 0; index < length; index++) {
	    //tracked, dxTotal, and dyTotal are modified by
	    //track()
	    track(id, index, 0, 0, 0, 0, 0, 0, refAngleInit, 0, shortRangeDXs, shortRangeDYs, shortRangeAngles);
	}
	
	AngleStat stat;
	//System.out.println("Angle data:");
	for (int count = 0; count < angleData.length; count++) {
	    stat = angleData[count];
	    //System.out.println(" "+count+": ("+stat.count+") "+stat.angle);
	}
	//System.out.println("width: "+id.width+"   height: "+id.height);

	//angleDataCapacity/2+1 is the maximum number of indexes this array will ever need to hold
	int[] maxIndexes = new int[angleDataCapacity/2+1];
	int curIndex = 0;
	//go through array and look for local maxima
	double maxIndex = 0;
	int maxCount = angleData[0].count;
	int thisCount;
	boolean rising = true;
	
	for (int index = 1; index < angleData.length; index++) {
	    thisCount = angleData[index].count;	    
	    if (rising) {
		if (thisCount < maxCount) {
		    //this means we've gone over a hump
		    maxIndexes[curIndex] = index-1;
		    curIndex++;
		    rising = false;
		}
		maxCount = thisCount;
		maxIndex = index;	       
	    }
	    else {
		if (thisCount > maxCount) {
		    rising = true;
		}
		maxCount = thisCount;
		maxIndex = index;
	    }		
	}

	for (int index = 0; index < curIndex; index++) {
	    int idx = maxIndexes[index];
	    int count = angleData[idx].count;
	    double angle = angleData[idx].angle;
	    //System.out.println(""+index+": ("+count+") "+angle);
	}

*/

	/* Now, just the center of the image and 0. */

	id.c1 = id.x+(id.width/2); 
	id.c2 = id.y+(id.height/2); 
	id.c3 = 0;
	super.process(id);
    }


    private void track(ImageData id, int index, int dx, int dy, int steps, int aryCount, int deltaXSum, int deltaYSum,
		       double refAngle, int angleCount, int[] oldShortRangeDXs, int[] oldShortRangeDYs, double[] oldShortRangeAngles) {
	//huge stack footprint, possible danger of stack overflow not currently handled
	
	//if pixel is within boundaries of image, is colored,
	//and has not been found yet...
	if ((index >= 0) && (index < vals.length) &&
	     (vals[index] == labelID) && (tracked[index]==0)) {
	    int[] shortRangeDXs = new int[shortRangeSamples];
	    int[] shortRangeDYs = new int[shortRangeSamples];
	    double[] shortRangeAngles = new double[shortRangeSamples];
	    System.arraycopy(oldShortRangeDXs, 0, shortRangeDXs, 0, shortRangeSamples);
	    System.arraycopy(oldShortRangeDYs, 0, shortRangeDYs, 0, shortRangeSamples);
	    System.arraycopy(oldShortRangeAngles, 0, shortRangeAngles, 0, shortRangeSamples);
	    
	    //int x = index % id.width;
	    //int y = index / id.width;
	    //System.out.println("x:"+x+"  y:"+y+"  dx:"+dx+" dy:"+dy+" refAngle:"+refAngle+"  steps:"+steps+
	    //	   "  aryCount:"+aryCount+" deltaXSum:"+deltaXSum+" deltaYSum:"+deltaYSum);
	    //if steps == 0, then dx, dy are zero, meaningless
	    if (steps != 0) {
		int nextAryCount = aryCount+1;
		if (nextAryCount >= shortRangeSamples)
		    nextAryCount = 0;
		shortRangeDXs[aryCount] = dx;
		shortRangeDYs[aryCount] = dy;
		deltaXSum += dx;
		deltaYSum += dy;

		if (steps >= shortRangeSamples) {
		    deltaXSum -= shortRangeDXs[nextAryCount];
		    deltaYSum -= shortRangeDYs[nextAryCount];
		    double angle;
		    if (deltaXSum == 0) {
			angle = Math.PI/2;
		    }
		    else {
			angle = Math.atan((double)deltaYSum/deltaXSum);
		    }
		    addAngleStat(angle);
		    /*		    
		    if (Math.abs(angle - refAngle) < angleEpsilon) {
			angleCount++;
			if (angleCount >= (int)(shortRangeSamples)) {
			    addAngleStat(angle);
			}
		    }
		    else {
			angleCount = 0;
			refAngle = angle;
		    }
		    */
		}
	    }


	    tracked[index] = (byte)1;

	    int newAryCount = aryCount+1;
	    if (newAryCount == shortRangeSamples)
		newAryCount = 0;

	    //search immediate vicinity
	    track(id, index-id.width*1-1,  1,  1, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount,
		  shortRangeDXs, shortRangeDYs, shortRangeAngles); //search one pixel up and to the left
	    track(id, index-id.width*1+0,  0,  1, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount,
		  shortRangeDXs, shortRangeDYs, shortRangeAngles); //search one pixel up
	    track(id, index-id.width*1+1,  1, -1, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount,
		  shortRangeDXs, shortRangeDYs, shortRangeAngles); //search one pixel up and to the right
	    track(id, index+id.width*0-1,  1,  0, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount,
		  shortRangeDXs, shortRangeDYs, shortRangeAngles); //search one pixel to the left
	    track(id, index+id.width*0+1,  1,  0, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount,
		  shortRangeDXs, shortRangeDYs, shortRangeAngles); //search one pixel to the right
	    track(id, index+id.width*1-1,  1, -1, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount,
		  shortRangeDXs, shortRangeDYs, shortRangeAngles); //search one pixel down and to the left
	    track(id, index+id.width*1+0,  0,  1, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount,
		  shortRangeDXs, shortRangeDYs, shortRangeAngles); //search one pixel down
	    track(id, index+id.width*1+1,  1,  1, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount,
		  shortRangeDXs, shortRangeDYs, shortRangeAngles); //search one pixel down and to the right

	    //search 2 pixels out
	    track(id, index-id.width*2-2, -2, -2, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount, shortRangeDXs, shortRangeDYs, shortRangeAngles);
	    track(id, index-id.width*2-1, -1, -2, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount, shortRangeDXs, shortRangeDYs, shortRangeAngles);
	    track(id, index-id.width*2+0,  0, -2, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount, shortRangeDXs, shortRangeDYs, shortRangeAngles);
	    track(id, index-id.width*2+1,  1, -2, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount, shortRangeDXs, shortRangeDYs, shortRangeAngles);	    
	    track(id, index-id.width*2+2,  2, -2, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount, shortRangeDXs, shortRangeDYs, shortRangeAngles);
	    track(id, index-id.width*1-2, -2, -1, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount, shortRangeDXs, shortRangeDYs, shortRangeAngles);
	    track(id, index-id.width*1+2,  2, -1, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount, shortRangeDXs, shortRangeDYs, shortRangeAngles);
	    track(id, index+id.width*0-2, -2,  0, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount, shortRangeDXs, shortRangeDYs, shortRangeAngles);
	    track(id, index+id.width*0+2,  2,  0, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount, shortRangeDXs, shortRangeDYs, shortRangeAngles);
	    track(id, index+id.width*1-2, -2,  1, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount, shortRangeDXs, shortRangeDYs, shortRangeAngles);
	    track(id, index+id.width*1+2,  2,  1, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount, shortRangeDXs, shortRangeDYs, shortRangeAngles);
	    track(id, index+id.width*2-2, -2,  2, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount, shortRangeDXs, shortRangeDYs, shortRangeAngles);
	    track(id, index+id.width*2-1, -1,  2, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount, shortRangeDXs, shortRangeDYs, shortRangeAngles);
	    track(id, index+id.width*2+0,  0,  2, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount, shortRangeDXs, shortRangeDYs, shortRangeAngles);
	    track(id, index+id.width*2+1,  1,  2, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount, shortRangeDXs, shortRangeDYs, shortRangeAngles);
	    track(id, index+id.width*2+2,  2,  2, steps+1, newAryCount,
		  deltaXSum, deltaYSum, refAngle, angleCount, shortRangeDXs, shortRangeDYs, shortRangeAngles);
	    
	}
    }
}
