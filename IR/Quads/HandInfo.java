// HandInfo.java, created Tue Aug 10 17:17:02 1999 by root
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

/**
 * <code>HandInfo</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: HandInfo.java,v 1.2 2002-02-25 21:05:12 cananian Exp $
 */

import harpoon.ClassFile.HClass;
import harpoon.IR.Quads.Quad;
import java.util.Map;

class HandInfo {
    //Need to denote
    //any handler [hclass=null, anyhandler=true]
    //default exit point [hclass=null, anyhandler=false]
    //other possiblities [hclas=hclass]
    
    private boolean anyhandler;
    private Quad handler;
    private HClass hclass;
    private int edge;
    private Map hm;

    HandInfo(boolean anyhandler, Quad handler, int edge, Map hm) {
	this.anyhandler=anyhandler;
	this.handler=handler;
	this.hclass=null;
	this.edge=edge;
	this.hm=hm;
    }
    HandInfo(HClass hclass, Quad handler, int edge, Map hm) {
	this.hclass=hclass;
	this.handler=handler;
	this.anyhandler=false;
	this.edge=edge;
	this.hm=hm;
    }
    Quad handler() {
	return handler;
    }
    int handleredge() {
	return edge;
    }
    Map map() {
	return hm;
    }
    boolean specificex() {
	return (hclass!=null);
    }
    boolean anyhandler() {
	return ((hclass==null)&&(anyhandler==true));
    }
    HClass hclass() {
	return hclass;
    }
}
