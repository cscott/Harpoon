// HumanRecognition.java, created by benster 5/27/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package imagerec.graph;

import imagerec.util.CommonMemory;

import java.awt.Frame;

public class HumanRecognition extends Node {

    private Pair head;
    private int size;

    public HumanRecognition() {
	this.head = null;
	this.size = 0;
    }


    /**
       The name of the variable in {@link CommonMemory} that this
       {@link HumanRecognition} node will set with true or false
       before it returns. If <code>memName</code> is null,
       then this {@link HumanRecognition} node will not set any
       value in {@link CommonMemory}.<br><br>
       If this variable is equal to <code>true</code>, this means that
       the <code>process()</code> method found blue in the
       image. If the variable is set to <code>false</code>, then
       no blue was found.<br><br>
       Remember that the boolean value of the variable in {@link CommonMemory}
       is actually stored in a {@link Boolean} object wrapper.
       @see CommonMemory
     */
    private String memName;

    /**
     * This method either tells the {@link HumanRecognition} node to
     * begin or stop
     * setting a "return value" in {@link CommonMemory}.
     * Specifying a name will ause the {@link HumanRecognition} node to
     * store either
     * <code>true</code> or <code>false</code> in the variable who's
     * name you specify
     * depending on whether the <code>process()</code> method detects
     * blue in an image.<br><br>
     *
     * By specifying a <code>null</code> name, you turn this feature off.
     * @param name The name of the variable in {@link CommonMemory}
     * where this {@link HumanRecognition}
     * node will store its "return value."
     *
     * @see CommonMemory
     */
    public void setCommonMemory(String name) {
	this.memName = name;
    }



    public void process(ImageData id) {
	int targetID = id.trackedObjectUniqueID;
	System.out.println("ImageData #"+id.id);
	System.out.println("   target #"+targetID);
	Pair currentPair = head;
	boolean foundIt = false;
	while (currentPair != null) {
	    if (currentPair.targetID == targetID) {
		foundIt = true;
		break;
	    }
	    currentPair = currentPair.next;
	}
       
	if (foundIt) {
	    Display d = currentPair.d;
	    Frame f = d.getFrame();
	    if (f.isVisible()) {
		currentPair.d.process(id);
		super.process(id);
		if (this.memName != null) {
		    CommonMemory.setValue(this.memName, new Boolean(true));
		}
	    }
	    else {
		if (this.memName != null) {
		    CommonMemory.setValue(this.memName, new Boolean(false));
		}
	    }

	}
	else {
	    Pair newPair = addTarget(id);
	    newPair.d.process(id);
	    super.process(id);
	    if (this.memName != null) {
		CommonMemory.setValue(this.memName, new Boolean(true));
	    }
	}
    }



    private Pair addTarget(ImageData id) {
	Pair newPair = new Pair(id.trackedObjectUniqueID, head);
	head = newPair;
	size++;
	return newPair;
    }

    private class Pair {
	Display d;
	int targetID;
	Pair next;
	Pair(int targetID){
	    init(targetID, null);
	}
	Pair(int targetID, Pair next) {
	    init(targetID, next);
	}
	private void init(int targetID, Pair next) {
	    this.targetID = targetID;
	    d = new Display("Target #"+targetID);
	    this.next = next;
	}
    }
}
