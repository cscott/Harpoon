//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                      ImmortalMemoryTests

* ImmortalMemory Tests *

Subtest 1
        "public static ImmortalMemory instance()"


* Memory Area Tests *

Subtest 2
        "public synchronized java.lang.Object newArray(java.lang.Class type,
        int number)"

Subtest 3
        "public synchronized java.lang.Object newInstance(java.lang.Class
        type)"

Subtest 4 [ NEW ]
        "public synchronized java.lang.Object newInstance(reflect.Constructor,
        Object [] params)"

Subtest 5
        "public static MemoryArea getMemoryArea(java.lang.Object object)"

Subtest 6
        "public long size()"

Subtest 7
        "public long memoryConsumed()"

Subtest 8
        "public long memoryRemaining()"

Subtest 9
        "public void enter(java.lang.Runnable logic)

Subtest 10
        "public void executeInArea(java.lang.Runnable logic)

*/

import java.util.*;
import javax.realtime.*;

public class ImmortalMemoryTest
{

    public static void run()
    {
        ImmortalMemory im = null;
        Object o = null;
        Tests.newTest("ImmortalMemoryTest (abstract MemoryAreas)");

        /* Subtest 1
        ** Method "public static ImmortalMemory instance()"
        */
        Tests.increment();
        try {
            System.out.println("ImmortalMemoryTest: instance()");
            im = ImmortalMemory.instance();
            if( !(im instanceof ImmortalMemory && im instanceof MemoryArea) )
                throw new Exception("Return object is not instanceof "+
                                    "ImmortalMemory nor MemoryArea");
        } catch (Exception e) {
            System.out.println("ImmortalMemoryTest: instance() failed");
            Tests.fail("ImmortalMemory.instance()",e);
        }

        /** Memory Area Tests **/

        /* Subtest 2
        ** Method "public synchronized java.lang.Object newArray(
        ** java.lang.Class type, int number)"
        */
        Tests.increment();
        try {
            System.out.println("ImmortalMemoryTest: newArray(Class,int)");
            im = ImmortalMemory.instance();
            int isize = 10;
            Integer[] nums = (Integer[])im.newArray(Class.
                                         forName("java.lang.Integer"), isize);
            if ( nums.length != isize )
                throw new Exception("Array not allocated");
        } catch (Exception e) {
            System.out.println("ImmortalMemoryTest: newArray(Class,int) "+
                               "failed");
            Tests.fail("im.newArray()", e);
        }

        /* Subtest 3
        ** Method "public synchronized java.lang.Object
        ** newInstance(java.lang.Class type)"
        */
        Tests.increment();
        try {
            System.out.println("ImmortalMemoryTest: newInstance(Constructor, Object[])");
            LinkedList list = (LinkedList)im.newInstance(
                                      Class.forName("java.util.LinkedList"));
            list.add("1");
            if (list.size()!=1)
                throw new Exception("List not allocated");
        } catch (Exception e) {
            System.out.println("ImmortalMemoryTest: newInstance(Constructor, Object[]) "+
                               "failed");
            Tests.fail("im.newInstance(Constructor, Object[])", e);
        }

        /* Subtest 4 [ NEW ]
        ** Method "public synchronized java.lang.Object
        ** newInstance(java.lang.Class type)"
        */
        Tests.increment();
        try {
            System.out.println("ImmortalMemoryTest: newInstance(Class)");
            LinkedList list = (LinkedList)im.newInstance(
                        Class.forName("java.util.LinkedList")
                                                .getConstructors()[0],
                        null);
            list.add("1");
            if (list.size()!=1)
                throw new Exception("List not allocated");
        } catch (Exception e) {
            System.out.println("ImmortalMemoryTest: newInstance(Class) "+
                               "failed");
            Tests.fail("im.newInstance()", e);
        }

        /*
        ** Subtests 5-10 (see MemTest.java) executed as a Realtime thread
        */

        ImmortalMemory i1 = ImmortalMemory.instance();

        AperiodicParameters ap = new AperiodicParameters(new RelativeTime(
                            1000L,100),new RelativeTime(500L,0),null,null);
        RealtimeThread t1 = new RealtimeThread(new PriorityParameters(11), ap,
                                          null, i1, null, new MemTest(i1));

        try {
            System.out.println("ImmortalMemoryTest: starting new tests");
            t1.start();
            t1.join();
        }
        catch(Exception e) {
            System.out.println("ImmortalMemoryTest: new tests failed");
            Tests.fail("MemoryArea Tests",e);
        }

        Tests.printSubTestReportTotals("ImmortalMemoryTest");

    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
