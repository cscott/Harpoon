package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class AsynchronouslyInterruptedExceptionTest 
{
	public static void run() 
	{
		AsynchronouslyInterruptedException aie = null;
		Object o = null;

		Tests.increment();
		try {
			aie = new AsynchronouslyInterruptedException();
			if( !(aie instanceof AsynchronouslyInterruptedException &&
				aie instanceof InterruptedException) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AsynchronouslyInterruptedExceptionTest");
		}

		Tests.increment();
		try {
//			boolean x = aie.disable(); // should return boolean but returns void
			throw new Exception();
		} catch (Exception e) {
			Tests.fail("AsynchronouslyInterruptedExceptionTest");
		}

		Tests.increment();
		try {
			Interruptible logic = null;
//			boolean x = aie.doInterruptible(logic); // should return boolean but returns void
			throw new Exception();
		} catch (Exception e) {
			Tests.fail("AsynchronouslyInterruptedExceptionTest");
		}

		Tests.increment();
		try {
//			boolean x = aie.enable(); // should return boolean but returns void
			throw new Exception();
		} catch (Exception e) {
			Tests.fail("AsynchronouslyInterruptedExceptionTest");
		}

		Tests.increment();
		try {
//			boolean x = aie.fire(); // should return boolean but returns void
			throw new Exception();
		} catch (Exception e) {
			Tests.fail("AsynchronouslyInterruptedExceptionTest");
		}

		Tests.increment();
		try {
			o = AsynchronouslyInterruptedException.getGeneric();
			if( !(o instanceof AsynchronouslyInterruptedException) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("AsynchronouslyInterruptedExceptionTest");
		}

		Tests.increment();
		try {
			boolean x = aie.happened(true);
		} catch (Exception e) {
			Tests.fail("AsynchronouslyInterruptedExceptionTest");
		}

		Tests.increment();
		try {
			boolean x = aie.isEnabled();
		} catch (Exception e) {
			Tests.fail("AsynchronouslyInterruptedExceptionTest");
		}

		Tests.increment();
		try {
			aie.propogate();
		} catch (Exception e) {
			Tests.fail("AsynchronouslyInterruptedExceptionTest");
		}
	}
}