// AccessFlags.java, created Mon Jan 18 22:44:33 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/** 
 * Represents a set of method or field access flags, containing
 * permissions and properties of a field or method.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AccessFlags.java,v 1.2 2002-02-25 21:05:22 cananian Exp $
 * @see "The Java Virtual Machine Specification"
 * @see ClassFile
 * @see FieldInfo
 * @see MethodInfo
 */
public class AccessFlags {
  /** May be accessed from outside package. */
  static final int ACC_PUBLIC  = 0x001;
  /** Usable only within defining class. */
  static final int ACC_PRIVATE = 0x002;
  /** May be accessed withing subclasses. */
  static final int ACC_PROTECTED=0x004;
  /** Is static. */
  static final int ACC_STATIC  = 0x008;
  /** No further assignment after init. */
  static final int ACC_FINAL   = 0x010;
  /** Wrap use in monitor lock. */
  static final int ACC_SYNCHRON= 0x020;
  /** Use new superclass semantics. */
  static final int ACC_SUPER   = 0x020;
  /** Is volatile; cannot be cached. */
  static final int ACC_VOLATILE= 0x040;
  /** Not touched by persistent object manager. */
  static final int ACC_TRANSIENT=0x080;
  /** Not implemented in Java. */
  static final int ACC_NATIVE   =0x100;
  /** Is an interface. */
  static final int ACC_INTERFACE=0x200;
  /** No implementation is provided. */
  static final int ACC_ABSTRACT =0x400;

  public int access_flags;

  /** Constructor. */
  public AccessFlags(int flags) {
    access_flags = flags;
  }
  /** Constructor. */
  public AccessFlags(ClassDataInputStream in) throws java.io.IOException {
    this(in.read_u2());
  }
  /** Write to bytecode file. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u2(access_flags);
  }

  public boolean isPublic()   { return (access_flags & ACC_PUBLIC) != 0; }
  public boolean isPrivate()  { return (access_flags & ACC_PRIVATE) != 0; }
  public boolean isProtected(){ return (access_flags & ACC_PROTECTED) != 0; }
  public boolean isStatic()   { return (access_flags & ACC_STATIC) != 0; }
  public boolean isFinal()    { return (access_flags & ACC_FINAL) != 0; }
  public boolean isVolatile() { return (access_flags & ACC_VOLATILE) != 0; }
  public boolean isTransient(){ return (access_flags & ACC_TRANSIENT) != 0; }
  public boolean isSynchronized(){return(access_flags& ACC_SYNCHRON) != 0; }
  public boolean isNative()   { return (access_flags & ACC_NATIVE) != 0; }
  public boolean isAbstract() { return (access_flags & ACC_ABSTRACT) != 0; }
  public boolean isSuper()    { return (access_flags & ACC_SUPER) != 0; }
  public boolean isInterface(){ return (access_flags & ACC_INTERFACE) != 0; }

  // ClassFile uses:  PUBLIC, FINAL, SUPER, INTERFACE, ABSTRACT.
  // FieldInfo uses:  PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, 
  //                  VOLATILE, TRANSIENT.
  // MethodInfo uses: PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL,
  //                  SYNCHRONIZED, NATIVE, ABSTRACT.

  /**
   * Returns a string with the access flags in canonical order.
   * Omits ACC_SUPER and ACC_INTERFACE.
   */
  public String toString() {
    StringBuffer r = new StringBuffer();
    if (isPublic()) r.append("public ");
    if (isProtected()) r.append("protected ");
    if (isPrivate()) r.append("private ");
    if (isAbstract()) r.append("abstract ");
    if (isStatic()) r.append("static ");
    if (isFinal()) r.append("final ");
    if (isTransient()) r.append("transient ");
    if (isVolatile()) r.append("volatile ");
    if (isSynchronized()) r.append("synchronized ");
    if (isNative()) r.append("native ");
    return r.toString().trim();
  }
}
