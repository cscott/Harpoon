package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class UnsupportedPhysicalMemoryExceptionTest 
{

	public static void run() 
	{
		UnsupportedPhysicalMemoryException upme = null;
		Object o = null;
		
		Tests.increment();
		try {
			upme = new UnsupportedPhysicalMemoryException();
			if( !(upme instanceof UnsupportedPhysicalMemoryException) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("UnsupportedPhysicalMemoryExceptionTest");
		}
		
		Tests.increment();
		try {
			String description = "blablabla";
			upme = new UnsupportedPhysicalMemoryException(description);
			if( !(upme instanceof UnsupportedPhysicalMemoryException) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("UnsupportedPhysicalMemoryExceptionTest");
		}
	}
}