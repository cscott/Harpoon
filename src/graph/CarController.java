// CarController.java, created by benster 5/29/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;

import java.awt.event.WindowAdapter;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;

/**
 * THIS CLASS MAKES THE ASSUMPTION THAT IT CAN PROCESS
 * IMAGEDATAS/ALERTS FASTER THAN THEY CAN COME IN. IF 
 * THIS IS NOT TRUE, THEN THERE WILL BE CONTENTION
 * OF SERVO COMMANDS
 */
public class CarController extends Node {

    private boolean enabled = false;

    private static final float idealX = 160;
    private static final float idealY = 120;

    private InterruptTimer timer;
    private EnableFrame frame;

    public CarController() {
	super(null);
	init();
    }
    
    private void init() {
	timer = new InterruptTimer();
	frame = new EnableFrame();
    }

    public void setEnabled(boolean b) {
	this.enabled = b;
	if (!b)
	    this.timer.stop();
    }

    public void process(ImageData id) {
	//id should have c1, c2, c3 set
	//either can come after RangeFind or
	//be the result of an alert call
	float x = id.c1;//pixel x location of target
        float y = id.c2;//pixel x location of target
	float z = id.c3;//distance to target in inches

        final float epsilon = 10;
	
	ImageData moveCommand = null;
	ImageData dirCommand = null;
	if (enabled) {
	    //if too far, then always move foward
	    if (y < idealY-epsilon) {
		moveCommand =
		    ImageDataManip.create(Command.SERVO_FORWARD_CONTINUE, 0);
		//if too much to left, turn wheels left 
		if (x < idealX-epsilon) {
		    System.out.println("CarController: moving forward and left");
		    dirCommand =
			ImageDataManip.create(Command.SERVO_LEFT_CONTINUE, 0);
		}
		//else turn wheels right
		else if (x > idealX+epsilon){
		    System.out.println("CarController: moving forward and right");
		    dirCommand =
			ImageDataManip.create(Command.SERVO_RIGHT_CONTINUE, 0);
		}
		else {
		}
	    }
	    //else if tank is too close move backwards
	    else if (y > idealY+epsilon){
		moveCommand =
		    ImageDataManip.create(Command.SERVO_BACKWARD_CONTINUE, 0);
		//if too much to left, turn wheels right 
		if (x < idealX-epsilon) {
		    System.out.println("CarController: moving backward and left");
		    dirCommand =
			ImageDataManip.create(Command.SERVO_RIGHT_CONTINUE, 0);
		}
		//else turn wheels left
		else if (x > idealX+epsilon) {
		    System.out.println("CarController: moving backward and right");
		    dirCommand =
			ImageDataManip.create(Command.SERVO_LEFT_CONTINUE, 0);
		}
	    }
	    //else [tank is at just the right distance]
	    else {
		//if tank is too far left, turn wheels right,
		//and move backwards
		if (x < idealX-epsilon) {
		    System.out.println("CarController: moving backward and right");
		    moveCommand =
			ImageDataManip.create(Command.SERVO_BACKWARD_CONTINUE, 0);
		    dirCommand =
			ImageDataManip.create(Command.SERVO_RIGHT_CONTINUE, 0);		
		}
		//else if tank is too far right, turn wheels left
		//and move backwards
		else if (x > idealX+epsilon) {
		    System.out.println("CarController: moving backward and left");
		    moveCommand =
			ImageDataManip.create(Command.SERVO_BACKWARD_CONTINUE, 0);
		    dirCommand =
			ImageDataManip.create(Command.SERVO_LEFT_CONTINUE, 0);		
		}
		//else [tank is centered, stop wheels]
		else {
		    System.out.println("CarController: stopping and centering");
		    moveCommand =
			ImageDataManip.create(Command.SERVO_STOP_MOVING, 0);
		    dirCommand =
			ImageDataManip.create(Command.SERVO_STOP_TURN, 0);
		}
		
	    }
	    
	    timer.reset();
	}
	//[not enabled]
	else {
	    System.out.println("CarController: not enabled, so stopping and centering");
	    moveCommand =
		ImageDataManip.create(Command.SERVO_STOP_MOVING, 0);
	    dirCommand =
		ImageDataManip.create(Command.SERVO_STOP_TURN, 0);
	}
	super.process(dirCommand);
	super.process(moveCommand);
    }

    private class InterruptTimer implements Runnable {
	
	private long lastCommandTime;
	private boolean started = false;

	//delay could be as long as "timeUntilInterrupt+sleepTime" milliseconds
	private static final long sleepTime = 200;
	private static final long timeUntilInterrupt = 2000;

	InterruptTimer() {
	    Thread t = new Thread(this);
	    t.start();
	}

	public void reset() {
	    lastCommandTime = System.currentTimeMillis();
	    if (!started)
		started = true;
	}

	public void stop() {
	    started = false;
	}

	public void run() {
	    boolean keepGoing = true;
	    while (keepGoing) {
		try {
		    Thread.currentThread().sleep(sleepTime);
		}
		catch (InterruptedException e) {
		}
		if (started &&
		    (System.currentTimeMillis()-lastCommandTime > timeUntilInterrupt)) {
		    ImageData servoCommand;
		    servoCommand =
			ImageDataManip.create(Command.SERVO_STOP_MOVING, 0);
		    CarController.super.process(servoCommand);
		    servoCommand =
			ImageDataManip.create(Command.SERVO_STOP_TURN, 0);
		    CarController.super.process(servoCommand);
		    stop();
		}
	    }
	}
    }

    private class EnableFrame extends Frame
	implements ActionListener {
	Button enableDisableButton;
	EnableFrame() {
	    super("Enable/Disable Car");
	    this.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			System.out.println("Cannot close the 'Enable/Disable Car' window.");
		    }
		});
	    this.setLayout(new BorderLayout());
	    enableDisableButton = new Button("Enable");
	    enableDisableButton.addActionListener(this);
	    this.add(enableDisableButton, BorderLayout.CENTER);
	    this.setSize(new Dimension(300, 150));
	    this.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
	    if (e.getActionCommand().compareTo("Enable") == 0) {
		CarController.this.setEnabled(true);
		enableDisableButton.setLabel("Disable");
		System.out.println("CarController: Controller ENABLED");
	    }
	    else {
		CarController.this.setEnabled(false);
		enableDisableButton.setLabel("Enable");
		System.out.println("CarController: Controller DISABLED");
	    }
	}
    }
}
