//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                      MemoryTypeConflictExceptionTest

Subtest 1:
        "public MemoryTypeConflictException()"

Subtest 2:
        "public MemoryTypeConflictException(java.lang.String description)"

Subtest 3:
        "public MemoryTypeConflictException(java.lang.String description)" checking
        value of String
*/

import javax.realtime.*;

public class MemoryTypeConflictExceptionTest
{

    public static void run()
    {
        MemoryTypeConflictException iae = null;
        Object o = null;

        Tests.newTest("MemoryTypeConflictExceptionTest");
        Tests.increment();

        /* Subtest 1:
        ** Constructor "public MemoryTypeConflictException()"
        */
        try {
            System.out.println("MemoryTypeConflictExceptionTest: IllegalAssignment"+
                               "Error()");
            iae = new MemoryTypeConflictException();
            if( !(iae instanceof MemoryTypeConflictException) )
                throw new Exception("Return object is not instanceof "+
                                    "MemoryTypeConflictException");
        } catch (Exception e) {
            System.out.println("MemoryTypeConflictExceptionTest: IllegalAssignment"+
                               "Error() failed");
            Tests.fail("new MemoryTypeConflictException()",e);
        }

        /* Subtest 2:
        ** Constructor "public MemoryTypeConflictException(java.lang.String
        ** description)"
        */
        Tests.increment();
        try {
            System.out.println("MemoryTypeConflictExceptionTest: IllegalAssignment"+
                               "Error(String)");
            iae = new MemoryTypeConflictException("ConstructorTest");
            if( !(iae instanceof MemoryTypeConflictException) )
                throw new Exception("Return object is not instanceof "+
                                    "MemoryTypeConflictException");
        } catch (Exception e) {
            System.out.println("MemoryTypeConflictExceptionTest: IllegalAssignment"+
                               "Error(String) failed");
            Tests.fail("new MemoryTypeConflictExceptionTest(description)",e);
        }

        /* Subtest 3:
        ** Constructor "public MemoryTypeConflictException(java.lang.String
        ** description)" checking value of String
        */

        Tests.increment();
        try {
            System.out.println("MemoryTypeConflictExceptionTest: IllegalAssignment"+
                               "Error(String)");
            String description = "DescriptionTest";
            iae = new MemoryTypeConflictException(description);
            String iaedescription;
            iaedescription = iae.toString();
            if (iaedescription.length() == 0)
                throw new Exception("String description is empty");

        } catch (Exception e) {
            System.out.println("MemoryTypeConflictExceptionTest: IllegalAssignment"+
                               "Error(String) failed");
            Tests.fail("new MemoryTypeConflictExceptionTest(description)",e);
        }

        Tests.printSubTestReportTotals("MemoryTypeConflictExceptionTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
