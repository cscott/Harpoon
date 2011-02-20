//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              PriorityCeilingEmulationTest

Subtest 1:
        "public PriorityCeilingEmulation(int ceiling)"

Subtest 2
        "public int getDefaultCeiling()"

** MonitorControl Tests **

Subtest NEW
        "public static MonitorControl getMonitorControl()"

Subtest 3
        "public static void setMonitorControl(MonitorControl policy)"

Subtest 4
        "public static void setMonitorControl(MonitorControl policy)" where policy is null

Subtest 5
        "public static void setMonitorControl(java.lang.Object monitor, MonitorControl policy)"

Subtest 6
        "public static void setMonitorControl(java.lang.Object monitor, MonitorControl policy)"
*/


import javax.realtime.*;

public class PriorityCeilingEmulationTest
{

    public static void run()
    {
        PriorityCeilingEmulation pce = null;
        Object o = null;
        int ceiling=10;

        Tests.newTest("PriorityCeilingEmulationTest (abstract "+
                      "MonitorControl)");

        /* Subtest 1:
        ** Constructor "public PriorityCeilingEmulation(int ceiling)"
        */
        Tests.increment();
        try {
            System.out.println("PriorityCeilingEmulationTest: "+
                               "PriorityCeilingEmulation(int)");
            pce = new PriorityCeilingEmulation(ceiling);
            if( !(pce instanceof PriorityCeilingEmulation && pce instanceof
                  MonitorControl) )
                throw new Exception("Return object is not instanceof "+
                                    "PriorityCeilingEmulation nor "+
                                    "MonitorControl");
        } catch (Exception e) {
            System.out.println("PriorityCeilingEmulationTest: "+
                               "PriorityCeilingEmulation(int) failed");
            Tests.fail("new PriorityCeilingEmulation(ceiling)",e);
        }

        /* Subtest 2
        ** Method "public int getDefaultCeiling()"
        */
        Tests.increment();
        try {
            System.out.println("PriorityCeilingEmulationTest: "+
                               "getDefaultCeiling()");
            int defaultCeiling = pce.getDefaultCeiling();
            if (defaultCeiling != ceiling)
                throw new Exception("Unexpected ceiling received");
        } catch (Exception e) {
            System.out.println("PriorityCeilingEmulationTest: "+
                               "getDefaultCeiling() failed");
            Tests.fail("pce.getDefaultCeiling()",e);
        }

        /* MonitorControl Tests */
        /* Subtest NEW
        ** Method "public static void getMonitorControl()"
        */
        Tests.increment();
        try {
            System.out.println("PriorityCeilingEmulationTest: "+
                               "getMonitorControl()");
            o = MonitorControl.getMonitorControl();
            // null is a valid return value here
            if( (o != null) && !(o instanceof MonitorControl) )
                throw new Exception("Return object is not instanceof "+
                                    "MonitorControl");
        } catch (Exception e) {
            System.out.println("PriorityCeilingEmulationTest: "+
                               "getMonitorControl() failed");
            Tests.fail("MonitorControl.getMonitorControl(ce)",e);
        }

        /* Subtest 3
        ** Method "public static void setMonitorControl(MonitorControl policy)"
        */
        Tests.increment();
        try {
            System.out.println("PriorityCeilingEmulationTest: "+
                               "setMonitorControl(MonitorControl)");
            MonitorControl.setMonitorControl(pce);
        } catch (Exception e) {
            System.out.println("PriorityCeilingEmulationTest: "+
                               "setMonitorControl(MonitorControl) failed");
            Tests.fail("MonitorControl.setMonitorControl(pce)",e);
        }

        /* Subtest 4
        ** Method "public static void setMonitorControl(MonitorControl
        ** policy)" where policy is null
        */
        Tests.increment();
        try {
            System.out.println("PriorityCeilingEmulationTest: "+
                               "setMonitorControl(MonitorControl)");
            MonitorControl.setMonitorControl(null);
        } catch (Exception e) {
            System.out.println("PriorityCeilingEmulationTest: "+
                               "setMonitorControl(MonitorControl) failed");
            Tests.fail("MonitorControl.setMonitorControl(null)",e);
        }

        /* Subtest 5
        ** Method "public static void setMonitorControl(java.lang.Object
        ** monitor, MonitorControl policy)"
        */
        Tests.increment();
        try {
            System.out.println("PriorityCeilingEmulationTest: "+
                               "setMonitorControl(Object,MonitorControl)");
            Object monitor = new Object();
            MonitorControl.setMonitorControl(monitor,pce);
        } catch (Exception e) {
            System.out.println("PriorityCeilingEmulationTest: "+
                               "setMonitorControl(Object,MonitorControl) "+
                               "failed");
            Tests.fail("MonitorControl.setMonitorControl(monitor, policy)",e);
        }

        /* Subtest 6
        ** Method "public static void setMonitorControl(java.lang.Object
        ** monitor, MonitorControl policy)"
        */
        Tests.increment();
        try {
            System.out.println("PriorityCeilingEmulationTest: "+
                               "setMonitorControl(null,null)");
            Object monitor = null;
            MonitorControl policy = null;
            MonitorControl.setMonitorControl(monitor, policy);
            System.out.println("PriorityCeilingEmulationTest: ");
        } catch (Exception e) {
            System.out.println("PriorityCeilingEmulationTest: "+
                               "setMonitorControl(null,null) failed");
            Tests.fail("MonitorControl.setMonitorControl(null,null)",e);
        }

        Tests.printSubTestReportTotals("PriorityCeilingEmulationTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
