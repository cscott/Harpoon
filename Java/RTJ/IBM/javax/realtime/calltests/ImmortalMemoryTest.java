package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class ImmortalMemoryTest 
{

	public static void run() 
	{
		ImmortalMemory im = null;
		Object o = null;
/*
		Tests.increment();
		try {
			im = ImmortalMemory.instance();
			if( !(im instanceof ImmortalMemory && im instanceof MemoryArea) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ImmortalMemoryTest");
		}

// MemoryArea tests

		Tests.increment();
		try {
			long sizeInBytes = 1000;
//			im = new HeapMemory(sizeInBytes); // protected
		} catch (Exception e) {
			Tests.fail("ImmortalMemoryTest");
		}

		Tests.increment();
		try {
			Runnable logic = null;
			im.enter(logic);
		} catch (Exception e) {
			Tests.fail("ImmortalMemoryTest");
		}

		Tests.increment();
		try {
			Object testObject = new Object();
			o = HeapMemory.getMemoryArea(testObject);
			if( !(im instanceof MemoryArea) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ImmortalMemoryTest");
		}

		Tests.increment();
		try {
			long consumed = im.memoryConsumed();
		} catch (Exception e) {
			Tests.fail("ImmortalMemoryTest");
		}

		Tests.increment();
		try {
			long consumed = im.memoryRemaining();
		} catch (Exception e) {
			Tests.fail("ImmortalMemoryTest");
		}

		Tests.increment();
		try {
			int x = 5;
			Class c = null;
			o = im.newArray(c, x);
		} catch (Exception e) {
			Tests.fail("ImmortalMemoryTest");
		}

		Tests.increment();
		try {
			Class c = null;
			o = im.newInstance(c);
		} catch (Exception e) {
			Tests.fail("ImmortalMemoryTest");
		}
*/
	}
}