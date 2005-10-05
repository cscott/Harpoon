// Timer.java, created Tue Jan 23 16:09:55 2001 by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

/**
 * <code>Timer</code> keeps track of how much time accumulates between
 * successive calls to start and stop.  This is useful when profiling the
 * compiler.
 *
 * <code>Timer t = new Timer();
 * 
 * for (int i=0; i<10; i++) {
 *   bar();
 *   t.start();
 *   foo();
 *   t.stop();
 * }
 * 
 * System.out.println(t.timeElapsed());</code>
 *
 * This prints the total time spent in foo in milliseconds.
 */

public class Timer {
    private long timeElapsed, timerStart;

    /** Create a new Timer and initialize timeElapsed to 0. */
    public Timer() {
	timeElapsed = 0;
	this.start();
    }

    /** Ask if the timer is currently running. */
    public boolean running() {
	return timerStart != -1;
    }

    /** Start the timer running. */
    public void start() {
	timerStart = System.currentTimeMillis();
    }

    /** Stop the timer. */
    public void stop() {
	timeElapsed += System.currentTimeMillis() - timerStart;
	assert timerStart != -1 : "Two Timer.stop() without Timer.start()";
	timerStart = -1;
    }

    public void freshStart() {
	timeElapsed = 0;
	this.start();
    }

    /** Return the cumulative amount of time elapsed between starts and stops
     *  in milliseconds. 
     */

    public long timeElapsed() {
	if(running()) {
	    return System.currentTimeMillis() - timerStart;
	}
	return timeElapsed;
    }

    /** Return a string representing the current state of the timer. */
    public String toString() {
	if(timerStart != -1) stop();
	StringBuffer sb = new StringBuffer();
	if (timerStart != -1) {
	    sb.append(((System.currentTimeMillis() - 
			timerStart + timeElapsed)));
	    sb.append(" ms");
	} else {
	    sb.append(timeElapsed); 
	    sb.append(" ms");
	}
	return sb.toString();
    }
}
