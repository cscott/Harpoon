// Clock.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

public abstract class Clock {
    /** A clock */    
    
    private RelativeTime resolution;
    private static RealtimeClock rtc = null;
    
    public Clock() {
	// TODO
    }
    
    public Clock(long millis, int nanos) {
	// TODO
	
	// Why would it be like this?
	//	resolution = new RelativeTime(1, 0);
    }
    
    public static Clock getRealtimeClock() {
	// Only allow one RealtimeClock, because all RealtimeClocks are
	// equivalent (i.e. they all advance in sync with the real world).
	if (rtc == null)
	    rtc = new RealtimeClock();
	return rtc;
    }
    
    public abstract RelativeTime getResolution();
    
    public AbsoluteTime getTime() {
	AbsoluteTime time = new AbsoluteTime();
	getTime(time);
	return time;
    }
    
    public abstract void getTime(AbsoluteTime time);
    public abstract void setResolution(RelativeTime res);
}
