// SetOrigDimensions.java, created by benster 5/29/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

public class SetOrigDimensions extends Node {
    public SetOrigDimensions() {
	super();
    }
    public void process(ImageData id) {
	//System.out.println("SetOrigDimensions.process()");
	id.origWidth = id.width;
	id.origHeight = id.height;
	super.process(id);
    }
}
