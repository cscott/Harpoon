// ************************************************************************
//    $Id: PeriodicThreadTest.java,v 1.1 2002-07-02 15:54:15 wbeebee Exp $
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
package edu.uci.ece.doc.rtjperf.thread;

import edu.uci.ece.ac.time.HighResTimer;
import edu.uci.ece.ac.time.HighResTime;
import edu.uci.ece.ac.time.PerformanceReport;
import javax.realtime.*;

public class PeriodicThreadTest {

    static final String THREAD_PERIOD = "ThreadPeriod";
    public static void main(String[] args) {
        
        final int count = Integer.parseInt(args[0]);
        int millis = Integer.parseInt(args[1]);
        int nanos = Integer.parseInt(args[2]);
        final String path = args[3];
        final PerformanceReport report = new PerformanceReport(THREAD_PERIOD + "Test" + millis + "." + nanos);
        final HighResTimer timer = new HighResTimer();

        ////////////////////////////////////////////////////////////
        // Runnable Logic Anonymous Class
        Runnable periodicThreadLogic = new Runnable() {
                public void run() {
                    RealtimeThread rtThread = null;
                    int roundCount = 0;
                    
                    try {
                        rtThread = RealtimeThread.currentRealtimeThread();

                        for (int i = 0; i < count; ++i) {
                            timer.start();
                            rtThread.waitForNextPeriod();
                            timer.stop();
                            report.addMeasuredVariable(THREAD_PERIOD, timer.getElapsedTime());
                        }
                        report.generateDataFile(path + "/");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                    
            };
        //
        ////////////////////////////////////////////////////////////

        RelativeTime period = new RelativeTime(millis, nanos); // T msec Period
        System.out.println("Running Test with Period: " + millis + "ms " + nanos + " ns");
        PeriodicParameters periodicParams =
            new PeriodicParameters(new RelativeTime(0, 0), // Start Time
                                  period,
                                  null,   // cost estimate
                                  period, // deadline == period
                                  null,   // overrun handler
                                  null);  // miss handler

        PriorityParameters prioParams =
            new PriorityParameters(PriorityScheduler.MAX_PRIORITY);

        RealtimeThread rtThread =
            new RealtimeThread(prioParams, // Sched Params
                               periodicParams, //Release Params
                               null, // mem params
                               null, // mem area
                               null, // processing group
                               periodicThreadLogic); // logic

        rtThread.start();
    }
    
}
