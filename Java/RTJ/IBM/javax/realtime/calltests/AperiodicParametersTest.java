package javax.realtime.calltests;


/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class AperiodicParametersTest 
{
	
	public static void run() 
	{
		AperiodicParameters ap = null;
		Object o = null;

		Tests.increment();
		try {
			RelativeTime cost = null;
			RelativeTime deadline = null;
			AsyncEventHandler overrunHandler = null;
			AsyncEventHandler missHandler = null;
			ap = new AperiodicParameters(cost, deadline, overrunHandler, missHandler);
			if( !(ap instanceof AperiodicParameters && ap instanceof ReleaseParameters) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AperiodicParametersTest");
		}

// ReleaseParameters tests

		Tests.increment();
		try {
			o = ap.getCost();
			if( !(o instanceof RelativeTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AperiodicParametersTest");
		}

		Tests.increment();
		try {
			o = ap.getCostOverrunHandler();
			if( !(o instanceof AsyncEventHandler) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AperiodicParametersTest");
		}

		Tests.increment();
		try {
			o = ap.getDeadlineMissHandler();
			if( !(o instanceof AsyncEventHandler) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AperiodicParametersTest");
		}

		Tests.increment();
		try {
			RelativeTime cost = null;
			ap.setCost(cost);
		} catch (Exception e) {
			Tests.fail("AperiodicParametersTest");
		}

		Tests.increment();
		try {
			AsyncEventHandler handler = null;
			ap.setCostOverrunHandler(handler);
		} catch (Exception e) {
			Tests.fail("AperiodicParametersTest");
		}

		Tests.increment();
		try {
			RelativeTime deadline = null;
			ap.setDeadline(deadline);
		} catch (Exception e) {
			Tests.fail("AperiodicParametersTest");
		}

		Tests.increment();
		try {
			AsyncEventHandler handler = null;
			ap.setDeadlineMissHandler(handler);
		} catch (Exception e) {
			Tests.fail("AperiodicParametersTest");
		}
	}
}
