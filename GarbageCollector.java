// GarbageCollector.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>GarbageCollector</code> provides an abstract class that provides
 *  a means to access and modify the temporal behavior of the garbage collector.
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public abstract class GarbageCollector {
    
    /** Instances of <code>RealtimeThread</code> are allowed to preempt the
     *  execution of the garbage collector (instances of 
     *  <code>NoHeapRealtimeThread</code> preempt immediately but instances
     *  of <code>RealtimeThread</code> must wait until the collector reaches
     *  a preemption-safe point).  Preemption latency is a measure of the
     *  maximum time a <code>RealtimeThread</code> may have to wait for
     *  the collector to reach a preemption-safe point.
     */
//      public abstract RelativeTime getPreemptionLatency();
}
