// MonitorControl.java, created by Dumitru Daniliuc
// Copyright (C) 2003 Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** Abstract superclass for all monitor control policy objects. */
public abstract class MonitorControl {
    
    protected static MonitorControl defaultMonitorControl = null;

    /** Create an instance of <code>MonitorControl</code>. */
    public MonitorControl() {}

    /** Gets the monitor control policy of the given instance of
     *  <code>this</code>.
     *
     *  @return The monitor control policy object.
     */
    public static MonitorControl getMonitorControl() {
	return defaultMonitorControl;
    }

    /** Gets the monitor control policy of the given instance of
     *  <code>Object</code>.
     *
     *  @return The monitor control policy object.
     */
    public static MonitorControl getMonitorControl(Object monitor) {
	return ((MonitorControl)monitor).getMonitorControl();
    }

    /** Sets the default monitor behavior for object monitors used by
     *  synchronized statements and methods in the system. The type
     *  of the policy object determines the type of behavior. Conforming
     *  implementations must support priority ceiling emulation and
     *  priority inheritance for fixed priority preemptive threads.
     *
     *  @param policy The new monitor control policy. If null nothing happens.
     */
    public static void setMonitorControl(MonitorControl policy) {
	defaultMonitorControl = policy;
    }

    /** Has the same effect as <code>setMonitorControl()</code>, except
     *  that the policy only affects the indicated object monitor.
     *
     *  @param monitor The monitor for which the new policy will be in use.
     *                 The policy will take effect on the first attempt to
     *                 lock the monitor after the completion of this method.
     *                 If null nothing will happen.
     *  @param policy The new policy for the object. If null nothing will happen.
     */
    public static void setMonitorControl(Object monitor,
					 MonitorControl monCtl) {
	((MonitorControl)monitor).setMonitorControl(monCtl);
    }
}
