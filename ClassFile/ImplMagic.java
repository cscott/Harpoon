// ImplMagic.java, created Fri Oct 16 00:29:13 1998 by cananian
package harpoon.ClassFile;

import harpoon.ClassFile.Raw.Attribute.*;
import harpoon.ClassFile.Raw.Constant.*;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
/**
 * <code>ImplMagic</code> provides concrete implementation for
 * <code>HClass</code>, <code>HMethod</code>, <code>HConstructor</code>,
 * and <code>HField</code> using the <code>harpoon.ClassFile.Raw</code>
 * package.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ImplMagic.java,v 1.2 1998-10-16 08:54:09 cananian Exp $
 */

abstract class ImplMagic  { // wrapper for the Real McCoy.
    static Hashtable preload = new Hashtable();
    static {
	for (Enumeration e=Loader.preloadable(); e.hasMoreElements(); ) {
	    try {
		ZipFile zf = new ZipFile((String)e.nextElement());
		for (Enumeration ee=zf.entries(); ee.hasMoreElements(); ) {
		    ZipEntry ze = (ZipEntry) ee.nextElement();
		    if (!ze.getName().toLowerCase().endsWith(".class"))
			continue; // not a .class file.
		    if (!ze.getName().toLowerCase().startsWith("java"))
			continue; // not worth preloading.
		    InputStream is = zf.getInputStream(ze);
		    harpoon.ClassFile.Raw.ClassFile raw =
			new harpoon.ClassFile.Raw.ClassFile(is);
		    System.err.println("Preloaded "+raw.this_class().name());
		    preload.put(raw.this_class().name(), raw);
		    is.close();
		}
		/*
		java.io.FileInputStream fis 
		    = new java.io.FileInputStream((String)e.nextElement());
		java.util.zip.ZipInputStream zis
		    = new java.util.zip.ZipInputStream(fis);
		while(true) {
		    ZipEntry ze = zis.getNextEntry();
		    if (!ze.getName().toLowerCase().endsWith(".class"))
			continue; // not a .class file.
		    if (!ze.getName().toLowerCase().startsWith("java"))
			continue; // not worth preloading.
		    harpoon.ClassFile.Raw.ClassFile raw =
			new harpoon.ClassFile.Raw.ClassFile(zis);
		    System.err.println("Preloaded "+raw.this_class().name());
		    preload.put(raw.this_class().name(), raw);
		    zis.closeEntry();
		}
		*/
	    } catch (java.io.IOException ex) { /* ignore */ }
	}
    }

    static HClass forPath(String path) {
	harpoon.ClassFile.Raw.ClassFile r = 
	    (harpoon.ClassFile.Raw.ClassFile) preload.get(path);
	if (r!=null) 
	    preload.remove(path);
	else {
	    InputStream is =
		Loader.getResourceAsStream(Loader.classToResource(path));
	    if (is!=null)
		r = new harpoon.ClassFile.Raw.ClassFile(is);
	    System.err.println("Loading "+path);
	}
	if (r==null) throw new NoClassDefFoundError(path);
	return new MagicClass(r);
    }

    static HClass forStream(InputStream is) throws java.io.IOException{
	harpoon.ClassFile.Raw.ClassFile raw =
	    new harpoon.ClassFile.Raw.ClassFile(is);
	return new MagicClass(raw);
    }

    static class MagicClass extends HClassCls {
	/** Creates a <code>MagicClass</code> from a 
	 *  <code>harpoon.ClassFile.Raw.ClassFile</code>. */
	MagicClass(harpoon.ClassFile.Raw.ClassFile classfile) {
	    this.name = classfile.this_class().name().replace('/','.');
	    this.register();
	    this.superclass = (classfile.super_class == 0)?null:
		forName(classfile.super_class().name().replace('/','.'));
	    this.interfaces = new HClass[classfile.interfaces_count()];
	    for (int i=0; i<interfaces.length; i++)
		interfaces[i] = 
		    forName(classfile.interfaces(i).name().replace('/','.'));
	    this.modifiers = classfile.access_flags.access_flags;
	    this.declaredFields = new HField[classfile.fields.length];
	    for (int i=0; i<declaredFields.length; i++)
		declaredFields[i] = new MagicField(this, classfile.fields[i]);
	    this.declaredMethods = new HMethod[classfile.methods.length];
	    for (int i=0; i<declaredMethods.length; i++)
		declaredMethods[i] = // constructors are different.
		    (classfile.methods[i].name().equals("<init>"))
		    ?(HMethod)new MagicConstructor(this, classfile.methods[i]) 
		    :(HMethod)new MagicMethod(this, classfile.methods[i]);
	    this.sourcefile = "";
	    for (int i=0; i<classfile.attributes.length; i++)
		if (classfile.attributes[i] instanceof AttributeSourceFile) {
		    this.sourcefile =
			((AttributeSourceFile)classfile.attributes[i])
			.sourcefile();
		    break;
		}
	} 
    } // END MagicClass
    
    // utility function to initialize HMethod/HConstructor.
    static private final void initMethod(HMethod _this, HClass parent,
		      harpoon.ClassFile.Raw.MethodInfo methodinfo) {
	_this.parent = parent;
	_this.name = methodinfo.name();
	_this.modifiers = methodinfo.access_flags.access_flags;
	{ // returnTypes
	    String desc = methodinfo.descriptor();
	    // snip off everything but the return value descriptor.
	    desc = desc.substring(desc.lastIndexOf(')')+1);
	    _this.returnType = HClass.forDescriptor(desc);
	}
	{ // parameterTypes
	    String desc = methodinfo.descriptor();
	    // snip off everything but the parameter list descriptors.
	    desc = desc.substring(1, desc.lastIndexOf(')'));
	    Vector v = new Vector();
	    for (int i=0; i<desc.length(); i++) {
		// make HClass for first param in list.
		v.addElement(HClass.forDescriptor(desc.substring(i)));
		// skip over the one we just added.
		while (desc.charAt(i)=='[') i++;
		if (desc.charAt(i)=='L') i=desc.indexOf(';', i);
	    }
	    _this.parameterTypes = new HClass[v.size()];
	    v.copyInto(_this.parameterTypes);
	}
	// Make sure our parsing/construction is correct.
	//System.out.println(_this.getDescriptor()+" vs "+methodinfo.descriptor());
	Util.assert(_this.getDescriptor().equals(methodinfo.descriptor()));
	
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
		Vector v = new Vector();
		for (int i=0; i<exceptions.number_of_exceptions(); i++) {
		    ConstantClass cc = exceptions.exception_index_table(i);
		    if (cc != null)
			v.addElement(HClass.forName(cc.name()
						    .replace('/','.')));
		}
		_this.exceptionTypes = new HClass[v.size()];
		v.copyInto(_this.exceptionTypes);
	    }
	}
	_this.isSynthetic = (synthetic!=null);
	
	// Add the default code representation, if method is not native.
	if (!Modifier.isNative(_this.getModifiers()) &&
	    !Modifier.isAbstract(_this.getModifiers()))
	    _this.putCode(new harpoon.IR.Bytecode.Code(_this, methodinfo));
    }

    static class MagicMethod extends HMethod {
	/** Creates a <code>MagicMethod</code> from a 
	 *  <code>harpoon.ClassFile.Raw.MethodInfo</code>. */
	MagicMethod(HClass parent, 
		    harpoon.ClassFile.Raw.MethodInfo methodinfo) {
	    initMethod(this, parent, methodinfo);
	}
    } // END MagicMethod

    static class MagicConstructor extends HConstructor {
	/** Creates a <code>MagicConstructor</code> from a 
	 *  <code>harpoon.ClassFile.Raw.MethodInfo</code>. */
	MagicConstructor(HClass parent,
			 harpoon.ClassFile.Raw.MethodInfo methodinfo) {
	    initMethod(this, parent, methodinfo);
	}
    } // END MagicConstructor

    static class MagicField extends HField {
	/** Creates a <code>MagicField</code> from a 
	 *  <code>harpoon.ClassFile.Raw.FieldInfo</code>. */
	MagicField(HClass parent, 
		   harpoon.ClassFile.Raw.FieldInfo fieldinfo) {
	    this.parent = parent;
	    this.type = HClass.forDescriptor(fieldinfo.descriptor());
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
    } // END MagicField
}
