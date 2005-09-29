// ImplMagic.java, created Fri Oct 16 00:29:13 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.IR.RawClass.AttributeCode;
import harpoon.IR.RawClass.AttributeConstantValue;
import harpoon.IR.RawClass.AttributeExceptions;
import harpoon.IR.RawClass.AttributeSourceFile;
import harpoon.IR.RawClass.AttributeSynthetic;
import harpoon.IR.RawClass.ConstantClass;
import harpoon.IR.RawClass.ConstantValue;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <code>ImplMagic</code> provides concrete implementation for
 * <code>HClass</code>, <code>HMethod</code>, <code>HConstructor</code>,
 * and <code>HField</code> using the <code>harpoon.IR.RawClass</code>
 * package.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ImplMagic.java,v 1.10 2005-09-29 04:15:16 salcianu Exp $
 */
abstract class ImplMagic  { // wrapper for the Real McCoy.

    static HClass forStream(Linker linker, java.io.InputStream is)
				       throws java.io.IOException {
	harpoon.IR.RawClass.ClassFile raw =
	    new harpoon.IR.RawClass.ClassFile(is);
	return new MagicClass(linker, raw);
    }

    static class MagicClass extends HClassCls {
	/** Creates a <code>MagicClass</code> from a 
	 *  <code>harpoon.IR.RawClass.ClassFile</code>. */
	MagicClass(Linker linker, harpoon.IR.RawClass.ClassFile classfile) {
	    super(linker);
	    this.name = classfile.this_class().name().replace('/','.');

	    this.superclass = (classfile.super_class == 0)?null:
		new ClassPointer(linker,
				 "L"+classfile.super_class().name()+";");

	    this.interfaces = new HPointer[classfile.interfaces_count()];
	    for (int i=0; i<interfaces.length; i++)
		interfaces[i] = 
		    new ClassPointer(linker, 
				     "L"+classfile.interfaces(i).name()+";");

	    this.modifiers = classfile.access_flags.access_flags;

	    this.declaredFields = new HField[classfile.fields.length];
	    for (int i=0; i<declaredFields.length; i++)
		declaredFields[i] = new MagicField(this, classfile.fields[i]);

	    this.declaredMethods = new HMethod[classfile.methods.length];
	    for (int i=0; i<declaredMethods.length; i++)
		declaredMethods[i] = // constructors are different.
		    (classfile.methods[i].name().equals("<init>"))
		    ?(HMethod)new MagicConstructor(this, classfile.methods[i])
		    :(classfile.methods[i].name().equals("<clinit>"))
		    ?(HMethod)new MagicInitializer(this, classfile.methods[i])
		    :(HMethod)new MagicMethod(this, classfile.methods[i]);

	    this.sourcefile = "";
	    for (int i=0; i<classfile.attributes.length; i++)
		if (classfile.attributes[i] instanceof AttributeSourceFile) {
		    this.sourcefile =
			((AttributeSourceFile)classfile.attributes[i])
			.sourcefile();
		    break;
		}

	    // for some odd reason, interfaces have 'java.lang.Object'
	    // as their superclass in the class file format.  In the
	    // rarified "real world", interfaces have *no* superclass.
	    // So fix fantasy to match reality.  See the Java Virtual
	    // Machine Specification, section 4.1, which offers zero
	    // explanation for this strange behaviour.
	    if (isInterface()) this.superclass = null;
	} 
	// optimize hashcode.
	private transient int hashcode=0;
	public int hashCode() { // 1 in 2^32 chance of recomputing frequently.
	    if (hashcode==0) hashcode = super.hashCode();
	    return hashcode;
	}
	// [AS 09/28/05]: added to stop a really NASTY bug.  We do need
	// Bill Pugh's tool as a standard part of javac :)
	public boolean equals(Object o) {
	    return (o instanceof HClass &&
		    ((HClass)o).getDescriptor().equals(getDescriptor()));
	}
    } // END MagicClass
    
    // utility function to initialize HMethod/HConstructor/HInitializer.
    static private final void initMethod(HMethodImpl _this, HClass parent,
		      harpoon.IR.RawClass.MethodInfo methodinfo) {
	_this.parent = parent;
	_this.name = methodinfo.name();
	_this.modifiers = methodinfo.access_flags.access_flags;
	{ // returnTypes
	    String desc = methodinfo.descriptor();
	    // snip off everything but the return value descriptor.
	    desc = desc.substring(desc.lastIndexOf(')')+1);
	    _this.returnType = new ClassPointer(parent.getLinker(), desc);
	}
	{ // parameterTypes
	    String desc = methodinfo.descriptor();
	    // snip off everything but the parameter list descriptors.
	    desc = desc.substring(1, desc.lastIndexOf(')'));
	    List v = new ArrayList();
	    for (int i=0; i<desc.length(); i++) {
		// make HClass for first param in list.
		v.add(new ClassPointer(parent.getLinker(),
				       desc.substring(i)));
		// skip over the one we just added.
		while (desc.charAt(i)=='[') i++;
		if (desc.charAt(i)=='L') i=desc.indexOf(';', i);
	    }
	    _this.parameterTypes = (HPointer[])
		v.toArray(new HPointer[v.size()]);
	}
	// Make sure our parsing/construction is correct.
	// COMMENTED OUT because it was causing us to load unnecessary classes
	//assert _this.getDescriptor().equals(methodinfo.descriptor());
	
	AttributeCode code = null;
	AttributeExceptions exceptions = null;
	AttributeSynthetic synthetic = null;
	// Crunch the attribute information.
	for (int i=0; i<methodinfo.attributes.length; i++) {
	    if (methodinfo.attributes[i] instanceof AttributeCode)
		code = (AttributeCode) methodinfo.attributes[i];
	    else if (methodinfo.attributes[i] instanceof AttributeExceptions)
		exceptions=(AttributeExceptions) methodinfo.attributes[i];
	    else if (methodinfo.attributes[i] instanceof AttributeSynthetic)
		synthetic =(AttributeSynthetic) methodinfo.attributes[i];
	}
	
	{ // parameterNames
	    _this.parameterNames = new String[_this.parameterTypes.length];
	    // for non-static methods, 0th local variable is 'this'.
	    int offset = _this.isStatic()?0:1;
	    // assign names.
	    for (int i=0; i<_this.parameterNames.length; i++) {
		_this.parameterNames[i]=
		    ((code==null)?null:
		     code.localName(0/*pc*/, offset));
		// longs and doubles take up two local variable slots.
		offset += (_this.parameterTypes[i]==HClass.Double || 
			   _this.parameterTypes[i]==HClass.Long) ? 2 : 1;
	    }
	}
	{ // exceptionTypes:
	    if (exceptions == null)
		_this.exceptionTypes = new HClass[0];
	    else {
		List v = new ArrayList();
		for (int i=0; i<exceptions.number_of_exceptions(); i++) {
		    ConstantClass cc = exceptions.exception_index_table(i);
		    if (cc != null)
			v.add(new ClassPointer(parent.getLinker(),
					       "L"+cc.name()+";"));
		}
		_this.exceptionTypes = (HPointer[])
		    v.toArray(new HPointer[v.size()]);
	    }
	}
	_this.isSynthetic = (synthetic!=null);

	// Add the default code representation, if method is not native.
	if (!Modifier.isNative(_this.getModifiers()) &&
	    !Modifier.isAbstract(_this.getModifiers()))
	    repository.put(meth2str(_this), methodinfo);
    }

    // keys in repository are strings, to avoid conflict-of-linker problems.
    private static String meth2str(HMethod m) {
	return m.getDeclaringClass().getName()+"."+m.getName()+
	    m.getDescriptor();
    }

    static final Map repository = new HashMap();
    public static final HCodeFactory codeFactory = new SerializableCodeFactory() {
	public String getCodeName() 
	{ return harpoon.IR.Bytecode.Code.codename; }
	public HCode convert(HMethod m)
	{
	    harpoon.IR.RawClass.MethodInfo methodinfo =
	    (harpoon.IR.RawClass.MethodInfo) repository.get(meth2str(m));
	    if (methodinfo==null) return null;
	    else return new harpoon.IR.Bytecode.Code(m, methodinfo);
	}
	public void clear(HMethod m) {
	    repository.remove(m); // make methodinfo garbage.
	}
    };
    
    static class MagicMethod extends HMethodImpl {
	/** Creates a <code>MagicMethod</code> from a 
	 *  <code>harpoon.IR.RawClass.MethodInfo</code>. */
	MagicMethod(HClass parent, 
		    harpoon.IR.RawClass.MethodInfo methodinfo) {
	    initMethod(this, parent, methodinfo);
	}
	// optimize hashcode.
	private transient int hashcode=0;
	public int hashCode() { // 1 in 2^32 chance of recomputing frequently.
	    if (hashcode==0) hashcode = super.hashCode();
	    return hashcode;
	}
    } // END MagicMethod

    static class MagicConstructor extends HConstructorImpl {
	/** Creates a <code>MagicConstructor</code> from a 
	 *  <code>harpoon.IR.RawClass.MethodInfo</code>. */
	MagicConstructor(HClass parent,
			 harpoon.IR.RawClass.MethodInfo methodinfo) {
	    initMethod(this, parent, methodinfo);
	}
	// optimize hashcode.
	private transient int hashcode=0;
	public int hashCode() { // 1 in 2^32 chance of recomputing frequently.
	    if (hashcode==0) hashcode = super.hashCode();
	    return hashcode;
	}
    } // END MagicConstructor

    static class MagicInitializer extends HInitializerImpl {
	/** Creates a <code>MagicInitializer</code> from a
	 *  <code>harpoon.IR.RawClass.MethodInfo</code>. */
	MagicInitializer(HClass parent,
			 harpoon.IR.RawClass.MethodInfo methodinfo) {
	    initMethod(this, parent, methodinfo);
	}
	// optimize hashcode.
	private transient int hashcode=0;
	public int hashCode() { // 1 in 2^32 chance of recomputing frequently.
	    if (hashcode==0) hashcode = super.hashCode();
	    return hashcode;
	}
    } // END MagicInitializer

    static class MagicField extends HFieldImpl {
	/** Creates a <code>MagicField</code> from a 
	 *  <code>harpoon.IR.RawClass.FieldInfo</code>. */
	MagicField(HClass parent, 
		   harpoon.IR.RawClass.FieldInfo fieldinfo) {
	    this.parent = parent;
	    this.type = new ClassPointer(parent.getLinker(),
					 fieldinfo.descriptor());
	    this.name = fieldinfo.name();
	    this.modifiers = fieldinfo.access_flags.access_flags;
	    {
		AttributeConstantValue attrconst = null;
		for (int i=0; i<fieldinfo.attributes.length; i++)
		    if (fieldinfo.attributes[i] 
			instanceof AttributeConstantValue) {
			attrconst =
			    (AttributeConstantValue) fieldinfo.attributes[i];
			break;
		    }
		this.constValue = (attrconst==null) ? null :
		    ((ConstantValue)attrconst.constantvalue_index()).value();
	    }
	    this.isSynthetic = false;
	    for (int i=0; i<fieldinfo.attributes.length; i++)
		if (fieldinfo.attributes[i] instanceof AttributeSynthetic)
		    this.isSynthetic=true;
	}
	// optimize hashcode.
	private transient int hashcode=0;
	public int hashCode() { // 1 in 2^32 chance of recomputing frequently.
	    if (hashcode==0) hashcode = super.hashCode();
	    return hashcode;
	}
    } // END MagicField
}
