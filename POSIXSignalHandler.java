package javax.realtime;

import java.util.HashMap;

public final class POSIXSignalHandler {
    /** Use instances of <code>asyncEvent</code> to ahandle POSIX signals. */

    private HashMap signalsHandlersLists = null;

    
    // Spec says all these fields should be final, but I don't see how
    // to make them final and to guarantee the "platform independence".
    // So I just "un-final-ed" them, and I'll set them at the runtime,
    // using JNI.
    public static /*final*/ int SIGABRT;
    public static /*final*/ int SIGALRM;
    public static /*final*/ int SIGBUS;
    public static /*final*/ int SIGCANCEL;
    public static /*final*/ int SIGCHLD;
    public static /*final*/ int SIGCLD;
    public static /*final*/ int SIGCONT;
    public static /*final*/ int SIGGEMT;
    public static /*final*/ int SIGFPE;
    public static /*final*/ int SIGFREEZE;
    public static /*final*/ int SIGHUP;
    public static /*final*/ int SIGILL;
    public static /*final*/ int SIGINT;
    public static /*final*/ int SIGIO;
    public static /*final*/ int SIGIOT;
    public static /*final*/ int SIGKILL;
    public static /*final*/ int SIGLOST;
    public static /*final*/ int SIGLWP;
    public static /*final*/ int SIGPIPE;
    public static /*final*/ int SIGPOLL;
    public static /*final*/ int SIGPROF;
    public static /*final*/ int SIGPWR;
    public static /*final*/ int SIGQUIT;
    public static /*final*/ int SIGSEGV;
    public static /*final*/ int SIGSTOP;
    public static /*final*/ int SIGSYS;
    public static /*final*/ int SIGTERM;
    public static /*final*/ int SIGTHAW;
    public static /*final*/ int SIGTRAP;
    public static /*final*/ int SIGTSTP;
    public static /*final*/ int SIGTTIN;
    public static /*final*/ int SIGTTOU;
    public static /*final*/ int SIGURG;
    public static /*final*/ int SIGUSR1;
    public static /*final*/ int SIGUSR2;
    public static /*final*/ int SIGVTALRM;
    public static /*final*/ int SIGWAITING;
    public static /*final*/ int SIGWINCH;
    public static /*final*/ int SIGXCPU;
    public static /*final*/ int SIGXFSZ;

    // I don't know what are the codes of all these signals, but I guess they are all <50.
    // If you know that any of these signals has a code >50, just change the number of elements.
    private AsyncEvent[] signalsHandlersList = new AsyncEvent[50];
    
    public POSIXSignalHandler() {
	// TODO
	setSignals();
    }

    public static void addHandler(int signal, AsyncEventHandler handler) {
	signalsHandlersList[signal].addHandler(handler);
    }

    public static void removeHandler(int signal, AsyncEventHandler handler) {
	signalsHandlersList[signal].removeHandler(handler);
    }

    public static void setHandler(int signal, AsyncEventHandler handler) {
	signalsHandlersList[signal].setHandler(handler);
    }

    // Not in specs, but needed for setting the "signals" fields.
    private native void setSignals();
}
