// Buffer.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * {@link Buffer} is a {@link Node}
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Buffer extends Node implements Runnable {
    private ImageData[] buf;
    private int dequeue, enqueue;
    private Thread thread = new Thread(this);

    public void run() {
	while (true) {
	    ImageData tmp;
	    synchronized (this) {
		if (dequeue == enqueue) {
		    try {
			System.out.println("Out of frames, waiting");
			wait();
		    } catch (InterruptedException e) {
			System.out.println(e.toString());
			System.exit(-1);
		    }
		}
		tmp = buf[dequeue];
		dequeue=(dequeue+1)%buf.length;
	    }
	    super.process(tmp);
	}
    }

    public Buffer(int size, Node out) {
	super(out);
	buf = new ImageData[size];
	thread.start();
    }

    public synchronized void process(ImageData id) {
	if (enqueue == dequeue) {
	    System.out.println("Notify fired.");
	    notify();
	}
	if ((enqueue+1)%buf.length == dequeue) {
	    System.out.println("Dropping frame" + id.id);
	    return; // Drop frames... too many to process.
	}
	buf[enqueue] = id;
	enqueue=(enqueue+1)%buf.length;
    }
}
