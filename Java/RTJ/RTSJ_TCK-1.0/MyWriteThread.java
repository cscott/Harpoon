/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/* MyWriteThread - Is a thread class used in subtest 9 of WaitFreeReadQueueTest,
**                 subtest 10 of WaitFreeWriteQueueTest and subtest 6 of
**                 WaitFreeDequeueTest to provide writes on the queues.
*/

import javax.realtime.*;
import com.timesys.*;

public class MyWriteThread extends Thread
{
    private WaitFreeReadQueue wfrq;
    private WaitFreeDequeue wfd;
    private WaitFreeWriteQueue wfwq;
    private int testCase;

    public MyWriteThread(int tcase) {
        System.out.println("MyWriteThread: MyWriteThread(int)");
        this.testCase = tcase;
    }

    public void setwfrQ(WaitFreeReadQueue rq) {
        System.out.println("MyWriteThread: setwfrQ(WaitFreeReadQueue)");
        this.wfrq = rq;
    }

    public void setwfd(WaitFreeDequeue rq) {
        System.out.println("MyWriteThread: setwfd(WaitFreeDequeue)");
        this.wfd = rq;
    }

    public void setwfwQ(WaitFreeWriteQueue wq) {
        System.out.println("MyWriteThread: setwfwQ(WaitFreeWriteQueue)");
        this.wfwq = wq;
    }

    public void run() {

        System.out.println("MyWriteThread: run()");
        Object obj;
        switch (testCase) {
        case 1:  // WaitFreeReadQueueTest
            System.out.println("MyWriteThread: Writing to WaitFreeReadQueue");
            obj = new Object();
            try {
                wfrq.write(obj);
            } catch (Exception e) {
                Tests.threadFail("MyWriteThread:  wfrq.write(obj) inside "+
                                 "of MyWriteThread testcase 1 could not "+
                                 "successfully write to the queue");
                e.printStackTrace();
            }
            System.out.println("MyWriteThread: Writing to WaitFreeReadQueue "+
                               "is complete");
            break;

        case 2: //WaitFreeDequeueTest - testing blockingRead() method
            System.out.println("MyWriteThread: Writing to WaitFreeDequeue");
            obj = new Object();
            try {
                wfd.nonBlockingWrite(obj);
            } catch (Exception e) {
                Tests.threadFail("MyWriteThread: wfd.nonBlockingWrite(obj) "+
                                 "inside of MyWriteThread testcase 2 could "+
                                 "not successfully write to the queue");
                e.printStackTrace();
            }
            System.out.println("MyWriteThread: Writing to WaitFreeDequeue is "+
                               "complete");

            break;

        case 3: //WaitFreeWriteQueue - testing read() on empty queue
            System.out.println("MyWriteThread: Writing to WaitFreeWriteQueue");
            obj = new Object();
            try {
                wfwq.write(obj);
            } catch (Exception e) {
                Tests.threadFail("MyWriteThread: wfwq.write(obj) inside of "+
                                 "MyWriteThread testcase 3 could not "+
                                 "successfully write to the queue");
            }
            System.out.println("MyWriteThread: Writing to WaitFreeWriteQueue "+
                               "is complete");
            break;
        }

    }
}

