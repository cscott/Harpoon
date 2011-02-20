//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                      SizeOutOfBoundsExceptionTest

Subtest 1:
        "public SizeOutOfBoundsException()"

Subtest 2:
        "public SizeOutOfBoundsException(java.lang.string description)"

Subtest 3
        "public SizeOutOfBoundsException(java.lang.String description)"
        checking value of String

*/

import javax.realtime.*;

public class SizeOutOfBoundsExceptionTest
{

    public static void run()
    {
        SizeOutOfBoundsException soobe = null;
        Object o = null;

        Tests.newTest("SizeOutOfBoundsExceptionTest");
        Tests.increment();

        /* Subtest 1:
        ** Constructor "public SizeOutOfBoundsException()"
        */

        try {
            System.out.println("SizeOutOfBoundsExceptionTest: SizeOutOfBounds"+
                               "Exception()");
            soobe = new SizeOutOfBoundsException();
            if( !(soobe instanceof SizeOutOfBoundsException) )
                throw new Exception("Return object is not instanceof "+
                                    "SizeOutOfBoundsException");
        } catch (Exception e) {
            System.out.println("SizeOutOfBoundsExceptionTest: SizeOutOfBounds"+
                               "Exception() failed");
            Tests.fail("new SizeOutOfBoundsException()",e);
        }

        /* Subtest 2:
        ** Constructor "public SizeOutOfBoundsException(java.lang.string
        ** description)"
        */

        Tests.increment();
        try {
            System.out.println("SizeOutOfBoundsExceptionTest: SizeOutOfBounds"+
                               "Exception(String)");
            soobe = new SizeOutOfBoundsException("ConstructorTest");
            if( !(soobe instanceof SizeOutOfBoundsException) )
                throw new Exception("Return object is not instanceof SizeOut"+
                                    "OfBoundsException");
        } catch (Exception e) {
            System.out.println("SizeOutOfBoundsExceptionTest: SizeOutOfBounds"+
                               "Exception(String)");
            Tests.fail("new SizeOutOfBoundsException(description)",e);
        }

        /* Subtest 3
        ** Constructor "public SizeOutOfBoundsException(java.lang.String
        ** description)" checking value of String
        */

        Tests.increment();
        try {
            System.out.println("SizeOutOfBoundsExceptionTest: SizeOutOfBounds"+
                               "Exception(String)");
            String description = "DescriptionTest";
            soobe = new SizeOutOfBoundsException(description);
            String soobedescription;
            soobedescription = soobe.toString();
            if (soobedescription.length() == 0)
                throw new Exception("String description is empty");

        } catch (Exception e) {
            System.out.println("SizeOutOfBoundsExceptionTest: SizeOutOfBounds"+
                               "Exception(String) failed");
            Tests.fail("new SizeOutOfBoundsException(description)",e);
        }

        Tests.printSubTestReportTotals("SizeOutOfBoundsExceptionTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
