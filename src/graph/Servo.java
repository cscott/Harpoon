// Servo.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * {@link Servo} is a {@link Node} which takes in specially formatted
 * ImageData's and parses them to control the servos.
 *
 * @see imagerec.util.Servo
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Servo extends Node {

    /** Construct a {@link Servo} to parse {@link ImageData}'s into servo commands. */
    public Servo() {
    }

    /** Process an {@link ImageData}, converting it into a series of {@link Servo} commands. 
     * 
     *  @param id ImageData with <code>.time</code> and <code>.command</code> which
     *            control the servos.
     */
    public void process(ImageData id) {
	switch (Command.read(id)) {
	case Command.SERVO_LEFT: { 
	    imagerec.util.Servo.left(id.time); 
	    break; 
	}
	case Command.SERVO_RIGHT: { 
	    imagerec.util.Servo.right(id.time); 
	    break;
	}
	case Command.SERVO_FORWARD: {
	    imagerec.util.Servo.forward(id.time);
	    break;
	}
	case Command.SERVO_BACKWARD: {
	    imagerec.util.Servo.backward(id.time);
	    break;
	}
	case Command.SERVO_LEFT_CONTINUE: {
	    imagerec.util.Servo.left();
	    break;
	}
	case Command.SERVO_RIGHT_CONTINUE: {
	    imagerec.util.Servo.right();
	    break;
	}
	case Command.SERVO_FORWARD_CONTINUE: {
	    imagerec.util.Servo.forward();
	    break;
	}
	case Command.SERVO_BACKWARD_CONTINUE: {
	    imagerec.util.Servo.backward();
	    break;
	}
	case Command.SERVO_STOP_TURN: {
	    imagerec.util.Servo.stop_turn();
	    break;
	}
	case Command.SERVO_STOP_MOVING: {
	    imagerec.util.Servo.stop_moving();
	    break;
	}
	default: {}
	}
    }

}
