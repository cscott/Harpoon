package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class PriorityInheritanceTest 
{

	public static void run() 
	{
		PriorityInheritance pi = null;
		Object o = null;

		Tests.increment();
		try {
			pi = new PriorityInheritance();
			if( !(pi instanceof PriorityInheritance && pi instanceof MonitorControl) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PriorityInheritanceTest");
		}

		Tests.increment();
		try {
//			o = PriorityInheritance.instance(); // does not seem to be implemented
//			if( !(o instanceof PriorityInheritance && o instanceof MonitorControl) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PriorityInheritanceTest");
		}
	}
}