//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              InaccessibleAreaExceptionTest

Subtest 1:
        "public InaccessibleAreaException()"

Subtest 2:
        "public InaccessibleAreaException(java.lang.string description)"

Subtest 3
        "public InaccessibleAreaException(java.lang.String description)" checking
        value of String
*/

import javax.realtime.*;

public class InaccessibleAreaExceptionTest
{

    public static void run()
    {
        InaccessibleAreaException mae = null;
        Object o = null;

        Tests.newTest("InaccessibleAreaExceptionTest");
        Tests.increment();

        /* Subtest 1:
        ** Constructor "public InaccessibleAreaException()"
        */
        try {
            System.out.println("InaccessibleAreaExceptionTest: InaccessibleAreaException()");
            mae = new InaccessibleAreaException();
            if( !(mae instanceof InaccessibleAreaException) )
                throw new Exception("Return object is not instanceof "+
                                    "InaccessibleAreaException");
        } catch (Exception e) {
            System.out.println("InaccessibleAreaExceptionTest: InaccessibleAreaException()");
            Tests.fail("new InaccessibleAreaException()",e);
        }

        /* Subtest 2:
        ** Constructor "public InaccessibleAreaException(java.lang.string description)"
        */

        Tests.increment();
        try {
            System.out.println("InaccessibleAreaExceptionTest: "+
                               "InaccessibleAreaException(String)");
        mae = new InaccessibleAreaException("ConstructorTest");
        if( !(mae instanceof InaccessibleAreaException) )
        throw new Exception("Return object is not instanceof "+
                            "InaccessibleAreaException");
        } catch (Exception e) {
            System.out.println("InaccessibleAreaExceptionTest: "+
                               "InaccessibleAreaException(String) failed");
            Tests.fail("new InaccessibleAreaException(description)",e);
        }

        /* Subtest 3
        ** Constructor "public InaccessibleAreaException(java.lang.String
        ** description)" checking value of String
        */
        Tests.increment();
        try {
            System.out.println("InaccessibleAreaExceptionTest: "+
                               "InaccessibleAreaException(String)");
            String description = "DescriptionTest";
            mae = new InaccessibleAreaException(description);
            String maedescription;
            maedescription = mae.toString();
            if (maedescription.length() == 0)
                throw new Exception("String description is empty");

        } catch (Exception e) {
            System.out.println("InaccessibleAreaExceptionTest: "+
                               "InaccessibleAreaException(String) failed");
            Tests.fail("new InaccessibleAreaException(description)",e);
        }

        Tests.printSubTestReportTotals("InaccessibleAreaExceptionTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
