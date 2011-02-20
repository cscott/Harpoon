/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/* MyRTThread - This class is a subclass of RealtimeThread and is used in
**              subtest 14 of the RealtimeThreadTest to provide a realtime
**              upon which to call interrupt().
*/

import javax.realtime.*;

public class MyRTThread extends RealtimeThread
{
    private Semaphore semaphore;
    private boolean m_wasInterrupted;

    public MyRTThread(Semaphore shared_semaphore)
    {
        System.out.println("MyRTThread: MyRTThread(Semaphore)");
        semaphore = shared_semaphore;
        m_wasInterrupted = false;
    }

    public void run()
    {
        try {
            System.out.println("MyRTThread: run()");
            semaphore.sNotify();
            Thread.sleep(15000);
            System.out.println("MyRTThread: MyRTThread waking up");
            Tests.threadFail("MyRTThread interrupt() did not throw an "+
                             "InterruptedException inside of MyRTThread as "+
                             "expected, and failed");
        } catch (Exception e) {
            if (! (e instanceof InterruptedException)) {
                Tests.threadFail("MyRTThread: interrupt() threw an "+
                                   "exception in MyRTThread other than "+
                                   "InterruptedException");
                e.printStackTrace();
            }
            else {
                System.out.println("MyRTThread: received "+
                                   "InterruptedException as expected");
                m_wasInterrupted = true;
            }
        }

    }

    public boolean wasInterrupted()
    {
        return m_wasInterrupted;
    }
}
