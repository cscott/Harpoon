// SchedulingParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

/** Subclasses of <code>SchedulingParameters(PriorityParameters,
 *  ImportanceParameters</code> and any others defined for particular
 *  schedulers) provide the parameters to be used by the
 *  <code>Scheduler</code>. Changes to the values in a parameters object
 *  affects the scheduling behavior of all the <code>Schedulable</code>
 *  objects to which it is bound.
 */
public abstract class SchedulingParameters {

    public SchedulingParameters() {}

}
