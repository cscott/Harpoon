// LabelControlKeyboard.java, created by benster 6/2/03
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
public class LabelControlKeyboard extends Node {
    Frame frame = new Frame("Label Control");
  
    /** Construct a {@link Keyboard} to parse keypresses and generate servo commands. 
     *
     *  @param out Node to send {@link ImageData}s to.
     */
    public LabelControlKeyboard(final Node out) {
	super(out);
	System.out.println("Initializing LabelControlKeyboard");
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
			System.out.println("LabelControlKeyboard: time between sends up");
			out.process(ImageDataManip.create(Command.TIME_UP, 0));
			break;
		    }
		    case KeyEvent.VK_DOWN: {
			System.out.println("LabelControlKeyboard: time between sends down");
			out.process(ImageDataManip.create(Command.TIME_DOWN, 0));
			break;
		    }
		    default: {}
		    }
		}
	    });
	frame.setLayout(new GridLayout(3,1));
	frame.add(new java.awt.Label("LABEL CONTROLS:  "));
	frame.add(new java.awt.Label("Increase time: Arrow Up"));
	frame.add(new java.awt.Label("Decrease time: Arrow Down"));
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
