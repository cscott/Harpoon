package javax.realtime;

/** Monitor control class specifying use of the priority inheritance
 *  protocol for object monitors. Objects under the influence of this
 *  protocol have the effect that a thread entering the monitor will
 *  boost the effective priority of the thtread in the monitor to its
 *  own effective priority. When that thread exits the monitor, its
 *  effective priority will be restored to its previous value.
 */
public class PriorityInheritance extends MonitorControl {

    private static PriorityInheritance defaultPriorityInheritance;

    public PriorityInheritance() {}

    /** Return a pointer to the singleton <code>PriorityInheritance</code>. */
    public static PriorityInheritance instance() {
	if (defaultPriorityInheritance == null)
	    defaultPriorityInheritance = new PriorityInheritance();

	return defaultPriorityInheritance;
    }
}
