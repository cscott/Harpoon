//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                      IllegalAssignmentErrorTest

Subtest 1:
        "public IllegalAssignmentError()"

Subtest 2:
        "public IllegalAssignmentError(java.lang.String description)"

Subtest 3:
        "public IllegalAssignmentError(java.lang.String description)" checking
        value of String
*/

import javax.realtime.*;

public class IllegalAssignmentErrorTest
{

    public static void run()
    {
        IllegalAssignmentError iae = null;
        Object o = null;

        Tests.newTest("IllegalAssignmentErrorTest");
        Tests.increment();

        /* Subtest 1:
        ** Constructor "public IllegalAssignmentError()"
        */
        try {
            System.out.println("IllegalAssignmentErrorTest: IllegalAssignment"+
                               "Error()");
            iae = new IllegalAssignmentError();
            if( !(iae instanceof IllegalAssignmentError) )
                throw new Exception("Return object is not instanceof "+
                                    "IllegalAssignmentError");
        } catch (Exception e) {
            System.out.println("IllegalAssignmentErrorTest: IllegalAssignment"+
                               "Error() failed");
            Tests.fail("new IllegalAssignmentError()",e);
        }

        /* Subtest 2:
        ** Constructor "public IllegalAssignmentError(java.lang.String
        ** description)"
        */
        Tests.increment();
        try {
            System.out.println("IllegalAssignmentErrorTest: IllegalAssignment"+
                               "Error(String)");
            iae = new IllegalAssignmentError("ConstructorTest");
            if( !(iae instanceof IllegalAssignmentError) )
                throw new Exception("Return object is not instanceof "+
                                    "IllegalAssignmentError");
        } catch (Exception e) {
            System.out.println("IllegalAssignmentErrorTest: IllegalAssignment"+
                               "Error(String) failed");
            Tests.fail("new IllegalAssignmentErrorTest(description)",e);
        }

        /* Subtest 3:
        ** Constructor "public IllegalAssignmentError(java.lang.String
        ** description)" checking value of String
        */

        Tests.increment();
        try {
            System.out.println("IllegalAssignmentErrorTest: IllegalAssignment"+
                               "Error(String)");
            String description = "DescriptionTest";
            iae = new IllegalAssignmentError(description);
            String iaedescription;
            iaedescription = iae.toString();
            if (iaedescription.length() == 0)
                throw new Exception("String description is empty");

        } catch (Exception e) {
            System.out.println("IllegalAssignmentErrorTest: IllegalAssignment"+
                               "Error(String) failed");
            Tests.fail("new IllegalAssignmentErrorTest(description)",e);
        }

        Tests.printSubTestReportTotals("IllegalAssignmentErrorTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
