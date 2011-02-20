package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class LTMemoryTest 
{

	public static void run() 
	{
		LTMemory ltm = null;
		Object o = null;

		Tests.increment();
		try {
			int initial = 1000;
			int maximum = 2000;
			ltm = new LTMemory(initial, maximum);
			if( !(ltm instanceof LTMemory && ltm instanceof ScopedMemory) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("LTMemoryTest");
		}
	}
	
}