//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              RealtimeSecurityTest

Subtest 1:
        "public RealtimeSecurity()"

Subtest 2:
        "public void checkAccessPhysical()"

Subtest 3:
        "public void checkAccessPhysicalRange(long base, long size)

Subtest 4:
        "public void checkAccessPhysicalRange(long base, long size)
        ** SecurityException is expected

Subtest 5:
        "public void checkSetFilter()"

Subtest 6:
        "public void checkSetScheduler()"
*/

import javax.realtime.*;

public class RealtimeSecurityTest
{
    public static void run()
    {
        RealtimeSecurity rs = null;
        Object o = null;

        Tests.newTest("RealtimeSecurityTest");

        /* Subtest 1:
        ** Constructor "public RealtimeSecurity()"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeSecurityTest: RealtimeSecurity()");
            rs = new RealtimeSecurity();
            if( !(rs instanceof RealtimeSecurity) )
                throw new Exception("Return object is not instance of "+
                                    "RealtimeSecurity");
        } catch (Exception e) {
            System.out.println("RealtimeSecurityTest: RealtimeSecurity() "+
                               "failed");
            Tests.fail("new RealtimeSecurity()",e);
        }

        /* Subtest 2:
        ** Method "public void checkAccessPhysical()"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeSecurityTest: checkAccessPhysical()");
            rs = new RealtimeSecurity();
            rs.checkAccessPhysical();
        } catch (Exception e) {
            System.out.println("RealtimeSecurityTest: checkAccessPhysical() "+
                               "failed");
            Tests.fail("rt.checkAccessPhysical()",e);
        }

        /* Subtest 3:
        ** Method "public void checkAccessPhysicalRange(long base, long size)
        */
        Tests.increment();
        try {
            System.out.println("RealtimeSecurityTest: checkAccessPhysical"+
                               "Range(long,long)");
            rs = new RealtimeSecurity();
            rs.checkAccessPhysicalRange(100L, 100L);
        } catch (Exception e) {
            System.out.println("RealtimeSecurityTest: checkAccessPhysical"+
                               "Range(long,long) failed");
            Tests.fail("rt.checkAccessPhysicalRange(100L,100L)",e);
        }

        /* Subtest 4:
        ** Method "public void checkAccessPhysicalRange(long base, long size)
        ** SecurityException is expected
        */
        Tests.increment();
        try {
            System.out.println("RealtimeSecurityTest: checkAccessPhysical"+
                               "Range(long,long)");
            rs = new RealtimeSecurity();
            rs.checkAccessPhysicalRange(0,999999999);
            Tests.fail("rs.checkAccessPhysicalRange(0,999999999) did not "+
                       "throw expected SecurityException");
        } catch (Exception e) {
            System.out.println("RealtimeSecurityTest: checkAccessPhysical"+
                               "Range(long,long) threw exception");
            if (! (e instanceof SecurityException))
                Tests.fail("rt.checkAccessPhysicalRange(0,999999999) threw "+
                           "an exception other than SecurityException",e);
            else
                System.out.println("...as expected.");
        }

        /* Subtest 5:
        ** Method "public void checkSetFilter()"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeSecurityTest: checkSetFilter()");
            rs = new RealtimeSecurity();
            rs.checkSetFilter();
        } catch (Exception e) {
            System.out.println("RealtimeSecurityTest: checkSetFilter() "+
                               "failed");
            Tests.fail("rs.checkSetFilter()",e);
        }

        /* Subtest 6:
        ** Method "public void checkSetScheduler()"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeSecurityTest: checkSetScheduler()");
            rs = new RealtimeSecurity();
            rs.checkSetScheduler();
        } catch (Exception e) {
            System.out.println("RealtimeSecurityTest: checkSetScheduler() "+
                               "failed");
            Tests.fail("rs.checkSetScheduler()",e);
        }

        Tests.printSubTestReportTotals("RealtimeSecurityTest");

    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
