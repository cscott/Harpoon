package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class MemoryScopeExceptionTest 
{

	public static void run() 
	{
		MemoryScopeException mse = null;
		Object o = null;
		
		Tests.increment();
		try {
			mse = new MemoryScopeException();
			if( !(mse instanceof MemoryScopeException) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("MemoryScopeExceptionTest");
		}
		
		Tests.increment();
		try {
			String description = "blablabla";
			mse = new MemoryScopeException(description);
			if( !(mse instanceof MemoryScopeException) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("MemoryScopeExceptionTest");
		}
	}
}