// Servo.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.util;

import ipaq.ServoThread;

/**
 * {@link Servo} maneuvers the vehicle.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Servo {
    private static final ServoThread servo1 = new ServoThread(1);
    private static final ServoThread servo2 = new ServoThread(2);

    /**
     * Move the vehicle forward.
     * 
     * @param time The amount of time (in milliseconds) to spend moving
     *             the car forward.
     */
    public static void forward(long time) {
	forward(time, 22);
    }
    
    /**
     * Move the vehicle forward at a specified speed.
     *
     * @param speed A number from 0 to 127 to indicate the speed
     *              to move forward.  Careful!  The car is very fast!
     * @param time The amount of time (in milliseconds) to spend moving
     *             the car forward.
     */              
    public static void forward(long time, int speed) {
	servo2.command((byte)(128+speed), time, (byte)128);
    }

    /**
     * Move the vehicle backward.
     *
     * @param time The amount of time (in milliseconds) to spend moving
     *             the car backward.
     */
    public static void backward(long time) {
	backward(time, 18);
    }

    /**
     * Move the vehicle backward at a specified speed.
     *
     * @param speed A number from 0 to 127 to indicate the speed
     *              to move backward.  Careful!  The car is very fast!
     * @param time The amount of time (in milliseconds) to spend moving
     *             the car backward.
     */
    public static void backward(long time, int speed) {
	servo2.command((byte)(128-speed), time, (byte)128);
    }

    /**
     * Turn the front wheels of the vehicle to the left.
     *
     * @param time The amount of time (in milliseconds) to spend with
     *             the steering locked to the left.
     */
    public static void left(long time) {
	left(time, 127);
    }

    /**
     * Turn the front wheels of the vehicle to the left to a specified degree.
     *
     * @param time The amount of time (in milliseconds) to spend with
     *             the steering locked to the left.
     * @param degree A number from 0 to 127 indicating the degree 
     *               to which the front wheels should turn left.
     */
    public static void left(long time, int degree) {
	servo1.command((byte)(128-degree), time, (byte)128);
    }
     
    /**
     * Turn the front wheels of the vehicle to the right.
     *
     * @param time The amount of time (in milliseconds) to spend with
     *             the steering locked to the right.
     */
    public static void right(long time) {
	right(time, 127);
    }

    /**
     * Turn the front wheels of the vehicle to the right to a specified degree.
     *
     * @param time The amount of time (in milliseconds) to spend with
     *             the steering locked to the right.
     * @param degree A number from 0 to 127 indicating the degree
     *               to which the front wheels should turn right.
     */
    public static void right(long time, int degree) {
	servo1.command((byte)(128+degree), time, (byte)128);
    }
}

