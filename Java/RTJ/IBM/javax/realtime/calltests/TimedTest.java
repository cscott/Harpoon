package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class TimedTest 
{

	public static void run() 
	{
		Timed t = null;
		Object o = null;

		Tests.increment();
		try {
			RelativeTime time = new RelativeTime(0,0);
			t = new Timed(time);
			if( !(t instanceof Timed && t instanceof AsynchronouslyInterruptedException) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("TimedTest");
		}

		Tests.increment();
		try {
			Interruptible logic = null;
//			boolean x = t.doInterruptible(logic); // should return boolean
			throw new Exception();
		} catch (Exception e) {
			Tests.fail("TimedTest");
		}

		Tests.increment();
		try {
			RelativeTime time = new RelativeTime(0,0);
			t.resetTime(time);
		} catch (Exception e) {
			Tests.fail("TimedTest");
		}
	}
}