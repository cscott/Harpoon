// GenType.java, created Tue Mar  7 18:32:52 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MetaMethods;

import harpoon.ClassFile.HClass;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.PointerAnalysis.PAWorkList;

import harpoon.Util.Util;

/**
 * <code>GenType</code> models a type. Although we can always record
 the possible types of a variable as a set of <code>HClass</code>es,
 this is inneficient in most of the cases. <code>GenType</code> tries
 to cope with this by representing both monomorphic types (which
 correspond to a single <code>HCode</code>) and polymorphic types
 (which correspond to a set of <code>HClass</code>es, a type cone
 rooted in a specific <code>HClass</code>)
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: GenType.java,v 1.3 2002-02-26 22:40:59 cananian Exp $
 */
public class GenType implements java.io.Serializable {

    /** Monomorphic type. This is an exact type, just the point 
	<code>hclass</code> into the type space. */
    public static final int MONO = 1;
    /** Polymorphic type. This is a cone into the type space, rooted in
	<code>hclass</code> and containing <code>hclass</code> and all its
	subtypes (subclasses). */
    public static final int POLY = 2;

    HClass hclass = null;
    int    kind   = POLY;

    /** Creates a <code>GenType</code>. <code>kind</code> should be <code>MONO</code>
     or <code>POLY</code>. */
    public GenType(HClass hclass, int kind) {
	Util.ASSERT((kind == MONO) || (kind == POLY) ,
		    "kind should be GenType.MONO or GenType.POLY");
        this.hclass = hclass;
	this.kind   = kind;
    }

    /** Creates a polymorphic type, having <code>hclass</code> as superclass.
     */
    public GenType(HClass hclass){
	this(hclass,POLY);
    }

    /** Checks whether this is a polymorphic type. */
    public boolean isPOLY(){
	return kind == POLY;
    }

    /** Returns the underlying <code>HClass</code>. 
	If <code>this</code> general type represents a monomorphic type,
	the result is exactly that type. Otherwise, for polymorphic types,
	the result is the root of the type cone. */
    public HClass getHClass(){
	return hclass;
    }

    /** Checks the equality of <code>this</code> object with object
	<code>o</code>. */
    public boolean equals(Object o){
	GenType gt2 = (GenType) o;
	return (kind == gt2.kind) && hclass.equals(gt2.hclass);
    }

    private transient int hash = 0;
    /** Computes the hash code of <code>this</code> object. */
    public int hashCode(){
	if(hash == 0)
	    hash = hclass.hashCode() + kind;
	return hash;
    }

    /** Checks whether <code>this</code> general type is included into the
	set of types abstracted by the general type <code>gt2</code>. */
    public boolean included(GenType gt2, ClassHierarchy ch){
	if(gt2 == null) return false;
	if(equals(gt2)) return true;

	// if the other type is monomorphic then either this type is
	// monomorphic and different (equals returned false) and so this type
	// is not included in it, or this is polymorphic and again gt2 cannot
	// include this.
	if(!gt2.isPOLY()) return false;

	// now, gt2 is known to be polymorphic, we have to check if hclass is
	// included in the gt2 type cone <=> hclass is a subclass of gt2.hclass
	// generate all the ancestors of hclass and see if gt2.hclass is among
	// them.

	PAWorkList W = new PAWorkList();
	W.add(hclass);
	while(!W.isEmpty()){
	    HClass c = (HClass) W.remove();

	    HClass parent = c.getSuperclass();
	    if(parent != null){
		if(parent.equals(gt2.hclass)) return true;
		W.add(parent);
	    }
	    
	    HClass[] interfs = c.getInterfaces();
	    for(int i = 0; i < interfs.length ; i++){
		if(interfs[i].equals(gt2.hclass)) return true;
		W.add(interfs[i]);
	    }
	}
	return false;
    }

    /** Pretty printer for debug purposes. */
    public String toString(){
	return "<" + hclass.toString() + "," + (isPOLY()?"P":"M") + ">"; 
    }

}
