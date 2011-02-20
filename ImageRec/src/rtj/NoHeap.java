// NoHeap.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.rtj;

import imagerec.*;
import javax.realtime.MemoryArea;

/* Needs wait free queues... */

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

/**
 *
 *
 */

public class NoHeap extends Node {
    private Thread t;

    public NoHeap(boolean noheap, MemoryArea ma, Node out) {
	super(out);
	if (noheap) {
	    t = new NoHeapRealtimeThread(ma) {
		public void run() {
		    out.process(id);
		}
	    }
	} else {
	    t = new RealtimeThread(ma) {
		public void run() {
		    out.process(id);
		}
	    }
	}
	t.start();
	t.join();
    }

    public synchronized void process(final ImageData id) {
	Thread t;
    }
}
