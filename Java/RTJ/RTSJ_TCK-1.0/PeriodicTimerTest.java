//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              PeriodicTimerTest

Subtest 1:
"public PeriodicTimer(HighResolutionTime start, RelativeTime interval,
AsyncEventHandler handler)"

Subtest 2:
"public PeriodicTimer(HighResolutonTime start, RelativeTime interval, Clock
clock, AsyncEventHandler handler)"

Subtest 3:
"public ReleaseParameters createReleaseParameters()"

Subtest 4:
"public AbsoluteTime getFireTime()"

Subtest 5:
"public RelativeTime getInterval()"

Subtest 6:
"public RelativeTime setInterval(RelativeTime interval)"

Subtest 7:
"public void start()" NOTE: fire() is not tested because it should really not
be called directly and causes IllegalMonitorStateExceptions to be thrown from
 Object.wait().  It is uncertain as to why this is a public method.
*/

import javax.realtime.*;

public class PeriodicTimerTest
{
    public static void run()
    {
        PeriodicTimer pt = null;
        Object o = null;

        Tests.newTest("PeriodicTimerTest");

        /* Subtest 1:
        ** Constructor "public PeriodicTimer(HighResolutionTime start,
        ** RelativeTime interval, AsyncEventHandler handler)"
        */
        Tests.increment();
        try {
            System.out.println("PeriodicTimerTest: PeriodicTimer(High"+
                               "ResolutionTime,RelativeTime,AsyncEvent"+
                               "Handler)");
            AEventHandler handler = new AEventHandler();
            o = new PeriodicTimer(new RelativeTime(1000L,100), new
                RelativeTime(2000L,0), handler);
            if( !(o instanceof PeriodicTimer && o instanceof Timer) )
                throw new Exception("Return object is not instanceof "+
                                    "PeriodicTimer nor Timer");
        } catch (Exception e) {
            System.out.println("PeriodicTimerTest: PeriodicTimer(High"+
                               "ResolutionTime,RelativeTime,AsyncEvent"+
                               "Handler) failed");
            Tests.fail("new PeriodicTimer(new RelativeTime(1000L,100), new "+
                       "RelativeTime(2000L,0), handler)",e);
        }

        /* Subtest 2:
        ** Constructor "public PeriodicTimer(HighResolutonTime start,
        ** RelativeTime interval, Clock clock, AsyncEventHandler handler)"
        */
        Tests.increment();
        try {
            System.out.println("PeriodicTimerTest: PeriodicTimer(High"+
                               "ResolutonTime,RelativeTime,Clock,"+
                               "AsyncEventHandler)");
            AEventHandler handler = new AEventHandler();
            o = new PeriodicTimer(new RelativeTime(5000L,0), new
                RelativeTime(10000L,0),Clock.getRealtimeClock(), handler);
            if( !(o instanceof PeriodicTimer && o instanceof Timer) )
                throw new Exception("Return object is not instanceof "+
                                    "PeriodicTimer nor Timer");
        } catch (Exception e) {
            System.out.println("PeriodicTimerTest: PeriodicTimer(High"+
                               "ResolutonTime,RelativeTime,Clock,"+
                               "AsyncEventHandler) failed");
            Tests.fail("new PeriodicTimer(new RelativeTime(5000L,0),new "+
                       "RelativeTime(10000L,0), Clock.getRealtimeClock(), "+
                       "handler)",e);
        }

        /* Subtest 3:
        ** Method "public ReleaseParameters createReleaseParameters()"
        */
        Tests.increment();
        try {
            System.out.println("PeriodicTimerTest: createReleaseParameters()");
            AEventHandler handler = new AEventHandler();
            pt = new PeriodicTimer(new RelativeTime(1000L,0), new
                RelativeTime(2000L,0),handler);
            o = pt.createReleaseParameters();
            if (! (o instanceof ReleaseParameters))
                throw new Exception("Return object is not instanceof "+
                                    "ReleaseParameters");
        } catch (Exception e) {
            System.out.println("PeriodicTimerTest: createReleaseParameters() "+
                               "failed");
            Tests.fail("pt.createReleaseParameters()",e);
        }

        /* Subtest 4:
        ** Method "public AbsoluteTime getFireTime()"
        */
        Tests.increment();
        try {
            System.out.println("PeriodicTimerTest: getFireTime()");
            AEventHandler handler = new AEventHandler();
            pt = new PeriodicTimer(new RelativeTime(1000L,0), new
                RelativeTime(2000L,0),handler);
            o = pt.getFireTime();
            if (! (o instanceof AbsoluteTime))
                throw new Exception("Return object is not instanceof "+
                                    "AbsoluteTime");
        } catch (Exception e) {
            System.out.println("PeriodicTimerTest: getFireTime()");
            Tests.fail("pt.getFireTime()",e);
        }

        /* Subtest 5:
        ** Method "public RelativeTime getInterval()"
        */
        Tests.increment();
        try {
            System.out.println("PeriodicTimerTest: getInterval()");
            long millis=3000L;
            int nanos=100;
            RelativeTime rt;
            AEventHandler handler = new AEventHandler();
            pt = new PeriodicTimer(new RelativeTime(1000L,0), new
                RelativeTime(millis,nanos),handler);
            o = pt.getInterval();
            if (! (o instanceof RelativeTime))
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime");
            rt = (RelativeTime)o;
            long gmillis = rt.getMilliseconds();
            int gnanos = rt.getNanoseconds();
            if ( (gmillis != millis) || (gnanos != nanos))
                throw new Exception("Returned interval does not match the "+
                                    "interval it was constructed with");
        } catch (Exception e) {
            System.out.println("PeriodicTimerTest: getInterval() failed");
            Tests.fail("pt.getInterval()",e);
        }

        /* Subtest 6:
        ** Method "public RelativeTime setInterval(RelativeTime interval)"
        */
        Tests.increment();
        try {
            System.out.println("PeriodicTimerTest: setInterval(RelativeTime)");
            long millis=4000L;
            int nanos=200;
            RelativeTime rt = new RelativeTime(millis,nanos);
            AEventHandler handler = new AEventHandler();
            pt = new PeriodicTimer(new RelativeTime(1000L,0), new
                RelativeTime(),handler);
            pt.setInterval(rt);
            RelativeTime grt = pt.getInterval();
            long gmillis = grt.getMilliseconds();
            int gnanos = grt.getNanoseconds();
            if ( (gmillis != millis) || (gnanos != nanos))
                throw new Exception("Returned interval does not match the "+
                                    "interval that was set");
        } catch (Exception e) {
            System.out.println("PeriodicTimerTest: setInterval(RelativeTime)");
            Tests.fail("pt.setInterval(rt)",e);
        }

        /* Subtest 7:
        ** Method "public void start()"
        */
        Tests.increment();
        try {
            System.out.println("PeriodicTimerTest: start()");
            AsyncEventHandler h
                = new AsyncEventHandler(null, null,null,
                                        new VTMemory(1048576,1048576),
                                        null,false,null){
                        public void handleAsyncEvent()
                        {
                            System.out.println("PeriodicTimerTest: Inside "+
                                               "AsyncEventHandler - Subtest 7"+
                                               " of PeriodicTimerTest");
                    }
                };

            pt = new PeriodicTimer(null, new RelativeTime(1000L,0), h);

            pt.start();
            for (int i=0;i<10000000;i++);
            pt.disable();

        } catch (Exception e) {
            System.out.println("PeriodicTimerTest: start() failed");
            Tests.fail("pt.start()",e);
        }


        Tests.printSubTestReportTotals("PeriodicTimerTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
