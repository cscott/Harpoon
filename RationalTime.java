package javax.realtime;

/** An object that represents a time interval millis/1E3+nanos/1E9
 *  seconds long that is divided into subintervals by some frequency.
 *  This is generally used in periodic events, threads, and
 *  feasibility analysis to specify periods where there is a basic
 *  period that must be adhered to strictly (the interval), but
 *  within that interval the periodic events are supposed to happen
 *  <code>frequency</code> times, as u niformly spaced as possible,
 *  but clock and scheduling jitter is moderately acceptable.
 */
public class RationalTime extends RelativeTime {

    private int frequency;

    /** Construct a new object of <code>RationalTime</code>. Equivalent to
     *  <code>new RationalTime(frequency, 1000, 0)</code> -- essentially
     *  a cycle-per-seconds value.
     */
    public RationalTime(int frequency) {
	// I think they messed up the order of the parameters in the spec.
	this(frequency, 1000, 0);
    }

    /** Constructs a new object of <code>RationalTime</code>. All arguments
     *  must be greater than or equal to 0.
     */
    public RationalTime(int frequency, long millis, int nanos)
	throws IllegalArgumentException {
	super(millis, nanos);
	this.frequency = frequency;
    }

    /** Constructs a new object of <code>RationalTime</code> from the given
     *  <code>RelativeTime</code>.
     */
    public RationalTime(int frequency, RelativeTime interval)
	throws IllegalArgumentException {
	this(frequency, interval.getMilliseconds(),
	     interval.getNanoseconds());
    }

    /** Convert this time to an absolute time. For a <code>RelativeTime</code>,
     *  this involved adding the clocks notion of now to this interval and
     *  constructing a new <code>AbsoluteTime</code> based on the sum.
     */
    public AbsoluteTime absolute(Clock clock, AbsoluteTime destination) {
	if (destination == null)
	    destination = new AbsoluteTime();
	if (clock == null)
	    clock = Clock.getRealtimeClock();
	
	destination.set(clock.getTime().add(this));
	return destination;
    }

    /** Add this time to an <code>AbsoluteTime</code>. It is almost the same as
     *  <code>destination.add(this, destination)</code> except that it accounts
     *  for (ie. divides by) the frequency.
     */
    public void addInterarrivalTo(AbsoluteTime destination) {
	destination.add(this, destination);
    }

    /** Return the frequency of this. */
    public int getFrequency() {
	return frequency;
    }

    /** Gets the time duration between two consecutive ticks using frequency. */
    public RelativeTime getInterarrivalTime() {
	RelativeTime temp = new RelativeTime();
	long t =  Math.round((1000000 * getMilliseconds() + getNanoseconds()) /
			     getFrequency());
	temp.set(t / 1000000, (int) (t % 1000000));
	return temp;
    }

    /** Gets the time duration between two consecutive ticks using frequency. */
    public RelativeTime getInterarrivalTime(RelativeTime dest) {
	if (dest == null) dest = new RelativeTime();
	long t =  Math.round((1000000 * getMilliseconds() + getNanoseconds()) /
			     getFrequency());
	dest.set(t / 1000000, (int) (t % 1000000));
	return dest;
    }

    /** Change the indicated interval of this to the sum of the values of the arguments. */
    public void set(long millis, int nanos)
	throws IllegalArgumentException {
	super.set(millis, nanos);
    }

    /** Set the frequency of this. */
    public void setFrequency(int frequency) throws ArithmeticException {
	this.frequency = frequency;
    }
}
