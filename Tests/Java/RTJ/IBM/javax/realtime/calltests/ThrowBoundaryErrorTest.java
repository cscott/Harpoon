package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class ThrowBoundaryErrorTest 
{

	public static void run() 
	{
		ThrowBoundaryError tbe = null;
		Object o = null;
		
		Tests.increment();
		try {
			tbe = new ThrowBoundaryError();
			if( !(tbe instanceof ThrowBoundaryError) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ThrowBoundaryErrorTest");
		}
		
		Tests.increment();
		try {
			String description = "blablabla";
			tbe = new ThrowBoundaryError(description);
			if( !(tbe instanceof ThrowBoundaryError) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ThrowBoundaryErrorTest");
		}
	}
}