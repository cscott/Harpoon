// CarControlKeyboard.java, created by benster
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
 */
public class CarControlKeyboard extends Node {
    Frame frame = new Frame("Car Control");
  
    /** Construct a {@link Keyboard} to parse keypresses and generate servo commands. 
     *
     *  @param out Node to send {@link ImageData}s to.
     */
    public CarControlKeyboard(final Node out) {
	super(out);
	System.out.println("Initializing car control keyboard");
	frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    frame.setVisible(false);
		}
	    });

	frame.addKeyListener(new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
		    //System.out.println("Keyboard: key pressed");
		    switch (e.getKeyCode()) {
		    case KeyEvent.VK_UP: {
			System.out.println("CarControlKeyboard: Interrupt time up");
			out.process(ImageDataManip.create(Command.INTERRUPT_TIME_UP, 0));
			break;
		    }
		    case KeyEvent.VK_DOWN: {
			System.out.println("CarControlKeyboard: Interrupt time down");
			out.process(ImageDataManip.create(Command.INTERRUPT_TIME_DOWN, 0));
			break;
		    }
		    case KeyEvent.VK_RIGHT: {
			System.out.println("CarControlKeyboard: Turn amount up");
			out.process(ImageDataManip.create(Command.TURN_AMOUNT_UP, 0));
			break;
		    }
		    case KeyEvent.VK_LEFT: {
			System.out.println("CarControlKeyboard: Turn amount down");
			out.process(ImageDataManip.create(Command.TURN_AMOUNT_DOWN, 0));
			break;
		    }
		    case KeyEvent.VK_KP_UP: {
			System.out.println("CarControlKeyboard: Backward speed up");
			out.process(ImageDataManip.create(Command.BACKWARD_SPEED_UP, 0));
			break;
		    }
		    case KeyEvent.VK_KP_DOWN: {
			System.out.println("CarControlKeyboard: Backward speed down");
			out.process(ImageDataManip.create(Command.BACKWARD_SPEED_DOWN, 0));
			break;
		    }
		    case KeyEvent.VK_KP_RIGHT: {
			System.out.println("CarControlKeyboard: Forwards speed up");
			out.process(ImageDataManip.create(Command.FORWARD_SPEED_UP, 0));
			break;
		    }
		    case KeyEvent.VK_KP_LEFT: {
			System.out.println("CarControlKeyboard: Forwards speed down");
			out.process(ImageDataManip.create(Command.FORWARD_SPEED_DOWN, 0));
			break;
		    }
		    default: {}
		    }
		}
	    });
	frame.setLayout(new GridLayout(12,1));
	frame.add(new java.awt.Label("CAR CONTROLS:  "));
	frame.add(new java.awt.Label("Increase interrupt time: Arrow Up"));
	frame.add(new java.awt.Label("Decrease interrupt time: Arrow Down"));
	frame.add(new java.awt.Label(""));
	frame.add(new java.awt.Label("Increase turn amount: Arrow Right"));
	frame.add(new java.awt.Label("Decrease turn amount: Arrow Left"));
	frame.add(new java.awt.Label(""));
	frame.add(new java.awt.Label("Increase backward speed: Numeric Keypad Up"));
	frame.add(new java.awt.Label("Decrease backward speed: Numeric Keypad Down"));
	frame.add(new java.awt.Label(""));
	frame.add(new java.awt.Label("Increase forward speed : Numeric Keypad Right"));
	frame.add(new java.awt.Label("Decrease forward speed : Numeric Keypad Left"));
	frame.pack();
	frame.setVisible(true);
    }

    /** Ignored
     *
     *  @param id Ignored, use <code>run()</code> to start.
     */
    public void process(ImageData id) {
    }

}
