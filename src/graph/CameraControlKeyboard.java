// CameraControlKeyboard.java, created by benster
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
 * {@link Keyboard} is a {@link Node} which takes input from the keyboard
 * and sends out servo commands.
 *
 * @see Servo
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class CameraControlKeyboard extends Node {
    Frame frame = new Frame("Camera Control Keyboard");
  
    private static final int WIDTH = 100;
    private static final int HEIGHT = 100;

    /** Construct a {@link Keyboard} to parse keypresses and generate servo commands. 
     *
     *  @param out Node to send {@link ImageData}s to.
     */
    public CameraControlKeyboard(final Node out) {
	super(out);
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
			System.out.println("CameraControlKeyboard: contrast up");
			out.process(ImageDataManip.create(Command.CONTRAST_UP, 0));
			break;
		    }
		    case KeyEvent.VK_DOWN: {
			System.out.println("CameraControlKeyboard: contrast down");
			out.process(ImageDataManip.create(Command.CONTRAST_DOWN, 0));
			break;
		    }
		    case KeyEvent.VK_LEFT: {
			System.out.println("CameraControlKeyboard: gain down");
			out.process(ImageDataManip.create(Command.CONTRAST_DOWN, 0));
			break;
		    }
		    case KeyEvent.VK_RIGHT: {
			System.out.println("CameraControlKeyboard: gain up");
			out.process(ImageDataManip.create(Command.CONTRAST_DOWN, 0));
			break;
		    }
		    case KeyEvent.VK_KP_UP: {
			System.out.println("CameraControlKeyboard: brightness up");
			out.process(ImageDataManip.create(Command.BRIGHTNESS_UP, 0));
			break;
		    }
		    case KeyEvent.VK_KP_DOWN: {
			System.out.println("CameraControlKeyboard: brightness down");
			out.process(ImageDataManip.create(Command.BRIGHTNESS_DOWN, 0));
			break;
		    }
		    case KeyEvent.VK_KP_RIGHT: {
			System.out.println("CameraControlKeyboard: frame rate up");
			out.process(ImageDataManip.create(Command.FRAME_RATE_UP, 0));
			break;
		    }
		    case KeyEvent.VK_KP_LEFT: {
			System.out.println("CameraControlKeyboard: frame rate down");
			out.process(ImageDataManip.create(Command.FRAME_RATE_DOWN, 0));
			break;
		    }
		    default: {}
		    }
		}
	    });
	frame.setLayout(new GridLayout(12,1));
	frame.add(new java.awt.Label("CAMERA CONTROLS:  "));
	frame.add(new java.awt.Label("Increase Brightness: Numeric Keypad Up"));
	frame.add(new java.awt.Label("Decrease Brightness: Numeric Keypad Down"));
	frame.add(new java.awt.Label(" "));
	frame.add(new java.awt.Label("Increase Contrast  : Arrow Up"));
	frame.add(new java.awt.Label("Decrease Contrast  : Arrow Down"));
	frame.add(new java.awt.Label(" "));
	frame.add(new java.awt.Label("Increase Gain      : Arrow Right"));
	frame.add(new java.awt.Label("Decrease Gain      : Arrow Left"));
	frame.add(new java.awt.Label(" "));
	frame.add(new java.awt.Label("Increase Frame Rate: Numeric Keypad Right"));
	frame.add(new java.awt.Label("Decrease Frame Rate: Numeric Keypad Left"));
	frame.pack();
	//frame.setSize(new Dimension(WIDTH, HEIGHT));
	frame.setVisible(true);
    }

    /** Ignored
     *
     *  @param id Ignored, use <code>run()</code> to start.
     */
    public void process(ImageData id) {
    }

}
