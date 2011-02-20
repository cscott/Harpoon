// PriorityCeilingEmulation.java, created by Dumitru Daniliuc
// Copyright (C) 2003 Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** Monitor control class specifying use of the priority ceiling
 *  emulation protocol for monitor objects. Objects under the
 *  influence of this protocol have the effect that a thread
 *  entering the monitor has its effective priority -- for
 *  priority-based dispatching -- raised to the ceiling on entry,
 *  and is restored to its previous effective priority when it
 *  exists the monitor.
 */
public class PriorityCeilingEmulation extends MonitorControl {

    protected int ceiling;

    /** Create a <code>PriorityCeilingEmulation</code> object
     *  with a given ceiling.
     *
     *  @param ceiling Priority ceiling value.
     */
    public PriorityCeilingEmulation(int ceiling) {
	super();
	this.ceiling = ceiling;
    }

    /** Get the priority ceiling for this 
     *  <code>PriorityCeilingEmulation</code> object.
     *
     *  @return The priority ceiling.
     */
    public int getDeafaultCeiling() {
	return ceiling;
    }
}
