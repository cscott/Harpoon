// Servo.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.util;

import servo.IPaqServoController;

/**
 * {@link Servo} maneuvers the vehicle.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Servo {
    static final IPaqServoController car = new IPaqServoController();

    private static final Servo servo1 = new ServoThread(1);
    private static final Servo servo2 = new ServoThread(2);

    /**
     * {@link ServoThread} is a thread which controls the servos.
     *
     * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
     */
    class ServoThread extends Thread {
	private final int servo;
	private byte start, stop;
	private long time;

	/** 
	 * Construct a new servo controlling thread and start it.
	 *
	 * @param servo the servo number to control (1-8).
	 */
	ServoThread(int servo) { 
	    this.servo = servo;
	    start(); 
	}

	/** 
	 * Execute a command on the servo.
	 */
	public synchronized void run() {
	    while (true) {
		wait();
		car.moveLocal(servo, start);
		sleep(time);
		car.moveLocal(servo, stop);
	    }
	}

	/** 
	 * Send a command to the servo.
	 *
	 * @param start The starting motion.
	 * @param time How long to do it (in milliseconds).
	 * @param stop The stopping motion.
	 */
	synchronized void command(byte start, long time, byte stop) {
	    this.start = start;
	    this.time = time;
	    this.stop = stop;
	    notify();
	}
    }

    /**
     * Move the vehicle forward.
     * 
     * @param time The amount of time (in milliseconds) to spend moving
     *             the car forward.
     */
    public void forward(long time) {
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
    public void forward(long time, byte speed) {
	servo2.command(128+speed, time, 128);
    }

    /**
     * Move the vehicle backward.
     *
     * @param time The amount of time (in milliseconds) to spend moving
     *             the car backward.
     */
    public void backward(long time) {
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
    public void backward(long time, byte speed) {
	servo2.command(128-speed, time, 128);
    }

    /**
     * Turn the front wheels of the vehicle to the left.
     *
     * @param time The amount of time (in milliseconds) to spend with
     *             the steering locked to the left.
     */
    public void left(long time) {
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
    public void left(long time, byte degree) {
	servo1.command(128-degree, time, 128);
    }
     
    /**
     * Turn the front wheels of the vehicle to the right.
     *
     * @param time The amount of time (in milliseconds) to spend with
     *             the steering locked to the right.
     */
    public void right(long time) {
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
    public void right(long time, byte degree) {
	servo1.command(128+degree, time, 128);
    }
}

