//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                      AsynchronouslyInterruptedExceptionTest

Subtest 1:
        "public AsynchronouslyInterruptedException()"

Subtest 2:
        "public synchronized boolean disable()" where the
        call is not within doInterruptible()

Subtest 3:
        "public synchronized boolean disable()" where the
        call is within doInterruptible()

        "public boolean doInterruptible(Interruptible logic)"

Subtest 4:
        "public boolean doInterruptible(Interruptible logic)"
        where logic is null.

Subtest 5:
        "public synchronized boolean enable()" where the call
        is not within doInterruptible()

Subtest 6:
        "public synchronized boolean enable()" where the
        call is within doInterruptible()

        "public boolean doInterruptible(Interruptible logic)"

Subtest 7:
        "public synchronized boolean fire()"

Subtest 8:
        "public synchronized boolean fire()" where there
        is not current invocation of doInterruptible();

Subtest 9:
        "public static AsynchronouslyInterruptedException getGeneric()"

Subtest 10:
        "public boolean isEnabled()"

Subtest 11:
        "public boolean isEnabled()" where thread is expected
        to be not available for interrupt

Subtest 12:
        "public boolean happened(boolean propagate)"
*/

import javax.realtime.*;

public class AsynchronouslyInterruptedExceptionTest
{
    public static void run3()
    {

        AsynchronouslyInterruptedException aie = null;
        Object o = null;
        Tests.newTest("AsynchronouslyInterruptedExceptionTest");

        /* Subtest 1:
        ** Constructor "public AsynchronouslyInterruptedException()"
        */
        Tests.increment();
        try {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "AsynchronouslyInterruptedException()");
            aie = new AsynchronouslyInterruptedException();
            if( !(aie instanceof AsynchronouslyInterruptedException &&
                  aie instanceof InterruptedException) )
                throw new Exception("Return object is not instanceof AsynchronouslyInterruptedException nor InterruptedException");
        } catch (Exception e) {
            Tests.fail("new AsynchronouslyInterruptedExceptionTest()",e);
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "AsynchronouslyInterruptedException()");
        }

        /* Subtest 2:
        ** Method "public synchronized boolean disable()" where the
        ** call is not within doInterruptible()
        */
        Tests.increment();
        try {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "disable()");
            aie = new AsynchronouslyInterruptedException();
            boolean x = aie.disable();
            if (x != false)
                throw new Exception("Did not receive expected false return");
        } catch (Exception e) {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "disable() failed");
            Tests.fail("aie.disable()",e);
        }

        /* Subtest 3:
        ** Method "public synchronized boolean disable()" where the
        ** call is within doInterruptible()
        **
        ** Method "public boolean doInterruptible(Interruptible logic)"
        */
        Tests.increment();
        try {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "disable()");
            Semaphore semaphore = new Semaphore();
            AsynchronouslyInterruptedException shared_aie = new AsynchronouslyInterruptedException();
            InterruptibleThread iThread = new InterruptibleThread(shared_aie, semaphore, 3);
            iThread.start();
            semaphore.sWait();
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "about to call fire()");
            shared_aie.fire();
            semaphore.sNotify();
        } catch (Exception e) {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "disable() failed");
            Tests.fail("e.disable()",e);
        }

        /* Subtest 4:
        ** Method "public boolean doInterruptible(Interruptible logic)"
        ** where logic is null.
        */
        Tests.increment();
        try {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "doInterruptible()");
            aie = new AsynchronouslyInterruptedException();
            Interruptible logic = null;
            boolean x = aie.doInterruptible(logic);
            if (x != true)
                throw new Exception("Did not receive expected true return");
        } catch (Exception e) {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "doInterruptible() failed");
            Tests.fail("aie.doInterruptible(null)",e);
        }

        /* Subtest 5:
        ** Method "public synchronized boolean enable()" where the call
        ** is not within doInterruptible()
        */
        Tests.increment();
        try {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "enable()");
            aie = new AsynchronouslyInterruptedException();
            boolean x = aie.enable();
            if (x != false)
                throw new Exception("Did not receive expected false return");
        } catch (Exception e) {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "enable() failed");
            Tests.fail("aie.enable()",e);
        }

        /* Subtest 6:
        ** Method "public synchronized boolean enable()" where the
        ** call is within doInterruptible()
        **
        ** Method "public boolean doInterruptible(Interruptible logic)"
        */
        Tests.increment();
        try {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "enable()");
            Semaphore semaphore = new Semaphore();
            AsynchronouslyInterruptedException shared_aie = new
                AsynchronouslyInterruptedException();
            InterruptibleThread iThread = new InterruptibleThread(shared_aie,
                                                               semaphore, 6);
            iThread.start();
            semaphore.sWait();
            iThread.interrupt();
        } catch (Exception e) {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "enable() failed");
            Tests.fail("e.enable()",e);
        }

        /* Subtest 7:
        ** Method "public synchronized boolean fire()"
        */
        Tests.increment();
        try {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "fire()");
            Semaphore semaphore = new Semaphore();
            AsynchronouslyInterruptedException shared_aie = new
                AsynchronouslyInterruptedException();
            InterruptibleThread iThread = new InterruptibleThread(shared_aie,
                                                               semaphore, 7);
            iThread.start();
            semaphore.sWait();
            boolean x = shared_aie.fire();
            if (x != true)
                throw new Exception("Did not receive expected true value");
        } catch (Exception e) {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "fire() failed");
            Tests.fail("aie.fire()",e);
        }

        /* Subtest 8:
        ** Method "public synchronized boolean fire()" where there
        ** is not current invocation of doInterruptible();
        */
        Tests.increment();
        try {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "fire()");
            aie = new AsynchronouslyInterruptedException();
            boolean x = aie.fire();
            if (x != false)
                throw new Exception("Did not receive expected false value");
        } catch (Exception e) {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "fire() failed");
            Tests.fail("aie.fire()",e);
        }

        /* Subtest 9:
        ** Method "public static AsynchronouslyInterruptedException
        ** getGeneric()"
        */
        Tests.increment();
        try {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "getGeneric()");
            o = AsynchronouslyInterruptedException.getGeneric();
            if( !(o instanceof AsynchronouslyInterruptedException) )
                throw new Exception("Return object is not instanceof "+
                                    "AsynchronouslyInterruptedException");
        } catch (Exception e) {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "getGeneric() failed");
            Tests.fail("AsynchronouslyInterruptedException.getGeneric()",e);
        }

        /* Subtest 10:
        ** Method "public boolean isEnabled()"
        */
        Tests.increment();
        try {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "isEnabled()");
            Semaphore semaphore = new Semaphore();
            AsynchronouslyInterruptedException shared_aie = new
                AsynchronouslyInterruptedException();
            InterruptibleThread iThread = new InterruptibleThread(shared_aie,
                                                               semaphore, 10);
            iThread.start();
            semaphore.sWait();
            boolean x = shared_aie.isEnabled();
            if (x != true)
                throw new Exception("AIE is not enabled when it is expected "+
                                    "to be");
            iThread.interrupt();

        } catch (Exception e) {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "isEnabled() failed");
            Tests.fail("shared_aie.isEnabled()",e);
        }

        /* Subtest 11:
        ** Method "public boolean isEnabled()" where thread is expected
        ** to be not available for interrupt
        */
        Tests.increment();
        try {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "isEnabled()");
            Semaphore semaphore = new Semaphore();
            AsynchronouslyInterruptedException shared_aie = new
                AsynchronouslyInterruptedException();
            InterruptibleThread iThread = new InterruptibleThread(shared_aie,
                                                               semaphore, 11);
            iThread.start();
            semaphore.sWait();
            boolean x = shared_aie.isEnabled();
            iThread.interrupt();
            if (x != false)
                throw new Exception("AIE is enabled when it is expected not "+
                                    "to be");

        } catch (Exception e) {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "isEnabled() failed");
            Tests.fail("shared_aie.isEnabled()",e);
        }

        /* Subtest 12:
        ** Method "public boolean happened(boolean propagate)"
        */
        Tests.increment();
        try {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "happened(boolean)");
            Semaphore semaphore = new Semaphore();
            AsynchronouslyInterruptedException shared_aie = new
                AsynchronouslyInterruptedException();
            InterruptibleThread iThread = new InterruptibleThread(shared_aie,
                                                               semaphore, 12);
            iThread.start();
            semaphore.sWait();
            shared_aie.fire();
        } catch (Exception e) {
            System.out.println("AsynchronouslyInterruptedExceptionTest: "+
                               "happened(boolean) failed");
            Tests.fail("e.happened(false)",e);
        }

        Tests.printSubTestReportTotals("AsynchronouslyInterruptedException"+
                                       "Test");
    }

    public static class AIETest implements Runnable
    {
        ImmortalMemory i1;
        public AIETest(ImmortalMemory im)
        {
            i1 = im;
        }

        public void run()
        {


            AsynchronouslyInterruptedExceptionTest.run3();
        }
    }

    public static void run2()
    {
        ImmortalMemory im = ImmortalMemory.instance();
        RealtimeThread t1
            = new RealtimeThread(null, null, null, im,
                                 null, new AIETest(im));

        try {
            t1.start();
            t1.join();
        }
        catch (Exception e) {
            System.out.println("Exception " + e.toString());
            Tests.fail("AIE Tests");
        }
    }

    public static void run() {
        RealtimeThread rt
            = new RealtimeThread(null, null, null,
                                 ImmortalMemory.instance(),
                                 null,
                                 new Runnable(){
                                         public void run() {
                                             AsynchronouslyInterruptedExceptionTest.run2();
                                         }
                                     }
                );

        rt.start();
        try {
            rt.join();
        }
        catch (Exception e) {
            System.out.println("Failed.");
        }

    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        AsynchronouslyInterruptedExceptionTest.run();
        Tests.conclude();
    }
}
