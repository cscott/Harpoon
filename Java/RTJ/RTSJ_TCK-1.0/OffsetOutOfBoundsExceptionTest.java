//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                      OffsetOutOfBoundsExceptionTest

Subtest 1:
        "public OffsetOutOfBoundsException()"

Subtest 2:
        "public OffsetOutOfBoundsException(java.lang.string description)"

Subtest 3
        "public OffsetOutOfBoundsException(java.lang.String description)"
        checking value of String
*/

import javax.realtime.*;

public class OffsetOutOfBoundsExceptionTest
{

    public static void run()
    {
        OffsetOutOfBoundsException ooobe = null;
        Object o = null;

        Tests.newTest("OffsetOutOfBoundsExceptionTest");
        Tests.increment();

        /* Subtest 1:
        ** Constructor "public OffsetOutOfBoundsException()"
        */
        try {
            System.out.println("OffsetOutOfBoundsExceptionTest: "+
                               "OffsetOutOfBoundsException()");
            ooobe = new OffsetOutOfBoundsException();
            if( !(ooobe instanceof OffsetOutOfBoundsException) )
                throw new Exception("Return object is not instanceof "+
                                    "OffsetOutOfBoundsException");
        } catch (Exception e) {
            System.out.println("OffsetOutOfBoundsExceptionTest: "+
                               "OffsetOutOfBoundsException()");
            Tests.fail("new OffsetOutOfBoundsException()",e);
        }

        /* Subtest 2:
        ** Constructor "public OffsetOutOfBoundsException(java.lang.string
        ** description)"
        */
        Tests.increment();
        try {
            System.out.println("OffsetOutOfBoundsExceptionTest: "+
                               "OffsetOutOfBoundsException(String)");
            ooobe = new OffsetOutOfBoundsException("ConstructorTest");
            if( !(ooobe instanceof OffsetOutOfBoundsException) )
                throw new Exception("Return object is not instanceof "+
                                    "OffsetOutOfBoundsException");
        } catch (Exception e) {
            System.out.println("OffsetOutOfBoundsExceptionTest: "+
                               "OffsetOutOfBoundsException()");
            Tests.fail("new OffsetOutOfBoundsException(description)",e);
        }

        /* Subtest 3
        ** Constructor "public OffsetOutOfBoundsException(java.lang.String
        ** description)" checking value of String
        */
        Tests.increment();
        try {
            System.out.println("OffsetOutOfBoundsExceptionTest: "+
                               "OffsetOutOfBoundsException(String)");
            String description = "DescriptionTest";
            ooobe = new OffsetOutOfBoundsException(description);
            String ooobedescription;
            ooobedescription = ooobe.toString();
            if (ooobedescription.length() == 0)
                throw new Exception("String description is empty");

        } catch (Exception e) {
            System.out.println("OffsetOutOfBoundsExceptionTest: "+
                               "OffsetOutOfBoundsException(String)");
            Tests.fail("new OffsetOutOfBoundsException(description)",e);
        }

        Tests.printSubTestReportTotals("OffsetOutOfBoundsExceptionTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
