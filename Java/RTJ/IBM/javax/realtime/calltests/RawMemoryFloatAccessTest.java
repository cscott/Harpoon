package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class RawMemoryFloatAccessTest 
{
	public static void run() 
	{
		RawMemoryFloatAccess rmfa = null;
		Object o = null;

// constructors protected

		Tests.increment();
		try {
			long size = 1000;
//			o = RawMemoryFloatAccess.create(new Object(), size); // apparently not yet implemented
			if( !(o instanceof RawMemoryAccess) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RawMemoryFloatAccessTest");
		}

		Tests.increment();
		try {
			long base = 500;
			long size = 1000;
//			o = RawMemoryFloatAccess.create(new Object(), base, size); // apparently not yet implemented
			if( !(o instanceof RawMemoryAccess) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RawMemoryFloatAccessTest");
		}

// rest of the methods are not static, and cannot instantiate to test
	}
}