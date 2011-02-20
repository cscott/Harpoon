/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/* InterruptibleThread - This class is a subclass of RealtimeThread used in
**                       the AsynchronouslyInterruptedExceptionTest to provide
**                       a thread containing interruptible logic. A shared
**                       semaphore is passed into the constructor and then
**                       into the interruptible logic constructor so that
**                       the master test can wait for and be notified by the
**                       interruptible logic.  The testcase passed in is used
**                       by the interruptible logic to perform the appropriate
**                       logic for the subtest being run.
*/
import javax.realtime.*;

public class InterruptibleThread extends RealtimeThread
{
    AsynchronouslyInterruptedException aie;
    Semaphore semaphore;
    int testCase;

    public InterruptibleThread(AsynchronouslyInterruptedException shared_aie,
                               Semaphore shared_semaphore,int tcase)
    {
        super();
        System.out.println("InterruptibleThread: Creating an interruptible "+
                           "thread");
        aie = shared_aie;
        semaphore = shared_semaphore;
        testCase = tcase;
    }

    public void run() {

        System.out.println("InterruptibleThread: run()");

        try {
            System.out.println("InterruptibleThread: "+
                               "Creating a MyInterruptible ");
            MyInterruptible mi = new MyInterruptible(semaphore,testCase);
            System.out.println("InterruptibleThread: "+
                               "Calling aie.doInterruptible(mi) ");
            boolean x = aie.doInterruptible(mi);
            System.out.println("InterruptibleThread: done");
        } catch(Exception e) {
            Tests.threadFail("InterruptibleThread: aie.doInterruptible(mi) "+
                             "in Subtest " + testCase + " of the "+
                             "AsynchronouslyInterruptedExceptionTest threw "+
                             "an exception");
            e.printStackTrace();
        }

    }
}
