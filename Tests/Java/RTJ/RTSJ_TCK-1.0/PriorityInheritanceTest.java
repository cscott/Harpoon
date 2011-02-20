//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              PriorityInheritanceTest

Subtest 1:
        "public class PriorityInheritance extends MonitorControl"

Subtest 2:
        "public static PriorityInheritance instance()"

Subtest NEW:
        "public static MonitorControl getMonitorControl()"
*/

import javax.realtime.*;

public class PriorityInheritanceTest {

    public static void run() {
        PriorityInheritance pi = null;
        Object o = null;

        Tests.newTest("PriorityInheritanceTest");

        /* Subtest 1:
        ** Constructor "public class PriorityInheritance extends
        ** MonitorControl"
        */
        Tests.increment();
        try {
            System.out.println("PriorityInheritanceTest: Priority"+
                               "Inheritance()");
            pi = new PriorityInheritance();
            if( !(pi instanceof PriorityInheritance && pi instanceof
                  MonitorControl) )
                throw new Exception("Return object is not instanceof "+
                                    "PriorityInheritance nor MonitorControl");
        } catch (Exception e) {
            System.out.println("PriorityInheritanceTest: Priority"+
                               "Inheritance()");
            Tests.fail("new PriorityInheritance()",e);
        }

        /* Subtest 2:
        ** Method "public static PriorityInheritance instance()"
        */
        Tests.increment();
        try {
            System.out.println("PriorityInheritanceTest: instance()");
            o = PriorityInheritance.instance();
            if( !(o instanceof PriorityInheritance && o instanceof
                  MonitorControl) )
                throw new Exception("Return object is not instanceof "+
                                    "PriorityInheritance nor MonitorControl");
        } catch (Exception e) {
            System.out.println("PriorityInheritanceTest: instance()");
            Tests.fail("PriorityInheritance.instance()",e);
        }

        /* Subtest NEW:
        ** Method "public static MonitorControl getMonitorControl()"
        */
        Tests.increment();
        try {
            System.out.println("PriorityInheritanceTest: getMonitorControl()");
            o = PriorityInheritance.getMonitorControl();
            if( !(o instanceof MonitorControl) )
                throw new Exception("Return object is not instanceof "+
                                    "MonitorControl");
        } catch (Exception e) {
            System.out.println("PriorityInheritanceTest: getMonitorControl()");
            Tests.fail("PriorityInheritance.getMonitorControl()",e);
        }

        Tests.printSubTestReportTotals("PriorityInheritanceTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
