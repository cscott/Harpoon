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

    /** This is also a request to retrieve an image from the {@link Cache}.
     Look at the code to see the difference.*/
    public static final int GET_NEXT_IMAGE = 4;

    /** This is a request to retrieve an image from the {@link Cache} 
     *  and crop it to the specified dimensions. 
     */
    public static final int GET_CROPPED_IMAGE = 2;

    /** This is the image returned from a request to a {@link Cache}. */
    public static final int RETRIEVED_IMAGE = 3;

    /** Tag an image with this command if the image is only intended for calibration of a
     *	particular node, and should not be passed along to subsequent nodes.
     *	Created initially for use by LabelBlue.java
     *	--Benji
     */
    public static final int CALIBRATION_IMAGE = 100;

    /**
     * This command created for use by LabelBlue.java
     * Tag an image with this command if you want the LabelBlue
     * node to only check to see if a sufficient amount of
     * blue exists in the image instead of performing an exhastive search.
     * In this case, the entire image will be passed on to the next
     * node rather than an image cropped around where the blue was
     * found.
     * --Benji
     */
    public static final int CHECK_FOR_BLUE = 110;

    /**
     * This command created for use with Switch.java.
     * @see Switch
     */
    public static final int GO_LEFT = 120;
    
    /**
     * This command created for use with Switch.java
     * @see Switch
     */
    public static final int GO_RIGHT = 121;

    
    /**
     * This command created for use with Switch.java
     * @see Switch
     */
    public static final int GO_BOTH = 122;
    
    /**
     * Move car to the left
     * @see Servo
     */
    public static final int SERVO_LEFT = 201;
    
    /**
     * Move car to the right
     * @see Servo
     */
    public static final int SERVO_RIGHT = 202;
    
    /**
     * Move car forward
     * @see Servo
     */
    public static final int SERVO_FORWARD = 203;
    
    /**
     * Move car backward
     * @see Servo
     */
    public static final int SERVO_BACKWARD = 204;

    /**
     * Move car left and continue moving left.
     * @see Servo
     */
    public static final int SERVO_LEFT_CONTINUE = 205;
    
    /**
     * Move car right and continue moving right.
     * @see Servo
     */
    public static final int SERVO_RIGHT_CONTINUE = 206;
    
    /**
     * Move car forward and continue moving forward.
     * @see Servo
     */
    public static final int SERVO_FORWARD_CONTINUE = 207;
    
    /**
     * Move car backward and continue moving backward.
     * @see Servo
     */
    public static final int SERVO_BACKWARD_CONTINUE = 208;
    
    /**
     * Stop the car from turning.
     * @see Servo
     */
    public static final int SERVO_STOP_TURN = 209;
    
    /**
     * Stop the car from moving.
     * @see Servo
     */
    public static final int SERVO_STOP_MOVING = 210;

    public static final int CONTRAST_UP = 300;
    public static final int CONTRAST_DOWN = 301;
    public static final int GAIN_DOWN = 302;
    public static final int GAIN_UP = 303;
    public static final int BRIGHTNESS_UP = 304;
    public static final int BRIGHTNESS_DOWN = 305;
    public static final int FRAME_RATE_UP = 306;
    public static final int FRAME_RATE_DOWN = 307;

    public static final int SET_CONTRAST = 310;
    public static final int SET_GAIN = 311;
    public static final int SET_BRIGHTNESS = 312;
    public static final int SET_FRAME_RATE = 313;

    public static final int TIME_UP = 330;
    public static final int TIME_DOWN = 331;

    public static final int SET_TIME = 332;

    public static final int BACKWARD_SPEED_UP = 340;
    public static final int BACKWARD_SPEED_DOWN = 341;
    public static final int FORWARD_SPEED_UP = 342;
    public static final int FORWARD_SPEED_DOWN = 343;
    public static final int INTERRUPT_TIME_UP = 350;
    public static final int INTERRUPT_TIME_DOWN = 351;
    public static final int TURN_AMOUNT_UP = 352;
    public static final int TURN_AMOUNT_DOWN = 353;
    
    public static final int SET_BACKWARD_SPEED = 360;
    public static final int SET_FORWARD_SPEED = 361;
    public static final int SET_INTERRUPT_TIME = 362;
    public static final int SET_TURN_AMOUNT = 363;

    public static final int IS_TANK = 400;
    public static final int IS_NOT_TANK = 401;

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
    public void process(ImageData id) {
	//if (tag == GO_RIGHT)
	//  System.out.println("Adding command: GO_RIGHT");
	tag(id, this.tag);
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

    public static void tag(ImageData id, int tag) {
	id.command = tag;
    }
}
