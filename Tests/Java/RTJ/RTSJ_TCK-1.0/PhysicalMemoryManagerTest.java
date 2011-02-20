//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              PhysicalMemoryFactorTest

Subtest 1:
        "public static final java.lang.String ALIGNED"

Subtest 2:
        "public static final java.lang.String BYTESWAP"

Subtest 3:
        "public static final java.lang.String DMA"

Subtest 4:
        "public static final java.lang.String SHARED"

Subtest 5:
        "public static boolean isRemovable(long address, long size)"

Subtest 6:
        "public static boolean isRemoved(long address, long size)"

Subtest 7:
        "public static void onInsertion(long address, long size,
                                        AsyncEventHandler aeh)"

Subtest 8:
        "public static void onRemoval(long address, long size,
                                      AsyncEventHandler aeh)"

Subtest 9:
        "public static void registerFilter(Object name,
                                           PhysicalMemoryTypeFilter filter)"

Subtest 10:
        "public static void removeFilter(Object name)"

*/


import javax.realtime.*;

public class PhysicalMemoryManagerTest
{

    public static class Filter implements PhysicalMemoryTypeFilter
    {
        public boolean contains(long base, long size) { return false; }
        public long find(long base, long size) { return 0; }
        public void initialize(long base, long vbase, long size) { return; }
        public boolean isPresent(long base, long size) { return false; }
        public boolean isRemovable() { return false; }
        public void onInsertion(long base, long size, AsyncEventHandler aeh)
            { return; }
        public void onRemoval(long base, long size, AsyncEventHandler aeh)
            { return; }
        public long vFind(long base, long size) { return 0; }

        // ??? MISSING FROM DOCUMENTATION
        public int getVMAttributes() { return 0; }
        public int getVMFlags() { return 0; }

    }

    public static void run()
    {
        Object o;
        PhysicalMemoryTypeFilter filter = new Filter();
        String filtername = "FILTER NAME";

        Tests.newTest("PhysicalMemoryManagerTest");


        /* Subtest 1:
        ** Field "public static final java.lang.String ALIGNED"
        */
        Tests.increment();
        try {
            System.out.println("PhysicalMemoryManagerTest: ALIGNED");
            o = PhysicalMemoryManager.ALIGNED;
            if( !(o instanceof String) )
                throw new Exception("ALIGNED is not instanceof String");
        } catch (Exception e) {
            System.out.println("PhysicalMemoryManagerTest: ALIGNED failed");
            Tests.fail("PhysicalMemoryManager.ALIGNED",e);
        }

        /* Subtest 2:
        ** Field "public static final java.lang.String BYTESWAP"
        */
        Tests.increment();
        try {
            System.out.println("PhysicalMemoryManagerTest: BYTESWAP");
            o = PhysicalMemoryManager.BYTESWAP;
            if( !(o instanceof String) )
                throw new Exception("BYTESWAP is not instanceof String");
        } catch (Exception e) {
            System.out.println("PhysicalMemoryManagerTest: BYTESWAP failed");
            Tests.fail("PhysicalMemoryManager.BYTESWAP",e);
        }

        /* Subtest 3:
        ** Field "public static final java.lang.String DMA"
        */
        Tests.increment();
        try {
            System.out.println("PhysicalMemoryManagerTest: DMA");
            o = PhysicalMemoryManager.DMA;
            if( !(o instanceof String) )
                throw new Exception("DMA is not instanceof String");
        } catch (Exception e) {
            System.out.println("PhysicalMemoryManagerTest: DMA failed");
            Tests.fail("PhysicalMemoryManager.DMA",e);
        }

        /* Subtest 4:
        ** Field "public static final java.lang.String SHARED"
        */
        Tests.increment();
        try {
            System.out.println("PhysicalMemoryManagerTest: SHARED");
            o = PhysicalMemoryManager.SHARED;
            if( !(o instanceof String) )
                throw new Exception("SHARED is not instanceof String");
        } catch (Exception e) {
            System.out.println("PhysicalMemoryManagerTest: SHARED failed");
            Tests.fail("PhysicalMemoryManager.SHARED",e);
        }

        /* Subtest 5:
        ** Method "public static isRemovable(long address, long size)
        */
        Tests.increment();
        try {
            System.out.println("PhysicalMemoryManagerTest: isRemovable(long, long)");
            boolean b = PhysicalMemoryManager.isRemovable(1000, 1);
        } catch (Exception e) {
            System.out.println("PhysicalMemoryManagerTest: isRemovable failed");
            Tests.fail("PhysicalMemoryManager.isRemovable",e);
        }

        /* Subtest 6:
        ** Method "public static isRemoved(long address, long size)
        */
        Tests.increment();
        try {
            System.out.println("PhysicalMemoryManagerTest: isRemoved(long, long)");
            boolean b = PhysicalMemoryManager.isRemoved(1000, 1);
        } catch (Exception e) {
            System.out.println("PhysicalMemoryManagerTest: isRemoved failed");
            Tests.fail("PhysicalMemoryManager.isRemoved",e);
        }

        /* Subtest 7:
        ** Method "public static void onInsertion(long address, long size,
        **                                         AsyncEventHandler aeh)
        */
        Tests.increment();
        try {
            System.out.println("PhysicalMemoryManagerTest: onInsertion(long, long, AEH)");
            PhysicalMemoryManager.onInsertion(1000, 1, null);
        } catch (Exception e) {
            System.out.println("PhysicalMemoryManagerTest: "+
                               "onInsertion failed");
            Tests.fail("PhysicalMemoryManager.onInsertion(...)",e);
        }

        /* Subtest 8:
        ** Method "public static void onRemoval(long address, long size,
        **                                      AsyncEventHandler aeh)
        */
        Tests.increment();
        try {
            System.out.println("PhysicalMemoryManagerTest: onRemoval(long, long, AEH)");
            PhysicalMemoryManager.onRemoval(1000, 1, null);
        } catch (Exception e) {
            System.out.println("PhysicalMemoryManagerTest: "+
                               "onRemoval failed");
            Tests.fail("PhysicalMemoryManager.onRemoval(...)",e);
        }

        /* Subtest 9:
        ** Method "public static void registerFilter(Object object,
        **                                           PhysicalMemoryFilterType f)
        */
        Tests.increment();
        try {
            System.out.println("PhysicalMemoryManagerTest: "+
                               "registerFilter(object, type)");
            PhysicalMemoryManager.registerFilter(filtername,filter);
        } catch (Exception e) {
            System.out.println("PhysicalMemoryManagerTest: "+
                               "registerFilter failed");
            Tests.fail("PhysicalMemoryManager.registerFilter(...)",e);
        }

        /* Subtest 10:
        ** Method "public static void removeFilter(Object object)
        */
        Tests.increment();
        try {
            System.out.println("PhysicalMemoryManagerTest: "+
                               "removeFilter(object)");
            PhysicalMemoryManager.removeFilter(filtername);
        } catch (Exception e) {
            System.out.println("PhysicalMemoryManagerTest: "+
                               "removeFilter failed");
            Tests.fail("PhysicalMemoryManager.removeFilter(...)",e);
        }

        Tests.printSubTestReportTotals("PhysicalMemoryManagerTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
