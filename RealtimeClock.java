// RealtimeClock.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

public class RealtimeClock extends Clock {

	private RelativeTime resolution;

	public RealtimeClock() {
		this(1, 0);
	}

	// Should resolution be fixed for the rtc, i.e. 1 millisecond? Not sure.
	public RealtimeClock(long millis, int nanos) {
		super();
		resolution = new RelativeTime(millis, nanos);
	}

	public RelativeTime getResolution() {
		return new RelativeTime(resolution);
	}
	
  public void setResolution(RelativeTime res) {
		resolution.set(res);
	}

	protected static native long getTimeInC();

	public void getTime(AbsoluteTime time) {
		long micros  = getTimeInC();
		time.set(micros / 1000, (int)((micros % 1000) * 1000));
	}
}
