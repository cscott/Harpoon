//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              VTPhysicalMemoryTest

Subtest 1:
        "public VTPhysicalMemory(Object type, long size)"

Subtest 2:
        "public VTPhysicalMemory(Object type, long base, long size)"

Subtest 3:
        "public VTPhysicalMemory(Object type, long base, long size,
                                 Runnable logic)"

Subtest 4:
        "public VTPhysicalMemory(Object type, long size, Runnable logic)"

Subtest 5:
        "public VTPhysicalMemory(Object type, long base, SizeEstimator size)"

Subtest 6:
        "public VTPhysicalMemory(Object type, long base, SizeEstimator size,
                                 Runnable logic)"

Subtest 7:
        "public VTPhysicalMemory(Object type, SizeEstimator size)"

Subtest 8:
        "public VTPhysicalMemory(Object type, SizeEstimator size,
                                 Runnable logic)"

*/


import javax.realtime.*;
import com.timesys.*;

public class VTPhysicalMemoryTest
{

    private static final long BASEADDR = 98*1024*1024;
    private static final long SIZE = 4096*32;
    
    public static void run()
    {
        Object o = null;
        Object type = new Object();
        long base = BASEADDR;
        PhysicalMemoryTypeFilter filter = null;

        Thread testThread = new Thread(){
                public void run() {
                    System.out.println("Inside of RealtimeThread");
                }
            };

        Class classobj;

        Tests.newTest("VTPhysicalMemoryTest");
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

        /* SETUP for PhysicalMemoryManager */
        try
        {
            filter = new DefaultPhysicalMemoryFilter(BASEADDR, SIZE);
            PhysicalMemoryManager.registerFilter(type,filter);
        }
        catch(Exception e)
        {
            System.out.println("Exception registering filter:" + e);
            e.printStackTrace();
            Tests.fail("VTPhyicalMemoryTest");
            return;
        }




        /* Subtest 1:
        ** Constructor "public VTPhysicalMemory(Object type, long size)"
        */
        try {
            System.out.println("VTPhysicalMemoryTest: VTPhysicalMemory([1])");

            long size = 4096;

            VTPhysicalMemory vtm = new VTPhysicalMemory(type, size);
            base = base+size;

            if( !(vtm instanceof VTPhysicalMemory &&
                  vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "VTPhysicalMemory nor ScopedMemory");

        } catch (Exception e) {
            System.out.println("VTPhysicalMemoryTest: " +
                               "VTPhysicalMemory([1]) failed");
            Tests.fail("new VTPhysicalMemory([1])",e);
        }

        /* Subtest 2:
        ** Constructor "public VTPhysicalMemory(Object type, long base,
        **                                      long size)"
        */
        Tests.increment();
        try {
            System.out.println("VTPhysicalMemoryTest: VTPhysicalMemory([2])");

            long size = 4096;

            VTPhysicalMemory vtm = new VTPhysicalMemory(type, base, size);
            base = base+size;

            if( !(vtm instanceof VTPhysicalMemory &&
                  vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "VTPhysicalMemory nor ScopedMemory");

        } catch (Exception e) {
            System.out.println("VTPhysicalMemoryTest: " +
                               "VTPhysicalMemory([2]) failed");
            Tests.fail("new VTPhysicalMemory([2])",e);
        }

        /* Subtest 3:
        ** Constructor "public VTPhysicalMemory(Object type, long base,
        **                                      long size, Runnable logic)"
        */
        Tests.increment();
        try {
            System.out.println("VTPhysicalMemoryTest: VTPhysicalMemory([3])");

            long size = 4096;
            Runnable logic = testThread;

            VTPhysicalMemory vtm = new VTPhysicalMemory(type, base, size,
                                                        logic);
            base=base+size;
            if( !(vtm instanceof VTPhysicalMemory &&
                  vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "VTPhysicalMemory nor ScopedMemory");

        } catch (Exception e) {
            System.out.println("VTPhysicalMemoryTest: " +
                               "VTPhysicalMemory([3]) failed");
            Tests.fail("new VTPhysicalMemory([3])",e);
        }

        /* Subtest 4:
        ** Constructor "public VTPhysicalMemory(Object type,
        **                                      long size, Runnable logic)"
        */
        Tests.increment();
        try {
            System.out.println("VTPhysicalMemoryTest: VTPhysicalMemory([4])");

            long size = 4096;
            Runnable logic = testThread;

            VTPhysicalMemory vtm = new VTPhysicalMemory(type, size, logic);
            base = base+size;
            if( !(vtm instanceof VTPhysicalMemory &&
                  vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "VTPhysicalMemory nor ScopedMemory");

        } catch (Exception e) {
            System.out.println("VTPhysicalMemoryTest: " +
                               "VTPhysicalMemory([4]) failed");
            Tests.fail("new VTPhysicalMemory([4])",e);
        }

        /* Subtest 5:
        ** Constructor "public VTPhysicalMemory(Object type, long base,
        **                                      SizeEstimator size)"
        */
        Tests.increment();
        try {
            System.out.println("VTPhysicalMemoryTest: VTPhysicalMemory([5])");

            SizeEstimator sizeest = new SizeEstimator();
            sizeest.reserve(classobj,1000);

            VTPhysicalMemory vtm = new VTPhysicalMemory(type, base, sizeest);

            base = base+(4096*((int) sizeest.getEstimate()/4096)) + 4096;
            if( !(vtm instanceof VTPhysicalMemory &&
                  vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "VTPhysicalMemory nor ScopedMemory");

        } catch (Exception e) {
            System.out.println("VTPhysicalMemoryTest: " +
                               "VTPhysicalMemory([5]) failed");
            Tests.fail("new VTPhysicalMemory([5])",e);
        }

        /* Subtest 6:
        ** Constructor "public VTPhysicalMemory(Object type, long base,
        **                                      SizeEstimator size,
        **                                      Runnable logic)"
        */
        Tests.increment();
        try {
            System.out.println("VTPhysicalMemoryTest: VTPhysicalMemory([6])");

            SizeEstimator sizeest = new SizeEstimator();
            sizeest.reserve(classobj,1000);
            Runnable logic = testThread;

            VTPhysicalMemory vtm = new VTPhysicalMemory(type, base, sizeest,
                                                        logic);

            //base = base+(2*sizeest.getEstimate());
            base = base+(4096*((int) sizeest.getEstimate()/4096)) + 4096;
            if( !(vtm instanceof VTPhysicalMemory &&
                  vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "VTPhysicalMemory nor ScopedMemory");

        } catch (Exception e) {
            System.out.println("VTPhysicalMemoryTest: " +
                               "VTPhysicalMemory([6]) failed");
            Tests.fail("new VTPhysicalMemory([6])",e);
        }

        /* Subtest 7:
        ** Constructor "public VTPhysicalMemory(Object type, SizeEst. size)"
        */
        Tests.increment();
        try {
            System.out.println("VTPhysicalMemoryTest: VTPhysicalMemory([7])");

            SizeEstimator sizeest = new SizeEstimator();
            sizeest.reserve(classobj,1000);

            VTPhysicalMemory vtm = new VTPhysicalMemory(type, sizeest);
            base = base+(4096*((int) sizeest.getEstimate()/4096)) + 4096;
            if( !(vtm instanceof VTPhysicalMemory &&
                  vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "VTPhysicalMemory nor ScopedMemory");

        } catch (Exception e) {
            System.out.println("VTPhysicalMemoryTest: " +
                               "VTPhysicalMemory([7]) failed");
            Tests.fail("new VTPhysicalMemory([7])",e);
        }

        /* Subtest 8:
        ** Constructor "public VTPhysicalMemory(Object type, SizeEst. size,
        **                                      Runnable logic)"
        */
        Tests.increment();
        try {
            System.out.println("VTPhysicalMemoryTest: VTPhysicalMemory([8])");

            SizeEstimator sizeest = new SizeEstimator();
            sizeest.reserve(classobj,1000);
            Runnable logic = testThread;

            VTPhysicalMemory vtm = new VTPhysicalMemory(type, sizeest, logic);
            base = base+(4096*((int) sizeest.getEstimate()/4096)) + 4096;
            if( !(vtm instanceof VTPhysicalMemory &&
                  vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "VTPhysicalMemory nor ScopedMemory");

        } catch (Exception e) {
            System.out.println("VTPhysicalMemoryTest: " +
                               "VTPhysicalMemory([8]) failed");
            Tests.fail("new VTPhysicalMemory([8])",e);
        }
        PhysicalMemoryManager.removeFilter(type);
        Tests.printSubTestReportTotals("VTPhysicalMemoryTest");

    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
