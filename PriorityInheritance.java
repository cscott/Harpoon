package javax.realtime;

public class PriorityInheritance extends MonitorControl {
    /** Monitor control class specifying use of the priority inheritance
     *  protocol for object monitors. Objects under the influence of this
     *  protocol have the effect that a thread entering the monitor will
     *  boost the effective priority of the thtread in the monitor to its
     *  own effective priority. When that thread exits the monitor, its
     *  effective priority will be restored to its previous value.
     */

    public PriorityInheritance() {
	// TODO
    }

    public static PriorityInheritance instance() {
	// TODO

	return null;
    }
}
