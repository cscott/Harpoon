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

    /** Calibration data. */
    private static final int FORWARD_SPEED = 22;
    private static final int BACKWARD_SPEED = 18;
    private static final int LEFT_AMOUNT = 127;
    private static final int RIGHT_AMOUNT = 127;
    private static final int SERVO1_CENTER = 128;
    private static final int SERVO2_CENTER = 128;
    
    /**
     * Start moving the vehicle forward, but don't stop.
     */
    public static void forward() {
	servo2.command(SERVO2_CENTER+FORWARD_SPEED, 0, SERVO2_CENTER+FORWARD_SPEED);
    }
    
    /**
     * Move the vehicle forward.
     * 
     * @param time The amount of time (in milliseconds) to spend moving
     *             the car forward.
     */
    public static void forward(long time) {
	forward(time, FORWARD_SPEED);
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
	servo2.command(SERVO2_CENTER+speed, time, SERVO2_CENTER);
    }
    
    /**
     * Start moving the vehicle backward, but don't stop.
     */
    public static void backward() {
	servo2.command(SERVO2_CENTER-BACKWARD_SPEED, 0, SERVO2_CENTER-BACKWARD_SPEED);
    }
    
    /**
     * Move the vehicle backward.
     *
     * @param time The amount of time (in milliseconds) to spend moving
     *             the car backward.
     */
    public static void backward(long time) {
	backward(time, BACKWARD_SPEED);
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
	servo2.command(SERVO2_CENTER-speed, time, SERVO2_CENTER);
    }

    /** 
     * Turn the front wheels left and don't stop.
     */
    public static void left() {
	servo1.command(SERVO1_CENTER-LEFT_AMOUNT, 0, SERVO1_CENTER-LEFT_AMOUNT);
    }

    /**
     * Turn the front wheels of the vehicle to the left.
     *
     * @param time The amount of time (in milliseconds) to spend with
     *             the steering locked to the left.
     */
    public static void left(long time) {
	left(time, LEFT_AMOUNT);
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
	servo1.command(SERVO1_CENTER-degree, time, SERVO1_CENTER);
    }
     
    /**
     * Turn the front wheels of the vehicle to the right and don't stop.
     */
    public static void right() {
	servo1.command(SERVO1_CENTER+RIGHT_AMOUNT, 0, SERVO1_CENTER+RIGHT_AMOUNT);
    }

    /**
     * Turn the front wheels of the vehicle to the right.
     *
     * @param time The amount of time (in milliseconds) to spend with
     *             the steering locked to the right.
     */
    public static void right(long time) {
	right(time, RIGHT_AMOUNT);
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
	servo1.command(SERVO1_CENTER+degree, time, SERVO1_CENTER);
    }

    /**
     * Stop turning.
     */
    public static void stop_turn() {
	servo1.command(SERVO1_CENTER, 0, SERVO1_CENTER);
    }
    
    /**
     * Stop moving forward or backward.
     */
    public static void stop_moving() {
	servo2.command(SERVO2_CENTER, 0, SERVO2_CENTER);
    }
}

