// Keyboard.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

import java.awt.Dimension;
import java.awt.Frame;

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
public class Keyboard extends Node {
    Frame frame = new Frame("Keyboard");
  
    private static final int LEFT = 37;
    private static final int RIGHT = 39;
    private static final int FORWARD = 38;
    private static final int BACKWARD = 40;

    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    /** Construct a {@link Keyboard} to parse keypresses and generate servo commands. 
     *
     *  @param out Node to send {@link ImageData}s to.
     */
    public Keyboard(final Node out) {
	super(out);
	frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    frame.setVisible(false);
		}
	    });
	frame.addKeyListener(new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
		    switch (e.getKeyCode()) {
		    case LEFT: {
			out.process(ImageDataManip.create(Command.SERVO_LEFT_CONTINUE, 0));
			break;
		    }
		    case RIGHT: {
			out.process(ImageDataManip.create(Command.SERVO_RIGHT_CONTINUE, 0));
			break;
		    }
		    case FORWARD: {
			out.process(ImageDataManip.create(Command.SERVO_FORWARD_CONTINUE, 0));
			break;
		    }
		    case BACKWARD: {
			out.process(ImageDataManip.create(Command.SERVO_BACKWARD_CONTINUE, 0));
			break;
		    }
		    default: {}
		    }
		}
		public void keyReleased(KeyEvent e) {
		    switch (e.getKeyCode()) {
		    case LEFT: {
			out.process(ImageDataManip.create(Command.SERVO_STOP_TURN, 0));
			break;
		    }
		    case RIGHT: {
			out.process(ImageDataManip.create(Command.SERVO_STOP_TURN, 0));
			break;
		    }
		    case FORWARD: {
			out.process(ImageDataManip.create(Command.SERVO_STOP_MOVING, 0));
			break;
		    }
		    case BACKWARD: {
			out.process(ImageDataManip.create(Command.SERVO_STOP_MOVING, 0));
			break;
		    }
		    }
		}
	    });
	frame.setSize(new Dimension(WIDTH, HEIGHT));
	frame.setVisible(true);
    }

    /** Ignored
     *
     *  @param id Ignored, use <code>run()</code> to start.
     */
    public void process(ImageData id) {
    }

}
