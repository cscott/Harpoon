package javax.realtime.calltests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000  All Rights Reserved
 */

import javax.realtime.*;

public class POSIXSignalHandlerTest 
{
	
	public static void run() {
		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGABRT;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGALRM;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGBUS;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGCANCEL;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGCHLD;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGCLD;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGCONT;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGEMT;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGFPE;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGFREEZE;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGHUP;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGILL;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGINT;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGIO;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGIOT;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGKILL;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGLOST;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGLWP;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGPIPE;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGPOLL;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGPROF;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGPWR;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGQUIT;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGSEGV;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGSTOP;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGSYS;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGTERM;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGTHAW;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGTRAP;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGTSTP;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGTTIN;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGTTOU;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGURG;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGUSR1;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGUSR2;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGVTALARM;
			throw new Exception();
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGWAITING;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGWINCH;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGXCPU;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int x = POSIXSignalHandler.SIGXFSZ;
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int signal = POSIXSignalHandler.SIGXFSZ;
			POSIXEventHandler handler = new POSIXEventHandler();
			POSIXSignalHandler.addHandler(signal, handler);
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int signal = POSIXSignalHandler.SIGXFSZ;
			POSIXEventHandler handler = new POSIXEventHandler();
			POSIXSignalHandler.removeHandler(signal, handler);
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}

		Tests.increment();
		try {
			int signal = POSIXSignalHandler.SIGXFSZ;
			POSIXEventHandler handler = new POSIXEventHandler();
			POSIXSignalHandler.setHandler(signal, handler);
		} catch (Exception e) {
			Tests.fail("POSIXSignalHandlerTest");
		}
	}
}




