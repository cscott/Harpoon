// HClass.java, created Fri Jul 31  4:33:28 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.ArrayFactory;
import net.cscott.jutil.ReferenceUnique;
import net.cscott.jutil.UniqueVector;
import harpoon.Util.Util;
import harpoon.Util.HClassUtil;
import harpoon.Util.ArraySet;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jpaul.DataStructs.MapFacts;
import jpaul.DataStructs.MapWithDefault;
import jpaul.DataStructs.NoCompTreeMap;

/**
 * Instances of the class <code>HClass</code> represent classes and 
 * interfaces of a java program.  Every array also belongs to a
 * class that is reflected as a <code>HClass</code> object that is
 * shared by all arrays with the same element type and number of
 * dimensions.  Finally, the primitive Java types
 * (<code>boolean</code>, <code>byte</code>, <code>char</code>,
 * <code>short</code>, <code>int</code>, <code>long</code>,
 * <code>float</code>, and <code>double</code>) and the keyword
 * <code>void</code> are also represented as <code>HClass</code> objects.
 * <p>
 * There is no public constructor for the class <code>HClass</code>.
 * <code>HClass</code> objects are created with the <code>forName</code>,
 * <code>forDescriptor</code> and <code>forClass</code> methods of
 * <code>Linker</code>.
 *
 * <p><b>A note on mutability:</b> <code>HClass</code> objects may be
 * mutable.  Of course, this is unpleasant for any attempt to cache
 * the result of the expensive computations on such objects (e.g.,
 * {@link #getMethod(String,String)}).  If you are VERY familiar with
 * Flex, you can use caching in the periods when <code>HClass</code>es
 * are guaranteed not to be mutated.  You just have to enter such a
 * period (we call it <i>immutability epoch</i>) explicitly using
 * {@link HClass.enterImmutableEpoch()} and end it using {@link
 * HClass.exitImmutableEpoch()}.  E.g.,

<p>
<pre>
 HClass.enterImmutableEpoch();

  Big computation that does not mutate HClass'es; e.g., a computation
  that calls HClass.getMethod(String, String) a lot: construction of a
  CallGraphImpl + all possible queries on it.

 HClass.exitImmutableEpoch();
</pre>

 * <p>Knowing that <code>HClass</code>es
 * are not mutated in a certain code sequence is actually tricky,
 * because most of the {@link harpoon.ClassFile.HCodeFactory
 * HCodeFactories} are lazy; a good method is to use a {@link
 * harpoon.ClassFile.CachingCodeFactory CachingCodeFactory} that has
 * already computed the code of every method of interest.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HClass.java,v 1.52 2005-10-25 14:33:21 salcianu Exp $
 * @see harpoon.IR.RawClass.ClassFile
 * @see java.lang.Class */
public abstract class HClass extends HPointer
  implements java.lang.Comparable<HClass>, ReferenceUnique, HType {
  /** The linker responsible for the resolution of this <code>HClass</code>
   *  object. */
  private final Linker _linker;
  boolean hasBeenModified = false;

  /** Protected constructor, not for external use. */
  HClass(Linker l) { _linker = l; }

  /**
   * Returns the linker responsible for the resolution of this
   * <code>HClass</code> object.
   */
  public final Linker getLinker() { return _linker; }

  /**
   * Returns a mutator for this <code>HClass</code>, or <code>null</code>
   * if this object is immutable.
   */
  public HClassMutator getMutator() { return null; }

  /**
   * Determines whether any part of this <code>HClass</code> has been
   * modified from its originally loaded state.
   */
  public boolean hasBeenModified() { return hasBeenModified; }

  /**
   * If this class represents an array type, returns the <code>HClass</code>
   * object representing the component type of the array; otherwise returns
   * null.
   * @see java.lang.reflect.Array
   */
  public abstract HClass getComponentType();

  /** 
   * Returns the fully-qualified name of the type (class, interface,
   * array, or primitive) represented by this <code>HClass</code> object,
   * as a <code>String</code>. 
   * @return the fully qualified name of the class or interface
   *         represented by this object.
   */
  public abstract String getName();

  /**
   * Returns the package name of this <code>HClass</code>.  If this
   * <code>HClass</code> represents a primitive or array type,
   * then returns null.  Returns <code>""</code> (a zero-length string)
   * if this class is not in a package.
   */
  public abstract String getPackage();

  /**
   * Returns a ComponentType descriptor for the type represented by this
   * <code>HClass</code> object.
   */
  public abstract String getDescriptor();

  /**
   * Returns a <code>HField</code> object that reflects the specified
   * declared field of the class or interface represented by this
   * <code>HClass</code> object.  The <code>name</code> parameter is a 
   * <code>String</code> that specifies the simple name of the
   * desired field.
   * @exception NoSuchFieldError
   *            if a field with the specified name is not found.
   * @see HField
   */
  public abstract HField getDeclaredField(String name)
    throws NoSuchFieldError;

  /**
   * Returns an array of <code>HField</code> objects reflecting all the
   * fields declared by the class or interface represented by this
   * <code>HClass</code> object.  This includes <code>public</code>,
   * <code>protected</code>, default (<code>package</code>) access,
   * and <code>private</code> fields, but excludes inherited fields.
   * Returns an array of length 0 if the class or interface declares
   * no fields, or if this <code>HClass</code> object represents a
   * primitive type.
   * @see "The Java Language Specification, sections 8.2 and 8.3"
   * @see HField
   */
  public abstract HField[] getDeclaredFields();

  /**
   * Returns a <code>HField</code> object that reflects the specified
   * accessible member field of the class or interface represented by this
   * <code>HClass</code> object.  The <code>name</code> parameter is
   * a <code>String</code> specifying the simple name of the
   * desired field. <p>
   * The field to be reflected is located by searching all member fields
   * of the class or interface represented by this <code>HClass</code>
   * object (and its superclasses and interfaces) for an accessible 
   * field with the specified name.
   * @see "The Java Language Specification, sections 8.2 and 8.3"
   * @exception NoSuchFieldError
   *            if a field with the specified name is not found.
   * @see HField
   */
  public final HField getField(String name) throws NoSuchFieldError {
    // construct master field list, if we haven't already.
    HField[] fields=getFields();
    // look for field name in master field list.
    // look backwards to be sure we find local fields first (scoping)
    for (int i=fields.length-1; i>=0; i--)
      if (fields[i].getName().equals(name))
	return fields[i];
    // can't find it.
    throw new NoSuchFieldError(getName()+"."+name);
  }

  /**
   * Returns an array containing <code>HField</code> objects reflecting
   * all the accessible fields of the class or interface represented by this
   * <code>HClass</code> object.  Returns an array of length 0 if the
   * class or interface has no accessible fields, or if it represents an 
   * array type or a primitive type. <p>
   * Specifically, if this <code>HClass</code> object represents a class,
   * returns the accessible fields of this class and of all its superclasses.
   * If this <code>HClass</code> object represents an interface, returns 
   * the accessible fields
   * of this interface and of all its superinterfaces.  If this 
   * <code>HClass</code> object represents an array type or a primitive
   * type, returns an array of length 0. <p>
   * The implicit length field for array types is not reflected by this
   * method.
   * @see "The Java Language Specification, sections 8.2 and 8.3"
   * @see HField
   */
  public HField[] getFields() { 
    if (isPrimitive() || isArray())
      return new HField[0];
    // do the actual work.
    UniqueVector<HField> v = new UniqueVector<HField>();
    // add fields from interfaces.
    HClass[] in = getInterfaces();
    for (int i=0; i<in.length; i++) {
      HField[] inf = in[i].getFields();
      for (int j=0; j<inf.length; j++)
	v.add(inf[j]);
    }
    // now fields from superclasses, subject to access mode constraints.
    HClass sup = getSuperclass();
    HField supf[] = (sup==null)?new HField[0]:sup.getFields();
    for (int i=0; i<supf.length; i++) {
      int m = supf[i].getModifiers();
      // private fields of superclasses are invisible.
      if (Modifier.isPrivate(m))
	continue; // skip this field.
      // default access is invisible if packages not identical.
      /** DISABLED: see notes in getMethods() [CSA 6-22-99] */
      /*
      if (!Modifier.isPublic(m) && !Modifier.isProtected(m))
	if (!supf[i].getDeclaringClass().getPackage().equals(frmPackage))
	  continue;
      */
      // all's good. Add this one.
      v.add(supf[i]);
    }
    // now fields from our local class.
    HField locf[] = getDeclaredFields();
    for (int i=0; i<locf.length; i++)
      v.add(locf[i]);
    
    // Merge into one array.
    return v.toArray(new HField[v.size()]);
  }

  /**
   * Returns a <code>HMethod</code> object that reflects the specified 
   * declared method of the class or interface represented by this 
   * <code>HClass</code> object.  The <code>name</code> parameter is a
   * <code>String</code> that specifies the simple name of the desired
   * method, and the <code>parameterTypes</code> parameter is an array
   * of <code>HClass</code> objects that identify the method's formal
   * parameter types, in declared order.
   * @exception NoSuchMethodError
   *            if a matching method is not found.
   * @see HMethod
   */
  public abstract HMethod getDeclaredMethod(String name,
					    HClass parameterTypes[])
    throws NoSuchMethodError;

  /**
   * Returns a <code>HMethod</code> object that reflects the specified 
   * declared method of the class or interface represented by this 
   * <code>HClass</code> object.  The <code>name</code> parameter is a
   * <code>String</code> that specifies the simple name of the desired
   * method, and <code>descriptor</code> is a string describing
   * the parameter types and return value of the method.
   * @exception NoSuchMethodError
   *            if a matching method is not found.
   * @see HMethod#getDescriptor
   */
  public abstract HMethod getDeclaredMethod(String name, String descriptor)
    throws NoSuchMethodError;

  /**
   * Returns an array of <code>HMethod</code> objects reflecting all the
   * methods declared by the class or interface represented by this
   * <code>HClass</code> object.  This includes <code>public</code>,
   * <code>protected</code>, default (<code>package</code>) access, and
   * <code>private</code> methods, but excludes inherited methods.
   * Returns an array of length 0 if the class or interface declares no
   * methods, or if this <code>HClass</code> object represents a primitive
   * type.<p>
   * Constructors are included.
   * @see "The Java Language Specification, section 8.2"
   * @see HMethod
   */
  public abstract HMethod[] getDeclaredMethods();

  /**
   * Returns an <code>HMethod</code> object that reflects the specified
   * accessible method of the class or interface represented by this
   * <code>HClass</code> object.  The <code>name</code> parameter is
   * a string specifying the simple name of the desired method, and
   * the <code>parameterTypes</code> parameter is an array of
   * <code>HClass</code> objects that identify the method's formal
   * parameter types, in declared order. <p>
   * The method to reflect is located by searching all the member methods
   * of the class or interface represented by this <code>HClass</code>
   * object for an accessible method with the specified name and exactly
   * the same formal parameter types.
   * @see "The Java Language Specification, sections 8.2 and 8.4"
   * @exception NoSuchMethodError if a matching method is not found.
   */
  public final HMethod getMethod(String name, HClass parameterTypes[])
    throws NoSuchMethodError {
    // construct master method list, if we haven't already.
    HMethod[] methods=getMethods();
    // look for method name in master method list.
    // look backwards to be sure we find local methods first (scoping).
    for (int i=methods.length-1; i>=0; i--)
      if (methods[i].getName().equals(name)) {
	HClass[] methodParamTypes = methods[i].getParameterTypes();
	if (methodParamTypes.length == parameterTypes.length) {
	  int j; for (j=0; j<parameterTypes.length; j++)
	    if (methodParamTypes[j] != parameterTypes[j])
	      break; // oops, this one doesn't match.
	  if (j==parameterTypes.length) // hey, we made it to the end!
	    return methods[i];
	}
      }
    // didn't find a match. Oh, well.
    throw new NoSuchMethodError(getName()+"."+name);
  }

  /**
   * Returns an <code>HMethod</code> object that reflects the specified
   * accessible method of the class or interface represented by this
   * <code>HClass</code> object.  The <code>name</code> parameter is
   * a string specifying the simple name of the desired method, and
   * the <code>descriptor</code> is a string describing the
   * parameter types and return value of the method. <p>
   * The method is located by searching all the member methods of
   * the class or interface represented by this <code>HClass</code>
   * object for an accessible method with the specified name and
   * exactly the same descriptor.
   * @see HMethod#getDescriptor
   * @exception NoSuchMethodError if a matching method is not found.
   */
  public final HMethod getMethod(String name, String descriptor)
    throws NoSuchMethodError {
    if(immutableEpoch) {
      // cache too old - throw it away
      if(cacheEpoch != currentEpoch) {
	refreshCache();
      }
      Map<String,HMethod> desc2hm = cacheGetMethod.get(name);
      HMethod hm = desc2hm.get(descriptor);
      if(hm == null) {
	hm = _getMethod(name, descriptor);
	desc2hm.put(descriptor, hm);
      }
      return hm;
    }
    return _getMethod(name, descriptor);
  }

  /*
    VERY IMPORTANT for adding caching behavior to HClass methods

    (1) examine getMethod(String,String) to see how a method should
    use the caching mechanism correctly

    (2) the cache for each method should be initialized in the
    refreshCache() method.
  */

  /* True if Flex is currently in an immutable epoch. */
  private static boolean immutableEpoch = false;
  /* Number id of the current immutable epoch (if any). */
  private static int currentEpoch = 0;
  /* Number id of the immutable epoch the cache(s) was/were computed.
     This allows us to avoid reusing old (incorrect) caches from a
     previous immutability epoch.  */
  private int cacheEpoch = -1;


  /** Notifies the <code>HClass</code> implementation that Flex
      enters an immutability epoch, i.e., a period of time when no
      HClass will be mutated.  Inside an immutability epoch, expensive
      <code>HClass</code> computations may perform caching.  These
      caches are invalidated at the end of the current immutability
      epoch, so there is danger of reusing caches from an old
      epoch. */
  public static final void enterImmutableEpoch() {
    currentEpoch++;
    immutableEpoch = true;
  }

  /** Notifies the <code>HClass</code> implementation that Flex exits
      an immutability epoch (i.e., in the future, Flex may mutate one
      or more <code>HClass</code>. */
  public static final void exitImmutableEpoch() {
    immutableEpoch = false;
  }

  /* The cache for getMethod(String,String). */
  private Map<String,Map<String,HMethod>> cacheGetMethod;

  /* Creates / refresh the caches: called the first time a cacheable
     method is invoked in an immutability epoch.  This way, we make
     sure we do not reuse the old caches (if any).  */
  private void refreshCache() {
    cacheGetMethod = 
      new MapWithDefault<String, Map<String,HMethod>>
      (new NoCompTreeMap<String,Map<String,HMethod>>(),
       MapFacts.<String,HMethod>noCompTree(),
       true);
    cacheEpoch = currentEpoch;
  }


  private final HMethod _getMethod(String name, String descriptor)
    throws NoSuchMethodError {
    // construct master method list, if we haven't already.
    HMethod[] methods=getMethods();
    // look for method name in master method list.
    // look backwards to be sure we find local methods first (scoping)
    for (int i=methods.length-1; i>=0; i--)
      if (methods[i].getName().equals(name) &&
	  methods[i].getDescriptor().equals(descriptor))
	return methods[i];
    // didn't find a match.
    throw new NoSuchMethodError(getName()+"."+name+"/"+descriptor);
  }
	
  /**
   * Returns an array containing <code>HMethod</code> object reflecting
   * all accessible member methods of the class or interface represented
   * by this <code>HClass</code> object, including those declared by
   * the class or interface and those inherited from superclasses and
   * superinterfaces.  Returns an array of length 0 if the class or
   * interface has no public member methods, or if the <code>HClass</code>
   * corresponds to a primitive type or array type.<p>
   * Constructors are included.
   * @see "The Java Language Specification, sections 8.2 and 8.4"
   */
  public HMethod[] getMethods() {
    if (isPrimitive())
      return new HMethod[0];
    // do the actual work.
    Map<String,HMethod> h = new HashMap<String,HMethod>(); // keep track of overriding
    List<HMethod> v = new ArrayList<HMethod>();

    // first methods we declare locally.
    HMethod[] locm = getDeclaredMethods();
    for (int i=0; i<locm.length; i++) {
      h.put(locm[i].getName()+locm[i].getDescriptor(), locm[i]);
      v.add(locm[i]);
    }
    locm=null; // free memory

    // grab fields from superclasses, subject to access mode constraints.
    // note interfaces have the methods of java.lang.Object, too.
    HClass sup = getSuperclass();
    if (isInterface()) sup=_linker.forName("java.lang.Object");//this not prim.
    HMethod supm[] = (sup==null)?new HMethod[0]:sup.getMethods();
    for (int i=0; i<supm.length; i++) {
      int m = supm[i].getModifiers();
      // private methods of superclasses are invisible.
      if (Modifier.isPrivate(m))
	continue; // skip this method.
      // default access is invisible if packages not identical
      /** SKIPPING this test, because the interpreter doesn't like it.
       **  For example, harpoon.IR.Quads.OPER invokes
       **  OperVisitor.dispatch() in method visit().  But dispatch() has
       **  package visibility and thus doesn't show up in
       **  SCCAnalysis...operVisitor, and a virtual dispatch to visit()
       **  on an object of type SCCAnalysis...operVisitor fails.  Current
       **  solution is to move this check into the interpreter; see
       **  harpoon.Interpret.Quads.Method. [CSA, 6-22-99] */
      /*
      if (!Modifier.isPublic(m) && !Modifier.isProtected(m))
	if (!supm[i].getDeclaringClass().getPackage().equals(frmPackage))
	  continue; // skip this (inaccessible) method.
      */
      // skip superclass constructors.
      if (supm[i] instanceof HConstructor)
	  continue;
      // don't add methods which are overriden by locally declared methods.
      if (h.containsKey(supm[i].getName()+supm[i].getDescriptor()))
	continue;
      // all's good.  Add this one.
      h.put(supm[i].getName()+supm[i].getDescriptor(), supm[i]);
      v.add(supm[i]);
    }
    sup=null; supm=null; // free memory.

    // Lastly, interface methods, if not already declared.
    // [interface methods will typically be explicitly declared in classes,
    //  even if not implemented (abstract), but superinterface methods aren't
    //  declared explicitly in interfaces.]
    HClass[] intc = getInterfaces();
    for (int i=0; i<intc.length; i++) {
      HMethod intm[] = intc[i].getMethods();
      for (int j=0; j<intm.length; j++) {
	// don't add methods which are overridden by locally declared methods
	if (h.containsKey(intm[j].getName()+intm[j].getDescriptor()))
	  continue;
	v.add(intm[j]);
      }
    }
    intc = null; // free memory.

    // Merge into a single array.
    return v.toArray(new HMethod[v.size()]);
  }

  /**
   * Returns an <code>HConstructor</code> object that reflects the 
   * specified declared constructor of the class or interface represented 
   * by this <code>HClass</code> object.  The <code>parameterTypes</code>
   * parameter is an array of <code>HClass</code> objects that
   * identify the constructor's formal parameter types, in declared order.
   * @exception NoSuchMethodError if a matching method is not found.
   */
  public abstract HConstructor getConstructor(HClass parameterTypes[])
    throws NoSuchMethodError;

  /**
   * Returns an array of <code>HConstructor</code> objects reflecting
   * all the constructors declared by the class represented by the
   * <code>HClass</code> object.  These are <code>public</code>,
   * <code>protected</code>, default (package) access, and
   * <code>private</code> constructors.  Returns an array of length 0
   * if this <code>HClass</code> object represents an interface or a
   * primitive type.
   * @see "The Java Language Specification, section 8.2"
   */
  public abstract HConstructor[] getConstructors();

  /**
   * Returns the class initializer method, if there is one; otherwise
   * <code>null</code>.
   * @see "The Java Virtual Machine Specification, section 3.8"
   */
  public abstract HInitializer getClassInitializer();

  /**
   * Returns the Java language modifiers for this class or interface,
   * encoded in an integer.  The modifiers consist of the Java Virtual
   * Machine's constants for public, protected, private, final, and 
   * interface; they should be decoded using the methods of class Modifier.
   * @see "The Java Virtual Machine Specification, table 4.1"
   * @see java.lang.reflect.Modifier
   */
  public abstract int getModifiers();

  /**
   * If this object represents any class other than the class 
   * <code>Object</code>, then the object that represents the superclass of 
   * that class is returned. 
   * <p> If this object is the one that represents the class
   * <code>Object</code> or this object represents an interface, 
   * <code>null</code> is returned.
   * If this object represents an array, then the <code>HClass</code>
   * representing <code>java.lang.Object</code> is returned.
   * @return the superclass of the class represented by this object.
   */
  public abstract HClass getSuperclass();

  /**
   * Determines the interfaces implemented by the class or interface 
   * represented by this object. 
   * <p> If this object represents a class, the return value is an
   * array containing objects representing all interfaces implemented by 
   * the class.  The order of the interface objects in the array corresponds 
   * to the order of the interface names in the implements clause of the 
   * declaration of the class represented by this object.
   * <p> If the object represents an interface, the array contains objects
   * representing all interfaces extended by the interface.  The order of
   * the interface objects in the array corresponds to the order of the
   * interface names in the extends clause of the declaration of the 
   * interface represented by this object.
   * <p> If the class or interface implements no interfaces, the method
   * returns an array of length 0.
   * <p><b>NOTE THAT the array returned does NOT contain interfaces
   * implemented by superclasses.</b>  Thus the interface list may
   * be incomplete.  This is pretty bogus behaviour, but it's what
   * our prototype, <code>java.lang.Class</code>, does.
   * @return an array of interfaces implemented by this class.
   */
  public abstract HClass[] getInterfaces();


  /** Return the parents of this <code>HClass</code>.  The returned
   *  set contains this class'es superclass and the interfaces that
   *  this class implements.  This information is not transitive: we
   *  do not consider the superclass of the superclass, nor the
   *  interfaces extended by the directly implemented interfaces. */
  public final Set<HClass> parents() {
    // odd inheritance properties:
    //  interfaces: all instances of an interface are also instances of
    //              java.lang.Object, so root all interfaces there.
    //  arrays: Integer[][]->Number[][]->Object[][]->Object[]->Object
    //      but also Set[][]->Collection[][]->Object[][]->Object[]->Object
    //      (i.e. interfaces are just as rooted here)
    // note every use of this.getLinker() below is safe because c is
    // guaranteed non-primitive in every context.
    Linker linker = this.getLinker();
    HClass base = HClassUtil.baseClass(this);
    int dims = HClassUtil.dims(this);
    HClass su = base.getSuperclass();
    HClass[] interfaces = base.getInterfaces();
    boolean isObjArray = this.getDescriptor().endsWith("[Ljava/lang/Object;");
    boolean isPrimArray = this.isArray() && base.isPrimitive();
    // root interface inheritance hierarchy at Object.
    if (interfaces.length==0 && base.isInterface())
      su = linker.forName("java.lang.Object");// c not prim.
    // create return value array.
    HClass[] parents = new HClass[interfaces.length +
				 ((su!=null || isObjArray || isPrimArray)
				  ? 1 : 0)];
    int n=0;
    if (su!=null)
      parents[n++] = HClassUtil.arrayClass(linker, //c not prim.
					   su, dims);
    for (int i=0; i<interfaces.length; i++)
      parents[n++] = HClassUtil.arrayClass(linker, //c not prim.
					   interfaces[i], dims);
    // don't forget Object[][]->Object[]->Object
    // (but remember also Object[][]->Cloneable->Object)
    if (isObjArray)
      parents[n++] = HClassUtil.arrayClass(linker, base, dims-1);
    // also!  int[] -> Object.
    if (isPrimArray) // c not prim.
      parents[n++] = linker.forName("java.lang.Object");
    // okay, done.  Did we size the array correctly?
    assert n==parents.length;
    // okay, return as Set.
    return new ArraySet<HClass>(parents);
  }


  /**
   * Return the name of the source file for this class, or a
   * zero-length string if the information is not available.
   * @see harpoon.IR.RawClass.AttributeSourceFile
   */
  public abstract String getSourceFile();

  /**
   * If this <code>HClass</code> is a primitive type, return the
   * wrapper class for values of this type.  For example:<p>
   * <DL><DD><CODE>HClass.forDescriptor("I").getWrapper()</CODE></DL><p>
   * will return <code>l.forName("java.lang.Integer")</code>.
   * Calling <code>getWrapper</code> with a non-primitive <code>HClass</code>
   * will return the value <code>null</code>.
   */
  public final HClass getWrapper(Linker l) {
    if (this==this.Boolean) return l.forName("java.lang.Boolean");
    if (this==this.Byte)    return l.forName("java.lang.Byte");
    if (this==this.Char)    return l.forName("java.lang.Character");
    if (this==this.Double)  return l.forName("java.lang.Double");
    if (this==this.Float)   return l.forName("java.lang.Float");
    if (this==this.Int)     return l.forName("java.lang.Integer");
    if (this==this.Long)    return l.forName("java.lang.Long");
    if (this==this.Short)   return l.forName("java.lang.Short");
    if (this==this.Void)    return l.forName("java.lang.Void");
    return null; // not a primitive type;
  }

  /**
   * If this <code>HClass</code> object represents an array type, 
   * returns <code>true</code>, otherwise returns <code>false</code>.
   */
  public abstract boolean isArray();
  /**
   * Determines if the specified <code>HClass</code> object represents an
   * interface type.
   * @return <code>true</code> is this object represents an interface;
   *         <code>false</code> otherwise.
   */
  public abstract boolean isInterface();

  /**
   * Determines if the specified <code>HClass</code> object represents a
   * primitive Java type. <p>
   * There are nine predefined <code>HClass</code> objects to represent
   * the eight primitive Java types and void.
   */
  public abstract boolean isPrimitive();

  /**
   * Determines if the class or interface represented by this 
   * <code>HClass</code> object is either the same as, or is a superclass
   * or superinterface of, the class or interface represented by the 
   * specified <code>HClass</code> parameter.  It returns
   * <code>true</code> if so, <code>false</code> otherwise.  If this
   * <code>HClass</code> object represents a primitive type, returns
   * <code>true</code> if the specified <code>HClass</code> parameter is
   * exactly this <code>HClass</code> object, <code>false</code>
   * otherwise.
   * <p> Specifically, this method tests whether the type represented
   * by the specified <code>HClass</code> parameter can be converted
   * to the type represented by this <code>HClass</code> object via an
   * identity conversion or via a widening reference conversion.
   * @see "The Java Language Specification, sections 5.1.1 and 5.1.4"
   * @exception NullPointerException
   *            if the specified <code>HClass</code> parameter is null.
   */
  public final boolean isAssignableFrom(HClass cls) {
    assert _linker == cls._linker || isPrimitive() || cls.isPrimitive();
    if (cls==null) throw new NullPointerException();
    // test identity conversion.
    if (cls==this) return true;
    // widening reference conversions...
    if (this.isPrimitive()) return false;
    // widening reference conversions from the null type:
    if (cls==HClass.Void) return true;
    if (cls.isPrimitive()) return false;
    // widening reference conversions from an array:
    if (cls.isArray()) {
      if (this == _linker.forName("java.lang.Object")) return true;
      if (this == _linker.forName("java.lang.Cloneable")) return true;
      // see http://java.sun.com/docs/books/jls/clarify.html
      if (this == _linker.forName("java.io.Serializable")) return true;
      if (isArray() &&
	  !getComponentType().isPrimitive() &&
	  !cls.getComponentType().isPrimitive() &&
	  getComponentType().isAssignableFrom(cls.getComponentType()))
	return true;
      return false;
    }
    // widening reference conversions from an interface type.
    if (cls.isInterface()) {
      if (this.isInterface() && this.isSuperinterfaceOf(cls)) return true;
      if (this == _linker.forName("java.lang.Object")) return true;
      return false;
    }
    // widening reference conversions from a class type:
    if (!this.isInterface() && this.isSuperclassOf(cls)) return true;
    if (this.isInterface() && this.isSuperinterfaceOf(cls)) return true;
    return false;
  }

  /**
   * Determines if this <code>HClass</code> is a superclass of a given
   * <code>HClass</code> <code>hc</code>. 
   * [Does not look at interface information.]
   * @return <code>true</code> if <code>this</code> is a superclass of
   *         <code>hc</code>, <code>false</code> otherwise.
   */
  public final boolean isSuperclassOf(HClass hc) {
    assert _linker == hc._linker || isPrimitive() || hc.isPrimitive();
    assert !this.isInterface();
    for ( ; hc!=null; hc = hc.getSuperclass())
      if (this == hc) return true;
    return false;
  }

  /**
   * Determines if this <code>HClass</code> is a superinterface of a given
   * <code>HClass</code> <code>hc</code>. 
   * [does not look at superclass information]
   * @return <code>true</code> if <code>this</code> is a superinterface of
   *         <code>hc</code>, <code>false</code> otherwise.
   */
  public final boolean isSuperinterfaceOf(HClass hc) {
    assert _linker == hc._linker || isPrimitive() || hc.isPrimitive();
    assert this.isInterface();
    UniqueVector<HClass> uv =
      new UniqueVector<HClass>();//unique in case of circularity 
    for ( ; hc!=null; hc = hc.getSuperclass())
      uv.add(hc);

    for (int i=0; i<uv.size(); i++)
      if (uv.get(i) == this) return true;
      else {
	HClass in[] = uv.get(i).getInterfaces();
	for (int j=0; j<in.length; j++)
	  uv.add(in[j]);
      }
    // ran out of possibilities.
    return false;
  }

  /**
   * Determines if this <code>HClass</code> is an instance of the given
   * <code>HClass</code> <code>hc</code>.
   */
  public final boolean isInstanceOf(HClass hc) {
    assert _linker == hc._linker || isPrimitive() || hc.isPrimitive();
    if (this.isArray()) {
      if (!hc.isArray()) 
	// see http://java.sun.com/docs/books/jls/clarify.html
	return (hc==_linker.forName("java.lang.Cloneable") ||
		hc==_linker.forName("java.io.Serializable") ||
		hc==_linker.forName("java.lang.Object"));
      HClass SC = this.getComponentType();
      HClass TC = hc.getComponentType();
      return ((SC.isPrimitive() && TC.isPrimitive() && SC==TC) ||
	      (!SC.isPrimitive()&&!TC.isPrimitive() && SC.isInstanceOf(TC)));
    } else { // not array.
      if (hc.isInterface())
	return hc.isSuperinterfaceOf(this);
      else // hc is class.
	if (this.isInterface()) // in recursive eval of array instanceof.
	  return (hc==_linker.forName("java.lang.Object"));
	else return hc.isSuperclassOf(this);
    }
  }

  public int hashCode() { return getDescriptor().hashCode(); }
  /**
   * Converts the object to a string.  The string representation is the
   * string <code>"class"</code> or <code>"interface"</code> followed by
   * a space and then the fully qualified name of the class.  If this
   * <code>HClass</code> object represents a primitive type,
   * returns the name of the primitive type.
   * @return a string representation of this class object.
   */
  public final String toString() {
    return (isPrimitive()?"":isInterface()?"interface ":"class ")+getName();
  }

  /**
   * Prints a formatted representation of this class.
   * Output is pseudo-Java source.
   */
  public final void print(java.io.PrintWriter pw) {
    int m;
    // package declaration.
    if (getPackage()!=null && !getPackage().equals(""))
      pw.println("package " + getPackage() + ";");
    // class declaration.
    m = getModifiers() & (~32); // unset the ACC_SUPER flag.
    pw.println(((m==0)?"":(Modifier.toString(m) + " ")) + 
	       (isInterface()?"interface ":"class ") + 
	       getSimpleTypeName(this));
    // superclass
    HClass sup = getSuperclass();
    if ((sup != null) && (!sup.getName().equals("java.lang.Object")))
      pw.println("    extends " + getSimpleTypeName(sup));
    // interfaces
    HClass in[] = getInterfaces();
    if (in.length > 0) {
      if (isInterface())
	pw.print("    extends ");
      else
	pw.print("    implements ");
      for (int i=0; i<in.length; i++) {
	pw.print(getSimpleTypeName(in[i]));
	if (i<in.length-1)
	  pw.print(", ");
      }
      pw.println();
    }
    pw.println("{");
    // declared fields.
    HField hf[] = getDeclaredFields();
    for (int i=0; i<hf.length; i++) {
      m = hf[i].getModifiers();
      pw.println("    " + 
		 ((m==0)?"":(Modifier.toString(m)+" ")) + 
		 getSimpleTypeName(hf[i].getType()) + " " +
		 hf[i].getName() + ";");
    }
    // declared methods.
    HMethod hm[] = getDeclaredMethods();
    for (int i=0; i<hm.length; i++) {
      StringBuffer mstr = new StringBuffer("    ");
      m = hm[i].getModifiers();
      if (m!=0) {
	mstr.append(Modifier.toString(m));
	mstr.append(' ');
      }
      if (hm[i] instanceof HInitializer) {
	mstr.append("static {};");
	pw.println(mstr.toString());
	continue;
      }
      if (hm[i] instanceof HConstructor) {
	mstr.append(getSimpleTypeName(this));
      } else {
	mstr.append(getSimpleTypeName(hm[i].getReturnType()));
	mstr.append(' ');
	mstr.append(hm[i].getName());
      }
      mstr.append('(');
      HClass[] mpt = hm[i].getParameterTypes();
      String[] mpn = hm[i].getParameterNames();
      for (int j=0; j<mpt.length; j++) {
	mstr.append(getSimpleTypeName(mpt[j]));
	mstr.append(' ');
	// use appropriate formal parameter name.
	if (mpn[j]!=null)
	  mstr.append(mpn[j]);
	else { // don't know it; use generic.
	  mstr.append('p'); mstr.append(j);
	}
	if (j<mpt.length-1)
	  mstr.append(", ");
      }
      mstr.append(')');
      HClass[] met = hm[i].getExceptionTypes();
      if (met.length>0)
	mstr.append(" throws ");
      for (int j=0; j<met.length; j++) {
	mstr.append(getSimpleTypeName(met[j]));
	if (j<met.length-1)
	  mstr.append(", ");
      }
      mstr.append(';');
      pw.println(mstr.toString());
    }
    // done.
    pw.println("}");
  }
  private String getSimpleTypeName(HClass cls) {
    String tn = HClass.getTypeName(cls);
    while (cls.isArray()) cls=cls.getComponentType();
    if (cls.getPackage()!=null &&
	(cls.getPackage().equals(getPackage()) ||
	 cls.getPackage().equals("java.lang"))) {
      int lastdot = tn.lastIndexOf('.');
      if (lastdot < 0) return tn;
      else return tn.substring(lastdot+1);
    }
    else return tn;
  }

  /*****************************************************************/
  // JSR-14 extensions.

  /**
   * Returns the <code>HType</code>s representing the interfaces
   * implemented by the class or interface represented by this object.
   * <p>
   * If this object represents a class, the return value is an
   * array containing objects representing all interfaces implemented
   * by the class. The order of the interface objects in the array
   * corresponds to the order of the interface names in the
   * <code>implements</code> clause of the declaration of the class
   * represented by this object.  In the case of an array class, the
   * interfaces <code>Cloneable</code> and <code>Serializable</code> are
   * returned in that order.
   * <p>
   * If this object represents an interface, the array contains
   * objects representing all interfaces extended by the interface. The
   * order of the interface objects in the array corresponds to the order
   * of the interface names in the <code>extends</code> clause of the
   * declaration of the interface represented by this object.
   * <p>
   * If this object represents a class or interface that implements no
   * interfaces, the method returns an array of length 0.
   * <p>
   * If this object represents a primitive type or void, the method
   * returns an array of length 0.
   * <p>
   * In particular, if the compile-time type of any superinterface is
   * a parameterized type, than an object of the appropriate type
   * (i.e., <code>HParameterizedType</code>) will be returned.
   * @return an array of interfaces implemented by this class.
   */
  public HType[] getGenericInterfaces() {
    throw new RuntimeException("Unimplemented");
  }
  /**
   * Returns the <code>HType</code> representing the superclass of the
   * entity (class, interface, primitive type or void) represented by
   * this <code>HClass</code>.  If this <code>HClass</code> represents
   * either the <code>Object</code> class, an interface, a primitive
   * type, or void, then null is returned.  If this object represents
   * an array class then the <code>HClass</code> object representing
   * the <code>Object</code> class is returned.
   * <p>
   * In particular, if the compile-time superclass declaration is a
   * parameterized type, than an object of the appropriate type (i.e.,
   * <code>HParameterizedType</code>) will be returned.
   * @return the superclass of the class represented by this object.
   */
  public HType getGenericSuperclass() {
    throw new RuntimeException("Unimplemented");
  }
  /**
   * Returns an array of <code>HClassTypeVariable</code> objects that
   * represents the type variables declared by the class or interface
   * represented by this <code>HClass</code> object, in declaration
   * order.  Returns an array of length 0 if the underlying class or
   * interface declares no type variables. */
  public HClassTypeVariable[] getTypeParameters() {
    throw new RuntimeException("Unimplemented");
  }

  /*****************************************************************/
  // Special classes for primitive types.
  // [note that these cannot be re-linked as they are hard-coded here]

  /** The <code>HClass</code> object representing the primitive type boolean.*/
  public static final HClass Boolean=new HClassPrimitive("boolean", "Z");
  /** The <code>HClass</code> object representing the primitive type byte.*/
  public static final HClass Byte=new HClassPrimitive("byte", "B");
  /** The <code>HClass</code> object representing the primitive type short.*/
  public static final HClass Short=new HClassPrimitive("short", "S");
  /** The <code>HClass</code> object representing the primitive type int.*/
  public static final HClass Int=new HClassPrimitive("int", "I");
  /** The <code>HClass</code> object representing the primitive type long.*/
  public static final HClass Long=new HClassPrimitive("long", "J");
  /** The <code>HClass</code> object representing the primitive type float.*/
  public static final HClass Float=new HClassPrimitive("float", "F");
  /** The <code>HClass</code> object representing the primitive type double.*/
  public static final HClass Double=new HClassPrimitive("double", "D");
  /** The <code>HClass</code> object representing the primitive type char.*/
  public static final HClass Char=new HClassPrimitive("char", "C");
  /** The <code>HClass</code> object representing the primitive type void.*/
  public static final HClass Void=new HClassPrimitive("void", "V");

  /** Array factory: returns new <code>HClass[]</code>. */
  public static final ArrayFactory<HClass> arrayFactory = Factories.hclassArrayFactory;

  /** HPointer interface. */
  final HClass actual() { return this; /* no dereferencing necessary. */ }

  // Comparable interface.
  /** Compares two <code>HClass</code>es by lexicographic order of their
   *  descriptors. */
  public final int compareTo(HClass o) {
    return getDescriptor().compareTo(o.getDescriptor());
  }

  // UTILITY CLASSES (used in toString methods all over the place)
  static String getTypeName(HPointer hc) {
    HClass hcc;
    try { hcc = (HClass) hc; }
    catch (ClassCastException e) {
      return (hc.getDescriptor().charAt(0)=='L') ?
	hc.getName() : getTypeName(hc.actual());
    }
    return getTypeName(hcc);
  }
  static String getTypeName(HClass hc) {
    if (hc.isArray()) {
      StringBuffer r = new StringBuffer();
      HClass sup = hc;
      int i=0;
      for (; sup.isArray(); sup = sup.getComponentType())
	i++;
      r.append(sup.getName());
      for (int j=0; j<i; j++)
	r.append("[]");
      return r.toString();
    }
    return hc.getName();
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
