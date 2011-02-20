//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              RealtimeThreadTest

Subtest 1:
        "public RealtimeThread(SchedulingParameters scheduling,
        ReleaseParameters release, MemoryParameters memory, MemoryArea area,
        ProcessingGroupParameters group, java.lang.Runnable logic)"

Subtest 2:
        "public RealtimeThread(SchedulingParameters scheduling,
        ReleaseParameters release)"

Subtest 3:
        "public RealtimeThread(SchedulingParameters scheduling)"

Subtest 4:
        "public RealtimeThread()"

Subtest 5:
        "public boolean addIfFeasible()"

Subtest 6:
        "public void addToFeasibility()"

Subtest 7:
        "public void setMemoryParametersIfFeasible(MemoryParameters memory)"

Subtest 8:
        "public void setReleaseParametersIfFeasible(ReleaseParameters memory)"

Subtest 9:
        "public void setIfFeasible()"

Subtest 10:
        "public static RealtimeThread currentRealtimeThread()"
** CastClassException is expected (since the currently running thread is not a
RealtimeThread)"

Subtest 11:
        "public static RealtimeThread currentRealtimeThread()"

Subtest 12:
        "public synchronized void deschedulePeriodic()"

Subtest 13:
        "public static MemoryArea getCurrentMemoryArea()"

Subtest 14:
        "public static MemoryArea getInitialMemoryAreaIndex()"

Subtest 15:
        "public MemoryArea getMemoryArea()"

Subtest 16:
        "public static MemoryArea getMemoryAreaStackDepth()"

Subtest 17:
        "public MemoryParameters getMemoryParameters()"

Subtest 18:
        "public static MemoryArea getOuterMemoryArea()"

Subtest 19:
        "public ProcessingGroupParameters getProcessingGroupParameters()"

Subtest 20:
        "public ReleaseParameters getReleaseParameters()"

Subtest 21:
        "public SchedulingParameters getSchedulingParameters()

Subtest 22:
        "public synchronized void interrupt()"

Subtest 23:
        "public void removeFromFeasibility()"

Subtest 24:
        "public void setMemoryParameters(MemoryParameters parameters)"

Subtest 25:
        "public void setProcessingGroupParameters(ProcessingGroupParameters
        parameters)"

Subtest 26:
        "public void setReleaseParameters(ReleaseParameters parameters)"

Subtest 27:
        "public void setScheduler(Scheduler scheduler)"

Subtest 28:
        "public void setSchedulingParameters(ScheduleringParameters
        scheduling)"

Subtest 29:
        "public static void sleep(Clock clock, HighResolutionTime time)

Subtest 30:
        "public static void sleep(HighResolutionTime time)"

Subtest 31:
        "public boolean waitForNextPeriod()"

Subtest 32:
        "public boolean waitForNextPeriod() where thread
*/

import javax.realtime.*;

public class RealtimeThreadTest {

    public static boolean tmp_test_status;

    public static void run()
    {
        RealtimeThread rtt = null;
        Object o = null;
        final PriorityParameters pp = new PriorityParameters(10);
        final AperiodicParameters ap = new AperiodicParameters(new
            RelativeTime(1000L,100),new RelativeTime(500L,0), null, null);
        final PeriodicParameters perp = new PeriodicParameters(new
            RelativeTime(1024L,100),new RelativeTime(1000L,0),new
                RelativeTime(900L,0),new RelativeTime(1000L,0),null,null);
        final MemoryParameters mp = new MemoryParameters(MemoryParameters.
                                              NO_MAX,MemoryParameters.NO_MAX);
        final LTMemory ltm = new LTMemory(1048576,1048576);
        final ProcessingGroupParameters pgp = new ProcessingGroupParameters(new
            RelativeTime(2000L,0),new RelativeTime(2000L,0),new
                RelativeTime(500L,0),new RelativeTime(100L,0), null,null);

        Thread myThread = new Thread(){
                public void run() {
                    System.out.println("Inside of RealtimeThread");
                }
            };


        Tests.newTest("RealtimeThreadTest");

        /* Subtest 1:
        ** Constructor "public RealtimeThread(SchedulingParameters scheduling,
        ** ReleaseParameters release, MemoryParameters memory, MemoryArea area,
        ** ProcessingGroupParameters group, java.lang.Runnable logic)"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: RealtimeThread(Scheduling"+
                               "Parameters,ReleaseParameters,Memory"+
                               "Parameters,MemoryArea,ProcessingGroup"+
                               "Parameters,Runnable)");
            o = new RealtimeThread(pp, ap, mp, ltm, pgp, myThread);
            if( !(o instanceof RealtimeThread && o instanceof Thread) )
                throw new Exception("Return object is not instanceof "+
                                    "RealtimeThread nor Thread");
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: RealtimeThread(Scheduling"+
                               "Parameters,ReleaseParameters,Memory"+
                               "Parameters,MemoryArea,ProcessingGroup"+
                               "Parameters,Runnable) failed");
            Tests.fail("new RealtimeThread(pp,ap,mp,ltm,pgp,myThread)",e);
        }
        /* Subtest 2:
        ** Constructor "public RealtimeThread(SchedulingParameters scheduling,
        ** ReleaseParameters release)"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: RealtimeThread(Scheduling"+
                               "Parameters,ReleaseParameters)");
            o = new RealtimeThread(pp, ap);
            if( !(o instanceof RealtimeThread && o instanceof Thread) )
                throw new Exception("Return object is not instanceof "+
                                    "RealtimeThread nor Thread");
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: RealtimeThread(Scheduling"+
                               "Parameters,ReleaseParameters) failed");
            Tests.fail("new RealtimeThread(pp,ap)",e);
        }

        /* Subtest 3:
        ** Constructor "public RealtimeThread(SchedulingParameters scheduling)"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: RealtimeThread(Scheduling"+
                               "Parameters)");
            o = new RealtimeThread(pp);
            if( !(o instanceof RealtimeThread && o instanceof Thread) )
                throw new Exception("Return object is not instaneof "+
                                    "RealtimeThread nor Thread");
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: RealtimeThread(Scheduling"+
                               "Parameters) failed");
            Tests.fail("new RealtimeThread(pp)",e);
        }

        /* Subtest 4:
        ** Constructor "public RealtimeThread()"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: RealtimeThread()");
            o = new RealtimeThread();
            if( !(o instanceof RealtimeThread && o instanceof Thread) )
                throw new Exception("Return object is not instanceof "+
                                    "RealtimeThread nor Thread");
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: RealtimeThread() failed");
            Tests.fail("new RealtimeThread()",e);
        }

        rtt = new RealtimeThread(pp, ap, mp, ltm, pgp, myThread);

        /* Subtest 5:
        ** Method "public void addIfFeasible()"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: addIfFeasible()");
            boolean b = rtt.addIfFeasible();
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: addIfFeasible() "+
                               "failed");
            Tests.fail("rtt.addIfFeasible()",e);
        }

        /* Subtest 6:
        ** Method "public void addToFeasibility()"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: addToFeasibility()");
            rtt.addToFeasibility();
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: addToFeasibility() "+
                               "failed");
            Tests.fail("rtt.addToFeasibility()",e);
        }

        /* Subtest 7:
        ** Method "public void setMemoryparametersIfFeasible
        **                               (MemoryParameters memory)"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: "+
                               "setMemoryParametersIfFeasible(MP)");
            boolean b = rtt.setMemoryParametersIfFeasible(mp);
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: "+
                               "setMemoryParametersIfFeasible(MP) "+
                               "failed");
            Tests.fail("rtt.setMemoryParametersIfFeasible(MP)",e);
        }

        /* Subtest 8:
        ** Method "public void setReleaseParametersIfFeasible(ReleaseParameters release)"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: "+
                               "setReleaseParametersIfFeasible(RP)");
            boolean b = rtt.setReleaseParametersIfFeasible(ap);
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: "+
                               "setReleaseParametersIfFeasible(RP) "+
                               "failed");
            Tests.fail("rtt.setReleaseParametersIfFeasible(RP)",e);
        }

        /* Subtest 9:
        ** Method "public void setIfFeasible(ReleaseParameters release,
        **                                      MemoryParameters memory)"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: setIfFeasible(RP,MP)");
            boolean b = rtt.setIfFeasible(ap,mp);
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: setIfFeasible(RP,MP) "+
                               "failed");
            Tests.fail("rtt.setIfFeasible(RP,MP)",e);
        }

        /* Subtest 10:
        ** Method "public static RealtimeThread currentRealtimeThread()"
        ** CastClassException is expected (since the currently running thread
        ** is not a RealtimeThread)"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: currentRealtimeThread()");
            o = RealtimeThread.currentRealtimeThread();
            Tests.fail("RealtimeThread.currentRealtimeThread() did not throw "+
                       "expected exception");
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: currentRealtimeThread() "+
                               "threw exception");
            if (! (e instanceof ClassCastException))
                Tests.fail("RealtimeThread.currentRealtimeThread()",e);
            else
                System.out.println("...as expected.");
        }

        /* Subtest 11:
        ** Method "public static RealtimeThread currentRealtimeThread()"
        */

        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: currentRealtimeThread()");
            final RealtimeThread curThread = new RealtimeThread(){
                    public void run() {
                        Object o1;
                        System.out.println("RealtimeThreadTest: Inside "+
                                           "curThread");
                        try {
                            o1 = RealtimeThread.currentRealtimeThread();
                            if ( !(o1 instanceof RealtimeThread))
                                throw new Exception("Return object is not "+
                                                  "instanceof RealtimeThread");
                        } catch (Exception e) {
                            Tests.fail("RealtimeThread.currentRealtime"+
                                       "Thread() from inside curThread",e);
                        }
                    }
                };
            curThread.start();
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: currentRealtimeThread() "+
                               "failed");
            Tests.fail("RealtimeThread.currentRealtimeThread()",e);
        }

        /* Subtest 12:
        ** Method "public synchronized void deschedulePeriodic()"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: deschedulePeriodic()");
            rtt.deschedulePeriodic();
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: deschedulePeriodic() "+
                               "failed");
            Tests.fail("rtt.deschedulePeriodic",e);
        }

        /* Subtest 13:
        ** Method "public static MemoryArea getCurrentMemoryArea()"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: getCurrentMemoryArea()");
            tmp_test_status = true;
            RealtimeThread rt =
                new RealtimeThread()
                    {
                        public void run()
                        {
                            Object o1 = RealtimeThread.getCurrentMemoryArea();
                            if( !(o1 instanceof MemoryArea) )
                                tmp_test_status = false;
                        }
                    };
            rt.start();
            rt.join();
            if (!tmp_test_status) {
                throw new Exception("Return object is not instanceof "+
                                    "MemoryArea");
            }
            else {
                System.out.println("It worked.");
            }
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: getCurrentMemoryArea() failed");
            Tests.fail("rtt.getCurrentMemoryArea()",e);
        }

        /* Subtest 14:
        ** Method "public MemoryArea getInitialMemoryAreaIndex()"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: getInitialMemoryAreaIndex()");
            long areaindex = RealtimeThread.getInitialMemoryAreaIndex();
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: getInitialMemoryAreaIndex() failed");
            Tests.fail("rtt.getInitialMemoryAreaIndex()",e);
        }

        /* Subtest 15:
        ** Method "public MemoryArea getMemoryArea()"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: getMemoryArea()");
            o = rtt.getMemoryArea();
            if( !(o instanceof MemoryArea) )
                throw new Exception("Return object is not instanceof "+
                                    "MemoryArea");
            if(! (ltm.equals(o)))
                throw new Exception("Unexpected memory area received");
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: getMemoryArea() failed");
            Tests.fail("rtt.getMemoryArea()",e);
        }

        /* Subtest 16:
        ** Method "public MemoryArea getMemoryAreaStackDepth()"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: getMemoryAreaStackDepth()");
            long stacksize = RealtimeThread.getMemoryAreaStackDepth();
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: getMemoryAreaStackDepth() failed");
            Tests.fail("rtt.getMemoryAreaStackDepth()",e);
        }

        /* Subtest 17:
        ** Method "public MemoryParameters getMemoryParameters()"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: getMemoryParameters()");
            o = rtt.getMemoryParameters();
            if( !(o instanceof MemoryParameters) )
                throw new Exception("Return object is not instanceof "+
                                    "MemoryParameters");
            if (! (mp.equals(o)))
                throw new Exception("Unexpected memory parameters received");
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: getMemoryParameters() "+
                               "failed");
            Tests.fail("rtt.getMemoryParameters",e);
        }

        /* Subtest 18:
        ** Method "public MemoryArea getOuterMemoryArea(int)"
        */
        Tests.increment();
        try {
            tmp_test_status = true;
            RealtimeThread rt =
                new RealtimeThread()
                    {
                        public void run()
                        {
                            Object o1 = RealtimeThread.getOuterMemoryArea(0);
                            if( !(o1 instanceof MemoryArea) )
                                tmp_test_status = false;
                        }
                    };
            rt.start();
            rt.join();
            if (!tmp_test_status) {
                throw new Exception("Return object is not instanceof "+
                                    "MemoryArea");
            }
            else {
                System.out.println("It worked.");
            }
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: getOuterMemoryArea() failed");
            Tests.fail("rtt.getOuterMemoryArea(int)",e);
        }

        /* Subtest 19:
        ** Method "public ProcessingGroupParameters getProcessingGroupParameters()"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: getProcessingGroup"+
                               "Parameters()");
            o = rtt.getProcessingGroupParameters();
            if( !(o instanceof ProcessingGroupParameters) )
                throw new Exception("Return object is not instanceof "+
                                    "ProcessingGroupParameters");
            if (! (pgp.equals(o)))
                throw new Exception("Unexpected processing group parameters "+
                                    "received");
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: getProcessingGroup"+
                               "Parameters() failed");
            Tests.fail("rtt.getProcessingGroupParameters",e);
        }

        /* Subtest 20:
        ** Method "public ReleaseParameters getReleaseParameters()"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: getReleaseParameters()");
            o = rtt.getReleaseParameters();
            if( !(o instanceof ReleaseParameters) )
                throw new Exception("Return object is not instanceof Release"+
                                    "Parameters");
            if( ! (ap.equals(o)))
                Tests.fail("rtt.getReleaseParameters()");
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: getReleaseParameters() "+
                               "failed");
            Tests.fail("rtt.getReleaseParameters()",e);
        }

        /* Subtest 21:
        ** Method "public SchedulingParameters getSchedulingParameters()
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: getScheduling"+
                               "Parameters()");
            o = rtt.getSchedulingParameters();
            if( !(o instanceof SchedulingParameters) )
                throw new Exception("Return object is not instanceof "+
                                    "SchedulingParameters");
            if (! (pp.equals(o)))
                Tests.fail("rtt.getSchedulingParameters()");
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: getScheduling"+
                               "Parameters() failed");
            Tests.fail("rtt.getSchedulingParameters()",e);
        }

        /* Subtest 22:
        ** Method "public synchronized void interrupt()"
        */

        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: interrupt()");
            Semaphore semaphore = new Semaphore();
            //                  MyThread aThread = new MyThread(semaphore);
            MyRTThread aThread = new MyRTThread(semaphore);
            if (aThread.isInterrupted() == true)
                throw new Exception("isInterrupted returned true");
            aThread.start();
            semaphore.sWait();
            aThread.interrupt();
            if (aThread.wasInterrupted() == false) {
                throw new Exception("Realtime Thread was not interrupted");
            }
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: interrupt() failed");
            Tests.fail("rtt.interrupt()",e);
        }


        /* Subtest 23:
        ** Method "public void removeFromFeasibility()"
        */

        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: removeFromFeasibility()");
            rtt.removeFromFeasibility();
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: removeFromFeasibility() "+
                               "failed");
            Tests.fail("rtt.removeFromFeasibility",e);
        }

        /* Subtest 24:
        ** Method "public void setMemoryParameters(MemoryParameters
        ** parameters)"
        */

        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: setMemoryParameters("+
                               "MemoryParameters)");
            MemoryParameters newmp = new MemoryParameters(100000L,100000L);
            rtt.setMemoryParameters(newmp);
            o = rtt.getMemoryParameters();
            if (! (newmp.equals(o)))
                throw new Exception("Memory Parameters not set properly");
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: setMemoryParameters("+
                               "MemoryParameters) failed");
            Tests.fail("rtt.setMemoryParameters(newmp)",e);
        }

        /* Subtest 25:
        ** Method "public void setProcessingGroupParameters(ProcessingGroupParameters parameters)"
        */

        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: setProcessingGroup"+
                               "Parameters(ProcessingGroupParameters)");
            ProcessingGroupParameters newpgp = new
                ProcessingGroupParameters(new RelativeTime(3000L,0),new
                    RelativeTime(3000L,0),new RelativeTime(1000L,0),new
                        RelativeTime(500L,0), null,null);
            rtt.setProcessingGroupParameters(newpgp);
            o = rtt.getProcessingGroupParameters();
            if (!(newpgp.equals(o)))
                throw new Exception("Processing Group Parameters not set "+
                                    "properly");
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest:  setProcessingGroup"+
                               "Parameters(ProcessingGroupParameters) failed");
            Tests.fail("rtt.setProcessingGroupParameters(newpgp)",e);
            System.out.println("RealtimeThreadTest: ");
        }

        /* Subtest 26:
        ** Method "public void setReleaseParameters(ReleaseParameters
        ** parameters)"
        */
        Tests.increment();

        try {
            System.out.println("RealtimeThreadTest: setReleaseParameters("+
                               "ReleaseParameters)");
            AperiodicParameters newap = new AperiodicParameters(new
                RelativeTime(3000L,0),new RelativeTime(1000L,0), null, null);
            rtt.setReleaseParameters(newap);
            o = rtt.getReleaseParameters();
            if (!(newap.equals(o)))
                throw new Exception("Release Parameters not set properly");
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: setReleaseParameters("+
                               "ReleaseParameters) failed");
            Tests.fail("rtt.setReleaseParameters(newap)",e);
        }

        /* Subtest 27:
        ** Method "public void setScheduler(Scheduler scheduler)"
        */

        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: setScheduler(Scheduler)");
            PriorityScheduler ps = PriorityScheduler.instance();
            rtt.setScheduler(ps);
            o = rtt.getScheduler();
            if (! (ps.equals(o)))
                throw new Exception("Scheduler not set properly");
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: setScheduler(Scheduler) "+
                               "failed");
            Tests.fail("rtt.setScheduler()",e);
        }

        /* Subtest 28:
        ** Method "public void setSchedulingParameters(Schedulering
        ** Parameters scheduling)"
        */

        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: setSchedulingParameters("+
                               "SchedulingParameters)");
            PriorityParameters newpp = new PriorityParameters(100);
            rtt.setSchedulingParameters(newpp);
            o = rtt.getSchedulingParameters();
            if (!(newpp.equals(o)))
                throw new Exception("Scheduling Parameters not set properly");
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: setSchedulingParameters("+
                               "SchedulingParameters) failed");
            Tests.fail("rtt.setSchedulingParameters(newpp)",e);
        }

        /* Subtest 29:
        ** Method "public static void sleep(Clock clock, HighResolutionTime
        ** time)
        */

        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: sleep(Clock,High"+
                               "ResolutionTime)");
            RealtimeThread aThread = new RealtimeThread(){
                    public void run() {
                        try{
                            System.out.println("aThread is sleeping");
                            RealtimeThread.sleep(Clock.getRealtimeClock(),new
                                RelativeTime(100L,0));
                            System.out.println("aThread woke up");
                        } catch (Exception e) {
                            Tests.fail("RealtimeThread.sleep()",e);
                        }
                    }
                };
            aThread.start();
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest:leep(Clock,High"+
                               "ResolutionTime) failed ");
            Tests.fail("RealtimeThread.sleep(Clock.getRealtimeClock(), new "+
                       "RelativeTime(100L,0))",e);
        }

        /* Subtest 30:
        ** Method "public static void sleep(HighResolutionTime time)"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: sleep(HighResolution"+
                               "Time)");
            RealtimeThread aThread = new RealtimeThread(){
                    public void run() {
                        try{
                            System.out.println("aThread2 is sleeping");
                            RealtimeThread.sleep(new RelativeTime(100L,0));
                            System.out.println("aThread2 woke up");
                        } catch (Exception e) {
                            Tests.fail("RealtimeThread.sleep()",e);
                        }
                    }
                };
            aThread.start();
        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: sleep(HighResolution"+
                               "Time) failed");
            Tests.fail("RealtimeThread.sleep(new RelativeTime(100L,0))",e);
        }

        /* Subtest 31:
        ** Method "public boolean waitForNextPeriod()"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: waitForNextPeriod()");
            final AbsoluteTime start = new AbsoluteTime(System.
                                                        currentTimeMillis(),0);
            final RelativeTime period = new RelativeTime(1000L,0);
            final RelativeTime cost = new RelativeTime(50L,0);
            final RelativeTime deadline = new RelativeTime(1000L,0);
            PriorityParameters pri_param = new PriorityParameters(10);
            PeriodicParameters period_param = new
                PeriodicParameters(start,period,cost,deadline,null,null);
            RealtimeThread aThread = new
                RealtimeThread(pri_param,period_param){
                    public void run() {
                        int i;
                        try {
                            for (i=0;i<3;i++)
                                {
                                    System.out.println("RealtimeThreadTest: "+
                                                       "Inside aThread for "+
                                                       "waitForNextPeriod() "+
                                                       "test");
                                    System.out.println("**** Loop# " + (i+1));
                                    waitForNextPeriod();
                                }
                            System.out.println("RealtimeThreadTest: aThread "+
                                               "for waitForNextPeriod() is "+
                                               "done");
                        } catch (Exception e) {
                            Tests.threadFail("RealtimeThreadTest: waitFor"+
                                             "NextPeriod() in Subtest 23 "+
                                             "of the RealtimeThreadTest "+
                                             "threw an exception");
                            e.printStackTrace();
                        }
                    }
                };
            aThread.start();

        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: waitForNextPeriod() "+
                               "failed");
            Tests.fail("aThread.waitForNextPeriod()",e);
        }

        /* Subtest 32:
        ** Method "public boolean waitForNextPeriod() where thread
        is not have a reference to a ReleaseParameters type of
        PeriodicParameters"
        */
        Tests.increment();
        try {
            System.out.println("RealtimeThreadTest: waitForNextPeriod()");
            final AbsoluteTime start = new AbsoluteTime(System.

currentTimeMillis(),0);
            final RelativeTime period = new RelativeTime(1000L,0);
            final RelativeTime cost = new RelativeTime(50L,0);
            final RelativeTime deadline = new RelativeTime(1000L,0);
            PriorityParameters pri_param = new PriorityParameters(10);
            AperiodicParameters aperiod_param = new AperiodicParameters(cost,
                                                     deadline,null,null);
            RealtimeThread aThread = new RealtimeThread(pri_param,
                                                        aperiod_param){
                    public void run() {
                        int i;
                        try {
                            for (i=0;i<3;i++)
                                {
                                    System.out.println("RealtimeThreadTest: "+
                                                       "Inside aThread2 for "+
                                                       "waitForNextPeriod() "+
                                                       "test");
                                    System.out.println("**** Loop# " + (i+1));
                                    waitForNextPeriod();
                                }
                            System.out.println("RealtimeThreadTest: aThread2 "+
                                               "for waitForNextPeriod() is "+
                                               "done");
                            Tests.threadFail("RealtimeThreadTest: waitFor"+
                                             "NextPeriod() in Subtest 24 "+
                                             "of the RealtimeThreadTest "+
                                             "did not throw expected "+
                                             "exception");
                        } catch (Exception e) {
                            if (! (e instanceof IllegalThreadStateException)) {
                                Tests.threadFail("RealtimeThreadTest: wait"+
                                                 "ForNextPeriod() in "+
                                                 "Subtest 24 of the Realtime"+
                                                 "ThreadTest threw an "+
                                                 "exception other than the "+
                                                 "expected IllegallThread"+
                                                 "StateException");
                                e.printStackTrace();
                            }
                        }
                    }
                };
            aThread.start();

        } catch (Exception e) {
            System.out.println("RealtimeThreadTest: waitForNextPeriod() "+
                               "failed");
            Tests.fail("aThread.waitForNextPeriod()",e);
        }

        Tests.printSubTestReportTotals("RealtimeThreadTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
