// Command.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details
package imagerec.graph;

/**
 * {@link Command} is a node which tags an image and sends a command to another node.
 * A node can later read the tag.  An image can only be tagged with one command at a time.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Command extends Node {

    /** This image is an ordinary image from the original stream. */ 
    public static final int NONE = 0;

    /** This is a request to retrieve an image from the {@link Cache}. */
    public static final int GET_IMAGE = 1;

    /** This is a request to retrieve an image from the {@link Cache} 
     *  and crop it to the specified dimensions. 
     */
    public static final int GET_CROPPED_IMAGE = 2;

    /** This is the image returned from a request to a {@link Cache}. */
    public static final int RETRIEVED_IMAGE = 3;

    private int tag;

    /** Construct a new {@link Command} node which will tag every image with a command. 
     *
     *  @param tag The command to tag the images.
     *  @param out The node to send tagged images to.
     */
    public Command(int tag, Node out) {
	super(out);
	this.tag = tag;
    }

    /** Tags images passing through this node. 
     *
     *  @param id The {@link ImageData} for the image to be tagged.
     */
    public synchronized void process(ImageData id) {
	id.command = tag;
	super.process(id);
    }

    /** Read a tag on an image. 
     *
     *  @param id The {@link ImageData} that has a tag to be read.
     *  @return The read tag.
     */
    public static int read(ImageData id) {
	return id.command;
    }
}
