//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              SporadicParametersTest

Subtest 1:
        "public SporadicParameters(RelativeTime minInterarrival,RelativeTime
        cost, RelativeTime deadlin, AsyncEventHandler overrunHandler,
        AsyncEventHandler missHandler)" where all values are null

Subtest 2:
        "public SporadicParameters(RelativeTime minInterarrival,RelativeTime
        cost, RelativeTime deadlin, AsyncEventHandler overrunHandler,
        AsyncEventHandler missHandler)"

Subtest 3:
        "public String getArrivalTimeQueueOverflowBehavior()"

Subtest 4:
        "public int getInitialArrivalTimeQueueLength()"

Subtest 5:
        "public RelativeTime getMinimumInterarrival()"

Subtest 6:
        "public String getMitViolationBehavior()"

Subtest 7:
        "public void setArrivalTimeQueueOverflowBehavior(String)"

Subtest 8:
        "public boolean setIfFeasible(RelativeTime cost, RelativeTime deadline)"

Subtest 9:
        "public boolean setIfFeasible(RelativeTime cost, RelativeTime deadline)"
        where all parameters are null

Subtest 10:
        "public boolean setIfFeasible(RelativeCost minInterarrival, RelativeTime cost, RelativeTime deadline)"

Subtest 11:
        "public boolean setIfFeasible(RelativeCost minInterarrival, RelativeTime cost, RelativeTime deadline)"
        where all parameters are null

Subtest 12:
        "public void setInitialArrivalTimeQueueLength(int)"

Subtest 13:
        "public void setMinimumInterarrival(RelativeTime minimum)"

Subtest 14:
        "public void setMitViolationBehavior(String)"

*/

import javax.realtime.*;

public class SporadicParametersTest
{

    public static void run()
    {
        SporadicParameters sp = null;
        Object o = null;
        RelativeTime minInterarrival = null;
        Tests.newTest("SporadicParametersTest");

        /* Subtest 1:
        ** Constructor "public SporadicParameters(RelativeTime minInterarrival,
        ** RelativeTime cost, RelativeTime deadlin, AsyncEventHandler
        ** overrunHandler, AsyncEventHandler missHandler)" where all values
        ** are null
        */

        Tests.increment();
        try {
            System.out.println("SporadicParametersTest: public Sporadic"+
                               "Parameters(null,null,null,null,null)");
            RelativeTime cost = null;
            RelativeTime deadline = null;
            AsyncEventHandler overrunHandler = null;
            AsyncEventHandler missHandler = null;
            sp = new SporadicParameters(minInterarrival, cost, deadline,
                                        overrunHandler, missHandler);
            if( !(sp instanceof SporadicParameters && sp instanceof
                  AperiodicParameters) )
                throw new Exception("Return object is not instanceof "+
                                    "SporadicParameters nor Aperiodic"+
                                    "Parameters");
        } catch (Exception e) {
            System.out.println("SporadicParametersTest: public Sporadic"+
                               "Parameters(null,null,null,null,null) failed");
            Tests.fail("new SporadicParameters(minInterarrival,cost,deadline,"+
                       "overrunHandler,missHandler",e);
        }

        /* Subtest 2:
        ** Constructor "public SporadicParameters(RelativeTime minInterarrival,
        ** RelativeTime cost, RelativeTime deadlin, AsyncEventHandler
        ** overrunHandler, AsyncEventHandler missHandler)"
        */
        Tests.increment();
        try {
            System.out.println("SporadicParametersTest: SporadicParameters("+
                               "RelativeTime,RelativeTime,RelativeTime,Async"+
                               "EventHandler,AsyncEventHandler)");
            minInterarrival = new RelativeTime(200L,0);
            RelativeTime cost = new RelativeTime(8000L,500);
            RelativeTime deadline = new RelativeTime(200L,0);
            AsyncEventHandler overrunHandler = new AEventHandler();
            AsyncEventHandler missHandler = new AEventHandler();

            sp = new SporadicParameters(minInterarrival, cost, deadline,
                                        overrunHandler, missHandler);
            if( !(sp instanceof SporadicParameters && sp instanceof
                  AperiodicParameters) )
                throw new Exception("Return object is not instanceof Sporadic"+
                                    "Parameters nor AperiodicParameters");
        } catch (Exception e) {
            System.out.println("SporadicParametersTest: SporadicParameters("+
                               "RelativeTime,RelativeTime,RelativeTime,Async"+
                               "EventHandler,AsyncEventHandler) failed");
            Tests.fail("new SporadicParameters(minInterarrival,cost,deadline,"+
                       "overrunHandler,missHandler",e);
        }

        /* Subtest 3:
        ** Method "public String getArrivalTimeQueueOverflowBehavior()"
        */
        Tests.increment();
        try {
            String s;

            System.out.println("SporadicParametersTest: "+
                               "getArrivalTimeQueueOverflowBehavior()");
            s = sp.getArrivalTimeQueueOverflowBehavior();
            System.out.println("getArrivalTimeQueueOverflowBehavior: " + s);
        } catch (Exception e) {
            System.out.println("SporadicParametersTest: "+
                               "getArrivalTimeQueueOverflowBehavior() failed");
            Tests.fail("sp.getArrivalTimeQueueOverflowBehavior()",e);
        }

        /* Subtest 4:
        ** Method "public int getInitialArrivalTimeQueueLength()"
        */
        Tests.increment();
        try {
            int i;

            System.out.println("SporadicParametersTest: "+
                               "getInitialArrivalTimeQueueLength()");
            i = sp.getInitialArrivalTimeQueueLength();
        } catch (Exception e) {
            System.out.println("SporadicParametersTest: getMinimum"+
                               "Interarrival() failed");
            Tests.fail("sp.getMiminumInterarrival()",e);
        }

        /* Subtest 5:
        ** Method "public RelativeTime getMinimumInterarrival()"
        */
        Tests.increment();
        try {
            System.out.println("SporadicParametersTest: getMinimum"+
                               "Interarrival()");
            sp.setMinimumInterarrival(minInterarrival);
            o = sp.getMinimumInterarrival();
            if( !(o instanceof RelativeTime) )
                throw new Exception("Return object is not instanceof Relative"+
                                    "Time");
            RelativeTime rt = (RelativeTime)o;
            if (!(rt.equals(minInterarrival)))
                throw new Exception("Unexpected Minimum Interarrival time "+
                                    "received");
        } catch (Exception e) {
            System.out.println("SporadicParametersTest: getMinimum"+
                               "Interarrival() failed");
            Tests.fail("sp.getMiminumInterarrival()",e);
        }

        /* Subtest 6:
        ** Method "public String getMitViolationBehavior()"
        */
        Tests.increment();
        try {
            String s;

            System.out.println("SporadicParametersTest: "+
                               "getMitViolationBehavior()");
            s = sp.getMitViolationBehavior();
            System.out.println("getMitViolationBehavior: " + s);
        } catch (Exception e) {
            System.out.println("SporadicParametersTest: "+
                               "getMitViolationBehavior() failed");
            Tests.fail("sp.getMitViolationBehavior()",e);
        }

        /* Subtest 7:
        ** Method "public void setArrivalTimeQueueOverflowBehavior(String)"
        */
        Tests.increment();
        try {
            String s;

            System.out.println("SporadicParametersTest: "+
                               "getArrivalTimeQueueOverflowBehavior()");

            s = SporadicParameters.arrivalTimeQueueOverflowExcept;
            sp.setArrivalTimeQueueOverflowBehavior(s);
            s = sp.getArrivalTimeQueueOverflowBehavior();
            System.out.println("getArrivalTimeQueueOverflowBehavior: " + s);

            s = SporadicParameters.arrivalTimeQueueOverflowIgnore;
            sp.setArrivalTimeQueueOverflowBehavior(s);
            s = sp.getArrivalTimeQueueOverflowBehavior();
            System.out.println("getArrivalTimeQueueOverflowBehavior: " + s);

            s = SporadicParameters.arrivalTimeQueueOverflowReplace;
            sp.setArrivalTimeQueueOverflowBehavior(s);
            s = sp.getArrivalTimeQueueOverflowBehavior();
            System.out.println("getArrivalTimeQueueOverflowBehavior: " + s);

            s = SporadicParameters.arrivalTimeQueueOverflowSave;
            sp.setArrivalTimeQueueOverflowBehavior(s);
            s = sp.getArrivalTimeQueueOverflowBehavior();
            System.out.println("getArrivalTimeQueueOverflowBehavior: " + s);

        } catch (Exception e) {
            System.out.println("SporadicParametersTest: "+
                               "getArrivalTimeQueueOverflowBehavior() failed");
            Tests.fail("sp.getArrivalTimeQueueOverflowBehavior()",e);
        }

        /* Subtest 8:
        ** Method "public boolean setIfFeasible(RelativeTime cost,
        **                                         RelativeTime deadline)
        */
        Tests.increment();
        try {
            System.out.println("SporadicParmetersTest: setIfFeasible"+
                               "(RelativeTime cost, RelativeTime deadline)");
            RelativeTime cost     = new RelativeTime( 100, 0);
            RelativeTime deadline = new RelativeTime(1000, 0);
            boolean b = sp.setIfFeasible(cost, deadline);
        } catch (Exception e) {
            System.out.println("SporadicParmetersTest: setIfFeasible"+
                               "(RT,RT) failed");
            Tests.fail("sp.setIfFeasible(RT,RT)",e);
        }

        /* Subtest 9:
        ** Method "public boolean setIfFeasible(RelativeTime cost,
        **                                         RelativeTime deadline)
        **   where all parameters are null
        */
        Tests.increment();
        try {
            System.out.println("SporadicParmetersTest: setIfFeasible"+
                               "(RelativeTime cost, RelativeTime deadline)");
            RelativeTime cost     = null;
            RelativeTime deadline = null;
            boolean b = sp.setIfFeasible(cost, deadline);
        } catch (Exception e) {
            System.out.println("SporadicParmetersTest: setIfFeasible"+
                               "(RT,RT) failed");
            Tests.fail("sp.setIfFeasible(RT,RT)",e);
        }

        /* Subtest 10:
        ** Method "public boolean setIfFeasible(RelativeTime minInterarr,
        **                                         RelativeTime cost,
        **                                         RelativeTime deadline)
        */
        Tests.increment();
        try {
            System.out.println("SporadicParmetersTest: setIfFeasible"+
                               "(RT,RT,RT)");
            RelativeTime mininter = new RelativeTime( 100, 0);
            RelativeTime cost     = new RelativeTime( 100, 0);
            RelativeTime deadline = new RelativeTime(1000, 0);
            boolean b = sp.setIfFeasible(mininter, cost, deadline);
        } catch (Exception e) {
            System.out.println("SporadicParmetersTest: setDeadlineMiss"+
                               "(RT,RT,RT) failed");
            Tests.fail("sp.setIfFeasible(RT,RT,RT)",e);
        }

        /* Subtest 11:
        ** Method "public boolean setIfFeasible(RelativeTime minInterarr,
        **                                         RelativeTime cost,
        **                                         RelativeTime deadline)
        **   where all parameters are null
        */
        Tests.increment();
        try {
            System.out.println("SporadicParmetersTest: setIfFeasible"+
                               "(RT,RT,RT)");
            RelativeTime mininter = null;
            RelativeTime cost     = null;
            RelativeTime deadline = null;
            boolean b = sp.setIfFeasible(mininter, cost, deadline);
        } catch (Exception e) {
            System.out.println("SporadicParmetersTest: setIfFeasible"+
                               "(RT,RT,RT) failed");
            Tests.fail("sp.setIfFeasible(RT,RT,RT)",e);
        }

        /* Subtest 12:
        ** Method "public void setInitialArrivalTimeQueueLength(int)"
        */
        Tests.increment();
        try {
            int i = 100;

            System.out.println("SporadicParametersTest: "+
                               "getInitialArrivalTimeQueueLength()");
            sp.setInitialArrivalTimeQueueLength(i);
        } catch (Exception e) {
            System.out.println("SporadicParametersTest: "+
                               "setInitialArrivalTimeQueueLength() failed");
            Tests.fail("sp.setInitialArrivalTimeQueueLength()",e);
        }

        /* Subtest 13:
        ** Method "public void setMinimumInterarrival(RelativeTime minimum)"
        */
        Tests.increment();
        try {
            System.out.println("SporadicParametersTest: setMinimum"+
                               "Interarrival(RelativeTime)");
            RelativeTime rt = new RelativeTime(500L,0);
            sp.setMinimumInterarrival(rt);
            RelativeTime rt2 = sp.getMinimumInterarrival();
            if (! (rt.equals(rt2)))
                throw new Exception("Minimum Interarrival time not set "+
                                    "properly");
        } catch (Exception e) {
            System.out.println("SporadicParametersTest: setMinimum"+
                               "Interarrival(RelativeTime) failed");
            Tests.fail("sp.setMinimumInterarrival(rt)",e);
        }

        /* Subtest 14:
        ** Method "public void setMitViolationBehavior(String)"
        */
        Tests.increment();
        try {
            String s;

            System.out.println("SporadicParametersTest: "+
                               "getMitViolationBehavior()");

            s = SporadicParameters.mitViolationExcept;
            sp.setMitViolationBehavior(s);
            s = sp.getMitViolationBehavior();
            System.out.println("getMitViolationBehavior: " + s);

            s = SporadicParameters.mitViolationIgnore;
            sp.setMitViolationBehavior(s);
            s = sp.getMitViolationBehavior();
            System.out.println("getMitViolationBehavior: " + s);

            s = SporadicParameters.mitViolationReplace;
            sp.setMitViolationBehavior(s);
            s = sp.getMitViolationBehavior();
            System.out.println("getMitViolationBehavior: " + s);

            s = SporadicParameters.mitViolationSave;
            sp.setMitViolationBehavior(s);
            s = sp.getMitViolationBehavior();
            System.out.println("getMitViolationBehavior: " + s);

        } catch (Exception e) {
            System.out.println("SporadicParametersTest: "+
                               "getMitViolationBehavior() failed");
            Tests.fail("sp.getMitViolationBehavior()",e);
        }

        Tests.printSubTestReportTotals("SporadicParametersTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
