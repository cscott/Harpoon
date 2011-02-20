package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class NoHeapRealtimeThreadTest 
{

	public static void run() 
	{
		NoHeapRealtimeThread nhrtt = null;
		Object o = null;

		Tests.increment();
		try {
			SchedulingParameters scheduling = null;
			ReleaseParameters release = null;
			MemoryParameters memory = null;
			MemoryArea area = null;
			ProcessingGroupParameters group = null;
			java.lang.Runnable logic = null;
			nhrtt = new NoHeapRealtimeThread(scheduling, release, memory, area, group, logic);
			if( !(nhrtt instanceof NoHeapRealtimeThread &&
				nhrtt instanceof RealtimeThread &&
				nhrtt instanceof Thread) )
				Tests.fail("NoHeapRealtimeThreadTest");
		} catch (IllegalArgumentException e) {
			/* Continue, this test passed since we attempted to create a NoHeapRealtimeThread
			   with null memory */
			   System.out.println("Passed the null memory area check.");
		}

		Tests.increment();
		try 
		{
			nhrtt = new NoHeapRealtimeThread(new PriorityParameters(11), 
											 null, 
											 null, 
											 ImmortalMemory.instance(), 
											 null, 
											 null);
			if( !(nhrtt instanceof NoHeapRealtimeThread &&
				nhrtt instanceof RealtimeThread &&
				nhrtt instanceof Thread) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("NoHeapRealtimeThreadTest");
		}


	}
}