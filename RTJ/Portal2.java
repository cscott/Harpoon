/* A more natural example */
import javax.realtime.*;

public class Portal2 extends NoHeapRealtimeThread {
    public static Integer i;

    public Portal2(MemoryArea ma) { super(null, ma); }
    
    public void run() {
	getMemoryArea().setPortal(new Integer(1+2));
    }

    public static void main(String args[]) {
	final VTMemory vt = new VTMemory();
	Portal2 p = new Portal2(vt);
	p.start();
	vt.enter(new Runnable() {
	    public void run() {
		while (vt.getPortal()==null) {}
		try {
		    i = (Integer)ImmortalMemory.instance().newInstance(
		      Integer.class.getConstructor(new Class[] { int.class }),
		      new Object[] { vt.getPortal() });
		} catch (Exception e) {}
	    }
	});
	System.out.println(i);
    }
}

