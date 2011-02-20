// Scope.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.rtj;

import imagerec.graph.*;
import javax.realtime.MemoryArea;
import javax.realtime.ScopedMemory;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

/**
 *
 *
 */

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
