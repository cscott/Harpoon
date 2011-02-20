package javax.realtime.memtests;


/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1999, 2000  All Rights Reserved
 */

import javax.realtime.*;

public class RawMemTest 
{

    final static int size = 1024;
    static RawMemoryAccess rma1;

    /**
     * Starts the application.
     * @param args an array of command-line arguments
    */
    public static void main(java.lang.String[] args) 
    {
	// Insert code to start the application here.
	rma1 = (RawMemoryAccess)DefaultPhysicalMemoryFactory.instance().create("", true, 4096L*10240L, size);
	rma1.map();
	byteTests();
	shortTests();
	intTests();
	longTests();
	exceptionTests();	
	subregionTests();
	threadTests();
	rma1.unmap();
	multipleTests();
	
    }

    public static byte byteFunc (long i) 
    {
	return (byte)((i*i + i) * ((i % 3 == 0) ? 1 : -1));
    }
    public static short shortFunc (long i) 
    {
	return (short)((i % 5 == 0) ? -i*i : i);
    }
    public static int intFunc (long i) 
    {
	return (int)(i*i*i + i);
    }
    public static long longFunc (long i) 
    {
	return (long)((i % 2 == 0) ? (i*i*Integer.MAX_VALUE) : -(i*i*Integer.MAX_VALUE));
    }

    private static void byteTests () 
    {
	final int byteLength = 1;
	for (long i = 0; i < size; i += byteLength) 
	{
            try
	    {
	        rma1.setByte(i, byteFunc(i));
			
	    } catch (OffsetOutOfBoundsException e) 
	    {
	    } 
	}
	for (long i = 0; i < size; i += byteLength) 
	{
	    try 
	    {
	        byte result;
			
		result = rma1.getByte(i);
		if (result != byteFunc(i))
		    error("Bad byte found in rma1.  Pos: " + i + " wanted: " + byteFunc(i) + " but got: " + result);
			
	    } catch (OffsetOutOfBoundsException e)
	    {
	    }
		
	}
	System.out.println ("Basic byte tests passed");

	// getBytes tests
	byte[] ba = new byte[size - 32];
	
	try 
	{
	    rma1.getBytes(16L, ba, 0, size - 32);
	} catch (OffsetOutOfBoundsException e)
	{
	}
	
	for (int i = 0; i < size - 32; i++) 
        {
	    if (ba[i] != byteFunc(i + 16))
	        error ("Bad byte found in first read array pos: " + i);
	}
	ba = new byte[size];
	for (int i = 0; i < size; i++) 
        {
	    ba[i] = -1;
	}
	
	try 
	{
	    rma1.getBytes(size / 2, ba, size / 2, size - (size/2));
	} catch (OffsetOutOfBoundsException e)
	{
	}
	
	for (int i = 0; i < size / 2; i++) 
        {
	    if (ba[i] != -1)
	        error ("Byte overwritten at pos: " + i);
	}
	for (int i = size / 2; i < size; i++) 
        {
	    if (ba[i] != byteFunc(i))
	        error ("Bad byte found in second read array pos: " + i);
	}
	
	// setBytes tests
	ba = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

	try 
	{
	    rma1.setBytes(size / 2, ba, 2, 4);
	} catch (OffsetOutOfBoundsException e)
	{
	}

	try 
	{
	    for (int i = 0; i < 4; i++) 
	    {
	        if (ba[i + 2] != rma1.getByte(size / 2 + i))
		    error ("Bytes not set correctly from array");
	    }	
	    System.out.println ("Byte array tests passed");

	} catch (OffsetOutOfBoundsException e)
	{
	}
    }

    private static void shortTests () 
    {
	try 
	{
    	    final int shortLength = 2;
	    for (long i = 0; i < size; i += shortLength) 
	    {
	        rma1.setShort(i, shortFunc(i));
	    }
	
	    for (long i = 0; i < size; i += shortLength) 
	    {
	        long result;
		if ((result = rma1.getShort(i)) != shortFunc(i))
		    error("Bad short found in rma1.  Wanted: " + shortFunc(i) + " but got: " + result);
	    }		
	
	    System.out.println ("Short tests passed");	
	
	    short[] sa = new short[(size - 32) / shortLength];

	    rma1.getShorts(16L, sa, 0, (size - 32) / shortLength);

	    for (int i = 0; i < (size - 32) / shortLength; i++) 
	    {
	        if (sa[i] != shortFunc(i*shortLength + 16))
		    error ("Bad short found in first read array pos: " + i);
	    }
	    sa = new short[size / shortLength];
	    for (int i = 0; i < size / shortLength; i++) 
	    {
	        sa[i] = -1;
	    }
	
	    rma1.getShorts(size / 2, sa, (size / 2) / shortLength, (size - (size/2)) / shortLength);
	
	    for (int i = 0; i < (size / 2) / shortLength; i++) 
	    {
	        if (sa[i] != -1)
		    error ("Short overwritten at pos: " + i);
	    }
	    for (int i = size / 2 / shortLength; i < size / shortLength; i++) 
	    {
	        if (sa[i] != shortFunc(i*shortLength))
		    error ("Bad short found in second read array pos: " + i);
            }
	
	    // setShorts tests
	    sa = new short[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
	    rma1.setShorts(size / 2, sa, 2, 4);
	    for (int i = 0; i < 4; i++) 
	    {
	        if (sa[i + 2] != rma1.getShort(size / 2 + i * shortLength))
		    error ("Shorts not set correctly from array");
	    }

		
	    System.out.println ("Short array tests passed");	
	
	} catch (OffsetOutOfBoundsException e)
	{
	}
	
    }

    private static void intTests () 
    {
	try 
	{
            final int intLength = 4;
	    for (long i = 0; i < size; i += intLength) 
	    {
	        rma1.setInt(i, intFunc(i));
	    }

	    for (long i = 0; i < size; i += intLength) 
	    {
	        int result;
		if ((result = rma1.getInt(i)) != intFunc(i))
		    error("Bad int found in rma1.  Wanted: " + intFunc(i) + " but got: " + result);
	    }		
	    System.out.println ("Int tests passed");
	
		int[] ia = new int[(size - 32) / intLength];
		rma1.getInts(16L, ia, 0, (size - 32) / intLength);

		for (int i = 0; i < (size - 32) / intLength; i++) 
		{
			if (ia[i] != intFunc(i*intLength + 16))
				error ("Bad int found in first read array pos: " + i);
		}
		ia = new int[size / intLength];
		for (int i = 0; i < size / intLength; i++) 
		{
			ia[i] = -1;
		}
		rma1.getInts(size / 2, ia, (size / 2) / intLength, (size - (size/2)) / intLength);
		for (int i = 0; i < (size / 2) / intLength; i++) 
		{
			if (ia[i] != -1)
				error ("int overwritten at pos: " + i);
		}
		for (int i = size / 2 / intLength; i < size / intLength; i++) 
		{
			if (ia[i] != intFunc(i*intLength))
				error ("Bad int found in second read array pos: " + i);
		}	
	
		// setInts tests
		ia = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		rma1.setInts(size / 2, ia, 2, 4);
		for (int i = 0; i < 4; i++) 
		{
			if (ia[i + 2] != rma1.getInt(size / 2 + i * intLength))
				error ("Ints not set correctly from array");
		}	
	
		System.out.println ("Int array tests passed");	
	} catch (OffsetOutOfBoundsException e)
	{
	}
}

private static void longTests () 
{
	try 
	{
	
		final int longLength = 8;
		for (long i = 0; i < size; i += longLength) 
			rma1.setLong(i, longFunc(i));

		for (long i = 0; i < size; i += longLength) 
		{
			long result;
			if ((result = rma1.getLong(i)) != longFunc(i))
				error("Bad long found in rma1.  Wanted: " + longFunc(i) + " but got: " + result);
		}		
		System.out.println ("Long tests passed");
	
		long[] la = new long[(size - 32) / longLength];
		rma1.getLongs(16L, la, 0, (size - 32) / longLength);

		for (int i = 0; i < (size - 32) / longLength; i++) 
		{
			if (la[i] != longFunc(i*longLength + 16))
				error ("Bad long found in first read array pos: " + i);
		}
		la = new long[size / longLength];
		for (int i = 0; i < size / longLength; i++) 
		{
			la[i] = -1;
		}
		rma1.getLongs(size / 2, la, (size / 2) / longLength, (size - (size/2)) / longLength);
		for (int i = 0; i < (size / 2) / longLength; i++) 
		{
			if (la[i] != -1)
				error ("long overwritten at pos: " + i);
		}
		for (int i = size / 2 / longLength; i < size / longLength; i++) 
		{
			if (la[i] != longFunc(i*longLength))
				error ("Bad long found in second read array pos: " + i);
		}	
	
		// setLongs tests
		la = new long[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		rma1.setLongs(size / 2, la, 2, 4);
		for (int i = 0; i < 4; i++) 
		{
			if (la[i + 2] != rma1.getLong(size / 2 + i * longLength))
				error ("Longs not set correctly from array");
		}	
	
		System.out.println ("Long array tests passed");		
		
	} catch (OffsetOutOfBoundsException e)
	{
	}
}

private static void exceptionTests () {
	try {
		rma1.getByte(-1);
		error("getByte 1: Should have thrown exception");
	} catch (OffsetOutOfBoundsException e) {
	}
	try {
		rma1.getByte(size);
		error("getByte 2: Should have thrown exception");
	} catch (OffsetOutOfBoundsException e) {
	}
	try {
		rma1.setByte(-1, (byte)0);
		error("setByte 1: Should have thrown exception");
	} catch (OffsetOutOfBoundsException e) {
	}
	try {
		rma1.setByte(size, (byte)0);
		error("setByte 2: Should have thrown exception");
	} catch (OffsetOutOfBoundsException e) {
	}

	try {
		rma1.getShort(-1);
		error("getShort 1: Should have thrown exception");
	} catch (OffsetOutOfBoundsException e) {
	}
	try {
		rma1.getShort(size - 1);
		error("getShort 2: Should have thrown exception");
	} catch (OffsetOutOfBoundsException e) {
	}
	try {
		rma1.setShort(-1, (short)0);
		error("setShort 1: Should have thrown exception");
	} catch (OffsetOutOfBoundsException e) {
	}
	try {
		rma1.setShort(size - 1, (short)0);
		error("setShort 2: Should have thrown exception");
	} catch (OffsetOutOfBoundsException e) {
	}
	
	try {
		rma1.getInt(-1);
		error("getInt 1: Should have thrown exception");
	} catch (OffsetOutOfBoundsException e) {
	}
	try {
		rma1.getInt(size - 3);
		error("getInt 2: Should have thrown exception");
	} catch (OffsetOutOfBoundsException e) {
	}
	try {
		rma1.setInt(-1, 0);
		error("setInt 1: Should have thrown exception");
	} catch (OffsetOutOfBoundsException e) {
	}
	try {
		rma1.setInt(size - 3, 0);
		error("setInt 2: Should have thrown exception");
	} catch (OffsetOutOfBoundsException e) {
	}
	
	try {
		rma1.getLong(-1);
		error("getLong 1: Should have thrown exception");
	} catch (OffsetOutOfBoundsException e) {
	}
	try {
		rma1.getLong(size - 7);
		error("getLong 2: Should have thrown exception");
	} catch (OffsetOutOfBoundsException e) {
	}
	try {
		rma1.setLong(-1, 0);
		error("setLong 1: Should have thrown exception");
	} catch (OffsetOutOfBoundsException e) {
	}
	try {
		rma1.setLong(size - 7, 0);
		error("setLong 2: Should have thrown exception");
	} catch (OffsetOutOfBoundsException e) {
	}

	// should write set<types>s/get<type>s array exception tests	
	System.out.println("Passed exception tests");
}

private static void subregionTests() 
{
	try 
	{
		final RawMemoryAccess subrma;
		int subBegin = size / 4;
		int subEnd = size - size / 4;
		for (int i = 0; i < size; i++)
			rma1.setByte(i, (byte)-1);
		
		try 
		{
			subrma = rma1.subregion(subBegin, subEnd - subBegin);
			subrma.map();
		} catch (SizeOutOfBoundsException size)
		{
			error("Subregion failed");
			return;
		}

		for (int i = 0; i < subEnd - subBegin; i++) 
		{
			subrma.setByte(i, byteFunc(i));
		}	
		for (int i = 0; i < subEnd - subBegin; i++) 
		{
			if (rma1.getByte(subBegin + i) != byteFunc(i)) 
			{
				error ("Subregion failed");
			}
		}
		System.out.println ("Subregion tests passed");
	} catch (OffsetOutOfBoundsException e)
	{
	}

}


public static void error () {
	System.out.println ("ERROR");
	System.exit(0);
}

public static void error (String str) {
	System.out.println ("ERROR - " + str);
	System.exit(0);
}

public static void printBytes (RawMemoryAccess rm) {
	for (int i = 0; i < size; i += 8) {
		for (int j = 0; j < 8; j++) {
			try {
				System.out.print (i+j+":"+rm.getByte(i+j)+"\n");
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		System.out.println();
	}
}

/**
 * Tests using multiple instances of RMA
 */
public static void multipleTests() 
{
	try 
	{
		rma1.unmap();
  
		RawMemoryAccess rma2 = (RawMemoryAccess)DefaultPhysicalMemoryFactory.instance().create(null, true, 4096L*15240L, size);
  
		rma1.map();
		rma2.map();
  
		for (int i = 0; i < size; i++) 
		{
			rma1.setByte(i, (byte)1);
		}
  
		for (int i = 0; i < size; i++) 
		{
			rma2.setByte(i, (byte)2);
		}
  
		for (int i = 0; i < size; i++) 
		{
			byte result;
			if ((result = rma1.getByte(i)) != 1) 
			{
				error ("Bad byte found in rma1.  Pos: " + i + " wanted: 1 but got: " + result);
			}	
    	}
    
    	for (int i = 0; i < size; i++) 
    	{
			byte result;
			if ((result = rma2.getByte(i)) != 2) 
			{
				error ("Bad byte found in rma2.  Pos: " + i + " wanted: 1 but got: " + result);
			}
    	}
    
   		rma1.unmap();
		rma2.unmap();
	
		System.out.println ("Multiple instance tests passed");
	} catch (OffsetOutOfBoundsException e)
	{
	}
}

public static void threadTests() 
{
		Thread myThread = new Thread () 
		{
			public void run () 
			{
				try 
				{
					for (int i = 0; i < size; i++) 
					{
						rma1.setByte(i, (byte)-3);
					}
				} catch (OffsetOutOfBoundsException e)
				{
				}
			}
		};
		myThread.start();
		try 
		{
			myThread.join();
		} catch (Exception e) 
		{
			System.out.println(e);
		}
		try 
		{
			for (int i = 0; i < size; i++) 
			{
				if (rma1.getByte(i) != -3) 
				{	
					error ("Thread tests: value not set");
				}
			}
		} catch (OffsetOutOfBoundsException e)
		{
		}
		System.out.println ("Thread tests passed");
}

}
