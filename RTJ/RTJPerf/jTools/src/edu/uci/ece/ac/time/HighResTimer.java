// ************************************************************************
//    $Id: HighResTimer.java,v 1.1 2002-07-02 15:35:35 wbeebee Exp $
// ************************************************************************
//
//                               jTools
//
//               Copyright (C) 2001-2002 by Angelo Corsaro.
//                         <corsaro@ece.uci.edu>
//                          All Rights Reserved.
//
//   Permission to use, copy, modify, and distribute this software and
//   its  documentation for any purpose is hereby  granted without fee,
//   provided that the above copyright notice appear in all copies and
//   that both that copyright notice and this permission notice appear
//   in  supporting  documentation. I don't make  any  representations
//   about the  suitability  of this  software for any  purpose. It is
//   provided "as is" without express or implied warranty.
//
//
// *************************************************************************
//  
// *************************************************************************
package edu.uci.ece.ac.time;

/**
 * This class provide a way of performing high resolution of the time
 * spent performing something.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class HighResTimer {

    
    long startClockTick;
    long stopClockTick;
    
    /**
     * Starts measuring the elapsed time.
     *
     */
    public void start() {
        this.startClockTick = HighResClock.getClockTickCount();
    }
    
    /**
     * Stops the time measurement.
     *
     */
    public void stop() {
        this.stopClockTick = HighResClock.getClockTickCount();
    }


    /**
     * Resets the timer.
     *
     */
    public void reset() {
        this.startClockTick = this.stopClockTick = 0;
    }

    public HighResTime getElapsedTime() {
        return HighResClock.clockTick2HighResTime(this.stopClockTick - this.startClockTick);
    }

    public void getElapsedTime(HighResTime time) {
        HighResClock.clockTick2HighResTime(this.stopClockTick - this.startClockTick, time);
    }

}
