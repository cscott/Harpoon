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
    private Display inputDisp = new Display("input");
    private Display matchDisp = new Display("matcher");

    private ImageData[] iddb;
    private String filePrefix;
    private int numFiles;
    private int threshold;
    private int[] order;

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
	
	int width = 50;
	int height = 50;

	/* Precompute search ordering. */
	order = new int[(width+1)*(height+1)*4]; 
	int maxDist = width*width+height*height;
	int i=0;
	for (int dist=0; dist<=maxDist; dist++) {
	    for (int y=-height; y<=height; y++) {
		for (int x=-width; x<=width; x++) {
		    if ((x*x+y*y)==dist) {
			order[i++] = y*width+x;
		    }
		}
	    }
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
	inputDisp.process(id1);
	matchDisp.process(id2);
	int size = id2.width*id2.height;
	long match = 0;
	long numPixels = 0;
	for (int pos=0; pos<size; pos++) {
	    if (id1.gvals[pos]>0) {
		int count = 0;
		numPixels++;
		for (int i=0; (i<size)&&(count<9); i++) {
		    int newIdx = order[i]+pos;
		    if ((newIdx<0)||(newIdx>size)) {
			count++;
		    } else {
			count=0;
			if (id2.gvals[newIdx]>0) {
			    match += i;
			}
		    }
		}

	    }

	}

  	System.out.println("Distance: "+match+" Pixels: "+numPixels+" thresh: "+
			   (((double)match)/((double)numPixels))+" threshold: "+
			   threshold);
  	return numPixels*threshold>match;
    }
}
