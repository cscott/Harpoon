// ************************************************************************
//    $Id: CreationLatencyTest.java,v 1.1 2002-07-02 15:54:24 wbeebee Exp $
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
package edu.uci.ece.doc.rtjperf.thread.plain;

import edu.uci.ece.ac.time.HighResTimer;
import edu.uci.ece.ac.time.HighResTime;
import edu.uci.ece.ac.time.PerformanceReport;

public class CreationLatencyTest  {
    final static String CREATION_LATENCY = "CreationLatency";
    final static String STARTUP_LATENCY = "StartupLatency";
    public static void main(String[] args) throws Exception {

        int count = Integer.parseInt(args[0]);
        String path = args[1];
        
        final HighResTimer timer = new HighResTimer();
        final PerformanceReport report = new PerformanceReport(CREATION_LATENCY);
        
        final Runnable logic = new Runnable() {
                public void run() {
                    timer.stop();
                    report.addMeasuredVariable(STARTUP_LATENCY, timer.getElapsedTime());
                    timer.reset();
                }
            };
        Thread rtThread = null;
        int priority = Thread.MAX_PRIORITY;
        for (int i = 0; i < count; i++) {
            timer.start();
            rtThread = new Thread(logic);
            rtThread.setPriority(priority);
            timer.stop();
            report.addMeasuredVariable(CREATION_LATENCY, timer.getElapsedTime());
            timer.reset();
            timer.start();
            rtThread.start();
            rtThread.join();
        }

        report.generateDataFile(args[1] + "/" + CREATION_LATENCY);
    }
    
}
