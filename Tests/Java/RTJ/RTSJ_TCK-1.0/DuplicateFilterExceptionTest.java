//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              DuplicateFilterExceptionTest

Subtest 1:
        "public DuplicateFilterException()"

Subtest 2:
        "public DuplicateFilterException(java.lang.string description)"

Subtest 3
        "public DuplicateFilterException(java.lang.String description)" checking
        value of String
*/

import javax.realtime.*;

public class DuplicateFilterExceptionTest
{

    public static void run()
    {
        DuplicateFilterException mae = null;
        Object o = null;

        Tests.newTest("DuplicateFilterExceptionTest");
        Tests.increment();

        /* Subtest 1:
        ** Constructor "public DuplicateFilterException()"
        */
        try {
            System.out.println("DuplicateFilterExceptionTest: DuplicateFilterException()");
            mae = new DuplicateFilterException();
            if( !(mae instanceof DuplicateFilterException) )
                throw new Exception("Return object is not instanceof "+
                                    "DuplicateFilterException");
        } catch (Exception e) {
            System.out.println("DuplicateFilterExceptionTest: DuplicateFilterException()");
            Tests.fail("new DuplicateFilterException()",e);
        }

        /* Subtest 2:
        ** Constructor "public DuplicateFilterException(java.lang.string description)"
        */

        Tests.increment();
        try {
            System.out.println("DuplicateFilterExceptionTest: "+
                               "DuplicateFilterException(String)");
        mae = new DuplicateFilterException("ConstructorTest");
        if( !(mae instanceof DuplicateFilterException) )
        throw new Exception("Return object is not instanceof "+
                            "DuplicateFilterException");
        } catch (Exception e) {
            System.out.println("DuplicateFilterExceptionTest: "+
                               "DuplicateFilterException(String) failed");
            Tests.fail("new DuplicateFilterException(description)",e);
        }

        /* Subtest 3
        ** Constructor "public DuplicateFilterException(java.lang.String
        ** description)" checking value of String
        */
        Tests.increment();
        try {
            System.out.println("DuplicateFilterExceptionTest: "+
                               "DuplicateFilterException(String)");
            String description = "DescriptionTest";
            mae = new DuplicateFilterException(description);
            String maedescription;
            maedescription = mae.toString();
            if (maedescription.length() == 0)
                throw new Exception("String description is empty");

        } catch (Exception e) {
            System.out.println("DuplicateFilterExceptionTest: "+
                               "DuplicateFilterException(String) failed");
            Tests.fail("new DuplicateFilterException(description)",e);
        }

        Tests.printSubTestReportTotals("DuplicateFilterExceptionTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
