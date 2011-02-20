// CarContollerControl.java, created by benster 6/2/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

/**
 * This class is intended to be in a pipeline that runs asynchronously
 * with a pipeline containing a {@link CarController} node, which
 * uses the imagerec.util.Servo class.<br><br>
 *
 * It allows for user control of car properties when coupled 
 * with an input node like {@link CarControlKeyboard}.<br><br>
 *
 * The following properties of {@link CarController} may be modified:<br>
 *     Time Until Command Interrupt: How long a command may run
 * before being interrupted. If the latency between commands
 * is less than this amount, then commands will never be
 * interrupted.<br>
 * The following static properties of (@link imagerec.util.Servo} may be modified:<br>
 *     Forward Speed: The power given to a motor when the car
 * is moving forward.<br>
 *     Backward Speed: The power given to a motor when hen the car
   is moving backward. This amount is typicaly much more than
 * when moving forward.<br>
 *     Turn Amount: How much the wheels are displaced from center when
 * turning either left or right.<br>
 *<br>
 *
 * To set a particular value:<br>
 *   Tag an {@link ImageData} with one of the following {@link Command}
 * tags and, if necessary, set the appropriate value to the {@link ImageData}'s
 * <code>time</code> field.<br><br>
 *
 * Command.SET_FORWARD_SPEED: Causes the {@link CarController}'s "Forward Speed" property
 * to be set to the value given in the {@link ImageData}'s <code>time</code>
 * field.<br>
 * Command.FORWARD_SPEED_UP: Causes the {@link CarController}'s "Forward Speed" property
 * to be incremented by DELTA_FORWARD_SPEED.<br>
 * Command.FORWARD_SPEED_DOWN: Causes the {@link CarController}'s "Forward Speed" property
 * to be decremented by DELTA_FORWARD_SPEED.<br>
 * Command.SET_BACKWARD_SPEED: Causes the {@link CarController}'s "Backward Speed" property
 * to be set to the value given in the {@link ImageData}'s <code>time</code>
 * field.<br>
 * Command.BACKWARD_SPEED_UP: Causes the {@link CarController}'s "Backward Speed" property
 * to be incremented by DELTA_BACKWARD_SPEED.<br>
 * Command.BACKWARD_SPEED_DOWN: Causes the {@link CarController}'s "Backward Speed" property
 * to be decremented by DELTA_BACKWARD_SPEED.<br>
 * Command.SET_TURN_AMOUNT: Causes the {@link CarController}'s "Turn Amount" property
 * to be set to the value given in the {@link ImageData}'s <code>time</code>
 * field.<br>
 * Command.TURN_AMOUNT_UP: Causes the {@link CarController}'s "Turn Amount" property
 * to be incremented by DELTA_TURN_AMOUNT.<br>
 * Command.TURN_AMOUNT_DOWN: Causes the {@link CarController}'s "Turn Amount" property
 * to be decremented by DELTA_TURN_AMOUNT.<br>
 * Command.SET_TURN_AMOUNT: Causes the {@link CarController}'s
 * "Time Until Command Interrupt" property
 * to be set to the value given in the {@link ImageData}'s <code>time</code>
 * field.<br>
 * Command.INTERRUPT_TIME_UP: Causes the {@link CarController}'s
 * "Time Until Command Interrupt" property
 * to be incremented by DELTA_INTERRUPT_TIME.<br>
 * Command.INTERRUPT_TIME_DOWN: Causes the {@link CarController}'s
 * "Time Until Command Interrupt" property
 * to be decremented by DELTA_INTERRUPT_TIME.<br>
 * <br>
 *
 * If the value specifed in the {@link ImageData}'s <code>time</code>
 * field is not within the appropriate range specified by this class's
 * constants, then the highest or lowest possible value
 * is substituted.
 
 *
 * This class assumes that the {@link CarController}'s and
 * {@link imagerec.util.Servo}'s properties are not
 * set by any other class.
 *
 * @see CarController
 * @see imagerec.util.Servo
 * @see CarControlKeyboard
 * @see Command
 *
 * @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
 */
public class CarControllerControl extends Node {

    /**
     * The {@link CarController} that will be mutated by
     * this {@link CarControllerControl} node.
     */
    private CarController myCarController;

    public static final int MAX_INTERRUPT_TIME = 5000;
    public static final int MIN_INTERRUPT_TIME = 0;
    public static final int DEFAULT_INTERRUPT_TIME = 500;
    public static final int DELTA_INTERRUPT_TIME = 100;
    private int currentInterruptTime;

    public static final int MAX_BACKWARD_SPEED = 127;
    public static final int MIN_BACKWARD_SPEED = 0;
    public static final int DEFAULT_BACKWARD_SPEED = 15;
    public static final int DELTA_BACKWARD_SPEED = 1;
    private int currentBackwardSpeed;

    public static final int MAX_FORWARD_SPEED = 127;
    public static final int MIN_FORWARD_SPEED = 0;
    public static final int DEFAULT_FORWARD_SPEED = 14;
    public static final int DELTA_FORWARD_SPEED = 1;
    private int currentForwardSpeed;

    public static final int MAX_TURN_AMOUNT = 127;
    public static final int MIN_TURN_AMOUNT = 0;
    public static final int DEFAULT_TURN_AMOUNT = 100;
    public static final int DELTA_TURN_AMOUNT = 5;;
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
