package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class ResourceLimitErrorTest 
{

	public static void run() 
	{
		ResourceLimitError rle = null;
		Object o = null;
		
		Tests.increment();
		try {
//			rle = new ResourceLimitError();
//			if( !(rle instanceof ResourceLimitError) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ResourceLimitErrorTest");
		}
		
		Tests.increment();
		try {
			String description = "blablabla";
//			rle = new ResourceLimitError(description);
//			if( !(rle instanceof ResourceLimitError) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ResourceLimitErrorTest");
		}
	}
}