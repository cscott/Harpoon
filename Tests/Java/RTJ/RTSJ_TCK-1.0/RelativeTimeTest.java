//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              RelativeTimeTest

Subtest 1:
        "public RelativeTime()"

Subtest 2:
        "public RelativeTime(long millis, int nanos)"

Subtest 3:
        "public RelativeTime(Relative time)"

Subtest 4:
        "public AbsoluteTime absolute(Clock clock)

Subtest 5:
        "public AbsoluteTime absolute(Clock clock)
        where clock is null

Subtest 6:
        "public AbsoluteTime absolute(Clock clock, AbsoluteTime destination)

Subtest 7:
        "public AbsoluteTime absolute(Clock clock, AbsoluteTime destination)
        where clock and destination are both null

Subtest 8:
        "public RelativeTime add(long millis, int nanos)"

Subtest 9:
        "public RelativeTime add(long millis, int nanos, RelativeTime
        destination)"

Subtest 10:
        "public RelativeTime add(long millis, int nanos, RelativeTime
        destination)" where destination is null

Subtest 11:
        "public RelativeTime add(RelativeTime time)"

Subtest 12:
        "public RelativeTime add(RelativeTime time, RelativeTime destination)

Subtest 13:
        "public RelativeTime add(RelativeTime time, RelativeTime destination)
        where the destination is null

Subtest 14:
        "public void addInterarrivalTo(AbsoluteTime destination)"

Subtest 15:
        "public RelativeTime getInterarrivalTime(RelativeTime destination)"

Subtest 16:
        "public RelativeTime getInterarrivalTime(RelativeTime destination)"
        where destination is null

Subtest 17:
        "public RelativeTime relative(Clock clock)

Subtest 18:
        "public RelativeTime relative(Clock clock)
        where clock is null

Subtest 19:
        "public RelativeTime relative(Clock clock, RelativeTime destination)

Subtest 20:
        "public RelativeTime relative(Clock clock, RelativeTime destination)
        where clock and destination are both null

Subtest 21:
        "public void set(HighResolutionTime time)

Subtest 22:
        "public void set(HighResolutionTime time) where time is null

Subtest 23:
        "public void set(long millis)"

Subtest 24:
        "public void set(long millis, int nanos)"

Subtest 25:
        "public final RelativeTime subtract(RelativeTime time)"

Subtest 26:
        "public RelativeTime subtract(RelativeTime time, RelativeTime
        destination)"

Subtest 27:
        "public RelativeTime subtract(RelativeTime time, RelativeTime
        destination)" where destination is null

Subtest 28:
        "public java.lang.String toString()"

Subtest 29:
        "public static void waitForObject(Object target,
                                          HighResolutionTime time)"

*/


import javax.realtime.*;
import java.util.*;

public class RelativeTimeTest
{
    public static void run()
    {
        RelativeTime rt = null;
        Object o = null;
        Tests.newTest("RelativeTimeTest");

        /* Subtest 1:
        ** Constructor "public RelativeTime()"
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: RelativeTime()");
            rt = new RelativeTime();
            if( !(rt instanceof RelativeTime && rt instanceof HighResolutionTime) )
                throw new Exception("Return object is not instanceof RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: RelativeTime() failed");
            Tests.fail("new RelativeTimeT()",e);
        }

        /* Subtest 2:
        ** Constructor "public RelativeTime(long millis, int nanos)"
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: RelativeTime(long,int)");
            rt = new RelativeTime(100L,100);
            if( !(rt instanceof RelativeTime && rt instanceof
                  HighResolutionTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: RelativeTime(long,int) "+
                               "failed");
            Tests.fail("new RelativeTime(100L,100)",e);
        }

        /* Subtest 3:
        ** Constructor "public RelativeTime(Relative time)"
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: RelativeTime(RelativeTime)");
            rt = new RelativeTime(100L,100);
            RelativeTime newrt = new RelativeTime(rt);
            if( !(newrt instanceof RelativeTime && newrt instanceof HighResolutionTime) )
                throw new Exception("Return object is not instanceof RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: RelativeTime(RelativeTime) "+
                               "failed");
            Tests.fail("new RelativeTime(rt)",e);
        }

        /* Subtest 4:
        ** method "public AbsoluteTime absolute(Clock clock)
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: absolute(Clock,"+
                               "AbsoluteTime)");
            Clock clock = Clock.getRealtimeClock();
            o = rt.absolute(clock);
            if (! (o instanceof AbsoluteTime && o instanceof
                   HighResolutionTime))
                throw new Exception("Return object is not instanceof "+
                                    "AbsoluteTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: absolute(Clock,"+
                               "AbsoluteTime) failed");
            Tests.fail("rt.absolute(clock, drt)",e);
        }

        /* Subtest 5:
        ** method "public AbsoluteTime absolute(Clock clock)
        ** where clock is null
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: absolute(null)");
            o = rt.absolute(null);
            if (! (o instanceof AbsoluteTime && o instanceof
                   HighResolutionTime))
                throw new Exception("Return object is not instanceof "+
                                    "AbsoluteTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: absolute(null) "+
                               "failed");
            Tests.fail("rt.absolute(null)",e);
        }

        /* Subtest 6:
        ** method "public AbsoluteTime absolute(Clock clock, AbsoluteTime
        ** destination)
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: absolute(Clock,"+
                               "AbsoluteTime)");
            AbsoluteTime dat = new AbsoluteTime();
            Clock clock = Clock.getRealtimeClock();
            o = rt.absolute(clock,dat);
            if (! (o instanceof AbsoluteTime && o instanceof
                   HighResolutionTime))
                throw new Exception("Return object is not instanceof "+
                                    "AbsoluteTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: absolute(Clock,"+
                               "AbsoluteTime) failed");
            Tests.fail("rt.absolute(clock, drt)",e);
        }

        /* Subtest 7:
        ** method "public AbsoluteTime absolute(Clock clock, AbsoluteTime
        ** destination) where clock and destination are both null
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: absolute(null, null)");
            o = rt.absolute(null, null);
            if (! (o instanceof AbsoluteTime && o instanceof
                   HighResolutionTime))
                throw new Exception("Return object is not instanceof "+
                                    "AbsoluteTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: absolute(null, null) "+
                               "failed");
            Tests.fail("rt.absolute(null, null)",e);
        }

        /* Subtest 8:
        ** Method "public RelativeTime add(long millis, int nanos)"
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: add(long,int)");
            long millis = 10223;
            int nanos = 100;
            o = rt.add(millis, nanos);
            if( !(o instanceof RelativeTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: add(long,int)");
            Tests.fail("rt.add(millis,nanos)",e);
        }

        /* Subtest 9:
        ** Method "public RelativeTime add(long millis, int nanos,
        ** RelativeTime destination)"
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: add(long,int,RelativeTime)");
            long millis = 10223;
            int nanos = 100;
            o = rt.add(millis, nanos, rt);
            if( !(o instanceof RelativeTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: add(long,int,RelativeTime) "+
                               "failed");
            Tests.fail("rt.add(millis,nanos,rt)",e);
        }

        /* Subtest 10
        ** Method "public RelativeTime add(long millis, int nanos,
        ** RelativeTime destination)" where destination is null
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: add(long,int,null)");
            long millis = 10223;
            int nanos = 100;
            o = rt.add(millis, nanos, null);
            if( !(o instanceof RelativeTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: add(long,int,null) failed");
            Tests.fail("rt.add(millis,nanos,null)",e);
        }

        /* Subtest 11:
        ** Method "public RelativeTime add(RelativeTime time)"
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: add(RelativeTime)");
            RelativeTime nrt = new RelativeTime(100L,100);
            o = rt.add(nrt);
            if( !(o instanceof RelativeTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: add(RelativeTime) failed");
            Tests.fail("rt.add(nrt)",e);
        }

        /* Subtest 12:
        ** Method "public RelativeTime add(RelativeTime time, RelativeTime
        ** destination)
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: add(RelativeTime,"+
                               "RelativeTime)");
            RelativeTime nrt = new RelativeTime(100L,100);
            o = rt.add(nrt, rt);
            if( !(o instanceof RelativeTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: add(RelativeTime,"+
                               "RelativeTime) failed");
            Tests.fail("rt.add(nrt,rt)",e);
        }

        /* Subtest 13:
        ** Method "public RelativeTime add(RelativeTime time,
        ** RelativeTime destination) where the destination is null
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: add(RelativeTime,null)");
            RelativeTime nrt = new RelativeTime(100L,100);
            o = rt.add(nrt, null);
            if( !(o instanceof RelativeTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: add(RelativeTime,null) "+
                               "failed");
            Tests.fail("rt.add(nrt,null)",e);
        }

        /* Subtest 14:
        ** Method "public void addInterarrivalTo(AbsoluteTime destination)"
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: addInterarrivalTo("+
                               "AbsoluteTime)");
            AbsoluteTime dest = new AbsoluteTime(0,0);
            rt.addInterarrivalTo(dest);
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: addInterarrivalTo("+
                               "AbsoluteTime) failed");
            Tests.fail("rt.addInterarrival(dest)",e);
        }

        /* Subtest 15:
        ** Method "public RelativeTime getInterarrivalTime(RelativeTime
        ** destination)"
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: getInterarrivalTime("+
                               "RelativeTime)");
            o = rt.getInterarrivalTime(rt);
            if( !(o instanceof RelativeTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: getInterarrivalTime("+
                               "RelativeTime) failed");
            Tests.fail("rt.getInterarrivalTime(rt)",e);
        }

        /* Subtest 16:
        ** Method "public RelativeTime getInterarrivalTime(RelativeTime
        ** destination)" where destination is null
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: getInterarrivalTime(null)");
            o = rt.getInterarrivalTime(null);
            if( !(o instanceof RelativeTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: getInterarrivalTime(null) "+
                               "failed");
            Tests.fail("rt.getInterarrivalTime(null)",e);
        }

        /* Subtest 17:
        ** method "public RelativeTime relative(Clock clock)
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: relative(Clock,"+
                               "RelativeTime)");
            Clock clock = Clock.getRealtimeClock();
            o = rt.relative(clock);
            if (! (o instanceof RelativeTime && o instanceof
                   HighResolutionTime))
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: relative(Clock,"+
                               "RelativeTime) failed");
            Tests.fail("rt.relative(clock, drt)",e);
        }

        /* Subtest 18:
        ** method "public RelativeTime relative(Clock clock)
        ** where clock is null
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: relative(null, null)");
            o = rt.relative(null);
            if (! (o instanceof RelativeTime && o instanceof
                   HighResolutionTime))
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: relative(null, null) "+
                               "failed");
            Tests.fail("rt.relative(null, null)",e);
        }

        /* Subtest 19:
        ** method "public RelativeTime relative(Clock clock, RelativeTime
        ** destination)
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: relative(Clock,"+
                               "RelativeTime)");
            RelativeTime dat = new RelativeTime();
            Clock clock = Clock.getRealtimeClock();
            o = rt.relative(clock,dat);
            if (! (o instanceof RelativeTime && o instanceof
                   HighResolutionTime))
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: relative(Clock,"+
                               "RelativeTime) failed");
            Tests.fail("rt.relative(clock, drt)",e);
        }

        /* Subtest 20:
        ** method "public RelativeTime relative(Clock clock, RelativeTime
        ** destination) where clock and destination are both null
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: relative(null, null)");
            o = rt.relative(null, null);
            if (! (o instanceof RelativeTime && o instanceof
                   HighResolutionTime))
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: relative(null, null) "+
                               "failed");
            Tests.fail("rt.relative(null, null)",e);
        }

        /* Subtest 21:
        ** HighResolutionTime's method "public void set(HighResolutionTime
        ** time)
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: set(HighResolutionTime)");
            rt.set(rt);
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: set(HighResolutionTime) "+
                               "failed");
            Tests.fail("rt.set(at)",e);
        }

        /* Subtest 22:
        ** HighResolutionTime's method "public void set(HighResolutionTime
        ** time) where time is null
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: set(HighResolutionTime)");
            RelativeTime nl = null;
            rt.set(nl);
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: set(HighResolutionTime) "+
                               "failed");
            Tests.fail("rt.set(nl)",e);
        }

        /* Subtest 23:
        ** HighResolutionTime's method "public void set(long millis)"
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: set(long)");
            rt.set(100L);
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: set(long) failed");
            Tests.fail("rt.set(100L)",e);
        }

        /* Subtest 24:
        ** HighResolutionTime's method "public void set
        ** (long millis, int nanos)"
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: set(long,int)");
            rt.set(100L, 100);
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: set(long,int) failed");
            Tests.fail("rt.set(100L,100)",e);
        }

        /* Subtest 25:
        ** Method "public final RelativeTime subtract(RelativeTime time)"
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: subtract(RelativeTime)");
            rt = new RelativeTime(1000L,100);
            RelativeTime srt = new RelativeTime(0L,100);
            o = rt.subtract(srt);
            if( !(o instanceof RelativeTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: subtract(RelativeTime) "+
                               "failed");
            Tests.fail("rt.subtract(srt)",e);
        }

        /* Subtest 26:
        ** Method "public RelativeTime subtract(RelativeTime time,
        ** RelativeTime destination)"
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: subtract(RelativeTime,"+
                               "RelativeTime)");
            RelativeTime drt = new RelativeTime();
            rt = new RelativeTime(1000L,100);
            RelativeTime srt = new RelativeTime(0L,200);
            o = rt.subtract(rt, drt);
            if( !(o instanceof RelativeTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: subtract(RelativeTime,"+
                               "RelativeTime) failed");
            Tests.fail("rt.subtract(rt,drt)",e);
        }

        /* Subtest 27:
        ** Method "public RelativeTime subtract(RelativeTime time,
        ** RelativeTime destination)" where destination is null
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: subtract(RelativeTime,"+
                               "null)");
            rt = new RelativeTime(1000L,100);
            RelativeTime srt = new RelativeTime(0L,200);
            o = rt.subtract(rt, null);
            if( !(o instanceof RelativeTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: subtract(RelativeTime,"+
                               "null) failed");
            Tests.fail("rt.subtract(rt,null)",e);
        }

        /* Subtest 28:
        ** method "public java.lang.String toString()"
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: toString()");
            String s;
            s = rt.toString();
            if( s.length() == 0 )
                throw new Exception("String is empty");
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: toString() failed");
            Tests.fail("rt.toString()",e);
        }

        /* Subtest 29:
        ** method "public void waitForObject(Object object,
        **                                   HighResolutionTime time)"
        */
        Tests.increment();
        try {
            System.out.println("RelativeTimeTest: waitForObject(obj, hrt)");
            Object obj = new Object();
            synchronized(obj) {
                RelativeTime time = new RelativeTime(1000,0);
                HighResolutionTime.waitForObject(obj, time);
            }
        } catch (Exception e) {
            System.out.println("RelativeTimeTest: waitForObject(obj.hrt) failed");
            Tests.fail("HRT.waitForObject(obj,hrt)",e);
        }

        Tests.printSubTestReportTotals("RelativeTimeTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
