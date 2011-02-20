package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class PriorityParametersTest 
{

	public static void run() 
	{
		PriorityParameters pp = null;
		Object o = null;

		Tests.increment();
		try {
			int priority = 10;
			pp = new PriorityParameters(priority);
			if( !(pp instanceof PriorityParameters && pp instanceof SchedulingParameters) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PriorityParametersTest");
		}

		Tests.increment();
		try {
			int priority = pp.getPriority();
			if( priority != 10 )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PriorityParametersTest");
		}

		Tests.increment();
		try {
			int priority = 5;
			pp.setPriority(5);
			if( pp.getPriority() != 5 )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PriorityParametersTest");
		}

		Tests.increment();
		try {
			o = pp.toString();
			if( !(o instanceof String) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PriorityParametersTest");
		}
	}
}