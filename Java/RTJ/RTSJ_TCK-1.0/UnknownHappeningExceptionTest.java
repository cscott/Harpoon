//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                      UnknownHappeningExceptionTest

Subtest 1:
        "public UnknownHappeningException()"

Subtest 2:
        "public UnknownHappeningException(java.lang.String description)"

Subtest 3:
        "public UnknownHappeningException(java.lang.String description)" checking
        value of String
*/

import javax.realtime.*;

public class UnknownHappeningExceptionTest
{

    public static void run()
    {
        UnknownHappeningException iae = null;
        Object o = null;

        Tests.newTest("UnknownHappeningExceptionTest");
        Tests.increment();

        /* Subtest 1:
        ** Constructor "public UnknownHappeningException()"
        */
        try {
            System.out.println("UnknownHappeningExceptionTest: IllegalAssignment"+
                               "Error()");
            iae = new UnknownHappeningException();
            if( !(iae instanceof UnknownHappeningException) )
                throw new Exception("Return object is not instanceof "+
                                    "UnknownHappeningException");
        } catch (Exception e) {
            System.out.println("UnknownHappeningExceptionTest: IllegalAssignment"+
                               "Error() failed");
            Tests.fail("new UnknownHappeningException()",e);
        }

        /* Subtest 2:
        ** Constructor "public UnknownHappeningException(java.lang.String
        ** description)"
        */
        Tests.increment();
        try {
            System.out.println("UnknownHappeningExceptionTest: IllegalAssignment"+
                               "Error(String)");
            iae = new UnknownHappeningException("ConstructorTest");
            if( !(iae instanceof UnknownHappeningException) )
                throw new Exception("Return object is not instanceof "+
                                    "UnknownHappeningException");
        } catch (Exception e) {
            System.out.println("UnknownHappeningExceptionTest: IllegalAssignment"+
                               "Error(String) failed");
            Tests.fail("new UnknownHappeningExceptionTest(description)",e);
        }

        /* Subtest 3:
        ** Constructor "public UnknownHappeningException(java.lang.String
        ** description)" checking value of String
        */

        Tests.increment();
        try {
            System.out.println("UnknownHappeningExceptionTest: IllegalAssignment"+
                               "Error(String)");
            String description = "DescriptionTest";
            iae = new UnknownHappeningException(description);
            String iaedescription;
            iaedescription = iae.toString();
            if (iaedescription.length() == 0)
                throw new Exception("String description is empty");

        } catch (Exception e) {
            System.out.println("UnknownHappeningExceptionTest: IllegalAssignment"+
                               "Error(String) failed");
            Tests.fail("new UnknownHappeningExceptionTest(description)",e);
        }

        Tests.printSubTestReportTotals("UnknownHappeningExceptionTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
