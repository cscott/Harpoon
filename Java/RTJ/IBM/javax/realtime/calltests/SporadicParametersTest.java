package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class SporadicParametersTest 
{

	public static void run() 
	{
		SporadicParameters sp = null;
		Object o = null;

		Tests.increment();
		try {
			RelativeTime minInterarrival = null;
			RelativeTime cost = null;
			RelativeTime deadline = null;
			AsyncEventHandler overrunHandler = null;
			AsyncEventHandler missHandler = null;
			sp = new SporadicParameters(minInterarrival, cost, deadline, overrunHandler, missHandler);
			if( !(sp instanceof SporadicParameters && sp instanceof AperiodicParameters) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("SporadicParametersTest");
		}

		Tests.increment();
		try {
			o = sp.getMinimumInterarrival();
			if( !(o instanceof RelativeTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("SporadicParametersTest");
		}

		Tests.increment();
		try {
			RelativeTime rt = null;
			sp.setMinimumInterarrival(rt);
		} catch (Exception e) {
			Tests.fail("SporadicParametersTest");
		}
	}
}