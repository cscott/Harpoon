// HandInfo.java, created Tue Aug 10 17:17:02 1999 by root
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

/**
 * <code>HandInfo</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: HandInfo.java,v 1.1.2.1 1999-08-10 21:58:21 bdemsky Exp $
 */

import harpoon.ClassFile.HClass;
import harpoon.IR.Quads.Quad;

class HandInfo {
    //Need to denote
    //any handler [hclass=null, anyhandler=true]
    //default exit point [hclass=null, anyhandler=false]
    //other possiblities [hclas=hclass]
    
    private boolean anyhandler;
    private Quad handler;
    private HClass hclass;
    private int edge;

    HandInfo(boolean anyhandler, Quad handler, int edge) {
	this.anyhandler=anyhandler;
	this.handler=handler;
	this.hclass=null;
	this.edge=edge;
    }
    HandInfo(HClass hclass, Quad handler, int edge) {
	this.hclass=hclass;
	this.handler=handler;
	this.anyhandler=false;
	this.edge=edge;
    }
    Quad handler() {
	return handler;
    }
    int handleredge() {
	return edge;
    }
    boolean specificex() {
	return (hclass!=null);
    }
    boolean anyhandler() {
	return ((hclass==null)&&(anyhandler==true));
    }
    boolean defaultexit() {
	return ((hclass==null)&&(anyhandler==false));
    }
    HClass hclass() {
	return hclass;
    }
}
