package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class OneShotTimerTest 
{
/*	
	public static void run() {
		OneShotTimer ost = null;
		Object o = null;

		Tests.increment();
		try {
			AsyncEventHandler handler = null;
			ost = new OneShotTimer(new RelativeTime(), handler);
			if( !(ost instanceof OneShotTimer && ost instanceof Timer) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("OneShotTimerTest");
		}
		
		Tests.increment();
		try {
//			example();
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("OneShotTimerTest");
		}
	}

	public static void example() {
		SchedulingParameters highPriority = new PriorityParameters(PriorityScheduler.instance().getMaxPriority());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {}
		(System.currentTimeMillis()).toString();
	}

	private static void TestTimer(String title, Timer t) {
		System.out.println("\n" + title + " test:\n");
		final long T0 = t.getFireTime().getMilliseconds();
		ReleaseParameters rp = t.createReleaseParameters();
		rp.setCost(new RelativeTime(10,0));
		rp.toString();
		t.addHandler(new AsyncEventHandler(highPriority, rp, null) {
			public void handleAsyncEvent() {
				(System.currentTimeMillis() - T0).toString();
			}
		});
		t.start();
	}
*/	
}