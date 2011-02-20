package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class ClockTest 
{
	public static void run() 
	{
		Clock clock = null;
		Object o = null;

		Tests.increment();
		try {
//			clock = Clock.getRealTimeClock();// causes stack trace
			if( !(clock instanceof Clock) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ClockTest");
		}

	}
}