package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class PeriodicParametersTest 
{

	public static void run() 
	{
		PeriodicParameters pp = null;
		Object o = null;

		Tests.increment();
		try {
			HighResolutionTime start = null;
			RelativeTime period = null;
			RelativeTime cost = null;
			RelativeTime deadline = null;
			AsyncEventHandler overrunHandler = null;
			AsyncEventHandler missHandler = null;
			pp = new PeriodicParameters(start, period, cost, deadline, overrunHandler, missHandler);
			if( !(pp instanceof PeriodicParameters && pp instanceof ReleaseParameters) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PeriodicParametersTest");
		}

		Tests.increment();
		try {
			o = pp.getPeriod();
			if( !(o instanceof RelativeTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PeriodicParametersTest");
		}

		Tests.increment();
		try {
			o = pp.getStart();
			if( !(o instanceof HighResolutionTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PeriodicParametersTest");
		}

		Tests.increment();
		try {
			RelativeTime period = null;
			pp.setPeriod(period);
		} catch (Exception e) {
			Tests.fail("PeriodicParametersTest");
		}

		Tests.increment();
		try {
			HighResolutionTime start = null;
			pp.setStart(start);
		} catch (Exception e) {
			Tests.fail("PeriodicParametersTest");
		}
	}
}