package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class WaitFreeWriteQueueTest 
{

	public static void run() 
	{
		WaitFreeWriteQueue wfwq = null;
		Object o = null;

		Tests.increment();
		try {
			Thread writer = new Thread();
			Thread reader = new Thread();
			int maximum = 100;
			MemoryArea memory = null;
			wfwq = new WaitFreeWriteQueue(writer, reader, maximum, memory);
			if( !(wfwq instanceof WaitFreeWriteQueue) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("WaitFreeWriteQueueTest");
		}
	}
}