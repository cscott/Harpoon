// CarContollerControl.java, created by benster 6/2/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

public class CarControllerControl extends Node {

    private CarController myCarController;

    private static final int MAX_INTERRUPT_TIME = 5000;
    private static final int MIN_INTERRUPT_TIME = 200;
    private static final int DEFAULT_INTERRUPT_TIME = 500;
    private static final int DELTA_INTERRUPT_TIME = 100;
    private int currentInterruptTime;

    private static final int MAX_BACKWARD_SPEED = 50;
    private static final int MIN_BACKWARD_SPEED = 10;
    private static final int DEFAULT_BACKWARD_SPEED = 15;
    private static final int DELTA_BACKWARD_SPEED = 1;
    private int currentBackwardSpeed;

    private static final int MAX_FORWARD_SPEED = 50;
    private static final int MIN_FORWARD_SPEED = 9;
    private static final int DEFAULT_FORWARD_SPEED = 14;
    private static final int DELTA_FORWARD_SPEED = 1;
    private int currentForwardSpeed;

    private static final int MAX_TURN_AMOUNT = 127;
    private static final int MIN_TURN_AMOUNT = 0;
    private static final int DEFAULT_TURN_AMOUNT = 100;
    private static final int DELTA_TURN_AMOUNT = 5;;
    private int currentTurnAmount;
    

    public CarControllerControl(CarController cc) {
	super(null);
	init(cc);
    }
    
    private void init(CarController cc) {
	myCarController = cc;
	currentInterruptTime = DEFAULT_INTERRUPT_TIME;
	currentForwardSpeed = DEFAULT_FORWARD_SPEED;
	currentBackwardSpeed = DEFAULT_BACKWARD_SPEED;
	myCarController.setInterruptTime(currentInterruptTime);
	imagerec.util.Servo.setForwardSpeed(currentForwardSpeed);
	imagerec.util.Servo.setBackwardSpeed(currentBackwardSpeed);
	imagerec.util.Servo.setTurnAmount(currentTurnAmount);
    }

    public void process(ImageData id) {
	switch (id.command) {
	case (Command.INTERRUPT_TIME_DOWN): {
	    currentInterruptTime -= DELTA_INTERRUPT_TIME;
	    if (currentInterruptTime < MIN_INTERRUPT_TIME)
		currentInterruptTime = MIN_INTERRUPT_TIME;
	    System.out.println("CarControl: InterruptTime="+currentInterruptTime);
	    myCarController.setInterruptTime(currentInterruptTime);
	    break;
	}
	case (Command.INTERRUPT_TIME_UP): {
	    currentInterruptTime += DELTA_INTERRUPT_TIME;
	    if (currentInterruptTime > MAX_INTERRUPT_TIME)
		currentInterruptTime = MAX_INTERRUPT_TIME;
	    System.out.println("CarControl: InterruptTime="+currentInterruptTime);
	    myCarController.setInterruptTime(currentInterruptTime);
	    break;
	}
	case (Command.BACKWARD_SPEED_DOWN): {
	    currentBackwardSpeed -= DELTA_BACKWARD_SPEED;
	    if (currentBackwardSpeed < MIN_BACKWARD_SPEED)
		currentBackwardSpeed = MIN_BACKWARD_SPEED;
	    System.out.println("CarControl: BackwardSpeed="+currentBackwardSpeed);
	    imagerec.util.Servo.setBackwardSpeed(currentBackwardSpeed);
	    break;
	}
	case (Command.BACKWARD_SPEED_UP): {
	    currentBackwardSpeed += DELTA_BACKWARD_SPEED;
	    if (currentBackwardSpeed > MAX_BACKWARD_SPEED)
		currentBackwardSpeed = MAX_BACKWARD_SPEED;
	    System.out.println("CarControl: BackwardSpeed="+currentBackwardSpeed);
	    imagerec.util.Servo.setBackwardSpeed(currentBackwardSpeed);
	    break;
	}
	case (Command.FORWARD_SPEED_DOWN): {
	    currentForwardSpeed -= DELTA_FORWARD_SPEED;
	    if (currentForwardSpeed < MIN_FORWARD_SPEED)
		currentForwardSpeed = MIN_FORWARD_SPEED;
	    System.out.println("CarControl: ForwardSpeed="+currentForwardSpeed);
	    imagerec.util.Servo.setForwardSpeed(currentForwardSpeed);
	    break;
	}
	case (Command.FORWARD_SPEED_UP): {
	    currentForwardSpeed += DELTA_FORWARD_SPEED;
	    if (currentForwardSpeed > MAX_FORWARD_SPEED)
		currentForwardSpeed = MAX_FORWARD_SPEED;
	    System.out.println("CarControl: ForwardSpeed="+currentForwardSpeed);
	    imagerec.util.Servo.setForwardSpeed(currentForwardSpeed);
	    break;
	}
	case (Command.TURN_AMOUNT_DOWN): {
	    currentTurnAmount -= DELTA_TURN_AMOUNT;
	    if (currentTurnAmount < MIN_TURN_AMOUNT)
		currentTurnAmount = MIN_TURN_AMOUNT;
	    System.out.println("CarControl: TurnAmount="+currentTurnAmount);
	    imagerec.util.Servo.setTurnAmount(currentTurnAmount);
	    break;
	}
	case (Command.TURN_AMOUNT_UP): {
	    currentTurnAmount += DELTA_TURN_AMOUNT;
	    if (currentTurnAmount > MAX_TURN_AMOUNT)
		currentTurnAmount = MAX_TURN_AMOUNT;
	    System.out.println("CarControl: TurnAmount="+currentTurnAmount);
	    imagerec.util.Servo.setTurnAmount(currentTurnAmount);
	    break;
	}
	case (Command.SET_INTERRUPT_TIME): {
	    currentInterruptTime = (int)id.time;
	    if (currentInterruptTime > MAX_INTERRUPT_TIME)
		currentInterruptTime = MAX_INTERRUPT_TIME;
	    if (currentInterruptTime < MIN_INTERRUPT_TIME)
		currentInterruptTime = MIN_INTERRUPT_TIME;
	    System.out.println("CarControl: InterruptTime="+currentInterruptTime);
	    myCarController.setInterruptTime(currentInterruptTime);
	    break;
	}
	case (Command.SET_FORWARD_SPEED): {
	    currentForwardSpeed = (int)id.time;
	    if (currentForwardSpeed > MAX_FORWARD_SPEED)
		currentForwardSpeed = MAX_FORWARD_SPEED;
	    if (currentForwardSpeed < MIN_FORWARD_SPEED)
		currentForwardSpeed = MIN_FORWARD_SPEED;
	    System.out.println("CarControl: ForwardSpeed="+currentForwardSpeed);
	    imagerec.util.Servo.setForwardSpeed(currentForwardSpeed);
	    break;
	}
	case (Command.SET_BACKWARD_SPEED): {
	    currentBackwardSpeed = (int)id.time;
	    if (currentBackwardSpeed > MAX_BACKWARD_SPEED)
		currentBackwardSpeed = MAX_BACKWARD_SPEED;
	    if (currentBackwardSpeed < MIN_BACKWARD_SPEED)
		currentBackwardSpeed = MIN_BACKWARD_SPEED;
	    System.out.println("CarControl: BackwardSpeed="+currentBackwardSpeed);
	    imagerec.util.Servo.setBackwardSpeed(currentBackwardSpeed);
	    break;
	}
	case (Command.SET_TURN_AMOUNT): {
	    currentTurnAmount = (int)id.time;
	    if (currentTurnAmount > MAX_TURN_AMOUNT)
		currentTurnAmount = MAX_TURN_AMOUNT;
	    if (currentTurnAmount < MIN_TURN_AMOUNT)
		currentTurnAmount = MIN_TURN_AMOUNT;
	    System.out.println("CarControl: TurnAmount="+currentTurnAmount);
	    imagerec.util.Servo.setTurnAmount(currentTurnAmount);
	    break;
	}
	default: {
	    System.out.println("CarControl: Unrecognized command");
	}
	}
	super.process(id);
    }
}
