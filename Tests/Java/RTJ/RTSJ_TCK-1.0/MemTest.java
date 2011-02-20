/* MemTest - This class contains MemoryArea subtests 4-8 of the
**           ImmortalMemoryTests that must be executed as Realtime threads.
*/

//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

import javax.realtime.*;


public class MemTest implements Runnable
{

    ImmortalMemory i1;

    public MemTest(ImmortalMemory one)
    {
        this.i1=one;
    }

    public void run()
    {
        String str1 = new String("Hello");

        /* Subtest 5
        ** Method "public static MemoryArea getMemoryArea(java.lang.Object
        ** object)"
        */
        Tests.increment();
        try {
            System.out.println("MemTest: getMemoryArea(Object)");
            MemoryArea ma;
            ma = MemoryArea.getMemoryArea(str1);
            if (!(ma instanceof MemoryArea))
                throw new Exception("Return object is not instanceof MemoryArea");
        } catch (Exception e) {
            System.out.println("MemTest: getMemoryArea(Object) failed");
            Tests.fail("MemoryArea.getMemoryArea()",e);
        }

        /* Subtest 6
        ** Method "public long size()"
        */
        Tests.increment();
        try {
            System.out.println("MemTest: size()");
            MemoryArea ma;
            ma = MemoryArea.getMemoryArea(str1);
            long size = ma.size();
            if (size < 0)
                throw new Exception("Memory size is less than 0");
        } catch (Exception e) {
            System.out.println("MemTest: size() failed");
            Tests.fail("ma.size()",e);
        }

        /* Subtest 7
        ** Method "public long memoryConsumed()"
        */
        Tests.increment();
        try {
            System.out.println("MemTest: memoryConsumed()");
            MemoryArea ma;
            ma = MemoryArea.getMemoryArea(str1);
            long consumed = ma.memoryConsumed();
            if (consumed < 0)
                throw new Exception("Memory consumed is less than 0");
        } catch (Exception e) {
            System.out.println("MemTest: memoryConsumed() failed");
            Tests.fail("ma.memoryConsumed()", e);
        }

        /* Subtest 8
        ** Method "public long memoryRemaining()"
        */
        Tests.increment();
        try {
            System.out.println("MemTest: memoryRemaining()");
            MemoryArea ma;
            ma = MemoryArea.getMemoryArea(str1);
            long remaining = ma.memoryRemaining();
            if (remaining < 0)
                throw new Exception("Memory remaining is less than 0");
        } catch (Exception e) {
            System.out.println("MemTest: memoryRemaining() failed");
            Tests.fail("ma.memoryRemaining()", e);
        }

        /* Subtest 9
        ** public void enter(java.lang.Runnable logic)
        */
        Tests.increment();
        try {
            System.out.println("MemTest: enter(Runnable)");
            i1.enter(new Runnable() {
                    public void run()
                    {
                        String str2 = new String("Here (9)");
                        System.out.println(str2);
                    }
                } );
        } catch (Exception e) {
            System.out.println("MemTest: enter(Runnable) failed");
            Tests.fail("i1.enter()",e);
        }

        /* Subtest 10
        ** public void executeInArea(Runnable logic)
        */
        Tests.increment();
        try {
            System.out.println("MemTest: executeInArea(Runnable)");
            i1.executeInArea(new Runnable() {
                    public void run()
                    {
                        String str2 = new String("Here (10)");
                        System.out.println(str2);
                    }
                } );
        } catch (Exception e) {
            System.out.println("MemTest: executeInArea(Runnable) failed");
            Tests.fail("i1.enter()",e);
        }

    }
}
