//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                      ClockTest
**
** This test uses a subclass of Clock, RealtimeClock, which is not
** listed in the RTSJ but is provided in JTime.

Subtest 1:
        "public RealtimeClock()"

Subtest 2:
        "public static Clock getRealtimeClock()"

Subtest 3:
        "public abstract RelativeTime getResolution"

Subtest 4:
        "public AbsoluteTime getTime()"

Subtest 5:
        "public abstract void getTime(AbsoluteTime time)"

Subtest 6:
        "public abstract void getTime(AbsoluteTime time)" where time is null

Subtest 7:
        "public abstract void setResolution(RelativeTime resolution)"
*/

import javax.realtime.*;

public class ClockTest
{
    /* This test uses a subclass of Clock, RealtimeClock, which is not
    ** listed in the RTSJ but is provided in JTime.
        */

    public static void run()
    {
        Object o = null;

        Tests.newTest("ClockTest");

        /* Subtest 1:
        **  getRealtimeClock()"
        */
        Tests.increment();
        try {
            System.out.println("ClockTest: getRealtimeClock()");
            o = Clock.getRealtimeClock();
            if (! (o instanceof Clock))
                throw new Exception("Return object not an instanceof "+
                                    "Clock");
        } catch (Exception e) {
            System.out.println("ClockTest: getRealtimeClock() failed");
            Tests.fail("new RealtimeClock()",e);
        }

        /* Subtest 2:
        ** Method "public static Clock getRealtimeClock()"
        */
        Tests.increment();
        try {
            System.out.println("ClockTest: getRealtimeClock()");
            o = Clock.getRealtimeClock();
            if( !(o instanceof Clock) )
                throw new Exception("Return object not an instanceof Clock");
        } catch (Exception e) {
            System.out.println("ClockTest: getRealtimeClock() failed");
            Tests.fail("Clock.getRealtimeClock()",e);
        }

        /* Subtest 3:
        ** Method "public abstract RelativeTime getResolution"
        */
        Tests.increment();
        System.out.println("ClockTest: ");
        try {
            System.out.println("ClockTest: getResolution()");
            Clock rtc = Clock.getRealtimeClock();
            o = rtc.getResolution();
            if (! (o instanceof RelativeTime))
                throw new Exception("Return object not an instanceof "+
                                    "RelativeTime");
        } catch (Exception e) {
            System.out.println("ClockTest: getResolution() failed");
            Tests.fail("rtc.getResolution()",e);
        }

        /* Subtest 4:
        ** Method "public AbsoluteTime getTime()"
        */
        Tests.increment();
        try {
            System.out.println("ClockTest: getTime()");
            Clock rtc = Clock.getRealtimeClock();
            o = rtc.getTime();
            if (!(o instanceof AbsoluteTime))
                throw new Exception("Return object not an instanceof "+
                                    "AbsoluteTIme");
        } catch (Exception e) {
            System.out.println("ClockTest: getTime() failed");
            Tests.fail("rtc.getTime()",e);
        }

        /* Subtest 5:
        ** Method "public abstract void getTime(AbsoluteTime time)"
        */
        Tests.increment();
        try {
            System.out.println("ClockTest: getTime(AbsoluteTime)");
            Clock rtc = Clock.getRealtimeClock();
            AbsoluteTime abt = new AbsoluteTime();
            rtc.getTime(abt);
        } catch (Exception e) {
            System.out.println("ClockTest: getTime(AbsoluteTime) failed");
            Tests.fail("rtc.getTime(abt)",e);
        }

        /* Subtest 6:
        ** Method "public abstract void getTime(AbsoluteTime time)" where
        ** time is null
        */
        Tests.increment();
        try {
            System.out.println("ClockTest: getTime(null)");
            Clock rtc = Clock.getRealtimeClock();
            System.out.println("ClockTest: ");
            AbsoluteTime abt = null;
            rtc.getTime(abt);
        } catch (Exception e) {
            System.out.println("ClockTest: getTime(null) failed");
            Tests.fail("rtc.getTime(null)",e);
        }

        /* Subtest 7:
        ** Method "public abstract void setResolution(RelativeTime resolution)"
        */
        Tests.increment();
        try {
            System.out.println("ClockTest: setResolution(RelativeTime)");
            Clock rtc = Clock.getRealtimeClock();
            RelativeTime rt = new RelativeTime(1L,0);
            rtc.setResolution(rt);
        } catch (Exception e)
            {
                System.out.println("ClockTest: setResolution(RelativeTime) "+
                                   "failed");
                Tests.fail("rtc.setResolution(rt)",e);
            }

        Tests.printSubTestReportTotals("ClockTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
