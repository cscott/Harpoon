//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              PeriodicParametersTest

Subtest 1:
        "public PeriodicParameters(HighResolutionTime start, RelativeTime
        period, RelativeTime cost, RelativeTime deadline, AsyncEventHandler
        overrunHandler, AsyncEventHandler missHandler)" where all given
        parameters are null

Subtest 2:
        "public PeriodicParameters(HighResolutionTime start, RelativeTime
        period, RelativeTime cost, RelativeTime deadline, AsyncEventHandler
        overrunHandler, AsyncEventHandler missHandler)" where given parameters
        have value

Subtest 3:
        "public RelativeTime getPeriod()"

Subtest 4:
        "public HighResolutionTime getStart()"

Subtest 5:
        "public void setPeriod(RelativeTime period)"

Subtest 6:
        "public void setStart(HighResolutionTime start)"

Subtest 7:
        "public boolean setIfFeasible(RT cost, RT deadline)"

Subtest 8:
        "public boolean setIfFeasible(RT period, RT cost, RT deadline)"

*/

import javax.realtime.*;

public class PeriodicParametersTest
{

    public static void run()
    {
        PeriodicParameters pp = null;
        Object o = null;

        RelativeTime start;
        RelativeTime period;
        RelativeTime cost;
        RelativeTime deadline;

        RelativeTime newperiod;
        RelativeTime newcost;
        RelativeTime newdeadline;

        /* Subtest 1:
        ** Constructor "public PeriodicParameters(HighResolutionTime start,
        ** RelativeTime period, RelativeTime cost, RelativeTime deadline,
        ** AsyncEventHandler overrunHandler, AsyncEventHandler missHandler)"
        ** where all given parameters are null
        */
        Tests.newTest("PeriodicParametersTest");
        Tests.increment();
        try {
            System.out.println("PeriodicParametersTest: PeriodicParameters("+
                               "null,null,null,null,null,null)");
            HighResolutionTime starttime = null;

            period = null;
            cost = null;
            deadline = null;

            AsyncEventHandler overrunHandler = null;
            AsyncEventHandler missHandler = null;
            pp = new PeriodicParameters(starttime, period, cost, deadline,
                                        overrunHandler, missHandler);
            if( !(pp instanceof PeriodicParameters && pp instanceof
                  ReleaseParameters) )
                throw new Exception("Return object is not instanceof "+
                                    "PeriodicParameters nor "+
                                    "ReleaseParameters");
        } catch (Exception e) {
            System.out.println("PeriodicParametersTest: PeriodicParameters("+
                               "null,null,null,null,null,null) failed");
            Tests.fail("new PeriodicParameters(null,null,null,null,null,null)",
                       e);
        }

        /* Subtest 2:
        ** Constructor "public PeriodicParameters(HighResolutionTime start,
        ** RelativeTime period, RelativeTime cost, RelativeTime deadline,
        ** AsyncEventHandler overrunHandler, AsyncEventHandler missHandler)"
        ** where given parameters have value
        */
        Tests.increment();
        start = new RelativeTime(1024L, 100);
        period = new RelativeTime(1000L, 0);
        cost = new RelativeTime(900L, 0);
        deadline = new RelativeTime(1000L, 0);
        AEventHandler overrunHandler = new AEventHandler();
        AEventHandler missHandler = new AEventHandler();
        try {
            System.out.println("PeriodicParametersTest: PeriodicParameters("+
                               "HighResolutionTime,RelativeTime,RelativeTime,"+
                               "RelativeTime,null,null)");
            pp = new PeriodicParameters(start, period, cost, deadline,
                                        overrunHandler, missHandler);
            if( !(pp instanceof PeriodicParameters && pp instanceof
                  ReleaseParameters) )
                throw new Exception("Return object is not instanceof "+
                                    "PeriodicParameters nor "+
                                    "ReleaseParameters");
        } catch (Exception e) {
            System.out.println("PeriodicParametersTest: PeriodicParameters("+
                               "HighResolutionTime,RelativeTime,RelativeTime,"+
                               "RelativeTime,null,null) failed");
            Tests.fail("new PeriodicParameters(start,period,cost,deadline,"+
                       "null,null)",e);
        }

        /* Subtest 3:
        ** Method "public RelativeTime getPeriod()"
        */
        Tests.increment();
        try {
            System.out.println("PeriodicParametersTest: getPeriod()");
            newperiod = new RelativeTime(2000, 0);
            pp.setPeriod(newperiod);
            RelativeTime rt = pp.getPeriod();
            if (rt.equals(newperiod)==false)
                {
                    throw new Exception("Period was not set correctly");
                }
        } catch (Exception e) {
            System.out.println("PeriodicParametersTest: getPeriod() failed");
            Tests.fail("pp.getPeriod()",e);
        }

        /* Subtest 4:
        ** Method "public HighResolutionTime getStart()"
        ** Note: In this test, I know that the return type should be
        ** also an instanceof RelativeTime because that is what I used
        ** in the constructor of Subtest 2:
        */
        Tests.increment();
        try {
            System.out.println("PeriodicParametersTest: getStart()");
            o = pp.getStart();
            if( !(o instanceof HighResolutionTime && o instanceof
                  RelativeTime) )
                throw new Exception("Return object is not instanceof "+
                                    "HighResolution nor RelativeTime");
            RelativeTime rt = (RelativeTime)o;
            if (rt.equals(start)==false)
                {
                    throw new Exception("Start time was not set correctly");
                }

        } catch (Exception e) {
            System.out.println("PeriodicParametersTest: getStart() failed");
            Tests.fail("pp.getStart()",e);
        }

        /*
        ** Subtest 5:
        ** Method "public void setPeriod(RelativeTime period)"
        */
        Tests.increment();
        try {
            System.out.println("PeriodicParametersTest: setPeriod(Relative"+
                               "Time)");
            RelativeTime rperiod = new RelativeTime(2000L,100);
            pp.setPeriod(rperiod);
            RelativeTime rt = pp.getPeriod();
            if (rt.equals(rperiod)==false) {
                throw new Exception("Period was not set correctly");
            }

        } catch (Exception e) {
            System.out.println("PeriodicParametersTest: setPeriod(Relative"+
                               "Time) failed");
            Tests.fail("pp.setPeriod(rperiod)",e);
        }

        /* Subtest 6:
        ** Method "public void setStart(HighResolutionTime start)"
        */

        Tests.increment();
        try {
            System.out.println("PeriodicParametersTest: setStart(High"+
                               "ResolutionTime)");
            AbsoluteTime astart = new AbsoluteTime(new java.util.Date());
            astart.add(10223L,100);
            pp.setStart(astart);
            HighResolutionTime st = pp.getStart();
            if (st.equals(astart)==false)
                throw new Exception("Start was not set correctly");
        } catch (Exception e) {
            System.out.println("PeriodicParametersTest: setStart(High"+
                               "ResolutionTime) failed");
            Tests.fail("pp.setStart(astart)",e);
        }

        /* Subtest 7:
        ** Method "public boolean setIfFeasible(RT period, RT cost)"
        */
        Tests.increment();
        try {
            System.out.println("PeriodicParametersTest: setIfFeasible(RT,RT)");
            {
                newcost     = new RelativeTime( 500, 0);
                newdeadline = new RelativeTime(1000, 0);
                boolean b = pp.setIfFeasible(newcost, newdeadline);
            }
        } catch (Exception e) {
            System.out.println("PeriodicParametersTest: setIfFeasible(RT,RT) failed");
            Tests.fail("pp.setIfFeasible(RT,RT)",e);
        }

        /* Subtest 8:
        ** Method "public boolean setIfFeasible(RT period, RT cost, RT deadline)"
        */
        Tests.increment();

        newperiod   = new RelativeTime(2000, 0);
        newcost     = new RelativeTime( 500, 0);
        newdeadline = new RelativeTime(1000, 0);
        try {

            System.out.println("PeriodicParametersTest: setIfFeasible(RT,RT,RT)");
            {
                boolean b = pp.setIfFeasible(newperiod, newcost,
                                             newdeadline);
            }
        } catch (Exception e) {
            System.out.println("PeriodicParametersTest: setIfFeasible(RT,RT,RT) failed");
            Tests.fail("pp.setIfFeasible(RT,RT,RT)",e);
        }

        Tests.printSubTestReportTotals("PeriodicParametersTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
