package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class RawMemoryAccessTest 
{
	public static void run() 
	{
		RawMemoryAccess rma = null;
		Object o = null;

// constructors protected

		Tests.increment();
		try {
			long size = 1000;
//			o = RawMemoryAccess.create(new Object(), size); // apparently not yet implemented
			if( !(o instanceof RawMemoryAccess) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RawMemoryAccessTest");
		}

		Tests.increment();
		try {
			long base = 500;
			long size = 1000;
//			o = RawMemoryAccess.create(new Object(), base, size); // apparently not yet implemented
			if( !(o instanceof RawMemoryAccess) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RawMemoryAccessTest");
		}

// rest of the methods are not static, and cannot instantiate to test
	}
}