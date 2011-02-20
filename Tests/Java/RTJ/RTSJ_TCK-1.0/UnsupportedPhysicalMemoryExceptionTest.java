//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                      UnsupportedPhysicalMemoryExceptionTest

Subtest 1:
        "public UnsupportedPhysicalMemoryException()"

Subtest 2:
        "public UnsupportedPhysicalMemoryException(java.lang.string
        description)"

Subtest 3
        "public UnsupportedPhysicalMemoryException(java.lang.String
        description)" checking value of String
*/

import javax.realtime.*;

public class UnsupportedPhysicalMemoryExceptionTest
{

    public static void run()
    {
        UnsupportedPhysicalMemoryException upme = null;
        Object o = null;

        Tests.newTest("UnsupportedPhysicalMemoryExceptionTest");
        Tests.increment();

        /* Subtest 1:
        ** Constructor "public UnsupportedPhysicalMemoryException()"
        */
        try {
            System.out.println("UnsupportedPhysicalMemoryExceptionTest: "+
                               "UnsupportedPhysicalMemoryException()");
            upme = new UnsupportedPhysicalMemoryException();
            if( !(upme instanceof UnsupportedPhysicalMemoryException) )
                throw new Exception("Return object is not instanceof "+
                                    "UnsupportedPhysicalMemoryException");
        } catch (Exception e) {
            System.out.println("UnsupportedPhysicalMemoryExceptionTest: "+
                               "UnsupportedPhysicalMemoryException() failed");
            Tests.fail("new UnsupportedPhysicalMemoryException()",e);
        }

        /* Subtest 2:
        ** Constructor "public UnsupportedPhysicalMemoryException(java.lang.
        ** string description)"
        */

        Tests.increment();
        try {
            System.out.println("UnsupportedPhysicalMemoryExceptionTest: "+
                               "UnsupportedPhysicalMemoryException(String)");
            upme = new UnsupportedPhysicalMemoryException("ConstructorTest");
            if( !(upme instanceof UnsupportedPhysicalMemoryException) )
                throw new Exception("Return object is not instanceof "+
                                    "UnsupportedPhysicalMemoryException");
        } catch (Exception e) {
            System.out.println("UnsupportedPhysicalMemoryExceptionTest: "+
                               "UnsupportedPhysicalMemoryException(String) "+
                               "failed");
            Tests.fail("new UnsupportedPhysicalMemoryException(description)",
                       e);
        }

        /* Subtest 3
        ** Constructor "public UnsupportedPhysicalMemoryException(java.lang.
        ** String description)" checking value of String
        */
        Tests.increment();
        try {
            System.out.println("UnsupportedPhysicalMemoryExceptionTest: "+
                               "UnsupportedPhysicalMemoryException(String)");
            String description = "DescriptionTest";
            upme = new UnsupportedPhysicalMemoryException(description);
            String upmedescription;
            upmedescription = upme.toString();
            if (upmedescription.length() == 0)
                throw new Exception("String description is empty");

        } catch (Exception e) {
            System.out.println("UnsupportedPhysicalMemoryExceptionTest: "+
                               "UnsupportedPhysicalMemoryException(String) "+
                               "failed");
            Tests.fail("new UnsupportedPhysicalMemoryException(description)",
                       e);
        }

        Tests.printSubTestReportTotals("UnsupportedPhysicalMemoryException"+
                                       "Test");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
