package javax.realtime;

public abstract class MonitorControl {
    /** Abstract superclass for all monitor control policy objects.
     */
    
    protected static MonitorControl defaultMonitorControl = null;

    public MonitorControl() {
	// TODO
    }

    public static MonitorControl getMonitorControl() {
	return defaultMonitorControl;
    }
    
    public static MonitorControl getMonitorlControl(Object Monitor) {
	// TODO

	return null;
    }
    
    public static void setMonitorControl(MonitorControl policy) {
	defaultMonitorControl = policy;
    }
    
    public static void setMonitorControl(Object monitor,
					 MonitorControl monCtl) {
	// TODO
    }
}
