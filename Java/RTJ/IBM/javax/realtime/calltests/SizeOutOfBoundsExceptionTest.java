package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class SizeOutOfBoundsExceptionTest 
{

	public static void run() 
	{
		SizeOutOfBoundsException soobe = null;
		Object o = null;
		
		Tests.increment();
		try {
			soobe = new SizeOutOfBoundsException();
			if( !(soobe instanceof SizeOutOfBoundsException) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("SizeOutOfBoundsExceptionTest");
		}
		
		Tests.increment();
		try {
			String description = "blablabla";
			soobe = new SizeOutOfBoundsException(description);
			if( !(soobe instanceof SizeOutOfBoundsException) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("SizeOutOfBoundsExceptionTest");
		}
	}
}