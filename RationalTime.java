package javax.realtime;

public class RationalTime extends RelativeTime {
    /** An object that represents a time interval millis/1E3+nanos/1E9
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
	if (destination == null)
	    destination = new AbsoluteTime();
	if (clock == null)
	    clock = Clock.getRealtimeClock();
	
	destination.set(clock.getTime().add(this));
	return destination;
    }

    public void addInterarrivalTo(AbsoluteTime destination) {
	destination.add(this, destination);
    }

    public int getFrequency() {
	return frequency;
    }

    public RelativeTime getInterarrivalTime() {
	RelativeTime temp = new RelativeTime();
	long t =  Math.round((1000000 * getMilliseconds() + getNanoseconds()) /
			     getFrequency());
	temp.set(t / 1000000, (int) (t % 1000000));
	return temp;
    }

    public RelativeTime getInterarrivalTime(RelativeTime dest) {
	if (dest == null) dest = new RelativeTime();
	long t =  Math.round((1000000 * getMilliseconds() + getNanoseconds()) /
			     getFrequency());
	dest.set(t / 1000000, (int) (t % 1000000));
	return dest;
    }

    public void set(long millis, int nanos)
	throws IllegalArgumentException {
	super.set(millis, nanos);
    }

    public void setFrequency(int frequency) throws ArithmeticException {
	this.frequency = frequency;
    }
}
