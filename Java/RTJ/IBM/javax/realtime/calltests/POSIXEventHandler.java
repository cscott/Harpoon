package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class POSIXEventHandler extends AsyncEventHandler
{
	POSIXEventHandler()
	{
	}
		
	public void handleAsyncEvent()
	{
		System.out.println("POSIXEventHandler testing hit handleAsyncEvent");
	}
}