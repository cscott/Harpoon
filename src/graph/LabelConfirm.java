// LabelConfirm.java, created by benster 6/01/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

public class LabelConfirm extends Node {
    private Label myLabel;
    public LabelConfirm (Label l) {
	super(null);
	myLabel = l;
    }

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
