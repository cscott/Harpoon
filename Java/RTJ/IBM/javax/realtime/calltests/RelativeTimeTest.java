package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */


import javax.realtime.*;
import java.util.*;

public class RelativeTimeTest 
{
	public static void run() 
	{
		RelativeTime rt = null;
		Object o = null;

		Tests.increment();
		try {
			rt = new RelativeTime();
			if( !(rt instanceof RelativeTime && rt instanceof HighResolutionTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RelativeTimeTest");
		}

		Tests.increment();
		try {
			rt = new RelativeTime(100L,100);
			if( !(rt instanceof RelativeTime && rt instanceof HighResolutionTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RelativeTimeTest");
		}

		Tests.increment();
		try {
			rt = new RelativeTime(new RelativeTime());
			if( !(rt instanceof RelativeTime && rt instanceof HighResolutionTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RelativeTimeTest");
		}

		Tests.increment();
		try {
			long millis = 10223;
			int nanos = 100;
			o = rt.add(millis, nanos);
			if( !(o instanceof RelativeTime && o instanceof HighResolutionTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RelativeTimeTest");
		}

		Tests.increment();
		try {
			long millis = 10223;
			int nanos = 100;
			o = rt.add(millis, nanos, rt);
			if( !(o instanceof RelativeTime && o instanceof HighResolutionTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RelativeTimeTest");
		}

		Tests.increment();
		try {
			o = rt.add(rt);
			if( !(o instanceof RelativeTime && o instanceof HighResolutionTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RelativeTimeTest");
		}

		Tests.increment();
		try {
			o = rt.add(rt, null);
			if( !(o instanceof RelativeTime && o instanceof HighResolutionTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RelativeTimeTest");
		}

		Tests.increment();
		try {
			AbsoluteTime dest = new AbsoluteTime(0,0);
			rt.addInterarrivalTo(dest);
		} catch (Exception e) {
			Tests.fail("RelativeTimeTest");
		}

		Tests.increment();
		try {
			o = rt.getInterarrivalTime(null);
			if( !(o instanceof RelativeTime && o instanceof HighResolutionTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RelativeTimeTest");
		}

		Tests.increment();
		try {
			o = rt.subtract(rt);
			if( !(o instanceof RelativeTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RelativeTimeTest");
		}

		Tests.increment();
		try {
			o = rt.subtract(rt, null);
			if( !(o instanceof RelativeTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RelativeTimeTest");
		}

		Tests.increment();
		try {
			o = rt.toString();
			if( !(o instanceof String) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RelativeTimeTest");
		}
	}
}