package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class MemoryParametersTest 
{

	public static void run() 
	{
		MemoryParameters mp = null;
		Object o = null;

		Tests.increment();
		try {
//			long x = MemoryParameters.NO_MAX;

			throw new Exception(); // apparently not yet implemented
		} catch (Exception e) {
			Tests.fail("MemoryParametersTest");
		}

		Tests.increment();
		try {
			long maxMemoryArea = 500;
			long maxImmortal = 300;
//			o = new MemoryParameters(maxMemoryArea, maxImmortal);
			if( !(o instanceof MemoryParameters) )
				throw new Exception(); // apparently not yet implemented
		} catch (Exception e) {
			Tests.fail("MemoryParametersTest");
		}

		Tests.increment();
		try {
			long maxMemoryArea = 500;
			long maxImmortal = 300;
			long allocationRate = 10;
//			o = new MemoryParameters(maxMemoryArea, maxImmortal, allocationRate);
			if( !(o instanceof MemoryParameters) )
				throw new Exception(); // apparently not yet implemented
		} catch (Exception e) {
			Tests.fail("MemoryParametersTest");
		}

// rest of methods need instantiation to test
	}
}