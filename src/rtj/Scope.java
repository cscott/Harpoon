package imagerec.rtj;

import imagerec.Node;
import imagerec.ImageData;
import javax.realtime.MemoryArea;
import javax.realtime.ScopedMemory;

public class Scope extends Node {
    private MemoryArea ma;
    private ScopedMemory sm = null;

    public Scope(MemoryArea ma, Node out) {
	super(out);
	if ((this.ma = ma) instanceof ScopedMemory) {
	    sm = (ScopedMemory)ma;
	}	
    }

    public Object getPortal() {
	if (sm == null) {
	    throw new Error("Not a scoped memory!");
	} else {
	    return sm.getPortal();
	}
    }

    public void setPortal(Object obj) {
	if (sm == null) {
	    throw new Error("Not a scoped memory!");
	} else {
	    sm.setPortal(obj);
	}
    }
    
    public synchronized void process(final ImageData id) {
	ma.enter(new Runnable() {
	    public void run() {
		Scope.super.process(id);
	    }
	});
    }
}
