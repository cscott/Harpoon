/* Tests - this is the main logic that calls every RTSJ API test class.
*/

// package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

import java.util.Vector;
import javax.realtime.*;
import com.timesys.*;

public class Tests
{

    /* Report option SHOWTESTS -
    **  true - reports every subtest as it runs
    **  false - only reports failed subtests (preferred)
    */
    private static final boolean SHOWTESTS = true;

    private static int threadFailureCount;
    private static int subcount;  //counts individual subtests for one test
    private static int subfailed; //counts individual failed subtests for one
                                  // test
    private static int count;     //counts all individual subtests
    private static int failed;    //counts all individual subtests that fail

    private static TestReportFile report;  //Report file


    /*
    ** Global Methods for tracking tests
    */

    public static void init()
    {
        System.setOut(System.err);

        System.out.println("Tests: Starting the testing procedure");
        count = 0;
        failed = 0;
        threadFailureCount=0;

        report = new TestReportFile();         //Create report file

        report.println("Running tests...");
        report.println("");

    }

    public static void conclude()
    {
        System.out.println("Tests: Letting threads spin down");

        delay(2);

        System.out.println("Tests: Done running tests");
        printReportTotals();

        try {
            //int pid = Dispatcher.getPID();
            System.out.println("");
            System.out.println(" ***********************************");
            System.out.println(" **** RTSJ API TESTING COMPLETE ****");
            //System.out.println(" **** Please type: kill -9 "+pid+" ****");
        } catch (Exception e) {
            System.out.println(" **** Cannot get PID!           ****");
        }
        System.out.println(" ***********************************");
        System.out.println("");
    }

    /*
    ** newTest - marks beginning of new Test
    */
    static void newTest( String arg )
    {
        report.println("");
        report.println(arg + ":");      // Prints Test heading
        subcount=0;                     // initializes subtest count
        subfailed=0;                    // initializes failed subtests
    }

    /*
    ** increment() - increments count of subtests within a test
    */
    static void increment()
    {
        count++;
        subcount++;
        if(SHOWTESTS)                  // Print what test is running
            report.println("  Running test: " + subcount);
    }

    /*
    ** fail() - reports a failed subtest
    */
    static void fail(String arg)
    {
        report.println("***TEST FAILED***  " + arg +
                       " failed at Subtest " + subcount);

        failed++;
        subfailed++;
    }

    /*
    ** fail() - reports a failed subtest with exception
    */
    static void fail(String arg, Exception e)
    {
        report.println("***TEST FAILED***  " + arg +
                       " failed at Subtest " + subcount);
        report.println("    " + e.toString());
        e.printStackTrace();

        failed++;
        subfailed++;
    }

    /*
    ** threadFail() - reports a failed test within a separate thread
    */
    static void threadFail()
    {
        report.println("***THREAD FAILED***");
        threadFailureCount++;
    }

    static void threadFail(String message)
    {
        report.println("***THREAD FAILED***: " + message);
        threadFailureCount++;
    }

    /*
    ** comment() - prints a comment into the report
    */
    static void comment(String arg)
    {
        report.println("  COMMENT: " + arg);
    }

    /*
    ** printSubTestReporTotals - called at the end of a test to print
    **                       all counts.
    */
    static void printSubTestReportTotals(String arg)
    {
        report.println("");
        report.println("  " + arg + " TOTALS:");
        report.println("  " + subcount + " tests RAN");
        report.println("  " + subfailed + " tests FAILED");
    }

    /*
    ** printReportTotals - called at the end of all tests to print
    **                 the total counts.
    */
    private static void printReportTotals()
    {
        report.println("");
        report.println("OVERALL TEST TOTALS:");
        report.println("  " + count + " tests RUN");
        report.println("  " + failed + " tests FAILED");
        report.println("  " + threadFailureCount + " test thread(s) FAILED");
        if (threadFailureCount > 0)
            report.println("    (see messages beginning with **** "+
                           "THREAD FAILURE:  in the standard output for more "+
                           "information)" );
    }


    // Value is just a relative number - don't want to call sleep here
    public static void delay(int value)
    {
        int i, j;

        final boolean showdelays = true;

        if (showdelays)
            System.out.println("Start delay: " + value);

        /*
        for (i = 0; i < value; i++) {
            for (j = 0; j < 10000; j++) {
                System.out.print("");
                Thread.yield();
            }
        }
        */
        try {
            RealtimeThread.sleep(value*1000);
        }
        catch (InterruptedException e) {
            System.out.println("Sleep interrupted.");
            // ignore;
        }

        if (showdelays)
            System.out.println("End delay: " + value);
    }

}
