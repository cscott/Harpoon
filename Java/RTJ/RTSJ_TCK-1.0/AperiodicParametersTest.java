//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              AperiodicParametersTest

Subtest 1:
        "public AperiodicParameters(RelativeTime cost,
                                    RelativeTime deadline,
                                     AsyncEventHandler overrunHandler,
                                     AsyncEventHandler missHandler)
             where parameters are all null

Subtest 2:
        "public AperiodicParameters(RelativeTime cost,
                                    RelativeTime deadline,
                                    AsyncEventHandler overrunHandler,
                                    AsyncEventHandler missHandler)

Subtest 3:
        "public RelativeTime getCost()"

Subtest 4:
        "public getCostOverrunHandler()"

Subtest 5:
        "public RelativeTime getDeadline()"

Subtest 6:
        "public AsyncEventHandler getDeadlineMissHandler()"

Subtest 7:
        "public void setCost(RelativeTime cost)"

Subtest 8:
        "public void setCostOverrunHandler(AsyncEventHandler handler)"

Subtest 9:
        "public void setDeadline(RelativeTime deadline)"

Subtest 10:
        "public void setDeadlineMissHandler(AsyncEventHandler handler)"

Subtest 11:
        "public boolean setIfFeasible(RelativeTime cost,
                                      RelativeTime deadline)"

Subtest 12:
        "public boolean setIfFeasible(RelativeTime cost,
                                      RelativeTime deadline)"
        where all parameters are null
*/

import javax.realtime.*;

public class AperiodicParametersTest {

    public static void run() {
        AperiodicParameters ap = null;
        Object o = null;

        Tests.newTest("AperiodicParametersTest (abstract ReleaseParameters)");
        Tests.increment();
        /* Subtest 1:
        ** Constructor "public AperiodicParameters(RelativeTime cost,
        ** RelativeTime deadline, AsyncEventHandler overrunHandler,
        ** AsyncEventHandler missHandler) where parameters are all null
        */
        try {
            System.out.println("AperiodicParmetersTest: AperiodicParameters("+
                               "RleativeTime,RelativeTime,AsyncEventHandler,"+
                               "AsyncEventHandler)");
            RelativeTime cost = null;
            RelativeTime deadline = null;
            AsyncEventHandler overrunHandler = null;
            AsyncEventHandler missHandler = null;
            ap = new AperiodicParameters(cost, deadline, overrunHandler,
                                         missHandler);
            if( !(ap instanceof AperiodicParameters && ap instanceof
                  ReleaseParameters) )
                throw new Exception("Return object is not instanceof "+
                                    "AperiodicParameters nor "+
                                    "ReleaseParameters");
        } catch (Exception e) {
            System.out.println("AperiodicParmetersTest: AperiodicParameters("+
                               "RleativeTime,RelativeTime,AsyncEventHandler,"+
                               "AsyncEventHandler) failed");
            Tests.fail("new AperiodicParametersTest(cost, deadline, "+
                       "overrunHandler,missHandler)",e);
        }

        Tests.increment();
        /* Subtest 2:
        ** Constructor "public AperiodicParameters(RelativeTime cost,
        ** RelativeTime deadline, AsyncEventHandler overrunHandler,
        ** AsyncEventHandler missHandler)
        */
        try {
            System.out.println("AperiodicParmetersTest: AperiodicParameters("+
                               "RelativeTime,RelativeTime,AsyncEventHandler,"+
                               "AsyncEventHandler)");
            RelativeTime cost = new RelativeTime(0,0);
            RelativeTime deadline = new RelativeTime(1000L,0);
            AEventHandler overrunHandler = new AEventHandler();
            AEventHandler missHandler = new AEventHandler();
            ap = new AperiodicParameters(cost, deadline, overrunHandler,
                                         missHandler);
            if( !(ap instanceof AperiodicParameters && ap instanceof
                  ReleaseParameters) )
                throw new Exception("Return object is not instanceof "+
                                    "AperiodicParameters nor "+
                                    "ReleaseParameters");
        } catch (Exception e) {
            System.out.println("AperiodicParmetersTest: AperiodicParameters("+
                               "RelativeTime,RelativeTime,AsyncEventHandler,"+
                               "AsyncEventHandler) failed");
            Tests.fail("new AperiodicParametersTest(cost, deadline, "+
                       "overrunHandler,missHandler)",e);
        }

        // ReleaseParameters tests
        /* Subtest 3:
        ** Method "public RelativeTime getCost()"
        */
        Tests.increment();
        try {
            System.out.println("AperiodicParmetersTest: getCost()");
            RelativeTime cost = new RelativeTime(0,0);
            RelativeTime deadline = new RelativeTime(1000L,0);
            AEventHandler overrunHandler = new AEventHandler();
            AEventHandler missHandler = new AEventHandler();
            ap = new AperiodicParameters(cost, deadline, overrunHandler,
                                         missHandler);
            o = ap.getCost();
            if( !(o instanceof RelativeTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime");
        } catch (Exception e) {
            System.out.println("AperiodicParmetersTest: getCost() failed");
            Tests.fail("ap.getCost()",e);
        }

        /* Subtest 4:
        ** Method "public getCostOverrunHandler()"
        */
        Tests.increment();
        try {
            System.out.println("AperiodicParmetersTest: getCostOverrun"+
                               "Handler()");
            o = ap.getCostOverrunHandler();
            if( !(o instanceof AsyncEventHandler) )
                throw new Exception("Return object is not instanceof "+
                                    "AsyncEventHandler");
        } catch (Exception e) {
            System.out.println("AperiodicParmetersTest: getCostOverrun"+
                               "Handler() failed");
            Tests.fail("ap.getCostOverrunHandler()",e);
        }

        /* Subtest 5:
        ** Method "public RelativeTime getDeadline()"
        */
        Tests.increment();
        try {
            System.out.println("AperiodicParmetersTest: getDeadline()");
            o = ap.getDeadline();
            if( !(o instanceof RelativeTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime");
        } catch (Exception e) {
            System.out.println("AperiodicParmetersTest: getDeadline() failed");
            Tests.fail("ap.getDeadline()",e);
        }

        /* Subtest 6:
        ** Method "public AsyncEventHandler getDeadlineMissHandler()"
        */
        Tests.increment();
        try {
            System.out.println("AperiodicParmetersTest: getDeadlineMiss"+
                               "Handler()");
            o = ap.getDeadlineMissHandler();
            if( !(o instanceof AsyncEventHandler) )
                throw new Exception("Return object is not instanceof "+
                                    "AsyncEventHandler");
        } catch (Exception e) {
            System.out.println("AperiodicParmetersTest: getDeadlineMiss"+
                               "Handler() failed");
            Tests.fail("ap.getDeadlineMissHandler()",e);
        }

        /* Subtest 7:
        ** Method "public void setCost(RelativeTime cost)"
        */
        Tests.increment();
        try {
            System.out.println("AperiodicParmetersTest: setCost(Relative"+
                               "Time)");
            long millis = 10223;
            int nanos = 100;
            RelativeTime cost = new RelativeTime(millis, nanos);
            ap.setCost(cost);
            RelativeTime rt;
            rt = ap.getCost();
            if (rt.compareTo(cost)!=0)
                {
                    throw new Exception("Cost was not set correctly");
                }

        } catch (Exception e) {
            System.out.println("AperiodicParmetersTest: setCost(Relative"+
                               "Time) failed");
            Tests.fail("ap.setCost(cost)",e);
        }

        /* Subtest 8:
        ** Method "public void setCostOverrunHandler(AsyncEventHandler handler)"
        */
        Tests.increment();
        try {
            System.out.println("AperiodicParmetersTest: setCostOverrun"+
                               "Handler(AsyncEventHandler)");
            AEventHandler handler = new AEventHandler();
            ap.setCostOverrunHandler(handler);
        } catch (Exception e) {
            System.out.println("AperiodicParmetersTest: setCostOverrun"+
                               "Handler(AsyncEventHandler) failed");
            Tests.fail("ap.setCostOverrunHandler(handler)",e);
        }

        /* Subtest 9:
        ** Method "public void setDeadline(RelativeTime deadline)"
        */

        Tests.increment();
        try {
            System.out.println("AperiodicParmetersTest: setDeadline(Relative"+
                               "Time)");
            long millis = 10223;
            int nanos = 100;
            RelativeTime deadline = new RelativeTime(millis, nanos);
            ap.setDeadline(deadline);
            RelativeTime rt;
            rt = ap.getDeadline();
            if (rt.compareTo(deadline)!=0)
                {
                    throw new Exception("Deadline was not set correctly");
                }

        } catch (Exception e) {
            System.out.println("AperiodicParmetersTest: setDeadline(Relative"+
                               "Time) failed");
            Tests.fail("ap.setDeadline(deadline)",e);
        }

        /* Subtest 10:
        ** Method "public void setDeadlineMissHandler(AsyncEventHandler
        ** handler)"
        */
        Tests.increment();
        try {
            System.out.println("AperiodicParmetersTest: setDeadlineMiss"+
                               "Handler(AsyncEventHandler)");
            AEventHandler handler = new AEventHandler();
            ap.setDeadlineMissHandler(handler);
        } catch (Exception e) {
            System.out.println("AperiodicParmetersTest: setDeadlineMiss"+
                               "Handler(AsyncEventHandler) failed");
            Tests.fail("ap.setDeadlineMissHandler(handler)",e);
        }

        /* Subtest 11:
        ** Method "public boolean setIfFeasible(RelativeTime cost,
        **                                         RelativeTime deadline)
        */
        Tests.increment();
        try {
            System.out.println("AperiodicParmetersTest: setIfFeasible"+
                               "(RelativeTime cost, RelativeTime deadline)");
            RelativeTime cost     = new RelativeTime( 100, 0);
            RelativeTime deadline = new RelativeTime(1000, 0);
            boolean b = ap.setIfFeasible(cost, deadline);
        } catch (Exception e) {
            System.out.println("AperiodicParmetersTest: setIfFeasible"+
                               "(RT,RT) failed");
            Tests.fail("ap.setIfFeasible(RT,RT)",e);
        }

        /* Subtest 12:
        ** Method "public boolean setIfFeasible(RelativeTime cost,
        **                                         RelativeTime deadline)
        **   where all parameters are null
        */
        Tests.increment();
        try {
            System.out.println("AperiodicParmetersTest: setIfFeasible"+
                               "(RelativeTime cost, RelativeTime deadline)");
            RelativeTime cost     = null;
            RelativeTime deadline = null;
            boolean b = ap.setIfFeasible(cost, deadline);
        } catch (Exception e) {
            System.out.println("AperiodicParmetersTest: setIfFeasible"+
                               "(RT,RT) failed");
            Tests.fail("ap.setIfFeasible(RT,RT)",e);
        }

        Tests.printSubTestReportTotals("AperiodicParametersTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
