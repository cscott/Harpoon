//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              TimedTest

Subtest 1:
        "public Timed(HighResolutionTime time)"

Subtest 2:
        "public Timed(HighResolutionTime time)"

Subtest 3:
        "public void resetTime(HighResolutionTime time)"

Subtest 4:
        "public boolean doInterruptible(Interruptible logic)"
*/

import javax.realtime.*;

public class TimedTest
{

    public static void run()
    {
        Timed t = null;
        Object o = null;
        Tests.newTest("TimedTest");

        /* Subtest 1:
        ** Constructor "public Timed(HighResolutionTime time)"
        */
        Tests.increment();
        try {
            System.out.println("TimedTest: Timed(HighResolutionTime)");
            RelativeTime time = new RelativeTime(0,0);
            t = new Timed(time);
            if( !(t instanceof Timed && t instanceof
                  AsynchronouslyInterruptedException) )
                throw new Exception("Return object is not instanceof Timed "+
                                    "nor AsynchronouslyInterruptedException");
        } catch (Exception e) {
            System.out.println("TimedTest: Timed(HighResolutionTime) failed");
            Tests.fail("new Timed(time)",e);
        }

        /* Subtest 2:
        ** Constructor "public Timed(HighResolutionTime time)"
        ** where time is null.
        ** IllegalArgumentException is expected
        */
        Tests.increment();
        try {
            System.out.println("TimedTest: Timed(null)");
            RelativeTime time = null;
            t = new Timed(time);
            Tests.fail("new Timed(null) did not throw expected exception");
        } catch (Exception e) {
            System.out.println("TimedTest: Timed(null) threw exception");
            if (! (e instanceof IllegalArgumentException))
                Tests.fail("new Timed(null) threw an exception other than "+
                           "IllegalArgumentException",e);
            else
                System.out.println("...as expected.");
        }

        /* Subtest 3:
        ** Method "public void resetTime(HighResolutionTime time)"
        */
        Tests.increment();
        try {
            System.out.println("TimedTest: resetTime(HighResolutionTime)");
            RelativeTime time = new RelativeTime(0,0);
            RelativeTime newtime = new RelativeTime(1000L,50);
            t = new Timed(time);
            t.resetTime(newtime);
        } catch (Exception e) {
            System.out.println("TimedTest: resetTime(HighResolutionTime) "+
                               "failed");
            Tests.fail("t.resetTime(newtime)");
        }

        /* Subtest 4:
        ** Method "public boolean doInterruptible(Interruptible logic)"
        */
        Tests.increment();
        try {
            System.out.println("TimedTest: doInterruptible(Interruptible)");
            Semaphore semaphore = new Semaphore();
            Timed shared_timed = new Timed(new RelativeTime(10000L,0));
            InterruptibleTimedThread iThread = new InterruptibleTimedThread(
                                                     shared_timed, semaphore);
            iThread.start();
            semaphore.sWait();
            boolean x = shared_timed.fire();
            Tests.delay(1);
            if (x!=true)
                throw new Exception("Did not receive expected true value "+
                                    "from fire()");
        } catch (Exception e) {
            System.out.println("TimedTest: doInterruptible(Interruptible) "+
                               "failed");
            Tests.fail("doInterruptible()",e);
        }

        Tests.printSubTestReportTotals("TimedTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
