// Buffer.java, created by wbeebee, harveyj
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>, Harvey Jones <harveyj@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * {@link Buffer} is a {@link Node} that saves incoming data into a buffer.
 * When this buffer is full, incoming data is dropped until slots open in the queue.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 * @author Harvey Jones <<a href="mailto:harveyj@mit.edu">harveyj@mit.edu</a>>
 */
public class Buffer extends Node implements Runnable {
    private ImageData[] buf;
    private int dequeue, enqueue;
    private Thread thread = new Thread(this);
    private boolean debug = false;

    /** Continually send data to the output node for processing. We <code>wait</code> when we are
     *  out of data to process.
     */
    public void run() {
	while (true) {
            ImageData tmp;
	    synchronized (this) {
 		if (dequeue == enqueue) {
	 	    try {
		 	if(debug){
			    System.out.println("Out of frames, waiting");
	 		}
			wait();
		    } catch (InterruptedException e) {
			System.out.println(e.toString());
			System.exit(-1);
		    }
		}
		tmp = buf[dequeue];
		dequeue=(dequeue+1)%buf.length;
	    }
	    // Note: this can hang somewhere else in the pipeline.
	    // Thus, we could possibly block process() if this was
	    // within the synchronized block, so move it outside.
	    super.process(tmp); 
	}
    }

    /** Construct a {@link Buffer} which will queue images, dropping them after its
     *  buffer is full.
     *
     *  @param size  The size of the queue that will store the buffered images.
     *  @param out   The node to send output to.
     *  @param debug Whether to print what is happening to the queue at 
     *  interesting times.
     */
    public Buffer(int size, Node out, boolean debug) {
	super(out);
	buf = new ImageData[size];
	this.debug=debug;
	thread.start();
    }

    /** Construct a {@link Buffer} which will queue images, dropping them after
     *  its buffer is full.
     *
     *  @param size  The size of the queue that will store the buffered images.
     *  @param out   The node to send output to.
     */
    public Buffer(int size, Node out) {
	this(size, out, false)
	buf = new ImageData[size];
	this.debug = false;
	thread.start();
    }

    /** Enqueue the incoming image. Wake up <code> run </code> if it is
     *  sleeping due to lack of data.
     *
     *  @param id   The image data to process.
     */
    public synchronized void process(ImageData id) {
	if (enqueue == dequeue) {
	    if (debug){
		System.out.println("Notify fired.");
	    }
	    notify();
	}
	if ((enqueue+1)%buf.length == dequeue) {
	    if(debug){
		System.out.println("Dropping frame" + id.id);
	    }
	    return; // Drop frames... too many to process.
	}
	buf[enqueue] = id;
	enqueue=(enqueue+1)%buf.length;
    }
}
