// LabelControl.java, created by benster 6/1/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;


/**
 * This class is intended to be in a pipeline that runs asynchronously
 * with a pipeline containing a {@link Label} node that is
 * configured for object tracking.<br><br>
 *
 * It allows for user control of label properties
 * when coupled with an input node like {@link LabelControlKeyboard}.<br><br>
 *
 * The following properties of {@link Label} may be modified:<br>
 *     <b>Time Between Sends:</b> The minimum amount of time between
 * when {@link Label} sends out {@link ImageData}s intended to
 * generate Alerts (per selected object).<br>
 * <br>
 * To set a particular value:<br>
 *   Tag an {@link ImageData} with one of the following {@link Command}
 * tags and, if necessary, set the appropriate value to the {@link ImageData}'s
 * <code>time</code> field.<br><br>
 *
 * Command.SET_TIME: Causes the {@link Label}'s "Time Between Sends" property
 * to be set to the value given in the {@link ImageData}'s <code>time</code>
 * field.<br>
 * Command.TIME_UP: Causes the {@link Label}'s
 * "Time Between Sends" property
 * to be incremented by DELTA_TIME.<br>
 * Command.TIME_DOWN: Causes the {@link Label}'s
 * "Time Between Sends" property
 * to be decremented by DELTA_TIME.<br>
 * <br>
 * If the value specifed in the {@link ImageData}'s <code>time</code>
 * field is not within the appropriate range specified by this class's
 * constants, then the highest or lowest possible value
 * is substituted.
 * 
 * @see Label
 * @see LabelControlKeyboard
 * @see Command
 * @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
 */
public class LabelControl extends Node {
    /**
     * The {@link Label} that this {@link LabelControl}
     * object will mutate.
     */
    private Label myLabel;

    public static final int MAX_TIME = 3000;
    public static final int MIN_TIME = 0;
    public static final int DEFAULT_TIME = 2000;
    public static final int DELTA_TIME = 100;


    /**
     * Constructs a {@link LabelControl} object that will
     * mutate the specified {@link Label} node.
     *
     * @param l The {@link Label} that will be mutated by 
     * this {@link LabelControl} node.
     */
    public LabelControl(Label l) {
	super(null);
	init(l);
    }

    /**
     * This method should be called by all constructors
     * to properly initialize object fields
     */
    private void init(Label l) {
	myLabel = l;
	myLabel.setTimeBetweenSends(DEFAULT_TIME);
    }

    /**
     * Processes the specified {@link ImageData} and sets the
     * appropriate field in the {@link Label} provided
     * in the constructor.
     *
     * @param id The ImageData to which commands and appropriate values are
     * attached.
     */
    public void process(ImageData id) {
	long currentTime = myLabel.getTimeBetweenSends();
	switch (id.command) {
	case (Command.TIME_DOWN): {
	    currentTime -= DELTA_TIME;
	    if (currentTime < MIN_TIME)
		currentTime = MIN_TIME;
	    System.out.println("LabelControl: Time="+currentTime);
	    myLabel.setTimeBetweenSends(currentTime);
	    break;
	}
	case (Command.TIME_UP): {
	    currentTime += DELTA_TIME;
	    if (currentTime > MAX_TIME)
		currentTime = MAX_TIME;
	    System.out.println("LabelControl: Time="+currentTime);
	    myLabel.setTimeBetweenSends(currentTime);
	}
	case (Command.SET_TIME): {
	    currentTime = id.time;
	    if (currentTime > MAX_TIME)
		currentTime = MAX_TIME;
	    if (currentTime < MIN_TIME)
		currentTime = MIN_TIME;
	    System.out.println("LabelControl: Time="+currentTime);
	    myLabel.setTimeBetweenSends(currentTime);
	    break;
	}
	default: {
	    System.out.println("LabelControl: Unrecognized command");
	}
	}
	super.process(id);
    }
}
