// Cache.java, created by benster
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * {@link Profile} records statistics about how successful image recognition.
 * Provide it with a delta location tolerance, a
 * delta size tolerance, and a list of frame numbers where
 * successful tank recognition occurs, and by using information
 * from the {@link ImageData}s that pass through, determines how often the 
 * pipeline correctly finds tanks in the following frames.<br><br>
 *
 * It does this by assuming that the location and size of the tank
 * does not move much per frame.
 *
 * The user is required to specify which frame contains the first
 * correctly recognized tank so that the {@link Profile}
 * node can calibrate itself.
 */
public class Profile extends Node {
    protected int[] goodFrameNumbers;
    protected int width;
    protected int height;
    protected int locTolerance;
    protected int sizeTolerance;

    protected int successfulCount = 0;
    protected int unsuccessfulCount = 0;
    protected int doubleCount = 0;
    protected int totalCount = 0;
    protected int lastFrameNumber = -1;
    protected int lastSuccessfulFrameNumber = -1;
    protected int lastX;
    protected int lastY;
    protected int lastWidth;
    protected int lastHeight;

    public Profile(int locTolerance, int sizeTolerance, int[] goodFrameNumbers) {
	this.locTolerance = locTolerance;
	this.sizeTolerance = sizeTolerance;
	this.goodFrameNumbers = goodFrameNumbers;
    }

    private boolean isGoodFrameNumber(int frameNumber) {
	//System.out.println("Testing frame #"+frameNumber);
	int size = goodFrameNumbers.length;
	for (int count = 0; count < size; count++) {
	    if (frameNumber == goodFrameNumbers[count]) {
		//System.out.println("Frame #"+frameNumber+" is good");
		return true;
	    }
	}
	//System.out.println("Frame #"+frameNumber+" is not good");
	return false;
    }

    private int[] duplicates = new int[256];
    private int curCount;

    public void process(ImageData id) {
	int curFrameNumber = id.id;
	totalCount++;
	//assuming frames come in order
	int diff = curFrameNumber - lastFrameNumber;
	//if diff == 0, then two tanks were found
	//which is assumed not to be correct
	if (diff == 0) {
	    doubleCount++;
	    curCount++;
	    duplicates[curCount-1]--;
	    duplicates[curCount]++;
	    
	}
	else {
	    duplicates[1]++;
	    curCount = 1;
	}

	//if this node has not seen any frames
	//where the tank is correctly recognized...
	if (isGoodFrameNumber(curFrameNumber)) {
	    lastX = id.x + id.width/2;
	    lastY = id.y + id.height/2;
	    lastWidth = id.width;
	    lastHeight = id.height;
	    lastFrameNumber = curFrameNumber;
	    lastSuccessfulFrameNumber = curFrameNumber;
	    System.out.println("Successful (Forced)");
	    successfulCount++;
	}
	else if (lastSuccessfulFrameNumber == -1) {
	    lastFrameNumber = curFrameNumber;
	    unsuccessfulCount++;
	    System.out.println("___UNSUCCESSFUL___");
	}
	else {
	    int curX = id.x + id.width/2;
	    int curY = id.y + id.height/2;
	    int curWidth = id.width;
	    int curHeight = id.height;
	    //System.out.println("Profile: #("+curFrameNumber+")");
	    //System.out.println("  last#: "+lastFrameNumber);
	    //System.out.println("  lastSuc#: "+lastSuccessfulFrameNumber);
	    //System.out.println("  curX:("+curX+")  lastX:("+lastX+")");
	    //System.out.println("  curY:("+curY+")  lastY:("+lastY+")");
	    //System.out.println("  curWidth:("+curWidth+")  lastWidth:("+lastWidth+")");
	    //System.out.println("  curHeight:("+curHeight+")  lastHeight:("+lastHeight+")");
	    //System.out.println("curX-lastX: "+(curX-lastX)+"  : "+locTolerance*diff);
	    if ((Math.abs(curX - lastX) <= locTolerance) &&
		(Math.abs(curY - lastY) <= locTolerance) &&
		(Math.abs(curWidth - lastWidth) <= sizeTolerance) &&
		(Math.abs(curHeight - lastHeight) <= sizeTolerance)) {
		System.out.println("Successful");
		lastX = curX;
		lastY = curY;
		//lastWidth = curWidth;
		//lastHeight = curHeight;
		lastFrameNumber = curFrameNumber;
		lastSuccessfulFrameNumber = curFrameNumber;
		successfulCount++;
	    }
	    else {
		System.out.println("___UNSUCCESSFUL___");
		lastFrameNumber = curFrameNumber;
		unsuccessfulCount++;
	    }

	}
	
	if (id.lastImage) {
	    System.out.println("Profile: ");
	    System.out.println("   successfulCount: "+successfulCount);
	    System.out.println(" unsuccessfulCount: "+unsuccessfulCount);
	    System.out.println("               sum: "+(successfulCount+unsuccessfulCount));
	    System.out.println("       doubleCount: "+doubleCount);
	    System.out.println("        totalCount: "+totalCount);
	    System.out.println("Duplicates:");
	    for(int count = 1; count<duplicates.length; count++) {
		if (duplicates[count] > 0)
		    System.out.println("  "+count+": "+duplicates[count]);
	    }
	    System.out.println("______________________________");
	}
	super.process(id);
    }

}
