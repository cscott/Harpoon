// Circle.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

/**
 *  This node circles an identified target in a picture.
 *  Given a target image (with no tag), it will query a {@link Cache} for the whole picture,
 *  circle the target, and send the image onto the <code>out</code> node.
 *  All other tagged images are blocked.
 *
 *  Cyclic graphs can be constructed with the <code>setLeft</code> and 
 *  <code>setRight</code> methods on {@link Node}.
 *
 *  @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Circle extends Node {
    class WorkList {
	ImageData id;
	WorkList next;

	WorkList(ImageData id, WorkList next) {
	    this.id = id;
	    this.next = next;
	}
    };
    
    private WorkList work = null;

    /** Construct a {@link Circle} node to circle images 
     *  Use with a {@link Command} to retrieve items from the {@link Cache}.
     *
     *  @param out Node to send circled images to.
     *  @param cache Cache to query for context of an image sent to this node.
     */
    public Circle(Node out, Node cache) {
	super(out, cache);
    }

    /** <code>process</code> an image of a target by querying a {@link Cache},
     *  circling the target, and send the image onto the <code>out</code> node.
     */
    public synchronized void process(ImageData id) {
	switch (Command.read(id)) {
	case Command.RETRIEVED_IMAGE: {
	    WorkList last = work;
	    for (WorkList w = work; w != null; w = (last = w).next) {
		if (w.id.id == id.id) {
		    ImageData wid = w.id;
		    ImageDataManip.ellipse(id, wid.x, wid.y, wid.width, wid.height);
		    if (w == work) {
			work = work.next;
		    } else {
			last.next = work.next;
		    }
		    getLeft().process(id);
		    break;
		}
	    }
	    break;
	}
	case Command.NONE: {
	    //add node to front of WorkList linked list
	    work = new WorkList(id, work);
	    getRight().process(id);
	    break;
	}
	default: {
	    // Block other images.
	}
	}

    }
}
