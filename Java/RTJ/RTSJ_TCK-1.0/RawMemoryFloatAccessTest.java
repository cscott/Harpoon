//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              RawMemoryFloatAccessTest

Subtest 1
        "public RawMemoryFloatAccess(java.lang.Object
        type, long size)

Subtest 2
        "public RawMemoryFloatAccess(java.lang.Object
        type, long base, long size)"

Subtest 3
        "public void setDouble( long offset, double value)" and
        "public double getDouble( long offset)"

Subtest 4
        "public void setDoubles( long offset, double[] doubles, int low, int
        number)" and
        "public void getDoubles( long offset, double[] doubles, int low, int
        number))"

Subtest 5
        "public void setFloat( long offset, float value)" and
        "public float getFloat( long offset)"

Subtest 6
        "public void setFloats( long offset, float[] floats, int low, int
        number)" and
        "public void getFloats( long offset, float[] floats, int low, int
        number))"

*/

import javax.realtime.*;
import com.timesys.*;

public class RawMemoryFloatAccessTest
{

    private static final long BASEADDR = 95*1024*1024;
    private static final long SIZE = 32*4096;
    
    public static void run()
    {
        RawMemoryFloatAccess rmfa = null;
        Object o = null;
        long base = BASEADDR;

        // constructors protected


        Tests.newTest("RawMemoryFloatAccessTest");

        Object type = new Object(); 

        DefaultPhysicalMemoryFilter filter = null;
        /* SETUP for PhysicalMemoryManager */
        try
        {
            filter = new DefaultPhysicalMemoryFilter(BASEADDR, SIZE);
            PhysicalMemoryManager.registerFilter(type,filter);
        }
        catch(Exception e)
        {
            System.out.println("An exception occurred while trying ton register filter");
            e.printStackTrace();
            Tests.fail("RawMemoryFloatAccessTest");
            return;
        }


        /* Subtest 1
        ** Constructor "public RawMemoryFloatAccess(Object type, long size)
        */
        Tests.increment();

        try {
            System.out.println("RawMemoryFloatAccessTest: createFloatAccess("+
                               "Object,long)");
            long size = 4096;
            o = new RawMemoryFloatAccess(type, size);
            base+=size;
            if( !(o instanceof RawMemoryFloatAccess) )
                throw new Exception("Return object is not instanceof "+
                                    "RawMemoryFloatAccess");
        } catch (Exception e) {
            System.out.println("RawMemoryFloatAccessTest: createFloatAccess("+
                               "Object,long) failed");
            Tests.fail("RawMemoryFloatAccess.createFloatAccess()",e);
        }

        /* Subtest 2
        ** Constructor "public RawMemoryFloatAccess(Object type,
        **                                          long base, long size)"
        */
        Tests.increment();
        try {
            System.out.println("RawMemoryFloatAccessTest: createFloatAccess("+
                               "Object,long,long)");
            long size = 4096;
            o = new RawMemoryFloatAccess(type, base, size);
            base+=size;
            if( !(o instanceof RawMemoryFloatAccess) )
                throw new Exception("Return object is not instanceof "+
                                    "RawMemoryFloatAccess");
        } catch (Exception e) {
            System.out.println("RawMemoryFloatAccessTest: createFloatAccess("+
                               "Object,long,long) failed");
            Tests.fail("RawMemoryFloatAccess.createFloatAccess()",e);
        }

        /* Subtest 3
        ** Methods "public void setDouble( long offset, double value)" and
        **         "public double getDouble( long offset)"
        */
        Tests.increment();
        try {
            System.out.println("RawMemoryFloatAccessTest: setDouble(long,"+
                               "double), getDouble(long)");
            long size = 4096;
            rmfa = new RawMemoryFloatAccess(type, base, size);
            base+=size;
            rmfa.setDouble(0L,(double)112.5);
            if(rmfa.getDouble(0L) != 112.5)
                throw new Exception("Double not set properly");
        } catch (Exception e) {
            System.out.println("RawMemoryFloatAccessTest: setDouble(long,"+
                               "double), getDouble(long) failed");
            Tests.fail("rmfa.setDouble() and rmfa.getDouble()",e);
        }

        /* Subtest 4
        ** Methods "public void setDoubles( long offset, double[] doubles,
        ** int low, int number)" and
        **         "public void getDoubles( long offset, double[] doubles,
        ** int low, int number))"
        */
        Tests.increment();
        try {
            System.out.println("RawMemoryFloatAccessTest: setDoubles(long,"+
                               "double[],int,int), getDoubles(long,double[],"+
                               "int,int)");
            double[] doubles = new double[4];
            doubles[0] = 1;
            doubles[1] = 2;
            rmfa.setDoubles(5L, doubles, 0, 2);
          rmfa.getDoubles(5L, doubles, 2, 2);

          if (!(doubles[0]==doubles[2] && doubles[1]==doubles[3]))
              throw new Exception("Doubles were not set properly");
        } catch (Exception e) {
            System.out.println("RawMemoryFloatAccessTest: setDoubles(long,"+
                               "double[],int,int), getDoubles(long,double[],"+
                               "int,int) failed");
            Tests.fail("rmfa.setDoubles() and rmfa.getDoubles()",e);
        }
        /* Subtest 5
        ** Methods "public void setFloat( long offset, float value)" and
        **         "public float getFloat( long offset)"
        */
        Tests.increment();
        try {
            System.out.println("RawMemoryFloatAccessTest: setFloat(long,"+
                               "float), getFloat(long)");
            rmfa.setFloat(0L,(float)124.5);
            if(rmfa.getFloat(0L) != 124.5)
                throw new Exception("Float not set properly");
        } catch (Exception e) {
            System.out.println("RawMemoryFloatAccessTest: setFloat(long,"+
                               "float), getFloat(long) failed");
            Tests.fail("rmfa.setFloat() and rmfa.getFloat()",e);
        }
        /* Subtest 6
        ** Methods "public void setFloats( long offset, float[] floats, int
        ** low, int number)" and
        **         "public void getFloats( long offset, float[] floats, int
        ** low, int number))"
        */
        Tests.increment();
        try {
            System.out.println("RawMemoryFloatAccessTest: setFloats(long,"+
                               "float[],int,int), getFloats(long,float[],int,"+
                               "int)");
            float[] floats = new float[4];
            floats[0] = 100;
            floats[1] = 200;
            rmfa.setFloats(5L, floats, 0, 2);
            rmfa.getFloats(5L, floats, 2, 2);

            System.out.println("floats[0]=" + floats[0]);
            System.out.println("floats[1]=" + floats[1]);
            System.out.println("floats[2]=" + floats[2]);
            System.out.println("floats[3]=" + floats[3]);
          if (!(floats[0]==floats[2] && floats[1]==floats[3]))
              throw new Exception("Floats were not set properly");
        } catch (Exception e) {
            System.out.println("RawMemoryFloatAccessTest: setFloats(long,"+
                               "float[],int,int), getFloats(long,float[],int,"+
                               "int) failed");
            Tests.fail("rmfa.setFloats() and rmfa.getFloats()",e);
        }
        Tests.printSubTestReportTotals("RawMemoryFloatAccessTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
