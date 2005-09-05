// ParamInfo.java, created Mon Sep  5 07:52:29 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2.Mutation;

import harpoon.ClassFile.HClass;
import harpoon.Temp.Temp;

import harpoon.Analysis.PA2.PANode;

import jpaul.DataStructs.DSUtil;

/**
 * <code>ParamInfo</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: ParamInfo.java,v 1.1 2005-09-05 16:38:57 salcianu Exp $
 */
public class ParamInfo {

    ParamInfo(Temp temp, HClass type, String declName, PANode node) {
	this.temp = temp;
	this.type = type;
	this.declName = declName;
	this.node = node;
    }
    private final Temp   temp;
    private final HClass type;
    private final String declName;
    private final PANode node;
    
    public Temp temp() { return temp; }
    public HClass type() { return type; }
    public String declName() { 
	if(declName == null) {
	    return temp.name();
	}
	return declName;
    }
    public PANode node() { return node; }
    
    public int hashCode() {
	if(hashCode == 0) {
	    hashCode = 
		temp.hashCode() + type.hashCode() + 
		declName.hashCode() + node.hashCode();
	}
	return hashCode;
    }
    private int hashCode;
    
    public boolean equals(Object obj) {
	if(obj == null) return false;
	if(obj == this) return true;
	if(!(obj instanceof ParamInfo)) return false;
	ParamInfo pi2 = (ParamInfo) obj;
	return 
	    DSUtil.checkEq(this.temp, pi2.temp) &&
	    DSUtil.checkEq(this.type, pi2.type) &&
	    DSUtil.checkEq(this.declName, pi2.declName) &&
	    DSUtil.checkEq(this.node, pi2.node);
    }
}
