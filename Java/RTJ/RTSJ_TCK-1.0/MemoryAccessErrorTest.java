//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              MemoryAccessErrorTest

Subtest 1:
        "public MemoryAccessError()"

Subtest 2:
        "public MemoryAccessError(java.lang.string description)"

Subtest 3
        "public MemoryAccessError(java.lang.String description)" checking
        value of String
*/

import javax.realtime.*;

public class MemoryAccessErrorTest
{

    public static void run()
    {
        MemoryAccessError mae = null;
        Object o = null;

        Tests.newTest("MemoryAccessErrorTest");
        Tests.increment();

        /* Subtest 1:
        ** Constructor "public MemoryAccessError()"
        */
        try {
            System.out.println("MemoryAccessErrorTest: MemoryAccessError()");
            mae = new MemoryAccessError();
            if( !(mae instanceof MemoryAccessError) )
                throw new Exception("Return object is not instanceof "+
                                    "MemoryAccessError");
        } catch (Exception e) {
            System.out.println("MemoryAccessErrorTest: MemoryAccessError()");
            Tests.fail("new MemoryAccessError()",e);
        }

        /* Subtest 2:
        ** Constructor "public MemoryAccessError(java.lang.string description)"
        */

        Tests.increment();
        try {
            System.out.println("MemoryAccessErrorTest: "+
                               "MemoryAccessError(String)");
        mae = new MemoryAccessError("ConstructorTest");
        if( !(mae instanceof MemoryAccessError) )
        throw new Exception("Return object is not instanceof "+
                            "MemoryAccessError");
        } catch (Exception e) {
            System.out.println("MemoryAccessErrorTest: "+
                               "MemoryAccessError(String) failed");
            Tests.fail("new MemoryAccessError(description)",e);
        }

        /* Subtest 3
        ** Constructor "public MemoryAccessError(java.lang.String
        ** description)" checking value of String
        */
        Tests.increment();
        try {
            System.out.println("MemoryAccessErrorTest: "+
                               "MemoryAccessError(String)");
            String description = "DescriptionTest";
            mae = new MemoryAccessError(description);
            String maedescription;
            maedescription = mae.toString();
            if (maedescription.length() == 0)
                throw new Exception("String description is empty");

        } catch (Exception e) {
            System.out.println("MemoryAccessErrorTest: "+
                               "MemoryAccessError(String) failed");
            Tests.fail("new MemoryAccessError(description)",e);
        }

        Tests.printSubTestReportTotals("MemoryAccessErrorTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
