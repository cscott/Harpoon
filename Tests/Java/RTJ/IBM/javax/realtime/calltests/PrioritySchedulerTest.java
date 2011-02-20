package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;
import java.lang.reflect.*;

public class PrioritySchedulerTest extends PriorityScheduler 
{

	public static void run() {
		PriorityScheduler ps = null;
		Object o = null;

		Tests.increment();
		try {
			ps = new PriorityScheduler();
			if( !(ps instanceof PriorityScheduler && ps instanceof Scheduler) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PrioritySchedulerTest");
		}

		Tests.increment();
		try {
			Schedulable s = null;
//			boolean b = ps.addToFeasibility(s);
//			if(false)
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PrioritySchedulerTest");
		}

		Tests.increment();
		try {
			Schedulable schedulable = null;
			ReleaseParameters release = null;
			MemoryParameters memory = null;
			boolean b = ps.changeIfFeasible(schedulable, release, memory);
		} catch (Exception e) {
			Tests.fail("PrioritySchedulerTest");
		}

		Tests.increment();
		try {
			Schedulable schedulable = null;
			ps.fireSchedulable(schedulable);
		} catch (Exception e) {
			Tests.fail("PrioritySchedulerTest");
		}

		Tests.increment();
		try {
			int x = PriorityScheduler.getMaxPriority(new Thread());
		} catch (Exception e) {
			Tests.fail("PrioritySchedulerTest");
		}

		Tests.increment();
		try {
			int x = ps.getMinPriority();
		} catch (Exception e) {
			Tests.fail("PrioritySchedulerTest");
		}

		Tests.increment();
		try {
			int x = PriorityScheduler.getMinPriority(new Thread());
		} catch (Exception e) {
			Tests.fail("PrioritySchedulerTest");
		}
		
		Tests.increment();
		try {
			int x = ps.getNormPriority();
		} catch (Exception e) {
			Tests.fail("PrioritySchedulerTest");
		}

		Tests.increment();
		try {
			int x = PriorityScheduler.getNormPriority(new Thread());
		} catch (Exception e) {
			Tests.fail("PrioritySchedulerTest");
		}
		
		Tests.increment();
		try {
			o = ps.getPolicyName();
			if( !(o instanceof String) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PrioritySchedulerTest");
		}
		
		Tests.increment();
		try {
			o = PriorityScheduler.instance();
			if( !(o instanceof PriorityScheduler && o instanceof Scheduler) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PrioritySchedulerTest");
		}
		
		Tests.increment();
		try {
			boolean b = ps.isFeasible();
		} catch (Exception e) {
			Tests.fail("PrioritySchedulerTest");
		}

		Tests.increment();
		try {
			Schedulable s = null;
//			ps.removeFromFeasibility(s); // protected
		} catch (Exception e) {
			Tests.fail("PrioritySchedulerTest");
		}

// Scheduler tests

		Tests.increment();
		try {
			o = ps.getDefaultScheduler();
			if( !(o instanceof Scheduler) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("PrioritySchedulerTest");
		}

		Tests.increment();
		try {
			Scheduler scheduler = null;
			PriorityScheduler.setDefaultScheduler(scheduler);
		} catch (Exception e) {
			Tests.fail("PrioritySchedulerTest");
		}

//		example();
		Tests.increment(); Tests.fail("PrioritySchedulerTest"); // example won't run
	}

	public static void example() {
		Tests.increment();
		try {
			Scheduler scheduler = findScheduler("EDF");
			if( scheduler != null ) {
				RealtimeThread t1 =
					new RealtimeThread(	(SchedulingParameters) new PriorityParameters(11),
										(ReleaseParameters) new PeriodicParameters(	null,
																new RelativeTime(100, 0),
																new RelativeTime(5,0),
																new RelativeTime(50,0),
																null,
																null)) {
											public void run() {}
										};
			}
		} catch (Exception e) {
			Tests.fail("PrioritySchedulerTest");
		}
	}

	public static Scheduler findScheduler(String policy) throws Exception {
		String className = System.getProperty("javax.realtime.scheduler." + policy);
		Class clazz;
		try {
			if(className != null && (clazz = Class.forName(className)) != null) {
				return (Scheduler) clazz.getMethod("instance",null).invoke(null,null);
			}
		} catch (ClassNotFoundException notFound) {
		} catch (NoSuchMethodException noSuch) {
		} catch (SecurityException security) {
		} catch (IllegalAccessException access) {
		} catch (IllegalArgumentException arg) {
		} catch (InvocationTargetException target) {
		}
		return null;
	}			
}