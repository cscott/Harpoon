// IPaqServoController.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package servo;

/**
 * {@link IPaqServoController} is a stub that simulates a car.
 * This class is overridden by FLEX to provide an interface to the
 * actual car.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class IPaqServoController {
    /** Should I provide output describing the actions of the car? */
    public static final boolean OUTPUT = true;

    /** Construct a new IPaqServoController to control the car. */
    public IPaqServoController() {}

    /** <code>move</code> simulates setting the ESC unit and servos of the car. 
     *
     * @param servo The servo to set (1-8).
     * @param position The position to set it to (1-255).
     */
    public void move(int servo, int position) {
	if ((servo>8)||(servo<1)) {
	    throw new RuntimeException("Servo is out of range.");
	}
	if ((position>255)||(position<1)) {
	    throw new RuntimeException("Position is out of range.");
	}
	if (OUTPUT) {
	    if (servo == 2) {
		if (position >= 200) {
		    System.out.println("Moving forward very fast!");
		} else if (position >= 150) {
		    System.out.println("Moving forward.");
		} else if (position >= 130) {
		    System.out.println("Trying to move forward, but the motor won't start.");
		} else if (position <= 50) {
		    System.out.println("Moving backward very fast!");
		} else if (position <= 110) {
		    System.out.println("Moving backward.");
		} else if (position <= 126 ) {
		    System.out.println("Trying to move backward, but the motor won't start.");
		} else {
		    System.out.println("Stopped moving forward or backward.");
		}
	    } else if (servo == 1) {
		if (position >= 200) {
		    System.out.println("Front wheels turned far to the right.");
		} else if (position >= 130) {
		    System.out.println("Front wheels tipped to the right.");
		} else if (position <= 125) {
		    System.out.println("Front wheels tipped to the left.");
		} else if (position <= 50) {
		    System.out.println("Front wheels turned far to the left.");
		} else {
		    System.out.println("Stopped turning.");
		}
	    } else {
		System.out.println("What servo is this?");
	    }
	}
    }
}
