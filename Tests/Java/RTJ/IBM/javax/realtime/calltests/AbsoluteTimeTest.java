package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */


import javax.realtime.*;
import java.util.*;

public class AbsoluteTimeTest 
{
	public static void run() 
	{
		AbsoluteTime at = null;
		Object o = null;

		Tests.increment();
		try {
			at = new AbsoluteTime();
			if( !(at instanceof AbsoluteTime && at instanceof HighResolutionTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			at = new AbsoluteTime(new AbsoluteTime());
			if( !(at instanceof AbsoluteTime && at instanceof HighResolutionTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			at = new AbsoluteTime(new java.util.Date());
			if( !(at instanceof AbsoluteTime && at instanceof HighResolutionTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			long millis = 10223;
			int nanos = 100;
			o = at.add(millis, nanos);
			if( !(o instanceof AbsoluteTime && o instanceof HighResolutionTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			long millis = 10223;
			int nanos = 100;
			o = at.add(millis, nanos, at);
			if( !(o instanceof AbsoluteTime && o instanceof HighResolutionTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			o = at.getDate();
			if( !(o instanceof Date) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}
// cause stack overflow

		Tests.increment();
		try {
//			o = at.subtract(at);
//			if( !(o instanceof AbsoluteTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
//			o = at.subtract(at, null);
//			if( !(o instanceof AbsoluteTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			RelativeTime time = new RelativeTime();
//			o = at.subtract(time);
//			if( !(o instanceof AbsoluteTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			RelativeTime time = new RelativeTime();
//			o = at.subtract(time, null);
//			if( !(o instanceof AbsoluteTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

// HighPrecisionTime tests

		Tests.increment();
		try {
			long millis = 10223;
			int nanos = 100;
			at = new AbsoluteTime(millis, nanos);
			if( !(at instanceof AbsoluteTime && at instanceof HighResolutionTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			Clock clock = null;
			AbsoluteTime dest = null;
			o = at.absolute(clock, dest);
			if( !(o instanceof AbsoluteTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			int x = at.compareTo(at);
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			int x = at.compareTo(new Object());
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			boolean x = at.equals(at);
			if( !x )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			boolean x = at.equals(new Object());
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			long x = at.getMilliseconds();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			long x = at.getNanoseconds();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			int x = at.hashCode();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			at.set(at);
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			at.set(100L);
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			at.set(100L, 100);
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}

		Tests.increment();
		try {
			example();
		} catch (Exception e) {
			Tests.fail("AbsoluteTimeTest");
		}
	}

	public static void example() throws Exception 
	{
		AbsoluteTime at;
		at = new AbsoluteTime(System.currentTimeMillis(), 0);
		at.toString(); // instead of System.out.print
		RelativeTime step = new RelativeTime(0, 500);	// 500 nanoseconds
		at.add(step).toString(); // instead of System.out.print
//		at.addNanoseconds(500).toString(); // instead of System.out.print
		// previous function not implemented (it's not in the spec!)
		AbsoluteTime dest = new AbsoluteTime(0,0);
		at.add(step,dest);
		dest.toString();
//		at.addNanoSeconds(500,at);
		// previous function not implemented (it's not in the spec!)
		at.toString();

		throw new Exception();
	}
}