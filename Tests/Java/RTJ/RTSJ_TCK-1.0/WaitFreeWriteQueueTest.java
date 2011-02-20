//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              WaitFreeWriteQueueTest

Subtest 1:
        "public WaitFreeWriteQueue(java.lang.Thread writer, java.lang.Thread
        reader, int maximum, MemoryArea memory)"

Subtest 2:
        "public void clear()"

Subtest 3:
        "public boolean force(java.lang.Object)"

Subtest 4:
        "public void isEmpty()"

Subtest 5:
        "public void isFull()"

Subtest 6:
        "public int size()"

Subtest 7:
        "public boolean write(java.lang.Object object)"
        "public synchronized java.lang.Object read()"

Subtest 8:
        "public boolean write(java.lang.Object object)" where queue is full

Subtest 9:
        "public boolean read(java.lang.Object object)" where queue is empty

*/

import javax.realtime.*;

public class WaitFreeWriteQueueTest
{
    public static void run() {
        RealtimeThread rtthread
            = new RealtimeThread (null,null,null, ImmortalMemory.instance(), null, null){
            public void run(){
                WaitFreeWriteQueue wfwq = null;
                Object o = null;
                Thread writer = new Thread();
                Thread reader = new Thread();
                int maximum = 100;
                ImmortalMemory memory = ImmortalMemory.instance();

                Tests.newTest("WaitFreeWriteQueueTest: ");

                /* Subtest 1:
                ** Constructor "public WaitFreeWriteQueue(Thread writer,
                ** java.lang.Thread reader, int maximum, MemoryArea memory)"
                */
                Tests.increment();
                try {
                    System.out.println("WaitFreeWriteQueueTest: WaitFreeWriteQueue("+
                                       "Thread,Thread,int,MemoryArea)");
                    wfwq = new WaitFreeWriteQueue(writer, reader, maximum, memory);
                    if( !(wfwq instanceof WaitFreeWriteQueue) )
                        throw new Exception("Return object is not instanceof "+
                                            "WaitFreeWriteQueue");
                } catch (Exception e) {
                    System.out.println("WaitFreeWriteQueueTest: WaitFreeWriteQueue("+
                                       "Thread,Thread,int,MemoryArea) failed");
                    Tests.fail("new WaitFreeWriteQueue(writer,reader,maximum,memory)",
                               e);
                }


                /* Subtest 2:
                ** Method "public void clear()"
                */
                Tests.increment();
                System.out.println("WaitFreeWriteQueueTest: clear()");
                try {
                    System.out.println("WaitFreeWriteQueueTest: ");
                    wfwq = new WaitFreeWriteQueue(writer, reader, maximum, memory);
                    wfwq.clear();
                } catch (Exception e) {
                    System.out.println("WaitFreeWriteQueueTest: clear() failed");
                    Tests.fail("wfwq.clear()",e);
                }

                /* Subtest 3:
                ** Method "public boolean force(java.lang.Object)"
                */
                Tests.increment();
                try {
                    System.out.println("WaitFreeWriteQueueTest: force(Object)");
                    o = new Object();

                    wfwq = new WaitFreeWriteQueue(writer, reader, maximum, memory);

                    boolean x = wfwq.force(o);

                    if (x != true)
                        throw new Exception("Did not return expected true value");
                } catch (Exception e) {
                    System.out.println("WaitFreeWriteQueueTest: force(Object) failed");
                    Tests.fail("wfwq.force(o)",e);
                }

                /* Subtest 4:
                ** Method "public void isEmpty()"
                */

                Tests.increment();
                try {
                    System.out.println("WaitFreeWriteQueueTest: isEmpty()");
                    wfwq = new WaitFreeWriteQueue(writer, reader, maximum, memory);
                    boolean x = wfwq.isEmpty();
                    if (x != true)
                        throw new Exception("Thinks the queue is not empty when it "+
                                            "is");
                    o = new Object();
                    wfwq.write(o);
                    x = wfwq.isEmpty();
                    if (x != false)
                        throw new Exception("Thinks the queue is empty when it is "+
                                            "not");
                } catch (Exception e) {
                    System.out.println("WaitFreeWriteQueueTest: isEmpty() failed");
                    Tests.fail("wfwq.isEmpty()",e);
                }

                /* Subtest 5:
                ** Method "public void isFull()"
                */
                Tests.increment();
                try {
                    System.out.println("WaitFreeWriteQueueTest: isFull()");
                    wfwq = new WaitFreeWriteQueue(writer, reader, 1, memory);
                    boolean x = wfwq.isFull();
                    if (x != false)
                        throw new Exception("Thinks the queue is full when it is not");
                    o = new Object();
                    wfwq.write(o);
                    x = wfwq.isFull();
                    if (x != true)
                        throw new Exception("Thinks the queue is not full when it is");
                } catch (Exception e) {
                    System.out.println("WaitFreeWriteQueueTest: isFull() failed");
                    Tests.fail("wfwq.isFull()",e);
                }

                /* Subtest 6:
                ** Method "public int size()"
                */
                Tests.increment();
                try {
                    System.out.println("WaitFreeWriteQueueTest: size()");
                    int size = 2;
                    wfwq = new WaitFreeWriteQueue(writer, reader, size, memory);
                    int i = wfwq.size();
                    if (i != size)
                        throw new Exception("Invalid size returned on empty queue");
                    o = new Object();
                    wfwq.write(o);
                    i = wfwq.size();
                    if (i != (size-1))
                        throw new Exception("Invalid size returned on non-empty "+
                                            "queue");
                    o = new Object();
                    wfwq.write(o);
                    i = wfwq.size();
                    if (i != 0)
                        throw new Exception("Invalid size returned on full queue");
                } catch (Exception e) {
                    System.out.println("WaitFreeWriteQueueTest: size() failed");
                    Tests.fail("wfwq.size()",e);
                }

                /* Subtest 7:
                ** Method "public boolean write(java.lang.Object object)"
                ** Method "public synchronized java.lang.Object read()"
                */
                Tests.increment();
                try {
                    System.out.println("WaitFreeWriteQueueTest: write(Object), "+
                                       "read()");
                    int size = 2;
                    wfwq = new WaitFreeWriteQueue(writer, reader, size, memory);
                    o = new Object();
                    boolean x = wfwq.write(o);
                    if (x != true)
                        throw new Exception("Object could not be written");
                    Object y = wfwq.read();
                    if (o.equals(y) != true)
                        throw new Exception("Object written is different from object "+
                                            "read");
                } catch (Exception e) {
                    System.out.println("WaitFreeWriteQueueTest: write(Object), "+
                                       "read() failed");
                    Tests.fail("wfwq.write(o)/wfwq.read()",e);
                }


                /* Subtest 8:
                ** Method "public boolean write(java.lang.Object object)" where queue
                **         is full
                */
                Tests.increment();
                try {
                    System.out.println("WaitFreeWriteQueueTest: write(Object)");
                    int size = 1;
                    wfwq = new WaitFreeWriteQueue(writer, reader, size, memory);
                    o = new Object();
                    Object y = new Object();
                    boolean x = wfwq.write(o);
                    x = wfwq.write(y);
                    if (x != false)
                        throw new Exception("Object written to full queue");
                } catch (Exception e) {
                    System.out.println("WaitFreeWriteQueueTest: write(Object) failed");
                    Tests.fail("wfwq.write(y)",e);
                }

                /* Subtest 9:
                ** Method "public synchronized java.lang.Object read()" where
                ** queue is empty
                */
                Tests.increment();
                try {
                    System.out.println("WaitFreeWriteQueueTest: read()");
                    MyWriteThread writeThread = new MyWriteThread(3);

                    WaitFreeWriteQueue writeQ
                        = new WaitFreeWriteQueue( writeThread,
                                                  reader,
                                                  maximum,
                                                  memory);

                    writeThread.setwfwQ(writeQ);
                    writeThread.start();
                    Object y = writeQ.read();
                    if (y == null)
                        throw new Exception("Object could not be read from Queue");
                } catch (Exception e) {
                    System.out.println("WaitFreeWriteQueueTest: read() failed");
                    Tests.fail("writeQ.read()",e);
                }

                Tests.printSubTestReportTotals("WaitFreeWriteQueueTest");
            }
                };
        rtthread.start();
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
