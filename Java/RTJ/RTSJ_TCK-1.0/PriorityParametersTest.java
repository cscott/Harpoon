//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                      PriorityParametersTest

Subtest 1:
        "public PriorityParameters(int priority)"

Subtest 2:
        "public int getPriority()"

Subtest 3:
        "public void setPriority(int priority)"

Subtest 4:
        "public java.lang.String toString()"
*/

import javax.realtime.*;

public class PriorityParametersTest
{

    public static void run()
    {
        PriorityParameters pp = null;
        Object o = null;

        Tests.newTest("PriorityParameters");

        /* Subtest 1:
        ** Constructor "public PriorityParameters(int priority)"
        */
        Tests.increment();
        try {
            System.out.println("PriorityParametersTest: PriorityParameters("+
                               "int)");
            pp = new PriorityParameters(10);
            if( !(pp instanceof PriorityParameters && pp instanceof
                  SchedulingParameters) )
                throw new Exception("Return object is not instanceof "+
                                    "PriorityParameters nor Scheduling"+
                                    "Parameters");
        } catch (Exception e) {
            System.out.println("PriorityParametersTest: PriorityParameters("+
                               "int) failed");
            Tests.fail("new PriorityParameters(priority)",e);
        }

        /* Subtest 2:
        ** Method "public int getPriority()"
        */
        Tests.increment();
        try {
            System.out.println("PriorityParametersTest: getPriority()");
            int priority = pp.getPriority();
            if( priority != 10 )
                throw new Exception("Unexpected priority received");
        } catch (Exception e) {
            System.out.println("PriorityParametersTest: getPriority() failed");
            Tests.fail("pp.getPriority",e);
        }

        /* Subtest 3:
        ** Method "public void setPriority(int priority)"
        */
        Tests.increment();
        try {
            System.out.println("PriorityParametersTest: setPriority(int)");
            pp.setPriority(5);
            if( pp.getPriority() != 5 )
                throw new Exception("Priority not set properly");
        } catch (Exception e) {
            System.out.println("PriorityParametersTest: setPriority(int) "+
                               "failed");
            Tests.fail("pp.getPriority()",e);
        }

        /* Subtest 4:
        ** Method "public java.lang.String toString()"
        */
        Tests.increment();
        try {
            System.out.println("PriorityParametersTest: toString()");
            o = pp.toString();
            if( !(o instanceof String) )
                throw new Exception("Return object is not instanceof String");
        } catch (Exception e) {
            System.out.println("PriorityParametersTest: toString() failed");
            Tests.fail("pp.toString",e);
        }

        Tests.printSubTestReportTotals("PriorityParametersTest");

    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
