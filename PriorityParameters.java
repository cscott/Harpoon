// PriorityParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

/** Instances of this class should be assigned to threads that are
 *  managed by schedulers which use a single integer to determine
 *  execution order. The base scheduler required by this specification
 *  and represented by the class <code>PriorityScheduler</code> is
 *  such a scheduler.
 */
public class PriorityParameters extends SchedulingParameters {
    
    private int priority;

    /** Create an instance of <code>SchedulingParameters</code> with
     *  the given priority.
     *
     *  @param priority The priority assigned to a thread. This value
     *                   is used in place of the value returned by
     *                   <code>java.langThread.setPriority(int)</code>.
     */
    public PriorityParameters(int priority) {
	this.priority = priority;
    }

    /** Gets the priority value.
     *
     *  @return The priority.
     */
    public int getPriority() {
	return priority;
    }

    /** Sets the priority value.
     *
     *  @param priority The value to which priority is set.
     *  @throws java.lang.IllegalArgumentException Thrown if the given
     *                                             priority value is less
     *                                             than the minimum priority
     *                                             of the scheduler of any of
     *                                             the associated threads of
     *                                             greater then the maximum
     *                                             priority of the scheduler
     *                                             of any of the associated threads.
     */
    public void setPriority(int priority)
	throws IllegalArgumentException {
	this.priority = priority;
    }
    
    /** Converts the priority value to a string.
     *
     *  @return The string representing the value of priority.
     */
    public String toString() {
	return String.valueOf(priority);
    }
}
