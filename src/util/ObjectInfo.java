// ObjectInfo.java, created by benster 5/8/2003
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.util;

import imagerec.graph.ImageData;

/**
 * Class that holds information about a single object being tracked.
 * This class is structred to store data keyed by each
 * frame that an object is recognized in.<br><br>
 *
 * The hope is that storing data over several frames
 * will allow analysis of averages so that momentary lapses
 * of image recognition will not be fatal.
 */
public class ObjectInfo {

    /**
     * Static field that keeps track of unique ID's that 
     * have already been assigned. The value of
     * this field is the ID to be assigned when
     * the next {@link ObjectInfo} is instantiated.
     */
    private static int uniqueIDCounter = 0;
    
    /**
     * Integer ID that is unique to all {@link ObjectInfo}s.
     */
    private int uniqueID;

    /**
     * Holds all data points for this {@link ObjectInfo}.
     */
    private ObjectDataPoint[] dataPoints;

    /**
     * Indicates the index of the first empty spot
     * in the data array.
     */
    private int index = 0;

    /**
     * The number of data points being stored in the data point array.
     * This value is not necessarily equal to the size of the array.
     */
    private int numberOfDataPoints = 0;

    /**
     * The default maximum number of data points that
     * an {@link ObjectInfo} will store at one time
     * if not otherwise specified.
     */
    public static final int defaultMaxDataPoints = 10;

    /**
       Construct an {@link ObjectInfo} object that
       will store the default number of data points.
     */
    public ObjectInfo() {
	init(defaultMaxDataPoints);
    }

    /**
     * Construct an {@link ObjectInfo} object that
     * will store the specified number of data points.
     *
     * @param maxDataPoints The maximum number of data points to be
     * stored at one time.
     */
    public ObjectInfo(int maxDataPoints) {
	init(maxDataPoints);
    }

    /**
     * This method should be called by all constructors to ensure
     * that the fields get intialized correctly.
     */
    private void init(int maxDataPoints) {
	this.dataPoints = new ObjectDataPoint[maxDataPoints];
	this.uniqueID = ObjectInfo.uniqueIDCounter++;
    }

    /**
     * Adds a data point to this {@link ObjectInfo}'s records.
     * If the number of data points already added to this
     * object equals or exceeds the specified number of maximum
     * data points, then the oldest data point will be dropped.
     */
    public void addDataPoint(ImageData id) {
	dataPoints[index] = new ObjectDataPoint(id);
	numberOfDataPoints++;
	index++;
	if (index == dataPoints.length)
	    index = 0;
    }

    /**
     * Returns the data point most recently added
     * to this object.
     * 
     * @throws RuntimeException Thrown if this
     * {@link ObjectInfo} contains no data points. 
     */
    public ObjectDataPoint getLastDataPoint() {
	if (numberOfDataPoints == 0)
	    throw new RuntimeException ("ObjectInfo contains no data points.");
	int lastIndex = index-1;
	if (lastIndex < 0)
	    lastIndex = dataPoints.length-1;
	return dataPoints[lastIndex];
    }

    /**
     * Returns the data point with the specified index.<br><br>
     *
     * The index works as follows:
     *     An index of 0 returns the most recently added data point.
     *     An index of 1 returns the second most recently added data point.
     *     ...
     *     An index of the max number of data points - 1 returns
     *        the least recently added data point.
     *
     * @throws RuntimeException Thrown if 
     * <code>index</code> >= number of existing data points.
     */
    public ObjectDataPoint getDataPoint(int index) {
	if ((numberOfDataPoints < dataPoints.length) &&
	    (index > numberOfDataPoints - 1)) {
	    if (numberOfDataPoints == 0) {
		throw new RuntimeException ("ObjectInfo contains no data points.");
	    }
	    else {
		throw new RuntimeException ("Invalid index specified: "+
					    "["+index+"]. Maximum is ["+
					    (numberOfDataPoints-1)+"].");
	    }
	}
	else if (index > dataPoints.length-1) {
	    throw new RuntimeException ("Invalid index specified: "+
					"["+index+"]. Maximum is ["+
					(dataPoints.length-1)+"].");	    
	}
	int newIndex = this.index-1 - index;
	if (newIndex < 0)
	    newIndex += dataPoints.length;
	return dataPoints[newIndex];
    }

    /**
       Returns the unique ID number of this {@link ObjectInfo}. IDs
       are unique to all {@link ObjectInfo}s.
     */
    public int getUniqueID() {
	return this.uniqueID;
    }
}
