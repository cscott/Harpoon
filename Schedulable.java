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
     *
     *  @return True, if the addition would result in the set of considered
     *          characteristics being feasible. False, if the addition would
     *          result in the sed of considered characteristics being infeasible
     *          or there is no assigned instance of <code>Scheduler</code>.
     */
    public boolean addIfFeasible();

    /** Inform the scheduler and cooperating facilities that scheduling and
     *  release characteristics of this instance of <code>Schedulable</code>
     *  should be considered in feasibility analysis until further notified.
     *
     *  @return True, if the addition was successful. False, if not.
     */
    public boolean addToFeasibility();

    /** Gets a reference to the <code>MemoryParameters</code> object.
     *
     *  @return A reference to the current <code>MemoryParameters</code> object.
     */
    public MemoryParameters getMemoryParameters();

    /** Gets a reference to the <code>ProcessingGroupParameters</code> object.
     *
     *  @return A reference to the current <code>ProcessingGroupParameters</code> object.
     */
    public ProcessingGroupParameters getProcessingGroupParameters();

    /** Gets a reference to the <code>ReleaseParameters</code> object.
     *
     *  @return A reference to the current <code>ReleaseParameters</code> object.
     */
    public ReleaseParameters getReleaseParameters();

    /** Gets a reference to the <code>SchedulingParameters</code> object.
     *
     *  @return A reference to the current <code>SchedulingParameters</code> object.
     */
    public SchedulingParameters getSchedulingParameters();

    /** Gets a reference to the <code>Scheduler</code> object.
     *
     *  @return A reference to the current <code>Scheduler</code> object.
     */
    public Scheduler getScheduler();

    /** Inform the scheduler and cooperating facilities that scheduling and
     *  release characteristics of this instance of <code>Schedulable</code> should
     *  <i>not</i> be considered in feasibility analysis until further notified.
     *
     *  @return True, if the removal was successful. False, if not.
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
     *
     *  @param release The proposed release parameters.
     *  @param memory The proposed memory parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
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
     *  
     *  @param release The proposed release parameters.
     *  @param memory The proposed memory parameters.
     *  @param group The proposed processing group parameters.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
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
     *
     *  @param release The proposed release parameters.
     *  @param group The proposed processing group parameters.
     *  @return True, if the resulting system is fesible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setIfFeasible(ReleaseParameters release, ProcessingGroupParameters group);

    /** Sets the memory parameters associated with this instance of <code>Schedulable</code>.
     *
     *  @param memory A <code>MemoryParameters</code> object which will become the
     *                memory parameters associated with <code>this</code> after
     *                the method call.
     */
    public void setMemoryParameters(MemoryParameters memory);

    /** The method first performs a feasibility analysis using the given memory
     *  parameters as replacements for the memory parameters of <code>this</code>.
     *  If the resulting system is feasible the method replaces the current
     *  memory parameterers of <code>this</code> with the new memory parameters.
     *
     *  @param memParam The proposed memory Parameters
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setMemoryParametersIfFeasible(MemoryParameters memParam);

    /** Sets the <code>ProcessingGroupParameters</code> of <code>this</code> only
     *  if the resulting set of scheduling and release characteristics is feasible.
     *
     *  @param pgp The <code>ProcessingGroupParameters</code> object. If null,
     *             nothing happens.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public void setProcessingGroupParameters(ProcessingGroupParameters pgp);

    /** Sets the <code>ProcessingGroupParameters</code> of <code>this</code> only
     *  if the resulting set of scheduling and release characteristics is feasible.
     *
     *  @param groupParameters The <code>ProcessingGroupParameters</code> object.
     *                         If null, nothing happens.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setProcessingGroupParametersIfFeasible(ProcessingGroupParameters groupParameters);

    /** Sets the release parameters associated with this instance of <code>Schedulable</code>.
     *  Since this affects the constraints expressed in the release parameters of the
     *  existing schedulable objects, this may change the feasibility of the current schedule.
     *
     *  @param release A <code>ReleaseParameters</code> object which will become the
     *                 release parameters associated with this after the method call.
     */
    public void setReleaseParameters(ReleaseParameters release);

    /** Set the <code>ReleaseParameters</code> for this schedulable object only if
     *  the resulting set of scheduling and release characteristics is feasible.
     *
     *  @param release The <code>ReleaseParameters</code> object. If null, nothing happens.
     *  @return True, if the resulting system is feasible and the changes are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setReleaseParametersIfFeasible(ReleaseParameters release);

    /** Sets the reference to the <code>SchedulingParameters</code> object.
     *
     *  @param scheduling A reference to the <code>SchedulingParameters</code> object.
     *  @throws java.lang.IllegalThreadStateException Thrown when:
     *                                                <code>((Thread.isAlive() &&
     *                                                Not Blocked) == true)</code>.
     *                                                (Where blocked means waiting in
     *                                                <code>Thread.wait(), Thread.join()
     *                                                </code> or <code>Thread.sleep()</code>).
     */
    public void setSchedulingParameters(SchedulingParameters scheduling);

    /** The method first performs a feasibility analysis using the given scheduling
     *  parameters as replacements for the scheduling parameters of <code>this</code>.
     *  If the resulting system is feasible the method replaces the current scheduling
     *  parameters of <code>this</code> with the new scheduling parameters.
     *
     *  @param scheduling The proposed scheduling parameters.
     *  @return True, if the resulting system is feasible and the chagens are made.
     *          False, if the resulting system is not feasible and no changes are made.
     */
    public boolean setSchedulingParametersIfFeasible(SchedulingParameters scheduling);

    /** Sets the reference to the <code>Scheduler</code> object. 
     *
     *  @param scheduler A reference to the <code>Scheduler</code> object.
     *  @throws java.lang.IllegalThreadStateException Thrown when:
     *                                                <code>((Thread.isAlive() &&
     *                                                Not Blocked) == true)</code>.
     *                                                (Where blocked means waiting
     *                                                in <code>Thread.wait(),
     *                                                Thread.join()</code> or
     *                                                <code>Thread.sleep()</code>).
     */
    public void setScheduler(Scheduler scheduler)
	throws IllegalThreadStateException;

    /** Sets the reference to the <code>Scheduler</code> object. 
     *
     *  @param scheduler A reference to the <code>Scheduler</code> object.
     *  @param scheduling A reference to the <code>SchedulingParameters</code> which
     *                    will be associated with <code>this</code>. If null, no
     *                    changes to current value of this parameter is made.
     *  @param release A reference to the <code>ReleaseParameters</code> which will
     *                 be associated with <code>this</code>. If null, no change to
     *                 current value of this parameter is made.
     *  @param memoryParameters A reference to the <code>MemoryParaemters</code>
     *                          which will be associated with <code>this</code>. If
     *                          null, no change to current value of this parameter
     *                          is made.
     *  @param processingGroup A reference to the <code>ProcessingGroupParameters</code>
     *                         which will be associated with <code>this</code>. If null,
     *                         no change to current value of this parameter is made.
     *  @throws java.lang.IllegalThreadStateException Thrown when:
     *                                                <code>((Thread.isAlive() &&
     *                                                Not Blocked) == true)</code>.
     *                                                (Where blocked means waiting
     *                                                in <code>Thread.wait(),
     *                                                Thread.join()</code> or
     *                                                <code>Thread.sleep()</code>).
     */
    public void setScheduler(Scheduler scheduler,
			     SchedulingParameters scheduling,
			     ReleaseParameters release,
			     MemoryParameters memoryParameters,
			     ProcessingGroupParameters processingGroup)
	throws IllegalThreadStateException;

    /** Return a UID for this Schedulable object. */
    public long getUID();

}
