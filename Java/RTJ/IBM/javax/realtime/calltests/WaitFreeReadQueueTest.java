package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class WaitFreeReadQueueTest 
{

	public static void run() 
	{
		WaitFreeReadQueue wfrq = null;
		Object o = null;

		Tests.increment();
		try {
			Thread writer = new Thread();
			Thread reader = new Thread();
			int maximum = 100;
			MemoryArea memory = null;
//			wfrq = new WaitFreeReadQueue(writer, reader, maximum, memory);
//			if( !(wfrq instanceof WaitFreeReadQueue) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("WaitFreeReadQueueTest");
		}
	}
}