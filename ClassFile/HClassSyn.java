// HClassSyn.java, created Wed Oct 14 16:03:26 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.lang.reflect.Modifier;

import harpoon.Util.Util;

/**
 * Instances of the class <code>HClassSyn</code> represent modifiable
 * classes and interfaces of a java program.  Arrays and primitive types
 * are not modifiable, and thus are not represented by 
 * <code>HClassSyn</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HClassSyn.java,v 1.8.2.1 2002-02-27 08:35:42 cananian Exp $
 * @see harpoon.ClassFile.HClass
 */
class HClassSyn extends HClassCls implements HClassMutator {

  /** Create an <code>HClassSyn</code> from a template <code>HClass</code>.
   *  The new <code>HClassSyn</code> will have the (fully-qualified)
   *  name specified by the <code>name</code> parameter.
   * @param template The template class to use.
   * @param name The fully-qualified name for this class object.
   */
  HClassSyn(Linker l, String name, HClass template) {
    super(l);
    assert !template.isArray();
    assert !template.isPrimitive();
    assert l==template.getLinker();
    this.name = name;
    this.superclass = template.getSuperclass();
    this.interfaces = template.getInterfaces();
    this.modifiers  = template.getModifiers();
    this.sourcefile = template.getSourceFile();

    this.declaredFields = new HFieldSyn[0];
    HField fields[] = template.getDeclaredFields();
    for (int i=0; i < fields.length; i++)
      addDeclaredField(fields[i].getName(), fields[i]);
    assert fields.length == declaredFields.length;

    this.declaredMethods= new HMethodSyn[0];
    HMethod methods[] = template.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++)
      if (methods[i] instanceof HInitializer)
	addClassInitializer();
      else if (methods[i] instanceof HConstructor)
	addConstructor((HConstructor)methods[i]);
      else
	addDeclaredMethod(methods[i].getName(), methods[i]);
    assert methods.length == declaredMethods.length;

    // ensure linker information is consistent.
    assert this.superclass==null || checkLinker((HClass)this.superclass);
    for (int i=0; i<this.interfaces.length; i++)
      assert checkLinker((HClass)this.interfaces[i]);

    hasBeenModified = true; // by default, mark this as 'modified'
  }
  /** private constructor for serialization. */
  private HClassSyn(Linker l, String name,
		    HClass superclass, HClass[] interfaces,
		    int modifiers, String sourcefile) {
    super(l);
    this.name = name;
    this.superclass = superclass;
    this.interfaces = interfaces;
    this.modifiers  = modifiers;
    this.sourcefile = sourcefile;
    this.declaredFields = null; // fill in during second pass
    this.declaredMethods= null; // fill in during second pass
    this.hasBeenModified = false; // fill in during second pass
  }
  public HClassMutator getMutator() { return this; }

  // implementation of HClassMutator.
  public HField addDeclaredField(String name, HClass type) {
    return addDeclaredField0(new HFieldSyn(this, name, type));
  }
  public HField addDeclaredField(String name, String descriptor) {
    return addDeclaredField0(new HFieldSyn(this, name, descriptor));
  }
  public HField addDeclaredField(String name, HField template) {
    return addDeclaredField0(new HFieldSyn(this, name, template));
  }
  // deal with array housekeeping.
  private HField addDeclaredField0(HField f) {
    assert f.getDeclaringClass()==this;
    for (int i=0; i<declaredFields.length; i++)
      if (declaredFields[i].equals(f))
	throw new DuplicateMemberException("Field "+f+" in "+this);
    declaredFields = 
      (HField[]) Util.grow(HField.arrayFactory,
			   declaredFields, f, declaredFields.length);
    fields=null; // invalidate cache.
    hasBeenModified=true; // flag the modification
    return f;
  }
  public void removeDeclaredField(HField f) throws NoSuchFieldError {
    for (int i=0; i<declaredFields.length; i++) {
      if (declaredFields[i].equals(f)) {
	declaredFields = (HField[]) Util.shrink(HField.arrayFactory,
						declaredFields, i);
	fields=null; // invalidate cache.
	hasBeenModified=true; // flag the modification
	return;
      }
    }
    throw new NoSuchMemberException("Field "+f);
  }

  public HInitializer addClassInitializer() {
    return (HInitializer)
      addDeclaredMethod0(new HInitializerSyn(this));
  }
  public void removeClassInitializer(HInitializer m) {
    removeDeclaredMethod(m);
  }
  public HConstructor addConstructor(String descriptor) {
    return (HConstructor)
      addDeclaredMethod0(new HConstructorSyn(this, descriptor));
  }
  public HConstructor addConstructor(HClass[] paramTypes) {
    for (int i=0; i<paramTypes.length; i++)
      assert checkLinker(paramTypes[i]);
    return (HConstructor)
      addDeclaredMethod0(new HConstructorSyn(this, paramTypes));
  }
  public HConstructor addConstructor(HConstructor template) {
    return (HConstructor)
      addDeclaredMethod0(new HConstructorSyn(this, template));
  }
  public void removeConstructor(HConstructor c) {
    removeDeclaredMethod(c);
  }
  public HMethod addDeclaredMethod(String name, String descriptor) {
    assert !name.equals("<init>") && !name.equals("<clinit>");
    return addDeclaredMethod0(new HMethodSyn(this, name, descriptor));
  }
  public HMethod addDeclaredMethod(String name, HClass[] paramTypes,
				   HClass returnType) {
    assert !name.equals("<init>") && !name.equals("<clinit>");
    assert checkLinker(returnType);
    for (int i=0; i<paramTypes.length; i++)
      assert checkLinker(paramTypes[i]);
    return addDeclaredMethod0(new HMethodSyn(this,name,paramTypes,returnType));
  }
  public HMethod addDeclaredMethod(String name, HMethod template) {
    assert !name.equals("<init>") && !name.equals("<clinit>");
    return addDeclaredMethod0(new HMethodSyn(this, name, template));
  }

  private HMethod addDeclaredMethod0(HMethod m) {
    assert m.getDeclaringClass()==this;
    for (int i=0; i<declaredMethods.length; i++)
      if (declaredMethods[i].equals(m))
	throw new DuplicateMemberException("Method "+m+" in "+this);
    declaredMethods = 
      (HMethod[]) Util.grow(HMethod.arrayFactory,
			    declaredMethods, m, declaredMethods.length);
    methods=null; // invalidate cache.
    constructors=null;
    hasBeenModified=true; // flag the modification
    return m;
  }
  public void removeDeclaredMethod(HMethod m) throws NoSuchMethodError {
    for (int i=0; i<declaredMethods.length; i++) {
      if (declaredMethods[i].equals(m)) {
	declaredMethods = (HMethod[]) Util.shrink(HMethod.arrayFactory,
						  declaredMethods, i);
	methods=null; // invalidate cache.
	constructors=null;
	hasBeenModified=true; // flag the modification
	return;
      }
    }
    throw new NoSuchMemberException("Method "+m);
  }

  public void addModifiers(int m) { setModifiers(getModifiers()|m); }
  public void removeModifiers(int m){ setModifiers(getModifiers()&(~m)); }
  public void setModifiers(int m) { 
    // are we changing an interface to a class?
    if ( Modifier.isInterface(modifiers) && !Modifier.isInterface(m)) {
      // make sure there are no superclasses or superinterfaces.
      assert superclass==null; // should be true for interfaces.
      if (interfaces.length!=0)
	throw new Error("Can't change a subinterface to a class. "+
			"Remove the inheritance first. ("+this+")");
      // inherit from java.lang.Object.
      superclass = getLinker().forName("java.lang.Object");
      // tag all the methods as abstract.
      for (int i=0; i<declaredMethods.length; i++)
	((HMethodSyn)declaredMethods[i]).setModifiers
	  (declaredMethods[i].getModifiers() | Modifier.ABSTRACT);
    }
    // are we changing a class to an interface?
    if (!Modifier.isInterface(modifiers) &&  Modifier.isInterface(m)) {
      // make sure there are no superclasses or superinterfaces.
      if (superclass != null && 
	  superclass.actual() != getLinker().forName("java.lang.Object"))
	throw new Error("Can't change a subclass to an interface. "+
			"Remove the inheritance first. ("+this+")");
      if (interfaces.length!=0)
	throw new Error("Can't change a class implementing interfaces "+
			"to an interface itself. Remove the inheritance "+
			"first. ("+this+")");
      // interfaces have no superclass.
      superclass = null;
      // tag all the methods as abstract
      for (int i=0; i<declaredMethods.length; i++) {
	HMethodSyn hm = (HMethodSyn) declaredMethods[i];
	hm.setModifiers(hm.getModifiers() | Modifier.ABSTRACT);
      }
    }
    if (modifiers!=m) hasBeenModified=true; // flag the modification
    modifiers = m;
  }

  public void setSuperclass(HClass sc) {
    if (sc==null)
      throw new Error("This is very odd.  " +
		      "Do you realize you're trying to create a new "+
		      "top-level object?  I'm not sure I should allow this."+
		      "  Please mail me at cananian@alumni.princeton.edu and "+
		      "tell me why you think it's a good idea.");
    assert !sc.isPrimitive();
    assert checkLinker(sc);
    // XXX more sanity checks?
    if (superclass != sc) hasBeenModified=true; // flag the modification
    superclass = sc;
    constructors=null;
    fields = null;
    methods = null;
  }

  public void addInterface(HClass in) {
    if (!in.isInterface()) throw new Error("Not an interface.");
    assert checkLinker(in);
    interfaces = (HClass[]) Util.grow(HClass.arrayFactory,
				      interfaces, in, interfaces.length);
    hasBeenModified = true;
  }
  public void removeInterface(HClass in) throws NoSuchClassException {
    assert checkLinker(in);
    for (int i=0; i<interfaces.length; i++) {
      if (interfaces[i].equals(in)) {
	interfaces = (HClass[]) Util.shrink(HClass.arrayFactory,
					    interfaces, i);
	hasBeenModified = true;
	return;
      }
    }
    throw new NoSuchClassException(in.toString());
  }
  public void removeAllInterfaces() {
    if (interfaces.length!=0) hasBeenModified = true;
    // wheee.
    interfaces = new HClass[0];
  }

  /**
   * Set the source file name for this class.
   */
  public void setSourceFile(String sf) {
    if (!this.sourcefile.equals(sf)) hasBeenModified = true;
    this.sourcefile = sf;
  }

  //----------------------------------------------------------
  // assertion helper.
  private boolean checkLinker(HClass hc) {
    return hc.isPrimitive() || hc.getLinker()==getLinker();
  }
  //----------------------------------------------------------

  /** Serializable interface. */
  public Object writeReplace() {
    return new Stub(this);
  }
  private static final class Stub implements java.io.Serializable {
    private final Linker linker;
    private final String name;
    private final HClass superclass;
    private final HClass[] interfaces;
    private final int modifiers;
    private final String sourcefile;
    private final FieldStub[] declaredFields;
    private final MethodStub[] declaredMethods;
    private final boolean hasBeenModified;
    Stub(HClassSyn c) { // store salient information
      // using the method versions below allows up to resolve
      // class name pointers.  note that we intern strings below.
      this.linker = c.getLinker();
      this.name = c.getName().intern();
      this.superclass = c.getSuperclass();
      this.interfaces = c.getInterfaces();
      this.modifiers  = c.getModifiers();
      this.sourcefile = c.getSourceFile().intern();
      this.hasBeenModified = c.hasBeenModified();
      // stub out fields and methods.
      HField[] declf = c.getDeclaredFields();
      this.declaredFields = new FieldStub[declf.length];
      for (int i=0; i<declf.length; i++)
	this.declaredFields[i] = new FieldStub(declf[i]);
      HMethod[] declm = c.getDeclaredMethods();
      this.declaredMethods= new MethodStub[declm.length];
      for (int i=0; i<declm.length; i++)
	this.declaredMethods[i] = new MethodStub(declm[i]);
    }
    public Object readResolve() throws java.io.ObjectStreamException {
      HClassSyn c = new HClassSyn(linker, name, superclass, interfaces,
				  modifiers, sourcefile);
      c.declaredFields = new HField[declaredFields.length];
      for (int i=0; i<declaredFields.length; i++)
	c.declaredFields[i] = declaredFields[i].reconstruct(c);
      c.declaredMethods = new HMethod[declaredMethods.length];
      for (int i=0; i<declaredMethods.length; i++)
	c.declaredMethods[i] = declaredMethods[i].reconstruct(c);
      // last because reconstruction twiddles the field.
      c.hasBeenModified = this.hasBeenModified;
      return c;
    }
  }
  private static class MemberStub implements java.io.Serializable {
    final String name;
    final int modifiers;
    final boolean isSynthetic;
    MemberStub(HMember hm) {
      this.name = hm.getName().intern();
      this.modifiers = hm.getModifiers();
      this.isSynthetic = hm.isSynthetic();
    }
  }
  private static class FieldStub extends MemberStub {
    final HPointer type;
    final Object constValue;
    FieldStub(HField hf) {
      super(hf);
      this.type = new ClassPointer(hf.getType());
      this.constValue = hf.getConstant();
    }
    HFieldSyn reconstruct(HClassSyn parent) {
      HFieldSyn hf = new HFieldSyn(parent, FieldStub.this.name, type);
      HFieldMutator hfm = hf.getMutator();
      hfm.setModifiers(FieldStub.this.modifiers);
      hfm.setConstant(constValue);
      hfm.setSynthetic(isSynthetic);
      return hf;
    }
  }
  static class MethodStub extends MemberStub {
    final String descriptor;
    final String[] parameterNames;
    final HPointer[] exceptionTypes;
    final boolean isConstructor;
    final boolean isInitializer;
    MethodStub(HMethod hm) {
      super(hm);
      this.isConstructor = hm instanceof HConstructor;
      this.isInitializer = hm instanceof HInitializer;
      this.descriptor = hm.getDescriptor().intern();
      String[] pn = hm.getParameterNames();
      this.parameterNames = new String[pn.length];
      for (int i=0; i<pn.length; i++)
	this.parameterNames[i] = (pn[i]==null) ? null : pn[i].intern();
      HClass[] et = hm.getExceptionTypes();
      this.exceptionTypes = new HPointer[et.length];
      for (int i=0; i<et.length; i++)
	this.exceptionTypes[i] = new ClassPointer(et[i]);
    }
    HMethodSyn reconstruct(HClassSyn parent) {
      return reconstruct
	(isInitializer ? new HInitializerSyn(parent):
	 isConstructor ? new HConstructorSyn(parent,descriptor):
	 new HMethodSyn(parent, MethodStub.this.name, descriptor));
    }
    HMethodSyn reconstruct(HClassArraySyn parent) {
      return reconstruct(new HMethodSyn(parent, MethodStub.this.name, descriptor));
    }
    private HMethodSyn reconstruct(HMethodSyn hm) {
      HMethodMutator hmm = hm.getMutator();
      hmm.setModifiers(MethodStub.this.modifiers);
      hmm.setSynthetic(isSynthetic);
      hmm.setParameterNames(parameterNames);
      // back door so that HPointer isn't resolved yet.
      hm.exceptionTypes = this.exceptionTypes;
      return hm;
    }
  }
}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
