//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              ScopedCycleExceptionTest

Subtest 1:
        "public ScopedCycleException()"

Subtest 2:
        "public ScopedCycleException(java.lang.string description)"

Subtest 3
        "public ScopedCycleException(java.lang.String description)" checking
        value of String
*/

import javax.realtime.*;

public class ScopedCycleExceptionTest
{

    public static void run()
    {
        ScopedCycleException mae = null;
        Object o = null;

        Tests.newTest("ScopedCycleExceptionTest");
        Tests.increment();

        /* Subtest 1:
        ** Constructor "public ScopedCycleException()"
        */
        try {
            System.out.println("ScopedCycleExceptionTest: ScopedCycleException()");
            mae = new ScopedCycleException();
            if( !(mae instanceof ScopedCycleException) )
                throw new Exception("Return object is not instanceof "+
                                    "ScopedCycleException");
        } catch (Exception e) {
            System.out.println("ScopedCycleExceptionTest: ScopedCycleException()");
            Tests.fail("new ScopedCycleException()",e);
        }

        /* Subtest 2:
        ** Constructor "public ScopedCycleException(java.lang.string description)"
        */

        Tests.increment();
        try {
            System.out.println("ScopedCycleExceptionTest: "+
                               "ScopedCycleException(String)");
        mae = new ScopedCycleException("ConstructorTest");
        if( !(mae instanceof ScopedCycleException) )
        throw new Exception("Return object is not instanceof "+
                            "ScopedCycleException");
        } catch (Exception e) {
            System.out.println("ScopedCycleExceptionTest: "+
                               "ScopedCycleException(String) failed");
            Tests.fail("new ScopedCycleException(description)",e);
        }

        /* Subtest 3
        ** Constructor "public ScopedCycleException(java.lang.String
        ** description)" checking value of String
        */
        Tests.increment();
        try {
            System.out.println("ScopedCycleExceptionTest: "+
                               "ScopedCycleException(String)");
            String description = "DescriptionTest";
            mae = new ScopedCycleException(description);
            String maedescription;
            maedescription = mae.toString();
            if (maedescription.length() == 0)
                throw new Exception("String description is empty");

        } catch (Exception e) {
            System.out.println("ScopedCycleExceptionTest: "+
                               "ScopedCycleException(String) failed");
            Tests.fail("new ScopedCycleException(description)",e);
        }

        Tests.printSubTestReportTotals("ScopedCycleExceptionTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
