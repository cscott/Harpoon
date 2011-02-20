//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              MemoryInUseExceptionTest

Subtest 1:
        "public MemoryInUseException()"

Subtest 2:
        "public MemoryInUseException(java.lang.string description)"

Subtest 3
        "public MemoryInUseException(java.lang.String description)" checking
        value of String
*/

import javax.realtime.*;

public class MemoryInUseExceptionTest
{

    public static void run()
    {
        MemoryInUseException mae = null;
        Object o = null;

        Tests.newTest("MemoryInUseExceptionTest");
        Tests.increment();

        /* Subtest 1:
        ** Constructor "public MemoryInUseException()"
        */
        try {
            System.out.println("MemoryInUseExceptionTest: MemoryInUseException()");
            mae = new MemoryInUseException();
            if( !(mae instanceof MemoryInUseException) )
                throw new Exception("Return object is not instanceof "+
                                    "MemoryInUseException");
        } catch (Exception e) {
            System.out.println("MemoryInUseExceptionTest: MemoryInUseException()");
            Tests.fail("new MemoryInUseException()",e);
        }

        /* Subtest 2:
        ** Constructor "public MemoryInUseException(java.lang.string description)"
        */

        Tests.increment();
        try {
            System.out.println("MemoryInUseExceptionTest: "+
                               "MemoryInUseException(String)");
        mae = new MemoryInUseException("ConstructorTest");
        if( !(mae instanceof MemoryInUseException) )
        throw new Exception("Return object is not instanceof "+
                            "MemoryInUseException");
        } catch (Exception e) {
            System.out.println("MemoryInUseExceptionTest: "+
                               "MemoryInUseException(String) failed");
            Tests.fail("new MemoryInUseException(description)",e);
        }

        /* Subtest 3
        ** Constructor "public MemoryInUseException(java.lang.String
        ** description)" checking value of String
        */
        Tests.increment();
        try {
            System.out.println("MemoryInUseExceptionTest: "+
                               "MemoryInUseException(String)");
            String description = "DescriptionTest";
            mae = new MemoryInUseException(description);
            String maedescription;
            maedescription = mae.toString();
            if (maedescription.length() == 0)
                throw new Exception("String description is empty");

        } catch (Exception e) {
            System.out.println("MemoryInUseExceptionTest: "+
                               "MemoryInUseException(String) failed");
            Tests.fail("new MemoryInUseException(description)",e);
        }

        Tests.printSubTestReportTotals("MemoryInUseExceptionTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
