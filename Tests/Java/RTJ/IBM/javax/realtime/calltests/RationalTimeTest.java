package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */


import javax.realtime.*;
import java.util.*;

public class RationalTimeTest 
{
	public static void run() 
	{
		RationalTime rt = null;
		Object o = null;

		Tests.increment();
		try {
			int freq = 10;
			rt = new RationalTime(freq);
			if( !(rt instanceof RationalTime && rt instanceof RelativeTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RationalTimeTest");
		}

		Tests.increment();
		try {
			long millis = 1;
			int nanos = 1;
			int freq = 10;
			rt = new RationalTime(freq, millis, nanos);
			if( !(rt instanceof RationalTime && rt instanceof RelativeTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RationalTimeTest");
		}

		Tests.increment();
		try {
			int freq = 10;
			RationalTime interval = new RationalTime(freq);
			rt = new RationalTime(freq, interval);
			if( !(rt instanceof RationalTime && rt instanceof RelativeTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RationalTimeTest");
		}

		Tests.increment();
		try {
//			Clock clock = Clock.getRealTimeClock();
//			o = rt.absolute(clock, null);
//			if( !(o instanceof AbsoluteTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RationalTimeTest");
		}

		Tests.increment();
		try {
			AbsoluteTime dest = new AbsoluteTime();
			rt.addInterarrivalTo(dest);
		} catch (Exception e) {
			Tests.fail("RationalTimeTest");
		}

		Tests.increment();
		try {
			int x = rt.getFrequency();
		} catch (Exception e) {
			Tests.fail("RationalTimeTest");
		}

		Tests.increment();
		try {
			long millis = 100;
			int nanos = 100;
			rt.set(millis, nanos);
		} catch (Exception e) {
			Tests.fail("RationalTimeTest");
		}

		Tests.increment();
		try {
			rt.setFrequency(10);
		} catch (Exception e) {
			Tests.fail("RationalTimeTest");
		}
	}
}