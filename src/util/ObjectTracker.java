// ObjectTracker.java, created by benster 5/8/2003
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.util;

/**
 * Clas responsible for keep track of objects of interest.
 * 
 * @see ObjectInfo
 */
public class ObjectTracker {
    
    ObjectInfoWrapper[] objectInfoWrappers;

    public ObjectTracker() {
	init();
    }

    private void init() {
	objectInfoWrappers = new ObjectInfoWrapper[0];
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
	int index;
	int newUniqueID = info.getUniqueID();
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

    private ObjectInfoWrapper getObjectInfoWrapper(int uniqueID) {
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

    public void setInteresting(int uniqueID, boolean interesting) {
	ObjectInfoWrapper oiw = getObjectInfoWrapper(uniqueID);
	oiw.interesting = interesting;
    }

    public boolean isInteresting(int uniqueID) {	
	ObjectInfoWrapper oiw = getObjectInfoWrapper(uniqueID);
	return oiw.interesting;
    }

    public ObjectInfo getClosestObject(int x, int y,
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
}
