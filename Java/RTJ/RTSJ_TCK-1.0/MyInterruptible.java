/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/* MyInterruptible - This class implements the Interruptible interface
**                   and is used by the InterruptibleThread class in the
**                   AsynchronouslyInterruptedExceptionTests
**                   to provide it's interruptible logic. A shared
**                   semaphore is passed into the constructor so that
**                   the master test can wait for and be notified by the
**                   interruptible logic.  The testcase passed in is used
**                   to perform the appropriate logic for the subtest being
**                   run.
*/

import javax.realtime.*;

public class MyInterruptible extends RealtimeThread implements Interruptible
{
    private Semaphore semaphore;
    private int testCase;
    private boolean disabled;

    public MyInterruptible(Semaphore shared_semaphore,int tcase)
    {

        System.out.println("MyInterruptible: MyInterruptible(Semaphore, int)");

        semaphore = shared_semaphore;
        testCase = tcase;
        disabled = false;
    }

    public void run(AsynchronouslyInterruptedException e) throws
    AsynchronouslyInterruptedException {
        int i;
        boolean x;

        System.out.println("MyInterruptible: run() - testCase=" + testCase);

        switch (testCase) {
        case 3: // disable() test
            disabled = e.disable();
            if (disabled != true) {
                Tests.threadFail("MyInterruptible: e.disable returned "+
                                   "unexpected false value in Subtest 3 of "+
                                   "AsynchronouslyInterrputedExceptionTest");
                                // Wake up master to continue with testing
                semaphore.sNotify();
            } else {
                semaphore.sNotify();

                System.out.println("MyInterruptible[3]: "+
                                   "delaying for 1");
                Tests.delay(1);

            }
            break;

        case 6:  // enable() test
            e.disable();
            x = e.enable();
            if (x != true) {
                Tests.threadFail("MyInterruptible[6]: e.enable() returned "+
                                 "unexpected false value from Subtest 6 of "+
                                 "AsynchronouslyInterruptedExceptionTest");
            }
            semaphore.sNotify();
            Tests.delay(1);

            Tests.threadFail("MyInterruptible[6]: interrupt never "+
                               "received in Subtest 6 of "+
                               "AsynchronouslyInterruptedExceptionTest");
            break;

        case 7:  // fire() test
            semaphore.sNotify();

            Tests.delay(1);

            Tests.threadFail("MyInterruptible[7]: interrupt never "+
                             "received in Subtest 7 of "+
                             "AsynchronouslyInterruptedExceptionTest");

            break;


        case 10:  // isEnabled() test
            semaphore.sNotify();
            Tests.delay(1);

            Tests.threadFail("MyInterruptible[10]: interrupt never "+
                             "received in Subtest 10 of "+
                             "AsynchronouslyInterruptedExceptionTest");

            break;

        case 11:  // isEnabled() test
            e.disable();
            semaphore.sNotify();
            Tests.delay(1);
            e.enable();
            Tests.threadFail("MyInterruptible[11]: interrupt never "+
                             "received in Subtest 11 of "+
                             "AsynchronouslyInterruptedExceptionTest");

            break;

        case 12: // happened() test
            if (e.happened(true) != false) {
                Tests.threadFail("MyInterruptible[12]: e.happened() "+
                                 "incorrectly recognizes this as the "+
                                 "current exception");
            }
            semaphore.sNotify();
            Tests.delay(1);
            break;

        default:
            Tests.threadFail("MyInterruptible: bad testcase passed in from "+
                             "AsynchronouslyInterruptedExceptionTest");
        }

        System.out.println("Leaving MyInterruptible.Run");
    }

    public void interruptAction(AsynchronouslyInterruptedException e)
    {
        switch (testCase) {
        case 3:
            if (disabled == true)
                Tests.threadFail("MyInterruptible[3]: MyInterruptible "+
                                 "processed interrupt even though "+
                                 "interrupt was disabled in Subtest 3 of "+
                                 "AsynchronouslyInterruptedExceptionTest");
            break;

        case 12:
            if (e.happened(true) == false) {
                Tests.threadFail("MyInterruptible[12]: MyInterruptible "+
                                 "processed interrupt even though the "+
                                 "state was changed to non-pending via "+
                                 "happened() method in Subtest 12 of "+
                                 "AsynchronouslyInterruptedExceptionTest");
            }
            break;

        }

        System.out.println("**** MyInterruptible's run method was excepted "+
                           "in Subtest " + testCase);
    }
}
