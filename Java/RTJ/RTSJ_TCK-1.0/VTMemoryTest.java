//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              VTMemoryTest

Subtest 1:
        "public VTMemory(int initial, int maximum)"

Subtest NEW:
        "public VTMemory(long initialSizeInBytes, long maxSizeInBytes,
                         Runnable logic)

Subtest NEW:
        "public VTMemory(SizeEstimator initial, SizeEstimator maximum)

Subtest NEW:
        "public VTMemory(SizeEstimator initial, SizeEstimator maximum,
                         Runnable logic)
*/


import javax.realtime.*;

public class VTMemoryTest
{

    public static void run()
    {
        VTMemory vtm = null;
        Object o = null;

        Thread testThread = new Thread(){
                public void run() {
                    System.out.println("Inside of RealtimeThread");
                }
            };

        Class classobj;

        Tests.newTest("VTMemoryTest");
        Tests.increment();

        /* SETUP */
        try {
            classobj = Class.forName("java.lang.Object");
        }
        catch (Exception e) {
            System.out.println("Unable to create class object for reserve");
            Tests.fail("VTPhyicalMemoryTest");
            return;
        }


        /* Subtest 1:
        ** Constructor "public VTMemory(int initial, int maximum)"
        */
        try {
            System.out.println("VTMemoryTest: VTMemory(int,int)");
            int initial = 1000;
            int maximum = 2000;
            vtm = new VTMemory(initial, maximum);
            if( !(vtm instanceof VTMemory && vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "VTMemory nor ScopedMemory");
        } catch (Exception e) {
            System.out.println("VTMemoryTest: VTMemory(int,int) failed");
            Tests.fail("new VTmemory(1000,2000)",e);
        }

        /* Subtest NEW:
        ** Constructor "public VTMemory(long initialSizeInBytes,
        ** long maxSizeInBytes, Runnable logic)
        */
        Tests.increment();
        try {
            System.out.println("VTMemoryTest: VTMemory(long,long,Runnable)");
            int initial = 1000;
            int maximum = 2000;
            Runnable logic = testThread;
            vtm = new VTMemory(initial, maximum, logic);
            if( !(vtm instanceof VTMemory && vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instanceof "+
                                    "VTMemory nor ScopedMemory");
        } catch (Exception e) {
            System.out.println("VTMemoryTest: VTMemory(long,long,Runnable) failed");
            Tests.fail("new VTMemory(initial, maximum, logic)",e);
        }

        /* Subtest NEW:
        ** Constructor "public VTMemory(SizeEstimator initial,
        **                              SizeEstimator maximum)
        */
        Tests.increment();
        try {
            System.out.println("VTMemoryTest: VTMemory(SizeEst,SizeEst)");
            SizeEstimator initial = new SizeEstimator();
            SizeEstimator maximum = new SizeEstimator();
            initial.reserve(classobj, 1000);
            maximum.reserve(classobj, 100);
            vtm = new VTMemory(initial, maximum);
            if( !(vtm instanceof VTMemory && vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instanceof "+
                                    "VTMemory nor ScopedMemory");
        } catch (Exception e) {
            System.out.println("VTMemoryTest: VTMemory(SizeEst,SizeEst) failed");
            Tests.fail("new VTMemory(initial, maximum)",e);
        }

        /* Subtest NEW:
        ** Constructor "public VTMemory(SizeEstimator initial,
        **                              SizeEstimator maximum,
        **                              Runnable logic)
        */
        Tests.increment();
        try {
            System.out.println("VTMemoryTest: VTMemory(SizeEst,SizeEst,Runnable)");
            SizeEstimator initial = new SizeEstimator();
            SizeEstimator maximum = new SizeEstimator();
            Runnable logic = testThread;
            initial.reserve(classobj, 1000);
            maximum.reserve(classobj, 100);
            vtm = new VTMemory(initial, maximum,logic);
            if( !(vtm instanceof VTMemory && vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instanceof "+
                                    "VTMemory nor ScopedMemory");
        } catch (Exception e) {
            System.out.println("VTMemoryTest: VTMemory(SizeEst,SizeEst,Runnable) failed");
            Tests.fail("new VTMemory(initial, maximum, logic)",e);
        }

        Tests.printSubTestReportTotals("VTMemoryTest");

    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
