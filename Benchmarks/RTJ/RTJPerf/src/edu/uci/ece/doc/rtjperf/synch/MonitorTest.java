/*-------------------------------------------------------------------------*
 * $Id: MonitorTest.java,v 1.1 2002-07-02 15:53:54 wbeebee Exp $
 *-------------------------------------------------------------------------*/

package edu.uci.ece.doc.rtjperf.synch;

import edu.uci.ece.ac.time.HighResTimer;
import edu.uci.ece.ac.time.HighResTime;
import edu.uci.ece.ac.time.PerformanceReport;
import edu.uci.ece.ac.concurrent.EventVariable;
import javax.realtime.*;

/**
 * This class provide a test to determine the amount of time it takes
 * to enter and exit a Java Monitor.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class MonitorTest {

    static final String MONITOR_ENTER_TIME =  "MonitorEnterTime";
    static final String MONITOR_EXIT_TIME =  "MonitorExitTime";
    
    static class Monitor {
        private HighResTimer timer;
        private PerformanceReport report;
        
        public Monitor(HighResTimer timer, PerformanceReport report) {
            this.timer = timer;
            this.report = report;
        }

        public synchronized void enter() {
            timer.stop();
            report.addMeasuredVariable(MONITOR_ENTER_TIME, timer.getElapsedTime());
            timer.reset();

            timer.start();
        }
    }

    public static void main(String[] args) {
        final int count = Integer.parseInt(args[0]);
        final PerformanceReport report =
            new PerformanceReport("MonitorTest");
        
        final String dataPath = args[1];
        
        Runnable logic = new Runnable() {
                public void run() {
                    final HighResTimer timer = new HighResTimer();
                    Monitor m = new Monitor(timer, report);

                    for (int i = 0; i < count; ++i) {
                        timer.start();
                        m.enter();
                        timer.stop();
                        report.addMeasuredVariable(MONITOR_EXIT_TIME, timer.getElapsedTime());
                        timer.reset();
                    }
                    try {
                        report.generateDataFile(dataPath + "/");
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
            };

        RealtimeThread rtThread =
            new RealtimeThread(new PriorityParameters(PriorityScheduler.MAX_PRIORITY),
                               null,
                               null,
                               null,
                               null,
                               logic);
        
        rtThread.start();
    }
}
