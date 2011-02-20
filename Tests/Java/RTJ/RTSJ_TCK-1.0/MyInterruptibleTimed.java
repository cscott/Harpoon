/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/* MyInterruptibleTimed - This class implements the Interruptible interface
**                        and is used by the InterruptibleTimedThread class in
**                        subtest 4 of the TimedTest to provide it's
**                        interruptible logic. A shared semaphore is passed
**                        into the constructor so that the master test can wait
**                        for and be notified by the interruptible logic.
*/
import javax.realtime.*;

/*
** This class is used in conjuction with TimedTest
** to provide doInterruptible logic to test against
*/

public class MyInterruptibleTimed extends RealtimeThread implements
Interruptible {
    private Semaphore semaphore;

    public MyInterruptibleTimed(Semaphore shared_semaphore)
    {
        System.out.println("MyInterruptibleTimed: MyInterruptibleTimed("+
                           "Semaphore)");
        semaphore = shared_semaphore;
    }

    public void run(AsynchronouslyInterruptedException e) throws Timed
    {
        int i;
        boolean x;

        System.out.println("MyInterruptibleTimed: run(Asynchronously"+
                           "InterruptedException)");

        semaphore.sNotify();

        Tests.delay(1);
        Tests.threadFail("MyInterruptibleTimed: interrupt never received "+
                           "in Subtest 4 of TimedTest");
    }

    public void interruptAction(AsynchronouslyInterruptedException e)
    {
        System.out.println("MyInterruptibleTimed: run method was excepted in "+
                           "Subtest 4");
    }

}
