package javax.realtime;

/** The wait-free classes facilitate communication and synchronization
 *  between instances of <code>RealtimeThread</code> and <code>Thread</code>.
 */
public class WaitFreeDequeue {

    protected Object[] queue = null;
    protected int queueSize;
    protected int currentIndex = 0;
    
    protected Thread writerThread = null, readerThread = null;
    protected MemoryArea memArea;

    /** A queue with unsynchronized and nonblocking <code>read()</code>
     *  and <code>write()</code> methods and synchronized and blocking
     *  <code>read()</code> and <code>write()</code> methods.
     *
     *  @param writer An instance of <code>Thread</code>.
     *  @param reader An instance of <code>Thread</code>.
     *  @param maximum The maximum number of elements in both the
     *                 <code>WaitFreeReadQueue</code> and the
     *                 <code>WaitFreeWriteQueue</code>.
     *  @param area The <code>MemoryArea</code> in which this object and
     *              internal elements are allocated
     *  @throws java.lang.IllegalArgumentException If an argument holds an
     *                                             invalid value. The current
     *                                             memory areas of
     *                                             <code>writer</code>,
     *                                             <code>reader</code>, and
     *                                             <code>memory</code> must be
     *                                             compatible with respect to
     *                                             the assignment and access
     *                                             rules for memory areas.
     */
    public WaitFreeDequeue(Thread writer, Thread reader,
			   int maximum, MemoryArea area)
	throws IllegalArgumentException {
	writerThread = writer;
	readerThread = reader;
	queueSize = maximum;
	memArea = area;

	// TODO (?)
    }

    /** Used to check if the queue is empty.
     *
     *  @return True, if the queue is empty. False, if it is not.
     */
    private boolean isEmpty() {
	return (currentIndex == 0);
    }

    /** Used to check if the queue is full.
     *
     *  @return True, if the queue is full. False, if it is not.
     */
    private boolean isFull() {
	return (currentIndex == queueSize - 1);
    }

    /** A synchronized call of the <code>read()</code> method of the
     *  underlying <code>WaitFreeWriteQueue</code>. This call blocks on
     *  queue empty and will wait until there is an element in the queue
     *  to return.
     *
     *  @return The <code>java.lang.Object</code> read.
     */
    public synchronized Object blockingRead() {
	while (isEmpty())
	    try {
		Thread.sleep(100);
	    } catch (Exception e) {};
	
	Object temp = queue[0];
	for (int i = 0; i < currentIndex; i++)
	    queue[i] = queue[i+1];
	currentIndex--;
	return temp;
    }

    /** A synchronized call of the <code>write()</code> method of the
     *  underlying <code>WaitFreeReadQueue</code>. This call blocks on
     *  queue full and waits until there is space in <code>this</code>.
     *
     *  @param object The <code>java.lang.Object</code> to place in
     *                <code>this</code>.
     *  @return True, if the write succeeded. False, if not.
     *  @throws MemoryScopeException If the write causes an access or
     *                               assignment violation.
     */
    public synchronized boolean blockingWrite(Object object)
	throws MemoryScopeException {
	while (isFull())
	    try {
		Thread.sleep(100);
	    } catch (Exception e) {};

	queue[++currentIndex] = object;
	return true;
    }

    /** If <code>this</code> is full then this call overwrites the
     *  last object written to <code>this</code> with the given
     *  object. If this is not full this call is equivalent to the
     *  <code>nonBlockingWrite()</code> call.
     *
     *  @param object The <code>java.lang.Object which will overwrite
     *                the last object if <code>this</code> is full.
     *                Otherwise <code>object</code> will be placed in
     *                <code>this</code.
     *  @return True, if an element was overwritten. False, if there
     *          is an empty element into which the write occured.
     */
    public boolean force(Object object) {
	if (!isFull()) {
	    boolean b = false;
	    try {
		b = nonBlockingWrite(object);
	    } catch (Exception e) {}
	    return b;
	}
	else {
	    queue[currentIndex] = object;
	    return true;
	}
    }

    /** An unsynchronized call of the <code>read()</code> method of the
     *  underlying <code>WaitFreeReadQueue</code>.
     *
     *  @return A <code>java.lang.Object</code> object read from
     *          <code>this</code>. If there are no elements in
     *          <code>this</code> then null is returned.
     */
    public Object nonBlockingRead() {
	if (isEmpty()) return null;
	else {
	    Object temp = queue[0];
	    for (int i = 0; i < currentIndex; i++)
		queue[i] = queue[i+1];
	    currentIndex--;
	    return temp;
	}
    }

    /** An unsynchronized call of the <code>write()</code> method of the
     *  underlying <code>WaitFreeWriteQueue</code>. This call does not
     *  block on queue full.
     *
     *  @param object The <code>java.lang.Object</code> to attempt to
     *                place in <code>this</code>.
     *  @return True, if the object is now in <code>this</code>, otherwise
     *          returns false.
     *  @throws MemoryScopeException If the write causes an access or
     *                               assignment violation.
     */
    public boolean nonBlockingWrite(Object object)
	throws MemoryScopeException {
	if (isFull()) return false;
	else {
	    queue[++currentIndex] = object;
	    return true;
	}
    }
}
