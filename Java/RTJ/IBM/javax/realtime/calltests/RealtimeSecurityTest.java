package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class RealtimeSecurityTest 
{
	public static void run() 
	{
		RealtimeSecurity rs = null;
		Object o = null;

		Tests.increment();
		try {
			rs = new RealtimeSecurity();
			if( !(rs instanceof RealtimeSecurity) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeSecurityTest");
		}

		Tests.increment();
		try {
			rs.checkAccessPhysical();
		} catch (SecurityException se) {
		} catch (Exception e) {
			Tests.fail("RealtimeSecurityTest");
		}

		Tests.increment();
		try {
			rs.checkAccessPhysicalRange(100L, 100L);
		} catch (SecurityException se) {
		} catch (Exception e) {
			Tests.fail("RealtimeSecurityTest");
		}

		Tests.increment();
		try {
			rs.checkSetFactory();
		} catch (SecurityException se) {
		} catch (Exception e) {
			Tests.fail("RealtimeSecurityTest");
		}

		Tests.increment();
		try {
//			rs.checkSetScheduler(); // not implemented
			throw new Exception();
		} catch (SecurityException se) {
		} catch (Exception e) {
			Tests.fail("RealtimeSecurityTest");
		}
	}
}