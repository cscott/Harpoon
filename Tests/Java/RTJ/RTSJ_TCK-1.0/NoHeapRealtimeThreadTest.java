//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              NoHeapRealtimeThreadTest

Subtest 1:
        "public NoHeapRealtimeThread(SchedulingParameters scheduling,
        MemoryArea area)" where area is ScopeMemory

Subtest 2:
        "public NoHeapRealtimeThread(SchedulingParameters scheduling,
        MemoryArea area)" where area is ImmortalMemory

Subtest 3:
        "public NoHeapRealtimeThread(SchedulingParameters scheduling,
        MemoryArea area)" where area is HeapMemory
        ** IllegalArgumentException is expected

Subtest 4:
        "public NoHeapRealtimeThread(SchedulingParameters scheduling,
        MemoryArea area)" where area is null
        ** IllegalArgumentException is expected

Subtest 5:
        "public NoHeapRealtimeThread(SchedulingParameters scheduling,
        MemoryArea area)" where scheduling is null

Subtest 6:
        "public NoHeapRealtimeThread(SchedulingParameters scheduling,
        ReleaseParameters release, MemoryArea area)" where area is ScopeMemory

Subtest 7:
        "public NoHeapRealtimeThread(SchedulingParameters scheduling,
        ReleaseParameters release, MemoryArea area)" where area is
        ImmortalMemory

Subtest 8:
        "public NoHeapRealtimeThread(SchedulingParameters scheduling,
        ReleaseParameters release, MemoryArea area)" where area is HeapMemory
        ** IllegalArgumentException is expected

Subtest 9:
        "public NoHeapRealtimeThread(SchedulingParameters scheduling,
        ReleaseParameters release, MemoryArea area)" where area is null
        ** IllegalArgumentException is expected

Subtest 10:
        "public NoHeapRealtimeThread(SchedulingParameters scheduling,
        ReleaseParameters release, MemoryArea area)" where scheduling is null
        and ReleaseParameters is null

Subtest 11:
        "public NoHeapRealtimeThread(SchedulingParameters scheduling,
        ReleaseParameters release, MemoryParameters memory, MemoryArea area,
        ProcessingGroupParameters group, java.lang.Runnable logic)" where area
        is ScopeMemory

Subtest 12:
        "public NoHeapRealtimeThread(SchedulingParameters scheduling,
        ReleaseParameters release, MemoryParameters memory, MemoryArea area,
        ProcessingGroupParameters group, java.lang.Runnable logic)" where area
        is ImmortalMemory

Subtest 13:
        "public NoHeapRealtimeThread(SchedulingParameters scheduling,
        ReleaseParameters release, MemoryParameters memory, MemoryArea area,
        ProcessingGroupParameters group, java.lang.Runnable logic)" where area
        is HeapMemory
        ** IllegalArgumentException is expected

Subtest 14:
       "public NoHeapRealtimeThread(SchedulingParameters scheduling,
       ReleaseParameters release, MemoryParameters memory, MemoryArea area,
       ProcessingGroupParameters group, java.lang.Runnable logic)" where area
       is null
       ** IllegalArgumentException is expected

Subtest 15:
        "public NoHeapRealtimeThread(SchedulingParameters scheduling,
        ReleaseParameters release, MemoryParameters memory, MemoryArea area,
        ProcessingGroupParameters group, java.lang.Runnable logic)" where
        scheduling, release, memory, and group are null
*/

import javax.realtime.*;

public class NoHeapRealtimeThreadTest
{

    public static void run()
    {
        NoHeapRealtimeThread nhrtt = null;
        Object o = null;
        Tests.newTest("NoHeapRealtimeThreadTest");
        final PriorityParameters pp = new PriorityParameters(10);
        final LTMemory ltm = new LTMemory(1048576,1048576);
        final ImmortalMemory im = ImmortalMemory.instance();
        final HeapMemory hm = HeapMemory.instance();

        final AperiodicParameters ap = new AperiodicParameters(new
                 RelativeTime(1000L,100),new RelativeTime(500L,0), null, null);

        final MemoryParameters mp = new MemoryParameters(MemoryParameters.
                                               NO_MAX,MemoryParameters.NO_MAX);

        final ProcessingGroupParameters pgp = new ProcessingGroupParameters(
        new RelativeTime(2000L,0),new RelativeTime(2000L,0),new RelativeTime(
        500L,0),new RelativeTime(100L,0),null,null);

        Thread myThread = new Thread() {
                public void run() {
                    System.out.println("**** Inside of myThread");
                }
            };


        /* Subtest 1:
        ** Constructor "public NoHeapRealtimeThread(SchedulingParameters
        ** scheduling, MemoryArea area)" where area is ScopeMemory
        */
        Tests.increment();
        try {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,MemoryArea)");
            o = new NoHeapRealtimeThread(pp,ltm);
            if (! (o instanceof NoHeapRealtimeThread) )
                throw new Exception("Return object is not instanceof "+
                                    "NoHeapRealtimeThread");

        } catch (Exception e) {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,MemoryArea) failed");
            Tests.fail("new NoHeapRealtimeThread(pp,ltm)",e);
        }

        /* Subtest 2:
        ** Constructor "public NoHeapRealtimeThread(SchedulingParameters
        ** scheduling, MemoryArea area)" where area is ImmortalMemory
        */
        Tests.increment();
        try {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,MemoryArea)");
            o = new NoHeapRealtimeThread(pp,im);
            if (! (o instanceof NoHeapRealtimeThread) )
                throw new Exception("Return object is not instanceof "+
                                    "NoHeapRealtimeThread");
        } catch (Exception e) {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,MemoryArea) failed");
            Tests.fail("new NoHeapRealtimeThread(pp,im)",e);
        }

        /* Subtest 3:
        ** Constructor "public NoHeapRealtimeThread(SchedulingParameters
        ** scheduling, MemoryArea area)" where area is HeapMemory
        ** IllegalArgumentException is expected
        */
        Tests.increment();
        try {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,MemoryArea)");
            o = new NoHeapRealtimeThread(pp,hm);
            Tests.fail("new NoHeapRealtimeThread(pp,hm) did not throw "+
                       "expected exception");
        } catch (Exception e) {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,MemoryArea) "+
                               " threw exception");
            if (! (e instanceof IllegalArgumentException) )
                Tests.fail("new NoHeapRealtimeThread(pp,hm) threw an "+
                           "exception other than IllegalArgumentException",e);
            else
                System.out.println("...as expected.");
        }

        /* Subtest 4:
        ** Constructor "public NoHeapRealtimeThread(SchedulingParameters
        ** scheduling, MemoryArea area)" where area is null
        ** IllegalArgumentException is expected
        */
        Tests.increment();
        try {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,MemoryArea)");
            o = new NoHeapRealtimeThread(pp,null);
            Tests.fail("new NoHeapRealtimeThread(pp,null) did not throw "+
                       "expected exception");
        } catch (Exception e) {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,MemoryArea) "+
                               " threw exception");
            if (! (e instanceof IllegalArgumentException) )
                Tests.fail("new NoHeapRealtimeThread(pp,null) threw an "+
                           "exception other than IllegalArgumentException",e);
            else
                System.out.println("...as expected.");
        }

        /* Subtest 5:
        ** Constructor "public NoHeapRealtimeThread(SchedulingParameters
        ** scheduling, MemoryArea area)" where scheduling is null
        */
        Tests.increment();
        try {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,MemoryArea)");
            o = new NoHeapRealtimeThread(null,ltm);
            if (! (o instanceof NoHeapRealtimeThread) )
                throw new Exception("Return object is not instanceof "+
                                    "NoHeapRealtimeThread");

        } catch (Exception e) {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,MemoryArea) failed");
            Tests.fail("new NoHeapRealtimeThread(null,ltm)",e);
        }

        /* Subtest 6:
        ** Constructor "public NoHeapRealtimeThread(SchedulingParameters
        ** scheduling, ReleaseParameters release, MemoryArea area)" where area
        ** is ScopeMemory
        */
        Tests.increment();
        try {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryArea)");
            o = new NoHeapRealtimeThread(pp,ap,ltm);
            if (! (o instanceof NoHeapRealtimeThread) )
                throw new Exception("Return object is not instanceof "+
                                    "NoHeapRealtimeThread");

        } catch (Exception e) {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryArea) failed");
            Tests.fail("new NoHeapRealtimeThread(pp,ap,ltm)",e);
        }

        /* Subtest 7:
        ** Constructor "public NoHeapRealtimeThread(SchedulingParameters
        ** scheduling, ReleaseParameters release, MemoryArea area)" where area
        ** is ImmortalMemory
        */
        Tests.increment();
        try {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryArea)");
            o = new NoHeapRealtimeThread(pp,ap,im);
            if (! (o instanceof NoHeapRealtimeThread) )
                throw new Exception("Return object is not instanceof "+
                                    "NoHeapRealtimeThread");
        } catch (Exception e) {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryArea) failed");
            Tests.fail("new NoHeapRealtimeThread(pp,ap,im)",e);
        }

        /* Subtest 8:
        ** Constructor "public NoHeapRealtimeThread(SchedulingParameters
        ** scheduling, ReleaseParameters release, MemoryArea area)" where area
        ** is HeapMemory
        ** IllegalArgumentException is expected
        */
        Tests.increment();
        try {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryArea)");
            o = new NoHeapRealtimeThread(pp,ap,hm);
            Tests.fail("new NoHeapRealtimeThread(pp,ap,hm) did not throw "+
                       "expected exception");
        } catch (Exception e) {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryArea) threw exception");
            if (! (e instanceof IllegalArgumentException) )
                Tests.fail("new NoHeapRealtimeThread(pp,ap,hm) threw an "+
                           "exception other than IllegalArgumentException",e);
            else
                System.out.println("...as expected.");
        }

        /* Subtest 9:
        ** Constructor "public NoHeapRealtimeThread(SchedulingParameters
        ** scheduling, ReleaseParameters release, MemoryArea area)" where area
        ** is null
        ** IllegalArgumentException is expected
        */
        Tests.increment();
        try {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryArea)");
            o = new NoHeapRealtimeThread(pp,ap,null);
            Tests.fail("new NoHeapRealtimeThread(pp,ap,null) did not throw "+
                       "expected exception");
        } catch (Exception e) {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryArea) threw exception");
            if (! (e instanceof IllegalArgumentException) )
                Tests.fail("new NoHeapRealtimeThread(pp,ap,null) threw an "+
                           "exception other than IllegalArgumentException",e);
            else
                System.out.println("...as expected.");
        }

        /* Subtest 10:
        ** Constructor "public NoHeapRealtimeThread(SchedulingParameters
        ** scheduling, ReleaseParameters release, MemoryArea area)" where
        ** scheduling is null and ReleaseParameters is null
        */
        Tests.increment();
        try {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryArea)");
            o = new NoHeapRealtimeThread(null,null,ltm);
            if (! (o instanceof NoHeapRealtimeThread) )
                throw new Exception("Return object is not instanceof "+
                                    "NoHeapRealtimeThread");

        } catch (Exception e) {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryArea) failed");
            Tests.fail("new NoHeapRealtimeThread(null,null,ltm)",e);
        }

        /* Subtest 11:
        ** Constructor "public NoHeapRealtimeThread(SchedulingParameters
        ** scheduling, ReleaseParameters release, MemoryParameters memory,
        ** MemoryArea area, ProcessingGroupParameters group,
        ** java.lang.Runnable logic)" where area is ScopeMemory
        */
        Tests.increment();
        try {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryParameters,MemoryArea,"+
                               "ProcessingGroupParameters,"+
                               "java.lang.Runnable)");
            o = new NoHeapRealtimeThread(pp,ap,mp,ltm,pgp,myThread);
            if (! (o instanceof NoHeapRealtimeThread) )
                throw new Exception("Return object is not instanceof "+
                                    "NoHeapRealtimeThread");

        } catch (Exception e) {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryParameters,MemoryArea,"+
                               "ProcessingGroupParameters,"+
                               "java.lang.Runnable) failed");
            Tests.fail("new NoHeapRealtimeThread(pp,ap,mp,ltm,pgp,myThread)",
                       e);
        }

        /* Subtest 12:
        ** Constructor "public NoHeapRealtimeThread(SchedulingParameters
        ** scheduling, ReleaseParameters release, MemoryParameters memory,
        ** MemoryArea area, ProcessingGroupParameters group, java.lang.Runnable
        ** logic)" where area is ImmortalMemory
        */
        Tests.increment();
        try {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryParameters,MemoryArea,"+
                               "ProcessingGroupParameters,"+
                               "java.lang.Runnable)");
            o = new NoHeapRealtimeThread(pp,ap,mp,im,pgp,myThread);
            if (! (o instanceof NoHeapRealtimeThread) )
                throw new Exception("Return object is not instanceof "+
                                    "NoHeapRealtimeThread");
        } catch (Exception e) {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryParameters,MemoryArea,"+
                               "ProcessingGroupParameters,"+
                               "java.lang.Runnable) failed");
            Tests.fail("new NoHeapRealtimeThread(pp,ap,mp,im,pgp,myThread)",e);
        }

        /* Subtest 13:
        ** Constructor "public NoHeapRealtimeThread(SchedulingParameters
        ** scheduling, ReleaseParameters release, MemoryParameters memory,
        ** MemoryArea area, ProcessingGroupParameters group,
        ** java.lang.Runnable logic)" where area is HeapMemory
        ** IllegalArgumentException is expected
        */
        Tests.increment();
        try {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryParameters,MemoryArea,"+
                               "ProcessingGroupParameters,"+
                               "java.lang.Runnable)");
            o = new NoHeapRealtimeThread(pp,ap,mp,hm,pgp,myThread);
            Tests.fail("new NoHeapRealtimeThread(pp,ap,mp,hm,pgp,myThread) "+
                       "did not throw expected exception");
        } catch (Exception e) {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryParameters,MemoryArea,"+
                               "ProcessingGroupParameters,"+
                               "java.lang.Runnable) failed");
            if (! (e instanceof IllegalArgumentException) )
                Tests.fail("new NoHeapRealtimeThread(pp,ap,mp,hm,pgp,"+
                           "myThread) threw an exception other than "+
                           "IllegalArgumentException",e);
            else
                System.out.println("...as expected.");
        }

        /* Subtest 14:
        ** Constructor "public NoHeapRealtimeThread(SchedulingParameters
        ** scheduling, ReleaseParameters release, MemoryParameters memory,
        ** MemoryArea area, ProcessingGroupParameters group,
        ** java.lang.Runnable logic)" where area is null
        ** IllegalArgumentException is expected
        */
        Tests.increment();
        try {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryParameters,null,"+
                               "ProcessingGroupParameters,"+
                               "java.lang.Runnable)");
            o = new NoHeapRealtimeThread(pp,ap,mp,null,pgp,myThread);
            Tests.fail("new NoHeapRealtimeThread(pp,ap,mp,null,pgp,myThread) "+
                       "did not throw expected exception");
        } catch (Exception e) {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryParameters,null,"+
                               "ProcessingGroupParameters,"+
                               "java.lang.Runnable) threw exception");
            if (! (e instanceof IllegalArgumentException) )
                Tests.fail("new NoHeapRealtimeThread(pp,ap,mp,null,pgp,"+
                           "myThread) threw an exception other than "+
                           "IllegalArgumentException",e);
            else
                System.out.println("...as expected.");
        }

        /* Subtest 15:
        ** Constructor "public NoHeapRealtimeThread(SchedulingParameters
        ** scheduling, ReleaseParameters release, MemoryParameters memory,
        ** MemoryArea area, ProcessingGroupParameters group,
        ** java.lang.Runnable logic)" where scheduling, release, memory, and
        ** group are null
        */
        Tests.increment();
        try {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "null,null,null,MemoryArea area,null,"+
                               "java.lang.Runnable)");
            o = new NoHeapRealtimeThread(null,null,null,ltm,null,myThread);
            if (! (o instanceof NoHeapRealtimeThread) )
                throw new Exception("Return object is not instanceof "+
                                    "NoHeapRealtimeThread");

        } catch (Exception e) {
            System.out.println("NoHeapRealtimeTest: NoHeapRealtimeThread("+
                               "null,null,null,MemoryArea area,null,"+
                               "java.lang.Runnable) failed");
            Tests.fail("new NoHeapRealtimeThread(null,null,null,ltm,null,"+
                       "myThread)",e);
        }


        Tests.printSubTestReportTotals("NoHeapRealtimeThreadTest");

    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}


