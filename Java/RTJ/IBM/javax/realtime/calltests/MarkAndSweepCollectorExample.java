package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class MarkAndSweepCollectorExample extends GarbageCollector 
{

	public MarkAndSweepCollectorExample() 
	{
	}

	public static void run() 
	{
		MarkAndSweepCollectorExample mc = null;
		Object o = null;

		Tests.increment();
		try {
			mc = new MarkAndSweepCollectorExample();
			if( !(mc instanceof MarkAndSweepCollectorExample && mc instanceof GarbageCollector) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("MarkAndSweepCollectorExampleTest");
		}

		Tests.increment();
		try {
			o = mc.getPreemptionLatency();
			if( !(o instanceof RelativeTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("MarkAndSweepCollectorExampleTest");
		}
	}
}