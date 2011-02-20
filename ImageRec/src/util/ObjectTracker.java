// ObjectTracker.java, created by benster 5/8/2003
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.util;

/**
 * Clas responsible for keep track of objects of interest.
 * Keeps an array of {@link ObjectInfo}s,
 * which, in turn, contain {@link ObjectDataPoint}s.
 *
 * This class is intended to be stored in {@link CommonMemory} so
 * it may be accessed by any interested Nodes.
 * 
 * @see ObjectInfo
 * @see ObjectDataPoint
 */
public class ObjectTracker {
    
    /**
     * The array of {@link ObjectInfo}s. The size of this array
     * is dynamically adjusted as {@link ObjectInfo}s are added
     * and deleted.
     */
    private ObjectInfoWrapper[] objectInfoWrappers;
    /**
     * The garbage collector which
     * periodically checks all contained {@link ObjectInfo}s
     * to see if they have been modified/updated recently.
     * If they have not, then the {@link ObjectInfo}s are
     * thrown away.
     *
     * @see ObjectTracker.GarbageCollector
     */
    private GarbageCollector gc;

    /**
     * Minimum guarenteed time (in ms) that 
     * unmodified {@link ObjectInfo}s will be preserved. Before
     * they are thrown out by the ObjectTracker's GarbageCollector.
     *
     * @see ObjectTracker.GarbageCollector
     */
    private static final long timeToForget = 10000;

    /**
     * Constructor that creates an empty ObjectTracker.
     */
    public ObjectTracker() {
	init();
    }

    /**
     * This method should be called by all constructors
     * to properly initialize the object.
     */
    private void init() {
	objectInfoWrappers = new ObjectInfoWrapper[0];
	gc = new GarbageCollector();
    }

    /**
     * Adds the information for a new object to be tracked.
     *
     * @param info The {@link ObjectInfo} of the object to be tracked.
     *
     * @throws RuntimeException Thrown if an {@link ObjectInfo} with 
     * the same unique ID is already being tracked.
     */
    public synchronized void addObjectInfo(ObjectInfo info) {
	//synchronized because of the garbage collector
	int index;
	int newUniqueID = info.getUniqueID();
	//'for' loop is just an assertion check
	//it can be removed. The exception
	//should never be thrown since
	//'newUniqueID' must be unique.
	for (index = 0; index < objectInfoWrappers.length; index++) {
	    if (objectInfoWrappers[index].objectInfo.getUniqueID() ==
		newUniqueID) {
		throw new RuntimeException("An ObjectInfo with the "+
					   "unique ID "+newUniqueID+
					   " is already being tracked.");
	    }
	}
	ObjectInfoWrapper newObjectInfoWrapper =
	    new ObjectInfoWrapper(info);
	int size = objectInfoWrappers.length;
	ObjectInfoWrapper[] newObjectInfoWrappers =
	    new ObjectInfoWrapper[size+1];
	System.arraycopy(objectInfoWrappers, 0,
			 newObjectInfoWrappers, 0, size);
	newObjectInfoWrappers[size] = newObjectInfoWrapper;
	objectInfoWrappers = newObjectInfoWrappers;
	if (size > 10) {
	    System.out.println("ObjectTracker: WARNING: more than "+
			       "100 items of interest.");
	}	
    }

    /**
     * Removes the information for an object already being tracked.
     *
     * @param uniqueID The uniqueID of the {@link ObjectInfo} to stop tracking.
     *
     * @throws RuntimeException Thrown if an {@link ObjectInfo} with 
     * the same unique ID is not being tracked.
     */
    public synchronized void remObjectInfo(int uniqueID) {
	//synchronized because of the garbage collector
	int index;
	for (index = 0; index < objectInfoWrappers.length; index++) {
	    if (objectInfoWrappers[index].objectInfo.getUniqueID() == 
		uniqueID)
		break;
	}
	//if found nothing, then throw exception
	if (index == objectInfoWrappers.length) {
	    throw new NoValueException("An ObjectInfo with the "+
				       "unique ID "+uniqueID+
				       " is not being tracked.");
	}
	ObjectInfoWrapper[] newObjectInfoWrappers =
	    new ObjectInfoWrapper[objectInfoWrappers.length-1];
	System.arraycopy(objectInfoWrappers, 0,
			 newObjectInfoWrappers, 0, index);
	System.arraycopy(objectInfoWrappers, index+1,
			 newObjectInfoWrappers, index,
			 newObjectInfoWrappers.length - index);
	objectInfoWrappers = newObjectInfoWrappers;
	return;	
    }

    private synchronized ObjectInfoWrapper getObjectInfoWrapper(int uniqueID) {
	//synchronized because of the garbage collector
	int index;
	for (index = 0; index < objectInfoWrappers.length; index++) {
	    if (objectInfoWrappers[index].objectInfo.getUniqueID() == 
		uniqueID)
		break;
	}
	//if found nothing, then throw exception
	if (index == objectInfoWrappers.length) {
	    throw new NoValueException("An ObjectInfo with the "+
				       "unique ID "+uniqueID+
				       " is not being tracked.");
	}
	return objectInfoWrappers[index];
    }

    /**
     * Set whether the given tracked object is "interesting".
     * The "interesting" property currently does not affect anything
     * within the ObjectTracker code or its dependencies.
     *
     * @param uniqueID The unique tracking ID for the object
     *        whose property will be set.
     * @param interesting Specifies the new value of the property. 
     */
    public void setInteresting(int uniqueID, boolean interesting) {
	ObjectInfoWrapper oiw = getObjectInfoWrapper(uniqueID);
	oiw.interesting = interesting;
    }

    /**
     * Returns the boolean value of the "interesting" property
     * of the specified tracked object.
     *
     * @param uniqueID The unique tracking ID for the object
     *                 whose "interesting" property value will
     *                 be returned.
     */
    public boolean isInteresting(int uniqueID) {	
	ObjectInfoWrapper oiw = getObjectInfoWrapper(uniqueID);
	return oiw.interesting;
    }

    /**
     * Returns the {@link ObjectInfo} whose last known x-y coordinate
     * is closest to the specified coordinate.<br><br
     * 
     * The actual coordinate that is compared is calculated as follows:
     *     <code>coordinate = (x+width/2,y+height/2)</code>
     *
     * @param x See above.
     * @param y See above.
     * @param width See above.
     * @param height See above.
     */
    public synchronized ObjectInfo getClosestObject(int x, int y,
				       int width, int height) {
	int size = objectInfoWrappers.length;
	int xLoc = x + width/2;
	int yLoc = y + height/2;
	double smallestDistance = Double.MAX_VALUE;
	ObjectInfo closestObjectInfo = null;
	for (int count = 0; count < size; count++) {
	    ObjectInfo currentInfo  = objectInfoWrappers[count].objectInfo;
	    ObjectDataPoint lastData = currentInfo.getLastDataPoint();
	    int curXLoc = lastData.getX() + lastData.getWidth()/2;
	    int curYLoc = lastData.getY() + lastData.getHeight()/2;
	    double currentDistance = 
		Math.sqrt(Math.pow(xLoc-curXLoc,2) + Math.pow(yLoc-curYLoc,2));
	    if (currentDistance < smallestDistance) {
		smallestDistance = currentDistance;
		closestObjectInfo = currentInfo;
	    }
	}
	return closestObjectInfo;
    }

    /**
     * Class used to store an {@link ObjectInfo} along
     * with meta-data for that ObjectInfo, like whether
     * the user is interested in this object.
     *
     * By default, all objects are interesting.
     *
     * @see ObjectInfo
     */
    private class ObjectInfoWrapper {
	ObjectInfo objectInfo;
	boolean interesting;
	ObjectInfoWrapper(ObjectInfo info) {
	    this.objectInfo = info;
	    this.interesting = true;
	}
    }

    /**
     * A private internal class which runs
     * an independent thread that removes {@link ObjectInfo}s
     * from the {@link ObjectTracker}'s array whose contents
     * have not been mutated/updated for a while. The maximum
     * amount of time an {@link ObjectInfo} may remain idle
     * is defined as a field of the {@link ObjectTracker}.
     */
    private class GarbageCollector implements Runnable {
	/**
	 * Constructor that initalizes and runs a new garbage
	 * collecting thread that will clean-up the contents
	 * of the {@link ObjectTracker} that instantiates it.
	 */
	GarbageCollector() {
	    Thread t = new Thread(this);
	    t.start();
	}

	/**
	 * Periodically performs garbage collection.
	 */
	public void run() {
	    boolean keepGoing = true;
	    while (keepGoing) {
		try {
		    Thread.currentThread().sleep(2000);
		}
		catch (InterruptedException e) {
		}
		synchronized(this) {
		    int size = objectInfoWrappers.length;
		    boolean[] remove = new boolean[size];
		    long currentTime = System.currentTimeMillis();
		    for (int count = 0; count < size; count++) {
			ObjectDataPoint odp = objectInfoWrappers[count].objectInfo.getLastDataPoint();
			if (currentTime - odp.getTime() > timeToForget) {
			    remove[count] = true;
			}
		    }
		    //backwards so that when an object is removed
		    //from the array
		    //remaining indexes don't need to change
		    for(int count = size-1; count >= 0; count--) {
			if (remove[count]) {
			    int uniqueID = objectInfoWrappers[count].objectInfo.getUniqueID();
			    System.out.println("ObjectTracker: Disposing of object #"+uniqueID);
			    ObjectTracker.this.remObjectInfo(uniqueID);
			}
		    }
		}//[synchronized(this)]
	    }//[while (keepGoing)]
	}//[public void run()]
    }//[class GarbageCollector]
}//[class ObjectTracker]
