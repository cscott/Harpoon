package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class AsyncEventTest 
{
/*	
	public static void run() {
		AsyncEvent ae = null;
		Object o = null;

		Tests.increment();
		try {
			ae = new AsyncEvent();
			if( !(ae instanceof AsyncEvent) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail();
		}

		Tests.increment();
		try {
			AsyncEventHandler handler = null;
			ae.addHandler(handler);
		} catch (Exception e) {
			Tests.fail();
		}

		Tests.increment();
		try {
			String happening = "jimbo";
			ae.bindTo(happening);
		} catch (Exception e) {
			Tests.fail();
		}

		Tests.increment();
		try {
			o = ae.createReleaseParameters();
			if( !(o instanceof ReleaseParameters) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail();
		}

		Tests.increment();
		try {
			ae.fire();
		} catch (Exception e) {
			Tests.fail();
		}

		Tests.increment();
		try {
			AsyncEventHandler target = null;
			boolean x = ae.handledBy(target);
		} catch (Exception e) {
			Tests.fail();
		}

		Tests.increment();
		try {
			AsyncEventHandler target = null;
			ae.removeHandler(target);
		} catch (Exception e) {
			Tests.fail();
		}

		Tests.increment();
		try {
			AsyncEventHandler target = null;
			ae.setHandler(target);
		} catch (Exception e) {
			Tests.fail();
		}

		Tests.increment();
		try {
//			example1();
				throw new Exception();
		} catch (Exception e) {
			Tests.fail();
		}

		Tests.increment();
		try {
//			example2();
				throw new Exception();
		} catch (Exception e) {
			Tests.fail();
		}

		Tests.increment();
		try {
//			example3();	// AIE example 1
				throw new Exception();
		} catch (Exception e) {
			Tests.fail();
		}

		Tests.increment();
		try {
//			example4();	// AIE example 2
				throw new Exception();
		} catch (Exception e) {
			Tests.fail();
		}

		Tests.increment();
		try {
//			example4();	// AIE example 3
				throw new Exception();
		} catch (Exception e) {
			Tests.fail();
		}

		Tests.increment();
		try {
//			example4();	// AIE example 4
				throw new Exception();
		} catch (Exception e) {
			Tests.fail();
		}
	}

	public static void example1() throws Exception {
		AsyncEventHandler h = new AsyncEventHandler() {
			public void handleAsyncEvent() {
				// System.out.println("Thefirst handler ran!\n");
			}
		};
		inputReady.addHandler(h);
		//System.out.print("Test 1\n");
		inputReady.fire();
		Thread.yield();
//		System.out.print("Fired the event\n");

		SchedulingParameters low = new PriorityParameters(PriorityScheduler.getMinPriority(null));
		inputReady.setHandler(new AsyncEventHandler(low,null,null) {
			public void handleAsyncEvent() {
				System.out.print("The low priority handler ran!\n");
			}
		});
		SchedulingParameters high = new PriorityParameters(PriorityScheduler.getMaxPriority(null));
		inputReady.addHandler(new AsyncEventHandler(high, null, null) {
			public void handleAsyncEvent() {
				System.out.print("The high priority handler ran!\n");
			}
		});
		
		System.out.print("\nTest2\n");
		inputReady.fire();
		System.out.print("After the fire\n");
		Thread.sleep(100);
		System.print("After the sleep\n");
		
		ReleaseParameters rp = inputReady.createReleaseParameters();
		rp.setCost(new RelativeTime(1,0));
		AsyncEventHandler h2 = new AsyncEventHandler(high, rp, null) {
			public void handleAsyncEvent() { System.out.print("WHatever...\n"); }};
	}

	public static void example2() throws Exception {
		MyInterrupt.runexample();
	}
*/	
}