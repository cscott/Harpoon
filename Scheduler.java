// Scheduler.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;
import java.util.HashSet;

/** An instance of <code>Scheduler</code> manages the execution of 
 *  schedulable objects and may implement a feasibility algorithm. The
 *  feasibility algorithm may determine if the known set of schedulable
 *  objects, given their particular execution ordering (or priority
 *  assignment), is a feasible schedule. Subclasses of <code>Scheduler</code>
 *  are used for alternative scheduling policies and should define an
 *  <code>instance()</code> class method to return the default
 *  instance of the subclass. The name of the subclass should be
 *  descriptive of the policy, allowing applications to deduce the
 *  policy available for the scheduler obtained via
 *  <code>getDefaultScheduler()</code>.
 */
public abstract class Scheduler {
    protected static Scheduler defaultScheduler = null;
    
    
    protected Scheduler() {}

    /** Inform the scheduler and cooperating facilities that the resource
     *  demands (as expressed in the associated instances of
     *  <code>SchedulingParameters, ReleaseParameters, MemoryParameters</code>
     *  and <code>ProcessingGroupParameters</code>) of this instance of
     *  <code>Schedulable</code> will be considered in the feasibility analysis
     *  of the associated <code>Scheduler</code> until further notice. Whether
     *  the resulting system is feasible or not, the addition is completed.
     */
    protected abstract boolean addToFeasibility(Schedulable schedulable);

    /** Trigger the execution of a schedulable object (like an
     *  <code>AsyncEventHandler</code>.
     */
    public abstract void fireSchedulable(Schedulable schedulable);

    /** Return a reference to the default scheduler. */
    public static Scheduler getDefaultScheduler() {
	if (defaultScheduler == null) {
	    setDefaultScheduler(PriorityScheduler.getScheduler());
	    return getDefaultScheduler();
	}
	return defaultScheduler;
    }

    /** Used to determine the policy of the <code>Scheduler</code>. */
    public abstract String getPolicyName();

    /** Returns true if and only if the system is able to satisfy the
     *  constraints expressed in the release parameters of the existing
     *  schedulable objects.
     */
    public abstract boolean isFeasible();
    protected abstract boolean isFeasible(Schedulable s, ReleaseParameters rp);

    /** Inform the scheduler and cooperating facilities that the resource
     *  demands (as expressed in the associated instances of
     *  <code>SchedulingParameters, ReleaseParameters, MemoryParameters</code>
     *  and <code>ProcessingGroupParameters</code>) of this instance of
     *  <code>Schedulable</code> should no longer be considered in the
     *  feasibility analysis of the associated <code>Scheduler</code>
     *  until further notice. Whether the resulting system is feasible
     *  or not, the subtraction is completed.
     */
    protected abstract boolean removeFromFeasibility(Schedulable schedulable);

    /** Set the default scheduler. This is the scheduler given to instances
     *  of <code>RealtimeThread</code> when they are constructed. The default
     *  scheduler is set to the required <code>PriorityScheduler</code> at
     *  startup.
     */
    public static void setDefaultScheduler(Scheduler scheduler) {
	defaultScheduler = scheduler;
    }

    /** Returns true if, after considering the values of the parameters, the task set
     *  would still be feasible. In this case the values of the parameters are changed.
     *  Returns false if, after considering the values of the parameters, the task set
     *  would not be feasible. In this case the values of the parameters are not changed.
     */
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory) {
	return false;
    }

    /** Returns true if, after considering the values of the parameters, the task set
     *  would still be feasible. In this case the values of the parameters are changed.
     *  Returns false if, after considering the values of the parameters, the task set
     *  would not be feasible. In this case the values of the parameters are not changed.
     */
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory,
				 ProcessingGroupParameters group) {
	return false;
    }

}
