//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              MarkAndSweepCollectorTest

Subtest 1
        "public MarkAndSweepCollector()"

Subtest 2
        "public RelativeTime getPreemptionLatency()"
*/

import javax.realtime.*;

public class MarkAndSweepCollectorTest
{

    public static void run()
    {
        MarkAndSweepCollector mc = null;
        Object o = null;


        Tests.newTest("MarkAndSweepCollectorTest (abstract GarbageCollector)");

        /* Subtest 1
        ** Constructor "public MarkAndSweepCollector()"
        */
        Tests.increment();
        try {
            System.out.println("MarkAndSweepCollectorTest: "+
                               "MarkAndSweepCollector()");
            mc = new MarkAndSweepCollector();
            if( !(mc instanceof MarkAndSweepCollector && mc instanceof
                  GarbageCollector) )
                throw new Exception("Return object is not instanceof "+
                                    "MarkAndSweepCollector nor "+
                                    "GarbageCollector");
        } catch (Exception e) {
            System.out.println("MarkAndSweepCollectorTest: "+
                               "MarkAndSweepCollector() failed");
            Tests.fail("new MarkAndSweepCollector()",e);
        }

        /* Subtest 2
        ** Method "public RelativeTime getPreemptionLatency()"
        */
        Tests.increment();
        try {
            System.out.println("MarkAndSweepCollectorTest: "+
                               "getPreemptionLatency()");
            o = mc.getPreemptionLatency();
            if( !(o instanceof RelativeTime) )
                throw new Exception("Return object is not instanceof RelativeTime");
        } catch (Exception e) {
            System.out.println("MarkAndSweepCollectorTest: "+
                               "getPreemptionLatency() failed");
            Tests.fail("mc.getPreemptionLatency()",e);
        }

        Tests.printSubTestReportTotals("MarkAndSweepCollectorTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
