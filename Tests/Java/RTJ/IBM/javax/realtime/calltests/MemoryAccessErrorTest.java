package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class MemoryAccessErrorTest 
{

	public static void run() 
	{
		MemoryAccessError mae = null;
		Object o = null;
		
		Tests.increment();
		try {
			mae = new MemoryAccessError();
			if( !(mae instanceof MemoryAccessError) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("MemoryAccessErrorTest");
		}
		
		Tests.increment();
		try {
			String description = "blablabla";
			mae = new MemoryAccessError(description);
			if( !(mae instanceof MemoryAccessError) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("MemoryAccessErrorTest");
		}
	}
}