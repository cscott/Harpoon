// ************************************************************************
//    $Id: HighResClock.java,v 1.1 2002-07-02 15:35:35 wbeebee Exp $
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
 * This class provide an high resolution clock. It is implemented
 * using through JNI the ACE_High_Res_Timer
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class HighResClock {
    
    static {
        System.loadLibrary("HRTime");
    }

    public static HighResTime getTime() {
        
        HighResTime time = new HighResTime();
        HighResClock.getTime(time);
        return time;
    }

    public static native void getTime(HighResTime time);

    public static native long getClockTickCount();

    public static HighResTime clockTick2HighResTime(long clockTicks) {
        HighResTime time = new HighResTime();
        clockTick2HighResTime(clockTicks, time);
        return time;
    }

    public static native void clockTick2HighResTime(long clockTicks, HighResTime time);
    
    /**
     * Returns the frequency of the clock in MHz
     *
     * @return a <code>float</code> value representing the frequency of the clock.
     */
    public static native float getClockFrequency();
    
    /**
     * Returns the period of the clock in nano seconds.
     *
     * @return a <code>double</code> value representing the period of the clock.
     */
    public static native double getClockPeriod();
    
}
