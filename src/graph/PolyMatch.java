// PolyMatch.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/** 
 * A {@link PolyMatch} node can match an image against a database
 * of polygons.
 *
 * @see Hough HoughPoly
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class PolyMatch extends Node {
    private class Poly {
	private int[] r, t, intensity;
    }

    private final Poly[] dbase;

    private final int threshold;

    /** Construct a {@link PolyMatch} node which will match input
     *  images to a set of polygons and return images that match above
     *  a given threshold.
     *
     *  @param filePrefix The prefix of the name of the files to load
     *                    the list of lines from.  The file names will be
     *                    <code>filePrefix.#</code>.
     *  @param number The number of files to load.
     *  @param threshold The acceptance threshold.
     *  @param out The node to send matched images to.
     */
    public PolyMatch(String filePrefix, int number, int threshold, Node out) {
	super(out);
	this.threshold = threshold;
	dbase = null;
	// ...
    }

    /** This method will match an {@link ImageData} against a set of 
     *  polygons and will send those that score above the given threshold
     *  onto the <code>out</code> node.
     *
     *  @param id The {@link ImageData} to match.
     */
    public synchronized void process(ImageData id) {
	
	super.process(id);
    }

}
