package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */


import javax.realtime.*;

public class IllegalAssignmentErrorTest 
{

	public static void run() 
	{
		IllegalAssignmentError iae = null;
		Object o = null;
		
		Tests.increment();
		try {
			iae = new IllegalAssignmentError();
			if( !(iae instanceof IllegalAssignmentError) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("IllegalAssignmentErrorTest");
		}
		
		Tests.increment();
		try {
			String description = "blablabla";
			iae = new IllegalAssignmentError(description);
			if( !(iae instanceof IllegalAssignmentError) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("IllegalAssignmentErrorTest");
		}
	}
}