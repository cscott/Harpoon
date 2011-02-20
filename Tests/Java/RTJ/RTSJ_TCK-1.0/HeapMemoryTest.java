//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                      HeapMemoryTest

Subtest 1:
        "public static HeapMemory instance()"
*/

import javax.realtime.*;

public class HeapMemoryTest
{
    public static void run()
    {
        HeapMemory hm = null;
        Object o = null;

        Tests.newTest("HeapMemoryTest");
        Tests.increment();

        /* Subtest 1:
        ** Method "public static HeapMemory instance()"
        */
        try {
            System.out.println("HeapMemoryTest: instance()");
            hm = HeapMemory.instance();
            if( !(hm instanceof HeapMemory) )
                throw new Exception("Return object is not instanceof "+
                                    "HeapMemory");
        } catch (Exception e) {
            System.out.println("HeapMemoryTest: instance() failed");
            Tests.fail("HeapMemory.instance()",e);
        }

        Tests.printSubTestReportTotals("HeapMemoryTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
