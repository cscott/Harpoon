/*-------------------------------------------------------------------------*
 * $Id: PerformanceTestCase.java,v 1.1 2002-07-02 15:54:06 wbeebee Exp $
 *-------------------------------------------------------------------------*/
package edu.uci.ece.doc.rtjperf.sys;

import edu.uci.ece.ac.time.*;

/**
 * This class provide a base class for all the performance testcases. 
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public abstract class PerformanceTestCase {

    protected final  HighResTimer runTime = new HighResTimer();
    protected final PerformanceReport performanceReport;

    public static final String TEST_EXEC_TIME = "TestExecutionTime";
    
    public PerformanceTestCase(String name, String description) {
        this.performanceReport = new PerformanceReport(name, description);
    }
    
    /**
     * Can be overridden by subclass to execute some warmup or set-up
     * code before the test logic.
     *
     */
    protected void preRun() { }


    /**
     * The method run takes care of invoking the preRun, runLogic, and
     * postRun methods. This method takes also a measurement of the
     * time spent running the runLogic method.
     *
     */
    public void run() {
        this.preRun();
        this.runTime.start();
        this.runLogic();
        this.runTime.stop();
        this.postRun();
        this.setTestExecutionTime();
    }

    protected void setTestExecutionTime() {
        this.performanceReport.addMeasuredVariable(TEST_EXEC_TIME,
                                                   this.runTime.getElapsedTime());
    }
    
    /**
     * This method has to be overridden by subclasses in order to provide  
     *
     */
    protected abstract void runLogic();
    
    /**
     * Generates a report for the test.
     *
     * @return a <code>PerformanceReport</code> value
     */
    public PerformanceReport getPerformanceReport() {
        return this.performanceReport;
    }
    
    /**
     * Can be overridden by subclass to execute some clean-up after
     * the test logic.
     *
     */
    protected void postRun() { }
}
