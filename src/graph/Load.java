// Load.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.*;

/**
 * A {@link Load} node will load a series of <code>.ppm.gz</code> files from the disk
 * and send them to the out node.  The filenames are <code>filePrefix.#</code>
 * where <code>#</code> ranges from <code>0</code> to <code>num-1</code> inclusive.
 *
 * @see Save
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Load extends Node {
    private String filePrefix;
    private int num;

    /** Construct a {@link Load} node that will load the files
     *  <code>filePrefix.0</code> through
     *  <code>filePrefix.num-1</code> and send them to
     *  <code>out</code> node.  */
    public Load(String filePrefix, int num, Node out) {
	super(out);
	this.filePrefix = filePrefix;
	this.num = num;
    }

    /** Load all the image files and send them, one at a time, to the <code>out</code> node. */
    public synchronized void process(ImageData id) {
	for (int i=0; i<num; i++) {
	    System.out.println("Loading image "+filePrefix+"."+i);
	    (id = ImageDataManip.readPPM(filePrefix+"."+i)).id = i;
	    super.process(id);
	}
    }
}
