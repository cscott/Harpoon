/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/* InterruptibleTimedThread - This class is a subclass of RealtimeThread
**                            used in
**                            subtest 4 of TimedTest to provide a thread
**                            containing interruptible logic. A shared
**                            semaphore is passed into the constructor and then
**                            into the interruptible logic constructor so that
**                            the master test can wait for and be notified by
**                            the interruptible logic.
*/
import javax.realtime.*;

public class InterruptibleTimedThread extends RealtimeThread
{
    Timed timed;
    Semaphore semaphore;

    public InterruptibleTimedThread(Timed shared_timed,Semaphore
                                    shared_semaphore)
    {
        super();
        System.out.println("InterruptibleTimedThread: InterruptibleTimed"+
                           "Thread(Timed,Semaphore)");
        timed = shared_timed;
        semaphore = shared_semaphore;

    }

    public void run() {

        System.out.println("InterruptibleTimedThread: run()");

        try {
            MyInterruptibleTimed mi = new MyInterruptibleTimed(semaphore);
            boolean x = timed.doInterruptible(mi);
        } catch(Exception e) {
            Tests.threadFail("InterruptibleTimedThread: timed.do"+
                             "Interruptible(mi) in Subtest 4 of the "+
                             "TimedTest threw an exception");
            e.printStackTrace();
        }

    }
}
