/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/* MyReadThread - Is a thread class used in subtest 10 of WaitFreeReadQueueTest
**                and subtest 7 of WaitFreeDequeueTest to provide reads on
**                the queues.
*/

import javax.realtime.*;
import com.timesys.*;

public class MyReadThread extends Thread
{
    private WaitFreeWriteQueue wfwq;
    private WaitFreeDequeue wfd;
    private WaitFreeReadQueue wfrq;
    private int testCase;

    public MyReadThread(int tcase) {
        System.out.println("MyReadThread: MyReadThread(int)");
        this.testCase = tcase;
    }

    public void setwfwQ(WaitFreeWriteQueue wq) {
        System.out.println("MyReadThread: setwfwQ(WaitFreeWriteQueue)");
        this.wfwq = wq;
    }

    public void setwfd(WaitFreeDequeue rq) {
        System.out.println("MyReadThread: setwfd(WaitFreeDequeue)");
        this.wfd = rq;
    }


    public void setwfrQ(WaitFreeReadQueue rq) {
        System.out.println("MyReadThread: setwfrQ(WaitFreeReadQueue)");
        this.wfrq = rq;
    }

    public void run() {
        Object obj;

        System.out.println("MyReadThread: run()");

        switch (testCase) {
        case 1: //WaitFreeDequeue - testing blockingWrite() method
            System.out.println("**** Reading from WaitFreeDequeue");
            try {
                obj = wfd.nonBlockingRead();
            } catch (Exception e) {
                Tests.threadFail("MyReadThread:  wfd.nonBlockingRead() "+
                                 "inside of MyReadThread testcase 2 could "+
                                 "not successfully read from the queue");
                e.printStackTrace();
            }
            System.out.println("MyReadThread: Reading from WaitFreeDequeue "+
                               "is complete");
            break;

        case 2: //WaitFreeReadQueue - testing write() on full queue
            System.out.println("MyReadThread: Reading from WaitFreeReadQueue");
            try {
                obj = wfrq.read();
            } catch (Exception e) {
                Tests.threadFail("MyReadThread: wfrq.read() inside of "+
                                 "MyReadThread testcase 3 could not "+
                                 "successfully read from the queue");
                e.printStackTrace();
            }
            System.out.println("MyReadThread: Reading from WaitFreeReadQueue "+
                               "is complete");
            break;
        }

    }
}

