package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */


import javax.realtime.*;

public class PriorityCeilingEmulationTest 
{

	public static void run() 
	{
		PriorityCeilingEmulation pce = null;
		Object o = null;

		Tests.increment();
		try {
			int ceiling = 10;
			pce = new PriorityCeilingEmulation(ceiling);
			if( !(pce instanceof PriorityCeilingEmulation && pce instanceof MonitorControl) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PriorityCeilingEmulationTest");
		}

		Tests.increment();
		try {
			int defaultCeiling = pce.getDefaultCeiling();
		} catch (Exception e) {
			Tests.fail("PriorityCeilingEmulationTest");
		}

		Tests.increment();
		try {
			MonitorControl policy = null;
			pce.setMonitorControl(policy);
		} catch (Exception e) {
			Tests.fail("PriorityCeilingEmulationTest");
		}

		Tests.increment();
		try {
			Object monitor = null;
			MonitorControl policy = null;
			pce.setMonitorControl(monitor, policy);
		} catch (Exception e) {
			Tests.fail("PriorityCeilingEmulationTest");
		}
	}
}