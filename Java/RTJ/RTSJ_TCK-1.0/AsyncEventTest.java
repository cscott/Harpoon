//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              AsyncEventTest

Subtest 1:
        "public AsyncEvent()"

Subtest 2:
        "public synchronized void addHandler(AsyncEventHandler handler)"
        where handler is null

Subtest 3:
        "public synchronized void addHandler(AsyncEventHandler handler)"

Subtest 4:
        "public void bindTo(java.lang.String happening)

Subtest 5:
        "public ReleaseParameters createReleaseParameters()"

Subtest 6:
        "public boolean handledBy(AsyncEventHandler target)"

Subtest 7:
        "public boolean handledBy(AsyncEventHandler target)" where target is
        not the handler

Subtest 8:
        "public boolean handledBy(AsyncEventHandler target)" where target is
        null

Subtest 9:
        "public synchronized void removeHandler(AsyncEventHandler handler)

Subtest 10:
        "public synchronized void removeHandler(AsyncEventHandler handler)
        where handler not associated with AsyncEvent

Subtest 11:
        "public synchronized void removeHandler(AsyncEventHandler handler)
        where handler is null

Subtest 12:
        "public synchronized void setHandler(AsyncEventHandler handler)"

Subtest 13:
        "public synchronized void setHandler(AsyncEventHandler handler)" where
        handler is null

Subtest 14:
        "public synchronized void fire()"

Subtest 15:
        "public void unBindTo(String happening)"

*/

import javax.realtime.*;
import com.timesys.*;

public class AsyncEventTest
{
    //  This may need to change depending on the implementation
    //  of the underlying system.
    public static final String IMPL_DEFINED_HAPPENING = "53";

    public static void run2() {
        AsyncEvent ae = null;
        Object o = null;
        int pid;

        BoundAsyncEventHandler h
            = new BoundAsyncEventHandler(null, null,null,
                                         new LTMemory(1048576,1048576),
                                         null,false,null){
                    public void handleAsyncEvent()
                    {
                        System.out.println("**** In h.handleAsyncEvent()");
                        Tests.delay(5);
                        System.out.println("**** Exit h.handleAsyncEvent()");
                    }
                };

        BoundAsyncEventHandler h2
            = new BoundAsyncEventHandler(null, null,null,
                                         new LTMemory(1048576,1048576),
                                         null,false,null){
                    public void handleAsyncEvent()
                    {
                        System.out.println("**** In h2.handleAsyncEvent()");
                        Tests.delay(5);
                        System.out.println("**** Exit h2.handleAsyncEvent()");
                    }
                };

        Tests.newTest("AsyncEventTest");

        /* Subtest 1:
        ** Constructor "public AsyncEvent()"
        */
        Tests.increment();

        try {
            System.out.println("AsyncEventTest: AsyncEvent()");
            ae = new AsyncEvent();
            if( !(ae instanceof AsyncEvent) )
                throw new Exception("Return object is not instanceof "+
                                    "AsyncEvent");
        } catch (Exception e) {
            System.out.println("AsyncEventTest: AsyncEvent() failed");
            Tests.fail("new AsyncEvent()",e);
        }

        /* Subtest 2:
        ** Method "public synchronized void addHandler(AsyncEventHandler
        ** handler)" where handler is null
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventTest: addHandler("+
                               "AsyncEventHandler)");
            BoundAsyncEventHandler nh = null;
            ae = new AsyncEvent();
            ae.addHandler(nh);
        } catch (Exception e) {
            System.out.println("AsyncEventTest: addHandler("+
                               "AsyncEventHandler) failed");
            Tests.fail("ae.addHandler(null)",e);
        }

        /* Subtest 3:
        ** Method "public synchronized void addHandler(AsyncEventHandler
        ** handler)"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventTest: addHandler("+
                               "AsyncEventHandler)");
            ae = new AsyncEvent();
            ae.addHandler(h);
        } catch (Exception e) {
            System.out.println("AsyncEventTest: addHandler("+
                               "AsyncEventHandler) failed");
            Tests.fail("ae.addHandler(h)",e);
        }

        /* Subtest 4:
        ** Method "public void bindTo(java.lang.String happening)
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventTest: bindTo(String)");
            ae = new AsyncEvent();
            ae.addHandler(h);
            ae.bindTo(IMPL_DEFINED_HAPPENING);
        } catch (Exception e) {
            System.out.println("AsyncEventTest: bindTo(String) failed");
            Tests.fail("ae.bindTo(63)",e);
        }

        /* Subtest 5:
        ** Method "public ReleaseParameters createReleaseParameters()"
        */

        Tests.increment();
        try {
            System.out.println("AsyncEventTest: createReleaseParameters()");
          ae = new AsyncEvent();
          o = ae.createReleaseParameters();
          if (! (o instanceof ReleaseParameters))
              throw new Exception("Return object is not instanceof "+
                                  "ReleaseParameters");
        } catch (Exception e) {
            System.out.println("AsyncEventTest: createReleaseParameters()"+
                               "failed");
            Tests.fail("ae.createReleaseParameters()",e);
        }

        /* Subtest 6:
        ** Method "public boolean handledBy(AsyncEventHandler target)"
        where target is the handler
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventTest: handledBy(AsyncEventHandler)");
            ae = new AsyncEvent();
            ae.addHandler(h);
            boolean hb = ae.handledBy(h);
            if (hb == false)
                throw new Exception("Handler not recognized");
        } catch (Exception e) {
            System.out.println("AsyncEventTest: handledBy(AsyncEventHandler) "+
                               "failed");
            Tests.fail("ae.handledBy(h)",e);
        }

        /* Subtest 7:
        ** Method "public boolean handledBy(AsyncEventHandler target)" where
        ** target is not the handler
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventTest: handledBy(AsyncEventHandler)");
            ae = new AsyncEvent();
            ae.addHandler(h);
            boolean hb = ae.handledBy(h2);
            if (hb == true)
                throw new Exception("Wrong handler recognized");
        } catch (Exception e) {
            System.out.println("AsyncEventTest: handledBy(AsyncEventHandler) "+
                               "failed");
            Tests.fail("ae.handledBy(h)",e);
        }

        /* Subtest 8:
        ** Method "public boolean handledBy(AsyncEventHandler target)" where
        ** target is null
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventTest: handledBy(AsyncEventHandler"+
                               "=null)");
            ae = new AsyncEvent();
            ae.addHandler(h);
            boolean hb = ae.handledBy(null);
            if (hb == true)
                throw new Exception("handledBy() method returned true for a "+
                                    "null target");
        } catch (Exception e) {
            System.out.println("AsyncEventTest: handledBy(AsyncEventHandler"+
                               "=null) failed");
            Tests.fail("ae.handledBy(null)",e);
        }

        /* Subtest 9:
        ** Method "public synchronized void removeHandler(AsyncEventHandler
        ** handler)
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventTest: removeHandler()");
            ae = new AsyncEvent();
            ae.addHandler(h);
            ae.removeHandler(h);
            if (ae.handledBy(h)==true)
                throw new Exception("Handler was not removed");
        } catch (Exception e) {
            System.out.println("AsyncEventTest: removeHandler() failed");
            Tests.fail("ae.removeHandler(h)",e);
        }

        /* Subtest 10:
        ** Method "public synchronized void removeHandler(AsyncEventHandler
        ** handler) where handler not associated with AsyncEvent
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventTest: removeHandler(AsyncEvent"+
                               "Handler)");
            ae = new AsyncEvent();
            ae.addHandler(h);
            ae.removeHandler(h2);
            if (ae.handledBy(h)==false)
                throw new Exception("Wrong handler was removed");
        } catch (Exception e) {
            System.out.println("AsyncEventTest: removeHandler(AsyncEvent"+
                               "Handler) failed");
            Tests.fail("ae.removeHandler(h)",e);
        }

        /* Subtest 11:
        ** Method "public synchronized void removeHandler(AsyncEventHandler
        ** handler) where handler is null
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventTest: removeHandler(null)");
            ae = new AsyncEvent();
            ae.addHandler(h);
            ae.removeHandler(null);
            if (ae.handledBy(h) == false)
                throw new Exception("Handler was removed");
        } catch (Exception e) {
            System.out.println("AsyncEventTest: removeHandler(null) failed");
            Tests.fail("ae.removeHandler(null)",e);
        }

        /* Subtest 12:
        ** Method "public synchronized void setHandler(AsyncEventHandler
        ** handler)"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventTest: setHandler("+
                               "AsyncEventHandler)");
            ae = new AsyncEvent();
            ae.addHandler(h);
            ae.setHandler(h2);
            if (ae.handledBy(h) == true)
                throw new Exception("Handler was not set properly");
        } catch (Exception e) {
            System.out.println("AsyncEventTest: setHandler("+
                               "AsyncEventHandler) failed");
            Tests.fail("ae.setHandler(h2)",e);
        }

        /* Subtest 13:
        ** Method "public synchronized void setHandler(AsyncEventHandler
        ** handler)" where handler is null
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventTest: setHandler(null)");
            ae = new AsyncEvent();
            ae.addHandler(h);
            ae.setHandler(null);
            if (ae.handledBy(h) == true)
                throw new Exception("Handler was not removed");
        } catch (Exception e) {
            System.out.println("AsyncEventTest: setHandler(null) failed");
            Tests.fail("ae.setHandler(h2)",e);
        }

        /* Subtest 14:
        ** Method "public synchronized void fire()"
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventTest: fire()");
            ae = new AsyncEvent();
            ae.addHandler(h);
            ae.addHandler(h2);
            ae.fire();
        } catch (Exception e) {
            System.out.println("AsyncEventTest: fire() failed");
            Tests.fail("ae.fire()",e);
        }

        /* Subtest 15:
        ** Method "public void unBindTo(java.lang.String happening)
        */
        Tests.increment();
        try {
            System.out.println("AsyncEventTest: unBindTo(String)");
            ae = new AsyncEvent();
            ae.addHandler(h);
            ae.bindTo(IMPL_DEFINED_HAPPENING);
            ae.unbindTo(IMPL_DEFINED_HAPPENING);
        } catch (Exception e) {
            System.out.println("AsyncEventTest: unBindTo(String) failed");
            Tests.fail("ae.unBindTo(63)",e);
        }

        Tests.printSubTestReportTotals("AsyncEventTest");
    }

    public static void run() {

        RealtimeThread rt
            = new RealtimeThread(null, null, null,
                                 ImmortalMemory.instance(),
                                 null,
                                 new Runnable(){
                                         public void run() {
                                             AsyncEventTest.run2();
                                         }
                                     }
                );

        rt.start();
        try {
            rt.join();
        }
        catch (Exception e) {
            System.out.println("Failed.");
        }

    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        AsyncEventTest.run();
        Tests.conclude();
    }
}
