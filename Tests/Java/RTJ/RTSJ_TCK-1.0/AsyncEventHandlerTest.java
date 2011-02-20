//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              AsyncEventHandlerTest

Subtest 1:
        "public AsyncEventHandler()"

Subtest 2:
        "public AsyncEventHandler(Scheduling Parameters scheduling,
        ReleaseParameters release, MemoryParameters memory, MemoryArea area,
        ProcessingGroupParameters group)

Subtest 3:
        "public AsyncEventHandler(Scheduling Parameters scheduling,
        ReleaseParameters release, MemoryParameters memory, MemoryArea area,
        ProcessingGroupParameters group, boolean noheap)" where all given
        parameters are null

Subtest 4:
        "public AsyncEventHandler(Scheduling Parameters scheduling,
        ReleaseParameters release, MemoryParameters memory, MemoryArea area,
        ProcessingGroupParameters group, boolean noheap)"

Subtest 5:
        "public AsyncEventHandler(boolean noheap)" where noheap is true
        ** IllegalArgumentException is expected
        ** Reason is because the current thread is not a
        ** NoHeapRealtimeThread

Subtest 6:
        "public AsyncEventHandler(boolean noheap)" where noheap is false

Subtest 7:
        "public void addToFeasibility()"

Subtest 8:
        "protected final synchronized int getAndIncrementPendingFireCount()"
        "protected final synchronized int getAndClearPendingFireCount()"
        "public abstract void handlerAsyncEvent()"

Subtest 9:
        "protected final synchronized int getAndDecrementPendingFireCount()"
        "protected final synchronized int getAndClearPendingFireCount()"

Subtest 10:
        "public MemoryArea getMemoryArea()"

Subtest 11:
        "public MemoryParameters getMemoryParameters()"

Subtest 11:
        "public int getPendingFireCount()"

Subtest 12:
        "public ProcessingGroupParameters getProcessingGroupParameters()

Subtest 13:
        "public ReleaseParameters getReleaseParameters()"

Subtest 14:
        "public Scheduler getScheduler()"

Subtest 15:
        "public SchedulingParameters getSchedulingParameters"

Subtest 16:
        "public void removeFromFeasibility()"

Subtest 17:
        "public void setMemoryParameters(MemoryParameters memory)"

Subtest NEW:
        "public void setMemoryParametersIfFeasible(MemoryParameters memory)"

Subtest 18:
        "public void setProcessingGroupParameters(PGP)"

Subtest NEW:
        "public void setProcessingGroupParametersIfFeasible(PGP)"

Subtest 19:
        "public void setReleaseParameters(ReleaseParameters parameters):

Subtest NEW:
        "public void setReleaseParametersIfFeasible(ReleaseParameters parameters):

Subtest 20:
        "public void setScheduler(Scheduler scheduler)"

Subtest 21:
        "public void setSchedulingParameters(SchedulingParameters parameters)"

Subtest NEW:
        "public void setReleaseParametersIfFeasible(ReleaseParameters parameters):

Subtest 22:
        "public final void run()"
*/

import javax.realtime.*;
import com.timesys.*;

public class AsyncEventHandlerTest {

    public static void run() {
        Object o = null;
        PriorityParameters pp = new PriorityParameters(10);
        AperiodicParameters ap = new AperiodicParameters(new
            RelativeTime(1000L,100),new RelativeTime(500L,0),null,null);
        MemoryParameters mp = new MemoryParameters(MemoryParameters.NO_MAX,
                                                   MemoryParameters.NO_MAX);
        ProcessingGroupParameters pgp = new ProcessingGroupParameters(new
            RelativeTime(2000L,0),new RelativeTime(2000L,0),new
                RelativeTime(500L,0),new RelativeTime(100L,0),null,null);

        Tests.newTest("AsyncEventHandlerTest");

        /* NOTE: Because AsyncEventHandler is an abstract class,
           the following tests use the class AEventHandler which
           extends the AsyncEventHandler class.
        */

        /* Subtest 1:
        ** Constructor "public AsyncEventHandler()"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: AsyncEventHandler()");
            o = new AEventHandler();
            if (! (o instanceof AsyncEventHandler))
                throw new Exception("Return object is not instanceof "+
                                    "AsyncEventHandler");
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: AsyncEventHandler() "+
                               "failed");
            Tests.fail("new AEventHandler()",e);
        }

        /* Subtest 2:
        ** Constructor "public AsyncEventHandler(Scheduling Parameters
        ** scheduling, ReleaseParameters release, MemoryParameters memory,
        ** MemoryArea area, ProcessingGroupParameters group)
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: AsyncEventHandler("+
                               "null,null,null,LTMemory,null,null)");
            o = new AEventHandler(null,null,null,
                                  new LTMemory(1048476,1048576),
                                  null,null);
            if (! (o instanceof AsyncEventHandler))
                throw new Exception("Return object is not instanceof "+
                                    "AsyncEventHandler");
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: AsyncEventHandler("+
                               "null,null,null,LTMemory,null,null)");
            Tests.fail("new AEventHandler(null,null,null,"+
                       "new LTMemory(1048476,1048576),null,null)",e);
        }

        /* Subtest 3:
        ** Constructor "public AsyncEventHandler(Scheduling Parameters
        ** scheduling, ReleaseParameters release, MemoryParameters memory,
        ** MemoryArea area, ProcessingGroupParameters group, boolean noheap)"
        ** where all given parameters are null
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: AsyncEventHandler("+
                               "null,null,null,LTMemory,null,true,null)");
            o = new AEventHandler(null,null,null,
                                  new LTMemory(1048476,1048576),
                                  null,true,null);
            if (! (o instanceof AsyncEventHandler))
                throw new Exception("Return object is not instanceof "+
                                    "AsyncEventHandler");
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: AsyncEventHandler("+
                               "null,null,null,LTMemory,null,true,null)"+
                               " failed");
            Tests.fail("new AEventHandler(null,null,null,new "+
                       "LTMemory(1048476,1048576),null,true,null)",e);
        }

        /* Subtest 4:
        ** Constructor "public AsyncEventHandler(Scheduling Parameters
        ** scheduling, ReleaseParameters release, MemoryParameters memory,
        ** MemoryArea area, ProcessingGroupParameters group, boolean noheap)"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: AsyncEventHandler("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryParameters,MemoryArea,ProcessingGroup"+
                               "Parameters, false, Runnable logic)");
            o = new AEventHandler(pp,ap,mp,
                                  new LTMemory(1048476,1048576),
                                  pgp,false,null);
            if (! (o instanceof AsyncEventHandler))
                throw new Exception("Return object is not instanceof "+
                                    "AsyncEventHandler");
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: AsyncEventHandler("+
                               "SchedulingParameters,ReleaseParameters,"+
                               "MemoryParameters,MemoryArea,ProcessingGroup"+
                               "Parameters, false, Runnable) failed");
            Tests.fail("new AEventHandler(null,null,null,new "+
                       "LTMemory(1048476,1048576),null,false,Runnable)",e);
        }

        /* Subtest 5:
        ** Constructor "public AsyncEventHandler(boolean noheap)"
        ** where noheap is true
        ** IllegalArgumentException is expected
        ** Reason is because the current thread is not a
        ** NoHeapRealtimeThread
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: AsyncEventHandler"+
                               "(true)");
            o = new AEventHandler(true);
            Tests.fail("new AEventHandler(true) did not throw expected "+
                       "exception");
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: AsyncEventHandler"+
                               "(true) threw exception");
            if (! (e instanceof IllegalArgumentException))
                Tests.fail("new AEventHandler(true)",e);
            else
                System.out.println("...as expected");
        }

        /* Subtest 6:
        ** Constructor "public AsyncEventHandler(boolean noheap)"
        ** where noheap is false
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: AsyncEventHandler"+
                               "(false)");
            o = new AEventHandler(false);
            if (! (o instanceof AsyncEventHandler))
                throw new Exception("Return object is not instanceof "+
                                    "AsyncEventHandler");
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: AsyncEventHandler"+
                               "(false)");
            Tests.fail("new AEventHandler(false)",e);
        }

        /* Subtest 7:
        ** Method "public void addToFeasibility()"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: addToFeasibility()");
            AEventHandler aeh
                = new AEventHandler(pp,ap,mp,
                                    new LTMemory(1048476,1048576),
                                    pgp,false,null);
            aeh.addToFeasibility();
        } catch(Exception e) {
            System.out.println("AsyncEventHandlerTest: addToFeasibility()"+
                               "failed");
            Tests.fail("aeh.addToFeasibility()",e);
        }

        /* Subtest 8:
        ** Method "protected final synchronized int
        ** getAndIncrementPendingFireCount()"
        ** Method "protected final synchronized int
        ** getAndClearPendingFireCount()"
        ** Method "public abstract void handlerAsyncEvent()"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: getAndIncrementPending"+
                               "FireCount(), getAndClearPendingFireCount(), "+
                               "handleAsyncEvent()");
            AsyncEventHandler h
                = new AsyncEventHandler(null, null,null,
                                        new VTMemory(1048576,1048576),
                                        null,false,null){
                        public void handleAsyncEvent()
                        {
                            System.out.println("**** Incrementing Pending "+
                                               "Fire Count");
                            int fireCount = getAndIncrementPendingFireCount();
                            if (fireCount != 1)
                                Tests.fail("AsyncEventHandlerTest failed - "+
                                           "fireCount is not 1 as expected - "+
                                           "it is " + fireCount + "!");
                            fireCount = getAndClearPendingFireCount();
                            if (fireCount != 2)
                                Tests.fail("AsyncEventHandlerTest failed - "+
                                           "fireCount is not 2 as expected - "+
                                           "it is " + fireCount + "!");
                        }
                    };

            AsyncEvent ae = new AsyncEvent();
            ae.addHandler(h);
            ae.fire();
            ae.setHandler(null);  //Remove handler
        } catch(Exception e) {
            System.out.println("AsyncEventHandlerTest: getAndIncrementPending"+
                               "FireCount(), getAndClearPendingFireCount(), "+
                               "handleAsyncEvent() failed");
            Tests.fail("getAndIncrementPendingFireCount() test thread could "+
                       "not process",e);
        }

        /* Subtest 9:
        ** Method "protected final synchronized int
        ** getAndDecrementPendingFireCount()"
        ** Method "protected final synchronized int
        ** getAndClearPendingFireCount()"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: getAndDecrementPending"+
                               "FireCount(), getAndClearPendingFireCount()");
            AsyncEventHandler h
                = new AsyncEventHandler(null, null,null,
                                        new VTMemory(1048576,1048576),
                                        null,false,null){
                        public void handleAsyncEvent()
                        {
                            System.out.println("**** Decrementing Pending "+
                                               "Fire Count");
                            int fireCount = getAndDecrementPendingFireCount();
                            if (fireCount != 1)
                                Tests.fail("AsyncEventHandlerTest failed - "+
                                           "fireCount is not 1 as expected - "+
                                           "it is " + fireCount + "!");
                            fireCount = getAndIncrementPendingFireCount();
                            if (fireCount != 0)
                                Tests.fail("AsyncEventHandlerTest failed - "+
                                           "fireCount is not 0 as expected - "+
                                           "it is " + fireCount + "!");
                        }
                    };

            AsyncEvent ae = new AsyncEvent();
            ae.addHandler(h);
            ae.fire();
            ae.setHandler(null);  //Remove handler
        } catch(Exception e) {
            System.out.println("AsyncEventHandlerTest: getAndDecrementPending"+
                               "FireCount(), getAndClearPendingFireCount() "+
                               "failed");
            Tests.fail("getAndDecrementPendingFireCount() test thread could "+
                       "not process",e);
        }

        /* Subtest 10:
        ** Method "public MemoryArea getMemoryArea()"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: getMemoryArea()");
            AEventHandler ae
                = new AEventHandler(null,null,null,
                                    new LTMemory(1048476,1048576),
                                    null,null);
            o = ae.getMemoryArea();
            if (! (o instanceof MemoryArea) && (o instanceof ScopedMemory) &&
                (o instanceof LTMemory))
                throw new Exception("Return object is not instanceof "+
                                    "MemoryArea nor ScopedMemory nor "+
                                    "LTMemory");
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: getMemoryArea() "+
                               "failed");
            Tests.fail("ae.getMemoryArea()",e);
        }

        /* Subtest 11:
        ** Method "public MemoryParameters getMemoryParameters()"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: getMemoryParameters()");
            AEventHandler ae
                = new AEventHandler(pp,ap,mp,
                                    new LTMemory(1048476,1048576),
                                    pgp,false,null);
            o = ae.getMemoryParameters();
            if (! (o instanceof MemoryParameters))
                throw new Exception("Return object is not instanceof "+
                                    "MemoryParameters");
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: getMemoryParameters() "+
                               "failed");
            Tests.fail("ae.getMemoryParameters()",e);
        }

        /* Subtest NEW:
        ** Method "protected int getPendingFireCount()"
        */
        Tests.increment();
        try {
            int firecount;
            System.out.println("AsyncEventHandlerTest: getPendingFireCount()");
            AEventHandler ae
                = new AEventHandler(pp,ap,mp,
                                    new LTMemory(1048476,1048576),
                                    pgp,false,null);
            // the class version is protected
            // the test class exposes it indirectly
            // via the method called below
            firecount = ae.getPendingFireCount_A();
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: getPendingFireCount() "+
                               "failed");
            Tests.fail("ae.getPendingFireCount()",e);
        }

        /* Subtest 12:
        ** Method "public ProcessingGroupParameters
        ** getProcessingGroupParameters()
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: "+
                               "getProcessingGroupParameters()");
            AEventHandler ae
                = new AEventHandler(pp,ap,mp,
                                    new LTMemory(1048476,1048576),
                                    pgp,false,null);
            o = ae.getProcessingGroupParameters();
            if (!(o instanceof ProcessingGroupParameters))
                throw new Exception("Return object is not instanceof "+
                                    "ProcessingGroupParameters");
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: "+
                               "getProcessingGroupParameters() failed");
            Tests.fail("ae.getProcessingGroupParameters()",e);
        }

        /* Subtest 13:
        ** Method "public ReleaseParameters getReleaseParameters()"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: "+
                               "getReleaseParameters()");
            AEventHandler ae
                = new AEventHandler(pp,ap,mp,
                                    new LTMemory(1048476,1048576),
                                    pgp,false,null);
            o = ae.getReleaseParameters();
            if (!(o instanceof ReleaseParameters))
                throw new Exception("Return object is not instancof "+
                                    "ReleaseParameters");
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: "+
                               "getReleaseParameters() failed");
            Tests.fail("ae.getReleaseParameters()",e);
        }

        /* Subtest 14:
        ** Method "public Scheduler getScheduler()"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: getScheduler()");
            AEventHandler ae
                = new AEventHandler(pp,ap,mp,
                                    new LTMemory(1048476,1048576),
                                    pgp,false,null);
            o = ae.getScheduler();
            if (!(o instanceof Scheduler))
                throw new Exception("Return object is not instanceof "+
                                    "Scheduler");
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: getScheduler() failed");
            Tests.fail("ae.getScheduler()",e);
        }

        /* Subtest 15:
        ** Method "public SchedulingParameters getSchedulingParameters()"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: getScheduling"+
                               "Parameters()");
            AEventHandler ae
                = new AEventHandler(pp,ap,mp,
                                    new LTMemory(1048476,1048576),
                                    pgp,false,null);
            o = ae.getSchedulingParameters();
            if (!(o instanceof SchedulingParameters))
                throw new Exception("Return object is not instance of "+
                                    "SchedulingParameters");
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: getScheduling"+
                               "Parameters() failed");
            Tests.fail("ae.getSchedulingParameters()",e);
        }

        /* Subtest 16:
        ** Method "public void removeFromFeasibility()"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: removeFrom"+
                               "Feasibility()");
            AEventHandler ae
                = new AEventHandler(pp,ap,mp,
                                    new LTMemory(1048476,1048576),
                                    pgp,false,null);
            ae.addToFeasibility();
            ae.removeFromFeasibility();
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: removeFrom"+
                               "Feasibility() failed");
            Tests.fail("ae.removeFromFeasibility()",e);
        }

        /* Subtest 17:
        ** Method "public void setMemoryParameters(MemoryParameters memory)"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: setMemoryParameters("+
                               "MemoryParameters)");
            AEventHandler ae  = new AEventHandler();
            ae.setMemoryParameters(mp);
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: setMemoryParameters("+
                               "MemoryParameters) failed");
            Tests.fail("ae.setMemoryParameters(mp)",e);
        }

        /* Subtest NEW:
        ** Method "public void setMemoryParametersIfFeasible(MemoryParameters memory)"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: setMemoryParametersIfFeasible("+
                               "MemoryParameters)");
            AEventHandler ae  = new AEventHandler();
            ae.setMemoryParametersIfFeasible(mp);
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: setMemoryParametersIfFeasible("+
                               "MemoryParameters) failed");
            Tests.fail("ae.setMemoryParametersIfFeasible(mp)",e);
        }

        /* Subtest 18:
        ** Method "public void setProcessingGroupParameters(PGP)"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: setProcessingGroup"+
                               "Parameters(ProcessingGroupParameters)");
            AEventHandler ae = new AEventHandler();
            ae.setProcessingGroupParameters(pgp);
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: setProcessingGroup"+
                               "Parameters(ProcessingGroupParameters) failed");
            Tests.fail("ae.setProcessingParameters(pgp)",e);
        }

        /* Subtest NEW:
        ** Method "public void setProcessingGroupParametersIfFeasible(PGP)"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: setProcessingGroup"+
                               "ParametersIfFeasible(ProcessingGroupParameters)");
            AEventHandler ae = new AEventHandler();
            ae.setProcessingGroupParametersIfFeasible(pgp);
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: setProcessingGroup"+
                               "ParametersIfFeasible(ProcessingGroupParameters) failed");
            Tests.fail("ae.setProcessingParametersIfFeasible(pgp)",e);
        }

        /* Subtest 19:
        ** Method "public void setReleaseParameters(ReleaseParameters
        ** parameters)
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: setReleaseParameters("+
                               "ReleaseParameters)");
            AEventHandler ae = new AEventHandler();
            ae.setReleaseParameters(ap);
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: setReleaseParameters("+
                               "ReleaseParameters) failed");
            Tests.fail("ae.setReleaseParameters(ap)",e);
        }

        /* Subtest NEW:
        ** Method "public void setReleaseParametersIfFeasible
        ** (ReleaseParameters parameters)
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: setReleaseParametersIfFeasible("+
                               "ReleaseParameters)");
            AEventHandler ae = new AEventHandler();
            ae.setReleaseParametersIfFeasible(ap);
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: setReleaseParametersIfFeasible("+
                               "ReleaseParameters) failed");
            Tests.fail("ae.setReleaseParametersIfFeasible(ap)",e);
        }

        /* Subtest 20:
        ** Method: "public void setScheduler(Scheduler scheduler)"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: setScheduler("+
                               "Scheduler)");
            AEventHandler ae = new AEventHandler();
            PriorityScheduler ps = PriorityScheduler.instance();
            ae.setScheduler(ps);
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: setScheduler("+
                               "Scheduler) failed");
            Tests.fail("ae.setScheduler(ps)",e);
        }

        /* Subtest 21:
        ** Method: "public void setSchedulingParameters(SchedulingParameters
        ** parameters)"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: setScheduling"+
                               "Parameters(SchedulingParameters)");
            AEventHandler ae = new AEventHandler();
            ae.setSchedulingParameters(pp);
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: setScheduling"+
                               "Parameters(SchedulingParameters) failed");
            Tests.fail("ae.setSchedulingParameters(pp)",e);
        }

        /* Subtest NEW:
        ** Method: "public void setSchedulingParametersIfFeasible(SchedulingParameters
        ** parameters)"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: setScheduling"+
                               "ParametersIfFeasible(SchedulingParameters)");
            AEventHandler ae = new AEventHandler();
            ae.setSchedulingParametersIfFeasible(pp);
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: setScheduling"+
                               "ParametersIfFeasible(SchedulingParameters) failed");
            Tests.fail("ae.setSchedulingParametersIfFeasible(pp)",e);
        }

        /* Subtest 22:
        ** Method: "public final void run()"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventHandlerTest: run()");
            AEventHandler aeh = new AEventHandler();
            aeh.run();
        } catch (Exception e) {
            System.out.println("AsyncEventHandlerTest: run() failed");
            Tests.fail("aeh.run()",e);
        }
        Tests.printSubTestReportTotals("AsyncEventHandlerTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
