// MetaMethod.java, created Tue Mar  7 15:52:47 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MetaMethods;

import java.util.List;
import java.util.Iterator;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;

import harpoon.Util.Util;

/**
 * <code>MetaMethod</code> is a specialization of a method, function of the
 types of its arguments.<br>

 For example, if we have a method <code>foo</code>
 declared as having a single parameter of type <code>Object</code>, if we
 know that in a specific call site it is called with an argument of type
 <code>A</code> and in some other call site with an argument of type
 <code>B</code>, then we can
 say that in the first case we call the meta-method consisting of method
 <code>&lt;foo,A&gt;</code> while in the second one we call the meta-method
 <code>&lt;foo,B&gt;</code>.<br>

 In languages that relies very heavily on
 inheritance and dynamic dispatch (virtual methods) such as Java, this will
 lead to a
 sparser call graph, removing some unrealizable call chains. In particular,
 it will simplify or even totally remove some artificial strongly connected
 components of pseudo mutually recursive methods.<br>

 In short, a meta-method is simply a method plus some types for its
 parameters. These types are supposed to be identically to or, preferably,
 narrower than the declared types for that method. Something you should
 keep in mind if you plan to use meta-methods is that many meta-methods
 can be generated through specialization from the <i>same</i> method. So,
 if you decide to modify only one of them, you should first make a copy
 of it, do whatever optimizations you do on that copy and modify the
 concerned call-sites to point to it.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: MetaMethod.java,v 1.3 2002-02-26 22:41:01 cananian Exp $
 */
public class MetaMethod implements java.io.Serializable {
    // Turns on some severe correctness tests.
    private static final boolean CAUTION = true;
 
    private HMethod hm;
    private GenType[] types = null;

    /** Creates a <code>MetaMethod</code> corresponding to the method
	<code>hm</code> and the types from the array "types". */
    public MetaMethod(HMethod hm, GenType[] types){
        this.hm  = hm;
	if(CAUTION){
	    HClass[] param_types = hm.getParameterTypes();
	    int nb_params = param_types.length + (hm.isStatic()?0:1);
	    if(nb_params != types.length)
		Util.ASSERT(false,"Wrong number of arguments");
	}
	this.types = new GenType[types.length];
	for(int i = 0 ; i < types.length ; i++)
	    this.types[i] = types[i];
    }

    /** Creates a <code>MetaMethod</code> corresponding to the method
	<code>hm</code> and the types declared for it. The types appearing
	in the declaration of the method are considered to be polymorphic or
	not, depending whether <code>polymorphic</code> is switched on/off. */
    public MetaMethod(HMethod hm, boolean polymorphic){
        this.hm  = hm;
	int type_kind = polymorphic?GenType.POLY:GenType.MONO;
	int skew = hm.isStatic()?0:1;
	HClass[] param_types = hm.getParameterTypes();
	types = new GenType[param_types.length + skew];
	if(!hm.isStatic())
	    types[0] = new GenType(hm.getDeclaringClass(),type_kind);
	for(int i = 0; i < param_types.length ; i++){
	    HClass hclass = param_types[i];
	    if(hclass.isPrimitive())
		types[i + skew] = new GenType(hclass, GenType.MONO);
	    else
		types[i + skew] = new GenType(hclass, type_kind);
	}
    }

    /** Creates a <code>MetaMethod</code> corresponding to the method
	<code>hm</code> and the types declared for it. All the types appearing
	in the declaration of the method are considered to be polymorphic.,
	that is we are maximally conservative in our initial estimation.
	The types of can be specialized to by using <code>setType</code>. */
    public MetaMethod(HMethod hm){
	this(hm, true);
    }

    /** Returns the <code>HMethod</code> that <code>this</code> meta-method
	is a specialization of. */
    public HMethod getHMethod(){ 
	return hm;
    }

    /** Returns the type of the <code>i</code>-th parameter. */
    public GenType getType(int i){
	return types[i];
    }

    /** Set the type of the <code>i</code>-th argument of <code>this</code>
	meta-method. The arguments are 0-indexed, with the receiver of the
	method in the first place if the method is non-static. This method
	should be used to specialize the types of the arguments. */
    public void setType(int i, GenType gt){
	types[i] = gt;
	hash = 0; // invalidate the cashed hash code
    }

    /** Returns the number of parameters of <code>this</code> metamethod. */
    public int nbParams(){
	return types.length;
    }

    // some caching hack
    private transient int hash = 0;
    /** Computes the hash code of <code>this</code> object. */
    public int hashCode(){
	if(hash != 0) return hash;
	hash = hm.hashCode();
	int nb_types = types.length;
	for(int i = 0; i < nb_types; i++)
	    hash += types[i].hashCode();
	return hash;
    }

    /** Checks the equality of <code>this</code> object with object 
	<code>o</code>. */
    public boolean equals(Object o){
	if (this == o) return true;
	MetaMethod mm2 = (MetaMethod) o;
	if(!hm.equals(mm2.hm)) return false;
	if(types.length != mm2.types.length) return false;
	for(int i = 0; i < types.length ; i++)
	    if(!types[i].equals(mm2.types[i])) return false;
	return true;
    }

    /** Checks whether two <code>MetaMethod</code>s are equal or not.
	handle <code>null</code> arguments. */
    public static boolean identical(MetaMethod mm1, MetaMethod mm2){
	if((mm1 == null) || (mm2 == null))
	    return mm1 == mm2;
	return mm1.equals(mm2);
    }

    /** Pretty printer for debug purposes. */
    public String toString(){
	StringBuffer buffer = new StringBuffer();
	buffer.append("< ");
	buffer.append(hm);
	buffer.append(" | ");
	for(int i = 0; i < types.length; i++){
	    if(i != 0) buffer.append(" ,");
	    buffer.append(types[i]);
	}
	buffer.append(" >");
	return buffer.toString();
    }


}
