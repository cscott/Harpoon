//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              MemoryParametersTest

Subtest 1:
        "public static final long NO_MAX"

Subtest 2:
        "public MemoryParameters(long maxMemoryArea, long maxImmortal)"
        where each parameter is > 0"

Subtest 3:
        "public MemoryParameters(long maxMemoryArea, long maxImmortal)"
        where each parameter is = 0"

Subtest 4:
        "public MemoryParameters(long maxMemoryArea, long maxImmortal)"
        where each parameter is = NO_MAX"

Subtest 5:
        "public MemoryParameters(long maxMemoryArea, long maxImmortal, long
        allocationRate)" where each parameter is > 0"

Subtest 6:
        "public MemoryParameters(long maxMemoryArea, long maxImmortal, long
        allocationRate)" where each parameter is = 0"

Subtest 7:
        "public MemoryParameters(long maxMemoryArea, long maxImmortal, long
        allocationRate)" where each parameter is = NO_MAX"

Subtest 8:
        "public long getAllocationRate()"

Subtest 9:
        "public void setAllocationRate(long rate)"

Subtest 10:
        "public long getMaxMemoryArea()";

Subtest 11:
        "public boolean setMaxMemoryArea(long maximum)"

Subtest 12:
        "public long getMaxImmortal()"

Subtest 13:
        "public boolean setMaxImmortal()"

Subtest 14:
        "public boolean setAllocationRateIfFeasible(long allocationRate)"

*/

import javax.realtime.*;

public class MemoryParametersTest
{

    public static void run()
    {
        MemoryParameters mp = null;
        Object o = null;

        Tests.newTest("MemoryParametersTest");

        /* Subtest 1:
        ** Field "public static final long NO_MAX"
        */
        Tests.increment();
        try {
            System.out.println("MemoryParametersTest: NO_MAX");
            long x = MemoryParameters.NO_MAX;
            if (x != -1)
                throw new Exception("NO_MAX not set to -1");

        } catch (Exception e) {
            System.out.println("MemoryParametersTest: NO_MAX failed");
            Tests.fail("MemoryParameters.NO_MAX",e);
        }

        /* Subtest 2:
        ** Constructor "public MemoryParameters(long maxMemoryArea,
        ** long maxImmortal)" where each parameter is > 0"
        */
        Tests.increment();
        try {
            System.out.println("MemoryParametersTest: MemoryParameters(long,"+
                               "long)");
            long maxMemoryArea = 500;
            long maxImmortal = 300;
            o = new MemoryParameters(maxMemoryArea, maxImmortal);
            if( !(o instanceof MemoryParameters) )
                throw new Exception("Return object is not instaneof "+
                                    "MemoryParameters");
        } catch (Exception e) {
            System.out.println("MemoryParametersTest: MemoryParameters(long,"+
                               "long) failed");
            Tests.fail("new MemoryParameters(500, 300)",e);
        }

        /* Subtest 3:
        ** Constructor "public MemoryParameters(long maxMemoryArea,
        ** long maxImmortal)" where each parameter is = 0"
        */
        Tests.increment();
        try {
            System.out.println("MemoryParametersTest: MemoryParameters(long,"+
                               "long)");
            long maxMemoryArea = 0;
            long maxImmortal = 0;
            o = new MemoryParameters(maxMemoryArea, maxImmortal);
            if( !(o instanceof MemoryParameters) )
                throw new Exception("Return object is not instaneof "+
                                    "MemoryParameters");
        } catch (Exception e) {
            System.out.println("MemoryParametersTest: MemoryParameters(long,"+
                               "long) failed");
            Tests.fail("new MemoryParameters(0, 0)",e);
        }

        /* Subtest 4:
        ** Constructor "public MemoryParameters(long maxMemoryArea,
        long maxImmortal)" where each parameter is = NO_MAX"
        */
        Tests.increment();
        try {
            System.out.println("MemoryParametersTest: MemoryParameters(long,"+
                               "long)");
            long maxMemoryArea = MemoryParameters.NO_MAX;
            long maxImmortal = MemoryParameters.NO_MAX;
            o = new MemoryParameters(maxMemoryArea, maxImmortal);
            if( !(o instanceof MemoryParameters) )
                throw new Exception("Return object is not instaneof "+
                                    "MemoryParameters");
        } catch (Exception e) {
            System.out.println("MemoryParametersTest: MemoryParameters(long,"+
                               "long) failed");
            Tests.fail("new MemoryParameters(NO_MAX, NO_MAX)",e);
        }

        /* Subtest 5:
        ** Constructor "public MemoryParameters(long maxMemoryArea,
        ** long maxImmortal, long allocationRate)" where each parameter is > 0"
        */
        Tests.increment();
        try {
            System.out.println("MemoryParametersTest: MemoryParameters(long,"+
                               "long,long)");
            long maxMemoryArea = 500;
            long maxImmortal = 300;
            long allocationRate = 10;
            o = new MemoryParameters(maxMemoryArea, maxImmortal,
                                     allocationRate);
            if( !(o instanceof MemoryParameters) )
                throw new Exception("Return object is not instanceof "+
                                    "MemoryParameters");
        } catch (Exception e) {
            System.out.println("MemoryParametersTest: MemoryParameters(long,"+
                               "long,long) failed");
            Tests.fail("new MemoryParameters(500,300,10)",e);
        }

        /* Subtest 6:
        ** Constructor "public MemoryParameters(long maxMemoryArea,
        long maxImmortal, long allocationRate)" where each parameter is = 0"
        */
        Tests.increment();
        try {
            System.out.println("MemoryParametersTest: MemoryParameters(long,"+
                               "long,long)");
            long maxMemoryArea = 0;
            long maxImmortal = 0;
            long allocationRate = 0;
            o = new MemoryParameters(maxMemoryArea, maxImmortal,
                                     allocationRate);
            if( !(o instanceof MemoryParameters) )
                throw new Exception("Return object is not instanceof "+
                                    "MemoryParameters");
        } catch (Exception e) {
            System.out.println("MemoryParametersTest: MemoryParameters(long,"+
                               "long,long) failed");
            Tests.fail("new MemoryParameters(0,0,0)",e);
        }

        /* Subtest 7:
        ** Constructor "public MemoryParameters(long maxMemoryArea,
        ** long maxImmortal, long allocationRate)" where each parameter is =
        ** NO_MAX"
        */
        Tests.increment();
        try {
            System.out.println("MemoryParametersTest: MemoryParameters(long,"+
                               "long,long)");
            long maxMemoryArea = MemoryParameters.NO_MAX;
            long maxImmortal = MemoryParameters.NO_MAX;
            long allocationRate = MemoryParameters.NO_MAX;
            o = new MemoryParameters(maxMemoryArea, maxImmortal,
                                     allocationRate);
            if( !(o instanceof MemoryParameters) )
                throw new Exception("Return object is not instanceof "+
                                    "MemoryParameters");
        } catch (Exception e) {
            System.out.println("MemoryParametersTest: MemoryParameters(long,"+
                               "long,long) failed");
            Tests.fail("new MemoryParameters(NO_MAX,NO_MAX,NO_MAX)",e);
        }

        /* Subtest 8:
        ** Method "public long getAllocationRate()"
        */
        Tests.increment();
        try {
            System.out.println("MemoryParametersTest: getAllocationRate()");
            long allocationRate = 10L;
            mp = new MemoryParameters(1000L, 1000L, allocationRate);
            long oar=mp.getAllocationRate();
            if (oar != allocationRate)
                throw new Exception("Unexcpected Allocation Rate");
        } catch (Exception e) {
            System.out.println("MemoryParametersTest: getAllocationRate() "+
                               "failed");
            Tests.fail("mp.getAllocationRate()",e);
        }

        /* Subtest 9:
        ** Method "public void setAllocationRate(long rate)"
        */
        Tests.increment();
        try {
            System.out.println("MemoryParametersTest: setAllocationRate("+
                               "long)");
            long newAllocationRate = 30L;
            mp.setAllocationRate(newAllocationRate);
            long ar = mp.getAllocationRate();
            if (ar != newAllocationRate)
                throw new Exception("Allocation Rate not set properly");
        } catch (Exception e) {
            System.out.println("MemoryParametersTest: setAllocationRate("+
                               "long) failed");
            Tests.fail("mp.setAllocationRate(30L)",e);
        }

        /* Subtest 10:
        ** Method "public long getMaxMemoryArea()";
        */
        Tests.increment();
        try {
            System.out.println("MemoryParametersTest: getMaxMemoryArea()");
            long MaxMemoryArea=600L;
            mp = new MemoryParameters(MaxMemoryArea,1000L);
            long mma=mp.getMaxMemoryArea();
            if (mma != MaxMemoryArea)
                throw new Exception("Unexpected MaxMemoryArea");
        } catch (Exception e) {
            System.out.println("MemoryParametersTest: getMaxMemoryArea() "+
                               "failed");
            Tests.fail("mp.getMaxMemoryArea()",e);
        }

        /* Subtest 11:
        ** Method "public boolean setMaxMemoryArea(long maximum)"
        */
        Tests.increment();
        try {
            System.out.println("MemoryParametersTest: setMaxMemoryArea(long)");
            long MaxMemoryArea=900L;
            boolean smma=mp.setMaxMemoryArea(MaxMemoryArea);
            long mma = mp.getMaxMemoryArea();
            if ( (smma == true) && (mma != MaxMemoryArea))
                throw new Exception("MaxMemoryArea not set properly");
        } catch (Exception e) {
            System.out.println("MemoryParametersTest: setMaxMemoryArea(long "+
                               ") failed");
            Tests.fail("mp.setMaxMemoryArea(900L)",e);
        }

        /* Subtest 12:
        ** Method "public long getMaxImmortal()"
        */
        Tests.increment();
        try {
            System.out.println("MemoryParametersTest: getMaxImmortal()");
            long MaxImmortal=600L;
            mp = new MemoryParameters(1000L,MaxImmortal);
            long mi=mp.getMaxImmortal();
            if (mi != MaxImmortal)
                throw new Exception("Unexpected MaxImmortal");
            System.out.println("MemoryParametersTest: ");
        } catch (Exception e) {
            System.out.println("MemoryParametersTest:getMaxImmortal() failed");
            Tests.fail("mp.getMaxImmortal()",e);
        }

        /* Subtest 13:
        ** Method "public boolean setMaxImmortal()"
        */
        Tests.increment();
        try {
            System.out.println("MemoryParametersTest: setMaxImmortal(long)");
            long MaxImmortal=900L;
            boolean smi=mp.setMaxImmortal(MaxImmortal);
            long mi = mp.getMaxImmortal();
            if ((smi == true) && (mi != MaxImmortal))
                throw new Exception("MaxImmortal not set properly");
        } catch (Exception e) {
            System.out.println("MemoryParametersTest: setMaxImmortal(long) "+
                               "failed");
            Tests.fail("mp.setMaxImmortal(900L)",e);
        }

        /* Subtest 14:
        ** Method "public boolean setAllocationRateIfFeasible(long allocationRate)"
        */
        Tests.increment();
        try {
            System.out.println("MemoryParametersTest: "+
                               "setAllocationRateIfFeasible(long)");
            long allocationRate = 10L;
            mp = new MemoryParameters(1000L, 1000L, allocationRate);
            boolean b=mp.setAllocationRateIfFeasible(20);
        } catch (Exception e) {
            System.out.println("MemoryParametersTest: "+
                               "setAllocationRateIfFeasible(long)"+
                               " failed");
            Tests.fail("mp.setAllocationRateIfFeasible(long)",e);
        }

        Tests.printSubTestReportTotals("MemoryParametersTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
