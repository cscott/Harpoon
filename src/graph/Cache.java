// Cache.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

/**
 * {@link Cache} is a {@link Node} that caches the last n images which 
 * passed through it.  To retrieve an image, send a <code>GET_IMAGE</code> 
 * {@link Command} to the {@link Cache}, and it will send the appropriate 
 * image along the <code>retrieve</code> node.<br><br>
 *
 * By default, this object stores references to the images that pass through it.
 * However, you may opt for this object to clone the ImageData and store a reference to that instead.
 * Do this by calling <code><i>ImageDataNode</i>.saveCopies(true)</code>.<br><br>
 *
 * Use with {@link Command} to tag the retrieved image as retrieved.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Cache extends Node {

    private ImageData[] ids;
    private int lastID=0;
    private int nextIDToSenc = 0;

    private boolean saveCopies = false;

    /** Construct a {@link Cache} node which caches the last n images which
     *  are sent to it.  Send an <code>GET_IMAGE</code> {@link Command}
     *  to retrieve an image. 
     *
     *  @param numIds The size of the cache in number of {@link ImageData}s.
     *  @param retrieve The node retrieved images go to.
     */
    public Cache(int numIds, Node retrieve) {
	super(null, retrieve);
	init(numIds);
    }

    /** Construct a {@link Cache} node which caches the last n images which
     *  passed through it.  Send an <code>GET_IMAGE</code> {@link Command}
     *  to retrieve an image. 
     *
     *  @param numIds The size of the cache in number of {@link ImageData}s.
     *  @param out The node to send images to.
     *  @param retrieve The node retrieved images go to.
     */
    public Cache(int numIds, Node out, Node retrieve) {
	super(out, retrieve);
	init(numIds);
    }

    /** 
     *  Method should be called by all constructors to initialize object fields.
     */    
    private void init(int numIds) {
	ids = new ImageData[numIds];
	
    }

    /**
     * Instructs this {@link Cache} node to save clones of the {@link ImageData}s
     * that pass through it instead of just saving references.
     * @param value If <code>true</code> then {@link Cache} node will save clones.
     *              If <code>false</code> then {@link Cache} node will save only references.
     */
    public void saveCopies(boolean value) {
	this.saveCopies = value;
    }

    /** <code>process</code> an {@link ImageData}.  If the {@link ImageData}
     *  is tagged with a GET_IMAGE {@link Command}, sends an image with the corresponding
     *  <code>id</code>, cropped to fit the given <code>width</code>, <code>height</code>,
     *  <code>x</code> and <code>y</code>.  If the image is not in the cache, it does not
     *  send an image to <code>retrieve</code>. 
     *
     *  @param id The {@link ImageData} which is either a request for an image from
     *            the cache, or an image to be added to the cache.
     */
    //public synchronized void process(ImageData id) {
    public void process(ImageData id) {
	//System.out.println("Cache.process()");
	//System.out.println("  id.command="+id.command);
	switch (Command.read(id)) {
	case Command.GET_CROPPED_IMAGE:
	    //System.out.println("Getting cropped image:");
	    //System.out.println("  id.id = "+id.id);
	    //System.out.println("  id.width = "+id.width);
	    //System.out.println("  id.height = "+id.height);
	    //System.out.println("");
	    //no break, so continues onto next case
	case Command.GET_IMAGE: {
	    Node right = getRight();
	    if (right!=null) {
		for (int i=0; i<ids.length; i++) {
		    ImageData retID = ids[i];
		    if ((retID != null)&&(retID.id == id.id)) {
			if (Command.read(id)==Command.GET_CROPPED_IMAGE) {
			    retID = ImageDataManip.crop(retID, id.x, id.y, id.width, id.height);
			}
			
			//WARNING: if the ImageData defining the cropping boundaries (id)
			//has information that you want propogated to the new image,
			//you must do that here.
			retID.c1 = id.c1;
			retID.c2 = id.c2;
			retID.c3 = id.c3;			
			retID.trackedObjectUniqueID = id.trackedObjectUniqueID;

			right.process(retID);
			break;
		    }
		}
	    }
	    break;
	}
// 	case Command.GET_NEXT_IMAGE: {
//	    ImageData retID = ids[nextIDToSend];
//	    nextIDToSend++;
//	    if (nextIDToSend >= ids.length)
//		nextIDToSend = 0;
//	    if (retID != null) {
//		right.process(retID);
//	    }
//	    break;
//	}
	case Command.NONE: {
	default: {
	    if (id!=null) {
		if (saveCopies) {
		    ids[lastID=(lastID+1)%ids.length] = ImageDataManip.clone(id);
		}
		else {
		    ids[lastID=(lastID+1)%ids.length] = id;
		}
	    }
	    Node left = getLeft();
	    if (left != null) {
		left.process(id);
	    }
	}
	//default: {
	//    Node left = getLeft();
	//    if (left != null) {
	//	left.process(id);
	//    }
	//}
	}
    }
}
