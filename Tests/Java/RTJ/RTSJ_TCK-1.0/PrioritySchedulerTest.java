//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              PrioritySchedulerTest

Subtest 1:
        "public PriorityScheduler()"

Subtest 2:
        "protected void addToFeasibility(Schedulable s)"
        NOTE: Currently implemented as public

Subtest 3:
        "public boolean setIfFeasible(Schedulable schedulable,
        ReleaseParameters release, MemoryParameters memory)"

Subtest 4:
        "public void fireSchedulable(Schedulable schedulable)"

Subtest 5:
        "public int getMaxPriority()"

Subtest 6:
        "public static int getMaxPriority(java.lang.Thread thread)"
        ** NOTE: The current implementation declares this as NON-static

Subtest 7:
        "public static int getMaxPriority(java.lang.Thread thread)" where
        thread is not scheduled by the required PriorityScheduler

Subtest 8:
        "public int getMinPriority()"

Subtest 9:
        "public static int getMinPriority(java.lang.Thread thread)"
        ** NOTE: The current implementation declares this as NON-static

Subtest 10:
        "public static int getMinPriority(java.lang.Thread thread)" where
        thread is not scheduled by the required PriorityScheduler

Subtest 11:
        "public int getNormPriority()"

Subtest 12:
        "public static int getNormPriority(java.lang.Thread thread)"
        ** NOTE: The current implementation declares this as NON-static

Subtest 13:
        "public static int getNormPriority(java.lang.Thread thread)" where
        thread is not scheduled by the required PriorityScheduler

Subtest 14:
        "public java.lang.String getPolicyName()"

Subtest 15:
        "public static PriorityScheduler instance()"

Subtest 16:
        "public boolean isFeasible()"

Subtest 17:
        "protected void removeFromFeasibility(Schedulable s)"
        ** NOTES: Currently implemented as public

Subtest 18:
        "protected void removeFromFeasibility(Schedulable s)" where s is not
        scheduled by this scheduler
        ** NOTES: Currently implemented as public

** Scheduler Tests **
Subtest 19:
        "public static Scheduler getDefaultScheduler()"

Subtest 20:
        "public static void setDefaultScheduler(Scheduler scheduler)
*/

import javax.realtime.*;
import java.lang.reflect.*;

public class PrioritySchedulerTest
{

    static class PriorityScheduler_SUB extends PriorityScheduler
    {
        public PriorityScheduler_SUB()
        {
            super();
        }

        public boolean addToFeasibility(Schedulable s)
        {
            return super.addToFeasibility(s);
        }

        public boolean removeFromFeasibility(Schedulable s)
        {
            return super.removeFromFeasibility(s);
        }
    }

    public static void run() {
        PriorityScheduler_SUB ps = null;
        Object o = null;
        Tests.newTest("PrioritySchedulerTest");
        PriorityParameters pp = new PriorityParameters(11);
        AperiodicParameters ap = new AperiodicParameters(new
            RelativeTime(1000L,100),new RelativeTime(500L,0),null,null);
        MemoryParameters mp = new MemoryParameters(MemoryParameters.NO_MAX,
                                                   MemoryParameters.NO_MAX);
        ProcessingGroupParameters pgp = new ProcessingGroupParameters(new
            RelativeTime(2000L,0),new RelativeTime(2000L,0),new
                RelativeTime(500L,0),new RelativeTime(100L,0),null,null);
        RTThread rtt = new RTThread(pp,ap);

        /* Subtest 1:
        ** Constructor "public PriorityScheduler()"
        ** NOTE 10/24/01 constructor was protected;
        ** change call to instance() so tests can proceed.
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: PriorityScheduler()");
            ps = new PriorityScheduler_SUB();
            if( !(ps instanceof PriorityScheduler && ps instanceof Scheduler) )
                throw new Exception("Return object is not instaneof "+
                                    "PriorityScheduler nor Scheduler");
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: PriorityScheduler() "+
                               "failed");
            Tests.fail("new PriorityScheduler()",e);
        }

        /* Subtest 2:
        ** Method "protected void addToFeasibility()"
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: addToFeasibility()");
            AEventHandler aeh = new AEventHandler();
            //ps.addToFeasibility(aeh);
            // NOTE: this is protected now
            aeh.addToFeasibility();
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: addToFeasibility()"+
                               "failed");
            Tests.fail("ps.addToFeasibility(aeh)",e);
        }

        /* Subtest 3:
        ** Method "public boolean setIfFeasible(Schedulable schedulable,
        ** ReleaseParameters release, MemoryParameters memory)"
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: setIfFeasible("+
                               "Schedulable,ReleaseParameters,"+
                               "MemoryParameters)");
            AperiodicParameters release = new AperiodicParameters(new
                RelativeTime(1000L,100),new RelativeTime(3000L,0),new
                    AEventHandler(), new AEventHandler());
            MemoryParameters memory = new MemoryParameters(60000L,60000L);
            AEventHandler aeh = new AEventHandler();
            boolean b = ps.setIfFeasible(aeh, release, memory);
            //boolean b = ps.setIfFeasible(rtt, release, memory);
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: setIfFeasible("+
                               "Schedulable,ReleaseParameters,"+
                               "MemoryParameters) failed");
            Tests.fail("ps.setIfFeasible(aeh,release,memory)",e);
        }

        /* Subtest 4:
        ** Method "public void fireSchedulable(Schedulable schedulable)"
        */

        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: fireSchedulable("+
                               "Schedulable)");
            AsyncEventHandler aeh = new AEventHandler();
            ps.fireSchedulable(aeh);
            //                  ps.fireSchedulable(rtt);
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: fireSchedulable("+
                               "Schedulable) failed");
            Tests.fail("ps.fireSchedulable(aeh)",e);
        }

        /* Subtest 5:
        ** Method "public int getMaxPriority()"
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: getMaxPriority()");
            int x = ps.getMaxPriority();
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: getMaxPriority() "+
                               "failed");
            Tests.fail("ps.getMaxPriority()",e);
        }

        /* Subtest 6:
        ** Method "public static int getMaxPriority(java.lang.Thread thread)"
        ** NOTE: The current implementation declares this as NON-static
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: getMaxPriority("+
                               "Thread)");
            RTThread rtt2 = new RTThread(pp,ap);
            // NOTE - protected
            // ps.addToFeasibility(rtt2);
            rtt2.addToFeasibility();
            int x = ps.getMaxPriority(rtt2);
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: getMaxPriority("+
                               "Thread) failed");
            Tests.fail("ps.getMaxPriority(rtt2)",e);
        }

        /* Subtest 7:
        ** Method "public static int getMaxPriority(java.lang.Thread thread)"
        ** where thread is not scheduled by the required PriorityScheduler
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: getMaxPriority("+
                               "Thread)");
            int x = ps.getMaxPriority(new Thread());
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: getMaxPriority("+
                               "Thread) failed");
            Tests.fail("ps.getMaxPriority(new Thread())",e);
        }

        /* Subtest 8:
        ** Method "public int getMinPriority()"
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: getMinPriority()");
            int x = ps.getMinPriority();
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: getMinPriority() "+
                               "failed");
            Tests.fail("ps.getMinPriority()",e);
        }

        /* Subtest 9:
        ** Method "public static int getMinPriority(java.lang.Thread thread)"
        ** NOTE: The current implementation declares this as NON-static
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: getMinPriority("+
                               "Thread)");
            RTThread rtt2 = new RTThread(pp,ap);

            // NOTE: this is protected
            //ps.addToFeasibility(rtt2);

            rtt2.addToFeasibility();
            int x = ps.getMinPriority(rtt2);
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: getMinPriority("+
                               "Thread) failed");
            Tests.fail("ps.getMinPriority(rtt2)",e);
        }

        /* Subtest 10:
        ** Method "public static int getMinPriority(java.lang.Thread thread)"
        ** where thread is not scheduled by the required PriorityScheduler
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: getMinPriority("+
                               "Thread)");
            int x = ps.getMinPriority(new Thread());
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: getMinPriority("+
                               "Thread) failed");
            Tests.fail("ps.getMinPriority(new Thread())",e);
        }

        /* Subtest 11:
        ** Method "public int getNormPriority()"
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: getNormPriority()");
            int x = ps.getNormPriority();
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: getNormPriority() "+
                               "failed");
            Tests.fail("ps.getNormPriority()",e);
        }

        /* Subtest 12:
        ** Method "public static int getNormPriority(java.lang.Thread thread)"
        ** NOTE: The current implementation declares this as NON-static
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: getNormPriority("+
                               "Thread)");
            RTThread rtt2 = new RTThread(pp,ap);
            // NOTE: protected
            // ps.addToFeasibility(rtt2);
            rtt2.addToFeasibility();
            int x = ps.getNormPriority(rtt2);
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: getNormPriority("+
                               "Thread) failed");
            Tests.fail("ps.getNormPriority(rtt2)",e);
        }

        /* Subtest 13:
        ** Method "public static int getNormPriority(java.lang.Thread thread)"
        ** where thread is not scheduled by the required PriorityScheduler
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: getNormPriority("+
                               "Thread)");
            int x = ps.getNormPriority(new Thread());
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: getNormPriority("+
                               "Thread) failed");
            Tests.fail("ps.getNormPriority(new Thread())",e);
        }

        /* Subtest 14:
        ** Method "public java.lang.String getPolicyName()"
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: getPolicyName()");
            o = ps.getPolicyName();
            if( !(o instanceof String) )
                throw new Exception("Return object is not instanceof String");
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: getPolicyName() "+
                               "failed");
            Tests.fail("ps.getPolicyName()",e);
        }

        /* Subtest 15:
        ** Method "public static PriorityScheduler instance()"
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: instance()");
            o = PriorityScheduler.instance();
            if( !(o instanceof PriorityScheduler && o instanceof Scheduler) )
                throw new Exception("Return object is not instanceof "+
                                    "PriorityScheduler nor Scheduler");
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: instance() failed");
            Tests.fail("PriorityScheduler.instance()",e);
            System.out.println("PrioritySchedulerTest: ");
        }

        /* Subtest 16:
        ** Method "public boolean isFeasible()"
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: isFeasible()");
            boolean b = ps.isFeasible();
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: isFeasible() failed");
            Tests.fail("ps.isFeasible()",e);
        }

        /* Subtest 17:
        ** Method "protected void removeFromFeasibility(Schedulable s)"
        ** NOTES: Currently implemented as public
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: removeFrom"+
                               "Feasibility()");
            RTThread rtt2 = new RTThread(pp,ap);
            // NOTE: now protected
            // ps.addToFeasibility(rtt2);
            rtt2.addToFeasibility();
            // NOTE: now protected
            //ps.removeFromFeasibility(rtt2);
            rtt2.removeFromFeasibility();
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: removeFrom"+
                               "Feasibility() failed");
            Tests.fail("ps.removeFromFeasibility(rtt2)",e);
        }

        /* Subtest 18:
        ** Method "protected void removeFromFeasibility(Schedulable s)" where
        ** s is not scheduled by this scheduler
        ** NOTES: Currently implemented as public
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: removeFrom"+
                               "Feasibility()");
            RTThread rtt2 = new RTThread(pp,ap);
            // NOTE: now protected
            // ps.removeFromFeasibility(rtt2);
            rtt2.removeFromFeasibility();
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: removeFrom"+
                               "Feasibility() failed");
            Tests.fail("ps.removeFromFeasibility(rtt2)",e);
        }

        /** Scheduler Tests **/
        /* Subtest 19:
        ** Overridden Method "public static Scheduler getDefaultScheduler()"
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: getDefaultScheduler()");
            o = PriorityScheduler.getDefaultScheduler();
            if( !(o instanceof Scheduler) )
                throw new Exception("Return object is not instanceof "+
                                    "Scheduler");
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: getDefaultScheduler()");
            Tests.fail("PriorityScheduler.getDefaultScheduler",e);
        }

        /* Subtest 20:
        ** Method "public static void setDefaultScheduler(Scheduler scheduler)
        */
        Tests.increment();
        try {
            System.out.println("PrioritySchedulerTest: setDefaultScheduler("+
                               "Scheduler)");
            PriorityScheduler prisched = PriorityScheduler.instance();
            PriorityScheduler.setDefaultScheduler(prisched);
        } catch (Exception e) {
            System.out.println("PrioritySchedulerTest: setDefaultScheduler("+
                               "Scheduler) failed");
            Tests.fail("PriorityScheduler.setDefaultScheduler(rtt2)",e);
        }

        Tests.printSubTestReportTotals("PrioritySchedulerTest");
    }

    /*
      public static void example() {
      Tests.increment();
      try {
      Scheduler scheduler = findScheduler("EDF");
      if( scheduler != null ) {
      RealtimeThread t1 =
      new RealtimeThread(       (SchedulingParameters) new PriorityParameters(11),
      (ReleaseParameters) new PeriodicParameters(       null,
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
    */

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
