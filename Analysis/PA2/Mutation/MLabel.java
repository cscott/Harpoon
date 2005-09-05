// MLabel.java, created Fri Sep  2 10:33:20 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2.Mutation;

import java.util.Map;
import java.util.HashMap;

import harpoon.ClassFile.HField;

import harpoon.Temp.Temp;

/**
 * <code>MLabel</code> - labels for the mutation NFA.  These labels
 * will appear in the generated regular expression that covers all
 * mutated locations.
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: MLabel.java,v 1.1 2005-09-05 15:02:43 salcianu Exp $ */
public abstract class MLabel {

    public static class Field extends MLabel {
	Field(HField hf) {
	    this.hf = hf;
	}
	public final HField hf;
	public String toString() {
	    if(hf.isStatic()) {
		return hf.getDeclaringClass().getName() + "." + hf.getName();
	    }
	    return hf.getName();
	}
    }
    
    public static class Reach extends MLabel {
	public String toString() {
	    return "REACH";
	}
    }
    
    public static MLabel field2mlabel(HField hf) {
	// hf == null means that all reachable objects may be mutated
	// special label "REACH"
	if(hf == null) return reach;
	
	MLabel mlabel = hf2mlabel.get(hf);
	if(mlabel == null) {
	    mlabel = new Field(hf);
	    hf2mlabel.put(hf, mlabel);
	}
	return mlabel;
    }
    private final static MLabel reach = new Reach();
    private final static Map<HField,MLabel> hf2mlabel = new HashMap<HField,MLabel>();
    
    
    public static class Param extends MLabel {
	Param(Temp temp, String name) {
	    this.temp = temp;
	    if(name == null) name = temp.toString();
	    this.name = name;
	}
	public final Temp temp;
	public final String name;
	public String toString() { return name; }
    }
    
    public static class ReachFromStat extends MLabel {
	public String toString() {
	    return "REACHfromSTAT";
	}
    }
    public static final MLabel reachFromStat = new ReachFromStat();
}
