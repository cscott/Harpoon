/* Tests - this is the main logic that calls every RTSJ API test class.
*/

// package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

import java.util.Vector;
import javax.realtime.*;
import com.timesys.*;

public class OneTest
{
    private java.lang.String[] m_args;

    public void RunTests()
    {
        String testName;
        int i;

        Tests.init();

        for (i = 0; i < m_args.length; i++) {
            testName = m_args[i];

            System.out.println("");
            System.out.println("********************************************");
            System.out.println("");

            if (testName.equals("AbsoluteTimeTest")) {
                System.out.println("Tests: AbsoluteTimeTest");
                AbsoluteTimeTest.run();
            }

            if (testName.equals("AperiodicParametersTest")) {
                System.out.println("Tests: AperiodicParametersTest");
                AperiodicParametersTest.run();
            }

            if (testName.equals("AsyncEventTest")) {
                System.out.println("Tests: AsyncEventTest");
                AsyncEventTest.run();
            }

            if (testName.equals("AsyncEventHandlerTest")) {
                System.out.println("Tests: AsyncEventHandlerTest");
                AsyncEventHandlerTest.run();
            }

            if (testName.equals("AsynchronouslyInterruptedExceptionTest")) {
                System.out.println("Tests: AsynchronouslyInterruptedExceptionTest");
                AsynchronouslyInterruptedExceptionTest.run();
            }

            if (testName.equals("BoundAsyncEventHandlerTest")) {
                System.out.println("Tests: BoundAsyncEventHandlerTest");
                BoundAsyncEventHandlerTest.run();
            }

            if (testName.equals("ClockTest")) {
                System.out.println("Tests: ClockTest");
                ClockTest.run();
            }

            if (testName.equals("DuplicateFilterExceptionTest")) {
                System.out.println("Tests: DuplicateFilterExceptionTest");
                DuplicateFilterExceptionTest.run();
            }

            if (testName.equals("HeapMemoryTest")) {
                System.out.println("Tests: HeapMemoryTest");
                HeapMemoryTest.run();
            }

            if (testName.equals("IllegalAssignmentErrorTest")) {
                System.out.println("Tests: IllegalAssignmentErrorTest");
                IllegalAssignmentErrorTest.run();
            }

            if (testName.equals("ImmortalMemoryTest")) {
                System.out.println("Tests: ImmortalMemoryTest");
                ImmortalMemoryTest.run();
            }

            if (testName.equals("ImmortalPhysicalMemoryTest")) {
                System.out.println("Tests: ImmortalPhysicalMemoryTest");
                ImmortalPhysicalMemoryTest.run();
            }

            if (testName.equals("ImportanceParametersTest")) {
                System.out.println("Tests: ImportanceParametersTest");
                ImportanceParametersTest.run();
            }

            if (testName.equals("InaccessibleAreaExceptionTest")) {
                System.out.println("Tests: InaccessibleAreaExceptionTest");
                InaccessibleAreaExceptionTest.run();
            }

            if (testName.equals("LTMemoryTest")) {
                System.out.println("Tests: LTMemoryTest");
                LTMemoryTest.run();
            }

            if (testName.equals("LTPhysicalMemoryTest")) {
                System.out.println("Tests: LTPhysicalMemoryTest");
                LTPhysicalMemoryTest.run();
            }

            if (testName.equals("MarkAndSweepCollectorTest")) {
                System.out.println("Tests: MarkAndSweepCollectorTest");
                MarkAndSweepCollectorTest.run();
            }

            if (testName.equals("MemoryAccessErrorTest")) {
                System.out.println("Tests: MemoryAccessErrorTest");
                MemoryAccessErrorTest.run();
            }

            if (testName.equals("MemoryInUseExceptionTest")) {
                System.out.println("Tests: MemoryInUseExceptionTest");
                MemoryInUseExceptionTest.run();
            }

            if (testName.equals("MemoryParametersTest")) {
                System.out.println("Tests: MemoryParametersTest");
                MemoryParametersTest.run();
            }

            if (testName.equals("MemoryScopeExceptionTest")) {
                System.out.println("Tests: MemoryScopeExceptionTest");
                MemoryScopeExceptionTest.run();
            }

            if (testName.equals("MemoryTypeConflictExceptionTest")) {
                System.out.println("Tests: MemoryTypeConflictExceptionTest");
                MemoryTypeConflictExceptionTest.run();
            }

            if (testName.equals("MITViolationExceptionTest")) {
                System.out.println("Tests: MITViolationExceptionTest");
                MITViolationExceptionTest.run();
            }

            if (testName.equals("NoHeapRealtimeThreadTest")) {
                System.out.println("Tests: NoHeapRealtimeThreadTest");
                NoHeapRealtimeThreadTest.run();
            }

            if (testName.equals("OffsetOutOfBoundsExceptionTest")) {
                System.out.println("Tests: OffsetOutOfBoundsExceptionTest");
                OffsetOutOfBoundsExceptionTest.run();
            }

            if (testName.equals("OneShotTimerTest")) {
                System.out.println("Tests: OneShotTimerTest");
                OneShotTimerTest.run();
            }

            if (testName.equals("PeriodicParametersTest")) {
                System.out.println("Tests: PeriodicParametersTest");
                PeriodicParametersTest.run();
            }

            if (testName.equals("PeriodicTimerTest")) {
                System.out.println("Tests: PeriodicTimerTest");
                PeriodicTimerTest.run();
            }

            if (testName.equals("PhysicalMemoryManagerTest")) {
                System.out.println("Tests: PhysicalMemoryManagerTest");
                PhysicalMemoryManagerTest.run();
            }

            if (testName.equals("POSIXSignalHandlerTest")) {
                System.out.println("Tests: POSIXSignalHandlerTest");
                POSIXSignalHandlerTest.run();
            }

            if (testName.equals("PriorityCeilingEmulationTest")) {
                System.out.println("Tests: PriorityCeilingEmulationTest");
                PriorityCeilingEmulationTest.run();
            }

            if (testName.equals("PriorityInheritanceTest")) {
                System.out.println("Tests: PriorityInheritanceTest");
                PriorityInheritanceTest.run();
            }

            if (testName.equals("PriorityParametersTest")) {
                System.out.println("Tests: PriorityParametersTest");
                PriorityParametersTest.run();
            }

            if (testName.equals("PrioritySchedulerTest")) {
                System.out.println("Tests: PrioritySchedulerTest");
                PrioritySchedulerTest.run();
            }

            if (testName.equals("ProcessingGroupParametersTest")) {
                System.out.println("Tests: ProcessingGroupParametersTest");
                ProcessingGroupParametersTest.run();
            }

            if (testName.equals("RationalTimeTest")) {
                System.out.println("Tests: RationalTimeTest");
                RationalTimeTest.run();
            }

            if (testName.equals("RawMemoryAccessTest")) {
                System.out.println("Tests: RawMemoryAccessTest");
                RawMemoryAccessTest.run();
            }

            if (testName.equals("RawMemoryFloatAccessTest")) {
                System.out.println("Tests: RawMemoryFloatAccessTest");
                RawMemoryFloatAccessTest.run();
            }

            if (testName.equals("RealtimeSecurityTest")) {
                System.out.println("Tests: RealtimeSecurityTest");
                RealtimeSecurityTest.run();
            }

            if (testName.equals("RealtimeSystemTest")) {
                System.out.println("Tests: RealtimeSystemTest");
                RealtimeSystemTest.run();
            }

            if (testName.equals("RealtimeThreadTest")) {
                System.out.println("Tests: RealtimeThreadTest");
                RealtimeThreadTest.run();
            }

            if (testName.equals("RelativeTimeTest")) {
                System.out.println("Tests: RelativeTimeTest");
                RelativeTimeTest.run();
            }

            if (testName.equals("ResourceLimitErrorTest")) {
                System.out.println("Tests: ResourceLimitErrorTest");
                ResourceLimitErrorTest.run();
            }

            if (testName.equals("SchedulerTest")) {
                System.out.println("Tests: SchedulerTest");
                SchedulerTest.run();
            }

            if (testName.equals("ScopedCycleExceptionTest")) {
                System.out.println("Tests: ScopedCycleExceptionTest");
                ScopedCycleExceptionTest.run();
            }

            if (testName.equals("SizeEstimatorTest")) {
                System.out.println("Tests: SizeEstimatorTest");
                SizeEstimatorTest.run();
            }

            if (testName.equals("SizeOutOfBoundsExceptionTest")) {
                System.out.println("Tests: SizeOutOfBoundsExceptionTest");
                SizeOutOfBoundsExceptionTest.run();
            }

            if (testName.equals("SporadicParametersTest")) {
                System.out.println("Tests: SporadicParametersTest");
                SporadicParametersTest.run();
            }

            if (testName.equals("ThrowBoundaryErrorTest")) {
                System.out.println("Tests: ThrowBoundaryErrorTest");
                ThrowBoundaryErrorTest.run();
            }

            if (testName.equals("TimedTest")) {
                System.out.println("Tests: TimedTest");
                TimedTest.run();
            }

            if (testName.equals("UnknownHappeningExceptionTest")) {
                System.out.println("Tests: UnknownHappeningExceptionTest");
                UnknownHappeningExceptionTest.run();
            }

            if (testName.equals("UnsupportedPhysicalMemoryExceptionTest")) {
                System.out.println("Tests: UnsupportedPhysicalMemoryExceptionTest");
                UnsupportedPhysicalMemoryExceptionTest.run();
            }

            if (testName.equals("VTMemoryTest")) {
                System.out.println("Tests: VTMemoryTest");
                VTMemoryTest.run();
            }

            if (testName.equals("VTPhysicalMemoryTest")) {
                System.out.println("Tests: VTPhysicalMemoryTest");
                VTPhysicalMemoryTest.run();
            }

            if (testName.equals("WaitFreeDequeueTest")) {
                System.out.println("Tests: WaitFreeDequeueTest");
                WaitFreeDequeueTest.run();
            }

            if (testName.equals("WaitFreeReadQueueTest")) {
                System.out.println("Tests: WaitFreeReadQueueTest");
                WaitFreeReadQueueTest.run();
            }

            if (testName.equals("WaitFreeWriteQueueTest")) {
                System.out.println("Tests: WaitFreeWriteQueueTest");
                WaitFreeWriteQueueTest.run();
            }

            System.out.println("");
            System.out.println("********************************************");
            System.out.println("");

        }

        Tests.conclude();
    }

    static class TestThread extends RealtimeThread
    {
        private java.lang.String[] m_args;

        public TestThread(java.lang.String[] args)
        {
            int i;

            m_args = new String[args.length];
            for (i = 0; i < args.length; i++) {
                m_args[i] = args[i];
            }
        }

        public void run()
        {
            System.out.println("Starting RUN");
            OneTest ot = new OneTest();
            ot.m_args = m_args;
            ot.RunTests();
            System.out.println("Ending RUN");
        }
    }

    /*
    public static void main(java.lang.String[] args)
    {
        ImmortalMemory IM = ImmortalMemory.instance();

        try {
            IM.executeInArea(new TestThread(args));
        }
        catch (Exception e) {
            System.out.println("Caught exception " + e.toString());
        }
    }
    */

    public static void main(java.lang.String[] args)
    {
        OneTest ot = new OneTest();
        ot.m_args = args;
        ot.RunTests();
    }
}
