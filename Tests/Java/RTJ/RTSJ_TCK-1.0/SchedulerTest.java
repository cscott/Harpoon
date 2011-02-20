//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              SchedulerTest

Subtest 1:
        "public boolean setIfFeasible(Schedulable schedulable,
        ReleaseParameters release, MemoryParameters memory)

Subtest 2:
        "public static void setDefaultScheduler(Scheduler scheduler)"

Subtest 3:
        "public static Scheduler getDefaultScheduler()"
*/

import javax.realtime.*;
import java.lang.reflect.*;
import java.util.*;

public class SchedulerTest
{

    public static void run() {

        /*
        ** This test uses a test subclass called NewScheduler which
        ** extends Scheduler.  The purpose of this is to test
        ** non abstract methods that are not overridden
        */

        Tests.newTest("SchedulerTest");
        Object o;
        PriorityParameters pp = new PriorityParameters(10);
        AperiodicParameters ap = new AperiodicParameters(new
            RelativeTime(1000L,100),new RelativeTime(500L,0),null,null);

        /* Subtest 1:
        ** Method "public boolean setIfFeasible(Schedulable schedulable,
        ** ReleaseParameters release, MemoryParameters memory)
        */
        Tests.increment();
        try {
            System.out.println("SchedulerTest: setIfFeasible(Schedulable,"+
                               "ReleaseParameters,MemoryParameters)");
            NewScheduler ns = new NewScheduler();
            AperiodicParameters release = new AperiodicParameters(new
                RelativeTime(1000L,100),new RelativeTime(3000L,0),new
                    AEventHandler(), new AEventHandler());
            MemoryParameters memory = new MemoryParameters(60000L,60000L);
            RTThread rt = new RTThread(pp,ap);
            boolean b = ns.setIfFeasible(rt, release, memory);
        } catch (Exception e) {
            System.out.println("SchedulerTest: setIfFeasible(Schedulable,"+
                               "ReleaseParameters,MemoryParameters) failed");
            Tests.fail("ns.setIfFeasible(rt,release,memory)",e);
        }

        /* Subtest 2:
        ** Method "public static void setDefaultScheduler(Scheduler scheduler)"
        */
        Tests.increment();
        try {
            System.out.println("SchedulerTest: setDefaultScheduler("+
                               "Scheduler)");
            NewScheduler ns = new NewScheduler();
            Scheduler.setDefaultScheduler(ns);
        } catch (Exception e) {
            System.out.println("SchedulerTest: setDefaultScheduler("+
                               "Scheduler) failed");
            Tests.fail("Scheduler.setDefaultScheduler(ns)");
        }

        /* Subtest 3:
        ** Method "public static Scheduler getDefaultScheduler()"
        */
        Tests.increment();
        try {
            System.out.println("SchedulerTest: getDefaultScheduler()");
            NewScheduler ns = new NewScheduler();
            o = Scheduler.getDefaultScheduler();
            if (! (o instanceof Scheduler))
                throw new Exception("Return object is not instanceof "+
                                    "Scheduler");
        } catch (Exception e) {
            System.out.println("SchedulerTest: getDefaultScheduler() failed");
            Tests.fail("ns.getDefaultScheduler()",e);
        }



        Tests.printSubTestReportTotals("SchedulerTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
