package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class HeapMemoryTest 
{
	public static void run() 
	{
		HeapMemory hm = null;
		Object o = null;

		Tests.increment();
		try {
//			hm = HeapMemory.instance(); // causes stack trace
//			if( !(hm instanceof HeapMemory) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("HeapMemoryTest");
		}

	}
}