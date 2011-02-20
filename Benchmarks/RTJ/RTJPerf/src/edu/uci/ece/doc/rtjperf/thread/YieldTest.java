// ************************************************************************
//    $Id: YieldTest.java,v 1.1 2002-07-02 15:54:15 wbeebee Exp $
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
import edu.uci.ece.ac.concurrent.EventVariable;
import javax.realtime.*;

public class YieldTest {

    final static String YIELD_LATENCY = "YieldLatency";
    static int activeThread = 0;

    static class YieldLogic implements Runnable {

        private int count;
        private int yieldCount = 0;
        private HighResTimer timer;
        private PerformanceReport report;
        private EventVariable event;
        
        YieldLogic(HighResTimer timer, int count, PerformanceReport report, EventVariable event) {
            this.timer = timer;
            this.count = count;
            this.report = report;
            this.event = event;
        }

        int getYieldCount() {
            return this.yieldCount;
        }
        
        public void run() {
            String name = Thread.currentThread().getName();
            //            event.signal();
            try {
                event.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            while (this.yieldCount < this.count) {
                this.timer.stop();
                this.yieldCount++;
                
                //                System.out.println(name + "  Executing\t" + yieldCount);
                //                System.out.flush();
                
                // Reach steady state before measuring...
                if (this.yieldCount >= 10)
                    report.addMeasuredVariable(YIELD_LATENCY, timer.getElapsedTime());
                
                //                System.out.println(name + "  Yielding\t" + yieldCount);
                //                System.out.flush();
                RealtimeThread.yield();
            }
        }
    }

    
    static class MainYieldLogic implements  Runnable {

        private PerformanceReport report;
        private int count;
        private int priority;
        private String reportPath;
        
        MainYieldLogic(int count, int priority, String reportPath) {
            report = new PerformanceReport(YIELD_LATENCY);
            this.count = count;
            this.priority = priority;
            this.reportPath = reportPath;
        }
        
        public void run() {
            try {
                HighResTimer timer = new HighResTimer();
                EventVariable event = new EventVariable();
                YieldLogic yieldLogic = new YieldLogic(timer, count, report, event);             
                RealtimeThread rt = new RealtimeThread(new PriorityParameters(this.priority),
                                                       null,  null, null, null, yieldLogic);
                rt.start();
                String name = Thread.currentThread().getName();
                int safenessCount = 0;
                event.signal();
                while ( yieldLogic.getYieldCount() < count && safenessCount < 10000) {
                    safenessCount++;
                    //                    System.out.println(name + "  Executing Main");
                    //                    System.out.flush();
                    timer.reset();
                    timer.start();
                    //                    System.out.println(name + "  Yielding Main");
                    RealtimeThread.yield();
                }
                report.generateDataFile(reportPath + "/");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
                        
    }

    
    public static void main(String[] args) throws Exception {
        final int count = Integer.parseInt(args[0]) + 10;
        final String path = args[1];
        int priority = 90;
        MainYieldLogic logic = new MainYieldLogic(count, priority, path);
        RealtimeThread rtThread = new RealtimeThread(new PriorityParameters(priority),
                                                     null,  null, null, null, logic);
        rtThread.start();
    }
}
