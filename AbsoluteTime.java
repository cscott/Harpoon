// RelativeTime.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
//    taking over Bryan Fink's code
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

import java.util.Date;

public class AbsoluteTime extends HighResolutionTime
{
	public static AbsoluteTime endOfDays = new AbsoluteTime(Long.MAX_VALUE, 0);

	public AbsoluteTime()	{
		this(0, 0);
	}

	public AbsoluteTime(AbsoluteTime t)	{
		this(t.getMilliseconds(), t.getNanoseconds());
	}

	public AbsoluteTime(Date date) {
		this(date.getTime(), 0);
	}

	// Notice: we normalize the time, converting every 10^6 ns to 1 ms.
	public AbsoluteTime(long millis, int nanos)	{
		super();
		set(millis, nanos);
	}

	// Hmmm? No idea what the Clock is used for
	public AbsoluteTime absolute(Clock clock, AbsoluteTime destination)	{
		if (destination != null)
			destination.set(this);
		return this;
	}

	public AbsoluteTime add(long millis, int nanos) {
		return new AbsoluteTime(getMilliseconds() + millis,
														getNanoseconds() + nanos);
	}

	public AbsoluteTime add(long millis, int nanos, AbsoluteTime destination) {
		if (destination == null)
			destination = new AbsoluteTime();

		destination.set(getMilliseconds() + millis,
										getNanoseconds() + nanos);
		return destination;
	}

	public final AbsoluteTime add(RelativeTime time) {
		return new AbsoluteTime(getMilliseconds() + time.getMilliseconds(),
														getNanoseconds() + time.getNanoseconds());
	}

	public AbsoluteTime add(RelativeTime time, AbsoluteTime destination) {
		if (destination == null)
			destination = new AbsoluteTime();
		
		destination.set(getMilliseconds() + time.getMilliseconds(),
										getNanoseconds() + time.getNanoseconds());
		return destination;
	}

	public Date getDate()	{
		return new Date(getMilliseconds());
	}

	public void set(Date d)	{
		set(d.getTime());
	}

	public final RelativeTime subtract(AbsoluteTime time) {
		return new RelativeTime(getMilliseconds() - time.getMilliseconds(),
														getNanoseconds() - time.getNanoseconds());
	}

	public RelativeTime subtract(AbsoluteTime time, RelativeTime destination) {
		if (destination == null)
			destination = new RelativeTime();

		destination.set(getMilliseconds() - time.getMilliseconds(),
										getNanoseconds() - time.getNanoseconds());
		return destination;
	}

	public final AbsoluteTime subtract(RelativeTime time) {
		return new AbsoluteTime(getMilliseconds() - time.getMilliseconds(),
														getNanoseconds() - time.getNanoseconds());
	}

	public AbsoluteTime subtract(RelativeTime time, AbsoluteTime destination) {
		if (destination == null)
			destination = new AbsoluteTime();

		destination.set(getMilliseconds() - time.getMilliseconds(),
										getNanoseconds() - time.getNanoseconds());
		return destination;
	}

	public String toString() {
		Date result = new Date();
		result.setTime(getMilliseconds());
	
		return "AbsoluteTime: "+result.toString()+" millis: "+
	    getMilliseconds()%1000+" nanos: "+
	    getNanoseconds();
	}
}
