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

public class AllTests
{
    public static void main(java.lang.String[] args)
    {
        Tests.init();

        System.out.println("Tests: AbsoluteTimeTest");
        AbsoluteTimeTest.run();

        System.out.println("Tests: AperiodicParametersTest");
        AperiodicParametersTest.run();

        System.out.println("Tests: AsyncEventTest");
        AsyncEventTest.run();

        System.out.println("Tests: AsyncEventHandlerTest");
        AsyncEventHandlerTest.run();

        System.out.println("Tests: AsynchronouslyInterruptedExceptionTest");
        AsynchronouslyInterruptedExceptionTest.run();

        System.out.println("Tests: BoundAsyncEventHandlerTest");
        BoundAsyncEventHandlerTest.run();

        System.out.println("Tests: ClockTest");
        ClockTest.run();

        System.out.println("Tests: DuplicateFilterExceptionTest");
        DuplicateFilterExceptionTest.run();

        System.out.println("Tests: HeapMemoryTest");
        HeapMemoryTest.run();

        System.out.println("Tests: IllegalAssignmentErrorTest");
        IllegalAssignmentErrorTest.run();

        System.out.println("Tests: ImmortalMemoryTest");
        ImmortalMemoryTest.run();

        System.out.println("Tests: ImmortalPhysicalMemoryTest");
        ImmortalPhysicalMemoryTest.run();

        System.out.println("Tests: ImportanceParametersTest");
        ImportanceParametersTest.run();

        System.out.println("Tests: InaccessibleAreaExceptionTest");
        InaccessibleAreaExceptionTest.run();

        System.out.println("Tests: LTMemoryTest");
        LTMemoryTest.run();

        System.out.println("Tests: LTPhysicalMemoryTest");
        LTPhysicalMemoryTest.run();

        System.out.println("Tests: MarkAndSweepCollectorTest");
        MarkAndSweepCollectorTest.run();

        System.out.println("Tests: MemoryAccessErrorTest");
        MemoryAccessErrorTest.run();

        System.out.println("Tests: MemoryInUseExceptionTest");
        MemoryInUseExceptionTest.run();

        System.out.println("Tests: MemoryParametersTest");
        MemoryParametersTest.run();

        System.out.println("Tests: MemoryScopeExceptionTest");
        MemoryScopeExceptionTest.run();

        System.out.println("Tests: MemoryTypeConflictExceptionTest");
        MemoryTypeConflictExceptionTest.run();

        System.out.println("Tests: MITViolationExceptionTest");
        MITViolationExceptionTest.run();

        System.out.println("Tests: NoHeapRealtimeThreadTest");
        NoHeapRealtimeThreadTest.run();

        System.out.println("Tests: OffsetOutOfBoundsExceptionTest");
        OffsetOutOfBoundsExceptionTest.run();

        System.out.println("Tests: OneShotTimerTest");
        OneShotTimerTest.run();

        System.out.println("Tests: PeriodicParametersTest");
        PeriodicParametersTest.run();

        System.out.println("Tests: PeriodicTimerTest");
        PeriodicTimerTest.run();

        System.out.println("Tests: PhysicalMemoryManagerTest");
        PhysicalMemoryManagerTest.run();

        System.out.println("Tests: POSIXSignalHandlerTest");
        POSIXSignalHandlerTest.run();

        System.out.println("Tests: PriorityCeilingEmulationTest");
        PriorityCeilingEmulationTest.run();

        System.out.println("Tests: PriorityInheritanceTest");
        PriorityInheritanceTest.run();

        System.out.println("Tests: PriorityParametersTest");
        PriorityParametersTest.run();

        System.out.println("Tests: PrioritySchedulerTest");
        PrioritySchedulerTest.run();

        System.out.println("Tests: ProcessingGroupParametersTest");
        ProcessingGroupParametersTest.run();

        System.out.println("Tests: RationalTimeTest");
        RationalTimeTest.run();

        System.out.println("Tests: RawMemoryAccessTest");
        RawMemoryAccessTest.run();

        System.out.println("Tests: RawMemoryFloatAccessTest");
        RawMemoryFloatAccessTest.run();

        System.out.println("Tests: RealtimeSecurityTest");
        RealtimeSecurityTest.run();

        System.out.println("Tests: RealtimeSystemTest");
        RealtimeSystemTest.run();

        System.out.println("Tests: RealtimeThreadTest");
        RealtimeThreadTest.run();

        System.out.println("Tests: RelativeTimeTest");
        RelativeTimeTest.run();

        System.out.println("Tests: ResourceLimitErrorTest");
        ResourceLimitErrorTest.run();


        System.out.println("Tests: SchedulerTest");
        SchedulerTest.run();

        System.out.println("Tests: ScopedCycleExceptionTest");
        ScopedCycleExceptionTest.run();

        System.out.println("Tests: SizeEstimatorTest");
        SizeEstimatorTest.run();

        System.out.println("Tests: SizeOutOfBoundsExceptionTest");
        SizeOutOfBoundsExceptionTest.run();

        System.out.println("Tests: SporadicParametersTest");
        SporadicParametersTest.run();

        System.out.println("Tests: ThrowBoundaryErrorTest");
        ThrowBoundaryErrorTest.run();

        System.out.println("Tests: TimedTest");
        TimedTest.run();

        System.out.println("Tests: UnknownHappeningExceptionTest");
        UnknownHappeningExceptionTest.run();

        System.out.println("Tests: UnsupportedPhysicalMemoryExceptionTest");
        UnsupportedPhysicalMemoryExceptionTest.run();

        System.out.println("Tests: VTMemoryTest");
        VTMemoryTest.run();

        System.out.println("Tests: VTPhysicalMemoryTest");
        VTPhysicalMemoryTest.run();

        System.out.println("Tests: WaitFreeDequeueTest");
        WaitFreeDequeueTest.run();
        Tests.delay(2);

        System.out.println("Tests: WaitFreeReadQueueTest");
        WaitFreeReadQueueTest.run();
        Tests.delay(2);

        System.out.println("Tests: WaitFreeWriteQueueTest");
        WaitFreeWriteQueueTest.run();
        Tests.delay(2);

        Tests.conclude();
    }
}
