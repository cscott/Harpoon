package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class WaitFreeDequeueTest 
{

	public static void run() 
	{
		WaitFreeDequeue wfd = null;
		Object o = null;

		Tests.increment();
		try {
			Thread writer = new Thread();
			Thread reader = new Thread();
			int maximum = 100;
			MemoryArea area = null;
//			wfd = new WaitFreeDequeue(writer, reader, maximum, area); // causes stack trace
//			if( !(wfd instanceof WaitFreeDequeue) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("WaitFreeDequeueTest");
		}
	}
}