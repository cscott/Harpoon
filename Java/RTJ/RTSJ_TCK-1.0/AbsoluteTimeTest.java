//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              AbsoluteTimeTest

Subtest 1:
        "public AbsoluteTime()"

Subtest 2:
        "public AbsoluteTime(AbsoluteTime time)"

Subtest 3:
        "public AbsoluteTime(AbsoluteTime time)" where time is null

Subtest 4:
        "public AbsoluteTime(java.util.Date date)"

Subtest 5:
        "public AbsoluteTime add(long millis, int nanos)"

Subtest 6:
        "public AbsoluteTime add(long millis, int nanos, AbsoluteTime destination)"

Subtest 7:
        "public AbsoluteTime add(long millis, int nanos, AbsoluteTime destination)" where destination is null

Subtest 8:
        "public AbsoluteTime add(RelativeTime time)"

Subtest 9:
        "public AbsoluteTime add(RelativeTime time, AbsoluteTime destination)"

Subtest 10:
        "public AbsoluteTime add(RelativeTime time, AbsoluteTime destination)" where destination is null

Subtest 11:
        "public java.util.Date getDate()"

Subtest 12:
        "public final RelativeTime subtract(AbsoluteTime time)"

Subtest 13:
        "public final RelativeTime subtract(AbsoluteTime time, RelativeTime destination"

Subtest 14:
        "public final RelativeTime subtract(AbsoluteTime time, RelativeTime destination" where the destination is null

Subtest 15:
        "public final AbsoluteTime subtract(RelativeTime time)"

Subtest 16:
        "public AbsoluteTime subtract(RelativeTime time, AbsoluteTime destination"

Subtest 17:
        "public AbsoluteTime subtract(RelativeTime time, AbsoluteTime destination" where the destination is null

Subtest 18:
        "public AbsoluteTime(long millis, int nanos)"
where millis and nanos are negative values

Subtest 19:
        "public AbsoluteTime(long millis, int nanos)"

Subtest 20:
        "public AbsoluteTime absolute(Clock clock)" where clock and destination are both null

Subtest 21:
        "public AbsoluteTime absolute(Clock clock)"

Subtest 22:
        "public AbsoluteTime absolute(Clock clock, AbsoluteTime destination)" where clock and destination are both null

Subtest 23:
        "public AbsoluteTime absolute(Clock clock, AbsoluteTime destination)"

Subtest 24:
        "public int compareTo(HighResolutionTime time)"

Subtest 25:
        "public int compareTo(java.lang.Object object)"

Subtest 26:
        "public boolean equals(HighResolutionTime time)"

Subtest 27:
        "public boolean equals(java.lang.Object object)"

Subtest 28:
        "public final long getMilliseconds()

Subtest 29:
        "public final int getNanoseconds()"

Subtest 30:
        "public int hashCode()"

Subtest 31:
        "public RelativeTime relative(Clock clock)" where clock and destination are both null

Subtest 32:
        "public RelativeTime relative(Clock clock)"

Subtest 33:
        "public RelativeTime relative(Clock clock, RelativeTime destination)" where clock and destination are both null

Subtest 34:
        "public RelativeTime relative(Clock clock, RelativeTime destination)"

Subtest 35:
        "public void set(java.util.Date date)"

Subtest 36:
        "public void set(HighResolutionTime time)

Subtest 37:
        "public void set(HighResolutionTime time) where time is null

Subtest 38:
        "public void set(long millis)"

Subtest 39:
        "public void set(long millis, int nanos)"

Subtest 40:
        "public java.lang.String toString()"

Subtest 41:
        "public static void waitForObject(Object target,
                                          HighResolutionTime time)"
*/

import javax.realtime.*;
import java.util.*;

public class AbsoluteTimeTest {

    public static void run() {
        AbsoluteTime at = null;
        Object o = null;

        Tests.newTest("AbsoluteTimeTest (abstract HighResolutionTime)");
        Tests.increment();

        /* Subtest 1:
        ** Constructor "public AbsoluteTime()"
        */

        try {
            System.out.println("AbsoluteTimeTest: AbsoluteTime()");
            at = new AbsoluteTime();
            if( !(at instanceof AbsoluteTime && at instanceof
                  HighResolutionTime) )
                throw new Exception("Return object not an instance of "+
                                    "AbsoluteTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: AbsoluteTime() failed");
            Tests.fail("new AbsoluteTime()",e);
        }

        /* Subtest 2:
        ** Constructor "public AbsoluteTime(AbsoluteTime time)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: AbsoluteTime(AbsoluteTime)");
            at = new AbsoluteTime(new AbsoluteTime());
            if( !(at instanceof AbsoluteTime && at instanceof
                  HighResolutionTime) )
                throw new Exception("Return object not an instance of "+
                                    "AbsoluteTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: AbsoluteTime(AbsoluteTime) "+
                               "failed");
            Tests.fail("new AbsoluteTime(new AbsoluteTime())",e);
        }

        /* Subtest 3:
        ** Constructor "public AbsoluteTime(AbsoluteTime time)" where
        ** time is null
        ** IllegalArgumentException expected
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: AbsoluteTime(null)");
            AbsoluteTime nl = null;
            at = new AbsoluteTime(nl);
            Tests.fail("new AbsoluteTime(null) did not throw exception -");
        } catch (Exception e) {

            if (! (e instanceof IllegalArgumentException)) {
                System.out.println("AbsoluteTimeTest: AbsoluteTime(null)"+
                                   "failed");
                Tests.fail("new AbsoluteTime(null) threw exception other than "+
                           "IllegalArgumentException",e);
            }
        }

        /* Subtest 4:
        ** Constructor "public AbsoluteTime(java.util.Date date)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: AbsoluteTime(Date)");
            at = new AbsoluteTime(new java.util.Date());
            if( !(at instanceof AbsoluteTime && at instanceof
                  HighResolutionTime) )
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: Constructor "+
                               "AbsoluteTime(Date) failed");
            Tests.fail("new AbsoluteTimeTest(new java.util.Date())",e);
        }

        /* Subtest 5:
        ** Method "public AbsoluteTime add(long millis, int nanos)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: add(long,int)");
            long millis = 10223;
            int nanos = 100;
            o = at.add(millis, nanos);
            if( !(o instanceof AbsoluteTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: add(long,int) failed");
            Tests.fail("at.add(millis,nanos)",e);
        }

        /* Subtest 6:
        ** Method "public AbsoluteTime add(long millis, int nanos,
        ** AbsoluteTime destination)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: add(long,int,AbsoluteTime)");
            long millis = 10223;
            int nanos = 100;
            o = at.add(millis, nanos, at);
            if( !(o instanceof AbsoluteTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: add(long,int,AbsoluteTime) "+
                               "failed");
            Tests.fail("at.add(millos,nanos,at)",e);
        }

        /* Subtest 7:
        ** Method "public AbsoluteTime add(long millis, int nanos,
        ** AbsoluteTime destination)" where destination is null
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: add(long,int,"+
                               "AbsoluteTime=null)");
            long millis = 10223;
            int nanos = 100;
            o = at.add(millis, nanos, null);
            if( !(o instanceof AbsoluteTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: add(long,int,"+
                               "AbsoluteTime=null) failed");
            Tests.fail("at.add(millos,nanos,null)",e);
        }

        /* Subtest 8:
        ** Method "public AbsoluteTime add(RelativeTime time)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: add(RelativeTime)");
            RelativeTime time = new RelativeTime();
            o = at.add(time);
            if( !(o instanceof AbsoluteTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: add(RelativeTime) failed");
            Tests.fail("at.add(reltime)",e);
        }

        /* Subtest 9:
        ** Method "public AbsoluteTime add(RelativeTime time,
        ** AbsoluteTime destination)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: add(RelativeTime,"+
                               "AbsoluteTime)");
            RelativeTime time = new RelativeTime();
            o = at.add(time,at);
            if( !(o instanceof AbsoluteTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: add(RelativeTime,"+
                               "AbsoluteTime) failed");
            Tests.fail("at.add(reltime, dest)",e);
        }

        /* Subtest 10:
        ** Method "public AbsoluteTime add(RelativeTime time,
        ** AbsoluteTime destination)" where destination is null
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: add(RleativeTime, "+
                               "AbsoluteTime=null)");
            RelativeTime time = new RelativeTime();
            o = at.add(time,null);
            if( !(o instanceof AbsoluteTime && o instanceof
                  HighResolutionTime) )
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: add(RleativeTime, "+
                               "AbsoluteTime=null) failed");
            Tests.fail("at.add(reltime, dest)",e);
        }

        /* Subtest 11:
        ** Method "public java.util.Date getDate()"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: getDate()");
            o = at.getDate();
            if( !(o instanceof Date) )
                throw new Exception("Return object not an instanceof Date");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: getDate failed");
            Tests.fail("at.getDate()",e);
        }

        /* Subtest 12:
0       ** Method "public final RelativeTime subtract(AbsoluteTime time)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: subtract(AbsoluteTime)");
            o = at.subtract(at);
            if( !(o instanceof RelativeTime) )
                throw new Exception("Return object not an instanceof "+
                                    "RelativeTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: subtract(AbsoluteTime) "+
                               "failed");
            Tests.fail("at.subtract(at)",e);
        }

        /* Subtest 13:
        ** Method "public final RelativeTime subtract(AbsoluteTime time,
        ** RelativeTime destination)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: subtract(AbsoluteTime,"+
                               "RelativeTime)");
            RelativeTime time = new RelativeTime();
            o = at.subtract(at, time);
            if( !(o instanceof RelativeTime) )
                throw new Exception("Return object not an instanceof "+
                                    "RelativeTime");
            if( time==null)
                throw new Exception("Destination is null");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: subtract(AbsoluteTime,"+
                               "RelativeTime) failed");
            Tests.fail("at.subtract(at,null)",e);
        }

        /* Subtest 14:
        ** Method "public final RelativeTime subtract(AbsoluteTime time,
        ** RelativeTime destination" where the destination is null
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: subtract(AbsoluteTime,"+
                               "RelativeTime=null)");
            o = at.subtract(at, null);
            if( !(o instanceof RelativeTime) )
                throw new Exception("Return object not an instanceof "+
                                    "RelativeTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: subtract(AbsoluteTime,"+
                               "RelativeTime=null) failed");
            Tests.fail("at.subtract(at,null)",e);
        }

        /* Subtest 15:
        ** Method "public final AbsoluteTime subtract(RelativeTime time)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: subtract(RelativeTime)");
            RelativeTime time = new RelativeTime();
            o = at.subtract(time);
            if( !(o instanceof AbsoluteTime) )
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: subtract(RelativeTime)"+
                               "failed");
            Tests.fail("at.subtract(time)",e);
        }

        /* Subtest 16:
        ** Method "public AbsoluteTime subtract(RelativeTime time,
        ** AbsoluteTime destination"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: subtract(RelativeTime,"+
                               "AbsoluteTime)");
            RelativeTime time = new RelativeTime();
            System.out.println("AbsoluteTimeTest: ");
            o = at.subtract(time, at);
            if( !(o instanceof AbsoluteTime) )
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTime");
            if (at==null)
                throw new Exception("Destination is null");

        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: subtract(RelativeTime,"+
                               "AbsoluteTime) failed");
            Tests.fail("at.subtract(time,null)",e);
        }

        /* Subtest 17:
        ** Method "public AbsoluteTime subtract(RelativeTime time,
        ** AbsoluteTime destination" where the destination is null
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: subtract(RelativeTime,"+
                               "AbsoluteTime)");
            RelativeTime time = new RelativeTime();
            o = at.subtract(time, null);
            if( !(o instanceof AbsoluteTime) )
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: subtract(RelativeTime,"+
                               "AbsoluteTime) failed");
            Tests.fail("at.subtract(time,null)",e);
        }

        /* Subtest 18:
        ** Constructor "public AbsoluteTime(long millis, int nanos)"
        ** where millis and nanos are negative values
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: AbsoluteTime(-long, -int)");
            long millis = -10223;
            int nanos = -100;
            at = new AbsoluteTime(millis, nanos);
            if( !(at instanceof AbsoluteTime && at instanceof
                  HighResolutionTime) )
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: AbsoluteTime(-long, -int) "+
                               "failed");
            Tests.fail("new AbsoluteTime(millis, nanos)",e);
        }

        /* Subtest 19:
        ** Constructor "public AbsoluteTime(long millis, int nanos)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: AbsoluteTime(long,int)");
            long millis = 10223;
            int nanos = 100;
            at = new AbsoluteTime(millis, nanos);
            if( !(at instanceof AbsoluteTime && at instanceof
                  HighResolutionTime) )
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTime nor HighResolutionTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: AbsoluteTime(long,int)");
            Tests.fail("new AbsoluteTime(millis, nanos)",e);
        }


        /* Subtest 20:
        ** Method "public AbsoluteTime absolute(Clock clock)"
        ** where clock is null
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: absolute(Clock,"+
                               "AbsoluteTime)");
            Clock clock = null;
            o = at.absolute(clock);
            if( !(o instanceof AbsoluteTime) )
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: AbsoluteTime(Clock,"+
                               "AbsoluteTime) failed");
            Tests.fail("at.absolute(clock, dest)",e);
        }

        /* Subtest 21:
        ** Method "public AbsoluteTime absolute(Clock clock)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: absolute(Clock, "+
                               "AbsoluteTime)");
            Clock clock = Clock.getRealtimeClock();
            o = at.absolute(clock);
            if( !(o instanceof AbsoluteTime) )
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: AbsoluteTime(Clock, "+
                               "AbsoluteTime)");
            Tests.fail("at.absolute(clock, dest)",e);
        }

        /* Subtest 22:
        ** Method "public AbsoluteTime absolute(Clock clock,
        ** AbsoluteTime destination)" where clock and destination are both null
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: absolute(Clock,"+
                               "AbsoluteTime)");
            Clock clock = null;
            AbsoluteTime dest = null;
            o = at.absolute(clock, dest);
            if( !(o instanceof AbsoluteTime) )
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: AbsoluteTime(Clock,"+
                               "AbsoluteTime) failed");
            Tests.fail("at.absolute(clock, dest)",e);
        }

        /* Subtest 23:
        ** Method "public AbsoluteTime absolute(Clock clock,
        ** AbsoluteTime destination)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: absolute(Clock, "+
                               "AbsoluteTime)");
            Clock clock = Clock.getRealtimeClock();
            AbsoluteTime dest = new AbsoluteTime();
            o = at.absolute(clock, dest);
            if( !(o instanceof AbsoluteTime) )
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: AbsoluteTime(Clock, "+
                               "AbsoluteTime)");
            Tests.fail("at.absolute(clock, dest)",e);
        }

        /* Subtest 24:
        ** HighResolutionTime's method "public int compareTo(HighResolutionTime
        ** time)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: compareTo("+
                               "HighResolutionTime)");
            int x = at.compareTo(at);
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: compareTo("+
                               "HighResolutionTime) failed");
            Tests.fail("at.compareTo(at)",e);
        }

        /* Subtest 25:
        ** HighResolutionTime's method "public int
        ** compareTo(java.lang.Object object)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: compareTo(object)");
            int x = at.compareTo(new Object());
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: compareTo(object) failed");
            Tests.fail("at.compareTo(new Object()",e);
        }

        /* Subtest 26:
        ** HighResolutionTime's method: "public boolean equals(
        ** HighResolutionTime time)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: equals(HighResolutionTime)");
            boolean x = at.equals(at);
            if( !x )
                throw new Exception("AbsoluteTime not found to be equal to "+
                                    "itself");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: equals(HighResolutionTime) "+
                               "failed");
            Tests.fail("at.equals(at)",e);
        }

        /* Subtest 27:
        ** HighResolutionTime's method: "public boolean equals(
        ** java.lang.Object object)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: equals(Object)");
            boolean x = at.equals(new Object());
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: equals(Object) failed");
            Tests.fail("at.equals(new Object())",e);
        }

        /* Subtest 28:
        ** HighResolutionTime's method: "public final long getMilliseconds()
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: getMilliseconds()");
            long x = at.getMilliseconds();
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: getMilliseconds() failed");
            Tests.fail("at.getMilliseconds()",e);
        }

        /* Subtest 29:
        ** HighResolutionTime's method: "public final int getNanoseconds()"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: getNanoseconds()");
            long x = at.getNanoseconds();
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: getNanoseconds() failed");
            Tests.fail("at.getNanoseconds()",e);
        }

        /* Subtest 30:
        ** HighResolutionTime's method: "public int hashCode()"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: hashCode()");
            int x = at.hashCode();
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: hashCode() failed");
            Tests.fail("at.hashCode()",e);
        }

        /* Subtest 31:
        ** Method "public RelativeTime relative(Clock clock)"
        ** where clock is null
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: relative(Clock)");
            Clock clock = null;
            o = at.relative(clock);
            if( !(o instanceof RelativeTime) )
                throw new Exception("Return object not an instanceof "+
                                    "RelativeTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: RelativeTime(Clock,"+
                               "RelativeTime) failed");
            Tests.fail("at.relative(clock, dest)",e);
        }

        /* Subtest 32:
        ** Method "public RelativeTime relative(Clock clock)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: relative(Clock)");
            Clock clock = Clock.getRealtimeClock();
            o = at.relative(clock);
            if( !(o instanceof RelativeTime) )
                throw new Exception("Return object not an instanceof "+
                                    "RelativeTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: RelativeTime(Clock, "+
                               "RelativeTime)");
            Tests.fail("at.relative(clock, dest)",e);
        }

        /* Subtest 33:
        ** Method "public RelativeTime relative(Clock clock,
        ** RelativeTime destination)" where clock and destination are both null
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: relative(Clock,"+
                               "RelativeTime)");
            Clock clock = null;
            RelativeTime dest = null;
            o = at.relative(clock, dest);
            if( !(o instanceof RelativeTime) )
                throw new Exception("Return object not an instanceof "+
                                    "RelativeTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: RelativeTime(Clock,"+
                               "RelativeTime) failed");
            Tests.fail("at.relative(clock, dest)",e);
        }

        /* Subtest 34:
        ** Method "public RelativeTime relative(Clock clock,
        ** RelativeTime destination)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: relative(Clock, "+
                               "RelativeTime)");
            Clock clock = Clock.getRealtimeClock();
            RelativeTime dest = new RelativeTime();
            o = at.relative(clock, dest);
            if( !(o instanceof RelativeTime) )
                throw new Exception("Return object not an instanceof "+
                                    "RelativeTime");
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: RelativeTime(Clock, "+
                               "RelativeTime)");
            Tests.fail("at.relative(clock, dest)",e);
        }

        /* Subtest 35:
        ** method "public void set(java.util.Date date)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: set(Date)");
            at.set(new java.util.Date());
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: set(Date) failed");
            Tests.fail("at.set(date)",e);
        }

        /* Subtest 36:
        ** HighResolutionTime's method "public void set(HighResolutionTime
        ** time)
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: set(HighResolutionTime)");
            at.set(at);
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: set(HighResolutionTime) "+
                               "failed");
            Tests.fail("at.set(at)",e);
        }

        /* Subtest 37:
        ** HighResolutionTime's method "public void set(HighResolutionTime
        ** time) where time is null
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: set(HighResolutionTime)");
            AbsoluteTime nl = null;
            at.set(nl);
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: set(HighResolutionTime) "+
                               "failed");
            Tests.fail("at.set(nl)",e);
        }

        /* Subtest 38:
        ** HighResolutionTime's method "public void set(long millis)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: set(long)");
            at.set(100L);
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: set(long) failed");
            Tests.fail("at.set(100L)",e);
        }

        /* Subtest 39:
        ** HighResolutionTime's method "public void set
        ** (long millis, int nanos)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: set(long,int)");
            at.set(100L, 100);
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: set(long,int) failed");
            Tests.fail("at.set(100L,100)",e);
        }

        /* Subtest 40:
        ** method "public java.lang.String toString()"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: toString()");
            String s;
            s = at.toString();
            if (s.length() == 0)
                {
                    throw new Exception("String is empty");
                }
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: toString() failed");
            Tests.fail("at.toString()",e);
        }

        /* Subtest 41:
        ** method "public void waitForObject(Object object,
        **                                   HighResolutionTime time)"
        */
        Tests.increment();
        try {
            System.out.println("AbsoluteTimeTest: waitForObject(obj, hrt)");
            Object obj = new Object();
            synchronized(obj) {
                AbsoluteTime time = new RelativeTime(1000,0).absolute(null);
                HighResolutionTime.waitForObject(obj, time);
            }
        } catch (Exception e) {
            System.out.println("AbsoluteTimeTest: waitForObject(obj.hrt) failed");
            Tests.fail("HRT.waitForObject(obj,hrt)",e);
        }

        Tests.printSubTestReportTotals("AbsoluteTimeTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
