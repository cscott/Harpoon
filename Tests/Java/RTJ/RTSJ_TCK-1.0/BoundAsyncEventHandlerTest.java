//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              BoundAsyncEventHandlerTest

Subtest 1:
        "public BoundAsyncEventHandler()"

 Subtest 2:
        "public BoundAsyncEventHandler(Scheduling Parameters scheduling,
        ReleaseParameters release, MemoryParameters memory, MemoryArea area,
        ProcessingGroupParameters group, boolean noheap)

*/

import javax.realtime.*;
import com.timesys.*;

public class BoundAsyncEventHandlerTest
{

    public static void run() {
        Object o = null;

        Tests.newTest("BoundAsyncEventHandlerTest");

        /* NOTE: Because BoundAsyncEventHandler is an abstract class,
           the following tests use the class BAEventHandler which
           extends the BoundAsyncEventHandler class.
        */

        /* Subtest 1:
        ** Constructor "public BoundAsyncEventHandler()"
        */
        Tests.increment();
        try {
            System.out.println("BoundAsyncEventHandlerTest: "+
                               "BoundAsyncEventHandler()");
            o = new BAEventHandler();
            if (! (o instanceof BoundAsyncEventHandler && o instanceof
                   AsyncEventHandler))
                throw new Exception("Return object is not instanceof "+
                                    "BoundAsyncEventHandler nor "+
                                    "AsyncEventHandler");
        } catch (Exception e) {
            System.out.println("BoundAsyncEventHandlerTest: "+
                               "BoundAsyncEventHandler() failed");
            Tests.fail("new BAEventHandler()",e);
        }

        /* Subtest 2:
        ** Constructor "public BoundAsyncEventHandler(Scheduling Parameters
        ** scheduling, ReleaseParameters release, MemoryParameters memory,
        ** MemoryArea area, ProcessingGroupParameters group, boolean noheap)
        */
        Tests.increment();
        try {
            System.out.println("BoundAsyncEventHandlerTest: "+
                               "BoundAsyncEventHandler(null,null,null,"+
                               "LTMemory,null,false)");
            o = new BAEventHandler(null,null,null,
                                   new LTMemory(1048476,1048576),
                                   null,false,null);
            if (! (o instanceof BoundAsyncEventHandler && o instanceof
                   AsyncEventHandler))
                throw new Exception("Return object is not instanceof "+
                                    "BoundAsyncEventHandler nor "+
                                    "AsyncEventHandler");
        } catch (Exception e) {
            System.out.println("BoundAsyncEventHandlerTest: "+
                               "BoundAsyncEventHandler(null,null,null,"+
                               "LTMemory,null,false)");
            Tests.fail("new BAEventHandler(null,null,null,new "+
                       "LTMemory(1048476,1048576),null,false)",e);
        }

        Tests.printSubTestReportTotals("BoundAsyncEventHandlerTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}
