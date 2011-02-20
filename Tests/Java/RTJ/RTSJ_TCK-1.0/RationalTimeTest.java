//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              RationalTimeTest

Subtest 1:
        "public RationalTime(int frequency)"

Subtest 2:
        "public RationalTime(int frequency, long millis, int nanos)"

Subtest 3:
        "public RationalTime(int frequency, RelativeTime interval)"

Subtest 4
        "public AbsoluteTime absolute(Clock clock, AbsoluteTime destination)"

Subtest 5
        "public AbsoluteTime absolute(Clock clock, AbsoluteTime destination)"
        where destination is null

Subtest 6
        "public void addInterarrivalTo(AbsoluteTime destination)

Subtest 7:
        "public void setFrequency(int frequency)"
        "public int getFrequency()"

Subtest 8:
        "public RelativeTime getInterarrivalTime()"

Subtest 9:
        "public RelativeTime getInterarrivalTime(RelativeTime dest)"

Subtest 10:
        "public RelativeTime getInterarrivalTime(RelativeTime dest)" where
        dest is null

Subtest 11:
        "public void set(long millis, int nanos)"

Subtest 12:
        "public void set(long millis, int nanos)" where millis is negative
                ** IllegalArgumentException is expected

Subtest 13:
        "public void set(long millis, int nanos)" where nanos is negative
                ** IllegalArgumentException is expected

Subtest 14:
        "public void set(long millis, int nanos)" where both millis and nanos
        are 0
                ** IllegalArgumentException is expected

Subtest 15:
        "public void setFrequency( int frequency )"

Subtest 16:
        "public void setFrequency( int frequency )" where frequency is negative
                ** ArithmeticException is expected
*/


import javax.realtime.*;
import java.util.*;

public class RationalTimeTest
{
    public static void run()
    {
        RationalTime rt = null;
        Object o = null;
        Tests.newTest("RationalTimeTest");

        /* Subtest 1:
        ** Constructor "public RationalTime(int frequency)"
        */
        Tests.increment();
        try {
            System.out.println("RationalTimeTest: RationalTime(int)");
            int freq = 10;
            rt = new RationalTime(freq);
            if( !(rt instanceof RationalTime && rt instanceof RelativeTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RationalTime nor RelativeTime");
        } catch (Exception e) {
            System.out.println("RationalTimeTest: RationalTime(int)");
            Tests.fail("new RationalTime(freq)",e);
        }

        /* Subtest 2:
        ** Constructor "public RationalTime(int frequency, long millis,
        ** int nanos)"
        */
        Tests.increment();
        try {
            System.out.println("RationalTimeTest: RationalTime(int,long,int)");
            long millis = 1;
            int nanos = 1;
            int freq = 10;
            rt = new RationalTime(freq, millis, nanos);
            if( !(rt instanceof RationalTime && rt instanceof RelativeTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RationalTime nor RelativeTime");
        } catch (Exception e) {
            System.out.println("RationalTimeTest: RationalTime(int,long,int) "+
                               "failed");
            Tests.fail("new RationalTime(freq,millis,nanos)",e);
        }

        /* Subtest 3:
        ** Constructor "public RationalTime(int frequency, RelativeTime
        ** interval)"
        */
        Tests.increment();
        try {
            System.out.println("RationalTimeTest: RationalTime(int,"+
                               "RelativeTime)");
            int freq = 10;
            RelativeTime interval = new RelativeTime(100L,0);
            rt = new RationalTime(freq, interval);
            if( !(rt instanceof RationalTime && rt instanceof RelativeTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RationalTime nor RelativeTime");
        } catch (Exception e) {
            System.out.println("RationalTimeTest: RationalTime(int,"+
                               "RelativeTime) failed");
            Tests.fail("new RationalTime(freq, interval)",e);
        }

        /* Subtest 4
        ** Method "public AbsoluteTime absolute(Clock clock, AbsoluteTime
        ** destination)"
        */
        Tests.increment();
        try {
            System.out.println("RationalTimeTest: absolute(Clock,Absolute"+
                               "Time)");
            Clock clock = Clock.getRealtimeClock();
            AbsoluteTime at = new AbsoluteTime();
            o = rt.absolute(clock, at);
            if( !(o instanceof AbsoluteTime) )
                throw new Exception("Return object is not instanceof "+
                                    "AbsoluteTime");
        } catch (Exception e) {
            System.out.println("RationalTimeTest: absolute(Clock,Absolute"+
                               "Time) failed");
            Tests.fail("rt.absolute(clock, at)",e);
        }

        /* Subtest 5
        ** Method "public AbsoluteTime absolute(Clock clock, AbsoluteTime
        ** destination)" where destination is null
        */
        Tests.increment();
        try {
            System.out.println("RationalTimeTest: absolute(Clock,null)");
            Clock clock = Clock.getRealtimeClock();
            o = rt.absolute(clock, null);
            if( !(o instanceof AbsoluteTime) )
                throw new Exception("Return object is not instanceof "+
                                    "AbsoluteTime");
        } catch (Exception e) {
            System.out.println("RationalTimeTest: absolute(Clock,null) "+
                               "failed");
            Tests.fail("rt.absolute(clock, null)",e);
        }

        /* Subtest 6
        ** Method "public void addInterarrivalTo(AbsoluteTime destination)
        */
        Tests.increment();
            System.out.println("RationalTimeTest: addInterarrivalTo("+
                               "AbsoluteTime)");
        try {
            System.out.println("RationalTimeTest: ");
            AbsoluteTime dest = new AbsoluteTime();
            rt.addInterarrivalTo(dest);
        } catch (Exception e) {
            System.out.println("RationalTimeTest: addInterarrivalTo("+
                               "AbsoluteTime) failed");
            Tests.fail("rt.addInterarrivalTo(dest)",e);
        }

        /* Subtest 7:
        ** Method "public void setFrequency(int frequency)" and
        ** Method "public int getFrequency()"
        */
        Tests.increment();
        try {
            System.out.println("RationalTimeTest: setFrequency(int),"+
                               "getFrequency()");
            int f = 20;
            rt.setFrequency(f);
            int x = rt.getFrequency();
            if (x != f)
                throw new Exception("Unexpected frequency");
        } catch (Exception e) {
            System.out.println("RationalTimeTest: setFrequency(int),"+
                               "getFrequency()");
            Tests.fail("rt.setFrequency/rt.getFrequency",e);
        }

        /* Subtest 8:
        ** Method "public RelativeTime getInterarrivalTime()"
        */
        Tests.increment();
        try {
            System.out.println("RationalTimeTest: getInterarrivalTime("+
                               "RelativeTime)");
            o = rt.getInterarrivalTime();
            if (! (o instanceof RelativeTime))
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime");
        } catch (Exception e) {
            System.out.println("RationalTimeTest: getInterarrivalTime()"+
                               "failed");
            Tests.fail("rt.getInterarrivalTime()",e);
        }

        /* Subtest 9:
        ** Method "public RelativeTime getInterarrivalTime(RelativeTime dest)"
        */
        Tests.increment();
        try {
            System.out.println("RationalTimeTest: getInterarrivalTime("+
                               "RelativeTime)");
            RelativeTime dest = new RelativeTime();
            o = rt.getInterarrivalTime(dest);
            if (! (o instanceof RelativeTime))
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime");
        } catch (Exception e) {
            System.out.println("RationalTimeTest: getInterarrivalTime("+
                               "RelativeTime) failed");
            Tests.fail("rt.getInterarrivalTime(dest)",e);
        }

        /* Subtest 10:
        ** Method "public RelativeTime getInterarrivalTime(RelativeTime dest)"
        ** where dest is null
        */
        Tests.increment();
        try {
            System.out.println("RationalTimeTest: getInterarrivalTime("+
                               "RelativeTime)");
            o = rt.getInterarrivalTime(null);
            if (! (o instanceof RelativeTime))
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime");
        } catch (Exception e) {
            System.out.println("RationalTimeTest: getInterarrivalTime("+
                               "RelativeTime) failed");
            Tests.fail("rt.getInterarrivalTime(null)",e);
        }

        /* Subtest 11:
        ** Method "public void set(long millis, int nanos)"
        */
        Tests.increment();
        try {
            System.out.println("RationalTimeTest: set(long,int)");
            long millis = 100;
            int nanos = 100;
            rt.set(millis, nanos);
        } catch (Exception e) {
            System.out.println("RationalTimeTest: set(long,int) failed");
            Tests.fail("rt.set(millis,nanos",e);
        }

        /* Subtest 12:
        ** Method "public void set(long millis, int nanos)" where millis is
        ** negative
        ** IllegalArgumentException is expected
        */
        Tests.increment();
        try {
            System.out.println("RationalTimeTest: set(long,int)");
            long millis = -10;
            int nanos = 100;
            rt.set(millis, nanos);
            Tests.fail("rt.set(-10,100) did not throw expected exception");
        } catch (Exception e) {
            System.out.println("RationalTimeTest: set(long,int) "+
                               "threw exception");
            if (! (e instanceof IllegalArgumentException))
                Tests.fail("rt.set(-10,100) threw an exception other "+
                           "than IllegalArgumentException",e);
            else
                System.out.println("...as expected");
        }

        /* Subtest 13:
        ** Method "public void set(long millis, int nanos)" where nanos is
        ** negative
        ** IllegalArgumentException is expected
        */
        Tests.increment();
        try {
            System.out.println("RationalTimeTest: set(long,int)");
            long millis = 100;
            int nanos = -100;
            rt.set(millis, nanos);
            Tests.fail("rt.set(100,-100) did not throw expected exception");
        } catch (Exception e) {
            System.out.println("RationalTimeTest: set(long,int) "+
                               "threw exception");
            if (! (e instanceof IllegalArgumentException))
                Tests.fail("rt.set(100,-100) threw an exception other "+
                           "than IllegalArgumentException",e);
            else
                System.out.println("...as expected");           
        }

        /* Subtest 14:
        ** Method "public void set(long millis, int nanos)" where both millis
        ** and nanos are 0
        ** IllegalArgumentException is expected
        */
        Tests.increment();
        try {
            System.out.println("RationalTimeTest: set(long,int)");
            long millis = 0;
            int nanos = 0;
            rt.set(millis, nanos);
            Tests.fail("rt.set(0,0) did not throw expected exception");
        } catch (Exception e) {
            System.out.println("RationalTimeTest: set(long,int) " +
                               "threw exception.");
            if (! (e instanceof IllegalArgumentException))
                Tests.fail("rt.set(0,0) threw an exception other than "+
                           "IllegalArgumentException",e);
            else
                System.out.println("...as expected");
        }

        /* Subtest 15:
        ** Method "public void setFrequency( int frequency )"
        */
        Tests.increment();
        try {
            System.out.println("RationalTimeTest: setFrequency(int)");
            rt.setFrequency(10);
        } catch (Exception e) {
            System.out.println("RationalTimeTest: setFrequency(int) failed");
            Tests.fail("setFrequency(10)",e);
        }

        /* Subtest 16:
        ** Method "public void setFrequency( int frequency )" where frequency
        ** is negative
        ** ArithmeticException is expected
        */
        Tests.increment();
        try {
            System.out.println("RationalTimeTest: setFrequency(int)");
            rt.setFrequency(-10);
            Tests.fail("rt.setFrequency(-10) did not throw expected "+
                       "exception");
        } catch (Exception e) {
            System.out.println("RationalTimeTest: setFrequency(int)" +
                               " threw exception");
            if (! (e instanceof ArithmeticException))
                Tests.fail("setFrequency(-10) threw an exception other "+
                           "than ArithmeticException",e);
            else
                System.out.println("...as expected.");
        }

        Tests.printSubTestReportTotals("RationalTimeTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
