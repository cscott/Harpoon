import javax.realtime.*;

public class Inheritance extends NoHeapRealtimeThread {
    static class WithField { public Object f; }

    public static VTMemory vt;
    public WithField obj;

    public Inheritance(WithField o) {
	super(null, new VTMemory());
	obj = o;
    }

    public void run() {
	obj.f = vt.newInstance(Object.class);
    }

    public static void main(String args[]) {
	try {
	    vt = (VTMemory)ImmortalMemory.instance().newInstance(VTMemory.class);
	} catch (Exception e) {}
	vt.enter(new Runnable() {
	    public void run() {
		WithField o = new WithField();
		(new Inheritance(o)).start();
	    }
	});
	
    }
}
