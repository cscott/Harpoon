//package javax.realtime.calltests;

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              RawMemoryAccessTest

Subtest 1
        "public RawMemoryAccess(java.lang.Object type, long size)

Subtest 2
        "public RawMemoryAccess(java.lang.Object type, long base,
        long size)"

Subtest 3
        "public void setByte( long offset, byte value)" and
        "public byte getByte( long offset)"

Subtest 4
        "public void setBytes( long offset, byte[] bytes, int low, int number)"
        and
        "public void getBytes( long offset, byte[] bytes, int low, int number))
        "

Subtest 5
        "public void setInt( long offset, int value)" and
        "public int getInt( long offset)"

Subtest 6
        "public void setInts( long offset, int[] ints, int low, int number)"
        and
        "public void getInts( long offset, int[] ints, int low, int number))"

Subtest 7
        "public void setLong( long offset, long value)" and
        "public byte getLong( long offset)"

Subtest 8
        "public void setLongs( long offset, long[] longs, int low, int number)"
        and
        "public void getLongs( long offset, long[] longs, int low, int number))
        "

Subtest 9
        "public void setShort( long offset, short value)" and
        "public byte getShort( long offset)"

Subtest 10
        "public void setShorts( long offset, short[] shorts, int low, int
        number)"
        "public void getShorts( long offset, short[] shorts, int low, int
        number))"

Subtest 11
        "public long getMappedAddress()"

Subtest 12
        "public long map()"

Subtest 13
        "public long map(long base)"

Subtest 14
        "public long map(long base, long size)"
*/

import javax.realtime.*;
import com.timesys.*;

public class RawMemoryAccessTest
{

    private static final long BASEADDR = 94*1024*1024;
    private static final long SIZE = 32*4096;
    
    
    public static void run()
    {
        byte[] bytes=new byte[10];
        RawMemoryAccess rma = null;
        Object o = null;
        long base = BASEADDR;

        Tests.newTest("RawMemoryAccessTest");

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
        ** Constructor "public RawMemoryAccess(java.lang.Object type,
        **                                     long size)
        */
        Tests.increment();

        try {
            System.out.println("RawMemoryAccessTest: RawMemoryAccess(Object,long)");
            long size = 4096;
            o = new RawMemoryAccess(type, size);
            base+=size;
            if( !(o instanceof RawMemoryAccess) )
                throw new Exception("Return object is not instanceof "+
                                    "RawMemoryAccess");
        } catch (Exception e) {
            System.out.println("RawMemoryAccessTest: create(Object,long) "+
                               "failed");
            Tests.fail("RawMemoryAccess.create()",e);
        }

        /* Subtest 2
        ** Constructor "public RawMemoryAccess RawMemoryAccess(Object type,
        **                                                     long base,
        **                                                     long size)"
        */
        Tests.increment();
        try {
            System.out.println("RawMemoryAccessTest: RawMemoryAccessTest(Object,long,"+
                               "long)");
            long size = 4096;
            o = new RawMemoryAccess(type, base, size);
            base+=size;
            if( !(o instanceof RawMemoryAccess) )
                throw new Exception("Return object is not instanceof "+
                                    "RawMemoryAccess");

        } catch (Exception e) {
            System.out.println("RawMemoryAccessTest: create(Object,long,"+
                               "long) failed");
            Tests.fail("RawMemoryAccess.create()",e);
        }

        /* Subtest 3
        ** Methods "public void setByte( long offset, byte value)" and
        **         "public byte getByte( long offset)"
        */
        Tests.increment();
        try {
            System.out.println("RawMemoryAccessTest: setByte(long,byte), "+
                               "getByte(long)");
            long size = 4096;
            rma = new RawMemoryAccess(type, base, size);
            base+=size;
            rma.setByte(0L,(byte)112);
            if(rma.getByte(0L) != 112)
                throw new Exception("Byte not set properly");
        } catch (Exception e) {
            System.out.println("RawMemoryAccessTest: setByte(long,byte), "+
                               "getByte(long) failed");
            Tests.fail("rma.setByte() and rma.getByte()",e);
        }

        /* Subtest 4
        ** Methods "public void setBytes( long offset, byte[] bytes, int low,
        ** int number)" and "public void getBytes( long offset, byte[] bytes,
        ** int low, int number))"
        */
        Tests.increment();
        try {
            System.out.println("RawMemoryAccessTest: setBytes(long,byte[],"+
                               "int,int), getBytes(long,byte[],int,int)");
            bytes[0] = 1;
            bytes[1] = 2;
            rma.setBytes(5L, bytes, 0, 2);
            rma.getBytes(5L, bytes, 2, 2);

            if (!(bytes[0]==bytes[2] && bytes[1]==bytes[3]))
                throw new Exception("Bytes were not set properly");
        } catch (Exception e) {
            System.out.println("RawMemoryAccessTest: setBytes(long,byte[],"+
                               "int,int), getBytes(long,byte[],int,int) "+
                               "failed");
            Tests.fail("rma.setBytes() and rma.getBytes()",e);
        }

        /* Subtest 5
        ** Methods "public void setInt( long offset, int value)" and
        **         "public int getInt( long offset)"
        */
        Tests.increment();
        try {
            System.out.println("RawMemoryAccessTest: setInt(long,int) ,"+
                               "getInt(long)");
            rma.setInt(0L,(int)124);
            if(rma.getInt(0L) != 124)
                throw new Exception("Int not set properly");
        } catch (Exception e) {
            System.out.println("RawMemoryAccessTest: setInt(long,int) ,"+
                               "getInt(long) failed");
            Tests.fail("rma.setInt() and rma.getInt()",e);
        }

        /* Subtest 6
        ** Methods "public void setInts( long offset, int[] ints, int low,
        ** int number)" and
        **         "public void getInts( long offset, int[] ints, int low, int
        ** number))"
        */

        Tests.increment();
        try {
            System.out.println("RawMemoryAccessTest: setInts(long,int[],int,"+
                               "int), getInts(long,int[],int,int)");
            int[] ints = new int[4];
            ints[0] = 100;
            ints[1] = 200;
            rma.setInts(5L, ints, 0, 2);
            rma.getInts(5L, ints, 2, 2);

            if (!(ints[0]==ints[2] && ints[1]==ints[3]))
          throw new Exception("Ints were not set properly");
        } catch (Exception e) {
            System.out.println("RawMemoryAccessTest: setInts(long,int[],int,"+
                               "int), getInts(long,int[],int,int) failed");
            Tests.fail("rma.setInts() and rma.getInts()",e);
        }

        /* Subtest 7
        ** Methods "public void setLong( long offset, long value)" and
        **         "public byte getLong( long offset)"
        */

        Tests.increment();
        try {
            System.out.println("RawMemoryAccessTest: setLong(long,long), "+
                               "getLong(long)");
            rma.setLong(0L,1024L);
            if(rma.getLong(0L) != 1024L)
                throw new Exception("Long not set properly");
        } catch (Exception e) {
            System.out.println("RawMemoryAccessTest: setLong(long,long), "+
                               "getLong(long) failed");
            Tests.fail("rma.setLong() and rma.getLong()",e);
        }

        /* Subtest 8
        ** Methods "public void setLongs( long offset, long[] longs, int low,
        ** int number)" and
        **         "public void getLongs( long offset, long[] longs, int low,
        ** int number))"
        */

        Tests.increment();
        try {
            System.out.println("RawMemoryAccessTest: setLongs(long,long[],"+
                               "int,int), getLongs(long,long[],int,int)");
            long[] longs = new long[4];
            longs[0] = 1000;
            longs[1] = 2000;
            rma.setLongs(5L, longs, 0, 2);
            rma.getLongs(5L, longs, 2, 2);

            if (!(longs[0]==longs[2] && longs[1]==longs[3]))
                throw new Exception("Longs were not set properly");
        } catch (Exception e) {
            System.out.println("RawMemoryAccessTest: setLongs(long,long[],"+
                               "int,int), getLongs(long,long[],int,int) "+
                               "failed");
            Tests.fail("rma.setLongs() and rma.getLongs()",e);
        }
        /* Subtest 9
        ** Methods "public void setShort( long offset, short value)" and
        **         "public byte getShort( long offset)"
        */
        Tests.increment();
        try {
            System.out.println("RawMemoryAccessTest: setShort(long,short), "+
                               "getShort(long)");
            rma.setShort(0L,(short)500);
            if(rma.getShort(0L) != (short)500)
                throw new Exception("Short not set properly");
        } catch (Exception e) {
            System.out.println("RawMemoryAccessTest: setShort(long,short), "+
                               "getShort(long) failed");
            Tests.fail("rma.setShort() and rma.getShort()",e);
        }

        /* Subtest 10
        ** Methods "public void setShorts( long offset, short[] shorts, int
        ** low, int number)" and
        **         "public void getShorts( long offset, short[] shorts, int
        ** low, int number))"
        */
        Tests.increment();
        try {
            System.out.println("RawMemoryAccessTest: setShorts(long,short[],"+
                               "int,int), getShorts(long,short[],int,int)");
            short[] shorts = new short[4];
            shorts[0] = 150;
            shorts[1] = 250;
            rma.setShorts(5L, shorts, 0, 2);
            rma.getShorts(5L, shorts, 2, 2);

            if (!(shorts[0]==shorts[2] && shorts[1]==shorts[3]))
                throw new Exception("Shorts were not set properly");
        } catch (Exception e) {
            System.out.println("RawMemoryAccessTest: setShorts(long,short[],"+
                               "int,int), getShorts(long,short[],int,int) "+
                               "failed");
            Tests.fail("rma.setShorts() and rma.getShorts()",e);
        }
        /* Subtest 11
        ** Method "public long getMappedAddress()"
        */
        Tests.increment();
        try {
            System.out.println("RawMemoryAccessTest: getMappedAddress()");
            long addr = rma.getMappedAddress();
        } catch (Exception e) {
            System.out.println("RawMemoryAccessTest: getMappedAddress() "+
                               "failed");
          Tests.fail("rma.getMappedAddress()",e);
        }

        /* Subtest 12
        ** Method "public long map()"
        */
        Tests.increment();
        try {
            System.out.println("RawMemoryAccessTest: map()");
            long lmap = rma.map();
        } catch (Exception e) {
            System.out.println("RawMemoryAccessTest: map() failed");
            Tests.fail("rma.map()",e);
        }

        /* Subtest 13
        ** Method "public long map(long base)"
        */
        Tests.increment();
        try {
            System.out.println("RawMemoryAccessTest: map(long)");
            long addr = rma.map(500L);
        } catch (Exception e) {
            System.out.println("RawMemoryAccessTest: map(long) failed");
            Tests.fail("rma.map(long base)",e);
        }
        /* Subtest 14
        ** Method "public long map(long base, long size)"
        */
        Tests.increment();
          try {
              System.out.println("RawMemoryAccessTest: map(long,long)");
              long addr = rma.map(500L,1000L);
          } catch (Exception e) {
              System.out.println("RawMemoryAccessTest: map(long,long) "+
                                 "failed");
              Tests.fail("rma.map(long base, long size)",e);
          }

          Tests.printSubTestReportTotals("RawMemoryAccessTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
