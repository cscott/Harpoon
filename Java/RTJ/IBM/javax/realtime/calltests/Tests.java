package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import java.util.Vector;
import javax.realtime.*;

/**
 * tests that are commented out AND say "abstract: tested in" or "interface" are ok
 * others need to be fixed to work
 */

public class Tests 
{

	// options
	private static final boolean SHOWTESTS = false;
	private static final boolean SHOWFAILED = true;

	private static int count; // package-visible

	private static Vector failed;

	/**
 	 * Starts the application.
 	 * @param args an array of command-line arguments
 	 */
	public static void main(java.lang.String[] args) 
	{
		count = 0;
		failed = new Vector();
	
		System.out.println("Running tests...");

		RealtimeThreadTest.run();
		NoHeapRealtimeThreadTest.run();		// only constructors to test
		//	SchedulableTest.run();				// interface
		//	SchedulerTest.run();				// abstract: tested in PriorityScheduler
		PrioritySchedulerTest.run();
		//	SchedulingParametersTest.run();		// abstract: only a constructor
		PriorityParametersTest.run();
		ImportanceParametersTest.run();
		//	ReleaseParametersTest.run();		// abstract: tested in AperiodicParameters
		PeriodicParametersTest.run();
		AperiodicParametersTest.run();
		SporadicParametersTest.run();
		ProcessingGroupParametersTest.run();
		//	MemoryAreaTest.run();				// abstract: cannot test subclasses at the moment
		HeapMemoryTest.run();
		//	ImmortalMemoryTest.run();			// cannot instantiate to test
		//	ScopedMemoryTest.run();				// abstract: needs testing in below classes
		//	VTMemoryTest.run();					// cannot instantiate to test
		//	LTMemoryTest.run();					// cannot instantiate to test
		PhysicalMemoryFactoryTest.run();
		ImmortalPhysicalMemoryTest.run();
		//	ScopedPhysicalMemoryTest.run();		// causes stack trace
		RawMemoryAccessTest.run();
		RawMemoryFloatAccessTest.run();
		MemoryParametersTest.run();
		//	GarbageCollectorTest.run();			// abstract: all methods abstract
		//	IncrementalCollectorExample.run();	// needs GarbageCollector which is not yet implemented
		//	MarkAndSweepCollectorExample.run(); // needs GarbageCollector which is not yet implemented
		//	MonitorControlTest.run();			// abstract: tested in PriorityCeilingEmulationTest
		PriorityCeilingEmulationTest.run();
		PriorityInheritanceTest.run();
		//	WaitFreeDequeueTest.run();			// causes stack trace
		//	WaitFreeReadQueueTest.run();		// causes stack trace
		//	WaitFreeWriteQueueTest.run();		// causes stack trace
		//	HighResolutionTimeTest.run();		// abstract: tested in AbsoluteTimeTest
		AbsoluteTimeTest.run();				// subtract() causes stack overflow
		RelativeTimeTest.run();
		RationalTimeTest.run();
		ClockTest.run();					// abstract but testable - getRealTimeClock breaks
		//	TimerTest.run();					// abstract: should be tested by subclasses
		//	OneShotTimerTest.run();				// causes stack trace
		//	PeriodicTimerTest.run();			// causes stack trace
		//	AsyncEventTest.run();
		//	AsyncEventHandlerTest.run();		// abstract: no subclasses to test
		//	BoundAsyncEventHandlerTest.run();	// abstract: no subclasses to test
		//	InterruptibleTest.run();			// interface
		AsynchronouslyInterruptedExceptionTest.run();
		TimedTest.run();
		POSIXSignalHandlerTest.run();
		RealtimeSecurityTest.run();
		RealtimeSystemTest.run();
		IllegalAssignmentErrorTest.run();
		MemoryAccessErrorTest.run();
		MemoryScopeExceptionTest.run();
		OffsetOutOfBoundsExceptionTest.run();
		ResourceLimitErrorTest.run();
		SizeOutOfBoundsExceptionTest.run();
		ThrowBoundaryErrorTest.run();
		UnsupportedPhysicalMemoryExceptionTest.run();

		printReport();
	}

	/**
 	 * Package-visible
	 */
	static void increment() 
	{
		count++;
		if(SHOWTESTS)
			System.out.println("Running test: " + count);
	}

	/**
	  * Package-visible
	  */
	static void fail(String arg) 
	{
		if (SHOWFAILED)
		{
			System.out.println(arg + " failed at test count " + count);
		}
		failed.addElement( new Integer(count) );
	}

	static void fail()
	{
		failed.addElement( new Integer(count) );
	}

	private static void printReport() 
	{
		System.out.println(count + " tests RUN");
		System.out.println(failed.size() + " tests FAILED");
		/*	
		if(SHOWFAILED) 
		{
			System.out.println("Failures:");
			for(int i = 0; i < failed.size(); i++)
				System.out.println("  " + failed.elementAt(i));
		}
		*/	
	}

}