package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class OffsetOutOfBoundsExceptionTest 
{

	public static void run() 
	{
		OffsetOutOfBoundsException ooobe = null;
		Object o = null;
		
		Tests.increment();
		try {
			ooobe = new OffsetOutOfBoundsException();
			if( !(ooobe instanceof OffsetOutOfBoundsException) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("OutOfBoundsExceptionTest");
		}
		
		Tests.increment();
		try {
			String description = "blablabla";
			ooobe = new OffsetOutOfBoundsException(description);
			if( !(ooobe instanceof OffsetOutOfBoundsException) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("OutOfBoundsExceptionTest");
		}
	}
}