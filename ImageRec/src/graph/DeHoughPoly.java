// DeHoughPoly.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * A {@link DeHoughPoly} node can take a list of lines and generate
 * <code>r vs. t</code> graphs from them.
 *
 * @see Hough HoughPoly DeHough
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class DeHoughPoly extends Node {
    private final String filePrefix;
    private final int number;
    
    /** Construct a {@link DeHoughPoly} which will take a list of lines
     *  and generate <code>r vs. t</code> graphs from them.
     *
     *  @param filePrefix The prefix of the name of the files to load the
     *                    list of lines from.  The file names will be
     *                    <code>filePrefix.#</code>.
     *  @param number The number of files to load.
     *  @param out The node to send the <code>r vs. t</code> graphs to.
     */
    public DeHoughPoly(String filePrefix, int number, Node out) {
	super(out);
	this.filePrefix = filePrefix;
	this.number = number;
    }

    /** This method starts sending <code>r vs. t</code> graphs to the
     *  <code>out</code> node.  Use the <code>run()</code> method
     *  to start a {@link DeHoughPoly}.
     * 
     *  @param id This {@link ImageData} is ignored.
     */
    public void process(ImageData id) {

	super.process(id);
    }

}
