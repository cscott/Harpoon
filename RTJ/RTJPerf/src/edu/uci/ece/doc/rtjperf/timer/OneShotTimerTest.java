// ************************************************************************
//    $Id: OneShotTimerTest.java,v 1.1 2002-07-02 15:54:55 wbeebee Exp $
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
// *************************************************************************/
package edu.uci.ece.doc.rtjperf.timer;

import edu.uci.ece.ac.time.HighResTimer;
import edu.uci.ece.ac.time.HighResTime;
import edu.uci.ece.ac.time.PerformanceReport;
import edu.uci.ece.ac.concurrent.EventVariable;
import javax.realtime.*;


public class OneShotTimerTest {

    static final String TIMEOUT_TIME = "TimeOutTime";

    public static void main(String[] args) {
        final int count = Integer.parseInt(args[0]);
        int millis = Integer.parseInt(args[1]); // time in msec
        int nanos = Integer.parseInt(args[2]);

        final PerformanceReport report =
            new PerformanceReport("OneShotTimerTest" + millis + "." + nanos);
        final HighResTimer timer = new HighResTimer();

        final String dataPath = args[3];
        final EventVariable event = new EventVariable();

        Runnable logic = new Runnable() {
                public void run() {
                    timer.stop();
                    report.addMeasuredVariable(TIMEOUT_TIME, timer.getElapsedTime());
                    timer.reset();
                    event.signal();
                }
            };

        PriorityParameters prioParams =
            new PriorityParameters(PriorityScheduler.MAX_PRIORITY);
        TimeoutHandler handler = new TimeoutHandler(prioParams, null, null, null, null, false,
                                                    logic);

        OneShotTimer ostimer =
            new OneShotTimer(new RelativeTime(millis, nanos),
                             handler);

        ostimer.enable();
        for (int i = 0; i < count; i++) {
            ostimer.start();
            timer.start();
            if (i == count - 1)
                break;
            try {
                event.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            report.generateDataFile(dataPath + "/");
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
