package javax.realtime;

public class PriorityCeilingEmulation extends MonitorControl {
    /** Monitor control class specifying use of the priority ceiling
     *  emulation protocol for monitor objects. Objects under the
     *  influence of this protocol have the effect that a thread
     *  entering the monitor has its effective priority -- for
     *  priority-based dispatching -- raised to the ceiling on entry,
     *  and is restored to its previous effective priority when it
     *  exists the monitor.
     */

    protected int ceiling;

    public PriorityCeilingEmulation(int ceiling) {
	super();
	this.ceiling = ceiling;
    }

    public int getDeafaultCeiling() {
	return ceiling;
    }
}
