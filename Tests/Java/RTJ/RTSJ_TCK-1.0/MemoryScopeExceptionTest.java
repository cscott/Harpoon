//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              MemoryScopeExceptionTeset

Subtest 1
        "public MemoryScopeException()"

Subtest 2
        "public class MemoryScopeException(java.lang.String description)"

Subtest 3
        "public class MemoryScopeException(java.lang.String description)"
*/

import javax.realtime.*;

public class MemoryScopeExceptionTest
{

    public static void run()
    {
        MemoryScopeException mse = null;
        Object o = null;

        Tests.newTest("MemoryScopeExceptionTest");
        Tests.increment();

        /* Subtest 1
        ** Constructor "public MemoryScopeException()"
        */
        try {
            System.out.println("MemoryScopeExceptionTest: "+
                               "MemoryScopeException()");
            mse = new MemoryScopeException();
            if( !(mse instanceof MemoryScopeException) )
                throw new Exception("Return object is not instanceof MemoryScopeException");
        } catch (Exception e) {
            System.out.println("MemoryScopeExceptionTest: "+
                               "MemoryScopeException() failed");
            Tests.fail("new MemoryScopeExceptionTest()",e);
        }

        /* Subtest 2
        ** Constructor "public class MemoryScopeException(java.lang.String description)"
        */
        Tests.increment();
        try {
            System.out.println("MemoryScopeExceptionTest: "+
                               "MemoryScopeException(String)");
            mse = new MemoryScopeException("ConstructorTest");
            if( !(mse instanceof MemoryScopeException) )
                throw new Exception("Return object is not instanceof MemoryAccessError");
        } catch (Exception e) {
            System.out.println("MemoryScopeExceptionTest: "+
                               "MemoryScopeException(String) failed");
            Tests.fail("new MemoryScopeExceptionTest(description)",e);
        }

        /* Subtest 3
        ** Constructor "public class MemoryScopeException(java.lang.String description)"
        */
        Tests.increment();
        try {
            System.out.println("MemoryScopeExceptionTest: "+
                               "MemoryScopeException(String)");
            String description = "DescriptionTest";
            mse = new MemoryScopeException(description);
            String msedescription;
            msedescription = mse.toString();
            if (msedescription.length() == 0)
                throw new Exception("String description is empty");
        } catch (Exception e) {
            System.out.println("MemoryScopeExceptionTest: "+
                               "MemoryScopeException(String)");
            Tests.fail("new MemoryScopeException(description)",e);
        }

        Tests.printSubTestReportTotals("MemoryScopeException");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
