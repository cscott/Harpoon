package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class PeriodicTimerTest 
{
	public static void run() 
	{
		PeriodicTimer pt = null;
		Object o = null;

		Tests.increment();
		try {
			AsyncEventHandler handler = null;
			pt = new PeriodicTimer(new RelativeTime(), new RelativeTime(), handler);
			if( !(pt instanceof PeriodicTimer && pt instanceof Timer) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PeriodicTimerTest");
		}
	}
}