//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              MITViolationExceptionTest

Subtest 1:
        "public MITViolationException()"

Subtest 2:
        "public MITViolationException(java.lang.string description)"

Subtest 3
        "public MITViolationException(java.lang.String description)" checking
        value of String
*/

import javax.realtime.*;

public class MITViolationExceptionTest
{

    public static void run()
    {
        MITViolationException mae = null;
        Object o = null;

        Tests.newTest("MITViolationExceptionTest");
        Tests.increment();

        /* Subtest 1:
        ** Constructor "public MITViolationException()"
        */
        try {
            System.out.println("MITViolationExceptionTest: MITViolationException()");
            mae = new MITViolationException();
            if( !(mae instanceof MITViolationException) )
                throw new Exception("Return object is not instanceof "+
                                    "MITViolationException");
        } catch (Exception e) {
            System.out.println("MITViolationExceptionTest: MITViolationException()");
            Tests.fail("new MITViolationException()",e);
        }

        /* Subtest 2:
        ** Constructor "public MITViolationException(java.lang.string description)"
        */

        Tests.increment();
        try {
            System.out.println("MITViolationExceptionTest: "+
                               "MITViolationException(String)");
        mae = new MITViolationException("ConstructorTest");
        if( !(mae instanceof MITViolationException) )
        throw new Exception("Return object is not instanceof "+
                            "MITViolationException");
        } catch (Exception e) {
            System.out.println("MITViolationExceptionTest: "+
                               "MITViolationException(String) failed");
            Tests.fail("new MITViolationException(description)",e);
        }

        /* Subtest 3
        ** Constructor "public MITViolationException(java.lang.String
        ** description)" checking value of String
        */
        Tests.increment();
        try {
            System.out.println("MITViolationExceptionTest: "+
                               "MITViolationException(String)");
            String description = "DescriptionTest";
            mae = new MITViolationException(description);
            String maedescription;
            maedescription = mae.toString();
            if (maedescription.length() == 0)
                throw new Exception("String description is empty");

        } catch (Exception e) {
            System.out.println("MITViolationExceptionTest: "+
                               "MITViolationException(String) failed");
            Tests.fail("new MITViolationException(description)",e);
        }

        Tests.printSubTestReportTotals("MITViolationExceptionTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
