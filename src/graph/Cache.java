// Cache.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

/**
 * {@link Cache} is a {@link Node} that caches the last n images which 
 * passed through it.  To retrieve an image, send a <code>GET_IMAGE</code> 
 * {@link Command} to the {@link Cache}, and it will send the appropriate 
 * image along the <code>retrieve</code> node.
 *
 * Use with {@link Command} to tag the retrieved image as retrieved.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Cache extends Node {

    private ImageData[] ids;
    private int lastID=0;

    /** Construct a {@link Cache} node which caches the last n images which
     *  are sent to it.  Send an <code>GET_IMAGE</code> {@link Command}
     *  to retrieve an image. 
     *
     *  @param numIds The size of the cache in number of {@link ImageData}s.
     *  @param retrieve The node retrieved images go to.
     */
    public Cache(int numIds, Node retrieve) {
	this(numIds, null, retrieve);
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
	ids = new ImageData[numIds];
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
    public synchronized void process(ImageData id) {
	switch (Command.read(id)) {
	case Command.GET_CROPPED_IMAGE:
	case Command.GET_IMAGE: {
	    Node right = getRight();
	    if (right!=null) {
		for (int i=0; i<ids.length; i++) {
		    ImageData retID = ids[i];
		    if ((retID != null)&&(retID.id == id.id)) {
			if (Command.read(id)==Command.GET_CROPPED_IMAGE) {
			    retID = ImageDataManip.crop(retID, id.x, id.y, id.width, id.height);
			}
			
			right.process(retID);
			break;
		    }
		}
	    }
	    break;
	}
	case Command.NONE: {
	    if (id!=null) {
		ids[lastID=(lastID+1)%ids.length] = id;
	    }
	}
	default: {
	    Node left = getLeft();
	    if (left != null) {
		left.process(id);
	    }
	}
	}
    }
}
