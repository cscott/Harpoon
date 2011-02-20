//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              ProcessingGroupParametersTest

Subtest 1
        "public ProcessingGroupParameters(HighResolutionTime start,
        RelativeTime period, RelativeTime cost, RelativeTime deadline,
        AsyncEventHandler overrunHandler, AsyncEventHandler missHandler)"
        where values are null

Subtest 2
        "public ProcessingGroupParameters(HighResolutionTime start,
        RelativeTime period, RelativeTime cost, RelativeTime deadline,
        AsyncEventHandler overrunHandler, AsyncEventHandler missHandler)"

Subtest 3:
        "public RelativeTime getCost()"

Subtest 4:
        "public AsyncEventHandler getCostOverrunHandler()"

Subtest 5:
        "public RelativeTime getDeadline()"

Subtest 6:
        "public AsyncEventHandler getDeadlineMissHandler()"

Subtest 7:
        "public RelativeTime getPeriod()"

Subtest 8:
        "public HighResolutionTime getStart()"

Subtest 9:
        "public void setCost(RelativeTime cost)"

Subtest 10:
        "public void setCostOverrunHandler(AsyncEventHandler handler)"

Subtest 11:
        "public void setDeadline(RelativeTime deadline)"

Subtest 12:
        "public void setDeadlineMissHandler(AsyncEventHandler handler)"

Subtest 13:
        "public void setPeriod(RelativeTime period)"

Subtest 14:
        "public void setStart(HighResolutionTime start)
*/

import javax.realtime.*;

public class ProcessingGroupParametersTest
{

    public static void run()
    {
        HighResolutionTime start = new RelativeTime(1000L,100);
        RelativeTime period = new RelativeTime(900L,0);
        RelativeTime cost = new RelativeTime(500L,0);
        RelativeTime deadline = new RelativeTime(600L,0);
        AsyncEventHandler overrunHandler = new AEventHandler();
        AsyncEventHandler missHandler = new AEventHandler();
        ProcessingGroupParameters pgp = null;
        Object o = null;
        Tests.newTest("ProcessingGroupParameters");

        /* Subtest 1
        ** Constructor "public ProcessingGroupParameters(HighResolutionTime
        ** start, RelativeTime period, RelativeTime cost, RelativeTime
        ** deadline, AsyncEventHandler overrunHandler, AsyncEventHandler
        ** missHandler)" where values are null
        */
        Tests.increment();
        try {
            System.out.println("ProcessingGroupParametersTest: Processing"+
                               "GroupParameters(null, null, null, null, null,"+
                               "null)");
            pgp = new ProcessingGroupParameters(null, null, null, null, null,
                                                null);
            if( !(pgp instanceof ProcessingGroupParameters) )
                throw new Exception("Return object is not instanceof "+
                                    "ProcessingGroupParameters");
        } catch (Exception e) {
            System.out.println("ProcessingGroupParametersTest: Processing"+
                               "GroupParameters(null, null, null, null, null,"+
                               "null)");
            Tests.fail("ProcessingGroupParameters(null,null,null,null,null,"+
                       "null",e);
        }

        /* Subtest 2
        ** Constructor "public ProcessingGroupParameters(HighResolutionTime
        ** start, RelativeTime period, RelativeTime cost, RelativeTime
        ** deadline, AsyncEventHandler overrunHandler, AsyncEventHandler
        ** missHandler)"
        */
        Tests.increment();
        try {
            System.out.println("ProcessingGroupParametersTest: Processing"+
                               "GroupParameters(HighResolutionTime,Relative"+
                               "Time,RelativeTime,RelativeTime,AsyncEvent"+
                               "Handler,AsyncEventHandler)");
            pgp = new ProcessingGroupParameters(start, period, cost, deadline,
                                                overrunHandler, missHandler);
            if( !(pgp instanceof ProcessingGroupParameters) )
                throw new Exception("Return object is not instanceof "+
                                    "ProcessingGroupParameters");
        } catch (Exception e) {
            System.out.println("ProcessingGroupParametersTest: Processing"+
                               "GroupParameters(HighResolutionTime,Relative"+
                               "Time,RelativeTime,RelativeTime,AsyncEvent"+
                               "Handler,AsyncEventHandler) failed");
            Tests.fail("new ProcessingGroupParameters(start,period,cost,"+
                       "deadline,overrunHandler,missHandler",e);
        }

        /* Subtest 3:
        ** Method "public RelativeTime getCost()"
        */
        Tests.increment();
        try {
            System.out.println("ProcessingGroupParametersTest: getCost()");
            o = pgp.getCost();
            if( !(o instanceof RelativeTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime");
            RelativeTime cost2 = (RelativeTime)o;
            if (!(cost2.equals(cost)))
                throw new Exception("Unexpected cost received");
        } catch (Exception e) {
            System.out.println("ProcessingGroupParametersTest: getCost() "+
                               "failed");
            Tests.fail("pgp.getCost()",e);
        }

        /* Subtest 4:
        ** Method "public AsyncEventHandler getCostOverrunHandler()"
        */
        Tests.increment();
        try {
            System.out.println("ProcessingGroupParametersTest: "+
                               "getCostOverrunHandler()");
            o = pgp.getCostOverrunHandler();
            if( !(o instanceof AsyncEventHandler) )
                throw new Exception("Return object is not instanceof "+
                                    "AsyncEventHandler");
            AsyncEventHandler handler = (AsyncEventHandler)o;
            if (!(handler.equals(overrunHandler)))
                throw new Exception("Unexpected cost overrun handler "+
                                    "received");
        } catch (Exception e) {
            System.out.println("ProcessingGroupParametersTest: "+
                               "getCostOverrunHandler() failed");
            Tests.fail("pgp.getCostOverrunHandler()",e);
        }

        /* Subtest 5:
        ** Method "public RelativeTime getDeadline()"
        */
        Tests.increment();
        try {
            System.out.println("ProcessingGroupParametersTest: getDeadline()");
            o = pgp.getDeadline();
            if( !(o instanceof RelativeTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime");
            RelativeTime deadline2 = (RelativeTime)o;
            if (!(deadline2.equals(deadline)))
                throw new Exception("Unexpected deadline received");
        } catch (Exception e) {
            System.out.println("ProcessingGroupParametersTest: getDeadline() "+
                               "failed");
            Tests.fail("pgp.getDeadline()",e);
        }

        /* Subtest 6:
        ** Method "public AsyncEventHandler getDeadlineMissHandler()"
        */
        Tests.increment();
        try {
            System.out.println("ProcessingGroupParametersTest: getDeadline"+
                               "MissHandler()");
            o = pgp.getDeadlineMissHandler();
            if( !(o instanceof AsyncEventHandler) )
                throw new Exception("Return object is not instanceof "+
                                    "AsyncEventHandler");
            AsyncEventHandler handler = (AsyncEventHandler)o;
            if (!(handler.equals(missHandler)))
                throw new Exception("Unexpected deadline missed handler "+
                                    "received");
        } catch (Exception e) {
            System.out.println("ProcessingGroupParametersTest: getDeadline"+
                               "MissHandler() failed");
            Tests.fail("pgp.getDeadlineMissHandler()",e);
        }

        /* Subtest 7:
        ** Method "public RelativeTime getPeriod()"
        */
        Tests.increment();
        try {
            System.out.println("ProcessingGroupParametersTest: getPeriod()");
            o = pgp.getPeriod();
            if( !(o instanceof RelativeTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime");
            RelativeTime period2 = (RelativeTime)o;
            if (!(period2.equals(period)))
                throw new Exception("Unexpected period received");
        } catch (Exception e) {
            System.out.println("ProcessingGroupParametersTest: getPeriod() "+
                               "failed");
            Tests.fail("pgp.getPeriod()",e);
        }

        /* Subtest 8:
        ** Method "public HighResolutionTime getStart()"
        */
        Tests.increment();
        try {
            System.out.println("ProcessingGroupParametersTest: getStart()");
            o = pgp.getStart();
            if( !(o instanceof HighResolutionTime) )
                throw new Exception("Return object is not instanceof "+
                                    "RelativeTime");
            HighResolutionTime start2 = (HighResolutionTime)o;
            if (!(start2.equals(start)))
                throw new Exception("Unexpected start time received");
        } catch (Exception e) {
            System.out.println("ProcessingGroupParametersTest: getStart() "+
                               "failed");
            Tests.fail("pgp.getStart()",e);
        }

        /* Subtest 9:
        ** Method "public void setCost(RelativeTime cost)"
        */
        Tests.increment();
        try {
            System.out.println("ProcessingGroupParametersTest: setCost("+
                               "RelativeTime)");
            RelativeTime cost1 = new RelativeTime(4500L,0);
            pgp.setCost(cost1);
            RelativeTime cost2 = pgp.getCost();
            if (!(cost2.equals(cost1)))
                throw new Exception("Cost not set properly");
        } catch (Exception e) {
            System.out.println("ProcessingGroupParametersTest: setCost("+
                               "RelativeTime) failed");
            Tests.fail("pgp.setCost(cost1)",e);
        }

        /* Subtest 10:
        ** Method "public void setCostOverrunHandler(AsyncEventHandler
        ** handler)"
        */
        Tests.increment();
        try {
            System.out.println("ProcessingGroupParametersTest: setCostOverrun"+
                               "Handler(AsyncEventHandler)");
            AsyncEventHandler aeh1 = new AEventHandler();
            pgp.setCostOverrunHandler(aeh1);
            AsyncEventHandler aeh2 = pgp.getCostOverrunHandler();
            if (!(aeh2.equals(aeh1)))
                throw new Exception("Cost overrun handler not set properly");
        } catch (Exception e) {
            System.out.println("ProcessingGroupParametersTest: setCostOverrun"+
                               "Handler(AsyncEventHandler) failed");
            Tests.fail("pgp.setCostOverrunHandler(aeh1)",e);
        }

        /* Subtest 11:
        ** Method "public void setDeadline(RelativeTime deadline)"
        */
        Tests.increment();
        try {
            System.out.println("ProcessingGroupParametersTest: setDeadline("+
                               "RelativeTime)");
            RelativeTime deadline1 = new RelativeTime(1000L,500);
            pgp.setDeadline(deadline1);
            RelativeTime deadline2 = pgp.getDeadline();
            if (!(deadline2.equals(deadline1)))
                throw new Exception("Deadline not set properly");
        } catch (Exception e) {
            System.out.println("ProcessingGroupParametersTest: setDeadline("+
                               "RelativeTime) failed");
            Tests.fail("pgp.setDeadline(deadline1)",e);
        }

        /* Subtest 12:
        ** Method "public void setDeadlineMissHandler(AsyncEventHandler
        ** handler)"
        */
        Tests.increment();
        try {
            System.out.println("ProcessingGroupParametersTest: setDeadline"+
                               "MissHandler(AsyncEventHandler)");
            AsyncEventHandler aeh1 = new AEventHandler();
            pgp.setDeadlineMissHandler(aeh1);
            AsyncEventHandler aeh2 = pgp.getDeadlineMissHandler();
            if (!(aeh2.equals(aeh1)))
                throw new Exception("Deadline Missed Handler is not set "+
                                    "properly");
        } catch (Exception e) {
            System.out.println("ProcessingGroupParametersTest: setDeadline"+
                               "MissHandler(AsyncEventHandler) failed");
            Tests.fail("pgp.setDeadlineMissHandler(aeh1)",e);
        }

        /* Subtest 13:
        ** Method "public void setPeriod(RelativeTime period)"
        */
        Tests.increment();
        try {
            System.out.println("ProcessingGroupParametersTest: setPeriod("+
                               "RelativeTime)");
            RelativeTime period1 = new RelativeTime(1000L,200);
            pgp.setPeriod(period1);
            RelativeTime period2 = pgp.getPeriod();
            if (!(period2.equals(period1)))
                throw new Exception("Period not set properly");
        } catch (Exception e) {
            System.out.println("ProcessingGroupParametersTest: setPeriod("+
                               "RelativeTime) failed");
            Tests.fail("pgp.setPeriod(period1)",e);
        }

        /* Subtest 14:
        ** Method "public void setStart(HighResolutionTime start)
        */
        Tests.increment();
        try {
            System.out.println("ProcessingGroupParametersTest: setStart(High"+
                               "ResolutionTime)");
            HighResolutionTime start1 = new RelativeTime(500L,0);
            pgp.setStart(start1);
            HighResolutionTime start2 = pgp.getStart();
            if (!(start2.equals(start1)))
                throw new Exception("Start time not set properly");
        } catch (Exception e) {
            System.out.println("ProcessingGroupParametersTest: setStart(High"+
                               "ResolutionTime) failed");
            Tests.fail("pgp.setStart(start1)",e);
        }

        Tests.printSubTestReportTotals("ProcessingGroupParametersTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
