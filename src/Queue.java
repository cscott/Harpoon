// Queue.java, created by wbeebee, modified by harveyj
// Copyright (C) 2003 Wes Beebee, Harvey Jones
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.Thread;
import java.lang.Object; 
/** The queue class provides blocking and non-blocking reads and writes
 *  to the same queue.  It combines the functionality of <code>WaitFreeReadQueue</code>,
 *  <code>WaitFreeWriteQueue</code>, and <code>WaitFreeDequeue</code> and adds 
 *  functionality in one generic class.  It permits multiple reading 
 *  and writing threads. It automatically allocates objects in the same MemoryArea
 *  as the queue is allocated.  This class is SMP-safe under concurrent updates.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 * @author Harvey Jones <<a href="mailto:harveyj@mit.edu">harveyj@mit.edu</a>>
 */
public class Queue {
    private Object notify;
    private Object[] queue;
    /* The high 16-bits are the write index, the low 16-bits are the read index. 
     *
     * Note that I use AtomicInteger rather than AtomicLong - this is because the Pentium II
     * does not have native 64-bit atomic updates.
     */
    private AtomicInteger indices = new AtomicInteger(0);
    private MemoryArea ma = null;

    private final static int MAX_SIZE=65536;
    private final static int WRITE_MASK = -MAX_SIZE;
    private final static int READ_MASK = MAX_SIZE-1;

    /** A queue with blocking and nonblocking reads and writes. 
     *
     *  @param maximum The maximum number of elements in the queue.
     *  @throws java.lang.IllegalArgumentException
     */
    public Queue(int maximum)
	throws IllegalArgumentException {
	this(maximum, null);
    }

    /** A queue with blocking and nonblocking reads and writes. 
     *  
     *  @param notify The object to notify when data is added. 
     *                If <code>notify</code> is <code>null</code>, 
     *                no object is notified.
     *  @param maximum The maximum number of elements in the queue.
     *  @throws java.lang.IllegalArgumentException
     */
    public Queue(int maximum, Object notify) 
	throws IllegalArgumentException {
	this.notify = notify;
	if ((maximum<1)||(maximum>MAX_SIZE)) {
	    throw new IllegalArgumentException("Maximum must be at least 1 and no more than " + MAX_SIZE +".");
	}
	this.queue = new Object[maximum];

    }

    public Queue(int maximum, Object notify, MemoryArea ma){
	this(maximum, notify);
	this.ma=ma;
    }

    /** Set <code>this</code> to empty. */
    public void clear() {
	int idx;
	while (!indices.compareAndSet(idx=indices.get(),
				      (((idx&WRITE_MASK)/MAX_SIZE)&READ_MASK)*(MAX_SIZE+1))) {}
    }

    /** Queries the queue to determine if <code>this</code> is empty.
     *  
     *  @return True, if <code>this</code> is empty. False, if 
     *          <code>this</code> is not empty.
     */
    public boolean isEmpty() {
	int idx = indices.get();
	return (((idx&WRITE_MASK)/MAX_SIZE)&READ_MASK)==(idx&READ_MASK);
    }

    /** Queries the queue to determine if <code>this</code> is full.
     *
     *  @return True, if <code>this</code> is full. False, if
     *          <code>this</code> is not full.
     */
    public boolean isFull() {
	int idx = indices.get();
	return ((((idx&WRITE_MASK)/MAX_SIZE)&READ_MASK)+1)%queue.length==(idx&READ_MASK);
    }
    
    /** Reads the next element in the queue unless the queue is empty.
     *  If the queue is empty, <code>null</code> is returned.
     *
     *  @return The instance of <code>java.lang.Object</code> read.
     *          <code>null</code>, if <code>this</code> was empty.
     *          Also, a <code>null</code> is returned if a concurrent
     *          update interrupted this read.  In either case, the correct
     *          behavior is to try again.
     */
    public synchronized Object nonBlockingRead() {
	int idx = indices.get();
	int readIdx = idx&READ_MASK;
	if ((((idx&WRITE_MASK)/MAX_SIZE)&READ_MASK) == readIdx) {
	    return null;
	} else if (indices.compareAndSet(idx, (idx&WRITE_MASK)|((readIdx+1)%queue.length))) {
	    this.notify();
	    return queue[readIdx];
	} else {
	    return null;
	}
    }

    /** Attempt to insert an element into the queue.
     *
     *  @param object The <code>java.lang.Object</code> to insert.
     *  @return True, if the write succeeded. False, if not.
     *  @throws MemoryScopeException
     */
    public boolean nonBlockingWrite(Object object) throws MemoryScopeException {
	ma.checkAccess(object);
	int idx = indices.get();
	int write = ((idx&WRITE_MASK)/MAX_SIZE)&READ_MASK;
	int newWrite = (write+1)%queue.length;
	int readIdx = idx&READ_MASK;
	if (newWrite==readIdx) {
	    return false;
	} else if (indices.compareAndSet(idx, (newWrite*MAX_SIZE)|readIdx)) {
	    queue[write]=object;
	    this.notify();
	    return true;
	} else {
	    return false;
	}
    }

    /** Determines the number of elements in <code>this</code>. */
    public int size() {
	int idx = indices.get();
	return ((((idx&WRITE_MASK)/MAX_SIZE)&READ_MASK)+queue.length-(idx&READ_MASK))%queue.length;
    }

    /** A synchronized and blocking read on the queue.
     * 
     *  @return The <code>java.lang.Object</code> read or null if
     *          <code>this</code> is empty.
     */
    public synchronized Object blockingRead() {
	while (true) {
	    int idx = indices.get();
	    int readIdx = idx&READ_MASK;
	    if ((((idx&WRITE_MASK)/MAX_SIZE)&READ_MASK) == readIdx) {
		waitForData();
		continue;
	    } else if (indices.compareAndSet(idx, (idx&WRITE_MASK)|((readIdx+1)%queue.length))) {
		Object tmp = queue[readIdx];
		this.notify();
		return tmp;
	    } else {
		/* Avoid deadly embrace */
		
		continue;
	    }
	}
    }

    /** A synchronized and blocking write.  This call blocks on
     *  queue full and will wait until there is space in the queue.
     *  
     *  @param object The <code>java.lang.Object</code> that is placed in
     *                <code>this</code>.
     *  @return True, if the write occurred.  False, if it did not.
     *  @throws MemoryScopeException.
     */
    public synchronized boolean blockingWrite(Object object) throws MemoryScopeException {
	ma.checkAccess(object);
	
	while (true) {
	    int idx = indices.get();
	    int write = ((idx&WRITE_MASK)/MAX_SIZE)&READ_MASK;
	    int newWrite = (write+1)%queue.length;
	    int readIdx = idx&READ_MASK;
	    if (newWrite==readIdx) {
		waitForRead();
		continue;
	    } else if (indices.compareAndSet(idx, (newWrite*MAX_SIZE)|readIdx)) {
		this.notify();
		queue[write]=object;
		break;
	    } else {
		/* Avoid deadly embrace */

		continue;
	    }
	}
	return true;
    }
    
    /** If <code>this</code> is empty <code>waitForData()</code> waits on
     *  the event until the writer inserts data. Note that true priority
     *  inversion does not occur since the writer locks a different object
     *  and the notify is executed by the <code>AsyncEventHandler</code>
     *  which has <code>noHeap</code> characteristics.
     */
    public void waitForData() {
	while(isEmpty())
	    this.wait();
    }

    /** If <code>this</code> is full <code>waitForRead()</code> waits on
     *  the event until the reader reads data. Note that true priority
     *  inversion does not occur since the reader locks a different object
     *  and the notify is executed by the <code>AsyncEventHandler</code>
     *  which has <code>noHeap</code> characteristics.
     */
    public void waitForRead() {
	while(isFull())
	    this.wait();
    }
	
    /** Force this <code>java.lang.Object</code> to replace the last one.
     *  If the reader should happen to have just removed the other
     *  <code>java.lang.Object</code> just as we were updating it, we will
     *  return false. False may mean that it just saw that we put in there.
     *  Either way, the best thing to do is to just write again -- which
     *  will succeed, and check on the readers side for consecutive
     *  identical read values.
     *
     *  @return True, if an element was overwritten. False, if there is an
     *          empty element into which the write occured.
     *  @throws MemoryScopeException
     */
    public boolean force(Object object) throws MemoryScopeException {
	ma.checkAccess(object);

	int idx = indices.get();
	int write = ((idx&WRITE_MASK)/MAX_SIZE)&READ_MASK;
	queue[(write+queue.length-1)%queue.length]=object;
	return idx == indices.get();
    }
}
