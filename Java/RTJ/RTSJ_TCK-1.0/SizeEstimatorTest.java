//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              SizeEstimatorTest

Subtest 1:
        Constructor "public SizeEstimator()"

Subtest 2:
        "public reserve(Class c, int n)"

Subtest 3:
        "public reserve(Class c=null, int n)"

Subtest 4:
        "public reserve(Class c=null, -1)"

Subtest 5:
        "public reserve(SizeEstimator s)"

Subtest 6:
        "public reserve(SizeEstimator s, int n)"

Subtest 7:
        "public reserve(SizeEstimator s, int n)"

*/

import javax.realtime.*;

public class SizeEstimatorTest
{

    public static void run()
    {
        Object o = null;

        /* Subtest 1:
        ** Constructor "public SizeEstimator()"
        */
        Tests.newTest("SizeEstimatorTest");
        Tests.increment();
        try {
            System.out.println("SizeEstimatorTest: SizeEstimator()");
            SizeEstimator size = new SizeEstimator();
            if( !(size instanceof SizeEstimator))
                throw new Exception("Return object is not instanceof "+
                                    "SizeEstimator");
        } catch (Exception e) {
            System.out.println("SizeEstimatorTest: SizeEstimator() failed");
            Tests.fail("new SizeEstimator()",
                       e);
        }

        /* Subtest 2:
        ** "public void reserve(Class c, int n)"
        */
        Tests.increment();
        try {
            System.out.println("SizeEstimatorTest: reserve(Class, int)");
            SizeEstimator size = new SizeEstimator();
            size.reserve(Class.forName("java.lang.Object"), 1000);
        } catch (Exception e) {
            System.out.println("SizeEstimatorTest: reserve(Class, int) failed");
            Tests.fail("new SizeEstimator(Class, int)",
                       e);
        }

        /* Subtest 3:
        ** "public void reserve(Class c, int n=-1)"
        */
        Tests.increment();
        try {
            System.out.println("SizeEstimatorTest: reserve(Class, -1)");
            SizeEstimator size = new SizeEstimator();
            size.reserve(Class.forName("java.lang.Object"), -1);
        } catch (Exception e) {
            System.out.println("SizeEstimatorTest: reserve(Class, int) failed");
            Tests.fail("new SizeEstimator(Class, int)",
                       e);
        }

        /* Subtest 4:
        ** "public void reserve(Class c=null, int n)"
        */
        Tests.increment();
        try {
            System.out.println("SizeEstimatorTest: reserve(Class, int)");
            SizeEstimator size = new SizeEstimator();
            size.reserve((Class) null, 1000);
        } catch (Exception e) {
            System.out.println("SizeEstimatorTest: reserve(Class, int) failed");
            Tests.fail("new SizeEstimator(Class, int)",
                       e);
        }

        /* Subtest 5:
        ** "public void reserve(SizeEstimator size)"
        */
        Tests.increment();
        try {
            System.out.println("SizeEstimatorTest: reserve(SE)");
            SizeEstimator base = new SizeEstimator();
            SizeEstimator size = new SizeEstimator();
            base.reserve(Class.forName("java.lang.Object"), 1000);
            size.reserve(base);
        } catch (Exception e) {
            System.out.println("SizeEstimatorTest: reserve(SizeEstimator) failed");
            Tests.fail("new SizeEstimator(SizeEstimator)",
                       e);
        }

        /* Subtest 6:
        ** "public void reserve(SizeEstimator size, int n)"
        */
        Tests.increment();
        try {
            System.out.println("SizeEstimatorTest: reserve(SE, int)");
            SizeEstimator base = new SizeEstimator();
            SizeEstimator size = new SizeEstimator();
            base.reserve(Class.forName("java.lang.Object"), 1000);
            size.reserve(base, 10);
        } catch (Exception e) {
            System.out.println("SizeEstimatorTest: reserve(SE, int) failed");
            Tests.fail("new SizeEstimator(SizeEstimator, int)",
                       e);
        }

        /* Subtest 7:
        ** "public void reserve(SizeEstimator size=null, int n)"
        */
        Tests.increment();
        try {
            System.out.println("SizeEstimatorTest: reserve(SE, int)");
            SizeEstimator size = new SizeEstimator();
            size.reserve((SizeEstimator) null, 10);
        } catch (Exception e) {
            System.out.println("SizeEstimatorTest: reserve(SE, int) failed");
            Tests.fail("new SizeEstimator(SizeEstimator, int)",
                       e);
        }

        Tests.printSubTestReportTotals("SizeEstimatorTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
