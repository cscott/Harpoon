// RealtimeSystem.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>RealtimeSystem</code> provides a means for tuning the behavior
 *  of the implementation by specifying parameters such as the maximum
 *  number of locks that can be in use concurrently, and the monitor 
 *  control policy.  In addition, <code>RealtimeSystem</code> provides
 *  a mechanism for obtaining access to the security manager, garbage
 *  collector and scheduler, to make queries from them or to set 
 *  parameters.
 */
public class RealtimeSystem {

    /** Value to set the byte ordering for the underlying hardware. */
    public static final byte BIG_ENDIAN = 0;
    /** Value to set the byte ordering for the underlying hardware. */
    public static final byte LITTLE_ENDIAN = 1;    
    /** The byte ordering of the underlying hardware. */    
    public static final byte BYTE_ORDER = 2;

    private static GarbageCollector garbageCollector = null;
    private static RealtimeSecurity securityManager = null;
    private static int maxConcurrentLocks;
    private static int concurrentLocksUsed;
    private static boolean maxHard;

    public RealtimeSystem() {
	maxConcurrentLocks = 0;
	concurrentLocksUsed = 0;
	maxHard = false;
    }

    /** Native call to retrieve the current <code>GarbageCollector</code>. */
    native static GarbageCollector getCurrentGC();

    /** Return a reference to the currently active garbage collector for the heap.
     *
     *  @return A <code>GarbageCollector</code> object which is the current
     *          collector collecting objects on the traditional Java heap.
     */
    public static GarbageCollector currentGC() {
	if (garbageCollector == null) {
	    garbageCollector = getCurrentGC();
	}
	return garbageCollector;
    }

    /** Gets the maximum number of locks that have been used concurrently.
     *  This value can be used for tuning the concurent locks parameter
     *  which is used as a hint by systems that use a monitor cache.
     *
     *  @return An intenger whose value is the number of locks in use at
     *          the time of the invocation of the method.
     */
    public static int getConcurrentLocksUsed() {
	return concurrentLocksUsed;
    }

    /** Gets the maximum number of locks that can be used concurrently
     *  without incurring an execution time increase as set by the
     *  <code>setMaximumConcurrentLocks()</code> methods.
     *
     *  @return An integer whose value is the maximum number of locks
     *          that can be in simultaneous use.
     */
    public static int getMaximumConcurrentLocks() {
	return maxConcurrentLocks;
    }

    /** Gets a reference to the security manager used to control access
     *  to real-time system features such as access to physical memory.
     *
     *  @return A <code>RealtimeSecurity</code> object representing the
     *          default realtime security manager.
     */
    public static RealtimeSecurity getSecurityManager() {
	return securityManager;
    }

    /** Sets the anticipated maximum number of locks that may be held
     *  or waited on concurrently.  Provide a hint to systems that use
     *  a monitor cache as to how much space to dedicate to the cache.
     *
     *  @param number An integer whose value becomes the number of locks
     *                that can be in simultaneous use without incurring
     *                an execution time increase. If <code>number</code>
     *                is less than or equal to zero nothing happens.
     */
    public static void setMaximumConcurrentLocks(int number) {
	if (number > 0) {
	    maxConcurrentLocks = number;
	}
    }

    /** Sets the anticipated maximum number of locks that may be held or
     *  waited on concurrently.  Provide a limit for the size of the 
     *  monitor cache on systems that provide one if hard is true.
     *
     *  @param number The maximum number of locks that can be in simultaneous
     *                use without incurring an execution time increase. If
     *                <code>number</code> is less than or equal to zero
     *                nothing happens.
     *  @param hard If true, <code>number</code> sets the limit. If a lock is
     *              attempted which would cause the number of locks to exceed
     *              <code>number</code> then a <code>ResourceLimitError</code>
     *              is thrown.
     */
    public static void setMaximumConcurrentLocks(int number, boolean hard) {
	if (number > 0) {
	    maxConcurrentLocks = number;
	    maxHard = hard;
	}
    }

    /** Set a new real-time security manager.
     *
     *  @param manager A <code>RealtimeSecurity</code> object which will
     *                 become the new security manager.
     *  @throws java.lang.SecurityException Thrown if security manager
     *                                      has already been set.
     */
    public static void setSecurityManager(RealtimeSecurity manager) 
	throws SecurityException
    {
	if (securityManager == null) {
	    securityManager = manager;
	} else {
	    throw new SecurityException("SecurityManager already set.");
	}
    }
}
