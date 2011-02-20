// ************************************************************************
//    $Id: SynchYieldTest.java,v 1.1 2002-07-02 15:54:15 wbeebee Exp $
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

public class SynchYieldTest {

    static final String SYNCH_YIELD_TIME = "SynchYieldTime";
    
    static class Synchornizer {
        
        private HighResTimer timer;
        private PerformanceReport report; 
        private EventVariable exitEvent;
        private EventVariable enterEvent;
        private boolean firstTime = true;
        
        public Synchornizer(EventVariable exitEvent,EventVariable enterEvent, PerformanceReport report) {
            this.exitEvent = exitEvent;
            this.enterEvent = enterEvent;
            this.report = report;
            timer = new HighResTimer();
        }

        public synchronized void enterLow() {
            // System.out.println("LP>> enterLow()");
            if (!firstTime) {
                // System.out.println("LP>> Signaling enterEvent...");
                enterEvent.signal();
            }
            else
                firstTime = false;
            try {
                // System.out.println("LP>> Waiting on exitEvent...");
                exitEvent.await();
                //                // System.out.println("LP>> exitEvent Signaled...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // System.out.println("LP>> leaveLow()");
            timer.start();
        }

        public synchronized void enterHigh() {
            // System.out.println("HP>> enterHigh()");
            timer.stop();
            report.addMeasuredVariable(SYNCH_YIELD_TIME, timer.getElapsedTime());
            timer.reset();
            // System.out.println("HP>> leaveHigh()");
        }
        
    }
    public static void main(String args[]) throws Exception {
        int MAX_PRIORITY = 90;
        final int count = Integer.parseInt(args[0]);
        final EventVariable exitEvent = new EventVariable();
        final EventVariable nextIterationEvent = new EventVariable();
        final EventVariable enterEvent = new EventVariable();
        final PerformanceReport report =
            new PerformanceReport(SYNCH_YIELD_TIME + "Test");

        final String dataPath = args[1];
        final Synchornizer synch = new Synchornizer(exitEvent, enterEvent, report);

        Runnable lowPrioLogic = new Runnable() {
                public void run() {
                    for (int i = 0; i < count; i++) {
                        synch.enterLow();
                        try {
                            if (i == count - 1)
                                break;
                            // System.out.println("LP>> Waiting on nextIterationEvent... - " + i);
                            nextIterationEvent.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

        Runnable highPrioLogic = new Runnable() {
                public void run() {
                    for (int i = 0; i < count; i++) {
                        // System.out.println("HP>> Signaling exitEvent...");
                        exitEvent.signal();
                        synch.enterHigh();
                        try {
                            if (i == count - 1)
                                break;
                            // System.out.println("HP>> Signaling nextIterationEvent...  - " + i);
                            nextIterationEvent.signal();
                            // System.out.println("HP>> Waiting on enterEvent...");
                            enterEvent.await();
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
                
            };

        RealtimeThread lowPrio =
            new RealtimeThread(new PriorityParameters(MAX_PRIORITY - 5),
                               null,
                               null,
                               null,
                               null,
                               lowPrioLogic);

        RealtimeThread highPrio =
            new RealtimeThread(new PriorityParameters(MAX_PRIORITY),
                               null,
                               null,
                               null,
                               null,
                               highPrioLogic);

        lowPrio.start();
        highPrio.start();
    }
}
