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

public class HumanRecognition extends Node {

    private Pair head;
    private int size;

    private static final int confirmEvery = 10;

    MyDisplay currentSelected = null;

    public HumanRecognition() {
	this.head = null;
	this.size = 0;
    }


    /**
       The name of the variable in {@link CommonMemory} that this
       {@link HumanRecognition} node will set with true or false
       before it returns. If <code>memName</code> is null,
       then this {@link HumanRecognition} node will not set any
       value in {@link CommonMemory}.<br><br>
       If this variable is equal to <code>true</code>, this means that
       the <code>process()</code> method found blue in the
       image. If the variable is set to <code>false</code>, then
       no blue was found.<br><br>
       Remember that the boolean value of the variable in {@link CommonMemory}
       is actually stored in a {@link Boolean} object wrapper.
       @see CommonMemory
     */
    private String memName;

    /**
     * This method either tells the {@link HumanRecognition} node to
     * begin or stop
     * setting a "return value" in {@link CommonMemory}.
     * Specifying a name will ause the {@link HumanRecognition} node to
     * store either
     * <code>true</code> or <code>false</code> in the variable who's
     * name you specify
     * depending on whether the <code>process()</code> method detects
     * blue in an image.<br><br>
     *
     * By specifying a <code>null</code> name, you turn this feature off.
     * @param name The name of the variable in {@link CommonMemory}
     * where this {@link HumanRecognition}
     * node will store its "return value."
     *
     * @see CommonMemory
     */
    public void setCommonMemory(String name) {
	this.memName = name;
    }



    public void process(ImageData id) {
	int targetID = id.trackedObjectUniqueID;
	//System.out.println("ImageData #"+id.id);
	//System.out.println("   target #"+targetID);
	//if (currentSelected == null)
	//    System.out.println("   No current selected");
	//else
	//    System.out.println(currentSelected.getFrame().getTitle()+" is selected");
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
	    currentPair.count++;
	    MyDisplay d = currentPair.d;
	    if (currentPair.count == confirmEvery) {
		currentPair.count = 0;
		Frame f = d.getFrame();
		
		if (d == currentSelected) {
		    //System.out.println(f.getTitle()+" is selected.");
		    currentPair.d.process(id);
		    super.process(id);
		    if (this.memName != null) {
			CommonMemory.setValue(this.memName, new Boolean(true));
		    }
		}
		else {
		    currentPair.d.process(id);
		    if (this.memName != null) {
			CommonMemory.setValue(this.memName, new Boolean(false));
		    }
		}
	    }
	    else {
		if (this.memName != null) {
		    if (d == currentSelected) {
			super.process(id);
			CommonMemory.setValue(this.memName, new Boolean(true));
		    }
		    else {
			CommonMemory.setValue(this.memName, new Boolean(false));			
		    }
		}
	    }
	}
	else { //if (!foundIt)
	    Pair newPair = addTarget(id);
	    newPair.d.process(id);
	    if (this.memName != null) {
		CommonMemory.setValue(this.memName, new Boolean(false));
	    }
	}
    }



    private Pair addTarget(ImageData id) {
	Pair newPair = new Pair(id.trackedObjectUniqueID, head);
	head = newPair;
	size++;
	return newPair;
    }

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

    private class Pair {
	MyDisplay d;
	int targetID;
	Pair next;
	int count;
	Pair(int targetID){
	    init(targetID, null);
	}
	Pair(int targetID, Pair next) {
	    init(targetID, next);
	}
	private void init(int targetID, Pair next) {
	    this.targetID = targetID;
	    d = new MyDisplay("Target #"+targetID);
	    this.next = next;
	    count = 0;
	}
    }

    public class MyDisplay extends Display {
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

}
