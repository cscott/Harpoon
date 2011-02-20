package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class ScopedPhysicalMemoryTest 
{

	public static void run() 
	{
		ScopedPhysicalMemory spm = null;
		Object o = null;

// constructors are protected

		Tests.increment();
		try {
			long base = 500;
			long size = 1000;
			o = ScopedPhysicalMemory.create(new Object(), base, size);
			if( !(o instanceof ScopedPhysicalMemory && o instanceof ScopedMemory) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ScopedPhysicalMemoryTest");
		}
	}
}