// HumanRecognition.java, created by benster 5/27/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package imagerec.graph;

import imagerec.util.CommonMemory;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import java.awt.event.WindowListener;
import java.awt.event.MouseListener;
import java.awt.event.KeyListener;

/**
 * {@link HumanRecognition} is currently the most effective
 * ATR. It is intended to work asynchronously with {@link Label},
 * receiving cropped {@link ImageData}s and sending out stripped
 * {@link ImageData}s tagged with either <code>Command.IS_TANK</code> or
 * <code>Command.IS_NOT_TANK</code>.<br><br>.
 *
 * The class springs up windows for each unique object being tracked,
 * and a human selects which object is a tank (or an object of interest)
 * by clicking the mouse within
 * the window or selecting the window and hitting [Enter] on his keypad.<br>
 * <br>
 * The current implementation allows only one object to be selected
 * at a time, however, this should be straightforward to change.
 * 
 */
public class HumanRecognition extends Node implements VariableLatency {

    /**
     * The head of the linked list containing all the
     * {@link Display}/targetID pairs maintained by this object.
     */
    private Pair head;

    /**
     * The {@link Display} containing the object that the user
     * has currently selected as the object of interest.
     */
    private MyDisplay currentSelected = null;

    /**
     * Creates a new {@link HumanRecognition} object.
     */
    public HumanRecognition() {
	this.head = null;
	addTarget(-2);
    }

    /**
     * The current latency of the algorithm. This value
     * is user set through the {@link VariableLatency}
     * interface.
     */
    private int latency;

    /**
     * First, reads an {@link ImageData}'s
     * <code>trackedObjectUniqueID</code> field.
     * If a {@link Display} already exists for that
     * ID, then that {@link Display} is updated. If not, then
     * a new one is created and is linked to that ID.
     *
     * The latency of this method is variable and can be
     * controlled by the user.
     */
    public void process(ImageData id) {
	
	try {
	    Thread.currentThread().sleep(latency-250);
	}
	catch (InterruptedException e) {
	}
	int targetID = id.trackedObjectUniqueID;
	//System.out.println("ImageData #"+id.id);
	//System.out.println("   target #"+targetID);
	Pair currentPair = head;
	boolean foundIt = false;
	while (currentPair != null) {
	    if (currentPair.targetID == targetID) {
		foundIt = true;
		break;
	    }
	    currentPair = currentPair.next;
	}
       
	if (foundIt) {
	    //currentPair.count++;
	    MyDisplay d = currentPair.d;
	    d.process(id);
	    
	    if (d == currentSelected) {
		id.command = Command.IS_TANK;
		super.process(id);
	    }
	    else {
		id.command = Command.IS_NOT_TANK;
		super.process(id);
	    }
	}
	//[if (foundIt)]
	else {
	    Pair newPair = addTarget(id);
	    newPair.d.process(id);
	    id.command = Command.IS_NOT_TANK;
	    super.process(id);
	}
    }


    /**
     * Creates a new {@link HumanRecognition.Pair} linked to
     * the <code>trackedObjectUniqueID</code>
     * of the specified {@link ImageData}.
     *
     * @param id The {@link ImageData} whose
     * <code>trackedObjectUniqueID</code> will be linked
     * to the new {@link HumanRecognition.Pair}.
     */
    private Pair addTarget(ImageData id) {
	return this.addTarget(id.trackedObjectUniqueID);
    }

    /**
     * Creates a new {@link HumanRecognition.Pair} using
     * the specified <code>trackedObjectUniqueID</code>.
     *
     * @param trackedObjectUniqueID The <code>trackedObjectUniqueID</code>
     * that will be linked to the new {@link HumanRecognition.Pair}. 
     */
    private Pair addTarget(int trackedObjectUniqueID) {
	Pair newPair = new Pair(trackedObjectUniqueID, head);
	head = newPair;
	return newPair;	
    }

    /**
     * Removes the specified {@link HumanRecognition.MyDisplay} from the linked list
     * of {@link HumanRecognition.Pair}s.
     *
     * @param d The {@link HumanRecognition.MyDisplay} to be removed.
     */
    void removeMe(MyDisplay d) {
	Pair currentPair = head;
	Pair last = null;
	while (currentPair != null) {
	    if (currentPair.d == d) {
		if (last == null) {
		    head = currentPair.next;
		}
		else {
		    last.next = currentPair.next;
		}
		break;
	    }
	    last = currentPair;
	    currentPair = currentPair.next;
	}
    }

    /**
     * A linked-list element that associates a {@link HumanRecognition.MyDisplay}
     * with a target ID.
     */
    private class Pair {
	MyDisplay d;
	int targetID;
	Pair next;
	
	/**
	 * Constructs a new {@link HumanRecognition.Pair}, which will
	 * display a new window linked to the specified
	 * target ID, and specify the next element in the
	 * linked list.
	 *
	 * @param targetID The unique ID that will be associated
	 * with the new {@link HumanRecognition.MyDisplay}.
	 * @param next The {@link HumanRecognition.Pair} that the newly
	 * created {@link HumanRecognition.Pair} will point to in the
	 * linked-list structure.
	 */
	Pair(int targetID, Pair next) {
	    init(targetID, next);
	}
	
	/**
	 * This method should be called by all constructors
	 * to properly initialize all object fields.
	 *
	 * @param targetID The unique ID that will be associated
	 * with the new {@link HumanRecognition.MyDisplay}.
	 * @param next The {@link HumanRecognition.Pair} that the newly
	 * created {@link HumanRecognition.Pair} will point to in the
	 * linked-list structure.	 */
	private void init(int targetID, Pair next) {
	    this.targetID = targetID;
	    if (targetID == -2) {
		d = new MyDisplay("NO TARGET");
	    }
	    else {
		d = new MyDisplay("Target #"+targetID);
	    }
	    this.next = next;
	}
    }

    /**
     * Class that adds AWT listener functionality to 
     * the existing {@link Display} class. It accepts
     * keyboard and mouse input to choose
     * which object displayed by the {@link HumanRecognition}
     * class is a tank or an object of interest.
     */
    public class MyDisplay extends Display {
	/**
	 * Constructs a new {@link HumanRecognition.MyDisplay} which
	 * will listen for AWT mouse and keyboard events.
	 */
	MyDisplay(String name) {
	    super(name);
	    super.getCanvas().addKeyListener(new KeyAdapter() {
		    public void keyPressed(KeyEvent e) {
			int code = e.getKeyCode();
			if (code == KeyEvent.VK_ENTER) {
			    HumanRecognition.this.currentSelected = MyDisplay.this;
			}
		    }
		});
	    super.getCanvas().addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent e){
			HumanRecognition.this.currentSelected = MyDisplay.this;
		    }
		});
	    frame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			System.out.println(MyDisplay.this.getFrame().getTitle()+" closing");
			frame.setVisible(false);
			frame.dispose();
			HumanRecognition.this.removeMe(MyDisplay.this);
		    }
		});
	}
	
	/**
	 * Returns the AWT Frame contained by this
	 * {@link Display} node.
	 */
	Frame getFrame() {
	    return super.frame;
	}
    }

    /**
     * Sets the approximate
     * latency of the {@link HumanRecognition} "algorithm".
     * 
     * @param latency The time that this "algorithm"
     * should take to execute.
     */
    public void setLatency(int latency) {
	this.latency = latency;
    }

}
