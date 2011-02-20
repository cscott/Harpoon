//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*
                        RealtimeSystemTest

Subtest 1:
        "public static final byte BIG_ENDIAN"

Subtest 2:
        "public static final byte BYTE_ORDER"

Subtest 3:
        "public static final byte LITTLE_ENDIAN"

Subtest 4:
        "public static GarbageCollector currentGC()"

Subtest 5:
        "public static int getConcurrentLocksUsed();"

Subtest 6:
        "public static int getMaximumConcurrentLocks()"

Subtest 7:
        "public static RealtimeSecurity getSecurityManager()"

Subtest 8:
        "public static void setMaximumConcurrentLocks(int number)"

Subtest 9:
        "public static void setMaximumConcurrentLocks(int number, boolean hard)"

Subtest 10:
        "public static void setSecurityManager(RealtimeSecurity manager)"
*/

import javax.realtime.*;

public class RealtimeSystemTest
{

    public static void run()
    {

        Object o = null;
        Tests.newTest("RealtimeSystemTest");

        // Subtest 1:
        // Field "public static final byte BIG_ENDIAN"
        //
        Tests.increment();
        try {
            System.out.println("RealtimeSystemTest: BIG_ENDIAN");
            byte b = RealtimeSystem.BIG_ENDIAN;
        } catch (Exception e) {
            System.out.println("RealtimeSystemTest: BIG_ENDIAN failed");
            Tests.fail("RealtimeSystem.BIG_ENDIAN",e);
        }

        // Subtest 2:
        // Field "public static final byte BYTE_ORDER"
        //
        Tests.increment();
        try {
            System.out.println("RealtimeSystemTest: BYTE_ORDER");
            byte b = RealtimeSystem.BYTE_ORDER;
        } catch (Exception e) {
            System.out.println("RealtimeSystemTest: BYTE_ORDER failed");
            Tests.fail("RealtimeSystem.BYTE_ORDER",e);
        }

        // Subtest 3:
        // Field "public static final byte LITTLE_ENDIAN"
        //
        Tests.increment();
        try {
            System.out.println("RealtimeSystemTest: LITTLE_ENDIAN");
            byte b = RealtimeSystem.LITTLE_ENDIAN;
        } catch (Exception e) {
            System.out.println("RealtimeSystemTest: LITTLE_ENDIAN failed");
            Tests.fail("RealtimeSystem.LITTLE_ENDIAN");
        }

        // Subtest 4:
        // Method "public static GarbageCollector currentGC()"
        //
        Tests.increment();
        try {
            System.out.println("RealtimeSystemTest: currentGC()");
            o = RealtimeSystem.currentGC();
            if( !(o instanceof GarbageCollector) )
                throw new Exception("Return object is not "+
                                    "instanceof GarbageCollector");
        } catch (Exception e) {
            System.out.println("RealtimeSystemTest: currentGC() failed");
            Tests.fail("RealtimeSystem.currentGC()",e);
        }

        // Subtest 5:
        // Method "public int getConcurrentLocksUsed();"
        //
        Tests.increment();
        try {
            System.out.println("RealtimeSystemTest: getConcurrentLocksUsed()");
            int i = RealtimeSystem.getConcurrentLocksUsed();
            System.out.println("getConcurrentLocksUsed() => " + i);
        } catch (Exception e) {
            System.out.println("RealtimeSystemTest: getConcurrentLocksUsed() "+
                               "failed");
            Tests.fail("RealtimeSystem.getConcurrentLocksUsed()",e);
        }

        // Subtest 6:
        // Method "public int getMaximumConcurrentLocks()"
        //
        Tests.increment();
        try {
            System.out.println("RealtimeSystemTest: getMaximumConcurrent"+
                               "Locks()");
            int i = RealtimeSystem.getMaximumConcurrentLocks();
            System.out.println("getMaximumConcurrentLocks() => " + i);
        } catch (Exception e) {
            System.out.println("RealtimeSystemTest: getMaximumConcurrent"+
                               "Locks() failed");
            Tests.fail("RealtimeSystem.getMaximumConcurrentLocks()",e);
        }

        // Subtest 7:
        // Method "public static RealtimeSecurity getSecurityManager()"
        //
        Tests.increment();
        try {
            System.out.println("RealtimeSystemTest: getSecurityManager()");
            o = RealtimeSystem.getSecurityManager();
            if( !(o instanceof RealtimeSecurity) )
                throw new Exception("Return Object is not instanceof "+
                                    "RealtimeSecurity");
        } catch (Exception e) {
            System.out.println("RealtimeSystemTest: getSecurityManager() "+
                               "failed");
            Tests.fail("RealtimeSystem.getSecurityManager()",e);
        }

        // Subtest 8:
        // Method "public void setMaximumConcurrentLocks(int number)"
        //
        Tests.increment();
        try {
            System.out.println("RealtimeSystemTest: setMaximumConcurrent"+
                               "Locks(int)");
            //int mcl=10;
            int mcl=-1;
            RealtimeSystem.setMaximumConcurrentLocks(mcl);
            int mcl2 = RealtimeSystem.getMaximumConcurrentLocks();
            if (mcl != mcl2)
                throw new Exception("Maximum Concurrent Locks not set "+
                                    "properly");
        } catch (Exception e) {
            System.out.println("RealtimeSystemTest: setMaximumConcurrent"+
                               "Locks(int) failed");
            Tests.fail("RealtimeSystem.setMaximumConcurrentLocks(mcl)",e);
        }

        // Subtest 9:
        // Method "public void setMaximumConcurrentLocks(int number, boolean
        // hard)"
        //
        Tests.increment();
        try {
            System.out.println("RealtimeSystemTest: setMaximumConcurrent"+
                               "Locks(int,boolean)");
            //int mcl=20;
            int mcl=-1;
            RealtimeSystem.setMaximumConcurrentLocks(mcl, true);
            int mcl2 = RealtimeSystem.getMaximumConcurrentLocks();
            if (mcl != mcl2)
                throw new Exception("Maximum Concurrent Locks not set "+
                                    "properly");
        } catch (Exception e) {
            System.out.println("RealtimeSystemTest: setMaximumConcurrent"+
                               "Locks(int,boolean)");
            Tests.fail("RealtimeSystem.setMaximumConcurrentLocks(mcl, true)",
                       e);
        }

        // Subtest 10:
        // Method "public static void setSecurityManager(RealtimeSecurity
        // manager)"
        //
        Tests.increment();
        try {
            System.out.println("RealtimeSystemTest: setSecurityManager("+
                               "RealtimeSecurity)");
            RealtimeSecurity manager = new RealtimeSecurity();
            RealtimeSystem.setSecurityManager(manager);
        } catch (Exception e) {
            System.out.println("RealtimeSystemTest: setSecurityManager("+
                               "RealtimeSecurity) failed");
            Tests.fail("RealtimeSystem.setSecurityManager(manager)",e);
        }
        Tests.printSubTestReportTotals("RealtimeSystemTest");

    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
