package javax.realtime;

/** Abstract superclass for all monitor control policy objects. */
public abstract class MonitorControl {
    
    protected static MonitorControl defaultMonitorControl = null;

    public MonitorControl() {}

    /** Return the system default monitor control policy. */
    public static MonitorControl getMonitorControl() {
	return defaultMonitorControl;
    }

    /** Return the monitor control policy for the given object. */
    public static MonitorControl getMonitorControl(Object monitor) {
	return ((MonitorControl)monitor).getMonitorControl();
    }

    /** Control the default monitor behavior for object monitors used
     *  by synchronized statements and methods in the system. The type
     *  of the policy object determines the type of behavior. Conforming
     *  implementations must support priority ceiling emulation and
     *  priority inheritance for fixed priority preemptive threads.
     */
    public static void setMonitorControl(MonitorControl policy) {
	defaultMonitorControl = policy;
    }

    /** Has the same effect as <code>setMonitorControl()</code>, except
     *  that the policy only affects the indicated object monitor.
     */
    public static void setMonitorControl(Object monitor,
					 MonitorControl monCtl) {
	((MonitorControl)monitor).setMonitorControl(monCtl);
    }
}
