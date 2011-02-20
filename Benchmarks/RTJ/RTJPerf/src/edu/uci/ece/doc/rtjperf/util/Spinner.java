/*-------------------------------------------------------------------------*
 * $Id: Spinner.java,v 1.1 2002-07-02 15:55:07 wbeebee Exp $
 *-------------------------------------------------------------------------*/

package edu.uci.ece.doc.rtjperf.util;

/**
 * This class represent a "spinner" in the sense that it spins
 * executing CPU cycle until, not asked to stop its spinning. This
 * class is meant to simulate background computation.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class Spinner implements Runnable {

    private boolean spin = true;
    
    public void run() {
        while (this.spin) { }
    }

    
    public synchronized void stop() {
        this.spin = false;
    }
}
