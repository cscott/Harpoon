//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                      ImportanceParametersTest

Subtest 1:
        "public ImportanceParameters(int priority, int importance)"

Subtest 2:
        "public int getImportance()"

Subtest 3:
        "public void setImportance(int importance)"

Subtest 4:
        "public java.lang.String toString()"
*/

import javax.realtime.*;

public class ImportanceParametersTest
{

    public static void run()
    {
        ImportanceParameters ip = null;
        Object o = null;
        Tests.newTest("ImportanceParametersTest");

        /* Subtest 1:
        ** Constructor "public ImportanceParameters(int priority,
        ** int importance)"
        */
        Tests.increment();
        try {
            System.out.println("ImportanceParametersTest: Importance"+
                               "Parameters(int,int)");
            int priority = 10;
            int importance = 12;
            ip = new ImportanceParameters(priority, importance);
            if( !(ip instanceof ImportanceParameters && ip instanceof
                  PriorityParameters) )
                throw new Exception("Return object is not instanceof "+
                                    "ImportanceParameters nor "+
                                    "PriorityParameters");
        } catch (Exception e) {
            System.out.println("ImportanceParametersTest: Importance"+
                               "Parameters(int,int)");
            Tests.fail("new ImportanceParameters(priority, importance)",e);
        }

        /* Subtest 2:
        ** Method "public int getImportance()"
        */
        Tests.increment();
        try {
            System.out.println("ImportanceParametersTest: getImportance()");
            int importance = ip.getImportance();
            if( importance != 12 )
                throw new Exception("Unexpected importance received");
        } catch (Exception e) {
            System.out.println("ImportanceParametersTest: getImportance() "+
                               "failed");
            Tests.fail("ip.getImportance()",e);
        }

        /* Subtest 3:
        ** Method "public void setImportance(int importance)"
        */
        Tests.increment();
        try {
            System.out.println("ImportanceParametersTest: setImportance()");
            int importance = 5;
            ip.setImportance(importance);
            if( ip.getImportance() != 5 )
                throw new Exception("Importance not set properly");
        } catch (Exception e) {
            System.out.println("ImportanceParametersTest: setImportance() "+
                               "failed");
            Tests.fail("ip.setImportance(importance)",e);
        }

        /* Subtest 4:
        ** Method "public java.lang.String toString()"
        */
        Tests.increment();
        try {
            System.out.println("ImportanceParametersTest: toString()");
            o = ip.toString();
            if( !(o instanceof String) )
                throw new Exception("Return object is not instanceof String");
        } catch (Exception e) {
            System.out.println("ImportanceParametersTest: toString() failed");
            Tests.fail("ip.toString()",e);
        }

        Tests.printSubTestReportTotals("ImportanceParametersTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
