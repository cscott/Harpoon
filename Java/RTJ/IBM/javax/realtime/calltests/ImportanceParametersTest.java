package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class ImportanceParametersTest 
{

	public static void run() 
	{
		ImportanceParameters ip = null;
		Object o = null;

		Tests.increment();
		try {
			int priority = 10;
			int importance = 12;
			ip = new ImportanceParameters(priority, importance);
			if( !(ip instanceof ImportanceParameters && ip instanceof PriorityParameters) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ImportanceParametersTest");
		}

		Tests.increment();
		try {
			int importance = ip.getImportance();
			if( importance != 12 )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ImportanceParametersTest");
		}

		Tests.increment();
		try {
			int importance = 5;
			ip.setImportance(importance);
			if( ip.getImportance() != 5 )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ImportanceParametersTest");
		}

		Tests.increment();
		try {
			o = ip.toString();
			if( !(o instanceof String) )
				throw new Exception();
		} catch (Exception e) {
			Tests.fail("ImportanceParametersTest");
		}
	}
}