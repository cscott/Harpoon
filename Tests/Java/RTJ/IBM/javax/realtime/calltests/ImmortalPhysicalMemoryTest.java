package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class ImmortalPhysicalMemoryTest 
{
	public static void run() 
	{
		ImportanceParameters ip = null;
		Object o = null;

// constructors are protected

		Tests.increment();
		try {
//			o = ImmortalPhysicalMemory.create(new Object(), 1000L);
//			if( !(o instanceof ImmortalPhysicalMemory) )
				throw new Exception(); // create() should be static!!
		} catch (Exception e) {
			Tests.fail("ImmortalPhysicalMemoryTest");
		}

		Tests.increment();
		try {
//			o = ImmortalPhysicalMemory.create(new Object(), 500L, 1000L);
//			if( !(o instanceof ImmortalPhysicalMemory) )
				throw new Exception(); // create() should be static!!
		} catch (Exception e) {
			Tests.fail("ImmortalPhysicalMemoryTest");
		}

		Tests.increment();
		try {
//			PhysicalMemoryFactory factory = null;
//			ImmortalPhysicalMemory.setFactory(factory);
			
			throw new Exception(); // setFactory seems to be not implemented
		} catch (Exception e) {
			Tests.fail("ImmortalPhysicalMemoryTest");
		}
	}
}