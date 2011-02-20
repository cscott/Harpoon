//package javax.realtime.calltests;

/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/*                              POSIXSignalHandlerTest
Subtest 1:
        POSIX signal SIGABRT

Subtest 2:
        POSIX signal SIGALRM

Subtest 3:
        POSIX signal SIGBUS

Subtest 4:
        POSIX signal SIGCANCEL

Subtest 5:
        POSIX signal SIGCHLD

Subtest 6:
        POSIX signal SIGCLD

Subtest 7:
        POSIX signal SIGCONT

Subtest 8:
        POSIX signal SIGEMT

Subtest 9:
        POSIX signal SIGFPE

Subtest 10:
        POSIX signal SIGFREEZE

Subtest 11:
        POSIX signal SIGHUP

Subtest 12:
        POSIX signal SIGILL

Subtest 13:
        POSIX signal SIGINT

Subtest 14:
        POSIX signal SIGIO

Subtest 15:
        POSIX signal SIGIOT

Subtest 16:
        POSIX signal SIGKILL

Subtest 17:
        POSIX signal SIGLOST

Subtest 18:
        POSIX signal SIGPIPE

Subtest 19:
        POSIX signal SIGPOLL

Subtest 20:
        POSIX signal SIGPROF

Subtest 21:
        POSIX signal SIGPWR

Subtest 22:
        POSIX signal SIGQUIT

Subtest 23:
        POSIX signal SIGSEGV

Subtest 24:
        POSIX signal SIGSYS

Subtest 25:
        POSIX signal SIGTERM

Subtest 26:
        POSIX signal SIGTHAW

Subtest 27:
        POSIX signal SIGTRAP

Subtest 28:
        POSIX signal SIGTTIN

Subtest 29:
        POSIX signal SIGTTOU

Subtest 30:
        POSIX signal SIGURG

Subtest 31:
        POSIX signal SIGUSR1

Subtest 32:
        POSIX signal SIGUSR2

Subtest 33:
        POSIX signal SIGVTALRM

Subtest 34:
        POSIX signal SIGWAITING

Subtest 35:
        POSIX signal SIGWINCH

Subtest 36:
        POSIX signal SIGXCPU

Subtest 37:
        POSIX signal SIGXFSZ

Subtest 38:
        "public static synchronized void addHandler(int signal,
        AsyncEventHandler handler)

Subtest 39:
        "public static synchronized void removeHandler(int signal,
        AsyncEventHandler handler)"

Subtest 40:
        "public static synchronized void setHandler(int signal,
        AsyncEventHandler handler)

Subtest 41:
        "public static synchronized voide setHandler(int signal,
        AsyncEventHandler handler) where handler is null
*/

import javax.realtime.*;

public class POSIXSignalHandlerTest
{

    public static void run() {
        Tests.newTest("POSIXSignalHandlerTest");

        /* Subtest 1:
        ** POSIX signal SIGABRT
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGABRT");
            int x = POSIXSignalHandler.SIGABRT;
            if (x <= 0)
                throw new Exception("Invalid SIGABRT");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGABRT "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGABRT",e);
        }


        /* Subtest 2:
        ** POSIX signal SIGALRM
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGALRM");
            int x = POSIXSignalHandler.SIGALRM;
            if (x <= 0)
                throw new Exception("Invalid SIGALRM");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGALRM "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGALRM",e);
        }

        /* Subtest 3:
        ** POSIX signal SIGBUS
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGBUS");
            int x = POSIXSignalHandler.SIGBUS;
            if (x <= 0)
                throw new Exception("Invalid SIGABRT");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGBUS "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGABRT",e);
        }

        /* Subtest 4:
        ** POSIX signal SIGCANCEL
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal "+
                               "SIGCANCEL");
            int x = POSIXSignalHandler.SIGCANCEL;
            if (x <= 0)
                throw new Exception("Invalid SIGCANCEL");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal "+
                               "SIGCANCEL failed");
            Tests.fail("POSIXSignalHandler.SIGCANCEL",e);
        }

        /* Subtest 5:
        ** POSIX signal SIGCHLD
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGCHLD");
            int x = POSIXSignalHandler.SIGCHLD;
            if (x <= 0)
                throw new Exception("Invalid SIGCHLD");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGCHLD "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGCHLD",e);
        }

        /* Subtest 6:
        ** POSIX signal SIGCLD
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGCLD");
            int x = POSIXSignalHandler.SIGCLD;
            if (x <= 0)
                throw new Exception("Invalid SIGCLD");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGCLD "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGCLD",e);
        }

        /* Subtest 7:
        ** POSIX signal SIGCONT
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGCONT");
            int x = POSIXSignalHandler.SIGCONT;
            if (x <= 0)
                throw new Exception("Invalid SIGCONT");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGCONT "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGCONT",e);
        }

        /* Subtest 8:
        ** POSIX signal SIGEMT
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGEMT");
            int x = POSIXSignalHandler.SIGEMT;
            if (x <= 0)
                throw new Exception("Invalid SIGEMT");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGEMT "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGEMT",e);
        }

        /* Subtest 9:
        ** POSIX signal SIGFPE
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGFPE");
            int x = POSIXSignalHandler.SIGFPE;
            if (x <= 0)
                throw new Exception("Invalid SIGFPE");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGFPE "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGFPE",e);
        }

        /* Subtest 10:
        ** POSIX signal SIGFREEZE
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal "+
                               "SIGFREEZE");
            int x = POSIXSignalHandler.SIGFREEZE;
            if (x <= 0)
                throw new Exception("Invalid SIGFREEZE");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal "+
                               "SIGFREEZE failed");
            Tests.fail("POSIXSignalHandler.SIGFREEZE",e);
        }

        /* Subtest 11:
        ** POSIX signal SIGHUP
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGHUP");
            int x = POSIXSignalHandler.SIGHUP;
            if (x <= 0)
                throw new Exception("Invalid SIGHUP");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGHUP "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGHUP",e);
        }

        /* Subtest 12:
        ** POSIX signal SIGILL
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGILL");
            int x = POSIXSignalHandler.SIGILL;
            if (x <= 0)
                throw new Exception("Invalid SIGILL");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGILL "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGILL",e);
        }

        /* Subtest 13:
        ** POSIX signal SIGINT
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGINT");
            int x = POSIXSignalHandler.SIGINT;
            if (x <= 0)
                throw new Exception("Invalid SIGINT");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGINT "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGINT",e);
        }

        /* Subtest 14:
        ** POSIX signal SIGIO
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGIO");
            int x = POSIXSignalHandler.SIGIO;
            if (x <= 0)
                throw new Exception("Invalid SIGIO");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGIO "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGIO",e);
        }

        /* Subtest 15:
        ** POSIX signal SIGIOT
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGIOT");
            int x = POSIXSignalHandler.SIGIOT;
            if (x <= 0)
                throw new Exception("Invalid SIGIOT");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGIOT "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGIOT",e);
        }

        /* Subtest 16:
        ** POSIX signal SIGKILL
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGKILL");
            int x = POSIXSignalHandler.SIGKILL;
            if (x <= 0)
                throw new Exception("Invalid SIGKILL");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGKILL "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGKILL",e);
        }

        /* Subtest 17:
        ** POSIX signal SIGLOST
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGLOST");
            int x = POSIXSignalHandler.SIGLOST;
            if (x <= 0)
                throw new Exception("Invalid SIGLOST");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGLOST "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGLOST",e);
        }

        /* Subtest 18:
        ** POSIX signal SIGPIPE
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGPIPE");
            int x = POSIXSignalHandler.SIGPIPE;
            if (x <= 0)
                throw new Exception("Invalid SIGPIPE");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGPIPE "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGPIPE",e);
        }

        /* Subtest 19:
        ** POSIX signal SIGPOLL
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGPOLL");
            int x = POSIXSignalHandler.SIGPOLL;
            if (x <= 0)
                throw new Exception("Invalid SIGPOLL");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGPOLL "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGPOLL",e);
        }

        /* Subtest 20:
        ** POSIX signal SIGPROF
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGPROF");
            int x = POSIXSignalHandler.SIGPROF;
            if (x <= 0)
                throw new Exception("Invalid SIGPROF");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGPROF "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGPROF",e);
        }

        /* Subtest 21:
        ** POSIX signal SIGPWR
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGPWR");
            int x = POSIXSignalHandler.SIGPWR;
            if (x <= 0)
                throw new Exception("Invalid SIGPWR");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGPWR "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGPWR",e);
        }

        /* Subtest 22:
        ** POSIX signal SIGQUIT
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGQUIT");
            int x = POSIXSignalHandler.SIGQUIT;
            if (x <= 0)
                throw new Exception("Invalid SIGQUIT");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGQUIT "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGQUIT",e);
        }

        /* Subtest 23:
        ** POSIX signal SIGSEGV
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGSEGV");
            int x = POSIXSignalHandler.SIGSEGV;
            if (x <= 0)
                throw new Exception("Invalid SIGSEGV");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGSEGV "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGSEGV",e);
        }

        /* Subtest 24:
        ** POSIX signal SIGSYS
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGSYS");
            int x = POSIXSignalHandler.SIGSYS;
            if (x <= 0)
                throw new Exception("Invalid SIGSYS");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGSYS "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGSYS",e);
        }

        /* Subtest 25:
        ** POSIX signal SIGTERM
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGTERM");
            int x = POSIXSignalHandler.SIGTERM;
            if (x <= 0)
                throw new Exception("Invalid SIGTERM");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGTERM "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGTERM",e);
        }

        /* Subtest 26:
        ** POSIX signal SIGTHAW
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGTHAW");
            int x = POSIXSignalHandler.SIGTHAW;
            if (x <= 0)
                throw new Exception("Invalid SIGTHAW");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGTHAW "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGTHAW",e);
        }

        /* Subtest 27:
        ** POSIX signal SIGTRAP
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGTRAP");
            int x = POSIXSignalHandler.SIGTRAP;
            if (x <= 0)
                throw new Exception("Invalid SIGTRAP");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGTRAP "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGTRAP",e);
        }

        /* Subtest 28:
        ** POSIX signal SIGTTIN
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGTTIN");
            int x = POSIXSignalHandler.SIGTTIN;
            if (x <= 0)
                throw new Exception("Invalid SIGTTIN");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGTTIN "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGTTIN",e);
        }

        /* Subtest 29:
        ** POSIX signal SIGTTOU
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGTTOU");
            int x = POSIXSignalHandler.SIGTTOU;
            if (x <= 0)
                throw new Exception("Invalid SIGTTOU");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGTTOU "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGTTOU",e);
        }

        /* Subtest 30:
        ** POSIX signal SIGURG
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGURG");
            int x = POSIXSignalHandler.SIGURG;
            if (x <= 0)
                throw new Exception("Invalid SIGURG");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGURG "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGURG",e);
        }

        /* Subtest 31:
        ** POSIX signal SIGUSR1
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGUSR1");
            int x = POSIXSignalHandler.SIGUSR1;
            if (x <= 0)
                throw new Exception("Invalid SIGUSR1");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGUSR1 "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGUSR1",e);
        }

        /* Subtest 32:
        ** POSIX signal SIGUSR2
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGUSR2");
            int x = POSIXSignalHandler.SIGUSR2;
            if (x <= 0)
                throw new Exception("Invalid SIGUSR2");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGUSR2 "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGUSR2",e);
        }

        /* Subtest 33:
        ** POSIX signal SIGVTALRM
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal "+
                               "SIGVTALRM");
            int x = POSIXSignalHandler.SIGVTALRM;
            if (x <= 0)
                throw new Exception("Invalid SIGVTALRM");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal "+
                               "SIGVTALRM failed");
            Tests.fail("POSIXSignalHandler.SIGVTALRM",e);
        }

        /* Subtest 34:
        ** POSIX signal SIGWAITING
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal "+
                               "SIGWAITING");
            int x = POSIXSignalHandler.SIGWAITING;
            if (x <= 0)
                throw new Exception("Invalid SIGWAITING");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal "+
                               "SIGWAITING failed");
            Tests.fail("POSIXSignalHandler.SIGWAITING",e);
        }

        /* Subtest 35:
        ** POSIX signal SIGWINCH
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal "+
                               "SIGWINCH");
            int x = POSIXSignalHandler.SIGWINCH;
            if (x <= 0)
                throw new Exception("Invalid SIGWINCH");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal "+
                               "SIGWINCH failed");
            Tests.fail("POSIXSignalHandler.SIGWINCH",e);
        }

        /* Subtest 36:
        ** POSIX signal SIGXCPU
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: signal SIGXCPU");
            int x = POSIXSignalHandler.SIGXCPU;
            if (x <= 0)
                throw new Exception("Invalid SIGXCPU");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: signal SIGXCPU "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGXCPU",e);
        }

        /* Subtest 37:
        ** POSIX signal SIGXFSZ
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGXFSZ");
            int x = POSIXSignalHandler.SIGXFSZ;
            if (x <= 0)
                throw new Exception("Invalid SIGXFSZ");
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: POSIX signal SIGXFSZ "+
                               "failed");
            Tests.fail("POSIXSignalHandler.SIGXFSZ",e);
        }


        /* Subtest 38:
        ** Method "public static synchronized void addHandler(int signal,
        ** AsyncEventHandler handler)
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: addHandler(int,"+
                               "AsyncEventHandler)");
            int signal = POSIXSignalHandler.SIGUSR1;
            AEventHandler handler = new AEventHandler();
            POSIXSignalHandler.addHandler(signal, handler);
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: addHandler(int,"+
                               "AsyncEventHandler) failed");
            Tests.fail("POSIXSignalHandler.addHandler(signal,handler)",e);
        }

        /* Subtest 39:
        ** Method "public static synchronized void removeHandler(int signal,
        ** AsyncEventHandler handler)"
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: removeHandler(int,"+
                               "AsyncEventHandler)");
            int signal = POSIXSignalHandler.SIGABRT;
            AEventHandler handler = new AEventHandler();
            POSIXSignalHandler.addHandler(signal, handler);
            POSIXSignalHandler.removeHandler(signal, handler);
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: removeHandler(int,"+
                               "AsyncEventHandler) failed");
            Tests.fail("POSIXSignalHandler.removeHandler(signal,handler)",e);
        }

        /* Subtest 40:
        ** Method "public static synchronized void setHandler(int signal,
        ** AsyncEventHandler handler)
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: setHandler(int,"+
                               "AsyncEventHandler)");
            int signal = POSIXSignalHandler.SIGUSR1;
            AEventHandler handler = new AEventHandler();
            POSIXSignalHandler.setHandler(signal, handler);
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: setHandler(int,"+
                               "AsyncEventHandler) failed");
            Tests.fail("POSIXSignalHandler.setHandler(signal,handler)",e);
            e.printStackTrace();
        }

        /* Subtest 41:
        ** Method "public static synchronized voide setHandler(int signal,
        ** AsyncEventHandler handler) where handler is null
        */
        Tests.increment();
        try {
            System.out.println("POSIXSignalHandlerTest: setHandler(int,null)");
            int signal = POSIXSignalHandler.SIGUSR1;
            AEventHandler handler = new AEventHandler();
            POSIXSignalHandler.addHandler(signal, handler);
            POSIXSignalHandler.setHandler(signal, null);
        } catch (Exception e) {
            System.out.println("POSIXSignalHandlerTest: setHandler(int,null) "+
                               "failed");
            Tests.fail("POSIXSignalHandler.setHandler(signal,null)",e);
        }

        Tests.printSubTestReportTotals("POSIXSignalHandlerTest");
    }

    public static void main(java.lang.String[] args)
    {
        Tests.init();
        run();
        Tests.conclude();
    }
}




