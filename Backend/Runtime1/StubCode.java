// StubCode.java, created Sat Oct 23 15:33:33 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeDerivation;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.METHOD;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.RETURN;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.THROW;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;

import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.List;
/**
 * <code>StubCode</code> is used to generate non-canonical tree code
 * stubs for native methods.  That is, a <code>StubCode</code> is a
 * thunk from runtime-native methods to the JNI interface which
 * C-code "native methods" conform to.<p>
 * <code>StubCode</code> will generate a thunk for any old method you
 * give to its constructor.  It is the job of the
 * <code>nativeTreeCodeFactory()</code> in <code>Runtime1.Runtime</code>
 * to weed out native from non-native methods, give the proper ones
 * to <code>StubCode</code>, and do the right thing with the stubs that
 * <code>StubCode</code> makes.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: StubCode.java,v 1.3 2003-10-21 02:11:02 cananian Exp $
 */
public class StubCode extends harpoon.IR.Tree.TreeCode {
    final TreeBuilder m_tb;
    final NameMap m_nm;
    final int EXC_OFFSET;
    final int REF_OFFSET;
    final DerivationGenerator m_dg;

    /** Creates a <code>StubCode</code>.
     * @param method The method for which to generate code.
     * @param frame The frame for the generated code.
     */
    public StubCode(HMethod method, Frame frame) {
        super(method, null, frame, new DerivationGenerator());
	this.m_nm = frame.getRuntime().getNameMap();
	this.m_tb = (TreeBuilder) frame.getRuntime().getTreeBuilder();
	// keep this synchronized with struct FNI_Thread_State in
	// Runtime/include/jni-private.h and TreeBuilder._call_FNI_Monitor()
	this.EXC_OFFSET = 1 * m_tb.POINTER_SIZE;
	this.REF_OFFSET = 3 * m_tb.POINTER_SIZE;
	this.m_dg = (DerivationGenerator) getTreeDerivation();
	this.tree = buildStub(method);
    }

    // here is the real stub builder code.
    Tree buildStub(HMethod method) {
	final HClass HCcls = frame.getLinker().forName("java.lang.Class");
	final HClass HCthw = frame.getLinker().forName("java.lang.Throwable");

	List stmlist = new ArrayList();
	// segment change
	stmlist.add(new SEGMENT(tf, null, SEGMENT.CODE));
	// add method header.
	// (remember that first method parameter in tree form is the
	//  'return exception address')
	HClass[] paramTypes = method.getParameterTypes();
	if (!method.isStatic()) {// add 'this' parameter for non-static methods
	    HClass[] nparamTypes = new HClass[paramTypes.length+1];
	    nparamTypes[0] = method.getDeclaringClass();
	    System.arraycopy(paramTypes,0,nparamTypes,1,paramTypes.length);
	    paramTypes = nparamTypes;
	}
	Temp[] paramTemps = new Temp[paramTypes.length+1];
	TEMP[] paramTEMPs = new TEMP[paramTypes.length+1];
	paramTemps[0] = new Temp(tf.tempFactory(), "rexaddr");
	paramTEMPs[0] = _TEMP(tf, null, HClass.Void, paramTemps[0]);
	for (int i=0; i<paramTypes.length; i++) {
	    paramTemps[i+1] = new Temp(tf.tempFactory(), "param"+i);
	    paramTEMPs[i+1] = _TEMP(tf, null, paramTypes[i], paramTemps[i+1]);
	}
	int rtype = (method.getReturnType()==HClass.Void) ? -1 :
	    class2type(method.getReturnType());
	stmlist.add(new METHOD(tf,null,m_nm.label(method), rtype, paramTEMPs));
	// get JNIEnv * (which is thread-local)
	Temp envT = new Temp(tf.tempFactory(), "env");
	stmlist.add(new NATIVECALL
		    (tf, null,
		     _TEMP(tf, null, HClass.Void, envT) /*retval*/,
		     _NAME(tf, null, HClass.Void,
			   new Label(m_nm.c_function_name("FNI_GetJNIEnv"))),
		     null/* no args*/));

	// reset the exception field in the thread state.
	/* CSA: i don't think we need to do this -- we clear the exception
	 * when we return. [mar-26-2000]
	stmlist.add(new MOVE(tf, null,
			     _MEM(tf, null, HClass.Void,
				     new BINOP(tf, null, Type.POINTER, Bop.ADD,
					       _TEMP(tf, null, HClass.Void,
							envT),
					       new CONST(tf, null, EXC_OFFSET)
					       )),
			     new CONST(tf, null))); // set to null.
	*/
	// save the top of the local ref stack (so we can restore it later)
	Temp refT = new Temp(tf.tempFactory(), "lref");
	stmlist.add(new MOVE(tf, null,
			     _TEMP(tf, null, HClass.Void, refT),
			     _MEM(tf, null, HClass.Void,
				     new BINOP(tf, null, Type.POINTER, Bop.ADD,
					       _TEMP(tf, null, HClass.Void,
							envT),
					       new CONST(tf, null, REF_OFFSET)
					       ))));
	// wrap objects in parameter list
	for (int i=0; i<paramTypes.length; i++)
	    if (!paramTypes[i].isPrimitive()) {
		Temp wrapped = new Temp(paramTemps[i+1]);
		stmlist.add(new NATIVECALL
			    (tf, null,
			     _TEMP
			     (tf, null, HClass.Void, wrapped),
			     _NAME
			     (tf, null, HClass.Void,
			      new Label(m_nm.c_function_name
					("FNI_NewLocalRef"))),
			     new ExpList
			      (_TEMP(tf, null, HClass.Void, envT),
			       new ExpList
			       (_TEMP(tf, null, paramTypes[i],paramTemps[i+1]),
				null))));
		// after this point, use wrapped temp
		paramTypes[i] = HClass.Void;
		paramTemps[i+1] = wrapped;
	    }
	// wrap a class object pointer for static methods.
	Temp classT = null;
	if (method.isStatic()) {
	    classT = new Temp(tf.tempFactory(), "jclass");
	    stmlist.add(new NATIVECALL
			(tf, null,
			 _TEMP
			 (tf,null,HClass.Void, classT),
			 _NAME
			 (tf,null,HClass.Void, new Label(m_nm.c_function_name
							 ("FNI_NewLocalRef"))),
			 new ExpList
			 (_TEMP(tf, null, HClass.Void, envT),
			  new ExpList
			  (_NAME(tf, null, HCcls,
				    m_nm.label(method.getDeclaringClass(),
					       "classobj")),
			   null))));
	}
	// if this method is synchronized, lock the object.
	Temp lockT = (classT != null) ? classT : paramTemps[1];
	emitMonitorLockUnlock(stmlist, envT, lockT, true/*lock*/);
	// make the native call's parameters, in reverse order
	ExpList jniParams = null;
	for (int i=paramTypes.length-1; i>=0; i--)
	    jniParams = new ExpList
		(_TEMP(tf, null, paramTypes[i], paramTemps[i+1]),
		 jniParams);
	// second parameter is either a jclass for a static method
	// or an (already added) 'this' object
	if (classT != null)
	    jniParams = new ExpList(_TEMP(tf, null, HCcls, classT),
				    jniParams);
	// first parameter is the JNIEnv *
	jniParams = new ExpList(_TEMP(tf, null, HClass.Void, envT),
				jniParams);
	// Do the call.
	Temp retT = null;
	HClass rettype = method.getReturnType();
	if (rettype!=HClass.Void) {
	    retT = new Temp(tf.tempFactory(), "retval");
	    if (!rettype.isPrimitive()) rettype=HClass.Void;//retval is wrapped
	}
	stmlist.add(new NATIVECALL(tf, null,
				   (retT==null) ? null :
				   _TEMP(tf, null, rettype, retT),
				   _NAME(tf, null, HClass.Void,
					 new Label(m_nm.c_function_name
						   (jniMangle(method)))),
				   jniParams));
	// now clean up afterward: check for exceptions, etc.
	Temp excT = new Temp(tf.tempFactory(), "excval");
	stmlist.add(new MOVE(tf, null,
			     _TEMP(tf, null, HClass.Void, excT),
			     _MEM(tf, null, HClass.Void,
				     new BINOP(tf, null, Type.POINTER, Bop.ADD,
					       _TEMP(tf, null, HClass.Void,
							envT),
					       new CONST(tf, null, EXC_OFFSET)
					       ))));
	Label no_exceptions = new Label();
	Label yes_exceptions = new Label();
	stmlist.add(new CJUMP(tf, null,
			      new BINOP(tf, null, Type.POINTER, Bop.CMPEQ,
					_TEMP(tf, null, HClass.Void, excT),
					new CONST(tf, null)/*null pointer*/),
			      no_exceptions, yes_exceptions));
	// no exceptions occurred.  unwrap return value and return from stub.
	stmlist.add(new LABEL(tf, null, no_exceptions, false));
	emitMonitorLockUnlock(stmlist, envT, lockT, false/*unlock*/);
	Exp retexp;
	if (retT==null)
	    retexp = new CONST(tf, null);
	else {
	    rettype = method.getReturnType();//reset to unwrapped type.
	    if (!rettype.isPrimitive()) {
		Temp unwrapped = new Temp(retT);
		stmlist.add(new NATIVECALL(tf, null,
					   _TEMP(tf, null, rettype, unwrapped),
					   _NAME(tf, null, HClass.Void,
						 new Label(m_nm.c_function_name
							   ("FNI_Unwrap"))),
					   new ExpList
					   (_TEMP(tf, null, HClass.Void, retT),
					    null)
					   ));
		retT = unwrapped; // after this point, use unwrapped temp
	    }
	    retexp = _TEMP(tf, null, rettype, retT);
	}
	emitFreeLocals(stmlist, envT, refT);
	stmlist.add(new RETURN(tf, null, retexp));
	// an exception occurred.  unwrap exception value and throw it.
	// (don't forget to clear the exception before we invoke any more
	//  jni functions!)
	stmlist.add(new LABEL(tf, null, yes_exceptions, false));
	stmlist.add(new NATIVECALL(tf, null, null,
				   _NAME(tf, null, HClass.Void,
					    new Label(m_nm.c_function_name
						      ("FNI_ExceptionClear"))),
				   new ExpList
				   (_TEMP(tf, null, HClass.Void, envT),
				    null)
				   ));
	emitMonitorLockUnlock(stmlist, envT, lockT, false/*unlock*/);
	Temp unwrapped = new Temp(excT);
	stmlist.add(new NATIVECALL(tf, null,
				   _TEMP(tf, null, HCthw, unwrapped),
				   _NAME(tf, null, HClass.Void,
					    new Label(m_nm.c_function_name
						      ("FNI_Unwrap"))),
				   new ExpList
				   (_TEMP(tf, null, HClass.Void, excT),
				    null)
				   ));
	excT = unwrapped; // after this point, use unwrapped temp
	emitFreeLocals(stmlist, envT, refT);
	stmlist.add(new THROW(tf, null,
			      _TEMP(tf, null, HCthw, excT),
			      _TEMP(tf, null, HClass.Void, paramTemps[0])
			      ));
	return Stm.toStm(stmlist);
    }
    private void emitMonitorLockUnlock(List stmlist, Temp envT,
				       Temp lockT, boolean isLock) {
	// if this method is not synchronized, skip this.
	if (!Modifier.isSynchronized(getMethod().getModifiers())) return;
	// okay, emit native call to FNI_MonitorEnter/Exit().
	Temp discardT = new Temp(tf.tempFactory(), "discard");
	stmlist.add(new NATIVECALL
		    (tf, null,
		     _TEMP(tf, null, HClass.Int, discardT)/* retval*/,
		     _NAME(tf, null, HClass.Void,
			   new Label(m_nm.c_function_name
				     (isLock ?
				      "FNI_MonitorEnter":"FNI_MonitorExit"))),
		     new ExpList(_TEMP(tf, null, HClass.Void, envT),
				 // note type of lockT is HClass.Void because
				 // it has been wrapped.
				 new ExpList(_TEMP(tf, null, HClass.Void,
						   lockT),  null))));
    }
    private void emitFreeLocals(List stmlist, Temp envT, Temp refT) {
    	stmlist.add(new NATIVECALL
		    (tf, null, null, // free local references
		     _NAME(tf, null, HClass.Void,
			      new Label(m_nm.c_function_name
					("FNI_DeleteLocalRefsUpTo"))),
		     new ExpList(_TEMP(tf, null, HClass.Void, envT),
		     new ExpList(_TEMP(tf, null, HClass.Void, refT),
				 null))));
    }

    private TEMP _TEMP(TreeFactory tf, HCodeElement src,
		       HClass type, Temp temp) {
	TEMP result = new TEMP(tf, src, class2type(type), temp);
	m_dg.putTypeAndTemp(result, type, temp);
	return result;
    }
    private MEM _MEM(TreeFactory tf, HCodeElement src,
		     HClass type, Exp exp) {
	MEM result = new MEM(tf, src, class2type(type), exp);
	m_dg.putType(result, type);
	return result;
    }
    private NAME _NAME(TreeFactory tf, HCodeElement src,
		       HClass type, Label label) {
	NAME result = new NAME(tf, src, label);
	m_dg.putType(result, type);
	return result;
    }
    
    private static int class2type(HClass hc) {
	if (!hc.isPrimitive()) return Type.POINTER;
	if (hc==HClass.Void) return Type.POINTER;
	if (hc==HClass.Boolean || hc==HClass.Byte || hc==HClass.Char ||
	    hc==HClass.Int || hc==HClass.Short) return Type.INT;
	if (hc==HClass.Double) return Type.DOUBLE;
	if (hc==HClass.Float) return Type.FLOAT;
	if (hc==HClass.Long) return Type.LONG;
	throw new Error("Unknown primitive type: "+hc);
    }

    private static String jniMangle(HMethod m) {
	// jni sez use short name unless two native methods would use the
	// same short name.  So, let's check that, shall we?
	boolean useShort = true;
	HMethod[] allm = m.getDeclaringClass().getMethods();
	for (int i=0; i<allm.length; i++)
	    if (Modifier.isNative(allm[i].getModifiers()) &&
		allm[i].getName().equals(m.getName()) &&
		!allm[i].equals(m))
		useShort = false;
	String mangled = "Java_" +
	    encode(m.getDeclaringClass().getName()) +
	    "_" +
	    encode(m.getName());
	if (!useShort) {
	    String desc = m.getDescriptor();
	    mangled += "__" +
		encode(desc.substring(1, desc.lastIndexOf(')')));
	}
	return mangled;
    }

    //-------- CODE BELOW THIS LINE WAS COPIED FROM DefaultNameMap.java ----
    // (so if you find a bug here, fix it there, too!)

    /** Apply the JNI-standard unicode-to-C encoding. */
    private static String encode(String s) {
	StringBuffer sb = new StringBuffer();
	for(int i=0; i<s.length(); i++) {
	    switch(s.charAt(i)) {
	    case '.':
	    case '/':
		sb.append("_");
		break;
	    case '_':
		sb.append("_1");
		break;
	    case ';':
		sb.append("_2");
		break;
	    case '[':
		sb.append("_3");
		break;
	    default:
		if ((s.charAt(i) >= 'a' &&
		     s.charAt(i) <= 'z') ||
		    (s.charAt(i) >= 'A' &&
		     s.charAt(i) <= 'Z') ||
		    (s.charAt(i) >= '0' &&
		     s.charAt(i) <= '9')) {
		    sb.append(s.charAt(i));
		} else {
		    sb.append("_0" + toHex(s.charAt(i), 4));
		}
		break;
	    }
	}
	return sb.toString();
    }
    /** Convert the integer <code>value</code> into a hexadecimal 
     *  string with at least <code>num_digits</code> digits. */
    private static String toHex(int value, int num_digits) {
	String hexval= // javah puts all hex vals in lowercase.
	    Integer.toHexString(value).toLowerCase();
	while(hexval.length()<num_digits) hexval="0"+hexval;
	return hexval;
    }
}
