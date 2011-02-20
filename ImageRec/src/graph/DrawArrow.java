// DrawArrow.java, created by benster
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

/**
 * This {@link Node} draws an arrow extending from an identified target
 * in the direction that target is facing.<br><br>
 *
 * To determine where the arrow will be drawn, this object
 * uses the fields 'c1', 'c2', 'c3' and 'angle' from the given ImageData object
 * it is passed.
 *
 * This object works as follows:
 *   Any time the <code>process()</code> method is called with an
 *   {@link ImageData} that is not tagged with a
 *   RETRIEVED_IMAGE command, the appropriate {@link ImageData} fields
 *   are saved onto a work list.<br><br>
 *
 *   Then, at some later point, the <code>process()</code> method
 *   of a {@link Cache} node should be called with an {@link ImageData}
 *   tagged with a GET_IMAGE command. The {@link Cache} node should
 *   then in turn call the {@link Circle} node's <code>process()</code>
 *   method with its stored {@link ImageData}.<br><br>
 *
 *   When the {@link Circle} node's <code>process()</code> is called
 *   for a second time, it empties its stored work list, drawing
 *   each arrow on the provided {@link ImageData}.
 *
 * @see Cache
 * @see Command
 *
 * @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
 * 
 */
public class DrawArrow extends Node {
    
    /**
     *  The Worklist class serves as a member of a linked list.
     *  It stores the information needed to draw an arrow on
     *  an {@link ImageData} when requested to do so.
     */
    private class WorkList {
	
	int x;
	int y;
	int width;
	int height;
	int id;
	double angle;
	WorkList next;

	WorkList(ImageData id, WorkList next) {
	    this.x = id.x;
	    this.y = id.y;
	    this.width = id.width;
	    this.height = id.height;
	    this.id = id.id;
	    this.angle = id.angle;
	    this.next = next;
	}
    };

    /**
     *  Linked list that stores information needed to
     *  draw requested arrows on an {@link ImageData}.
     */
    private WorkList workList = null;

    /** Construct a {@link DrawArrow} node to circle images 
     *  Use with a {@link Command} to retrieve items from the {@link Cache}.
     *
     *  @param out Node to send circled images to.
     *  @param cache Node to call after a non-tagged {@link ImageData}
     *  is passed to the object and its appropriate data stored. The path
     *  pointed to by this branch should contain a {@link Cache} node
     *  at some point.
     */
    public DrawArrow(Node out, Node cache) {
	super(out, cache);
    }
    
    /** Process an image either by storing the appropriate information in a work list,then querying
	a {@link Cache} node, or by drawing arrows on the object using stored information. 
     */
    //public synchronized void process(ImageData id) {
    public void process(ImageData id) {
	//System.out.println("Draw Arrow: processing image #"+id.id);
	//System.out.println("Draw Arrow:     c1: "+id.c1);
	//System.out.println("Draw Arrow:     c2: "+id.c2);
	//System.out.println("Draw Arrow:     c3: "+id.c3);
	switch (Command.read(id)) {
	case Command.RETRIEVED_IMAGE: {
	    //System.out.println("Draw Arrow: RETRIEVED IMAGE, so drawing arrow");

	    WorkList last = workList;
	    for (WorkList w = workList; w != null; w = (last = w).next) {
		//System.out.println("Draw Arrow: items contained in the worklist");
		if (w.id == id.id) {
		    //System.out.println("Draw Arrow: work list id == this id");
		    ImageDataManip.drawArrow(id, w.x, w.y, w.width, w.height, w.angle);
		    if (w == workList) {
			workList = workList.next;
		    } else {
			last.next = workList.next;
		    }
		    if (getLeft() != null) {
			//System.out.println("Draw Arrow: going left");
			getLeft().process(id);
		    }
		    break;
		}
	    }
	    break;
	}
	    //treat any other Commands by building up the work list.
	default: {
	    //System.out.println("Draw Arrow: NO TAG, so storing worklist");
	    //add node to front of WorkList linked list
	    workList = new WorkList(id, workList);
	    getRight().process(id);
	    break;
	}
	}
	
    }    
}
