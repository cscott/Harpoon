// Match.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

/**
 * Match an input image against a database of templates.  Will send only the matched
 * images onto the next node.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Match extends Node {

    private ImageData[] iddb;
    private String filePrefix;
    private int numFiles;
    private int threshold;

    /** Construct a {@link Match} node that uses the default matching algorithm
     *  and parameters.
     *
     *  @param out The node to send matches to.
     */
    public Match(Node out) {
	this("dbase/plane/dbsh.ppm.gz", 100, out);
    }
    
    /** Construct a {@link Match} node using Steven Howard's original database.
     * 
     *  @param db
     *  @param threshold The threshold to determine whether an image is a match or not.
     *  @param out The node to send matches to.
     */
    public Match(String fileName, int threshold, Node out) {
	super(out);
	iddb = new ImageData[22];
	this.threshold = threshold;

	ImageData db = ImageDataManip.readPPM(fileName);
	for (int i=0; i<iddb.length; i++) {
	    iddb[i] = ImageDataManip.crop(db, (i%7)*100, (i/7)*100, 100, 100);
	}
    }

    /** Construct a {@link Match} node to send matches to <code>out</code>. 
     *
     *  @param filePrefix The filename prefix for the database of images to match against.
     *  @param numFiles The number of files in the database.
     *  @param cache Whether to cache the database in memory.
     *  @param threshold The threshold to determine whether an image is a match or not.
     *  @param out The node to send matches to.
     */
    public Match(String filePrefix, int numFiles, boolean cache, int threshold, Node out) {
	super(out);
	this.numFiles = numFiles;
	this.filePrefix = filePrefix;
	this.threshold = threshold;
	if (cache) {
	    iddb = new ImageData[numFiles];
	    for (int i=0; i<numFiles; i++) {
		iddb[i] = ImageDataManip.readPPM(filePrefix+"."+i);
	    }
	}
    }

    /** <code>process</code> an image of an object and see if it matches the template. 
     *
     *  @param id The image to process.
     */
    public synchronized void process(ImageData id) {
	int length = (iddb!=null)?iddb.length:numFiles;
	ImageData id1 = id;
	for (int i=0; i<length; i++) {
	    ImageData id2 = (iddb!=null)?iddb[i]:ImageDataManip.readPPM(filePrefix+"."+i);
	    if ((i==0)&&((id1.width!=id2.width)||
			 (id1.height!=id2.height))) {
		id1 = ImageDataManip.scale(id1, id2.width, id2.height);
	    }
	    if (match(id1, id2)) {
		super.process(id);
		break;
	    }
	}
    }

    private boolean match(ImageData id1, ImageData id2) {
	/* This sucks!!!! n^4! */
//  	int width = id2.width;
//  	int height = id2.height;
//  	int match = 0;
//  	int size = height*width;
//  	for (int pos=0; pos<size; pos++) {
//  	    if (id1.gvals[pos]==127) {
//  		int dist=Integer.MAX_VALUE;
//  		for (int pos2=0; pos2<size; pos2++) {
//  		    if (id2.gvals[pos2]==127) {
//  			int newDist=(pos%width)-(pos2%width);
//  			int newDist2=(pos/width)-(pos/width);
//  			if ((newDist=newDist*newDist+newDist2*newDist2)<dist) {
//  			    dist = newDist;
//  			}
//  		    }
//  		}
//  		match+=dist;
//  	    }
//  	}
//  	System.out.println("Distance: "+match);
//  	return match<threshold;
	return true;
    }

    /** A faster version of the above. */
    private boolean matchFast(ImageData id1, ImageData id2) {
//  	if ((id1.width!=id2.width)||(id1.height!=id2.height)) {
//  	    throw new Error("Incorrect width/height of input image.");
//  	}

//  	int match = 0;
//  	int width = id1.width;
//  	int height = id1.height;
	
//  	for (int pos=0; pos<size; pos++) {
//  	    if (id1.gvals[pos]==127) {

//  	    }

//  	}

	return true;
    }

}
