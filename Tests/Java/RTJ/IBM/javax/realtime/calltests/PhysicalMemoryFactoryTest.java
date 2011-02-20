package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class PhysicalMemoryFactoryTest 
{

	public static void run() 
	{
		PhysicalMemoryFactory pmf = null;
		Object o = null;

		Tests.increment();
		try {
			o = PhysicalMemoryFactory.ALIGNED;
			if( !(o instanceof String) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PhysicalMemoryFactoryTest");
		}

		Tests.increment();
		try {
			o = PhysicalMemoryFactory.BYTESWAP;
			if( !(o instanceof String) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PhysicalMemoryFactoryTest");
		}

		Tests.increment();
		try {
			o = PhysicalMemoryFactory.DMA;
			if( !(o instanceof String) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PhysicalMemoryFactoryTest");
		}

		Tests.increment();
		try {
			o = PhysicalMemoryFactory.SHARED;
			if( !(o instanceof String) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PhysicalMemoryFactoryTest");
		}
		
//		create()				// protected
//		getTypedMemoryBase()	// protected
	}
}