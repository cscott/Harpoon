//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              WaitFreeDequeueTest

Subtest 1:
        "public WaitFreeDequeue(java.lang.Thread writer, java.lang.Thread
        reader, int maximum, MemoryArea area)"

Subtest 2:
        "public boolean nonBlockingWrite(java.lang.Object object)"
        "public java.lang.Object nonBlockingRead()"

Subtest 3:
        "public boolean nonBlockingWrite()" where queue is full

Subtest 4:
        "public boolean nonBlockingRead()" where queue is empty

Subtest 5:
        "public boolean force(java.lang.Object)"

Subtest 6:
        "public void blockingRead()"

Subtest 7:
        "public void blockingWrite()"

*/

import javax.realtime.*;

public class WaitFreeDequeueTest
{

    public class WaitFreeDequeueTest_Thread implements Runnable {

        public WaitFreeDequeueTest_Thread(){
            Thread t = new Thread(this, "WaitFreeDequeueTest");
            t.start();
        }

        public void run() {

            WaitFreeDequeue wfd = null;
            Object o = null;
            Thread writer = new Thread();
            Thread reader = new Thread();
            int maximum=100;
            ImmortalMemory memory = ImmortalMemory.instance();

            Tests.newTest("WaitFreeDequeueTest");

            // Subtest 1:
            // Constructor "public WaitFreeDequeue(java.lang.Thread writer,
            //                                     java.lang.Thread reader,
            //                                     int maximum,
            //                                     MemoryArea area)"
            //

            Tests.increment();
            try {
                System.out.println("WaitFreeDequeueTest: WaitFreeDequeue(Thread,"+
                                   "Thread,int,MemoryArea)");
                wfd = new WaitFreeDequeue(writer, reader, maximum, memory);
                if( !(wfd instanceof WaitFreeDequeue) )
                    throw new Exception("Return object is not instanceof "+
                                        "WaitFreeDequeue");
            } catch (Exception e) {
                System.out.println("WaitFreeDequeueTest: WaitFreeDequeue(Thread,"+
                                   "Thread,int,MemoryArea) failed");
                Tests.fail("new WaitFreeDequeue(writer,reader,maximum,memory",e);
            }

            // Subtest 2:
            // Method "public boolean nonBlockingWrite(java.lang.Object object)"
            // Method "public java.lang.Object nonBlockingRead()"
            //
            Tests.increment();
            try {
                System.out.println("WaitFreeDequeueTest: nonBlockingWrite("+
                                   "Object), nonBlockingRead()");
                int size = 2;
                wfd = new WaitFreeDequeue(writer, reader, size, memory);
                o = new Object();
                boolean x = wfd.nonBlockingWrite(o);
                if (x != true)
                    throw new Exception("Object could not be written");
                /*
                        Object y = wfd.nonBlockingRead();
                        if (o.equals(y) != true)
                        throw new Exception("Object written is different from object "+
                        "read");
                */
            } catch (Exception e) {
                System.out.println("WaitFreeDequeueTest: nonBlockingWrite("+
                                   "Object), nonBlockingRead() failed");
                Tests.fail("wfd.nonBlockingWrite(o)/wfd.nonBlockingRead()",e);
            }


            // Subtest 3:
            // Method "public boolean nonBlockingWrite()" where queue is full
            //
            Tests.increment();
            try {
                System.out.println("WaitFreeDequeueTest: nonBlockingWrite()");
                int size = 1;
                wfd = new WaitFreeDequeue(writer, reader, size, memory);
                o = new Object();
                Object y = new Object();
                boolean x = wfd.nonBlockingWrite(o);
                x = wfd.nonBlockingWrite(y);
                if (x != false)
                    throw new Exception("Object written to full queue");
            } catch (Exception e) {
                System.out.println("WaitFreeDequeueTest: nonBlockingWrite() "+
                                   "failed");
                Tests.fail("wfd.nonBlockingWrite(y)",e);
            }

            // Subtest 4:
            // Method "public boolean nonBlockingRead()" where queue is empty
            //
            Tests.increment();
            try {
                System.out.println("WaitFreeDequeueTest: nonBlockingRead()");
                int size = 1;
                wfd = new WaitFreeDequeue(writer, reader, size, memory);
                Object y = new Object();
                y = wfd.nonBlockingRead();
                if (y != null)
                    throw new Exception("Object read from empty queue");
            } catch (Exception e) {
                System.out.println("WaitFreeDequeueTest: nonBlockingRead() "+
                                   "failed");
                Tests.fail("wfd.nonBlockingRead()",e);
            }

            // Subtest 5:
            // Method "public boolean force(java.lang.Object)"
            //
            Tests.increment();
            try {
                System.out.println("WaitFreeDequeueTest: force(Object)");
                o = new Object();
                wfd = new WaitFreeDequeue(writer, reader, maximum, memory);
                boolean x = wfd.force(o);
                if (x != true)
                    throw new Exception("Did not return expected true value");
                Object y = new Object();
                x = wfd.force(y);
                if (x != true)
                    throw new Exception("Did not return expected true value");
            } catch (Exception e) {
                System.out.println("WaitFreeDequeueTest: force(Object) failed");
                Tests.fail("wfd.force(o)",e);
            }

            // Subtest 6:
            // Method "public void blockingRead()"
            //
            Tests.increment();
            try {
                System.out.println("WaitFreeDequeueTest: blockingRead()");
                MyWriteThread writeThread = new MyWriteThread(2);

                WaitFreeDequeue q = new WaitFreeDequeue( writeThread, reader,
                                                         maximum, memory);

                writeThread.setwfd(q);
                writeThread.start();
                Object y = q.blockingRead();
                if (y == null)
                    throw new Exception("Object could not be read from Queue");
            } catch (Exception e) {
                System.out.println("WaitFreeDequeueTest: blockingRead() failed");
                Tests.fail("q.blockingRead()",e);
            }

            // Subtest 7:
            // Method "public void blockingWrite()"
            //
            Tests.increment();
            try {
                System.out.println("WaitFreeDequeueTest: blockingWrite()");
                MyReadThread readThread = new MyReadThread(1);

                WaitFreeDequeue q = new WaitFreeDequeue( writer, readThread,
                                                         maximum, memory);

                readThread.setwfd(q);
                readThread.start();
                Object y = new Object();
                boolean x = q.blockingWrite(y);
                if (x != true)
                    throw new Exception("Object could not be written to Queue");
            } catch (Exception e) {
                System.out.println("WaitFreeDequeueTest: blockingWrite() failed");
                Tests.fail("q.blockingWrite(y)",e);
            }


            Tests.printSubTestReportTotals("WaitFreeDequeueTest");

        }
    }//WaitFreeDequeueTest_Thread

    class TestThread extends RealtimeThread{
        public TestThread(){
            super(null, null, null, ImmortalMemory.instance(), null, null);
        }
        public void run(){
            new WaitFreeDequeueTest_Thread();
        }
    };

    public static void run()
    {
        try{
            WaitFreeDequeueTest theTest = new WaitFreeDequeueTest();
            WaitFreeDequeueTest.TestThread thread = theTest.new TestThread();
            thread.start();
            thread.join();
        }
        catch(java.lang.InterruptedException e){
            System.out.println("Unexpected exception caught");
            Tests.fail("WaitFreeDequeueTest",e);
        }
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}//WaitFreeDequeueTest
