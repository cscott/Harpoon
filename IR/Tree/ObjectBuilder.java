package harpoon.IR.Tree;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.NameMap;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The <code>ObjectBuilder</code> class is a utility class designed to
 * ease the task of statically creating Java objects in the tree form. 
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: ObjectBuilder.java,v 1.1.2.4 1999-09-11 20:06:41 cananian Exp $
 *
 */
public abstract class ObjectBuilder { 
    private static final Map classes = new HashMap();
    private static final Map fields  = new HashMap();
    private static final Map methods = new HashMap(); 
    private static final Map strings = new HashMap();

    private static final boolean DEBUG = true;
    private static final void DEBUGln(String s) {
	if (DEBUG) System.out.println("ObjectBuilder: " + s);
    }
    private static final void DEBUG(String s) {
	if (DEBUG) System.out.print("ObjectBuilder: " + s);
    }


    /** 
     * Constructs an object using the specified parameters.  Returns an
     * <code>ESEQ</code>, <code>e</code>, such that <code>e.stm</code>
     * contains the tree instructions necessary to construct the object
     * in memory and <code>e.exp,</code> contains a pointer to the 
     * newly constructed object.  
     * 
     * Some classes require special handling, and cannot be created with this
     * method.  Specifically, use <code>buildClass()</code> to create 
     * <code>java.lang.Class</code> objects, use <code>buildField()</code> 
     * to create <code>java.lang.reflect.Field</code> objects, use 
     * <code>buildMethod()</code>to create 
     * <code>java.lang.reflect.Method</code> objects,and use 
     * <code>buildString</code> to create <code>java.lang.String</code> 
     * objects.  All other non-array classes can be constructed using this 
     * method.  
     * 
     * @param tf           The <code>TreeFactory</code> used to construct the 
     *                     object
     * @param frame        The <code>Frame</code> contain the machine-specific 
     *                     details of this tree form
     * @param hclass       The class of the object to build
     * @param hashcode     The hashcode of the object
     * @param fields       An array of the object's fields
     * @param fieldValues  An array of object's initial field values.  
     *                     For all <code>i</code>, <code>fieldValues[i]</code>
     *                     represents the initial value of 
     *                     <code>fields[i]</code>.  
     */                    
    public static ESEQ buildObject(TreeFactory tf, Frame frame, 
				   HClass hclass, int hashcode, 
				   HField[] fields, Exp[] fieldValues) { 
	Util.assert(!hclass.isInterface() &&
		    !hclass.isArray()     &&
		    !hclass.isPrimitive());
	Util.assert
	    (!hclass.equals(HClass.forName("java.lang.Class")) &&
	     !hclass.equals(HClass.forName("java.lang.String")) &&
	     !hclass.equals(HClass.forName("java.lang.reflect.Method")) &&
	     !hclass.equals(HClass.forName("java.lang.reflect.Field")));
	Util.assert(fields.length==fieldValues.length);

  
	OffsetMap offm    = frame.getOffsetMap();
	NameMap   nm      = frame.getRuntime().nameMap;
	int       ws      = offm.wordsize();
	Label     clsRef  = new Label();
	ArrayList d       = new ArrayList();
	ArrayList u       = new ArrayList();
	List      stms    = new ArrayList();

	// Assign the Class object a hashcode
	addS(offm.hashCodeOffset(hclass)/ws, 
	    _D(new CONST(tf, null,hashcode)),u,d);
	// Assign the Class object a class ptr
	addS(offm.clazzPtrOffset(hclass)/ws,
	    _D(new NAME(tf,null,nm.label(hclass))),u,d);
	for (int i=0; i<fields.length; i++) { 
	    Util.assert(fields[i].getDeclaringClass()==hclass);
	    addS(offm.offset(fields[i])/ws,_D(fieldValues[i]),u,d);
	}

	Collections.reverse(u);
	stms.addAll(u);
	stms.add(new LABEL(tf,null,clsRef,false));
	stms.addAll(d);

	return new ESEQ(tf,null,Stm.toStm(stms),new NAME(tf,null,clsRef));
    }

    /** 
     * Constructs an array using the specified parameters.  Returns an
     * <code>ESEQ</code>, <code>e</code>, such that <code>e.stm</code>
     * contains the tree instructions necessary to construct the array
     * in memory and <code>e.exp,</code> contains a pointer to the 
     * newly constructed object.  The length of the array will be equal
     * to <code>elements.length</code>. 
     * 
     * @param tf           The <code>TreeFactory</code> used to construct the 
     *                     object
     * @param frame        The <code>Frame</code> containing the 
     *                     machine-specific details of this tree form
     * @param hclass       The class of the array to build
     * @param hashcode     The hashcode of the array
     * @param elements     An array of <code>Exp</code> objects, corresponding
     *                     to the initial values of the constructed array's
     *                     elements. 
     */                    
    public static ESEQ buildArray(TreeFactory tf, Frame frame, HClass hclass, 
				  int hashcode, Exp[] elements) { 
	Util.assert(hclass.isArray());

	OffsetMap offm    = frame.getOffsetMap();
	NameMap   nm      = frame.getRuntime().nameMap;
	int       ws      = offm.wordsize();
	Label     clsRef  = new Label();
	ArrayList u       = new ArrayList();
	ArrayList d       = new ArrayList();
	List      stms    = new ArrayList();

	// Assign the array a length
	addS(offm.lengthOffset(hclass)/ws,
	     _D(new CONST(tf,null,elements.length)),u,d);
	// Assign the Class object a hashcode
	addS(offm.hashCodeOffset(hclass)/ws, 
	    _D(new CONST(tf, null,hclass.hashCode())),u,d);
	// Assign the Class object a class ptr
	addS(offm.clazzPtrOffset(hclass)/ws,
	    _D(new NAME(tf,null,nm.label(hclass))),u,d);
	for (int i=0; i<elements.length; i++) { 
	    addS((offm.elementsOffset(hclass)/ws)+i, _D(elements[i]),u,d);
	}

	Collections.reverse(u);
	stms.addAll(u);
	stms.add(new LABEL(tf,null,clsRef,false));
	stms.addAll(d);

	return new ESEQ(tf,null,Stm.toStm(stms),new NAME(tf,null,clsRef));
    }

    /**
     * Returns an <code>ESEQ</code> object, <code>e</code>, such that
     * <code>e.stm</code> contains the tree instructions necessary to 
     * lay out a new <code>java.lang.Class</code> object in read-only memory, 
     * and <code>e.exp</code> contains a pointer to this object.  The returned
     * <code>ESEQ</code> will contain instructions to lay out the object in
     * memory only the first time this method is called.  Subsequent calls
     * return only a pointer to this memory. 
     * 
     * The structure of a <code>Class</code> object is not specified in the
     * java language spec.  This implementation has 4 word-long fields:
     * <UL>
     * <LI> A <code>String</code> object containing the name of the class
     * <LI> a pointer to the clazz info of the class
     * <LI> an array of <code>java.lang.reflect.Method</code> objects
     *      representing the methods of this class
     * <LI> an array of <code>java.lang.reflect.Field</code> objects
     * </UL>
     *
     * @param tf       The <code>TreeFactory</code> used to construct the 
     *                 object
     * @param frame    contains architecture specific information to use
     *                 in the generation of tree code.  
     * @param hclass   the type of the <code>java.lang.Class</code> object
     *                 to be created.
     */
    public static ESEQ buildClass(TreeFactory tf, Frame frame, HClass hclass) {
	DEBUGln("buildClass() called for " + hclass);
	OffsetMap offm   = frame.getOffsetMap();
	NameMap   nm      = frame.getRuntime().nameMap;
	int       ws     = offm.wordsize();
	Label     clsRef = new Label();

	// Check the cache of class object
	if (classes.containsKey(hclass.getDescriptor()))  
	    return (ESEQ)classes.get(hclass.getDescriptor());
	else 
	    classes.put(hclass.getDescriptor(), 
			new ESEQ(tf,null,new EXP(tf,null,new CONST(tf,null)),
				 new NAME(tf,null,clsRef)));

	if (hclass.isPrimitive()) return buildPrimitiveClass(tf,frame,hclass);

	// Temporary lists of statements
	ArrayList u     = new ArrayList();
	ArrayList d     = new ArrayList();
	ArrayList s     = new ArrayList();
	// The statement list which will ultimately be converted to 
	// a Stm and returned
	List      stms  = new ArrayList();

	// Useful HClass's 
	HClass HCClass   = HClass.forName("java.lang.Class");
	HClass HCmethodA = HClass.forDescriptor("[Ljava/lang/reflect/Method;");
	HClass HCfieldA  = HClass.forDescriptor("[Ljava/lang/reflect/Field;");

	// Convert all methods to an array of java.lang.reflect.Method objects,
	// represented in tree form
	HMethod[] methods  = hclass.getDeclaredMethods();
	Exp[]     methodsInTreeForm;
	for (int i=0; i<methods.length; i++) { 
	    ESEQ methodData = buildMethod(tf,frame,methods[i]);
	    if (!Stm.isNop(methodData.stm)) stms.add(methodData.stm);
	    if (methods[i].isStatic()) {} //s.add(methodData.exp); }
	    else { 
		addE(offm.offset(methods[i])/ws,methodData.exp,u,d);
	    }
	}
	Collections.reverse(u);
	u.addAll(d);
	u.removeAll(Collections.nCopies(1, null));

	// Transfer static & non-static methods to separate arrays
   	Exp[] tNSMethods = (Exp[])u.toArray(new Exp[u.size()]);
  	Exp[] tSMethods  = (Exp[])s.toArray(new Exp[s.size()]);
	methodsInTreeForm = new Exp[tNSMethods.length+tSMethods.length];
	// Copy all methods into one array 
	System.arraycopy
	    (tNSMethods, 0, methodsInTreeForm, 0, tNSMethods.length);
	System.arraycopy
	    (tSMethods,0,methodsInTreeForm,tNSMethods.length,tSMethods.length);

	// Convert all fields to an array of java.lang.reflect.Field objects,
	// represented in tree form
	u.clear(); d.clear(); s.clear();
	HField[] fields  = hclass.getDeclaredFields();
	Exp[]    fieldsInTreeForm;
	for (int i=0; i<fields.length; i++) { 
	    ESEQ fieldData = buildField(tf,frame,fields[i]);
	    if (!Stm.isNop(fieldData.stm)) stms.add(fieldData.stm);
	    if (fields[i].isStatic()) {} //s.add(fieldData.exp); }
	    else { addE(offm.offset(fields[i])/ws,fieldData.exp,u,d); }
	}
	Collections.reverse(u);
	u.addAll(d);
	u.removeAll(Collections.nCopies(1, null));

	// Transfer non-static and static fields to separate arrays, 
   	Exp[] tNSFields = (Exp[])u.toArray(new Exp[0]);
  	Exp[] tSFields  = (Exp[])s.toArray(new Exp[0]);
	fieldsInTreeForm = new Exp[tNSFields.length+tSFields.length];
	// Copy all fields into one array 
	System.arraycopy
	    (tNSFields, 0, fieldsInTreeForm, 0, tNSFields.length);
	System.arraycopy
	    (tSFields,0,fieldsInTreeForm,tNSFields.length,tSFields.length);
	//Util.assert(fieldsInTreeForm.length==fields.length);
	u.clear(); d.clear();
	
	// Create a string object representing the name of this Class object
	ESEQ stringData  = buildString(tf, frame, hclass.getName());
	if (!Stm.isNop(stringData.stm)) stms.add(stringData.stm);

	// Create a method array representing the methods of this class object
	ESEQ methodArray = buildArray(tf,frame, HCmethodA,
				      methodsInTreeForm.hashCode(),
				      methodsInTreeForm);
	if (!Stm.isNop(methodArray.stm)) stms.add(methodArray.stm);

	// Create a field array representing the fields of this class object
	ESEQ fieldArray  = buildArray(tf,frame, HCfieldA,
				      fieldsInTreeForm.hashCode(),
				      fieldsInTreeForm);
	if (!Stm.isNop(fieldArray.stm)) stms.add(fieldArray.stm);

	
	// Assign the Class object a hashcode
	addS(offm.hashCodeOffset(HCClass)/ws, 
	    _D(new CONST(tf, null,HCClass.hashCode())),u,d);
	// Assign the Class object a class ptr
	addS(offm.clazzPtrOffset(HCClass)/ws,
	    _D(new NAME(tf,null,nm.label(HCClass))),u,d);
	// Assign the Class object a name field
	addS(offm.fieldsOffset(HCClass)/ws, _D(stringData.exp), u,d);
	// Assign the Class object a pointer to the class descriptor it 
	// of the class it represents
	addS((offm.fieldsOffset(HCClass)/ws)+1, 
	     _D(new NAME(tf,null,nm.label(hclass))),u,d);
	// Assign the Class object a pointer to an array of Method objects
	addS((offm.fieldsOffset(HCClass)/ws)+2,_D(methodArray.exp),u,d);
	// Assign the Class object a pointer to an array of Field objects
	addS((offm.fieldsOffset(HCClass)/ws)+3,_D(fieldArray.exp),u,d);
	
	Collections.reverse(u);
	stms.addAll(u);
	stms.add(new LABEL(tf,null,clsRef,false));
	stms.addAll(d);
	
	return new ESEQ(tf,null,Stm.toStm(stms),new NAME(tf,null,clsRef));
    }

    // Constructs a java.lang.Class object for primitive classes
    private static ESEQ buildPrimitiveClass(TreeFactory tf, Frame frame,
					   HClass hclass) { 
	OffsetMap offm     = frame.getOffsetMap();
	NameMap   nm      = frame.getRuntime().nameMap;
	int       ws       = offm.wordsize();
	Label     clsRef   = new Label();
	ArrayList u        = new ArrayList();
	ArrayList d        = new ArrayList();
	List      stms     = new ArrayList();

	HClass    HCClass  = HClass.forName("java.lang.Class");
	String    name     = null;

	switch (hclass.getDescriptor().charAt(0)) { 
	case 'Z': name = "boolean"; break;
	case 'B': name = "byte";    break;
	case 'S': name = "short";   break;
	case 'I': name = "int";     break;
	case 'J': name = "long";    break;
	case 'F': name = "float";   break;
	case 'D': name = "double";  break;
	case 'C': name = "char";    break;
	case 'V': name = "void";    break;
	default: 
	    throw new Error
		("Unrecognized descriptor: " + hclass.getDescriptor());
	}
	
	ESEQ stringData = buildString(tf,frame,name);
	if (!Stm.isNop(stringData.stm)) stms.add(stringData.stm);

	// Assign the Class object a hashcode
	addS(offm.hashCodeOffset(HCClass)/ws, 
	    _D(new CONST(tf, null,hclass.hashCode())),u,d);
	// Assign the Class object a class ptr
	addS(offm.clazzPtrOffset(HCClass)/ws,
	    _D(new NAME(tf,null,nm.label(HCClass))),u,d);
	// Assign the Class object a name field
	addS(offm.fieldsOffset(HCClass)/ws, _D(stringData.exp), u,d);
	// Assign the Class object a pointer to the class descriptor it 
	// of the class it represents
	addS((offm.fieldsOffset(HCClass)/ws)+1, 
	     _D(new NAME(tf,null,nm.label(hclass))),u,d);

	Collections.reverse(u);
	stms.addAll(u);
	stms.add(new LABEL(tf,null,clsRef,false));
	stms.addAll(d);
	
	return new ESEQ(tf,null,Stm.toStm(stms),new NAME(tf,null,clsRef));
    }


    /**
     * Returns an <code>ESEQ</code> object, <code>e</code>, such that
     * <code>e.stm</code> contains the tree instructions necessary to 
     * lay out a new <code>java.lang.reflect.Field</code> object in 
     * read-only memory, and <code>e.exp</code> contains a pointer to this 
     * object.  The returned <code>ESEQ</code> will contain instructions to lay
     * out the object in memory only the first time this method is called.  
     * Subsequent calls return only a pointer to this memory. 
     * 
     * @param tf       The <code>TreeFactory</code> used to construct the 
     *                 object
     * @param frame    contains architecture specific information to use
     *                 in the generation of tree code.  
     * @param field    a representation of the field to create
     */
    public static ESEQ buildField(TreeFactory tf,Frame frame,HField field) { 
	OffsetMap offm        = frame.getOffsetMap();
	NameMap   nm      = frame.getRuntime().nameMap;
	int       ws          = offm.wordsize();
	Label     clsRef = new Label();

	// Check the fields cache
	if (fields.containsKey(field)) return (ESEQ)fields.get(field);
	else 
	    fields.put(field,
			new ESEQ(tf,null,new EXP(tf,null,new CONST(tf,null)),
				 new NAME(tf,null,clsRef)));

	ArrayList u       = new ArrayList();
	ArrayList d       = new ArrayList();
	List      stms    = new ArrayList();

	HClass    HCfield = HClass.forName("java.lang.reflect.Field");

	// Compute tree representation of field name
	ESEQ stringData = buildString(tf,frame, field.getName());
	if (!Stm.isNop(stringData.stm)) stms.add(stringData.stm);

	// Compute tree representation of field type
	ESEQ typeData   = buildClass(tf,frame, field.getType());
	if (!Stm.isNop(typeData.stm)) stms.add(typeData.stm);

	HField[] HCFfields = new HField[] { 
	    HCfield.getField("name"),
	    HCfield.getField("type"),
	    HCfield.getField("clazz")
	    //HCfield.getField("modifiers")//,
	    //HCfield.getField("slot")
	};

	Exp[]    HCFfieldValues = { 
	    stringData.exp,   // The "name" field
	    typeData.exp,     // The "type" field
	    new NAME          // The "clazz" field
	    (tf,null,nm.label(HCfield))
	};

	u.clear(); d.clear();

	// Assign the Field object a hashcode
	addS(offm.hashCodeOffset(HCfield)/ws,
	    _D(new CONST(tf,null,field.hashCode())),u,d);
	// Assign the Field object a class ptr
	addS(offm.clazzPtrOffset(HCfield)/ws,
	    _D(new NAME(tf,null,nm.label(HCfield))),u,d);
	// Assign the field values of the Method object
	for (int i=0; i<HCFfields.length; i++) { 
	    addS(offm.fieldsOffset(HCfield)/ws + offm.offset(HCFfields[i])/ws,
		 _D(HCFfieldValues[i]),u,d);
	}
	Collections.reverse(u);
	stms.addAll(u);
	stms.add(new LABEL(tf,null,clsRef,false));
	stms.addAll(d);
	
	return new ESEQ(tf,null,Stm.toStm(stms),new NAME(tf,null,clsRef));
    }
    
    
    /**
     * Returns an <code>ESEQ</code> object, <code>e</code>, such that
     * <code>e.stm</code> contains the tree instructions necessary to 
     * lay out a new <code>java.lang.reflect.Method</code> object in 
     * read-only memory, and <code>e.exp</code> contains a pointer to this 
     * object.  The returned <code>ESEQ</code> will contain instructions to lay
     * out the object in memory only the first time this method is called.  
     * Subsequent calls return only a pointer to this memory. 
     * 
     * @param tf       The <code>TreeFactory</code> used to construct the 
     *                 object
     * @param frame    contains architecture specific information to use
     *                 in the generation of tree code.  
     * @param method   a representation of the method to create
     */
    public static ESEQ buildMethod(TreeFactory tf,Frame frame,HMethod method) {
	DEBUGln("buildMethod() called for " + method);

 	Exp[]    tParameterTypes, tExceptionTypes;
	HClass[] parameterTypes, exceptionTypes;

	OffsetMap offm = frame.getOffsetMap();
	NameMap   nm      = frame.getRuntime().nameMap;
	int       ws   = offm.wordsize();
	Label     clsRef = new Label();

	// Check the methods cache
	if (methods.containsKey(method)) 
	    return (ESEQ)methods.get(method);
	else 
	    methods.put(method, 
			new ESEQ(tf,null,new EXP(tf,null,new CONST(tf,null)),
				 new NAME(tf,null,clsRef)));
	
	ArrayList u    = new ArrayList();
	ArrayList d    = new ArrayList();
	List      stms = new ArrayList();

	HClass   HCmethod  = HClass.forName("java.lang.reflect.Method");
	HClass   HCclassA  = HClass.forDescriptor("[Ljava/lang/Class;");

	// Compute tree representation of method name
	ESEQ stringData = buildString(tf,frame, method.getName());
	if (!Stm.isNop(stringData.stm)) stms.add(stringData.stm);
	
	// Compute tree representation of return type
	ESEQ rtData     = buildClass(tf,frame, method.getReturnType());
	if (!Stm.isNop(rtData.stm)) stms.add(rtData.stm);

	// Compute tree representation of parameter types 
	parameterTypes  = method.getParameterTypes();
	tParameterTypes = new Exp[parameterTypes.length];
	for (int j=0; j<parameterTypes.length; j++) { 
	    ESEQ paramTypeData = buildClass(tf,frame, parameterTypes[j]);
	    if (!Stm.isNop(paramTypeData.stm)) stms.add(paramTypeData.stm);
	    tParameterTypes[j] = paramTypeData.exp;
	}
	ESEQ ptData = buildArray(tf,frame, HCclassA, 
				 parameterTypes.hashCode(),
				 tParameterTypes);
	if (!Stm.isNop(ptData.stm)) stms.add(ptData.stm);

	// Compute tree representation of exception types
	exceptionTypes  = method.getExceptionTypes();
	tExceptionTypes = new Exp[exceptionTypes.length];
	for (int j=0; j<exceptionTypes.length; j++) { 
	    ESEQ excTypeData = buildClass(tf,frame, exceptionTypes[j]);
	    if (!Stm.isNop(excTypeData.stm)) stms.add(excTypeData.stm);
	    tExceptionTypes[j] = excTypeData.exp;
	}
	ESEQ etData = buildArray(tf,frame, HCclassA,
				 exceptionTypes.hashCode(),
				 tExceptionTypes);
	if (!Stm.isNop(etData.stm)) stms.add(etData.stm);

	// Finally, combine this data into one object.
	HField[] HCMfields = new HField[] { 
	    HCmethod.getField("name"),
	    HCmethod.getField("returnType"),
	    HCmethod.getField("parameterTypes"),
	    HCmethod.getField("exceptionTypes"),
 	    HCmethod.getField("clazz")
	    //HCmethod.getField("modifiers")//,
	    //HCmethod.getField("slot")
	};
	Exp[] HCMfieldValues = new Exp[] { 
	    stringData.exp,   // the "name" field
	    rtData.exp,       // the "returnType" field
	    ptData.exp,       // the "parameterTypes" field
	    etData.exp,       // the "exceptionTypes" field
	    new NAME	      // the "clazz" field
	    (tf,null,nm.label(method.getDeclaringClass()))
	};

	u.clear(); d.clear();
	// Assign the Method object a hashcode
	addS(offm.hashCodeOffset(HCmethod)/ws, 
	    _D(new CONST(tf,null,method.hashCode())),u,d);
	// Assign the Method object a class ptr
	addS(offm.clazzPtrOffset(HCmethod)/ws,
	    _D(new NAME(tf,null,nm.label(HCmethod))),u,d);
	// Assign the field values of the Method object
	for (int i=0; i<HCMfields.length; i++) { 
	    addS(offm.offset(HCMfields[i])/ws,_D(HCMfieldValues[i]),u,d);
	}
	Collections.reverse(u);
	stms.addAll(u);
	stms.add(new LABEL(tf,null,clsRef,false));
	stms.addAll(d);

	return new ESEQ
	    (tf,null,Stm.toStm(stms),new NAME(tf,null,clsRef));
    }


    /**
     * Returns an <code>ESEQ</code> object, <code>e</code>, such that
     * <code>e.stm</code> contains the tree instructions necessary to 
     * lay out a new <code>java.lang.String</code> object in read-only memory, 
     * and <code>e.exp</code> contains a pointer to this object.  The returned
     * <code>ESEQ</code> will contain instructions to lay out the object in
     * memory only the first time this method is called.  Subsequent calls
     * return only a pointer to this memory. 
     * 
     * @param tf           The <code>TreeFactory</code> used to construct the 
     *                     object
     * @param frame    contains architecture specific information to use
     *                 in the generation of tree code.  
     * @param str      The String which we wish to lay out in memory
     * */
    public static ESEQ buildString(TreeFactory tf, Frame frame, String str) { 
	OffsetMap offm      = frame.getOffsetMap();
	NameMap   nm      = frame.getRuntime().nameMap;
	int       ws   = offm.wordsize();
	Label clsRef  = new Label();

	if (strings.containsKey(str)) return (ESEQ)strings.get(str);
	else { 
	    strings.put(str,
			new ESEQ
			(tf,null,
			 new EXP(tf,null,new CONST(tf,null)),
			 new NAME(tf,null,clsRef)));
	}

	ArrayList d    = new ArrayList();
	ArrayList u    = new ArrayList();
	List      stms = new ArrayList();

	HClass    HCstring  = HClass.forName("java.lang.String");
	HClass    HCcharA   = HClass.forDescriptor("[C");

	char[] strCA  = str.toCharArray();
	Exp[] tStrCA  = new Exp[strCA.length];
	for (int i=0; i<strCA.length; i++) 
	    tStrCA[i] = new CONST(tf,null,16,true,(int)strCA[i]);// SMALL CONST

	ESEQ charArrayData = buildArray(tf, frame, HCcharA, 
					strCA.hashCode(),tStrCA);
	if (!Stm.isNop(charArrayData.stm)) stms.add(charArrayData.stm);

	// Assign the Class object a hashcode
	addS(offm.hashCodeOffset(HCstring)/ws, 
	    _D(new CONST(tf, null,str.hashCode())),u,d);
	// Assign the Class object a class ptr
	addS(offm.clazzPtrOffset(HCstring)/ws,
	    _D(new NAME(tf,null,nm.label(HCstring))),u,d);
	addS(offm.offset(HCstring.getField("count"))/ws,
	    _D(new CONST(tf,null,str.length())),u,d);
	addS(offm.offset(HCstring.getField("offset"))/ws,
	    _D(new CONST(tf,null,0)),u,d);
	addS(offm.offset(HCstring.getField("value"))/ws,
	    _D(charArrayData.exp),u,d);

	Collections.reverse(u);
	stms.addAll(u);
	stms.add(new LABEL(tf,null,clsRef,false));
	stms.addAll(d);
	      
	return new ESEQ(tf,null,Stm.toStm(stms),new NAME(tf,null,clsRef));
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    //                                                           //
    //                     Utility methods                       //
    //                                                           //
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

    private static DATA _D(Exp e) { 
	return new DATA(e.getFactory(),null,e);
    }

    private static void addS(int index, Tree elem, 
			     ArrayList up, ArrayList down) { 
	int size, requiredSize;
	if (index<0) { 
	    requiredSize = (-index);
	    if (requiredSize > up.size()) { 
		up.ensureCapacity(requiredSize);
		for (int i=up.size(); i<requiredSize; i++) 
		    up.add(_D(new CONST(elem.getFactory(),null)));
	    }	    
	    up.set(-index-1, elem);
	}
	else {
	    requiredSize = index+1;
	    if (requiredSize > down.size()) { 
		down.ensureCapacity(requiredSize);
		for (int i=down.size(); i<requiredSize; i++) 
		    down.add(_D(new CONST(elem.getFactory(),null)));
	    }	    
	    down.set(index, elem);
	}
    }


    private static void addE(int index, Tree elem, 
			     ArrayList up, ArrayList down) { 
	int size, requiredSize;
	if (index<0) { 
	    requiredSize = (-index);
	    if (requiredSize > up.size()) { 
		up.ensureCapacity(requiredSize);
		for (int i=up.size(); i<requiredSize; i++) 
		    up.add(new CONST(elem.getFactory(),null));
	    }	    
	    up.set(-index-1, elem);
	}
	else {
	    requiredSize = index+1;
	    if (requiredSize > down.size()) { 
		down.ensureCapacity(requiredSize);
		for (int i=down.size(); i<requiredSize; i++) 
		    down.add(new CONST(elem.getFactory(),null));
	    }	    
	    down.set(index, elem);
	}
    }
}

