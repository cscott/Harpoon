package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class RealtimeSystemTest 
{
	
	public static void run() 
	{
		Object o = null;

		Tests.increment();
		try {
			byte b = RealtimeSystem.BIG_ENDIAN;
		} catch (Exception e) {
			Tests.fail("RealtimeSystemTest");
		}

		Tests.increment();
		try {
			byte b = RealtimeSystem.BYTE_ORDER;
		} catch (Exception e) {
			Tests.fail("RealtimeSystemTest");
		}

		Tests.increment();
		try {
			byte b = RealtimeSystem.LITTLE_ENDIAN;
		} catch (Exception e) {
			Tests.fail("RealtimeSystemTest");
		}

		Tests.increment();
		try {
			o = RealtimeSystem.currentGC();
			if( !(o instanceof GarbageCollector) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeSystemTest");
		}

		Tests.increment();
		try {
//			int i = RealtimeSystem.getConcurrentLocksUsed(); // not implemented
			throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeSystemTest");
		}

		Tests.increment();
		try {
//			int i = RealtimeSystem.getMaximumConcurrentLocks(); // not implemented
			throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeSystemTest");
		}

		Tests.increment();
		try {
			o = RealtimeSystem.getSecurityManager();
			if( !(o instanceof SecurityManager) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeSystemTest");
		}

		Tests.increment();
		try {
//			RealtimeSystem.setMaximumConcurrentLocks(10); // not implemented
			throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeSystemTest");
		}

		Tests.increment();
		try {
//			RealtimeSystem.setMaximumConcurrentLocks(10, true); // not implemented
			throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeSystemTest");
		}

		Tests.increment();
		try {
			RealtimeSecurity manager = new RealtimeSecurity();
			RealtimeSystem.setSecurityManager(manager);
			throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeSystemTest");
		}
	}
}