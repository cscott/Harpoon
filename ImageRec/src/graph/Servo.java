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
	//System.out.println("Servo.process()");
	switch (Command.read(id)) {
	case Command.SERVO_LEFT: {
	    //System.out.println("Servo: turning left for "+id.time);
	    imagerec.util.Servo.left(id.time); 
	    break; 
	}
	case Command.SERVO_RIGHT: { 
	    //System.out.println("Servo: turning right for "+id.time);
	    imagerec.util.Servo.right(id.time); 
	    break;
	}
	case Command.SERVO_FORWARD: {
	    //System.out.println("Servo: going forward for "+id.time);
	    imagerec.util.Servo.forward(id.time);
	    break;
	}
	case Command.SERVO_BACKWARD: {
	    //System.out.println("Servo: going backward for "+id.time);
	    imagerec.util.Servo.backward(id.time);
	    break;
	}
	case Command.SERVO_LEFT_CONTINUE: {
	    //System.out.println("Servo: going left indefinitely");
	    imagerec.util.Servo.left();
	    break;
	}
	case Command.SERVO_RIGHT_CONTINUE: {
	    //System.out.println("Servo: going right indefinitely");
	    imagerec.util.Servo.right();
	    break;
	}
	case Command.SERVO_FORWARD_CONTINUE: {
	    //System.out.println("Servo: going forward indefinitely");
	    imagerec.util.Servo.forward();
	    break;
	}
	case Command.SERVO_BACKWARD_CONTINUE: {
	    //System.out.println("Servo: going backward indefinitely");
	    imagerec.util.Servo.backward();
	    break;
	}
	case Command.SERVO_STOP_TURN: {
	    //System.out.println("Servo: straightening wheels");
	    imagerec.util.Servo.stop_turn();
	    break;
	}
	case Command.SERVO_STOP_MOVING: {
	    //System.out.println("Servo: stopping movement");
	    imagerec.util.Servo.stop_moving();
	    break;
	}
	default: {}
	}
    }

}
