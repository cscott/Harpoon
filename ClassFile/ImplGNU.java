// ImplGNU.java, created Fri Oct 16 00:17:32 1998 by cananian
package harpoon.ClassFile;

import gnu.bytecode.*;
import java.util.Enumeration;
import java.util.Vector;
/**
 * <code>ImplGNU</code> provides concrete implementations for
 * <code>HClass</code>, <code>HMethod</code>, and <code>HField</code>
 * using the <code>gnu.bytecode</code> package.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ImplGNU.java,v 1.2 1998-10-16 07:30:52 cananian Exp $
 */

abstract class ImplGNU  { // wrapper for the RealStuff (TM)

    static HClass forStream(java.io.InputStream is) throws java.io.IOException{
	gnu.bytecode.ClassType ct =
	    gnu.bytecode.ClassFileInput.readClassType(is);
	return new GNUClass(ct);
    }

    // make an HClass from gnu.bytecode.ClassType
    static class GNUClass extends HClassCls { 
	/** Create a <code>GNUClass</code> from a 
	 *  <code>gnu.bytecode.ClassType</code>. */
	GNUClass(gnu.bytecode.ClassType classtype) {
	    this.name = classtype.getName();
	    register();
	    this.superclass = (classtype.getSuper()==null)?null:
		forName(classtype.getSuper().getName());
	    if (classtype.getInterfaces()==null)
		this.interfaces=new HClass[0];
	    else {
		ClassType[] ctin = classtype.getInterfaces();
		this.interfaces = new HClass[ctin.length];
		for (int i=0; i<ctin.length; i++)
		    this.interfaces[i] = forName(ctin[i].getName());
	    }
	    this.modifiers  = classtype.getModifiers();
	    {
		Vector v = new Vector();
		for(Enumeration e=classtype.fields(); e.hasMoreElements(); ){
		    Field f = (Field) e.nextElement();
		    v.addElement(new GNUField(this, f));
		}
		this.declaredFields = new HField[v.size()];
		v.copyInto(this.declaredFields);
	    }
	    {
		// Read methods from classtype.methods.
		Vector v = new Vector();
		for(Enumeration e=classtype.constructors();
		    e.hasMoreElements(); ) {
		    Method m = (Method) e.nextElement();
		    v.addElement(new GNUConstructor(this, m));
		}
		for(Enumeration e=classtype.methods(); e.hasMoreElements(); ){
		    Method m = (Method) e.nextElement();
		    v.addElement(new GNUMethod(this, m));
		}
		this.declaredMethods = new HMethod[v.size()];
		v.copyInto(this.declaredMethods);
	    }
	    this.sourcefile = classtype.getSourceFile();
	}
    } // END GNUClass

    // utility function to initialize HMethod/HConstructor
    static private final void initMethod(HMethod _this, HClass parent,
					 gnu.bytecode.Method m) {
	_this.parent = parent;
	_this.name = m.getName();
	_this.modifiers = m.getModifiers();
	_this.returnType =
	    HClass.forDescriptor(m.getReturnType().getSignature());
	gnu.bytecode.Type[] pt = m.getParameterTypes();
	_this.parameterTypes = new HClass[pt.length];
	for (int i=0; i<pt.length; i++)
	    _this.parameterTypes[i]=HClass.forDescriptor(pt[i].getSignature());
	_this.parameterNames = new String[pt.length]; // xxx fill in
	_this.exceptionTypes = new HClass[0]; // xxx
	_this.isSynthetic = false;
    }

    static class GNUMethod extends HMethod {
	/** Create a <code>GNUMethod</code> from a 
	 *  <code>gnu.bytecode.Method</code>. */
	GNUMethod(HClass parent, gnu.bytecode.Method m) {
	    initMethod(this, parent, m);
	}
    } // END GNUMethod

    static class GNUConstructor extends HConstructor {
	/** Create a <code>GNUConstructor</code> from a 
	 *  <code>gnu.bytecode.Method</code>. */
	GNUConstructor(HClass parent, gnu.bytecode.Method m) {
	    initMethod(this, parent, m);
	}
    } // END GNUConstructor

    static class GNUField extends HField {
	/** Create a <code>GNUField</code> from a 
	 *  <code>gnu.bytecode.Field</code>. */
	GNUField(HClass parent, gnu.bytecode.Field f) {
	    this.parent = parent;
	    this.type = HClass.forDescriptor(f.getType().getSignature());
	    this.name = f.getName();
	    this.modifiers = f.getModifiers();
	    this.constValue = null;
	    this.isSynthetic = false;
	}
    } // END GNUField
} // END ImplGNU
