//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              ImmortalPhysicalMemoryTest

Subtest 1:
        "public ImmortalPhysicalMemory(Object type, long size)"

Subtest 2:
        "public ImmortalPhysicalMemory(Object type, long base, long size)"

Subtest 3:
        "public ImmortalPhysicalMemory(Object type, long base, long size,
                                 Runnable logic)"

Subtest 4:
        "public ImmortalPhysicalMemory(Object type, long size, Runnable logic)"

Subtest 5:
        "public ImmortalPhysicalMemory(Object type, long base, SizeEstimator size)"

Subtest 6:
        "public ImmortalPhysicalMemory(Object type, long base, SizeEstimator size,
                                 Runnable logic)"

Subtest 7:
        "public ImmortalPhysicalMemory(Object type, SizeEstimator size)"

Subtest 8:
        "public ImmortalPhysicalMemory(Object type, SizeEstimator size,
                                 Runnable logic)"

*/


import javax.realtime.*;
import com.timesys.*;

public class ImmortalPhysicalMemoryTest
{

    private static final long BASEADDR = 100663296;
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

        Tests.newTest("ImmortalPhysicalMemoryTest");
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
            Tests.fail("ImmortalPhyicalMemoryTest");
            return;
        }


        /* Subtest 1:
        ** Constructor "public ImmortalPhysicalMemory(Object type, long size)"
        */
        try {
            System.out.println("ImmortalPhysicalMemoryTest: ImmortalPhysicalMemory([1])");

            long size = 4096;

            ImmortalPhysicalMemory vtm = new ImmortalPhysicalMemory(type, size);
            base = base+size;

            if( !(vtm instanceof ImmortalPhysicalMemory &&
                  vtm instanceof MemoryArea) )
                throw new Exception("Return object is not instance of "+
                                    "ImmortalPhysicalMemory nor MemoryArea");

        } catch (Exception e) {
            System.out.println("ImmortalPhysicalMemoryTest: " +
                               "ImmortalPhysicalMemory([1]) failed");
            Tests.fail("new ImmortalPhysicalMemory([1])",e);
        }

        /* Subtest 2:
        ** Constructor "public ImmortalPhysicalMemory(Object type, long base,
        **                                      long size)"
        */
        Tests.increment();
        try {
            System.out.println("ImmortalPhysicalMemoryTest: ImmortalPhysicalMemory([2])");

            long size = 4096;

            ImmortalPhysicalMemory vtm = new ImmortalPhysicalMemory(type, base, size);
            base = base+size;

            if( !(vtm instanceof ImmortalPhysicalMemory &&
                  vtm instanceof MemoryArea) )
                throw new Exception("Return object is not instance of "+
                                    "ImmortalPhysicalMemory nor MemoryArea");

        } catch (Exception e) {
            System.out.println("ImmortalPhysicalMemoryTest: " +
                               "ImmortalPhysicalMemory([2]) failed");
            Tests.fail("new ImmortalPhysicalMemory([2])",e);
        }

        /* Subtest 3:
        ** Constructor "public ImmortalPhysicalMemory(Object type, long base,
        **                                      long size, Runnable logic)"
        */
        Tests.increment();
        try {
            System.out.println("ImmortalPhysicalMemoryTest: ImmortalPhysicalMemory([3])");

            long size = 4096;
            Runnable logic = testThread;

            ImmortalPhysicalMemory vtm = new ImmortalPhysicalMemory(type, base, size,
                                                        logic);
            base = base+size;
            if( !(vtm instanceof ImmortalPhysicalMemory &&
                  vtm instanceof MemoryArea) )
                throw new Exception("Return object is not instance of "+
                                    "ImmortalPhysicalMemory nor MemoryArea");

        } catch (Exception e) {
            System.out.println("ImmortalPhysicalMemoryTest: " +
                               "ImmortalPhysicalMemory([3]) failed");
            Tests.fail("new ImmortalPhysicalMemory([3])",e);
        }

        /* Subtest 4:
        ** Constructor "public ImmortalPhysicalMemory(Object type,
        **                                      long size, Runnable logic)"
        */
        Tests.increment();
        try {
            System.out.println("ImmortalPhysicalMemoryTest: ImmortalPhysicalMemory([4])");

            long size = 4096;
            Runnable logic = testThread;

            ImmortalPhysicalMemory vtm = new ImmortalPhysicalMemory(type, size, logic);
            base = base+size;
            if( !(vtm instanceof ImmortalPhysicalMemory &&
                  vtm instanceof MemoryArea) )
                throw new Exception("Return object is not instance of "+
                                    "ImmortalPhysicalMemory nor MemoryArea");

        } catch (Exception e) {
            System.out.println("ImmortalPhysicalMemoryTest: " +
                               "ImmortalPhysicalMemory([4]) failed");
            Tests.fail("new ImmortalPhysicalMemory([4])",e);
        }

        /* Subtest 5:
        ** Constructor "public ImmortalPhysicalMemory(Object type, long base,
        **                                      SizeEstimator size)"
        */
        Tests.increment();
        try {
            System.out.println("ImmortalPhysicalMemoryTest: ImmortalPhysicalMemory([5])");

            SizeEstimator size = new SizeEstimator();
            size.reserve(classobj,4096);

            ImmortalPhysicalMemory vtm = new ImmortalPhysicalMemory(type, base, size);
            base = base+(4096*((int) size.getEstimate()/4096)) + 4096;

            if( !(vtm instanceof ImmortalPhysicalMemory &&
                  vtm instanceof MemoryArea) )
                throw new Exception("Return object is not instance of "+
                                    "ImmortalPhysicalMemory nor MemoryArea");

        } catch (Exception e) {
            System.out.println("ImmortalPhysicalMemoryTest: " +
                               "ImmortalPhysicalMemory([5]) failed");
            Tests.fail("new ImmortalPhysicalMemory([5])",e);
        }

        /* Subtest 6:
        ** Constructor "public ImmortalPhysicalMemory(Object type, long base,
        **                                      SizeEstimator size,
        **                                      Runnable logic)"
        */
        Tests.increment();
        try {
            System.out.println("ImmortalPhysicalMemoryTest: ImmortalPhysicalMemory([6])");

            long size = 4096;
            SizeEstimator sizeest = new SizeEstimator();
            sizeest.reserve(classobj,4096);
            Runnable logic = testThread;

            ImmortalPhysicalMemory vtm = new ImmortalPhysicalMemory(type, base,
                                                                    sizeest,
                                                                    logic);
            base = base+(4096*((int) sizeest.getEstimate()/4096)) + 4096;

            if( !(vtm instanceof ImmortalPhysicalMemory &&
                  vtm instanceof MemoryArea) )
                throw new Exception("Return object is not instance of "+
                                    "ImmortalPhysicalMemory nor MemoryArea");

        } catch (Exception e) {
            System.out.println("ImmortalPhysicalMemoryTest: " +
                               "ImmortalPhysicalMemory([6]) failed");
            Tests.fail("new ImmortalPhysicalMemory([6])",e);
        }

        /* Subtest 7:
        ** Constructor "public ImmortalPhysicalMemory(Object type, SizeEst. size)"
        */
        Tests.increment();
        try {
            System.out.println("ImmortalPhysicalMemoryTest: ImmortalPhysicalMemory([7])");

            SizeEstimator size = new SizeEstimator();
            size.reserve(classobj,4096);

            ImmortalPhysicalMemory vtm = new ImmortalPhysicalMemory(type, size);
            base = base+(4096*((int) size.getEstimate()/4096)) + 4096;
            if( !(vtm instanceof ImmortalPhysicalMemory &&
                  vtm instanceof MemoryArea) )
                throw new Exception("Return object is not instance of "+
                                    "ImmortalPhysicalMemory nor MemoryArea");

        } catch (Exception e) {
            System.out.println("ImmortalPhysicalMemoryTest: " +
                               "ImmortalPhysicalMemory([7]) failed");
            Tests.fail("new ImmortalPhysicalMemory([7])",e);
        }

        /* Subtest 8:
        ** Constructor "public ImmortalPhysicalMemory(Object type, SizeEst. size,
        **                                      Runnable logic)"
        */
        Tests.increment();
        try {
            System.out.println("ImmortalPhysicalMemoryTest: ImmortalPhysicalMemory([8])");

            SizeEstimator size = new SizeEstimator();
            size.reserve(classobj,4096);
            Runnable logic = testThread;

            ImmortalPhysicalMemory vtm = new ImmortalPhysicalMemory(type, size, logic);
            base = base+(4096*((int) size.getEstimate()/4096)) + 4096;
            if( !(vtm instanceof ImmortalPhysicalMemory &&
                  vtm instanceof MemoryArea) )
                throw new Exception("Return object is not instance of "+
                                    "ImmortalPhysicalMemory nor MemoryArea");

        } catch (Exception e) {
            System.out.println("ImmortalPhysicalMemoryTest: " +
                               "ImmortalPhysicalMemory([8]) failed");
            Tests.fail("new ImmortalPhysicalMemory([8])",e);
        }

        PhysicalMemoryManager.removeFilter(type);
        Tests.printSubTestReportTotals("ImmortalPhysicalMemoryTest");

    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
