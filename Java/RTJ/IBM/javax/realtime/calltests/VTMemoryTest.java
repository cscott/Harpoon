package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */


import javax.realtime.*;

public class VTMemoryTest 
{

	public static void run() 
	{
		VTMemory vtm = null;
		Object o = null;

		Tests.increment();
		try {
			int initial = 1000;
			int maximum = 2000;
			vtm = new VTMemory(initial, maximum);
			if( !(vtm instanceof VTMemory && vtm instanceof ScopedMemory) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("VTmemoryTest");
		}

		Tests.increment();
		try {
			example();
		} catch (Exception e) {
			Tests.fail("VTmemoryTest");
		}

		Tests.increment();
		try {
//			example2();
			throw new Exception();
		} catch (Exception e) {
			Tests.fail("VTmemoryTest");
		}
	}

	public static void example() {
		final ScopedMemory scope = new LTMemory(16 * 1024, 2*(16*1024));
		
		scope.enter(new Runnable() {
			public void run() {
				try {
					HeapMemory.instance().newInstance(Class.forName("Foo"));
					scope.getOuterScope().newInstance(Class.forName("Foo"));
				} catch (ClassNotFoundException e) {
				} catch (IllegalAccessException ia) {
				} catch (InstantiationException ie) {
			}}});
	}

	public static void example2() {
		final ScopedMemory scope = new LTMemory(16 * 1024, 2*(16*1024));
		RealtimeThread t1 = new RealtimeThread((SchedulingParameters) new PriorityParameters(11),
											   new MemoryParameters(scope),
												new Runnable() {
													public void run() {}
												});
		RealtimeThread t2 = new RealtimeThread((SchedulingParameters) new PriorityParameters(11),
												new MemoryParameters(scope),
												new Runnable() {
													public void run() {}
												});
		boolean interrupted = false;
		do {
			try {
				t1.join();
			} catch (InterruptedException ie) {
				interrupted = true;
			}
		} while (interrupted);
		interrupted = false;
		do {
			try {
				t2.join();
			} catch (InterruptedException ie) {
				interrupted = true;
			}
		} while (interrupted);
		
		RealtimeThread t3 = new RealtimeThread((SchedulingParameters) new PriorityParameters(11),
												new MemoryParameters(scope),
												new Runnable() {
													public void run() {}
												});
		t3.start();
	}
}