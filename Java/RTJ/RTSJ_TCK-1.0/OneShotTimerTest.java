//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              OneShotTimerTest

Subtest 1:
        "public OneShotTimer(HighResolutionTime time, AsyncEventHandler
        handler)"

Subtest 2:
        "public OneShotTimer(HighResolutonTime time, Clock clock,
        AsyncEventHandler handler)"

Subtest 3:
        "public ReleaseParameters createReleaseParameters()"

Subtest 4:
        "public void disable()"

Subtest 5:
        "public void destroy()"

Subtest 6:
        "public void enable()"

Subtest 7:
        "public Clock getClock()"

Subtest 8:
        "public AbsoluteTime getFireTime()"

Subtest 9:
        "public void reschedule(HighResolutionTime time)"

Subtest 10:
        "public void start()"
*/

import javax.realtime.*;

public class OneShotTimerTest
{

    public static void run() {
        OneShotTimer ost = null;
        Object o = null;

        Tests.newTest("OneShotTimerTest (abstract Timer)");

        /* Subtest 1:
        ** Constructor "public OneShotTimer(HighResolutionTime time,
        ** AsyncEventHandler handler)"
        */
        Tests.increment();
        try {
            System.out.println("OneShotTimerTest: OneShotTimer(HighResolution"+
                               "Time,AsyncEventHandler)");
            AEventHandler handler = new AEventHandler();
            ost = new OneShotTimer(new RelativeTime(), handler);
            if( !(ost instanceof OneShotTimer && ost instanceof Timer) )
                throw new Exception("Return object is not instanceof "+
                                    "OneShotTimer nor Timer");
        } catch (Exception e) {
            System.out.println("OneShotTimerTest: OneShotTimer(HighResolution"+
                               "Time,AsyncEventHandler)");
            Tests.fail("new OneShotTimer(new RelativeTime(), handler)",e);
        }

        /* Subtest 2:
        ** Constructor "public OneShotTimer(HighResolutonTime time, Clock
        ** clock, AsyncEventHandler handler)"
        */
        Tests.increment();
        try {
            System.out.println("OneShotTimerTest: OneShotTimer(HighResoluton"+
                               "Time,Clock,AsyncEventHandler)");
            AEventHandler handler = new AEventHandler();
            ost = new OneShotTimer(new RelativeTime(),
                                   Clock.getRealtimeClock(), handler);
            if( !(ost instanceof OneShotTimer && ost instanceof Timer) )
                throw new Exception("Return object is not instanceof "+
                                    "OneShotTimer nor Timer");
        } catch (Exception e) {
            System.out.println("OneShotTimerTest: OneShotTimer(HighResoluton"+
                               "Time,Clock,AsyncEventHandler) failed");
            Tests.fail("new OneShotTimer(new RelativeTime(), "+
                       "Clock.getRealtimeClock(), handler)",e);
        }

        // Timer Tests
        /* Subtest 3:
        ** Method "public ReleaseParameters createReleaseParameters()"
        */
        Tests.increment();
        try {
            System.out.println("OneShotTimerTest: createReleaseParameters()");
            AEventHandler handler = new AEventHandler();
            ost = new OneShotTimer(new RelativeTime(), Clock.
                                   getRealtimeClock(), handler);
            o = ost.createReleaseParameters();
            if (o == null)
                System.out.println("o is null");
            if (! (o instanceof ReleaseParameters))
                throw new Exception("Return object is not instanceof "+
                                    "ReleaseParameters");
        } catch (Exception e) {
            System.out.println("OneShotTimerTest: createReleaseParameters()"+
                               " failed");
            Tests.fail("ost.createReleaseParameters()",e);
        }

        /* Subtest 4:
        ** Method "public void destroy()"
        */
        Tests.increment();
        try {
            System.out.println("OneShotTimerTest: destroy()");
            AEventHandler handler = new AEventHandler();
            ost = new OneShotTimer(new RelativeTime(),
                                   Clock.getRealtimeClock(), handler);
            ost.destroy();
        } catch (Exception e) {
            System.out.println("OneShotTimerTest: destroy() failed");
            Tests.fail("ost.destroy()",e);
        }

        /* Subtest 5:
        ** Method "public void disable()"
        */
        Tests.increment();
        try {
            System.out.println("OneShotTimerTest: disable()");
            AEventHandler handler = new AEventHandler();
            ost = new OneShotTimer(new RelativeTime(),
                                   Clock.getRealtimeClock(), handler);
            ost.disable();
        } catch (Exception e) {
            System.out.println("OneShotTimerTest: disable() failed");
            Tests.fail("ost.disable()",e);
        }

        /* Subtest 6:
        ** Method "public void enable()"
        */
        Tests.increment();
        try {
            System.out.println("OneShotTimerTest: enable()");
            AEventHandler handler = new AEventHandler();
            ost = new OneShotTimer(new RelativeTime(),
                                   Clock.getRealtimeClock(), handler);
            ost.enable();
        } catch (Exception e) {
            System.out.println("OneShotTimerTest: enable() failed");
            Tests.fail("ost.disable()",e);
        }


        /* Subtest 7:
        ** Method "public Clock getClock()"
        */
        Tests.increment();
        try {
            System.out.println("OneShotTimerTest: getClock()");
            AEventHandler handler = new AEventHandler();
            ost = new OneShotTimer(new RelativeTime(),
                                   Clock.getRealtimeClock(), handler);
            o = ost.getClock();
            if (! (o instanceof Clock))
                throw new Exception("Return object is not instanceof Clock");
        } catch (Exception e) {
            System.out.println("OneShotTimerTest: getClock() failed");
            Tests.fail("ost.getClock()",e);
        }

        /* Subtest 8:
        ** Method "public AbsoluteTime getFireTime()"
        */
        Tests.increment();
        try {
            System.out.println("OneShotTimerTest: getFireTime()");
            AEventHandler handler = new AEventHandler();
            ost = new OneShotTimer(new RelativeTime(),
                                   Clock.getRealtimeClock(), handler);
            o = ost.getFireTime();
            if (! (o instanceof AbsoluteTime))
                throw new Exception("Return object is not instanceof "+
                                    "AbsoluteTime");
        } catch (Exception e) {
            System.out.println("OneShotTimerTest: getFireTime()");
            Tests.fail("ost.getFireTime()",e);
        }

        /* Subtest 9:
        ** Method "public void reschedule(HighResolutionTime time)"
        */
        Tests.increment();
        try {
            System.out.println("OneShotTimerTest: reschedule(HighResolution"+
                               "Time)");
            AEventHandler handler = new AEventHandler();
            ost = new OneShotTimer(new RelativeTime(),
                                   Clock.getRealtimeClock(), handler);
            ost.reschedule(new RelativeTime(3000L,300));
        } catch (Exception e) {
            System.out.println("OneShotTimerTest: reschedule(HighResolution"+
                               "Time) failed");
            Tests.fail("ost.reschedule()",e);
        }

        /* Subtest 10:
        ** Method "public void start()"
        */
        Tests.increment();
        try {
            System.out.println("OneShotTimerTest: start()");
            AsyncEventHandler h
                = new AsyncEventHandler(null, null,null,
                                        new VTMemory(1048576,1048576),
                                        null,false,null){
                        public void handleAsyncEvent()
                        {
                            System.out.println("OneShotTimerTest: Inside "+
                                               "AsyncEventHandler - Subtest 9"+
                                               " of OneShotTimerTest");
                        }
                    };

            ost = new OneShotTimer(new RelativeTime(),h);
            ost.start();
        } catch (Exception e) {
            System.out.println("OneShotTimerTest: start() failed");
            Tests.fail("ost.start()",e);
        }
        Tests.printSubTestReportTotals("OneShotTimerTest");
    }

    /*
      public static void example() {
      SchedulingParameters highPriority = new PriorityParameters(PriorityScheduler.instance().getMaxPriority());
      try {
      Thread.sleep(1000);
      } catch (InterruptedException ie) {}
      (System.currentTimeMillis()).toString();
      }

      private static void TestTimer(String title, Timer t) {
      System.out.println("\n" + title + " test:\n");
      final long T0 = t.getFireTime().getMilliseconds();
      ReleaseParameters rp = t.createReleaseParameters();
      rp.setCost(new RelativeTime(10,0));
      rp.toString();
      t.addHandler(new AsyncEventHandler(highPriority, rp, null) {
      public void handleAsyncEvent() {
      (System.currentTimeMillis() - T0).toString();
      }
      });
      t.start();
      }
    */

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
