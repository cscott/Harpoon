// ************************************************************************
//    $Id: EventHandlerLogic.java,v 1.1 2002-07-02 15:53:04 wbeebee Exp $
// ************************************************************************
//
//                               RTJPerf
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
//
// *************************************************************************
//  
// *************************************************************************
package edu.uci.ece.doc.rtjperf.asynch;

// -- jTools Import --
import edu.uci.ece.ac.jargo.*;
import edu.uci.ece.ac.time.*;
import edu.uci.ece.ac.concurrent.EventVariable;

/**
 * This class encapsulates the event handling logic.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class EventHandlerLogic implements Runnable {

    private PerformanceReport performanceReport;
    private HighResTimer timer;
    private EventVariable eventVar;
    private boolean memProfiling;
    private HighResTime time = new HighResTime();
    
    static final String DISPATCH_DELAY = "DispatchDelay";
    
    public EventHandlerLogic(boolean memProfiling) {
        this.memProfiling = memProfiling;
    }
    
    public EventHandlerLogic(PerformanceReport performanceReport,
                             HighResTimer timer,
                             EventVariable eventVar,
                             boolean memProfiling) {
        this.performanceReport = performanceReport;
        this.timer = timer;
        this.eventVar = eventVar;
        this.memProfiling = memProfiling;
    }

    public final void init(PerformanceReport performanceReport, HighResTimer timer, EventVariable eventVar) {
        this.performanceReport = performanceReport;
        this.timer = timer;
        this.eventVar = eventVar;
    }

    public final void run() {
        timer.stop();
        if (!this.memProfiling) 
            performanceReport.addMeasuredVariable(DISPATCH_DELAY, timer.getElapsedTime());
        else {
            timer.getElapsedTime(time);
            time.printTo(System.out);
        }
        timer.reset();
        eventVar.signal();
    }
}

