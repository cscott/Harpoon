package javax.realtime;

import java.util.HashMap;

/** Use instances of <code>AsyncEvent</code> to handle POSIX signals. Usage:
 *  <p>
 *  <code>
 *      POSIXSignalHandler.addHandler(SIGINT, intHandler);
 *  </code>
 *  <p>
 *  This class is required to be implemented only if the underlying
 *  system supports POSIX signals.
 */
public final class POSIXSignalHandler {

    // Spec says all these fields should be final, but I don't see how
    // to make them final and to guarantee the "platform independence".
    // So I just "un-final-ed" them, and I'll set them at the runtime,
    // using JNI.

    /** Used by abort, replace <code>SIBIOT</code> in the future. */
    public static /*final*/ int SIGABRT;
    /** Alarm clock. */
    public static /*final*/ int SIGALRM;
    /** Bus error. */
    public static /*final*/ int SIGBUS;
    /** Thread cancellation signal used by <code>libthread</code>. */
    public static /*final*/ int SIGCANCEL;
    /** Child status change alis (POSIX). */
    public static /*final*/ int SIGCHLD;
    /** Child status change. */
    public static /*final*/ int SIGCLD;
    /** Stopped process has been continued. */
    public static /*final*/ int SIGCONT;
    /** EMT instructions. */
    public static /*final*/ int SIGEMT;
    /**  Floaating point exception. */
    public static /*final*/ int SIGFPE;
    /** Special signal used by CPR. */
    public static /*final*/ int SIGFREEZE;
    /** Hangup. */
    public static /*final*/ int SIGHUP;
    /** Illegal instruction (not reset when caught). */
    public static /*final*/ int SIGILL;
    /** Interrupt (rubout). */
    public static /*final*/ int SIGINT;
    /** Socked I/O possible (<code>SIGPOLL alias). */
    public static /*final*/ int SIGIO;
    /** IOT instruction. */
    public static /*final*/ int SIGIOT;
    /** Kill (cannot be caught or ignored). */
    public static /*final*/ int SIGKILL;
    /** Resource lost (e.g., record-lock lost). */
    public static /*final*/ int SIGLOST;
    /** Special signal used by thread library. */
    public static /*final*/ int SIGLWP;
    /** Write on a pipe with no one to read it. */
    public static /*final*/ int SIGPIPE;
    /** Pollable event occured. */
    public static /*final*/ int SIGPOLL;
    /** Profiling timer expired. */
    public static /*final*/ int SIGPROF;
    /** Power-fail restart. */
    public static /*final*/ int SIGPWR;
    /** Quit (ASCII FS). */
    public static /*final*/ int SIGQUIT;
    /** Segmentation violation. */
    public static /*final*/ int SIGSEGV;
    /** Stop (cannot be caught or ignored). */
    public static /*final*/ int SIGSTOP;
    /** Bad argument to system call. */
    public static /*final*/ int SIGSYS;
    /** Software termination signal from kill. */
    public static /*final*/ int SIGTERM;
    /** Special signal used by CPR. */
    public static /*final*/ int SIGTHAW;
    /** Trace trap (not reset when caught). */
    public static /*final*/ int SIGTRAP;
    /** User stop requested from tty. */
    public static /*final*/ int SIGTSTP;
    /** Background tty read attempted. */
    public static /*final*/ int SIGTTIN;
    /** Background tty write attempted. */
    public static /*final*/ int SIGTTOU;
    /** Urgent socket condition. */
    public static /*final*/ int SIGURG;
    /** User defined signal = 1. */
    public static /*final*/ int SIGUSR1;
    /** User defined signal = 2. */
    public static /*final*/ int SIGUSR2;
    /** Virtual timer expired. */
    public static /*final*/ int SIGVTALRM;
    /** Process's lwps are blocked. */
    public static /*final*/ int SIGWAITING;
    /** Window size change. */
    public static /*final*/ int SIGWINCH;
    /** Exceeded cpu limit. */
    public static /*final*/ int SIGXCPU;
    /** Exceeded file size limit. */
    public static /*final*/ int SIGXFSZ;

    // I don't know what are the codes of all these signals, but I guess they are all <50.
    // If you know that any of these signals has a code >50, just change MAX_SIG.
    private static final int MAX_SIG = 50;
    private static AsyncEvent[] signalsHandlersList = new AsyncEvent[MAX_SIG];
    
    public POSIXSignalHandler() {
	setSignals();
	for (int i = 0; i < MAX_SIG; i++) signalsHandlersList[i] = new AsyncEvent();
    }

    /** Add the given <code>AsyncEventHandler</code> to the list of
     *  handlers of the <code>AsyncEvent</code> of the given signal.
     */
    public static void addHandler(int signal, AsyncEventHandler handler) {
	signalsHandlersList[signal].addHandler(handler);
    }

    /** Remove the given <code>AsyncEventHandler</code> to the list
     *  of handlers of the <code>AsyncEvent</code> of the given signal.
     */
    public static void removeHandler(int signal, AsyncEventHandler handler) {
	signalsHandlersList[signal].removeHandler(handler);
    }

    /** Set the given <code>AsyncEventHandler</code> as the handler
     *  of the <code>AsyncEvent</code> of the given signal.
     */
    public static void setHandler(int signal, AsyncEventHandler handler) {
	signalsHandlersList[signal].setHandler(handler);
    }

    /** Assigns machine-dependent values to all <code>SIGxxxx</code> constants. */
    private native void setSignals();
}
