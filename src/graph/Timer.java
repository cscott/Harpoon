// Timer.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Timer extends Node {
    private boolean start;
    private boolean announce;
    private long total = 0;
    private long frames = 0;

    public Timer(boolean start, boolean announce, Node out) {
	super(out);
	this.start = start;
	this.announce = announce;
    }

    public synchronized void process(ImageData id) {
	long time = System.currentTimeMillis();
	frames++;
	if (start) {
	    id.time = time;
	} else {
	    total += id.time = time-id.time;
	    if (announce) {
		System.out.println(id.time);
	    }
	}
	super.process(id);
    }

    public long getTotal() {
	return total;
    }

    public long getFrames() {
	return frames;
    }
}
