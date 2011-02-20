//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              ThrowBoundaryErrorTest

Subtest 1:
        "public ThrowBoundaryError()"

Subtest 2:
        "public ThrowBoundaryError(java.lang.string description)"

Subtest 3
        "public ThrowBoundaryError(java.lang.String description)" checking
        value of String
*/

import javax.realtime.*;

public class ThrowBoundaryErrorTest
{

    public static void run()
    {
        ThrowBoundaryError tbe = null;
        Object o = null;

        Tests.newTest("ThrowBoundaryErrorTest");
        Tests.increment();

        /* Subtest 1:
        ** Constructor "public ThrowBoundaryError()"
        */
        try {
            System.out.println("ThrowBoundryError: ThrowBoundaryError()");
            tbe = new ThrowBoundaryError();
            if( !(tbe instanceof ThrowBoundaryError) )
                throw new Exception("Return object is not instanceof "+
                                    "ThrowBoundaryError");
        } catch (Exception e) {
            System.out.println("ThrowBoundryError: ThrowBoundaryError() "+
                               "failed");
            Tests.fail("new ThrowBoundaryError()",e);
        }

        /* Subtest 2:
        ** Constructor "public ThrowBoundaryError(java.lang.string
        ** description)"
        */

        Tests.increment();
        try {
            System.out.println("ThrowBoundryError: ThrowBoundaryError("+
                               "String)");
            tbe = new ThrowBoundaryError("ConstructorTest");
            if( !(tbe instanceof ThrowBoundaryError) )
                throw new Exception("Return object is not instanceof "+
                                    "ThrowBoundaryError");
        } catch (Exception e) {
            System.out.println("ThrowBoundryError: ThrowBoundaryError("+
                               "String) failed");
            Tests.fail("new ThrowBoundaryError(description)",e);
        }

        /* Subtest 3
        ** Constructor "public ThrowBoundaryError(java.lang.String
        ** description)" checking value of String
        */
        Tests.increment();
        try {
            System.out.println("ThrowBoundryError: ThrowBoundaryError("+
                               "String)");
            String description = "DescriptionTest";
            tbe = new ThrowBoundaryError(description);
            String tbedescription;
            tbedescription = tbe.toString();
            if (tbedescription.length() == 0)
                throw new Exception("String description is empty");

        } catch (Exception e) {
            System.out.println("ThrowBoundryError: ThrowBoundaryError("+
                               "String) failed");
            Tests.fail("new ThrowBoundaryError(description)",e);
        }

        Tests.printSubTestReportTotals("ThrowBoundaryErrorTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
