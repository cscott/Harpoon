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

    public static final byte BIG_ENDIAN = 0;
    public static final byte LITTLE_ENDIAN = 1;    
    public static final byte BYTE_ORDER = 2;

    private static GarbageCollector garbageCollector = null;
    private static RealtimeSecurity securityManager = null;
    private int maxConcurrentLocks;
    private int concurrentLocksUsed;
    private boolean maxHard;

    public RealtimeSystem() {
	maxConcurrentLocks = 0;
	concurrentLocksUsed = 0;
	maxHard = false;
    }

    /** Native call to retrieve the current <code>GarbageCollector</code>.
     */

    native static GarbageCollector getCurrentGC();

    /** Return a reference to the currently active garbage collector
     *  for the heap.
     */
    public static GarbageCollector currentGC() {
	if (garbageCollector == null) {
	    garbageCollector = getCurrentGC();
	}
	return garbageCollector;
    }

    /** Get the maximum number of locks that have been used concurrently.
     *  This value can be used for tuning the concurrent locks parameter,
     *  which is used as a hint by systems that use a monitor cache.
     */
    public int getConcurrentLocksUsed() {
	return concurrentLocksUsed;
    }

    /** Get the maximum number of locks that can be used concurrently
     *  without incurring an execution time increase as set by the
     *  setMaximumConcurrentLocks() methods.
     */

    public int getMaximumConcurrentLocks() {
	return maxConcurrentLocks;
    }

    /** Get a reference to the security manager used to control access
     *  to real-time system features such as access to physical memory.
     */

    public static RealtimeSecurity getSecurityManager() {
	return securityManager;
    }

    /** Set the anticipated maximum number of locks that may be held
     *  or waited on concurrently.  Provide a hint to systems that use
     *  a monitor cache as to how much space to dedicate to the cache.
     */

    public void setMaximumConcurrentLocks(int number) {
	if (number > 0) {
	    maxConcurrentLocks = number;
	}
    }

    /** Set the anticipated maximum number of locks that may be held or
     *  waited on concurrently.  Provide a limit for the size of the 
     *  monitor cache on systems that provide one if hard is true.
     */

    public void setMaximumConcurrentLocks(int number, boolean hard) {
	if (number > 0) {
	    maxConcurrentLocks = number;
	    maxHard = hard;
	}
    }

    /** Set a new real-time security manager.
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
