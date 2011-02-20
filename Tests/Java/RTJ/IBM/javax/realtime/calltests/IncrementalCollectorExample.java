package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class IncrementalCollectorExample extends GarbageCollector 
{

	public IncrementalCollectorExample() 
	{
	}

	public static void run() 
	{
		IncrementalCollectorExample ic = null;
		Object o = null;

		Tests.increment();
		try {
			ic = new IncrementalCollectorExample();
			if( !(ic instanceof IncrementalCollectorExample && ic instanceof GarbageCollector) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("IncrementalCollectorExamplesTest");
		}

		Tests.increment();
		try {
			long x = ic.getMaximumReclamationRate();
		} catch (Exception e) {
			Tests.fail("IncrementalCollectorExamplesTest");
		}

		Tests.increment();
		try {
			o = ic.getPreemptionLatency();
			if( !(o instanceof RelativeTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("IncrementalCollectorExamplesTest");
		}

		Tests.increment();
		try {
			int x = ic.getReadBarrierOverhead();
		} catch (Exception e) {
			Tests.fail("IncrementalCollectorExamplesTest");
		}

		Tests.increment();
		try {
			int x = ic.getWriteBarrierOverhead();
		} catch (Exception e) {
			Tests.fail("IncrementalCollectorExamplesTest");
		}

		Tests.increment();
		try {
			int rate = 10;
			ic.setReclamationRate(rate);
		} catch (Exception e) {
			Tests.fail("IncrementalCollectorExamplesTest");
		}
	}
}