// SetOrigDimensions.java, created by benster 5/29/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * This {@link Node} is intended to be used early
 * on in a pipeline to set {@link ImageData}s'
 * <code>origWidth</code> and <code>origHeight</code> fields,
 * before the {@link ImageData} is ever cropped.<br><br>
 *
 * This makes the original width and height of an {@link ImageData}
 * available to any {@link Node}s further down the pipeline.
 */
public class SetOrigDimensions extends Node {
    /**
     * Create a new {@link SetOrigDimensions} node.
     */
    public SetOrigDimensions() {
	super();
    }
    /**
     * Sets the specified {@link ImageData}'s
     * <code>origWidth</code> and <code>origHeight</code>
     * fields and passes it on to the next nodes.
     *
     * @param id The {@link ImageData} to be modified.
     */
    public void process(ImageData id) {
	//System.out.println("SetOrigDimensions.process()");
	id.origWidth = id.width;
	id.origHeight = id.height;
	super.process(id);
    }
}
