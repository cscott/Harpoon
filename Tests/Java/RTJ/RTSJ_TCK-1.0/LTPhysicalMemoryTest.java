//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              LTPhysicalMemoryTest

Subtest 1:
        "public LTPhysicalMemory(Object type, long size)"

Subtest 2:
        "public LTPhysicalMemory(Object type, long base, long size)"

Subtest 3:
        "public LTPhysicalMemory(Object type, long base, long size,
                                 Runnable logic)"

Subtest 4:
        "public LTPhysicalMemory(Object type, long size, Runnable logic)"

Subtest 5:
        "public LTPhysicalMemory(Object type, long base, SizeEstimator size)"

Subtest 6:
        "public LTPhysicalMemory(Object type, long base, SizeEstimator size,
                                 Runnable logic)"

Subtest 7:
        "public LTPhysicalMemory(Object type, SizeEstimator size)"

Subtest 8:
        "public LTPhysicalMemory(Object type, SizeEstimator size,
                                 Runnable logic)"

*/


import javax.realtime.*;
import com.timesys.*;

public class LTPhysicalMemoryTest
{

    private static final long BASEADDR = 99*1024*1024;
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

        Tests.newTest("LTPhysicalMemoryTest");
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
        ** Constructor "public LTPhysicalMemory(Object type, long size)"
        */
        try {
            System.out.println("LTPhysicalMemoryTest: LTPhysicalMemory([1])");

            long size = 4096;

            LTPhysicalMemory vtm = new LTPhysicalMemory(type, size);
            base = base+size;

            if( !(vtm instanceof LTPhysicalMemory &&
                  vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "LTPhysicalMemory nor ScopedMemory");

        } catch (Exception e) {
            System.out.println("LTPhysicalMemoryTest: " +
                               "LTPhysicalMemory([1]) failed");
            Tests.fail("new LTPhysicalMemory([1])",e);
        }

        /* Subtest 2:
        ** Constructor "public LTPhysicalMemory(Object type, long base,
        **                                      long size)"
        */
        Tests.increment();
        try {
            System.out.println("LTPhysicalMemoryTest: LTPhysicalMemory([2])");

            long size = 4096;

            LTPhysicalMemory vtm = new LTPhysicalMemory(type, base, size);
            base = base+size;

            if( !(vtm instanceof LTPhysicalMemory &&
                  vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "LTPhysicalMemory nor ScopedMemory");

        } catch (Exception e) {
            System.out.println("LTPhysicalMemoryTest: " +
                               "LTPhysicalMemory([2]) failed");
            Tests.fail("new LTPhysicalMemory([2])",e);
        }

        /* Subtest 3:
        ** Constructor "public LTPhysicalMemory(Object type, long base,
        **                                      long size, Runnable logic)"
        */
        Tests.increment();
        try {
            System.out.println("LTPhysicalMemoryTest: LTPhysicalMemory([3])");

            long size = 4096;
            Runnable logic = testThread;

            LTPhysicalMemory vtm = new LTPhysicalMemory(type, base, size,
                                                        logic);
            base=base+size;
            if( !(vtm instanceof LTPhysicalMemory &&
                  vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "LTPhysicalMemory nor ScopedMemory");

        } catch (Exception e) {
            System.out.println("LTPhysicalMemoryTest: " +
                               "LTPhysicalMemory([3]) failed");
            Tests.fail("new LTPhysicalMemory([3])",e);
        }

        /* Subtest 4:
        ** Constructor "public LTPhysicalMemory(Object type,
        **                                      long size, Runnable logic)"
        */
        Tests.increment();
        try {
            System.out.println("LTPhysicalMemoryTest: LTPhysicalMemory([4])");

            long size = 4096;
            Runnable logic = testThread;

            LTPhysicalMemory vtm = new LTPhysicalMemory(type, size, logic);
            base = base+size;
            if( !(vtm instanceof LTPhysicalMemory &&
                  vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "LTPhysicalMemory nor ScopedMemory");

        } catch (Exception e) {
            System.out.println("LTPhysicalMemoryTest: " +
                               "LTPhysicalMemory([4]) failed");
            Tests.fail("new LTPhysicalMemory([4])",e);
        }

        /* Subtest 5:
        ** Constructor "public LTPhysicalMemory(Object type, long base,
        **                                      SizeEstimator size)"
        */
        Tests.increment();
        try {
            System.out.println("LTPhysicalMemoryTest: LTPhysicalMemory([5])");

            SizeEstimator sizeest = new SizeEstimator();
            sizeest.reserve(classobj,1000);

            LTPhysicalMemory vtm = new LTPhysicalMemory(type, base, sizeest);

            base = base+(4096*((int) sizeest.getEstimate()/4096)) + 4096;
            if( !(vtm instanceof LTPhysicalMemory &&
                  vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "LTPhysicalMemory nor ScopedMemory");

        } catch (Exception e) {
            System.out.println("LTPhysicalMemoryTest: " +
                               "LTPhysicalMemory([5]) failed");
            Tests.fail("new LTPhysicalMemory([5])",e);
        }

        /* Subtest 6:
        ** Constructor "public LTPhysicalMemory(Object type, long base,
        **                                      SizeEstimator size,
        **                                      Runnable logic)"
        */
        Tests.increment();
        try {
            System.out.println("LTPhysicalMemoryTest: LTPhysicalMemory([6])");

            SizeEstimator sizeest = new SizeEstimator();
            sizeest.reserve(classobj,1000);
            Runnable logic = testThread;

            LTPhysicalMemory vtm = new LTPhysicalMemory(type, base, sizeest,
                                                        logic);

            base = base+(4096*((int) sizeest.getEstimate()/4096)) + 4096;
            if( !(vtm instanceof LTPhysicalMemory &&
                  vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "LTPhysicalMemory nor ScopedMemory");

        } catch (Exception e) {
            System.out.println("LTPhysicalMemoryTest: " +
                               "LTPhysicalMemory([6]) failed");
            Tests.fail("new LTPhysicalMemory([6])",e);
        }

        /* Subtest 7:
        ** Constructor "public LTPhysicalMemory(Object type, SizeEst. size)"
        */
        Tests.increment();
        try {
            System.out.println("LTPhysicalMemoryTest: LTPhysicalMemory([7])");

            SizeEstimator sizeest = new SizeEstimator();
            sizeest.reserve(classobj,1000);

            LTPhysicalMemory vtm = new LTPhysicalMemory(type, sizeest);
            base = base+(4096*((int) sizeest.getEstimate()/4096)) + 4096;
            if( !(vtm instanceof LTPhysicalMemory &&
                  vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "LTPhysicalMemory nor ScopedMemory");

        } catch (Exception e) {
            System.out.println("LTPhysicalMemoryTest: " +
                               "LTPhysicalMemory([7]) failed");
            Tests.fail("new LTPhysicalMemory([7])",e);
        }

        /* Subtest 8:
        ** Constructor "public LTPhysicalMemory(Object type, SizeEst. size,
        **                                      Runnable logic)"
        */
        Tests.increment();
        try {
            System.out.println("LTPhysicalMemoryTest: LTPhysicalMemory([8])");

            SizeEstimator sizeest = new SizeEstimator();
            sizeest.reserve(classobj,1000);
            Runnable logic = testThread;

            LTPhysicalMemory vtm = new LTPhysicalMemory(type, sizeest, logic);
            base = base+(4096*((int) sizeest.getEstimate()/4096)) + 4096;
            if( !(vtm instanceof LTPhysicalMemory &&
                  vtm instanceof ScopedMemory) )
                throw new Exception("Return object is not instance of "+
                                    "LTPhysicalMemory nor ScopedMemory");

        } catch (Exception e) {
            System.out.println("LTPhysicalMemoryTest: " +
                               "LTPhysicalMemory([8]) failed");
            Tests.fail("new LTPhysicalMemory([8])",e);
        }
        PhysicalMemoryManager.removeFilter(type);
        Tests.printSubTestReportTotals("LTPhysicalMemoryTest");

    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
