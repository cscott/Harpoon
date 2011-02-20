package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class RealtimeThreadTest 
{

	public static void run() 
	{
		RealtimeThread rtt = null;
		Object o = null;
/*
		Tests.increment();
		try {
			SchedulingParameters scheduling = null;
			ReleaseParameters release = null;
			MemoryParameters memory = null;
			MemoryArea area = null;
			ProcessingGroupParameters group = null;
			java.lang.Runnable logic = null;
			rtt = new RealtimeThread(scheduling, release, memory, area, group, logic);
			if( !(rtt instanceof RealtimeThread && rtt instanceof RealtimeThread) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			SchedulingParameters scheduling = null;
			ReleaseParameters release = null;
			rtt = new RealtimeThread(scheduling, release);
			if( !(rtt instanceof RealtimeThread && rtt instanceof RealtimeThread) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			SchedulingParameters scheduling = null;
			rtt = new RealtimeThread(scheduling);
			if( !(rtt instanceof RealtimeThread && rtt instanceof RealtimeThread) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}
*/
		Tests.increment();
		try {
			rtt = new RealtimeThread();
			if( !(rtt instanceof RealtimeThread && rtt instanceof Thread) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			rtt.addToFeasibility();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			o = RealtimeThread.currentRealtimeThread();
			if( !(o instanceof RealtimeThread) )
				throw new Exception();
		} catch (ClassCastException cce) { // should pass
			if( (o instanceof RealtimeThread) )
				Tests.fail("RealtimeThreadTest");
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			rtt.deschedulePeriodic();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
//			Object o = rtt.getMemoryArea();
//			if( !(o instanceof MemoryArea) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			o = rtt.getMemoryParameters();
			if( !(o instanceof MemoryParameters) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			o = rtt.getProcessingGroupParameters();
			if( !(o instanceof ProcessingGroupParameters) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			o = rtt.getReleaseParameters();
			if( !(o instanceof ReleaseParameters) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			o = rtt.getSchedulingParameters();
			if( !(o instanceof SchedulingParameters) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			rtt.interrupt();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			rtt.removeFromFeasibility();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			MemoryParameters mp = null;
			rtt.setMemoryParameters(null);
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			ProcessingGroupParameters pgp = null;
			rtt.setProcessingGroupParameters(pgp);
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			ReleaseParameters rp = null;
			rtt.setReleaseParameters(rp);
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			Scheduler s = null;
			rtt.setScheduler(s);
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			SchedulingParameters sp = null;
			rtt.setSchedulingParameters(sp);
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			Clock c = null;
			HighResolutionTime time = null;
			rtt.sleep(c,time);
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			HighResolutionTime time = null;
//			rtt.sleep(time);
//			if(false)
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			boolean b = rtt.waitForNextPeriod();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}
		
		example();
	}

	public static void example() {
		Tests.increment();
		try {
			RealtimeThread rt = new RealtimeThread();

			if( !rt.getScheduler().isFeasible() )
				throw new Exception();
			rt.start();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}

		Tests.increment();
		try {
			SchedulingParameters sp = new PriorityParameters(PriorityScheduler.getNormPriority(null));
//			RealtimeThread rt = new RealtimeThread(sp);
//			if( !rt.getScheduler().isFeasible() )
				throw new Exception();
//			rt.start();
		} catch (Exception e) {
			Tests.fail("RealtimeThreadTest");
		}
	}
}