package javax.realtime;

public class ThreadedAsyncEventHandler extends AsyncEventHandler {

    /**
     * Creates a new <code>ThreadedAsyncEventHandler</code> instance whose
     * {@link SchedulingParameters} are inherited from the current thread
     * and does not have either {@link ReleaseParameters} or
     * {@link MemoryParameters}.
     *
     */
    public ThreadedAsyncEventHandler() {
        super(null, null, null, null, null, false, null);
    }

    /**
     * Creates a new <code>ThreadedAsyncEventHandler</code> instance whose
     * parameters are inherited from the current thread, if it is a
     * {@link RealtimeThread} ,or null other wise.
     *
     * @param noHeap A flag meaning, when <code>true</code>, that this
     * will have characteristics identical to a {@link
     * NoHeapRealtimeThread}. A false value means this will have
     * characteristics identical to a {@link RealtimeThread}. If
     * <code>true</code> and the current thread is not a {@link
     * NoHeapRealtimeThread} or a {@link RealtimeThread} executing
     * within a {@link ScopedMemory} or {@link ImmortalMemory} scope
     * then an {@link IllegalArgumentException} is thrown.
     */
    public ThreadedAsyncEventHandler(boolean noHeap) {
        super(null, null, null, null, null, noHeap, null);
    } 

    /**
     * Creates a new <code>ThreadedAsyncEventHandler</code> instance whose
     * parameters are inherited from the current thread, if it is a
     * {@link RealtimeThread}, or null otherwise.
     *
     * @param logic The <code>java.lang.Runnable</code> object whose
     * run is executed by handleAsyncEvent.
     */
    public ThreadedAsyncEventHandler(Runnable logic) {
        this(null, null, null, null, null, false, logic);
    }


    /**
     * Creates a new <code>ThreadedAsyncEventHandler</code> instance whose
     * parameters are inherited from the current thread, if it is a
     * {@link RealtimeThread}, or null otherwise.
     *
     * @param noHeap A flag meaning, when <code>true</code>, that this
     * will have characteristics identical to a {@link
     * NoHeapRealtimeThread}. A false value means this will have
     * characteristics identical to a {@link RealtimeThread}. If
     * <code>true</code> and the current thread is not a {@link
     * NoHeapRealtimeThread} or a {@link RealtimeThread} executing
     * within a {@link ScopedMemory} or {@link ImmortalMemory} scope
     * then an {@link IllegalArgumentException} is thrown.
     * @param logic -The <code>java.lang.Runnable</code> object whose
     * run is executed by handleAsyncEvent.
     * @exception IllegalArgumentException 
     */
    public ThreadedAsyncEventHandler(boolean noHeap, Runnable logic) throws IllegalArgumentException {
        super(null, null, null, null, null, noHeap, logic);
    }


    /**
     * Creates a new <code>ThreadedAsyncEventHandler</code> instance with the
     * specified parameters.
     *
     * @param schedulingParam a <code>SchedulingParameters</code> value
     * which will be associated with the constructed instance of this. 
     * If <code>null</code> this will be assigned the reference to the
     * {@link SchedulingParameters} of the current thread.
     * @param releaseParam a <code>ReleaseParameters</code> value
     * which will be associated with the constructed instance of this. 
     * If null this will have no {@link ReleaseParameters}.
     * @param memoryParam a <code>MemoryParameters</code> value which
     * will be associated with the constructed instance of this. If
     * null this will have no {@link MemoryParameters}.
     * @param memoryArea The {@link MemoryArea} for this
     * <code>ThreadedAsyncEventHandler</code>. If null, inherit the current
     * memory area at the time of construction. The initial memory
     * area must be a reference to a {@link ScopedMemory} or {@link
     * ImmortalMemory} object if <code>noheap</code> is
     * <code>true</code>.
     * @param groupParam A {@link ProcessingGroupParameters} object
     * to which this will be associated. If null this will not be
     * associated with any processing group.
     * @param nonHeap A flag meaning, when <code>true</code>, that this
     * will have characteristics identical to a {@link
     * NoHeapRealtimeThread}. A false value means this will have
     * characteristics identical to a {@link RealtimeThread}. If
     * <code>true</code> and the current thread is not a {@link
     * NoHeapRealtimeThread} or a {@link RealtimeThread} executing
     * within a {@link ScopedMemory} or {@link ImmortalMemory} scope
     * then an {@link IllegalArgumentException} is thrown.
     * @exception IllegalArgumentException 
     */
    public ThreadedAsyncEventHandler(SchedulingParameters schedulingParam,
                             ReleaseParameters releaseParam,
                             MemoryParameters memoryParam,
                             MemoryArea memoryArea,
                             ProcessingGroupParameters groupParam,
                             boolean noHeap) throws IllegalArgumentException {

        super(schedulingParam, releaseParam, memoryParam, memoryArea, groupParam, noHeap, null);
    }


    
    /**
     * Creates a new <code>ThreadedAsyncEventHandler</code> instance with the
     * specified parameters.
     *
     * @param schedulingParam a <code>SchedulingParameters</code> value
     * which will be associated with the constructed instance of this. 
     * If <code>null</code> this will be assigned the reference to the
     * {@link SchedulingParameters} of the current thread.
     * @param releaseParam a <code>ReleaseParameters</code> value
     * which will be associated with the constructed instance of this. 
     * If null this will have no {@link ReleaseParameters}.
     * @param memoryParam a <code>MemoryParameters</code> value which
     * will be associated with the constructed instance of this. If
     * null this will have no {@link MemoryParameters}.
     * @param memoryArea The {@link MemoryArea} for this
     * <code>ThreadedAsyncEventHandler</code>. If null, inherit the current
     * memory area at the time of construction. The initial memory
     * area must be a reference to a {@link ScopedMemory} or {@link
     * ImmortalMemory} object if <code>noheap</code> is
     * <code>true</code>.
     * @param groupParam A {@link ProcessingGroupParameters} object
     * to which this will be associated. If null this will not be
     * associated with any processing group.
     * @param logic The <code>java.lang.Runnable</code> object whose
     * run is executed by handleAsyncEvent.
     * @exception IllegalArgumentException 
     */
    public ThreadedAsyncEventHandler(SchedulingParameters schedulingParam,
                             ReleaseParameters releaseParam,
                             MemoryParameters memoryParam,
                             MemoryArea memoryArea,
                             ProcessingGroupParameters groupParam,
                             Runnable logic) throws IllegalArgumentException {

        super(schedulingParam, releaseParam, memoryParam, memoryArea, groupParam, false, logic);
    }
    

    /**
     * Creates a new <code>ThreadedAsyncEventHandler</code> instance with the
     * specified parameters.
     *
     * @param schedulingParam a <code>SchedulingParameters</code> value
     * which will be associated with the constructed instance of this. 
     * If <code>null</code> this will be assigned the reference to the
     * {@link SchedulingParameters} of the current thread.
     * @param releaseParam a <code>ReleaseParameters</code> value
     * which will be associated with the constructed instance of this. 
     * If null this will have no {@link ReleaseParameters}.
     * @param memoryParam a <code>MemoryParameters</code> value which
     * will be associated with the constructed instance of this. If
     * null this will have no {@link MemoryParameters}.
     * @param memoryArea The {@link MemoryArea} for this
     * <code>ThreadedAsyncEventHandler</code>. If null, inherit the current
     * memory area at the time of construction. The initial memory
     * area must be a reference to a {@link ScopedMemory} or {@link
     * ImmortalMemory} object if <code>noheap</code> is
     * <code>true</code>.
     * @param groupParam A {@link ProcessingGroupParameters} object
     * to which this will be associated. If null this will not be
     * associated with any processing group.
     * @param nonHeap A flag meaning, when <code>true</code>, that this
     * will have characteristics identical to a {@link
     * NoHeapRealtimeThread}. A false value means this will have
     * characteristics identical to a {@link RealtimeThread}. If
     * <code>true</code> and the current thread is not a {@link
     * NoHeapRealtimeThread} or a {@link RealtimeThread} executing
     * within a {@link ScopedMemory} or {@link ImmortalMemory} scope
     * then an {@link IllegalArgumentException} is thrown.
     * @param logic The <code>java.lang.Runnable</code> object whose
     * run is executed by handleAsyncEvent.
     * @exception IllegalArgumentException 
     */
    public ThreadedAsyncEventHandler(SchedulingParameters schedulingParam,
                             ReleaseParameters releaseParam,
                             MemoryParameters memoryParam,
                             MemoryArea memoryArea,
                             ProcessingGroupParameters groupParam,
                             boolean nonHeap,
                             Runnable logic) throws IllegalArgumentException {
        super(schedulingParam,
              releaseParam,
              memoryParam,
              memoryArea,
              groupParam,
              nonHeap,
              logic);
              
    }
}
