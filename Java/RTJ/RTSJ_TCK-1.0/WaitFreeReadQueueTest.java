//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              WaitFreeReadQueueTest

Subtest 1:
        "public WaitFreeReadQueue(java.lang.Thread writer, java.lang.Thread
        reader, int maximum, MemoryArea memory)"

Subtest 2:
        "public  WaitFreeReadQueue(java.lang.Thread writer, java.lang.Thread
        reader, int maximum, MemoryArea memory, boolean notify)"

Subtest 3:
        "public void clear()"

Subtest 4:
        "public void isEmpty()"

Subtest 5:
        "public void isFull()"

Subtest 6:
        "public int size()"

Subtest 7:
        "public boolean write()"
        "public boolean read()"

Subtest 8:
        "public boolean read()" where queue is empty

Subtest 9:
        "public void waitForData()"

Subtest 10:
        "public boolean write(java.lang.Object object)" where queue is full
*/

import javax.realtime.*;

public class WaitFreeReadQueueTest
{

    public static void run()
    {
        RealtimeThread rtthread
            = new RealtimeThread (null,null,null,
                                  ImmortalMemory.instance(),
                                  null, null){
            public void run()
            {
                WaitFreeReadQueue wfrq = null;
                Object o = null;
                Thread writer = new Thread();
                Thread reader = new Thread();
                int maximum=100;
                ImmortalMemory memory = ImmortalMemory.instance();

                Tests.newTest("WaitFreeReadQueueTest");

                /* Subtest 1:
                ** Constructor "public WaitFreeReadQueue(java.lang.Thread writer,
                ** java.lang.Thread reader, int maximum, MemoryArea memory)"
                */
                Tests.increment();
                try {
                    System.out.println("WaitFreeReadQueueTest: WaitFreeReadQueue("+
                                       "Thread,Thread,int,MemoryArea)");
                    wfrq = new WaitFreeReadQueue(writer, reader, maximum, memory);
                    if( !(wfrq instanceof WaitFreeReadQueue) )
                        throw new Exception("Return object is not instanceof WaitFree"+
                                            "ReadQueue");
                } catch (Exception e) {
                    System.out.println("WaitFreeReadQueueTest: WaitFreeReadQueue("+
                                       "Thread,Thread,int,MemoryArea) failed");
                    Tests.fail("new WaitFreeReadQueue(writer,reader,maximum,memory)",
                               e);
                }

                /* Subtest 2:
                ** Constructor "public WaitFreeReadQueue(java.lang.Thread writer,
                ** java.lang.Thread reader, int maximum, MemoryArea memory, boolean
                ** notify)"
                */

                Tests.increment();
                try {
                    System.out.println("WaitFreeReadQueueTest: WaitFreeReadQueue("+
                                       "Thread,Thread,int,MemoryArea,boolean)");
                    wfrq = new WaitFreeReadQueue(writer, reader, maximum, memory,
                                                 false);
                    if( !(wfrq instanceof WaitFreeReadQueue) )
                        throw new Exception("Return object is not instanceof "+
                                            "WaitFreeReadQueue");
                } catch (Exception e) {
                    System.out.println("WaitFreeReadQueueTest: WaitFreeReadQueue("+
                                       "Thread,Thread,int,MemoryArea,boolean) failed");
                    Tests.fail("new WaitFreeReadQueue(writer,reader,maximum,memory)",
                               e);
                }

                /* Subtest 3:
                ** Method "public void clear()"
                */
                Tests.increment();
                try {
                    System.out.println("WaitFreeReadQueueTest: clear()");
                    wfrq = new WaitFreeReadQueue(writer, reader, maximum, memory);
                    wfrq.clear();
                } catch (Exception e) {
                    System.out.println("WaitFreeReadQueueTest: clear() failed");
                    Tests.fail("wfrq.clear()",e);
                }

                /* Subtest 4:
                ** Method "public void isEmpty()"
                */
                Tests.increment();
                try {
                    System.out.println("WaitFreeReadQueueTest: isEmpty()");
                    wfrq = new WaitFreeReadQueue(writer, reader, maximum, memory);
                    boolean x = wfrq.isEmpty();
                    if (x != true)
                        throw new Exception("Thinks the queue is not empty when it "+
                                            "is");
                    o = new Object();
                    wfrq.write(o);
                    x = wfrq.isEmpty();
                    if (x != false)
                        throw new Exception("Thinks the queue is empty when it is "+
                                            "not");
                } catch (Exception e) {
                    System.out.println("WaitFreeReadQueueTest: isEmpty() failed");
                    Tests.fail("wfrq.isEmpty()",e);
                }

                /* Subtest 5:
                ** Method "public void isFull()"
                */
                Tests.increment();
                try {
                    System.out.println("WaitFreeReadQueueTest: isFull()");
                    wfrq = new WaitFreeReadQueue(writer, reader, 1, memory);
                    boolean x = wfrq.isFull();
                    if (x != false)
                        throw new Exception("Thinks the queue is full when it is not");
                    o = new Object();
                    wfrq.write(o);
                    x = wfrq.isFull();
                    if (x != true)
                        throw new Exception("Thinks the queue is not full when it is");
                } catch (Exception e) {
                    System.out.println("WaitFreeReadQueueTest: isFull() failed");
                    Tests.fail("wfrq.isFull()",e);
                }

                /* Subtest 6:
                ** Method "public int size()"
                */
                Tests.increment();
                try {
                    System.out.println("WaitFreeReadQueueTest: size()");
                    int size = 2;
                    wfrq = new WaitFreeReadQueue(writer, reader, size, memory);
                    int i = wfrq.size();
                    if (i != size)
                        throw new Exception("Invalid size returned on empty queue");
                    o = new Object();
                    wfrq.write(o);
                    i = wfrq.size();
                    if (i != (size-1))
                        throw new Exception("Invalid size returned on non-empty "+
                                            "queue");
                    o = new Object();
                    wfrq.write(o);
                    i = wfrq.size();
                    if (i != 0)
                        throw new Exception("Invalid size returned on full queue");
                } catch (Exception e) {
                    System.out.println("WaitFreeReadQueueTest: size() failed");
                    Tests.fail("wfrq.size()",e);
                }

                /* Subtest 7:
                ** Method "public boolean write()"
                ** Method "public boolean read()"
                */
                Tests.increment();
                try {
                    System.out.println("WaitFreeReadQueueTest: write(Object), read()");
                    int size = 2;
                    wfrq = new WaitFreeReadQueue(writer, reader, size, memory);
                    o = new Object();
                    boolean x = wfrq.write(o);
                    if (x != true)
                        throw new Exception("Object could not be written");
                    Object y = wfrq.read();
                    if (o.equals(y) != true)
                        throw new Exception("Object written is different from object "+
                                            "read");
                } catch (Exception e) {
                    System.out.println("WaitFreeReadQueueTest: write(Object), read() "+
                                       "failed");
                    Tests.fail("wfrq.write(o)/wfrq.read()",e);
                }


                /* Subtest 8:
                ** Method "public boolean read()" where queue is empty
                */
                Tests.increment();
                try {
                    System.out.println("WaitFreeReadQueueTest: read()");
                    int size = 1;
                    wfrq = new WaitFreeReadQueue(writer, reader, size, memory);
                    Object y = new Object();
                    y = wfrq.read();
                    if (y != null)
                        throw new Exception("Object read from empty queue");
                } catch (Exception e) {
                    System.out.println("WaitFreeReadQueueTest: read() failed");
                    Tests.fail("wfrq.read()",e);
                }

                /* Subtest 9:
                ** Method "public void waitForData()"
                */
                Tests.increment();
                try {
                    System.out.println("WaitFreeReadQueueTest: waitForData()");
                    MyWriteThread writeThread = new MyWriteThread(1);

                    WaitFreeReadQueue readQ
                        = new WaitFreeReadQueue( writeThread,
                                                 reader, maximum,
                                                 memory, false);

                    writeThread.setwfrQ(readQ);
                    writeThread.start();
                    readQ.waitForData();
                    sleep(1000);
                    Object y = readQ.read();
                    if (y == null)
                        throw new Exception("Object could not be read from Queue");
                } catch (Exception e) {
                    System.out.println("WaitFreeReadQueueTest: waitForData() failed");
                    Tests.fail("wfrq.waitForData()",e);
                }

                /* Subtest 10:
                ** Method "public boolean write(java.lang.Object object)" where queue
                ** is full
                */
                Tests.increment();
                try {
                    System.out.println("WaitFreeReadQueueTest: write(Object)");
                    int size = 1;
                    MyReadThread readThread = new MyReadThread(2);
                    wfrq = new WaitFreeReadQueue(writer, readThread, size, memory);
                    o = new Object();
                    Object y = new Object();
                    boolean x = wfrq.write(o);
                    readThread.setwfrQ(wfrq);
                    readThread.start();
                    x = wfrq.write(y);
                    if (x != true)
                        throw new Exception("Object not written to queue");
                } catch (Exception e) {
                    System.out.println("WaitFreeReadQueueTest: write(Object) failed");
                    Tests.fail("wfrq.write(y)",e);
                }

                Tests.printSubTestReportTotals("WaitFreeReadQueueTest");
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
