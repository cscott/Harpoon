// GarbageCollector.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

/** The system shall provide dynamic ans static information characterizing
 *  the temporal behavior and imposed overhead of any garbage collection
 *  algorithm provided by the system. This information shall be made
 *  available to applications via methods on subclasses of
 *  <code>GarbageCollector</code>. Implementations are allowed to provide
 *  any set of methods in subclasses as long as the temporal behavior and
 *  overhead are sufficiently categorized. The implementations are also
 *  required to fully document the subclasses. In addition, the method(s)
 *  in <code>GarbageCollector</code> shall be made available by all implementation.
 */
public abstract class GarbageCollector {
    
    public GarbageCollector() {}

    /** Preemption latency is a measure of the maximum time a
     *  <code>RealtimeThread</code> may have to wait for the collector to
     *  reach a preemption-safe point. Instances of <code>RealtimeThread</code>
     *  are allowed to preempt the garbage collector (instances of
     *  <code>NoHeapRealtimeThread</code> preempt immediately but instances
     *  of <code>RealtimeThread</code> must wait until the collector reaches
     *  a preemption-safe point.
     */
    public abstract RelativeTime getPreemptionLatency();
}
