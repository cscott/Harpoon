/*-------------------------------------------------------------------------*
 * $Id: PrioInvTest.java,v 1.1 2002-07-02 15:53:54 wbeebee Exp $
 *-------------------------------------------------------------------------*/

package edu.uci.ece.doc.rtjperf.synch;

import edu.uci.ece.ac.time.HighResTimer;
import edu.uci.ece.ac.time.HighResTime;
import edu.uci.ece.ac.time.PerformanceReport;
import edu.uci.ece.ac.concurrent.EventVariable;
import javax.realtime.*;

/**
 * This class provide a classical priority inversion test, in which a
 * low priority thread is inside a Java monitor and an high priority
 * thread is waiting on that monitor. At the same time a mid-priority
 * thread becomes ready to execute.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class PrioInvTest {

    static final String PRIO_INV_TIME = "PrioInvTime";

    /**
     * This methods simply burns cpu cycle for a time that is
     * proportional to the argument passed.
     *
     * @param workAmount The amount of work to be performed.
     */
    static void cpuBurner(double workAmount) {
        
        double increment = 0.0000005;
        double count = 0;
        
        while (count < workAmount) {
            count += increment;
        }
    }

    static void cpuBurner(double workAmount, char ch) {
        
        double increment = 0.0000005;
        double count = 0;
        
        while (count < workAmount) {
            count += increment;
            System.out.print(ch);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    //
    //     INNER CLASSES
    //
    
    static class Monitor {

        private EventVariable lowPrioEnterEvent;
        private EventVariable highPrioReadyEvent;
        private HighResTimer timer;
        private PerformanceReport report;
        
        public Monitor(EventVariable lowPrioEnterEvent,
                       EventVariable highPrioReadyEvent,
                       HighResTimer timer,
                       PerformanceReport report)
        {
            this.lowPrioEnterEvent = lowPrioEnterEvent;
            this.highPrioReadyEvent = highPrioReadyEvent;
            this.timer = timer;
            this.report = report;
        }

        public synchronized void enterLow() {
            System.out.println(">> LowPrio: Monitor Entered");
            lowPrioEnterEvent.signal();
            HighResTimer execTimer = new HighResTimer(); 
//             try {
//                 System.out.println(">> LowPrio:  waiting for highPrioReadyEvent");
//                 this.highPrioReadyEvent.await();
//                 System.out.println(">> LowPrio: highPrioReadyEvent Notified");
//             } catch (InterruptedException e) {
//                 e.printStackTrace();
//             }
            //            RealtimeThread.yield();
            System.out.println(">> LowPrio: Starting to burn some CPU Cycles");
            execTimer.start();
            PrioInvTest.cpuBurner(1);
            execTimer.stop();
            System.out.println(">> LowPrio: Done  burning  CPU Cycles for: " + execTimer.getElapsedTime());
            System.out.println(">> LowPrio: Exiting Monitor");
        }

        public synchronized void enterHigh() {
            System.out.println(">> HighPrio: Monitor Entered");
            this.timer.stop();
            //            this.report.addMeasuredVariable(PRIO_INV_TIME, timer.getElapsedTime());
            System.out.println(">> HighPrio: Exiting Monitor");
            System.out.println(">> HighPrio: Waited Time: " + this.timer.getElapsedTime());
            this.timer.reset();
        }
    }

    public static void main(String[] args) {

        MonitorControl.setMonitorControl(PriorityInheritance.instance());
        HighResTimer execTimer = new HighResTimer();
        execTimer.start();
        PrioInvTest.cpuBurner(1);
        execTimer.stop();
        execTimer.getElapsedTime();
        System.out.println("Reference Time: " + execTimer.getElapsedTime());

        final HighResTimer timer = new HighResTimer();
        final EventVariable lowPrioEnterEvent = new EventVariable();
        final EventVariable highPrioReadyEvent = new EventVariable();
        final EventVariable midPrioStartEvent = new EventVariable();
        final EventVariable startTestEvent = new EventVariable();
        final PerformanceReport report = new PerformanceReport(PRIO_INV_TIME + "Test");
        final Monitor monitor = new Monitor(lowPrioEnterEvent, highPrioReadyEvent, timer, report);

        // -- Low Priority Thread Logic --
        Runnable lowPrioLogic = new Runnable() {
                public void run() {
                    System.out.println(">> LowPrioLogic: Started");
                    startTestEvent.broadCastSignal();
                    monitor.enterLow();
                    System.out.println(">> LowPrioLogic: Completed");
                }
            };
        PriorityParameters lowPrioParams =
            new PriorityParameters( 15);//PriorityScheduler.MIN_PRIORITY);

        RealtimeThread lowPrioThread = new RealtimeThread(lowPrioParams,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          lowPrioLogic);
        
        // -- Mid Priority Thread Logic --
        Runnable midPrioLogic = new Runnable() {
                public void run() {
                    try {
                        
                        startTestEvent.await();
                        midPrioStartEvent.await();
                        System.out.println(">> MidPrioLogic: Started");
                        PrioInvTest.cpuBurner(2);//, '.');
                        System.out.println(">> MidPrioLogic: Completed");
                    } catch (InterruptedException e) {
                        e. printStackTrace();
                    }
                }
            };
        PriorityParameters medPrioParams =
            new PriorityParameters(20);
                                   //(PriorityScheduler.MAX_PRIORITY- PriorityScheduler.MIN_PRIORITY)/2);
        
        RealtimeThread medPrioThread = new RealtimeThread(medPrioParams,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          midPrioLogic);


        Runnable highPrioLogic = new Runnable() {
                public void run() {
                    try {
                        startTestEvent.await();
                        lowPrioEnterEvent.await();
                        System.out.println(">> HighPrioLogic: Started");
                        highPrioReadyEvent.signal();
                        midPrioStartEvent.signal();
                        System.out.println(">> HighPrioLogic:  Waiting on Monitor");
                        timer.start();
                        monitor.enterHigh();
                        System.out.println(">> HighPrioLogic: Completed");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
        PriorityParameters highPrioParams = new PriorityParameters(50);// PriorityScheduler.MAX_PRIORITY);
        RealtimeThread highPrioThread = new RealtimeThread(highPrioParams,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           highPrioLogic);

        highPrioThread.start();
        medPrioThread.start();
        lowPrioThread.start();
    }
}
