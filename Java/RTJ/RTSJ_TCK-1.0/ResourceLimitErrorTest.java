//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                      ResourceLimitErrorTest

Subtest 1:
        "public ResourceLimitError()"

Subtest 2:
        "public ResourceLimitError(java.lang.string description)"

Subtest 3
        "public ResourceLimitError(java.lang.String description)" checking
        value of String
*/

import javax.realtime.*;

public class ResourceLimitErrorTest
{

    public static void run()
    {
        ResourceLimitError rle = null;
        Object o = null;

        Tests.newTest("ResourceLimitErrorTest");
        Tests.increment();

        /* Subtest 1:
        ** Constructor "public ResourceLimitError()"
        */

        try {
            System.out.println("ResourceLimitError: ResourceLimitError()");
            rle = new ResourceLimitError();
            if( !(rle instanceof ResourceLimitError) )
                throw new Exception("Return object is not instanceof "+
                                    "ResourceLimitError");
        } catch (Exception e) {
            System.out.println("ResourceLimitError: ResourceLimitError() "+
                               "failed");
            Tests.fail("new ResourceLimitError()",e);
        }

        /* Subtest 2:
        ** Constructor "public ResourceLimitError(java.lang.string
        ** description)"
        */

        Tests.increment();
        try {
            System.out.println("ResourceLimitError: ResourceLimitError("+
                               "String)");
            rle = new ResourceLimitError("ConstructorTest");
            if( !(rle instanceof ResourceLimitError) )
                throw new Exception("Return object is not instanceof "+
                                    "ResourceLimitError");
        } catch (Exception e) {
            System.out.println("ResourceLimitError: ResourceLimitError("+
                               "String) failed");
            Tests.fail("new ResourceLimitError(description)",e);
        }

        /* Subtest 3
        ** Constructor "public ResourceLimitError(java.lang.String
        ** description)" checking value of String
        */
        Tests.increment();
        try {
            System.out.println("ResourceLimitError: ResourceLimitError("+
                               "String)");
            String description = "DescriptionTest";
            rle = new ResourceLimitError(description);
            String rledescription;
            rledescription = rle.toString();
            if (rledescription.length() == 0)
                throw new Exception("String description is empty");

        } catch (Exception e) {
            System.out.println("ResourceLimitError: ResourceLimitError("+
                               "String) failed");
            Tests.fail("new ResourceLimitError(description)",e);
        }

        Tests.printSubTestReportTotals("ResourceLimitErrorTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
