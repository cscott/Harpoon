// Schedulable.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;
import java.io.*;

/** Handlers and other objects can be run by a <code>Scheduler</code> if they
 *  provide a <code>run()</code> method and the methods defined below. The
 *  <code>Scheduler</code> uses this information to create a suitable context
 *  to execute the <code>run()</code> method.
 */
public interface Schedulable extends java.lang.Runnable {

    /** Add the scheduling and release characteristics of <code>this</code>
     *  to the set of such chracteristics already being considered, if the
     *  addition would result in the new, larger set being feasible.
     */
    public boolean addIfFeasible();

    /** Inform the scheduler and cooperating facilities that the resource
     *  demands (as expressed in the associated instances of
     *  <code>SchedulingParameters, ReleaseParameters, MemoryParameters</code>
     *  and <code>ProcessingGroupParameters</code>) of this instance of
     *  <code>Schedulable</code> will be considered in the feasibility analysis
     *  of the associated <code>Scheduler</code> until further notice. Whether
     *  the resulting system is feasible or not, the addition is completed.
     */
    public boolean addToFeasibility();

    /** Return the <code>MemoryParameters</code> of this schedulable object. */
    public MemoryParameters getMemoryParameters();

    /** Return the <code>ProcessingGroupParameters</code> of this schedulable object. */
    public ProcessingGroupParameters getProcessingGroupParameters();

    /** Return the <code>ReleaseParameters</code> of this schedulable object. */
    public ReleaseParameters getReleaseParameters();

    /** Return the <code>SchedulingParameters</code> of this schedulable object. */
    public SchedulingParameters getSchedulingParameters();

    /** Return the <code>Scheduler</code> of this schedulable object. */
    public Scheduler getScheduler();

    /** Inform the scheduler and cooperating facilities that the resource
     *  demands (as expressed in the associated instances of
     *  <code>SchedulingParameters, ReleaseParameters, MemoryParameters</code>
     *  and <code>ProcessingGroupParameters</code>) of this instance of
     *  <code>Schedulable</code> should no longer be considered in the
     *  feasibility analysis of the associated <code>Scheduler</code>
     *  until further notice. Whether the resulting system is feasible
     *  or not, the subtraction is completed.
     */
    public void removeFromFeasibility();

    /** This method appears in many classes in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance of
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The
     *  method first performs a feasibility analysis using the new scheduling
     *  characteristics of either <code>this</code> or the given instance of
     *  <code>Schedulable</code>. If the resulting system is feasible the method
     *  replaces the current sheduling characteristics, of either <code>this</code>
     *  or the given instance of <code>Schedulable</code> as appropriate, with the
     *  new scheduling characteristics.
     */
    public boolean setIfFeasible(ReleaseParameters release, MemoryParameters memory);

    /** This method appears in many classes in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance of
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The
     *  method first performs a feasibility analysis using the new scheduling
     *  characteristics of either <code>this</code> or the given instance of
     *  <code>Schedulable</code>. If the resulting system is feasible the method
     *  replaces the current sheduling characteristics, of either <code>this</code>
     *  or the given instance of <code>Schedulable</code> as appropriate, with the
     *  new scheduling characteristics.
     */
    public boolean setIfFeasible(ReleaseParameters release, MemoryParameters memory,
				 ProcessingGroupParameters group);

    /** This method appears in many classes in the RTSJ and with various parameters.
     *  The parameters are either new scheduling characteristics for an instance of
     *  <code>Schedulable</code> or an instance of <code>Schedulable</code>. The
     *  method first performs a feasibility analysis using the new scheduling
     *  characteristics of either <code>this</code> or the given instance of
     *  <code>Schedulable</code>. If the resulting system is feasible the method
     *  replaces the current sheduling characteristics, of either <code>this</code>
     *  or the given instance of <code>Schedulable</code> as appropriate, with the
     *  new scheduling characteristics.
     */
    public boolean setIfFeasible(ReleaseParameters release, ProcessingGroupParameters group);

    /** Set the <code>MemoryParameters</code> of this schedulable object. */
    public void setMemoryParameters(MemoryParameters memory);

    /** Set the <code>MemoryParameters</code> of this schedulable object
     *  only if the resulting task set is feasible.
     */
    public boolean setMemoryParametersIfFeasible(MemoryParameters memParam);

    /** Set the <code>ProcessingGroupParameters</code> of this schedulable object. */
    public void setProcessingGroupParameters(ProcessingGroupParameters pgp);

    /** Set the <code>ProcessingGroupParameters</code> of this schedulable
     *  object only if the resulting task set is feasible.
     */
    public boolean setProcessingGroupParametersIfFeasible(ProcessingGroupParameters groupParameters);

    /** Set the <code>ReleaseParameters</code> of this schedulable object. */
    public void setReleaseParameters(ReleaseParameters release);

    /** Set the <code>ReleaseParameters</code> of this schedulable object
     *  only if the resulting task set is feasible.
     */
    public boolean setReleaseParametersIfFeasible(ReleaseParameters release);

    /** Set the <code>SchedulingParameters</code> of this schedulable object */
    public void setSchedulingParameters(SchedulingParameters scheduling);

    /** Set the <code>SchedulingParameters</code> of this schedulable object
     *  only if the resulting task set is feasible.
     */
    public boolean setSchedulingParametersIfFeasible(SchedulingParameters scheduling);

    /** Set the <code>Scheduler</code> for this schedulable object. */
    public void setScheduler(Scheduler scheduler)
	throws IllegalThreadStateException;

    /** Set the <code>Scheduler</code> for this schedulable object. */
    public void setScheduler(Scheduler scheduler,
			     SchedulingParameters scheduling,
			     ReleaseParameters release,
			     MemoryParameters memoryParameters,
			     ProcessingGroupParameters processingGroup)
	throws IllegalThreadStateException;
}
