// EqTempSets.java, created Thu May 25 20:26:25 2000 by pnkfelix
/// EqTempSets.java, created Thu May 25 19:42:05 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Util.Collections.DisjointSet;
import harpoon.Util.CombineIterator;
import harpoon.Util.Util;
import harpoon.Temp.Temp;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * <code>EqTempSets</code> tracks a set of disjoint set of temps, and
 * potentially associates each set with a favored register temp (which
 * itself is not part of the set)
 * <p>
 *  Overview: an EqTempSets is a pair &lt;S,M&gt;, where <ul>
 *  <li>   S is a set of disjoint sets of temps
 *  <li>   M is a mapping from disjoint sets to register temps. </ul>
 *  Each element of S is represented by one of its members,
 *  called the Representative (or Rep for short).
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: EqTempSets.java,v 1.1.2.10 2001-11-08 00:21:59 cananian Exp $
 */
public abstract class EqTempSets implements harpoon.Temp.TempMap {
    
    /** Constructs and returns a new <code>EqTempSets</code>.  The
	returned <code>EqTempSets</code> will have a usable toString()
	method (but have less efficent operation) if
	the <code>printable</code> argument is true.
     */
    public static EqTempSets make(RegAlloc ra, boolean printable) { 
	EqTempSets eqt;
	if (printable) {
	    eqt = new EqTempSets1();
	} else {
	    eqt = new EqTempSets2();
	}
	eqt.ra = ra;
	return eqt;
    }

    protected RegAlloc ra; // needed for isRegister operation

    // maps set-representatives to the register associated with that
    // set.  Undefined for sets not associated with a register.
    protected HashMap repToReg = new HashMap();
    
    /** Associates <code>t</code> with <code>reg</code>.
	effects: M_post = { getRep( `t' ) -> `reg' } + M 
     */
    public void associate(Temp t, Temp reg) {
	repToReg.put(getRep(t), reg);
    }
    
    /** Returns the register temp associated with <code>t</code>. 
	effects: if `t' is a register temp, returns `t'.
	         else if the set for `t' has an 
		         register `r', returns `r'
		 else returns null
    */
    public Temp getReg(Temp t) {
	if (ra.isRegister(t)) 
	    return t;
	else {
	    Temp rep = getRep(t);
	    if (repToReg.containsKey(rep)) {
		return (Temp) repToReg.get(rep);
	    } else {
		return null;
	    }
	}
    }
    
    public Temp tempMap(Temp t) {
	Temp r = getReg(t);
	if (r == null) r = getRep(t);
	return r;
    }

    // optional operation that makes the sets in this immutable.
    void lock() { return; }
    
    /** Adds an equivalency between <code>t1</code> and
	<code>t2</code> to <code>this</code>.
	requires: <t1> or <t2> is not a register 
	modifies: this
	effects: puts <t1> and <t2> in the same equivalence class,
                 unifying all the temps in the two equivalence
		 classes for <t1> and <t2>
		 unless:
		 - one of the temps is a register and the equivalence
		   set for the other temp already has a register 
		   => no modification to this
    */
    public void add(Temp t1, Temp t2) {
	Util.assert( (!ra.isRegister(t1)) ||
		     (!ra.isRegister(t2)) , "need non-register");
	
	Temp rep1 = getRep(t1);
	Temp rep2 = getRep(t2);
	
	if (ra.isRegister(t1)) {
	    if ( repToReg.containsKey(rep2) ) { 
		return;
	    } else { 
		repToReg.put(rep2, t1); 
	    }
	} else if (ra.isRegister(t2)) {
	    if (repToReg.containsKey(rep1) ) { 
		return;
	    } else { 
		repToReg.put(rep1, t2); 
	    }
	} else {
	    union(t1, t2);
	    
	    Temp newRep = getRep(t1);
	    
	    if (repToReg.containsKey(rep1)) {
		Temp reg = (Temp) repToReg.get(rep1);
		repToReg.remove(rep1);
		repToReg.put(newRep, reg);
	    }
	    if (repToReg.containsKey(rep2)) {
		Temp reg = (Temp) repToReg.get(rep2);
		repToReg.remove(rep2);
		repToReg.put(newRep, reg);
	    }
	}
    }
    
    /** Returns the rep for <code>t</code>.
	effects: if `t' is not a register, returns the set-rep for
	the set containing `t'.  If `t' is a register, returns
	`t'. 
    */
    public abstract Temp getRep(Temp t);
    
    /** Unifies <code>t1</code> and <code>t2</code>.
	requires: <t1> and <t2> are not registers
	modifies: this
	effects: unifies the equivalence classes for t1 and t2,
	         removing the old equivalence sets and creating a
		 new one whose rep is either t1 or t2.
    */
    protected abstract void union(Temp t1, Temp t2);
    
}

// (inefficient implementation with useful toString)
class EqTempSets1 extends EqTempSets {
    private Map tempToSet = new HashMap();
    private Map setToRep = new HashMap();
    private List sets = new ArrayList();
    
    private boolean locked = false;
    
    void lock() {
	tempToSet = Collections.unmodifiableMap(tempToSet);
	setToRep = Collections.unmodifiableMap(setToRep);
	sets = Collections.unmodifiableList(sets);
	locked = true;
    }
    
    public Temp getRep(Temp t) {
	if (ra.isRegister(t)) return t;
	Set s = (Set) tempToSet.get(t);
	if (s == null) {
	    if (locked) return t;

	    s = new HashSet();
	    sets.add(s);
	    s.add(t);
	    tempToSet.put(t, s);
	    setToRep.put(s, t);
	    return t;
	} else {
	    return (Temp) setToRep.get(s);
	}
    }
    protected void union(Temp t1, Temp t2) {
	Set s1 = (Set) tempToSet.get(t1);
	if (s1 == null) s1 = new HashSet();
	Set s2 = (Set) tempToSet.get(t2);
	if (s2 == null) s2 = new HashSet();
	
	HashSet s3 = new HashSet(s1);
	s3.addAll(s2);
	Iterator iter = new CombineIterator(s1.iterator(), s2.iterator());
	while(iter.hasNext()) {
	    Temp t = (Temp) iter.next();
	    tempToSet.put(t, s3);
	}
	setToRep.remove(s1);  sets.remove(s1);
	setToRep.remove(s2);  sets.remove(s2);
	setToRep.put(s3, t1); sets.add(s3);
    }
    public String toString() {
	StringBuffer sb = new StringBuffer(sets.size()*32);
	Iterator iter = sets.iterator();	
	int i=0;
	while(iter.hasNext()) {
	    i++;
	    Set s = (Set) iter.next();
	    sb.append("< Set"+i+": ");
	    sb.append(s.toString());
	    Object rep = setToRep.get(s);
	    sb.append(", Rep:" + rep);
	    sb.append(", Reg:" + repToReg.get(rep)+" >");
	    if (iter.hasNext()) sb.append("\n");
	}
	return sb.toString();
    }
}

// efficient implementation that will replace instances of
// EqTempSets1 once the code using EqTempSets1 has been debugged
// completely and EqTempSets1.toString() is not needed. 
class EqTempSets2 extends EqTempSets {
    private DisjointSet dss = new DisjointSet();
    
    public Temp getRep(Temp t) {
	if (ra.isRegister(t))
	    return t;
	else 
	    return (Temp) dss.find(t);
    }
    
    protected void union(Temp t1, Temp t2) {
	dss.union(t1, t2);
    }
}
