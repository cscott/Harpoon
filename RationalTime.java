package javax.realtime;

public class RationalTime extends RelativeTime {
    /** An object that represents a time interval millis/1E3+nanos/!E9
     *  seconds long that is divided into subintervals by some frequency.
     *  This is generally used in periodic events, threads, and
     *  feasibility analysis to specify periods where there is a basic
     *  period that must be adhered to strictly (the interval), but
     *  within that interval the periodic events are supposed to happen
     *  <code>frequency</code> times, as u niformly spaced as possible,
     *  but clock and scheduling jitter is moderately acceptable.
     */

    private int frequency;

    public RationalTime(int frequency) {
	// I think they messed up the order of the parameters in the spec.
	this(frequency, 1000, 0);
    }

    public RationalTime(int frequency, long millis, int nanos)
	throws IllegalArgumentException {
	super(millis, nanos);
	this.frequency = frequency;
    }

    public RationalTime(int frequency, RelativeTime interval)
	throws IllegalArgumentException {
	this(frequency, interval.getMilliseconds(),
	     interval.getNanoseconds());
    }

    public AbsoluteTime absolute(Clock clock, AbsoluteTime destination) {
	// TODO

	return destination;
    }

    public void addInterarrivalTo(AbsoluteTime destination) {
	// TODO
    }

    public int getFrequency() {
	return frequency;
    }

    public RelativeTime getInterarrivalTime() {
	// TODO

	return null;
    }

    public RelativeTime getInterarrivalTime(RelativeTime dest) {
	// TODO

	return dest;
    }

    public void set(long millis, int nanos)
	throws IllegalArgumentException {
	// TODO
    }

    public void setFrequency(int frequency) throws ArithmeticException {
	this.frequency = frequency;
    }
}
