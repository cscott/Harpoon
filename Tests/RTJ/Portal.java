import javax.realtime.*;

public class Portal extends RealtimeThread {
    public boolean set = false;
    public static LTMemory ctA = new LTMemory(502, 1004);
    public LTMemory ctBC = null;
    public static Portal p;

    Portal(MemoryArea ma) { super(null, null, null, ma, null, null); }

    public void run() {
	if (ctBC == null) (ctBC = new LTMemory(0, 952)).enter(this);
	else if (set) ctA.setPortal(ctBC);
	else if (ctA.getPortal() == null) 
	    (ctA = new LTMemory(386, 1063)).enter(p);
    }

    public static void main(String args[]) {
	(new Portal(ctA)).start();
	try { 
	    p = (Portal)ImmortalMemory.instance().newInstance(Portal.class);
	} catch (Exception e) {}
	p.set = true;
	ctA.enter(new Runnable() {
	    public void run() { p.start(); }
	});
    }
}
