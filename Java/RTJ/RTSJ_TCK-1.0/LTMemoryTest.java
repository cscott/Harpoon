//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              LTMemoryTest

Subtest 1:
        "public LTMemory(long initialSizeInBytes, long maxSizeInBytes)

Subtest 2:
        "public LTMemory(long initialSizeInBytes, long maxSizeInBytes,
                         Runnable logic)

Subtest 3:
        "public LTMemory(SizeEstimator initial, SizeEstimator maximum)

Subtest 4:
        "public LTMemory(SizeEstimator initial, SizeEstimator maximum,
                         Runnable logic)
Subtest 5:
        "public long getMaximumSize()"

Subtest 6:
        "public Object getPortal()"

Subtest 7:
        "public void setPortal(Object)"

Subtest 8:
        "public void enter()"

Subtest 9:
        "public void enter(Runnable logic)"


*/

import javax.realtime.*;

public class LTMemoryTest
{

    public static void run()
    {
        LTMemory ltm = null;
        Object o = null;
        Thread testThread = new Thread(){
                public void run() {
                    System.out.println("Inside of RealtimeThread");
                }
            };

        Class classobj;

        Tests.newTest("LTMemoryTest");
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
        ** Constructor "public LTMemory(long initialSizeInBytes,
        ** long maxSizeInBytes)
        */
        try {
            System.out.println("LTMemoryTest: LTMemory(long,long)");
            int initial = 1000;
            int maximum = 2000;
            ltm = new LTMemory(initial, maximum);
            if( !(ltm instanceof LTMemory && ltm instanceof ScopedMemory) )
                throw new Exception("Return object is not instanceof "+
                                    "LTMemory nor ScopedMemory");
        } catch (Exception e) {
            System.out.println("LTMemoryTest: LTMemory(long,long) failed");
            Tests.fail("new LTMemory(initial, maximum)",e);
        }

        /* Subtest 2:
        ** Constructor "public LTMemory(long initialSizeInBytes,
        ** long maxSizeInBytes, Runnable logic)
        */
        Tests.increment();
        try {
            System.out.println("LTMemoryTest: LTMemory(long,long,Runnable)");
            int initial = 1000;
            int maximum = 2000;
            Runnable logic = testThread;
            ltm = new LTMemory(initial, maximum, logic);
            if( !(ltm instanceof LTMemory && ltm instanceof ScopedMemory) )
                throw new Exception("Return object is not instanceof "+
                                    "LTMemory nor ScopedMemory");
        } catch (Exception e) {
            System.out.println("LTMemoryTest: LTMemory(long,long,Runnable) failed");
            Tests.fail("new LTMemory(initial, maximum, logic)",e);
        }

        /* Subtest 3:
        ** Constructor "public LTMemory(SizeEstimator initial,
        **                              SizeEstimator maximum)
        */
        Tests.increment();
        try {
            System.out.println("LTMemoryTest: LTMemory(SizeEst,SizeEst)");
            SizeEstimator initial = new SizeEstimator();
            SizeEstimator maximum = new SizeEstimator();
            initial.reserve(classobj, 1000);
            maximum.reserve(classobj, 100);
            ltm = new LTMemory(initial, maximum);
            if( !(ltm instanceof LTMemory && ltm instanceof ScopedMemory) )
                throw new Exception("Return object is not instanceof "+
                                    "LTMemory nor ScopedMemory");
        } catch (Exception e) {
            System.out.println("LTMemoryTest: LTMemory(SizeEst,SizeEst) failed");
            Tests.fail("new LTMemory(initial, maximum)",e);
        }

        /* Subtest 4:
        ** Constructor "public LTMemory(SizeEstimator initial,
        **                              SizeEstimator maximum,
        **                              Runnable logic)
        */
        Tests.increment();
        try {
            System.out.println("LTMemoryTest: LTMemory(SizeEst,SizeEst,Runnable)");
            SizeEstimator initial = new SizeEstimator();
            SizeEstimator maximum = new SizeEstimator();
            Runnable logic = testThread;
            initial.reserve(classobj, 1000);
            maximum.reserve(classobj, 100);
            ltm = new LTMemory(initial, maximum,logic);
            if( !(ltm instanceof LTMemory && ltm instanceof ScopedMemory) )
                throw new Exception("Return object is not instanceof "+
                                    "LTMemory nor ScopedMemory");
        } catch (Exception e) {
            System.out.println("LTMemoryTest: LTMemory(SizeEst,SizeEst,Runnable) failed");
            Tests.fail("new LTMemory(initial, maximum, logic)",e);
        }

        /* Subtest 5:
        ** Method "public long getMaximumSize()"
        */
        Tests.increment();
        try {
            System.out.println("LTMemoryTest: getMaximumSize()");
            Runnable logic = testThread;
            int size = 1000;
            int base = 100;
            ltm = new LTMemory(size, base);
            long gotsize = ltm.getMaximumSize();
            if( (gotsize != size) )
                throw new Exception("Return size is not correct.");
        } catch (Exception e) {
            System.out.println("LTMemoryTest: getMaximumSize() failed");
            Tests.fail("getMaximumSize()",e);
        }

        /* Subtest 6:
        ** Method "public Object getPortal()"
        */
        Tests.increment();
        try {
            System.out.println("LTMemoryTest: getPortal()");
            Runnable logic = testThread;
            ltm = new LTMemory(100, 2000);
            o = ltm.getPortal();
            // null is a valid return
            if( (o != null) && !(o instanceof Object) )
                throw new Exception("Return object is not instanceof Object");
        } catch (Exception e) {
            System.out.println("LTMemoryTest: getPortal() failed");
            Tests.fail("getPortal()",e);
        }

        /* Subtest 7:
        ** Method "public setPortal(Object)"
        */
        Tests.increment();
        try {
            System.out.println("LTMemoryTest: setPortal()");
            Runnable logic = testThread;
            ltm = new LTMemory(100, 2000);
            ltm.setPortal(null);
        } catch (Exception e) {
            System.out.println("LTMemoryTest: setPortal() failed");
            Tests.fail("setPortal()",e);
        }

        /* Subtests 8-:
        ** Method "public enter(Runnable logic)"
        */
        ImmortalMemory i1 = ImmortalMemory.instance();

        AperiodicParameters ap
            = new AperiodicParameters(new RelativeTime(1000L,100),
                                      new RelativeTime( 500L,0),
                                      null,null);
        RealtimeThread t1
            = new RealtimeThread(new PriorityParameters(11), ap,
                                 null, i1, null, new ScopeTest(i1));

        System.out.println("** DDD");
        try {
            System.out.println("LTTest: starting new Scope tests");
            t1.start();
            t1.join();
        }
        catch(Exception e) {
            System.out.println("LTlMemoryTest: new Scope tests failed");
            Tests.fail("ScopedMemory Tests",e);
        }

        Tests.printSubTestReportTotals("LTMemoryTest");
    }


    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
