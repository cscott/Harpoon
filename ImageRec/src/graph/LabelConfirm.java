// LabelConfirm.java, created by benster 6/01/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * This class is intended to be a part of a distributed ATR,
 * where the {@link Label} node is responsible for generating
 * cropped images for further analysis, and the asynchronous (remote)
 * part determines if those cropped images contain tanks.
 *
 * {@link ImageData}s passed to this class should be tagged
 * either with <code>Command.IS_TANK</code> or <code>Command>IS_NO_TANK</code>
 * and should have it's <code>trackedObjectUniqueID</code> set to a valid value.
 * 
 * This class will then report back to the {@link Label} node, telling it
 * whether the specified tracked object is a tank or not.
 *
 */
public class LabelConfirm extends Node {
    /**
     * The {@link Label} object to which conformations and denials
     * will be relayed.
     */
    private Label myLabel;
    public LabelConfirm (Label l) {
	super(null);
	myLabel = l;
    }

    /**
     * Takes confirmations and denials
     * and relays the information to the {@link Label} node
     * specified in the constructor.
     */
    public void process(ImageData id) {
	if (id.command == Command.IS_TANK)
	    myLabel.confirm(id.trackedObjectUniqueID);
	else if (id.command == Command.IS_NOT_TANK) {
	    myLabel.deny(id.trackedObjectUniqueID);
	}
	else {
	    System.out.println("LabelConfirm: WARNING, UNRECOGNIZED COMMAND");
	}
	super.process(id);
    }
}
