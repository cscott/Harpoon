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


public class ScopeTest implements Runnable
{

    ImmortalMemory i1;
    Runnable logic;


    public ScopeTest(ImmortalMemory one)
    {
        System.out.println("Inside of ScopeTest.(ctor)");
        this.i1=one;
        this.logic = new Thread() {
                public void run() {
                    System.out.println("Inside of ScopeTest.Thread.run");
                }
            };
    }

    public void run()
    {
        String str1 = new String("Hello");

        System.out.println("Inside of ScopeTest.run");

        /* Subtest 8
        ** Method "public void enter()"
        */
        Tests.increment();
        try {
            System.out.println("ScopeTest: enter()");
            LTMemory ltm = new LTMemory(1000, 2000, logic);
            ltm.enter();
        } catch (Exception e) {
            System.out.println("LTMemoryTest: enter() failed");
            Tests.fail("enter()",e);
        }

        /* Subtest 9
        ** Method "public void enter()"
        */
        Tests.increment();
        try {
            System.out.println("ScopeTest: enter(Runnable logic)");
            LTMemory ltm = new LTMemory(1000, 2000, logic);
            ltm.enter(new Runnable() {
                    public void run()
                    {
                        System.out.println("Inside subtest 9");
                    }
                    } );
        } catch (Exception e) {
            System.out.println("LTMemoryTest: enter(Runnable) failed");
            Tests.fail("enter(logic)",e);
        }

        /* Subtest 10
        ** Method "public void join()"
        */
        Tests.increment();
        try {
            System.out.println("ScopeTest: join()");
            LTMemory ltm = new LTMemory(1000, 2000, logic);
            ltm.join();
        } catch (Exception e) {
            System.out.println("LTMemoryTest: join() failed");
            Tests.fail("join()",e);
        }

        /* Subtest 11
        ** Method "public void join(HighResolutionTime hrt)"
        */
        Tests.increment();
        try {
            System.out.println("ScopeTest: join(HighResolutionTime time)");
            LTMemory ltm = new LTMemory(1000, 2000, logic);
            HighResolutionTime timelimit = new RelativeTime(0,1000);
            ltm.join(timelimit);
        } catch (Exception e) {
            System.out.println("LTMemoryTest: join() failed");
            Tests.fail("join()",e);
        }

        /* Subtest 11
        ** Method "public void joinAndEnter(Runnable)"
        */
        Tests.increment();
        try {
            Runnable logic = new Thread() {
                    public void Run()
                    {
                        String str = "Subtest 11.Run";
                        System.out.println(str);
                    }
                };

            System.out.println("ScopeTest: joinAndEnter(Runnable)");
            LTMemory ltm = new LTMemory(1000, 2000, logic);
            ltm.joinAndEnter(logic);
        } catch (Exception e) {
            System.out.println("LTMemoryTest: joinAndEnter(Runnable) failed");
            Tests.fail("joinAndEnter(Runnable)",e);
        }

        /* Subtest 12
        ** Method "public void joinAndEnter(Runnable, HRT)"
        */
        Tests.increment();
        try {
            Runnable logic = new Thread() {
                    public void Run()
                    {
                        String str = "Subtest 11.Run";
                        System.out.println(str);
                    }
                };

            System.out.println("ScopeTest: joinAndEnter(Runnable,HRT)");
            LTMemory ltm = new LTMemory(1000, 2000, logic);
            HighResolutionTime timelimit = new RelativeTime(0,1000);
            ltm.joinAndEnter(logic, timelimit);
        } catch (Exception e) {
            System.out.println("LTMemoryTest: joinAndEnter(Runnable,HRT) failed");
            Tests.fail("joinAndEnter(Runnable,HRT)",e);
        }

        System.out.println("Leaving ScopeTest.run");

    }
}
