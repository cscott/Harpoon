package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class ProcessingGroupParametersTest 
{

	public static void run() 
	{
		ProcessingGroupParameters pgp = null;
		Object o = null;

		Tests.increment();
		try {
			HighResolutionTime start = null;
			RelativeTime period = null;
			RelativeTime cost = null;
			RelativeTime deadline = null;
			AsyncEventHandler overrunHandler = null;
			AsyncEventHandler missHandler = null;
			pgp = new ProcessingGroupParameters(start, period, cost, deadline, overrunHandler, missHandler);
			if( !(pgp instanceof ProcessingGroupParameters) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ProcessingGroupParametersTest");
		}

		Tests.increment();
		try {
			o = pgp.getCost();
			if( !(o instanceof RelativeTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ProcessingGroupParametersTest");
		}

		Tests.increment();
		try {
			o = pgp.getCostOverrunHandler();
			if( !(o instanceof AsyncEventHandler) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ProcessingGroupParametersTest");
		}

		Tests.increment();
		try {
			o = pgp.getDeadline();
			if( !(o instanceof RelativeTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ProcessingGroupParametersTest");
		}

		Tests.increment();
		try {
			o = pgp.getDeadlineMissHandler();
			if( !(o instanceof AsyncEventHandler) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ProcessingGroupParametersTest");
		}

		Tests.increment();
		try {
			o = pgp.getPeriod();
			if( !(o instanceof RelativeTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ProcessingGroupParametersTest");
		}

		Tests.increment();
		try {
			o = pgp.getStart();
			if( !(o instanceof HighResolutionTime) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ProcessingGroupParametersTest");
		}

		Tests.increment();
		try {
			RelativeTime rt = null;
			pgp.setCost(rt);
		} catch (Exception e) {
			Tests.fail("ProcessingGroupParametersTest");
		}

		Tests.increment();
		try {
			AsyncEventHandler aeh = null;
			pgp.setCostOverrunHandler(aeh);
		} catch (Exception e) {
			Tests.fail("ProcessingGroupParametersTest");
		}

		Tests.increment();
		try {
			RelativeTime deadline = null;
			pgp.setDeadline(deadline);
		} catch (Exception e) {
			Tests.fail("ProcessingGroupParametersTest");
		}

		Tests.increment();
		try {
			AsyncEventHandler aeh = null;
			pgp.setDeadlineMissHandler(aeh);
		} catch (Exception e) {
			Tests.fail("ProcessingGroupParametersTest");
		}

		Tests.increment();
		try {
			RelativeTime period = null;
			pgp.setPeriod(period);
		} catch (Exception e) {
			Tests.fail("ProcessingGroupParametersTest");
		}

		Tests.increment();
		try {
			HighResolutionTime start = null;
			pgp.setStart(start);
		} catch (Exception e) {
			Tests.fail("ProcessingGroupParametersTest");
		}
	}
}